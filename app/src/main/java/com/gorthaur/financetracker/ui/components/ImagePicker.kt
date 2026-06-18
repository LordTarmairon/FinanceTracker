package com.gorthaur.financetracker.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gorthaur.financetracker.core.util.ImageUtils

/** Permite lanzar la cámara o la galería desde un Composable. */
class ImagePickController(
    val pickFromGallery: () -> Unit,
    val takePhoto: () -> Unit
)

/**
 * Registra los lanzadores de cámara y galería y devuelve un controlador.
 * Hacer la foto no requiere permiso de CÁMARA porque delega en la app de cámara
 * del sistema (no declaramos el permiso en el manifest).
 */
@Composable
fun rememberImagePickController(onPicked: (Uri) -> Unit): ImagePickController {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let(onPicked) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraUri?.let(onPicked) }

    return remember {
        ImagePickController(
            pickFromGallery = { galleryLauncher.launch("image/*") },
            takePhoto = {
                val uri = ImageUtils.createCameraImageUri(context)
                cameraUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }
}
