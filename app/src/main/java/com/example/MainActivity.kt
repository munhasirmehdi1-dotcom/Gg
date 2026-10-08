package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.DrawingPad
import com.example.ui.components.SAMPLE_MOTIFS
import com.example.ui.components.loadSampleBitmap
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JacquardTab
import com.example.viewmodel.JacquardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JacquardApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JacquardApp(viewModel: JacquardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Load initial sample sketch on first launch
    LaunchedEffect(Unit) {
        if (state.sourceBitmap == null) {
            val sample = SAMPLE_MOTIFS[0]
            val bmp = loadSampleBitmap(context, sample)
            viewModel.loadNewSketch(bmp, sample.titleEn)
        }
    }

    // Handle back button for sub-modes
    BackHandler(enabled = state.isDrawingPadOpen || state.activeTab != JacquardTab.SKETCH) {
        if (state.isDrawingPadOpen) {
            viewModel.openDrawingPad(false)
        } else {
            viewModel.selectTab(JacquardTab.SKETCH)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.isHindi) "जैकवार्ड स्टूडियो" else "Jacquard Studio",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (state.isHindi) "स्केच से बीएमपी डिज़ाइन कनवर्टर" else "Sketch to BMP Jacquard CAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Language Switch Button
                    TextButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("lang_toggle_btn")
                    ) {
                        Text(
                            if (state.isHindi) "ENG" else "हिन्दी",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Direct Quick WhatsApp Share Button
                    IconButton(
                        onClick = { viewModel.shareBmpToWhatsApp(context) },
                        modifier = Modifier.testTag("quick_share_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share on WhatsApp")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (!state.isDrawingPadOpen) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    JacquardTab.values().forEach { tab ->
                        val isSelected = state.activeTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                when (tab) {
                                    JacquardTab.SKETCH -> Icon(Icons.Default.Image, contentDescription = tab.titleEn)
                                    JacquardTab.SIZE -> Icon(Icons.Default.AspectRatio, contentDescription = tab.titleEn)
                                    JacquardTab.COLORS -> Icon(Icons.Default.Palette, contentDescription = tab.titleEn)
                                    JacquardTab.WEAVE -> Icon(Icons.Default.GridView, contentDescription = tab.titleEn)
                                    JacquardTab.EXPORT -> Icon(Icons.Default.Share, contentDescription = tab.titleEn)
                                }
                            },
                            label = {
                                Text(
                                    if (state.isHindi) tab.titleHi else tab.titleEn,
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                if (state.isDrawingPadOpen) {
                    DrawingPad(
                        onSketchFinished = { bmp ->
                            viewModel.loadNewSketch(bmp, "Hand Drawn Sketch")
                        },
                        onCancel = { viewModel.openDrawingPad(false) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AnimatedContent(
                        targetState = state.activeTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabContent"
                    ) { currentTab ->
                        when (currentTab) {
                            JacquardTab.SKETCH -> {
                                SketchScreen(
                                    currentDesign = state.currentDesign,
                                    sourceBitmap = state.sourceBitmap,
                                    isHindi = state.isHindi,
                                    onLoadSketch = { bmp, title ->
                                        viewModel.loadNewSketch(bmp, title)
                                    },
                                    onOpenDrawingPad = { viewModel.openDrawingPad(true) },
                                    onNext = { viewModel.selectTab(JacquardTab.SIZE) }
                                )
                            }
                            JacquardTab.SIZE -> {
                                SizeScreen(
                                    config = state.config,
                                    sourceBitmap = state.sourceBitmap,
                                    isHindi = state.isHindi,
                                    onUpdateHooks = { viewModel.updateHooks(it) },
                                    onUpdatePicks = { viewModel.updatePicks(it) },
                                    onUpdateConfig = { viewModel.updateConfig(it) },
                                    onNext = { viewModel.selectTab(JacquardTab.COLORS) }
                                )
                            }
                            JacquardTab.COLORS -> {
                                ColorScreen(
                                    design = state.currentDesign,
                                    isHindi = state.isHindi,
                                    onUpdateColorCount = { viewModel.updateColorCount(it) },
                                    onToggleCleanStray = { viewModel.toggleStrayPixelCleaning(it) },
                                    onEditYarn = { viewModel.setEditingYarn(it) },
                                    onNext = { viewModel.selectTab(JacquardTab.WEAVE) }
                                )
                            }
                            JacquardTab.WEAVE -> {
                                WeaveScreen(
                                    design = state.currentDesign,
                                    renderedBitmap = state.renderedBitmap,
                                    isFabricSimulation = state.isFabricSimulation,
                                    isHindi = state.isHindi,
                                    onToggleFabricSimulation = { viewModel.toggleFabricSimulation(it) },
                                    onUpdateColorWeave = { id, weave -> viewModel.updateColorWeave(id, weave) },
                                    onNext = { viewModel.selectTab(JacquardTab.EXPORT) }
                                )
                            }
                            JacquardTab.EXPORT -> {
                                ExportScreen(
                                    design = state.currentDesign,
                                    isHindi = state.isHindi,
                                    onFormatChanged = { fmt ->
                                        viewModel.updateConfig(state.config.copy(bmpFormat = fmt))
                                    },
                                    onShareBmpToWhatsApp = { ctx -> viewModel.shareBmpToWhatsApp(ctx) },
                                    onSharePictureToWhatsApp = { ctx -> viewModel.sharePictureToWhatsApp(ctx) },
                                    onSaveBmpToDownloads = { ctx -> viewModel.saveBmpToDownloads(ctx) }
                                )
                            }
                        }
                    }
                }

                // Processing Indicator Overlay
                if (state.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = if (state.isHindi) "जैकवार्ड मैट्रिक्स प्रोसेस हो रहा है..." else "Processing Jacquard Matrix...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Color Picker Dialog
                state.editingYarn?.let { yarn ->
                    state.currentDesign?.let { design ->
                        ColorPickerDialog(
                            yarn = yarn,
                            allPalette = design.palette,
                            onColorUpdated = { newColor, newName ->
                                viewModel.updatePaletteColor(yarn.id, newColor, newName)
                            },
                            onMergeWith = { targetId ->
                                viewModel.mergePaletteColors(yarn.id, targetId)
                            },
                            onDismiss = { viewModel.setEditingYarn(null) }
                        )
                    }
                }
            }
        }
    }
}
