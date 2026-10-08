package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.JacquardEngine
import com.example.model.YarnColor

@Composable
fun ColorPickerDialog(
    yarn: YarnColor,
    allPalette: List<YarnColor>,
    onColorUpdated: (Color, String) -> Unit,
    onMergeWith: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableFloatStateOf(yarn.color.red) }
    var green by remember { mutableFloatStateOf(yarn.color.green) }
    var blue by remember { mutableFloatStateOf(yarn.color.blue) }
    var yarnName by remember { mutableStateOf(yarn.name) }
    var showMergeMode by remember { mutableStateOf(false) }

    val currentColor = Color(red, green, blue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (showMergeMode) "Merge Color / रंग मिलाएं" else "Customize Color / रंग बदलें",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!showMergeMode) {
                    // Preview chip and Name field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(currentColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        )

                        OutlinedTextField(
                            value = yarnName,
                            onValueChange = { yarnName = it },
                            label = { Text("Yarn Name / धागे का नाम") },
                            modifier = Modifier.weight(1f).testTag("yarn_name_input"),
                            singleLine = true
                        )
                    }

                    // Traditional Textile Swatches
                    Text("Textile Yarn Presets / पारम्परिक रंग:", style = MaterialTheme.typography.labelMedium)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(96.dp)
                    ) {
                        items(JacquardEngine.PRESET_YARNS) { preset ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(preset.color)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                    .clickable {
                                        red = preset.color.red
                                        green = preset.color.green
                                        blue = preset.color.blue
                                        yarnName = preset.name
                                    }
                                    .testTag("preset_swatch_${preset.name}")
                            )
                        }
                    }

                    // RGB Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("R: ${(red * 255).toInt()}", fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            Slider(
                                value = red,
                                onValueChange = { red = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f).testTag("red_slider")
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G: ${(green * 255).toInt()}", fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            Slider(
                                value = green,
                                onValueChange = { green = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f).testTag("green_slider")
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("B: ${(blue * 255).toInt()}", fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            Slider(
                                value = blue,
                                onValueChange = { blue = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f).testTag("blue_slider")
                            )
                        }
                    }

                    if (allPalette.size > 2) {
                        TextButton(
                            onClick = { showMergeMode = true },
                            modifier = Modifier.testTag("enable_merge_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.CallMerge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Merge this color into another / दूसरे रंग में मिलाएं")
                        }
                    }
                } else {
                    // Merge Mode: Select target color
                    Text(
                        "Select which color to merge '${yarn.name}' into. All its pixels will take the target color:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    allPalette.filter { it.id != yarn.id }.forEach { target ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMergeWith(target.id) }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(target.color)
                                )
                                Text(target.name, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.weight(1f))
                                Text("${"%.1f".format(target.percentage)}%", fontSize = 12.sp)
                            }
                        }
                    }

                    TextButton(onClick = { showMergeMode = false }) {
                        Text("Back / वापस")
                    }
                }
            }
        },
        confirmButton = {
            if (!showMergeMode) {
                Button(
                    onClick = {
                        onColorUpdated(currentColor, yarnName)
                    },
                    modifier = Modifier.testTag("save_color_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Apply / लागू करें")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Cancel / रद्द करें")
            }
        }
    )
}
