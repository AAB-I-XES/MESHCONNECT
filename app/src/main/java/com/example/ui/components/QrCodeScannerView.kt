package com.example.ui.components

import android.Manifest
import android.content.Context
import android.graphics.ImageFormat
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MeshPrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun QrCodeScannerModal(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            try {
                launcher.launch(Manifest.permission.CAMERA)
            } catch (e: Exception) {
                Log.w("QrScanner", "Permission launch failed", e)
            }
        }
    }

    var manualInputText by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }

    val samplePeers = remember {
        listOf(
            Triple("node_alpha_8821", "pub_secp256k1_alpha", "Alpha Peer (Wi-Fi Direct)"),
            Triple("node_bravo_9430", "pub_secp256k1_bravo", "Bravo Mesh Relay"),
            Triple("node_charlie_1102", "pub_secp256k1_charlie", "Charlie Node (BLE)")
        )
    }

    Surface(
        color = NavyBackground.copy(alpha = 0.98f),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                CameraXQrScannerView(
                    flashEnabled = flashEnabled,
                    onQrScanned = { result ->
                        onQrScanned(result)
                        onDismiss()
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Access Requested",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enable camera access to scan physical QR codes, or select a demo peer below to simulate a live scan.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = MeshPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }

            // Top Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .bounceClick(onClick = onDismiss)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }

                Text(
                    text = "Scan Mesh QR Code",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = { flashEnabled = !flashEnabled },
                    enabled = hasCameraPermission,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.85f))
                        .bounceClick(onClick = { flashEnabled = !flashEnabled })
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) NeonAmber else TextSecondary
                    )
                }
            }

            // Bottom Manual Payload & Sample Peers Panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quick Demo Peer Scan (1-Tap Test)",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samplePeers.forEach { (meshId, pubKey, name) ->
                        val payload = "MESHLINK:$meshId:$pubKey:$name"
                        Surface(
                            color = NavyBackground,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MeshPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick(onClick = {
                                    onQrScanned(payload)
                                    onDismiss()
                                })
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = meshId.take(10),
                                    color = MeshPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Manual Input / Paste Raw Payload",
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = manualInputText,
                        onValueChange = { manualInputText = it },
                        placeholder = { Text("MESHLINK:node_id:key...", fontSize = 12.sp, color = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MeshPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = NavyBackground,
                            unfocusedContainerColor = NavyBackground
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualInputText.isNotBlank()) {
                                onQrScanned(manualInputText.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeshPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = manualInputText.isNotBlank(),
                        modifier = Modifier.bounceClick(onClick = {
                            if (manualInputText.isNotBlank()) {
                                onQrScanned(manualInputText.trim())
                                onDismiss()
                            }
                        })
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Connect", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CameraXQrScannerView(
    flashEnabled: Boolean,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineY"
    )

    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    LaunchedEffect(flashEnabled) {
        try {
            cameraControl?.enableTorch(flashEnabled)
        } catch (e: Exception) {
            Log.w("QrScanner", "Torch toggle unavailable", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cameraError ?: "Camera unavailable",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(cameraExecutor, QrCodeFrameAnalyzer { result ->
                                        onQrScanned(result)
                                    })
                                }

                            val cameraSelector = when {
                                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                                else -> null
                            }

                            if (cameraSelector != null) {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalyzer
                                )
                                cameraControl = camera.cameraControl
                            } else {
                                cameraError = "No camera sensor detected in this environment. Use manual payload input below."
                            }
                        } catch (e: Throwable) {
                            Log.e("QrScanner", "Camera initialization failed", e)
                            cameraError = "Camera sensor busy or unavailable. Paste payload manually below."
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanner Frame Overlay
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .border(2.dp, MeshPrimary, RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineY = size.height * scanLineY
                    drawLine(
                        color = MeshPrimary,
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 4f
                    )
                }
            }
        }
    }
}

private class QrCodeFrameAnalyzer(
    private val onQrDecoded: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    private var hasFoundCode = false

    override fun analyze(image: ImageProxy) {
        if (hasFoundCode) {
            image.close()
            return
        }

        if (image.format == ImageFormat.YUV_420_888 || image.format == ImageFormat.YUV_422_888 || image.format == ImageFormat.YUV_444_888) {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = image.width
            val height = image.height

            val source = PlanarYUVLuminanceSource(
                data, width, height, 0, 0, width, height, false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decodeWithState(binaryBitmap)
                if (result != null && !result.text.isNull_Blank()) {
                    hasFoundCode = true
                    onQrDecoded(result.text)
                }
            } catch (_: Exception) {
                // Frame contained no QR code, continue scanning
            } finally {
                reader.reset()
            }
        }
        image.close()
    }

    private fun String?.isNull_Blank(): Boolean = this == null || this.isBlank()
}
