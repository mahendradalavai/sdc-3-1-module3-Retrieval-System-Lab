package com.example

import com.example.engine.LocalEmbeddingEngine
import com.example.engine.VectorMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    @Test
    fun testVectorMathCosineSimilarityIdenticalVectors() {
        val vecA = floatArrayOf(1f, 0f, 0f)
        val vecB = floatArrayOf(1f, 0f, 0f)
        val result = VectorMath.calculateCosineSimilarity(vecA, vecB)

        assertEquals(1.0f, result.cosineSimilarity, 0.001f)
        assertEquals(0.0f, result.angleDegrees, 0.001f)
        assertEquals(1.0f, result.dotProduct, 0.001f)
    }

    @Test
    fun testVectorMathCosineSimilarityOrthogonalVectors() {
        val vecA = floatArrayOf(1f, 0f, 0f)
        val vecB = floatArrayOf(0f, 1f, 0f)
        val result = VectorMath.calculateCosineSimilarity(vecA, vecB)

        assertEquals(0.0f, result.cosineSimilarity, 0.001f)
        assertEquals(90.0f, result.angleDegrees, 0.001f)
        assertEquals(0.0f, result.dotProduct, 0.001f)
    }

    @Test
    fun testVectorMathCosineSimilarityOppositeVectors() {
        val vecA = floatArrayOf(1f, 0f)
        val vecB = floatArrayOf(-1f, 0f)
        val result = VectorMath.calculateCosineSimilarity(vecA, vecB)

        assertEquals(-1.0f, result.cosineSimilarity, 0.001f)
        assertEquals(180.0f, result.angleDegrees, 0.001f)
    }

    @Test
    fun testLocalEmbeddingEngineRelevanceRanking() {
        val engine = LocalEmbeddingEngine()

        val query = "deep space telescope distant galaxy optics"
        val relevantDoc = "The James Webb Space Telescope uses infrared mirrors to observe primordial galaxies and distant stars."
        val irrelevantDoc = "Sourdough bread baking requires wild yeast fermentation and kneading gluten dough."

        val qVec = engine.generateEmbedding(query)
        val relVec = engine.generateEmbedding(relevantDoc)
        val irrelVec = engine.generateEmbedding(irrelevantDoc)

        val simRelevant = VectorMath.calculateCosineSimilarity(qVec, relVec).cosineSimilarity
        val simIrrelevant = VectorMath.calculateCosineSimilarity(qVec, irrelVec).cosineSimilarity

        assertTrue(
            "Relevant doc similarity ($simRelevant) should be significantly greater than irrelevant doc ($simIrrelevant)",
            simRelevant > simIrrelevant + 0.2f
        )
    }

    @Test
    fun testVectorMathNormalizeL2() {
        val vec = floatArrayOf(3f, 4f)
        val normalized = VectorMath.normalizeL2(vec)
        val norm = VectorMath.l2Norm(normalized)

        assertEquals(1.0f, norm, 0.001f)
        assertEquals(0.6f, normalized[0], 0.001f)
        assertEquals(0.8f, normalized[1], 0.001f)
    }
}
