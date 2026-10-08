package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacquardDesign
import com.example.model.YarnColor

@Composable
fun ColorScreen(
    design: JacquardDesign?,
    isHindi: Boolean,
    onUpdateColorCount: (Int) -> Unit,
    onToggleCleanStray: (Boolean) -> Unit,
    onEditYarn: (YarnColor) -> Unit,
    onNext: () -> Unit
) {
    if (design == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val colorCountPresets = listOf(2, 3, 4, 6, 8, 12, 16)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quantization / Color Reduction Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "रंगों की संख्या (Target Colors)" else "Target Color Count (Quantization)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${design.palette.size} Colors",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (isHindi) "जैकवार्ड लूम के अनुसार स्केच को सीमित रंगों में बदलें" else "Quantize continuous sketch tones into exact loom yarn colors",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colorCountPresets.forEach { count ->
                        FilterChip(
                            selected = design.palette.size == count,
                            onClick = { onUpdateColorCount(count) },
                            label = { Text("$count") },
                            modifier = Modifier.testTag("color_count_chip_$count")
                        )
                    }
                }
            }
        }

        // Stray Pixel Filter (Speckle Cleaner) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi) "अनावश्यक पिक्सल सफाई (Noise Despeckle)" else "Stray Pixel Filter (Despeckle)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = if (isHindi) "एकल पिक्सल हटाकर बुनाई के कार्ड साफ रखें" else "Eliminate isolated rogue dots to prevent loom yarn snags",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = design.config.cleanStrayPixels,
                    onCheckedChange = onToggleCleanStray,
                    modifier = Modifier.testTag("stray_clean_switch")
                )
            }
        }

        // Interactive Yarn Palette List Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "जैकवार्ड धागा पैलेट (Yarn Palette)" else "Jacquard Yarn Palette",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (isHindi) "रंग बदलने के लिए टैप करें" else "Tap yarn to edit color",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Palette Items
                design.palette.forEach { yarn ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEditYarn(yarn) }
                            .testTag("yarn_item_${yarn.id}"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Color Chip
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(yarn.color)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            )

                            // Yarn Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = yarn.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "${yarn.hexCode} • ${yarn.assignedWeave.labelEn}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Coverage percentage & pixel count
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${"%.1f".format(yarn.percentage)}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${yarn.pixelCount} pts",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Color",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Next Step Button
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_weave_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (isHindi) "आगे: बुनाई सिमुलेशन और ग्रिड देखें →" else "Next: Weave Simulation & CAD Grid →",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
