package com.gorthaur.financetracker.data.remote

import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.ExchangeRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Obtiene los tipos de cambio actuales desde frankfurter.app (datos del BCE,
 * gratis y sin clave). La base es el euro.
 */
class ExchangeRateClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** Devuelve los tipos actualizados o null si falla la conexión. */
    suspend fun fetchLatest(): ExchangeRates? = withContext(Dispatchers.IO) {
        val targets = CurrencyCode.entries
            .filter { it != CurrencyCode.EUR }
            .joinToString(",") { it.code }
        val url = "https://api.frankfurter.app/latest?from=EUR&to=$targets"

        val request = Request.Builder().url(url).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val rates = JSONObject(body).optJSONObject("rates") ?: return@withContext null

                val map = mutableMapOf(CurrencyCode.EUR to 1.0)
                CurrencyCode.entries.forEach { currency ->
                    if (currency == CurrencyCode.EUR) return@forEach
                    val value = rates.optDouble(currency.code, Double.NaN)
                    if (!value.isNaN() && value > 0) map[currency] = value
                }
                ExchangeRates(map)
            }
        } catch (e: Exception) {
            null
        }
    }
}
