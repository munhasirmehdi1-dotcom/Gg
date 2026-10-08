package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JacquardConfig
import com.example.ui.components.ZoomableImageViewer

enum class SizeInputMode(val labelEn: String, val labelHi: String) {
    HOOKS_PICKS("Hooks & Picks (ताना/बाना)", "हुक व पिक्स (ताना/बाना)"),
    FABRIC_CM("Fabric Size in CM (सेमी)", "कपड़े का माप (सेमी)"),
    WALLPAPER("Wallpaper / Screen (वॉलपेपर)", "वॉलपेपर / स्क्रीन")
}

@Composable
fun SizeScreen(
    config: JacquardConfig,
    sourceBitmap: Bitmap?,
    isHindi: Boolean,
    onUpdateHooks: (Int) -> Unit,
    onUpdatePicks: (Int) -> Unit,
    onUpdateConfig: (JacquardConfig) -> Unit,
    onNext: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var inputMode by remember { mutableStateOf(SizeInputMode.HOOKS_PICKS) }

    // Direct typed text states (completely decoupled from re-rendering loops for 0ms typing latency)
    var typedWidthText by remember(config.hooks) { mutableStateOf(config.hooks.toString()) }
    var typedHeightText by remember(config.picks) { mutableStateOf(config.picks.toString()) }

    var typedWidthCmText by remember(config.widthCm) { mutableStateOf("%.1f".format(config.widthCm)) }
    var typedHeightCmText by remember(config.heightCm) { mutableStateOf("%.1f".format(config.heightCm)) }

    var epiText by remember(config.epi) { mutableStateOf(config.epi.toString()) }
    var ppiText by remember(config.ppi) { mutableStateOf(config.ppi.toString()) }

    var isAspectLocked by remember(config.isAspectLocked) { mutableStateOf(config.isAspectLocked) }

    // Aspect ratio of source sketch
    val sourceRatio = remember(sourceBitmap) {
        if (sourceBitmap != null && sourceBitmap.width > 0) {
            sourceBitmap.height.toFloat() / sourceBitmap.width.toFloat()
        } else 1.0f
    }

    // Check if user has uncommitted changes
    val hasUncommittedChanges = remember(typedWidthText, typedHeightText, config.hooks, config.picks) {
        (typedWidthText.toIntOrNull() ?: config.hooks) != config.hooks ||
        (typedHeightText.toIntOrNull() ?: config.picks) != config.picks
    }

    // Function to apply current typed numbers
    fun commitCurrentSize() {
        focusManager.clearFocus()
        val newW = typedWidthText.toIntOrNull()?.coerceIn(16, 5000) ?: config.hooks
        val newH = typedHeightText.toIntOrNull()?.coerceIn(16, 5000) ?: config.picks
        onUpdateConfig(config.copy(hooks = newW, picks = newH, isAspectLocked = isAspectLocked))
    }

    // Width typing handler
    fun onWidthTyped(newWStr: String) {
        typedWidthText = newWStr
        val newW = newWStr.toIntOrNull() ?: return
        if (isAspectLocked && newW in 16..5000) {
            val autoH = (newW * sourceRatio).toInt().coerceIn(16, 5000)
            typedHeightText = autoH.toString()
        }
    }

    // Height typing handler
    fun onHeightTyped(newHStr: String) {
        typedHeightText = newHStr
        val newH = newHStr.toIntOrNull() ?: return
        if (isAspectLocked && newH in 16..5000 && sourceRatio > 0f) {
            val autoW = (newH / sourceRatio).toInt().coerceIn(16, 5000)
            typedWidthText = autoW.toString()
        }
    }

    // CM typing handler
    fun commitCmSize() {
        focusManager.clearFocus()
        val wCm = typedWidthCmText.toFloatOrNull() ?: return
        val hCm = typedHeightCmText.toFloatOrNull() ?: return
        val wInches = wCm / 2.54f
        val hInches = hCm / 2.54f
        val calculatedHooks = (wInches * config.epi).toInt().coerceIn(16, 5000)
        val calculatedPicks = (hInches * config.ppi).toInt().coerceIn(16, 5000)

        typedWidthText = calculatedHooks.toString()
        typedHeightText = calculatedPicks.toString()
        onUpdateConfig(config.copy(hooks = calculatedHooks, picks = calculatedPicks))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Zoomable Image Preview Card
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
                            text = if (isHindi) "🔍 इमेज को ज़ूम करके देखें (Live Preview)" else "🔍 Zoom & Inspect Image (Live Preview)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isHindi) "स्मूथ 60fps पिंच ज़ूम और फुलस्क्रीन व्यू" else "Ultra-smooth 60fps pinch-to-zoom & fullscreen view",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                sourceBitmap?.let { bmp ->
                    ZoomableImageViewer(
                        bitmap = bmp,
                        title = if (isHindi) "स्केच प्रिव्यू (Zoom View)" else "Sketch Zoom View",
                        isHindi = isHindi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }
            }
        }

        // 2. Mode Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isHindi) "साइज टाइप करने का तरीका चुनें:" else "Select Typing Mode:",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SizeInputMode.values().forEach { mode ->
                        FilterChip(
                            selected = inputMode == mode,
                            onClick = { inputMode = mode },
                            label = {
                                Text(
                                    if (isHindi) mode.labelHi else mode.labelEn,
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.testTag("size_mode_${mode.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. Direct Typing Card (Fast, zero-latency text fields)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "✍️ साइज टाइप करके लिखें" else "✍️ Type Size Directly (Width & Height)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            if (isHindi) "अनुपात लॉक" else "Lock Ratio",
                            fontSize = 11.sp,
                            color = if (isAspectLocked) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        IconButton(
                            onClick = { isAspectLocked = !isAspectLocked },
                            modifier = Modifier.size(32.dp).testTag("toggle_aspect_lock_btn")
                        ) {
                            Icon(
                                if (isAspectLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock Ratio",
                                tint = if (isAspectLocked) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                when (inputMode) {
                    SizeInputMode.HOOKS_PICKS, SizeInputMode.WALLPAPER -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Width
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (inputMode == SizeInputMode.WALLPAPER) "चौड़ाई / Width (px)" else "चौड़ाई / Warp Hooks (ताना)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = typedWidthText,
                                    onValueChange = { onWidthTyped(it) },
                                    modifier = Modifier.fillMaxWidth().testTag("typed_width_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { commitCurrentSize() }),
                                    singleLine = true,
                                    trailingIcon = {
                                        if (typedWidthText.isNotEmpty()) {
                                            IconButton(onClick = { onWidthTyped("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val cur = typedWidthText.toIntOrNull() ?: 480
                                            onWidthTyped((cur - 120).coerceAtLeast(16).toString())
                                            commitCurrentSize()
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("-120", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val cur = typedWidthText.toIntOrNull() ?: 480
                                            onWidthTyped((cur + 120).toString())
                                            commitCurrentSize()
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("+120", fontSize = 10.sp)
                                    }
                                }
                            }

                            // Height
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (inputMode == SizeInputMode.WALLPAPER) "ऊंचाई / Height (px)" else "लंबाई / Weft Picks (बाना)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = typedHeightText,
                                    onValueChange = { onHeightTyped(it) },
                                    modifier = Modifier.fillMaxWidth().testTag("typed_height_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { commitCurrentSize() }),
                                    singleLine = true,
                                    trailingIcon = {
                                        if (typedHeightText.isNotEmpty()) {
                                            IconButton(onClick = { onHeightTyped("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val cur = typedHeightText.toIntOrNull() ?: 600
                                            onHeightTyped((cur - 120).coerceAtLeast(16).toString())
                                            commitCurrentSize()
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("-120", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val cur = typedHeightText.toIntOrNull() ?: 600
                                            onHeightTyped((cur + 120).toString())
                                            commitCurrentSize()
                                        },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("+120", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // Wallpaper suggestions
                        if (inputMode == SizeInputMode.WALLPAPER) {
                            Text(
                                text = if (isHindi) "💡 वॉलपेपर सुझाव: Phone (1080×2400), Desktop (1920×1080), Square (1200×1200)"
                                else "💡 Tip: You can type any wallpaper resolution, e.g. 1080×2400 or 1920×1080.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SuggestionChip(
                                    onClick = {
                                        isAspectLocked = false
                                        typedWidthText = "1080"
                                        typedHeightText = "2400"
                                        commitCurrentSize()
                                    },
                                    label = { Text("Phone (1080×2400)", fontSize = 10.sp) }
                                )
                                SuggestionChip(
                                    onClick = {
                                        isAspectLocked = false
                                        typedWidthText = "1920"
                                        typedHeightText = "1080"
                                        commitCurrentSize()
                                    },
                                    label = { Text("Desktop (1920×1080)", fontSize = 10.sp) }
                                )
                                SuggestionChip(
                                    onClick = {
                                        isAspectLocked = false
                                        typedWidthText = "1200"
                                        typedHeightText = "1200"
                                        commitCurrentSize()
                                    },
                                    label = { Text("Square (1200×1200)", fontSize = 10.sp) }
                                )
                            }
                        }

                        // Prominent "Apply Typed Size" Button
                        Button(
                            onClick = { commitCurrentSize() },
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("apply_size_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasUncommittedChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (hasUncommittedChanges) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (hasUncommittedChanges) {
                                    if (isHindi) "साइज लागू करें (Apply Typed Size) ⚡" else "Apply Typed Size ⚡"
                                } else {
                                    if (isHindi) "साइज लागू है (Current Size Applied)" else "Size Applied ✓"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    SizeInputMode.FABRIC_CM -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("कपड़े की चौड़ाई (CM)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = typedWidthCmText,
                                    onValueChange = { typedWidthCmText = it },
                                    modifier = Modifier.fillMaxWidth().testTag("typed_width_cm_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { commitCmSize() }),
                                    singleLine = true,
                                    trailingIcon = { Text("cm", fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("कपड़े की लंबाई (CM)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = typedHeightCmText,
                                    onValueChange = { typedHeightCmText = it },
                                    modifier = Modifier.fillMaxWidth().testTag("typed_height_cm_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { commitCmSize() }),
                                    singleLine = true,
                                    trailingIcon = { Text("cm", fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) }
                                )
                            }
                        }

                        Button(
                            onClick = { commitCmSize() },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isHindi) "सेंटीमीटर साइज लागू करें" else "Apply CM Size")
                        }
                    }
                }
            }
        }

        // 4. Calculated Live Dimension Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "📐 वर्तमान डिज़ाइन का माप (Matrix & Size)" else "📐 Current Matrix & Physical Size",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${config.hooks} × ${config.picks} pts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(if (isHindi) "ताना (Warp Width)" else "Warp Width", fontSize = 11.sp)
                        Text(
                            "${config.hooks} Hooks (${"%.1f".format(config.widthCm)} cm / ${"%.2f".format(config.widthInches)}\")",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (isHindi) "बाना (Weft Height)" else "Weft Height", fontSize = 11.sp)
                        Text(
                            "${config.picks} Picks (${"%.1f".format(config.heightCm)} cm / ${"%.2f".format(config.heightInches)}\")",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }

        // 5. Loom Density (EPI & PPI) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isHindi) "लूम डेंसिटी टाइप करें (EPI / PPI)" else "Loom Density (Type EPI / PPI)",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = epiText,
                        onValueChange = {
                            epiText = it
                            it.toIntOrNull()?.let { e ->
                                if (e in 10..300) onUpdateConfig(config.copy(epi = e))
                            }
                        },
                        label = { Text("EPI (ताना प्रति इंच)") },
                        modifier = Modifier.weight(1f).testTag("epi_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ppiText,
                        onValueChange = {
                            ppiText = it
                            it.toIntOrNull()?.let { p ->
                                if (p in 10..300) onUpdateConfig(config.copy(ppi = p))
                            }
                        },
                        label = { Text("PPI (बाना प्रति इंच)") },
                        modifier = Modifier.weight(1f).testTag("ppi_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }

        // 6. Next Step Button
        Button(
            onClick = {
                commitCurrentSize()
                onNext()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("next_color_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (isHindi) "आगे: रंग और पैलेट कस्टमाइज़ करें →" else "Next: Customize Colors & Palette →",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
