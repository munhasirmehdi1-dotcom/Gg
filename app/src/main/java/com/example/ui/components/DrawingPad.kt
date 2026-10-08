package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class DrawStroke(
    val path: Path,
    val color: Color,
    val width: Float,
    val isEraser: Boolean = false
)

@Composable
fun DrawingPad(
    modifier: Modifier = Modifier,
    onSketchFinished: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }

    val presetColors = listOf(
        Color.Black,
        Color(0xFF800020), // Maroon
        Color(0xFF1E3A8A), // Royal Blue
        Color(0xFF006A4E), // Emerald
        Color(0xFFD4AF37), // Gold
        Color(0xFFFF7722), // Saffron
        Color(0xFF555555)  // Gray
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("drawing_cancel_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
                Spacer(Modifier.width(4.dp))
                Text("Cancel / रद्द करें")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                    },
                    enabled = strokes.isNotEmpty(),
                    modifier = Modifier.testTag("drawing_undo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }

                IconButton(
                    onClick = { strokes.clear() },
                    modifier = Modifier.testTag("drawing_clear_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear")
                }

                Button(
                    onClick = {
                        // Render strokes to a 1000x1000 white background bitmap
                        val size = 1000
                        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                        val canvas = AndroidCanvas(bmp)
                        canvas.drawColor(android.graphics.Color.WHITE)

                        val paint = AndroidPaint().apply {
                            isAntiAlias = true
                            style = AndroidPaint.Style.STROKE
                            strokeCap = AndroidPaint.Cap.ROUND
                            strokeJoin = AndroidPaint.Join.ROUND
                        }

                        strokes.forEach { s ->
                            paint.strokeWidth = s.width * 2.5f
                            if (s.isEraser) {
                                paint.color = android.graphics.Color.WHITE
                            } else {
                                paint.color = android.graphics.Color.argb(
                                    (s.color.alpha * 255).toInt(),
                                    (s.color.red * 255).toInt(),
                                    (s.color.green * 255).toInt(),
                                    (s.color.blue * 255).toInt()
                                )
                            }
                            // Convert Compose Path to Android Path
                            val aPath = android.graphics.Path()
                            // Approximate rendering
                            canvas.drawPath(s.path.asAndroidPath(), paint)
                        }

                        onSketchFinished(bmp)
                    },
                    modifier = Modifier.testTag("drawing_done_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Use Sketch")
                    Spacer(Modifier.width(4.dp))
                    Text("Use Sketch / तैयार")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Tool controls: colors and pen/eraser
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = !isEraser,
                            onClick = { isEraser = false },
                            label = { Text("Pen / पेन") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("tool_pen_chip")
                        )
                        FilterChip(
                            selected = isEraser,
                            onClick = { isEraser = true },
                            label = { Text("Eraser / मिटाएं") },
                            leadingIcon = { Icon(Icons.Default.AutoFixNormal, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("tool_eraser_chip")
                        )
                    }

                    // Stroke width slider
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(140.dp)) {
                        Text("${strokeWidth.toInt()}pt", fontSize = 12.sp)
                        Slider(
                            value = strokeWidth,
                            onValueChange = { strokeWidth = it },
                            valueRange = 2f..30f,
                            modifier = Modifier.weight(1f).testTag("stroke_width_slider")
                        )
                    }
                }

                // Color choices
                if (!isEraser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColors.forEach { col ->
                            val isSelected = selectedColor == col
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .testTag("color_choice_${col.value}")
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { selectedColor = col },
                                            onDrag = { _, _ -> }
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Drawing Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .testTag("sketch_drawing_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isEraser, selectedColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val p = Path().apply { moveTo(offset.x, offset.y) }
                                currentPath = p
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPath?.lineTo(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                currentPath?.let { p ->
                                    strokes.add(
                                        DrawStroke(
                                            path = p,
                                            color = if (isEraser) Color.White else selectedColor,
                                            width = strokeWidth,
                                            isEraser = isEraser
                                        )
                                    )
                                }
                                currentPath = null
                            },
                            onDragCancel = {
                                currentPath = null
                            }
                        )
                    }
            ) {
                // Draw existing strokes
                strokes.forEach { s ->
                    drawPath(
                        path = s.path,
                        color = s.color,
                        style = Stroke(
                            width = s.width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Draw currently active stroke
                currentPath?.let { p ->
                    drawPath(
                        path = p,
                        color = if (isEraser) Color.White else selectedColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
