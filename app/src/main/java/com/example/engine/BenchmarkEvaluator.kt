package com.example.engine

import com.example.data.model.DocumentEntity
import com.example.data.model.RetrievalResult

data class SingleBenchmarkResult(
    val query: BenchmarkQuery,
    val topResults: List<RetrievalResult>,
    val hitAt1: Boolean,
    val hitAt3: Boolean,
    val reciprocalRank: Float,
    val bestMatchTitle: String,
    val bestMatchScore: Float
)

data class BenchmarkSummary(
    val totalQueries: Int,
    val precisionAt1: Float, // Fraction where rank 1 matched
    val precisionAt3: Float, // Fraction where top 3 contained a match
    val meanReciprocalRank: Float, // MRR
    val queryResults: List<SingleBenchmarkResult>
)

class BenchmarkEvaluator(
    private val localEngine: LocalEmbeddingEngine
) {

    suspend fun runEvaluation(
        corpus: List<DocumentEntity>,
        embeddingType: String
    ): BenchmarkSummary {
        val queryResults = mutableListOf<SingleBenchmarkResult>()
        var hit1Count = 0
        var hit3Count = 0
        var sumReciprocalRank = 0f

        val queries = DefaultCorpus.BENCHMARK_QUERIES

        for (bQuery in queries) {
            val queryVec = if (embeddingType == "GEMINI" && GeminiClient.isApiKeyConfigured()) {
                val res = GeminiClient.fetchEmbedding(bQuery.queryText)
                res.getOrNull() ?: localEngine.generateEmbedding(bQuery.queryText)
            } else {
                localEngine.generateEmbedding(bQuery.queryText)
            }

            val ranked = corpus.map { doc ->
                val docVec = VectorMath.parseCsvToVector(doc.embeddingCsv)
                val breakdown = VectorMath.calculateCosineSimilarity(queryVec, docVec)
                RetrievalResult(
                    document = doc,
                    rank = 0,
                    similarityBreakdown = breakdown
                )
            }.sortedByDescending { it.score }
                .mapIndexed { idx, res -> res.copy(rank = idx + 1) }

            val top1 = ranked.firstOrNull()
            val top3 = ranked.take(3)

            val hit1 = top1?.document?.category.equals(bQuery.targetCategory, ignoreCase = true)
            val hit3 = top3.any { it.document.category.equals(bQuery.targetCategory, ignoreCase = true) }

            val firstRelevantIdx = ranked.indexOfFirst {
                it.document.category.equals(bQuery.targetCategory, ignoreCase = true)
            }
            val rr = if (firstRelevantIdx >= 0) 1.0f / (firstRelevantIdx + 1) else 0f

            if (hit1) hit1Count++
            if (hit3) hit3Count++
            sumReciprocalRank += rr

            queryResults.add(
                SingleBenchmarkResult(
                    query = bQuery,
                    topResults = top3,
                    hitAt1 = hit1,
                    hitAt3 = hit3,
                    reciprocalRank = rr,
                    bestMatchTitle = top1?.document?.title ?: "N/A",
                    bestMatchScore = top1?.score ?: 0f
                )
            )
        }

        val count = queries.size.toFloat()
        return BenchmarkSummary(
            totalQueries = queries.size,
            precisionAt1 = if (count > 0) hit1Count / count else 0f,
            precisionAt3 = if (count > 0) hit3Count / count else 0f,
            meanReciprocalRank = if (count > 0) sumReciprocalRank / count else 0f,
            queryResults = queryResults
        )
    }
}
