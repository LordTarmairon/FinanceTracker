package com.gorthaur.financetracker.data.remote

import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.model.ReceiptScan
import com.gorthaur.financetracker.core.model.TransactionCategory
import com.gorthaur.financetracker.core.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Posibles motivos por los que el escaneo con IA puede fallar. */
enum class ScanErrorReason { NO_API_KEY, NETWORK, API, PARSE, NO_IMAGE }

sealed interface ScanResult {
    data class Success(val scan: ReceiptScan) : ScanResult
    data class Failure(val reason: ScanErrorReason, val detail: String? = null) : ScanResult
}

/**
 * Envía la foto de un ticket a la API de mensajes de Claude (visión) y
 * devuelve los datos del movimiento ya estructurados.
 *
 * En Android usamos HTTP directo (OkHttp) en lugar del SDK de servidor.
 * La clave de API la introduce el usuario en Ajustes; nunca va incrustada.
 */
class ReceiptAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun scan(
        imageBase64: String,
        apiKey: String,
        defaultCurrency: CurrencyCode
    ): ScanResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext ScanResult.Failure(ScanErrorReason.NO_API_KEY)

        val requestJson = buildRequestBody(imageBase64, defaultCurrency)

        val request = Request.Builder()
            .url(MESSAGES_URL)
            .header("x-api-key", apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .header("content-type", "application/json")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val detail = runCatching {
                        JSONObject(body).getJSONObject("error").getString("message")
                    }.getOrNull() ?: "HTTP ${response.code}"
                    return@withContext ScanResult.Failure(ScanErrorReason.API, detail)
                }
                parseResponse(body, defaultCurrency)
            }
        } catch (e: Exception) {
            ScanResult.Failure(ScanErrorReason.NETWORK, e.message)
        }
    }

    private fun buildRequestBody(imageBase64: String, defaultCurrency: CurrencyCode): JSONObject {
        val imageBlock = JSONObject()
            .put("type", "image")
            .put(
                "source",
                JSONObject()
                    .put("type", "base64")
                    .put("media_type", "image/jpeg")
                    .put("data", imageBase64)
            )
        val textBlock = JSONObject()
            .put("type", "text")
            .put("text", "Extrae los datos de este ticket o factura y clasifícalo.")

        val userMessage = JSONObject()
            .put("role", "user")
            .put("content", JSONArray().put(imageBlock).put(textBlock))

        return JSONObject()
            .put("model", MODEL)
            .put("max_tokens", 1024)
            .put("system", systemPrompt(defaultCurrency))
            .put("messages", JSONArray().put(userMessage))
            .put("output_config", JSONObject().put("format", outputSchema()))
    }

    private fun systemPrompt(defaultCurrency: CurrencyCode): String {
        val today = LocalDate.now()
        val categories = TransactionCategory.entries.joinToString("\n") { c ->
            "- ${c.name} (${c.type})"
        }
        return """
            Eres un asistente que extrae información de tickets y facturas a partir de una imagen.
            Devuelve un único movimiento económico con estos campos:
            - type: "EXPENSE" para gastos (lo más habitual) o "INCOME" si es una nómina,
              transferencia recibida o factura que cobras tú.
            - title: nombre corto y legible (normalmente el comercio o concepto).
            - amount: importe TOTAL como número positivo, usando punto decimal.
            - currency: código ISO de 3 letras (por defecto "${defaultCurrency.code}" si no se ve).
            - category: elige la más adecuada de esta lista (usa la clave exacta):
            $categories
            - date: fecha del ticket en formato YYYY-MM-DD. Si no es visible usa $today.
            - notes: detalle breve opcional (p. ej. productos principales).

            Si la imagen no es un ticket válido, devuelve igualmente tu mejor estimación.
        """.trimIndent()
    }

    private fun outputSchema(): JSONObject {
        val categoryKeys = JSONArray().apply {
            TransactionCategory.entries.forEach { put(it.name) }
        }
        val properties = JSONObject()
            .put("type", JSONObject().put("type", "string").put("enum", JSONArray().put("EXPENSE").put("INCOME")))
            .put("title", JSONObject().put("type", "string"))
            .put("amount", JSONObject().put("type", "number"))
            .put("currency", JSONObject().put("type", "string"))
            .put("category", JSONObject().put("type", "string").put("enum", categoryKeys))
            .put("date", JSONObject().put("type", "string"))
            .put("notes", JSONObject().put("type", "string"))

        val schema = JSONObject()
            .put("type", "object")
            .put("properties", properties)
            .put("required", JSONArray().put("type").put("title").put("amount").put("currency").put("category").put("date"))
            .put("additionalProperties", false)

        return JSONObject()
            .put("type", "json_schema")
            .put("schema", schema)
    }

    private fun parseResponse(body: String, defaultCurrency: CurrencyCode): ScanResult {
        val text = runCatching {
            val content = JSONObject(body).getJSONArray("content")
            (0 until content.length())
                .map { content.getJSONObject(it) }
                .firstOrNull { it.optString("type") == "text" }
                ?.optString("text")
        }.getOrNull() ?: return ScanResult.Failure(ScanErrorReason.PARSE)

        val json = runCatching { JSONObject(stripFences(text)) }.getOrNull()
            ?: return ScanResult.Failure(ScanErrorReason.PARSE)

        val type = TransactionType.fromName(json.optString("type"))
        val amount = json.optDouble("amount", Double.NaN)
        if (amount.isNaN()) return ScanResult.Failure(ScanErrorReason.PARSE)

        val currency = CurrencyCode.fromCode(json.optString("currency")) ?: defaultCurrency
        val category = TransactionCategory.fromKey(json.optString("category"), type)
        val title = json.optString("title").ifBlank { "Ticket" }
        val notes = json.optString("notes").takeIf { it.isNotBlank() }
        val date = parseDate(json.optString("date"))

        return ScanResult.Success(
            ReceiptScan(
                type = type,
                title = title,
                amount = kotlin.math.abs(amount),
                currencyCode = currency.code,
                category = category,
                dateEpochMillis = date,
                notes = notes
            )
        )
    }

    private fun parseDate(raw: String): Long {
        val parsed = runCatching { LocalDate.parse(raw.trim()) }.getOrNull() ?: LocalDate.now()
        return parsed.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun stripFences(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private companion object {
        const val MESSAGES_URL = "https://api.anthropic.com/v1/messages"
        const val ANTHROPIC_VERSION = "2023-06-01"
        const val MODEL = "claude-opus-4-8"
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
