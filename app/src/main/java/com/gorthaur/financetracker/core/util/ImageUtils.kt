package com.gorthaur.financetracker.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Utilidades para manejar las fotos de tickets y productos:
 * copiarlas a almacenamiento interno, crear destinos para la cámara
 * y prepararlas (reescaladas) para enviarlas a la IA.
 */
object ImageUtils {

    private const val MAX_DIMENSION = 1600
    private const val JPEG_QUALITY = 85

    /** Crea un fichero vacío en caché y devuelve su Uri vía FileProvider para la cámara. */
    fun createCameraImageUri(context: Context): Uri {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(cameraDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Copia la imagen apuntada por [source] a un fichero persistente dentro de
     * `filesDir/receipts` y devuelve su ruta absoluta, o null si falla.
     * Las imágenes elegidas en la galería solo dan permiso temporal, por eso
     * las persistimos antes de guardarlas en la base de datos.
     */
    fun persistImage(context: Context, source: Uri): String? {
        return try {
            val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
            val target = File(receiptsDir, "img_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(target).use { output -> input.copyTo(output) }
            } ?: return null
            target.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lee la imagen, la reescala para no enviar megas innecesarios y la
     * devuelve codificada en Base64 lista para la API de visión.
     */
    fun toBase64Jpeg(context: Context, source: Uri): String? {
        return try {
            val bytes = context.contentResolver.openInputStream(source)?.use { it.readBytes() }
                ?: return null
            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val scaled = downscale(original)
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            if (scaled != original) scaled.recycle()
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun downscale(bitmap: Bitmap): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= MAX_DIMENSION) return bitmap
        val ratio = MAX_DIMENSION.toFloat() / largest
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
