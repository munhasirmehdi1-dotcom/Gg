package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacquardDesign
import kotlin.math.roundToInt

@Composable
fun PointPaperGrid(
    design: JacquardDesign,
    renderedBitmap: Bitmap,
    isFabricSimulation: Boolean,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var inspectedCoordinate by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 25f)
        offset += panChange
    }

    val imageBitmap = remember(renderedBitmap) { renderedBitmap.asImageBitmap() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color(0xFF0F172A))
            .testTag("point_paper_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformState)
                .pointerInput(design, scale, offset) {
                    detectTapGestures { tapOffset ->
                        val bmpW = design.widthHooks.toFloat()
                        val bmpH = design.heightPicks.toFloat()

                        // Disambiguate PointerInputScope.size
                        val canvasW = this.size.width.toFloat()
                        val canvasH = this.size.height.toFloat()

                        val fitScale = minOf(canvasW / bmpW, canvasH / bmpH)
                        val drawW = bmpW * fitScale * scale
                        val drawH = bmpH * fitScale * scale

                        val left = (canvasW - drawW) / 2f + offset.x
                        val top = (canvasH - drawH) / 2f + offset.y

                        if (tapOffset.x in left..(left + drawW) && tapOffset.y in top..(top + drawH)) {
                            val relX = (tapOffset.x - left) / drawW
                            val relY = (tapOffset.y - top) / drawH
                            val hook = (relX * design.widthHooks).toInt().coerceIn(0, design.widthHooks - 1)
                            val pick = (relY * design.heightPicks).toInt().coerceIn(0, design.heightPicks - 1)
                            inspectedCoordinate = Pair(hook, pick)
                        } else {
                            inspectedCoordinate = null
                        }
                    }
                }
                .testTag("point_paper_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val bmpW = design.widthHooks.toFloat()
            val bmpH = design.heightPicks.toFloat()

            val fitScale = minOf(canvasW / bmpW, canvasH / bmpH)
            val drawW = bmpW * fitScale * scale
            val drawH = bmpH * fitScale * scale

            val left = (canvasW - drawW) / 2f + offset.x
            val top = (canvasH - drawH) / 2f + offset.y

            // Draw Jacquard Bitmap
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
            )

            // Draw technical 8x8 Point Paper Grid overlay when zoomed in
            val pixelDisplayWidth = drawW / bmpW
            if (pixelDisplayWidth >= 5f && !isFabricSimulation) {
                val gridColor = Color(0x33FFFFFF)
                val majorGridColor = Color(0x88FFCC00) // 8x8 major textile division

                // Draw vertical warp lines
                for (x in 0..design.widthHooks step 1) {
                    val lineX = left + x * pixelDisplayWidth
                    if (lineX in 0f..canvasW) {
                        val isMajor = (x % 8 == 0)
                        drawLine(
                            color = if (isMajor) majorGridColor else gridColor,
                            start = Offset(lineX, top),
                            end = Offset(lineX, top + drawH),
                            strokeWidth = if (isMajor) 1.5f else 0.5f
                        )
                    }
                }

                // Draw horizontal weft lines
                val pixelDisplayHeight = drawH / bmpH
                for (y in 0..design.heightPicks step 1) {
                    val lineY = top + y * pixelDisplayHeight
                    if (lineY in 0f..canvasH) {
                        val isMajor = (y % 8 == 0)
                        drawLine(
                            color = if (isMajor) majorGridColor else gridColor,
                            start = Offset(left, lineY),
                            end = Offset(left + drawW, lineY),
                            strokeWidth = if (isMajor) 1.5f else 0.5f
                        )
                    }
                }
            }
        }

        // Zoom Controls & Reset
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { scale = (scale * 1.5f).coerceAtMost(25f) },
                modifier = Modifier.size(36.dp).testTag("zoom_in_btn")
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }
            FilledTonalIconButton(
                onClick = { scale = (scale / 1.5f).coerceAtLeast(0.5f) },
                modifier = Modifier.size(36.dp).testTag("zoom_out_btn")
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }
            FilledTonalButton(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                    inspectedCoordinate = null
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp).testTag("reset_view_btn")
            ) {
                Text("100%", fontSize = 12.sp)
            }
        }

        // Coordinate & Yarn Inspector HUD
        inspectedCoordinate?.let { (hook, pick) ->
            val pIdx = design.pixelIndices.getOrNull(pick * design.widthHooks + hook) ?: 0
            val yarn = design.palette.getOrNull(pIdx)

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .testTag("inspector_hud_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    yarn?.let {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(it.color, RoundedCornerShape(4.dp))
                        )
                    }

                    Column {
                        Text(
                            text = "Hook (ताना): ${hook + 1} | Pick (बाना): ${pick + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        yarn?.let {
                            Text(
                                text = "${it.name} (${it.hexCode}) • Weave: ${it.assignedWeave.labelEn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
