package com.marcormz.jellyapp.data

import android.util.Log
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrl

interface StockBackend {
    suspend fun attemptAddToCart(product: Product): StockCheckResult
}

sealed interface StockCheckResult {
    data class Rejected(
        val httpStatus: Int,
        val errorCode: String,
    ) : StockCheckResult

    data class NetworkFailure(
        val reason: String,
    ) : StockCheckResult
}

/**
 * Deliberately calls a real HTTPS test endpoint that always responds with HTTP 409.
 * This creates a genuine network exchange for Quantum Metric's automatic capture.
 */
class HttpStockBackend(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(12, TimeUnit.SECONDS)
        .build(),
    private val endpoint: String = TEST_ENDPOINT,
) : StockBackend {
    override suspend fun attemptAddToCart(product: Product): StockCheckResult =
        withContext(Dispatchers.IO) {
            val url = endpoint.toHttpUrl().newBuilder()
                .addQueryParameter("item_id", product.sku)
                .build()
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .post(
                    """{"sku":"${product.sku}","quantity":1}"""
                        .toRequestBody(JSON_MEDIA_TYPE),
                )
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val result = if (response.isSuccessful) {
                        StockCheckResult.NetworkFailure("Respuesta inesperada del backend.")
                    } else {
                        val code = if (response.code == 409) {
                            "OUT_OF_STOCK"
                        } else {
                            "HTTP_${response.code}"
                        }
                        StockCheckResult.Rejected(
                            httpStatus = response.code,
                            errorCode = code,
                        )
                    }
                    Log.w(
                        TAG,
                        "POST ${request.url} -> HTTP ${response.code}; ${result.describe()}",
                    )
                    result
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: IOException) {
                Log.e(TAG, "POST ${request.url} -> network failure", error)
                StockCheckResult.NetworkFailure(
                    error.message ?: "No se pudo contactar con el backend.",
                )
            }
        }

    private fun StockCheckResult.describe(): String = when (this) {
        is StockCheckResult.Rejected -> "$errorCode ($httpStatus)"
        is StockCheckResult.NetworkFailure -> "NETWORK_ERROR ($reason)"
    }

    companion object {
        private const val TAG = "JellyStockBackend"
        private const val TEST_ENDPOINT = "https://httpbin.org/status/409"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
