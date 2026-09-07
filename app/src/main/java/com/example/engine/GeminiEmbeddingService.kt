package com.example.engine

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class EmbedPart(
    @param:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class EmbedContent(
    @param:Json(name = "parts") val parts: List<EmbedPart>
)

@JsonClass(generateAdapter = true)
data class EmbedContentRequest(
    @param:Json(name = "content") val content: EmbedContent
)

@JsonClass(generateAdapter = true)
data class EmbeddingValues(
    @param:Json(name = "values") val values: List<Float>
)

@JsonClass(generateAdapter = true)
data class EmbedContentResponse(
    @param:Json(name = "embedding") val embedding: EmbeddingValues?
)

interface GeminiEmbeddingApi {
    @POST("v1beta/models/text-embedding-004:embedContent")
    suspend fun embedContent(
        @Query("key") apiKey: String,
        @Body request: EmbedContentRequest
    ): EmbedContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: GeminiEmbeddingApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiEmbeddingApi::class.java)
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun fetchEmbedding(text: String): Result<FloatArray> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        try {
            val request = EmbedContentRequest(
                content = EmbedContent(parts = listOf(EmbedPart(text = text)))
            )
            val response = api.embedContent(apiKey = apiKey, request = request)
            val values = response.embedding?.values
            if (values != null && values.isNotEmpty()) {
                val array = values.toFloatArray()
                Result.success(VectorMath.normalizeL2(array))
            } else {
                Result.failure(IllegalStateException("Empty embedding returned from Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
