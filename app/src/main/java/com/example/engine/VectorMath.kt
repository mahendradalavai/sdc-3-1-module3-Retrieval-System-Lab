package com.example.engine

import com.example.data.model.SimilarityBreakdown
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object VectorMath {

    fun parseCsvToVector(csv: String): FloatArray {
        if (csv.isBlank()) return FloatArray(0)
        return try {
            csv.split(",")
                .mapNotNull { it.trim().toFloatOrNull() }
                .toFloatArray()
        } catch (e: Exception) {
            FloatArray(0)
        }
    }

    fun vectorToCsv(vec: FloatArray): String {
        return vec.joinToString(",") { "%.5f".format(it) }
    }

    fun dotProduct(vecA: FloatArray, vecB: FloatArray): Float {
        val size = min(vecA.size, vecB.size)
        var sum = 0f
        for (i in 0 until size) {
            sum += vecA[i] * vecB[i]
        }
        return sum
    }

    fun l2Norm(vec: FloatArray): Float {
        var sumSq = 0f
        for (v in vec) {
            sumSq += v * v
        }
        return sqrt(sumSq)
    }

    fun normalizeL2(vec: FloatArray): FloatArray {
        val norm = l2Norm(vec)
        if (norm == 0f) return vec.copyOf()
        val result = FloatArray(vec.size)
        for (i in vec.indices) {
            result[i] = vec[i] / norm
        }
        return result
    }

    fun euclideanDistance(vecA: FloatArray, vecB: FloatArray): Float {
        val size = min(vecA.size, vecB.size)
        var sumSq = 0f
        for (i in 0 until size) {
            val diff = vecA[i] - vecB[i]
            sumSq += diff * diff
        }
        return sqrt(sumSq)
    }

    fun calculateCosineSimilarity(vecA: FloatArray, vecB: FloatArray): SimilarityBreakdown {
        if (vecA.isEmpty() || vecB.isEmpty()) {
            return SimilarityBreakdown(
                cosineSimilarity = 0f,
                dotProduct = 0f,
                queryNorm = 0f,
                docNorm = 0f,
                angleDegrees = 90f
            )
        }

        val size = min(vecA.size, vecB.size)
        var dot = 0f
        var normASq = 0f
        var normBSq = 0f
        val dimensionContributions = ArrayList<Pair<Int, Float>>(size)

        for (i in 0 until size) {
            val a = vecA[i]
            val b = vecB[i]
            val prod = a * b
            dot += prod
            normASq += a * a
            normBSq += b * b
            dimensionContributions.add(Pair(i, prod))
        }

        val normA = sqrt(normASq)
        val normB = sqrt(normBSq)
        val denom = normA * normB

        val rawSim = if (denom > 1e-8f) (dot / denom) else 0f
        val sim = rawSim.coerceIn(-1.0f, 1.0f)

        // Angle theta in degrees: arccos(similarity)
        val angleRad = acos(sim.toDouble())
        val angleDeg = Math.toDegrees(angleRad).toFloat()

        // Sort dimensions by absolute magnitude of contribution
        dimensionContributions.sortByDescending { abs(it.second) }

        return SimilarityBreakdown(
            cosineSimilarity = sim,
            dotProduct = dot,
            queryNorm = normA,
            docNorm = normB,
            angleDegrees = angleDeg,
            topContributingDimensions = dimensionContributions.take(6),
            sampleDimensionsQuery = vecA.take(8),
            sampleDimensionsDoc = vecB.take(8)
        )
    }

    /**
     * Projects a list of N-dimensional vectors onto 2D space (x, y in [-1, 1])
     * using PCA (Principal Component Analysis with power iteration).
     */
    fun projectTo2D(vectors: List<FloatArray>): List<Pair<Float, Float>> {
        if (vectors.isEmpty()) return emptyList()
        val dim = vectors.first().size
        if (dim < 2) {
            return vectors.map { Pair(it.getOrElse(0) { 0f }, 0f) }
        }

        // 1. Calculate centroid (mean vector)
        val mean = FloatArray(dim)
        for (vec in vectors) {
            val s = min(dim, vec.size)
            for (i in 0 until s) {
                mean[i] += vec[i]
            }
        }
        val count = vectors.size.toFloat()
        for (i in 0 until dim) {
            mean[i] /= count
        }

        // 2. Mean-centered vectors
        val centered = vectors.map { vec ->
            val c = FloatArray(dim)
            val s = min(dim, vec.size)
            for (i in 0 until s) {
                c[i] = vec[i] - mean[i]
            }
            c
        }

        // 3. Power iteration for 1st Principal Component (PC1)
        var pc1 = FloatArray(dim) { 1f }
        pc1 = normalizeL2(pc1)
        for (iter in 0 until 15) {
            val next = FloatArray(dim)
            for (v in centered) {
                val proj = dotProduct(v, pc1)
                for (i in 0 until dim) {
                    next[i] += proj * v[i]
                }
            }
            val norm = l2Norm(next)
            if (norm > 1e-6f) {
                pc1 = normalizeL2(next)
            }
        }

        // 4. Power iteration for 2nd Principal Component (PC2), orthogonal to PC1
        var pc2 = FloatArray(dim) { if (it % 2 == 0) 1f else -1f }
        for (iter in 0 until 15) {
            // Deflate / orthogonalize against pc1
            val proj1 = dotProduct(pc2, pc1)
            for (i in 0 until dim) {
                pc2[i] -= proj1 * pc1[i]
            }
            pc2 = normalizeL2(pc2)

            val next = FloatArray(dim)
            for (v in centered) {
                val proj = dotProduct(v, pc2)
                for (i in 0 until dim) {
                    next[i] += proj * v[i]
                }
            }
            val norm = l2Norm(next)
            if (norm > 1e-6f) {
                pc2 = normalizeL2(next)
            }
        }

        // 5. Project each vector onto pc1 and pc2
        val raw2D = centered.map { v ->
            val x = dotProduct(v, pc1)
            val y = dotProduct(v, pc2)
            Pair(x, y)
        }

        // 6. Scale and normalize to fit nicely in [-0.9, 0.9]
        var maxAbsX = 0.001f
        var maxAbsY = 0.001f
        for (pt in raw2D) {
            maxAbsX = max(maxAbsX, abs(pt.first))
            maxAbsY = max(maxAbsY, abs(pt.second))
        }

        return raw2D.map { (x, y) ->
            Pair(
                ((x / maxAbsX) * 0.85f).coerceIn(-0.95f, 0.95f),
                ((y / maxAbsY) * 0.85f).coerceIn(-0.95f, 0.95f)
            )
        }
    }
}
