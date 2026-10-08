package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacquardDesign
import com.example.ui.components.SampleMotifSelector
import com.example.ui.components.ZoomableImageViewer

@Composable
fun SketchScreen(
    currentDesign: JacquardDesign?,
    sourceBitmap: Bitmap?,
    isHindi: Boolean,
    onLoadSketch: (Bitmap, String) -> Unit,
    onOpenDrawingPad: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    // Gallery / File Picker launcher supporting BMP, PNG, JPEG
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                onLoadSketch(bmp, "Uploaded Sketch")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            onLoadSketch(it, "Camera Paper Sketch")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Architecture,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = if (isHindi) "कोई भी स्केच को जैकवार्ड में बदलें" else "Convert Any Sketch to Jacquard BMP",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (isHindi) "BMP, PNG, JPG अपलोड करें या हाथ से बनाएं" else "Upload BMP, PNG, JPEG, take photo or draw sketch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Action Buttons Grid (Upload, Camera, Draw)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Upload Button (BMP, PNG, JPEG)
            FilledTonalButton(
                onClick = { filePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(74.dp).testTag("upload_file_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isHindi) "फाइल अपलोड\n(BMP/PNG/JPG)" else "Upload File\n(BMP/PNG/JPG)",
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }

            // Camera Button
            FilledTonalButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.weight(1f).height(74.dp).testTag("camera_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isHindi) "कैमरा फोटो\n(कागज़ स्केच)" else "Camera\n(Paper Sketch)",
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }

            // Draw Sketch Button
            FilledTonalButton(
                onClick = onOpenDrawingPad,
                modifier = Modifier.weight(1f).height(74.dp).testTag("draw_sketch_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isHindi) "हाथ से बनाएं\n(Sketchpad)" else "Draw Sketch\n(In-App Pad)",
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // Sample Motif Selector
        SampleMotifSelector(
            onSelectSample = onLoadSketch,
            modifier = Modifier.fillMaxWidth()
        )

        // Current Active Sketch Preview
        sourceBitmap?.let { bmp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "वर्तमान स्केच (Active Sketch)" else "Current Active Sketch",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (isHindi) "🔍 पिंच करके या बटनों से ज़ूम करें" else "🔍 Pinch or use buttons to zoom & inspect",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${bmp.width} × ${bmp.height} px",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Interactive Zoomable Sketch Viewer
                    ZoomableImageViewer(
                        bitmap = bmp,
                        title = if (isHindi) "एक्टिव स्केच (ज़ूम व्यू)" else "Active Sketch (Zoom View)",
                        isHindi = isHindi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    // Next action button
                    Button(
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_size_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (isHindi) "आगे: साइज़ टाइप करें (Width & Height) →" else "Next: Type Size (Width & Height) →",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
