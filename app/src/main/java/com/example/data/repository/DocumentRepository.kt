package com.example.data.repository

import com.example.data.dao.DocumentDao
import com.example.data.model.DocumentEntity
import com.example.engine.DefaultCorpus
import com.example.engine.GeminiClient
import com.example.engine.LocalEmbeddingEngine
import com.example.engine.VectorMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val localEngine: LocalEmbeddingEngine
) {

    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocumentsFlow()

    suspend fun ensureCorpusInitialized() = withContext(Dispatchers.IO) {
        val count = documentDao.getDocumentCount()
        if (count == 0) {
            resetToDefaultCorpus("LOCAL")
        }
    }

    suspend fun resetToDefaultCorpus(embeddingType: String): List<DocumentEntity> = withContext(Dispatchers.IO) {
        documentDao.deleteAllDocuments()
        val entities = DefaultCorpus.DOCUMENTS.map { item ->
            val embedding = if (embeddingType == "GEMINI" && GeminiClient.isApiKeyConfigured()) {
                val result = GeminiClient.fetchEmbedding("${item.title} ${item.content}")
                result.getOrNull() ?: localEngine.generateEmbedding("${item.title} ${item.content}")
            } else {
                localEngine.generateEmbedding("${item.title} ${item.content}")
            }

            DocumentEntity(
                title = item.title,
                content = item.content,
                category = item.category,
                embeddingCsv = VectorMath.vectorToCsv(embedding),
                embeddingType = embeddingType
            )
        }
        documentDao.insertDocuments(entities)
        documentDao.getAllDocuments()
    }

    suspend fun addDocument(
        title: String,
        content: String,
        category: String,
        embeddingType: String
    ): DocumentEntity = withContext(Dispatchers.IO) {
        val fullText = "$title $content"
        val embedding = if (embeddingType == "GEMINI" && GeminiClient.isApiKeyConfigured()) {
            val res = GeminiClient.fetchEmbedding(fullText)
            res.getOrNull() ?: localEngine.generateEmbedding(fullText)
        } else {
            localEngine.generateEmbedding(fullText)
        }

        val entity = DocumentEntity(
            title = title,
            content = content,
            category = category,
            embeddingCsv = VectorMath.vectorToCsv(embedding),
            embeddingType = embeddingType
        )
        val id = documentDao.insertDocument(entity)
        entity.copy(id = id.toInt())
    }

    suspend fun deleteDocument(id: Int) = withContext(Dispatchers.IO) {
        documentDao.deleteDocumentById(id)
    }

    suspend fun recomputeAllEmbeddings(targetType: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val docs = documentDao.getAllDocuments()
            val updated = docs.map { doc ->
                val fullText = "${doc.title} ${doc.content}"
                val vec = if (targetType == "GEMINI" && GeminiClient.isApiKeyConfigured()) {
                    val res = GeminiClient.fetchEmbedding(fullText)
                    res.getOrNull() ?: localEngine.generateEmbedding(fullText)
                } else {
                    localEngine.generateEmbedding(fullText)
                }
                doc.copy(
                    embeddingCsv = VectorMath.vectorToCsv(vec),
                    embeddingType = targetType
                )
            }
            documentDao.insertDocuments(updated)
            Result.success(updated.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
