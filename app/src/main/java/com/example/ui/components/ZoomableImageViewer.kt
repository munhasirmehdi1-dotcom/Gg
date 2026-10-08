package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ZoomableImageViewer(
    bitmap: Bitmap,
    title: String = "Image Preview",
    isHindi: Boolean = false,
    modifier: Modifier = Modifier,
    allowFullscreen: Boolean = true
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showFullscreenDialog by remember { mutableStateOf(false) }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .clipToBounds()
            .pointerInput(bitmap) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.7f, 25f)
                    offset += pan
                }
            }
            .testTag("zoomable_image_viewer")
    ) {
        // Hardware GPU accelerated image layer
        Image(
            bitmap = imageBitmap,
            contentDescription = title,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )

        // Top info overlay: Dimensions & Current Zoom %
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.65f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(
                    text = "${(scale * 100).toInt()}% | ${bitmap.width}×${bitmap.height} px",
                    color = Color.White,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Bottom right zoom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { scale = (scale * 1.5f).coerceAtMost(25f) },
                modifier = Modifier.size(34.dp).testTag("zoom_in_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
            }

            FilledTonalIconButton(
                onClick = { scale = (scale / 1.5f).coerceAtLeast(0.7f) },
                modifier = Modifier.size(34.dp).testTag("zoom_out_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
            }

            FilledTonalIconButton(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier.size(34.dp).testTag("zoom_reset_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom", modifier = Modifier.size(18.dp))
            }

            if (allowFullscreen) {
                FilledTonalIconButton(
                    onClick = { showFullscreenDialog = true },
                    modifier = Modifier.size(34.dp).testTag("fullscreen_zoom_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen Zoom", modifier = Modifier.size(18.dp))
                }
            }
        }

        // Quick hint
        if (scale <= 1f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = if (isHindi) "पिंच करके ज़ूम करें" else "Pinch to zoom",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }

    // Fullscreen Zoom Dialog with Hardware Acceleration
    if (showFullscreenDialog) {
        Dialog(
            onDismissRequest = { showFullscreenDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var fullScale by remember { mutableFloatStateOf(1.5f) }
            var fullOffset by remember { mutableStateOf(Offset.Zero) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(bitmap) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                fullScale = (fullScale * zoom).coerceIn(0.5f, 30f)
                                fullOffset += pan
                            }
                        }
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = fullScale
                                scaleY = fullScale
                                translationX = fullOffset.x
                                translationY = fullOffset.y
                            },
                        contentScale = ContentScale.Fit
                    )

                    // Top Bar with Close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${bitmap.width} × ${bitmap.height} px | Zoom: ${(fullScale * 100).toInt()}%",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = { showFullscreenDialog = false },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Bottom controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { fullScale = (fullScale * 1.5f).coerceAtMost(30f) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Zoom In")
                        }

                        FilledTonalButton(
                            onClick = { fullScale = (fullScale / 1.5f).coerceAtLeast(0.5f) }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Zoom Out")
                        }

                        Button(
                            onClick = {
                                fullScale = 1f
                                fullOffset = Offset.Zero
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("100% Fit")
                        }
                    }
                }
            }
        }
    }
}
