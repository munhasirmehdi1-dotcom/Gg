package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BmpFormat
import com.example.model.JacquardDesign

@Composable
fun ExportScreen(
    design: JacquardDesign?,
    isHindi: Boolean,
    onFormatChanged: (BmpFormat) -> Unit,
    onShareBmpToWhatsApp: (Context) -> Unit,
    onSharePictureToWhatsApp: (Context) -> Unit,
    onSaveBmpToDownloads: (Context) -> Unit
) {
    val context = LocalContext.current

    if (design == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val cfg = design.config

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WhatsApp Action Card (Primary Highlight)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF075E54)), // WhatsApp Brand Green
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = if (isHindi) "व्हाट्सएप शेयरिंग (WhatsApp Sharing)" else "WhatsApp Instant Sharing",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = if (isHindi) "लूम ऑपरेटर या कारीगर को तुरंत भेजें" else "Send BMP or Visual Picture directly to chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // 1. Share BMP to WhatsApp Button
                Button(
                    onClick = { onShareBmpToWhatsApp(context) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("share_whatsapp_bmp_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366), // WhatsApp bright green
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FilePresent, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isHindi) "व्हाट्सएप पर BMP फाइल भेजें (Loom CAM)" else "Share BMP File on WhatsApp (Loom CAM)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // 2. Share Visual Picture to WhatsApp Button
                FilledTonalButton(
                    onClick = { onSharePictureToWhatsApp(context) },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("share_whatsapp_picture_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = Color(0xFF075E54)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isHindi) "व्हाट्सएप पर फोटो / पिक्चर भेजें" else "Share Visual Picture on WhatsApp",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // BMP Format Selector & Local Save Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isHindi) "💾 BMP फाइल फॉर्मेट विकल्प" else "💾 BMP Export Options",
                    style = MaterialTheme.typography.titleMedium
                )

                // Format selector
                BmpFormat.values().forEach { fmt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = if (cfg.bmpFormat == fmt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = cfg.bmpFormat == fmt,
                                onClick = { onFormatChanged(fmt) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(fmt.label, style = MaterialTheme.typography.titleSmall)
                                Text(fmt.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Save to Device Downloads Button
                OutlinedButton(
                    onClick = { onSaveBmpToDownloads(context) },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_downloads_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isHindi) "डिवाइस में BMP सेव करें (Downloads)" else "Save BMP to Device (Downloads)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Technical Jacquard Design Specification Card
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
                        text = if (isHindi) "📋 तकनीकी डिज़ाइन विवरण" else "📋 Jacquard Technical Sheet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${design.widthHooks * design.heightPicks} pts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                // Matrix Specs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (isHindi) "ताना हुक (Hooks)" else "Warp Hooks", fontSize = 12.sp)
                        Text("${design.widthHooks} ends", style = MaterialTheme.typography.titleSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isHindi) "बाना पिक्स (Picks)" else "Weft Picks", fontSize = 12.sp)
                        Text("${design.heightPicks} picks", style = MaterialTheme.typography.titleSmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isHindi) "घनत्व (EPI×PPI)" else "Loom Density", fontSize = 12.sp)
                        Text("${cfg.epi} × ${cfg.ppi}", style = MaterialTheme.typography.titleSmall)
                    }
                }

                // Dimensions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (isHindi) "कपड़े का माप (सेमी)" else "Dimensions (Metric)", fontSize = 12.sp)
                        Text("${"%.1f".format(cfg.widthCm)} × ${"%.1f".format(cfg.heightCm)} cm", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isHindi) "कपड़े का माप (इंच)" else "Dimensions (Inches)", fontSize = 12.sp)
                        Text("${"%.2f".format(cfg.widthInches)}\" × ${"%.2f".format(cfg.heightInches)}\"", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "रंग व धागे का विवरण (Yarn Details):" else "Yarn Palette Breakdown:",
                    style = MaterialTheme.typography.labelMedium
                )

                design.palette.forEach { yarn ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(yarn.color)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Text(yarn.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(yarn.assignedWeave.labelEn, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(yarn.percentage)}%", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
