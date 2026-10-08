package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.JacquardEngine
import com.example.engine.ShareManager
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class JacquardTab(val titleEn: String, val titleHi: String, val iconName: String) {
    SKETCH("Sketch", "स्केच", "image"),
    SIZE("Size", "साइज़", "aspect_ratio"),
    COLORS("Colors", "रंग", "palette"),
    WEAVE("Weave", "बुनाई", "grid_view"),
    EXPORT("Export", "शेयर", "share")
}

data class JacquardUiState(
    val activeTab: JacquardTab = JacquardTab.SKETCH,
    val sourceBitmap: Bitmap? = null,
    val sourceTitle: String = "Paisley (Kalka)",
    val config: JacquardConfig = JacquardConfig(hooks = 480, picks = 640, colorCount = 4),
    val currentDesign: JacquardDesign? = null,
    val renderedBitmap: Bitmap? = null,
    val isFabricSimulation: Boolean = false,
    val isProcessing: Boolean = false,
    val isDrawingPadOpen: Boolean = false,
    val editingYarn: YarnColor? = null,
    val isHindi: Boolean = false,
    val lastExportedPath: String? = null,
    val toastMessage: String? = null
)

class JacquardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(JacquardUiState())
    val uiState: StateFlow<JacquardUiState> = _uiState.asStateFlow()

    fun selectTab(tab: JacquardTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun toggleLanguage() {
        _uiState.value = _uiState.value.copy(isHindi = !_uiState.value.isHindi)
    }

    fun openDrawingPad(open: Boolean) {
        _uiState.value = _uiState.value.copy(isDrawingPadOpen = open)
    }

    fun setEditingYarn(yarn: YarnColor?) {
        _uiState.value = _uiState.value.copy(editingYarn = yarn)
    }

    fun toggleFabricSimulation(enabled: Boolean) {
        val state = _uiState.value
        val design = state.currentDesign ?: return
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                if (enabled) {
                    JacquardEngine.renderWeaveSimulation(design)
                } else {
                    JacquardEngine.renderColorBitmap(design)
                }
            }
            _uiState.value = _uiState.value.copy(
                isFabricSimulation = enabled,
                renderedBitmap = bmp
            )
        }
    }

    fun loadNewSketch(bitmap: Bitmap, title: String = "Jacquard Sketch") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sourceBitmap = bitmap,
                sourceTitle = title,
                isDrawingPadOpen = false,
                isProcessing = true
            )

            // Auto-adjust picks according to aspect ratio if locked
            val currentCfg = _uiState.value.config
            val adjustedPicks = if (currentCfg.isAspectLocked && bitmap.width > 0) {
                val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                (currentCfg.hooks * ratio).toInt().coerceIn(64, 3000)
            } else {
                currentCfg.picks
            }

            val updatedConfig = currentCfg.copy(picks = adjustedPicks)

            processDesignInternal(bitmap, title, updatedConfig)
        }
    }

    private var processJob: kotlinx.coroutines.Job? = null

    fun updateConfig(newConfig: JacquardConfig) {
        val currentBitmap = _uiState.value.sourceBitmap ?: return
        processJob?.cancel()
        processJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, config = newConfig)
            processDesignInternal(currentBitmap, _uiState.value.sourceTitle, newConfig)
        }
    }

    fun updateHooks(hooks: Int) {
        val currentCfg = _uiState.value.config
        val src = _uiState.value.sourceBitmap
        val adjustedPicks = if (currentCfg.isAspectLocked && src != null && src.width > 0) {
            val ratio = src.height.toFloat() / src.width.toFloat()
            (hooks * ratio).toInt().coerceIn(64, 3000)
        } else {
            currentCfg.picks
        }
        updateConfig(currentCfg.copy(hooks = hooks, picks = adjustedPicks))
    }

    fun updatePicks(picks: Int) {
        val currentCfg = _uiState.value.config
        updateConfig(currentCfg.copy(picks = picks))
    }

    fun updateColorCount(colors: Int) {
        val currentCfg = _uiState.value.config
        updateConfig(currentCfg.copy(colorCount = colors))
    }

    fun toggleStrayPixelCleaning(enabled: Boolean) {
        val currentCfg = _uiState.value.config
        updateConfig(currentCfg.copy(cleanStrayPixels = enabled))
    }

    fun updatePaletteColor(colorId: Int, newColor: Color, newName: String) {
        val currentDesign = _uiState.value.currentDesign ?: return
        viewModelScope.launch {
            val updatedDesign = JacquardEngine.updatePaletteColor(currentDesign, colorId, newColor, newName)
            val bmp = withContext(Dispatchers.Default) {
                if (_uiState.value.isFabricSimulation) {
                    JacquardEngine.renderWeaveSimulation(updatedDesign)
                } else {
                    JacquardEngine.renderColorBitmap(updatedDesign)
                }
            }
            _uiState.value = _uiState.value.copy(
                currentDesign = updatedDesign,
                renderedBitmap = bmp,
                editingYarn = null
            )
        }
    }

    fun updateColorWeave(colorId: Int, newWeave: WeaveType) {
        val currentDesign = _uiState.value.currentDesign ?: return
        viewModelScope.launch {
            val updatedDesign = JacquardEngine.updateColorWeave(currentDesign, colorId, newWeave)
            val bmp = withContext(Dispatchers.Default) {
                if (_uiState.value.isFabricSimulation) {
                    JacquardEngine.renderWeaveSimulation(updatedDesign)
                } else {
                    JacquardEngine.renderColorBitmap(updatedDesign)
                }
            }
            _uiState.value = _uiState.value.copy(
                currentDesign = updatedDesign,
                renderedBitmap = bmp
            )
        }
    }

    fun mergePaletteColors(sourceColorId: Int, targetColorId: Int) {
        val currentDesign = _uiState.value.currentDesign ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, editingYarn = null)
            val updatedDesign = withContext(Dispatchers.Default) {
                JacquardEngine.mergeColors(currentDesign, sourceColorId, targetColorId)
            }
            val bmp = withContext(Dispatchers.Default) {
                if (_uiState.value.isFabricSimulation) {
                    JacquardEngine.renderWeaveSimulation(updatedDesign)
                } else {
                    JacquardEngine.renderColorBitmap(updatedDesign)
                }
            }
            _uiState.value = _uiState.value.copy(
                currentDesign = updatedDesign,
                renderedBitmap = bmp,
                config = updatedDesign.config,
                isProcessing = false
            )
        }
    }

    fun shareBmpToWhatsApp(context: Context) {
        val design = _uiState.value.currentDesign ?: return
        ShareManager.shareBmpToWhatsApp(context, design, design.config.bmpFormat)
    }

    fun sharePictureToWhatsApp(context: Context) {
        val design = _uiState.value.currentDesign ?: return
        ShareManager.sharePictureToWhatsApp(context, design, _uiState.value.isFabricSimulation)
    }

    fun saveBmpToDownloads(context: Context) {
        val design = _uiState.value.currentDesign ?: return
        val path = ShareManager.saveBmpToDevice(context, design, design.config.bmpFormat)
        _uiState.value = _uiState.value.copy(lastExportedPath = path)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    private suspend fun processDesignInternal(bitmap: Bitmap, title: String, config: JacquardConfig) {
        val design = withContext(Dispatchers.Default) {
            JacquardEngine.processSketch(bitmap, title, config)
        }
        val bmp = withContext(Dispatchers.Default) {
            if (_uiState.value.isFabricSimulation) {
                JacquardEngine.renderWeaveSimulation(design)
            } else {
                JacquardEngine.renderColorBitmap(design)
            }
        }
        _uiState.value = _uiState.value.copy(
            currentDesign = design,
            renderedBitmap = bmp,
            config = config,
            isProcessing = false
        )
    }
}
