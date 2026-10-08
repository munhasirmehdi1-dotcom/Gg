package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacquardDesign
import com.example.model.WeaveType
import com.example.ui.components.PointPaperGrid

@Composable
fun WeaveScreen(
    design: JacquardDesign?,
    renderedBitmap: Bitmap?,
    isFabricSimulation: Boolean,
    isHindi: Boolean,
    onToggleFabricSimulation: (Boolean) -> Unit,
    onUpdateColorWeave: (Int, WeaveType) -> Unit,
    onNext: () -> Unit
) {
    if (design == null || renderedBitmap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedYarnIdForWeave by remember { mutableStateOf(design.palette.firstOrNull()?.id ?: 0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mode Toggle Bar (Point Paper vs Fabric Simulation)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isFabricSimulation,
                        onClick = { onToggleFabricSimulation(false) },
                        label = { Text(if (isHindi) "ग्राफ पेपर (Point Paper)" else "Point Paper Graph") },
                        leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("mode_graph_chip")
                    )
                    FilterChip(
                        selected = isFabricSimulation,
                        onClick = { onToggleFabricSimulation(true) },
                        label = { Text(if (isHindi) "कपड़ा सिमुलेशन (Fabric)" else "Woven Fabric") },
                        leadingIcon = { Icon(Icons.Default.Texture, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("mode_simulation_chip")
                    )
                }

                Text(
                    text = "${design.widthHooks}×${design.heightPicks}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Weave assignment toolbar per yarn color
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Yarn selection row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(design.palette) { yarn ->
                        val isSelected = yarn.id == selectedYarnIdForWeave
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedYarnIdForWeave = yarn.id },
                            label = { Text("${yarn.name} (${yarn.assignedWeave.labelEn})", fontSize = 11.sp) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(yarn.color)
                                )
                            },
                            modifier = Modifier.testTag("weave_yarn_chip_${yarn.id}")
                        )
                    }
                }

                // Weave pattern choices for selected yarn
                val selectedYarn = design.palette.find { it.id == selectedYarnIdForWeave }
                selectedYarn?.let { yarn ->
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(WeaveType.values()) { weave ->
                            FilterChip(
                                selected = yarn.assignedWeave == weave,
                                onClick = { onUpdateColorWeave(yarn.id, weave) },
                                label = { Text(if (isHindi) weave.labelHi else weave.labelEn, fontSize = 11.sp) },
                                modifier = Modifier.testTag("weave_choice_${weave.name}")
                            )
                        }
                    }
                }
            }
        }

        // Interactive Zoomable Point Paper Canvas Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PointPaperGrid(
                design = design,
                renderedBitmap = renderedBitmap,
                isFabricSimulation = isFabricSimulation,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Bar with "Next: Export & Share"
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_export_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isHindi) "आगे: BMP एक्सपोर्ट और व्हाट्सएप शेयर →" else "Next: Export BMP & WhatsApp Share →",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
