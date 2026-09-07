package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DocumentEntity
import com.example.data.model.RetrievalResult
import com.example.data.repository.DocumentRepository
import com.example.engine.BenchmarkEvaluator
import com.example.engine.BenchmarkSummary
import com.example.engine.DefaultCorpus
import com.example.engine.GeminiClient
import com.example.engine.LocalEmbeddingEngine
import com.example.engine.VectorMath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectedNode(
    val id: Int,
    val title: String,
    val category: String,
    val x: Float, // -1f to 1f
    val y: Float, // -1f to 1f
    val similarityToQuery: Float = 0f,
    val isQuery: Boolean = false
)

data class RetrievalUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val searchDurationMs: Long = 0,
    val corpus: List<DocumentEntity> = emptyList(),
    val rankedResults: List<RetrievalResult> = emptyList(),
    val threshold: Float = 0.05f,
    val topK: Int = 5,
    val selectedCategory: String? = null,
    val embeddingType: String = "LOCAL", // "LOCAL" or "GEMINI"
    val isGeminiKeyConfigured: Boolean = false,
    val queryVector: FloatArray = FloatArray(0),
    val selectedResultForMath: RetrievalResult? = null,
    val projectedNodes: List<ProjectedNode> = emptyList(),
    val benchmarkSummary: BenchmarkSummary? = null,
    val isBenchmarking: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class RetrievalViewModel(application: Application) : AndroidViewModel(application) {

    private val localEngine = LocalEmbeddingEngine()
    private val database = AppDatabase.getDatabase(application)
    private val repository = DocumentRepository(database.documentDao(), localEngine)
    private val benchmarkEvaluator = BenchmarkEvaluator(localEngine)

    private val _uiState = MutableStateFlow(
        RetrievalUiState(
            isGeminiKeyConfigured = GeminiClient.isApiKeyConfigured()
        )
    )
    val uiState: StateFlow<RetrievalUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureCorpusInitialized()
            repository.allDocuments.collect { docs ->
                _uiState.update { it.copy(corpus = docs) }
                if (_uiState.value.query.isNotBlank()) {
                    performRetrieval(_uiState.value.query)
                } else {
                    computeDefaultProjections(docs)
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(150) // smooth debounce
            if (newQuery.isBlank()) {
                _uiState.update {
                    it.copy(
                        rankedResults = emptyList(),
                        queryVector = FloatArray(0),
                        isSearching = false
                    )
                }
                computeDefaultProjections(_uiState.value.corpus)
            } else {
                performRetrieval(newQuery)
            }
        }
    }

    fun executeQueryImmediately(queryText: String) {
        _uiState.update { it.copy(query = queryText) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performRetrieval(queryText)
        }
    }

    private suspend fun performRetrieval(queryText: String) {
        val startTime = System.currentTimeMillis()
        _uiState.update { it.copy(isSearching = true, errorMessage = null) }

        val type = _uiState.value.embeddingType
        val queryVecResult = if (type == "GEMINI" && GeminiClient.isApiKeyConfigured()) {
            val res = GeminiClient.fetchEmbedding(queryText)
            res.getOrElse {
                _uiState.update { s ->
                    s.copy(
                        statusMessage = "Gemini API failed (${it.localizedMessage ?: "error"}), falling back to Local Engine."
                    )
                }
                localEngine.generateEmbedding(queryText)
            }
        } else {
            localEngine.generateEmbedding(queryText)
        }

        val allDocs = _uiState.value.corpus
        val ranked = allDocs.map { doc ->
            val docVec = VectorMath.parseCsvToVector(doc.embeddingCsv)
            val breakdown = VectorMath.calculateCosineSimilarity(queryVecResult, docVec)
            RetrievalResult(
                document = doc,
                rank = 0,
                similarityBreakdown = breakdown
            )
        }.sortedByDescending { it.score }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }

        val duration = System.currentTimeMillis() - startTime

        // Compute 2D PCA projection of all docs + query
        val vectorsForProjection = mutableListOf<FloatArray>()
        for (item in ranked) {
            vectorsForProjection.add(VectorMath.parseCsvToVector(item.document.embeddingCsv))
        }
        vectorsForProjection.add(queryVecResult) // Query is the last element

        val coords = VectorMath.projectTo2D(vectorsForProjection)
        val docNodes = ranked.mapIndexed { idx, res ->
            val pt = coords.getOrElse(idx) { Pair(0f, 0f) }
            ProjectedNode(
                id = res.document.id,
                title = res.document.title,
                category = res.document.category,
                x = pt.first,
                y = pt.second,
                similarityToQuery = res.score,
                isQuery = false
            )
        }

        val queryCoord = coords.lastOrNull() ?: Pair(0f, 0f)
        val queryNode = ProjectedNode(
            id = -1,
            title = "Query: \"$queryText\"",
            category = "Search Query",
            x = queryCoord.first,
            y = queryCoord.second,
            similarityToQuery = 1.0f,
            isQuery = true
        )

        _uiState.update {
            it.copy(
                isSearching = false,
                searchDurationMs = duration,
                queryVector = queryVecResult,
                rankedResults = ranked,
                projectedNodes = listOf(queryNode) + docNodes
            )
        }
    }

    private fun computeDefaultProjections(docs: List<DocumentEntity>) {
        if (docs.isEmpty()) return
        val vectors = docs.map { VectorMath.parseCsvToVector(it.embeddingCsv) }
        val coords = VectorMath.projectTo2D(vectors)
        val nodes = docs.mapIndexed { idx, doc ->
            val pt = coords.getOrElse(idx) { Pair(0f, 0f) }
            ProjectedNode(
                id = doc.id,
                title = doc.title,
                category = doc.category,
                x = pt.first,
                y = pt.second,
                similarityToQuery = 0f,
                isQuery = false
            )
        }
        _uiState.update { it.copy(projectedNodes = nodes) }
    }

    fun setThreshold(threshold: Float) {
        _uiState.update { it.copy(threshold = threshold) }
    }

    fun setTopK(k: Int) {
        _uiState.update { it.copy(topK = k) }
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun switchEmbeddingType(type: String) {
        if (type == _uiState.value.embeddingType) return
        _uiState.update { it.copy(embeddingType = type, statusMessage = "Updating embeddings with $type engine...") }
        viewModelScope.launch {
            repository.recomputeAllEmbeddings(type)
            if (_uiState.value.query.isNotBlank()) {
                performRetrieval(_uiState.value.query)
            }
            _uiState.update { it.copy(statusMessage = "Engine switched to $type") }
        }
    }

    fun selectResultForMath(result: RetrievalResult?) {
        _uiState.update { it.copy(selectedResultForMath = result) }
    }

    fun dismissMathDialog() {
        _uiState.update { it.copy(selectedResultForMath = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    fun addCustomDocument(title: String, content: String, category: String) {
        viewModelScope.launch {
            try {
                repository.addDocument(
                    title = title,
                    content = content,
                    category = category,
                    embeddingType = _uiState.value.embeddingType
                )
                _uiState.update { it.copy(statusMessage = "Added \"$title\" to corpus.") }
                if (_uiState.value.query.isNotBlank()) {
                    performRetrieval(_uiState.value.query)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to add document: ${e.message}") }
            }
        }
    }

    fun deleteDocument(id: Int) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            if (_uiState.value.query.isNotBlank()) {
                performRetrieval(_uiState.value.query)
            }
        }
    }

    fun resetCorpus() {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Resetting corpus to default benchmark...") }
            repository.resetToDefaultCorpus(_uiState.value.embeddingType)
            if (_uiState.value.query.isNotBlank()) {
                performRetrieval(_uiState.value.query)
            }
            _uiState.update { it.copy(statusMessage = "Corpus reset complete.") }
        }
    }

    fun runBenchmark() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBenchmarking = true) }
            val summary = benchmarkEvaluator.runEvaluation(
                corpus = _uiState.value.corpus,
                embeddingType = _uiState.value.embeddingType
            )
            _uiState.update {
                it.copy(
                    isBenchmarking = false,
                    benchmarkSummary = summary
                )
            }
        }
    }
}
