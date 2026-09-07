package com.example.data.model

data class SimilarityBreakdown(
    val cosineSimilarity: Float,
    val dotProduct: Float,
    val queryNorm: Float,
    val docNorm: Float,
    val angleDegrees: Float,
    val topContributingDimensions: List<Pair<Int, Float>> = emptyList(),
    val sampleDimensionsQuery: List<Float> = emptyList(),
    val sampleDimensionsDoc: List<Float> = emptyList()
)

data class RetrievalResult(
    val document: DocumentEntity,
    val rank: Int,
    val similarityBreakdown: SimilarityBreakdown
) {
    val score: Float get() = similarityBreakdown.cosineSimilarity
    val scorePercentage: Int get() = ((similarityBreakdown.cosineSimilarity.coerceIn(0f, 1f)) * 100).toInt()
}
