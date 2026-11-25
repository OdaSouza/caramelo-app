package com.draxio.mapa_teste

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.core.graphics.scale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.draxio.mapa_teste.ui.components.CarameloPillButton
import com.draxio.mapa_teste.ui.components.CarameloScreenTitle
import com.draxio.mapa_teste.ui.components.CarameloSegmentedProgress
import com.draxio.mapa_teste.ui.components.CarameloTopBar
import com.draxio.mapa_teste.ui.components.PillStyle
import com.draxio.mapa_teste.ui.theme.*

class VehiclePhotoActivity : ComponentActivity() {

    private var capturedBitmap by mutableStateOf<Bitmap?>(null)

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            Toast.makeText(this, "Foto capturada com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Captura de foto cancelada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // rotação: preserva a foto já tirada
        capturedBitmap?.let { bmp ->
            bitmapToSmallJpeg(bmp)?.let { outState.putByteArray("vehicle_photo_jpeg", it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // rotação: preserva a foto já tirada
        if (capturedBitmap == null) {
            capturedBitmap = jpegToBitmap(savedInstanceState?.getByteArray("vehicle_photo_jpeg"))
        }
        setContent {
            Mapa_testeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VehiclePhotoScreen(
                        bitmap = capturedBitmap,
                        onBack = { finish() },
                        onTakePicture = {
                            try {
                                takePictureLauncher.launch(null)
                            } catch (_: android.content.ActivityNotFoundException) {
                                Toast.makeText(this, "Nenhum app de câmera disponível.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFinishRegistration = {
                            if (capturedBitmap != null) {
                                Toast.makeText(this, "Veículo salvo. Falta a verificação de segurança.", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, SecurityCheckActivity::class.java))
                            } else {
                                Toast.makeText(this, "Por favor, tire uma foto do automóvel.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VehiclePhotoScreen(
    bitmap: Bitmap?,
    onBack: () -> Unit,
    onTakePicture: () -> Unit,
    onFinishRegistration: () -> Unit
) {
    val frameShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CarameloTopBar(title = "Foto do veículo", onBack = onBack)

            // Indicador de etapa
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CarameloSegmentedProgress(progress = 4f / 5f, segments = 5, height = 8.dp)
                Text(
                    text = "Passo 4 de 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CarameloScreenTitle(title = "Fotografe o automóvel")

            Text(
                text = "Fotografe o veículo inteiro, de frente ou de lado, com a placa visível.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(frameShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        BorderStroke(
                            2.dp,
                            if (bitmap != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        frameShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto do Automóvel",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nenhuma foto capturada",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toque abaixo para tirar a foto do veículo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (bitmap != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Foto capturada. Confira se o veículo está bem enquadrado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        CarameloPillButton(
            text = if (bitmap == null) "Tirar foto" else "Tirar outra foto",
            icon = Icons.Rounded.PhotoCamera,
            onClick = onTakePicture,
            modifier = Modifier.fillMaxWidth(),
            style = if (bitmap == null) PillStyle.Primary else PillStyle.Outline
        )

        Spacer(modifier = Modifier.height(10.dp))

        CarameloPillButton(
            text = "Continuar para verificação",
            onClick = onFinishRegistration,
            modifier = Modifier.fillMaxWidth(),
            style = if (bitmap == null) PillStyle.Outline else PillStyle.Primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VehiclePhotoScreenPreview() {
    Mapa_testeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            VehiclePhotoScreen(
                bitmap = null,
                onBack = {},
                onTakePicture = {},
                onFinishRegistration = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VehiclePhotoScreenDarkPreview() {
    Mapa_testeTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            VehiclePhotoScreen(
                bitmap = null,
                onBack = {},
                onTakePicture = {},
                onFinishRegistration = {}
            )
        }
    }
}

// rotação: preserva a foto já tirada (versão reduzida em JPEG, até ~200 KB, para caber no Bundle)
private fun bitmapToSmallJpeg(source: Bitmap, maxBytes: Int = 200 * 1024): ByteArray? {
    return try {
        var current = source
        val longSide = maxOf(current.width, current.height)
        if (longSide > 1024) {
            val scale = 1024f / longSide
            current = current.scale((current.width * scale).toInt().coerceAtLeast(1), (current.height * scale).toInt().coerceAtLeast(1))
        }
        var bytes = ByteArray(0)
        repeat(5) {
            val stream = java.io.ByteArrayOutputStream()
            current.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            bytes = stream.toByteArray()
            if (bytes.size <= maxBytes) return bytes
            current = current.scale((current.width * 0.75f).toInt().coerceAtLeast(1), (current.height * 0.75f).toInt().coerceAtLeast(1))
        }
        bytes
    } catch (_: Exception) {
        null
    }
}

private fun jpegToBitmap(bytes: ByteArray?): Bitmap? =
    if (bytes == null) null else try {
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) {
        null
    }
