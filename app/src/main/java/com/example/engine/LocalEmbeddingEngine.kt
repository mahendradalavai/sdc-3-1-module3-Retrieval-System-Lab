package com.example.engine

import java.util.Locale
import kotlin.math.ln
import kotlin.math.sin

class LocalEmbeddingEngine {

    companion object {
        const val EMBEDDING_DIM = 64

        // Stopwords to ignore
        private val STOPWORDS = setOf(
            "a", "an", "the", "and", "or", "but", "if", "then", "else", "when",
            "at", "by", "for", "with", "about", "against", "between", "into",
            "through", "during", "before", "after", "above", "below", "to",
            "from", "up", "down", "in", "out", "on", "off", "over", "under",
            "is", "are", "was", "were", "be", "been", "being", "have", "has",
            "had", "do", "does", "did", "it", "its", "they", "them", "their",
            "this", "that", "these", "those", "can", "will", "just", "should"
        )

        // Domain clusters that anchor semantic dimensions for realistic semantic retrieval
        private val DOMAIN_KEYWORDS = mapOf(
            // Dimensions 0..11: AI, Machine Learning, Neural Networks
            0 to listOf(
                "ai", "artificial", "intelligence", "neural", "network", "deep", "learning",
                "model", "transformer", "attention", "token", "tensor", "algorithm", "llm",
                "gradient", "weights", "inference", "training", "compute", "backpropagation",
                "vector", "embedding", "nlp", "vision", "latent", "parameter"
            ),
            // Dimensions 12..23: Astronomy, Space, Astrophysics
            12 to listOf(
                "telescope", "space", "galaxy", "galaxies", "astronomy", "astrophysics",
                "universe", "star", "stars", "planet", "planets", "cosmic", "orbit", "hubble",
                "webb", "blackhole", "lightyear", "photon", "observatory", "gravity", "nebula",
                "radiation", "supernova", "interstellar", "exoplanet"
            ),
            // Dimensions 24..35: Culinary, Baking, Fermentation Science
            24 to listOf(
                "sourdough", "bread", "yeast", "fermentation", "baking", "flour", "dough",
                "gluten", "crust", "crumb", "bacteria", "lactobacillus", "acid", "temperature",
                "oven", "culinary", "flavor", "hydration", "starter", "artisan", "carbohydrate"
            ),
            // Dimensions 36..47: Renewable Energy, Climate & Solar
            36 to listOf(
                "solar", "photovoltaic", "energy", "renewable", "wind", "turbine", "battery",
                "grid", "electricity", "silicon", "clean", "megawatt", "kilowatt", "inverter",
                "decarbonization", "storage", "efficiency", "climate", "power", "generator"
            ),
            // Dimensions 48..59: Biomedicine, Genetics & Healthcare
            48 to listOf(
                "dna", "rna", "protein", "cell", "crispr", "gene", "genetic", "vaccine",
                "immune", "antibody", "sequencing", "clinical", "biomedical", "pathogen",
                "therapeutic", "molecular", "enzyme", "mutation", "virus", "therapy"
            )
        )
    }

    /**
     * Converts arbitrary text into a 64-dimensional dense semantic embedding vector.
     */
    fun generateEmbedding(text: String): FloatArray {
        val vector = FloatArray(EMBEDDING_DIM)
        if (text.isBlank()) return vector

        val tokens = tokenize(text)
        if (tokens.isEmpty()) return vector

        val termFrequencies = HashMap<String, Int>()
        for (token in tokens) {
            termFrequencies[token] = (termFrequencies[token] ?: 0) + 1
        }

        // 1. Process Domain Anchors
        for ((baseDim, keywords) in DOMAIN_KEYWORDS) {
            var domainMatchWeight = 0f
            for (keyword in keywords) {
                val count = termFrequencies[keyword] ?: 0
                if (count > 0) {
                    domainMatchWeight += (1f + ln(count.toFloat())) * 2.5f
                } else {
                    // Check stem prefix
                    for ((token, freq) in termFrequencies) {
                        if (token.length >= 4 && keyword.startsWith(token.take(4))) {
                            domainMatchWeight += (1f + ln(freq.toFloat())) * 1.2f
                        }
                    }
                }
            }

            if (domainMatchWeight > 0f) {
                // Distribute weight across the 12 domain dimensions using sinusoidal projection
                for (offset in 0 until 12) {
                    val dim = baseDim + offset
                    val phase = (offset.toFloat() / 12f) * Math.PI.toFloat() * 2f
                    val wave = (sin(phase) * 0.5f + 0.5f) + 0.2f
                    vector[dim] += domainMatchWeight * wave
                }
            }
        }

        // 2. Dense Feature Hashing for vocabulary and character 3-grams
        for ((token, freq) in termFrequencies) {
            val tfWeight = 1f + ln(freq.toFloat())
            val wordHash = fnv1a(token)
            val dimIndex = (wordHash % EMBEDDING_DIM + EMBEDDING_DIM) % EMBEDDING_DIM
            val sign = if ((wordHash and 1) == 0) 1f else -1f
            vector[dimIndex] += sign * tfWeight * 1.5f

            // Character 3-grams for typo & morphology tolerance
            if (token.length >= 3) {
                for (i in 0..token.length - 3) {
                    val gram = token.substring(i, i + 3)
                    val gramHash = fnv1a(gram)
                    val gDim = (gramHash % EMBEDDING_DIM + EMBEDDING_DIM) % EMBEDDING_DIM
                    val gSign = if ((gramHash and 2) == 0) 0.6f else -0.6f
                    vector[gDim] += gSign * 0.4f
                }
            }
        }

        // 3. Normalize vector to unit length (L2 norm = 1.0)
        return VectorMath.normalizeL2(vector)
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in STOPWORDS }
    }

    private fun fnv1a(str: String): Int {
        var hash = -2128831035 // 0x811c9dc5
        for (ch in str) {
            hash = hash xor ch.code
            hash = hash * 16777619 // 0x01000193
        }
        return hash
    }
}
