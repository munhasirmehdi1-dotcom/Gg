package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class BmpFormat(val label: String, val description: String) {
    INDEXED_8BIT("8-Bit Indexed BMP", "Standard for Bonas, Staubli & Textile CAD (with Color Palette)"),
    TRUECOLOR_24BIT("24-Bit RGB BMP", "Full color Windows Bitmap")
}

enum class WeaveType(val labelEn: String, val labelHi: String, val matrixSize: Int) {
    PLAIN_1_1("Plain 1/1", "सादा बुनाई (Plain)", 2),
    TWILL_2_2("Twill 2/2", "ट्विल 2/2 (Twill)", 4),
    TWILL_3_1("Twill 3/1", "ट्विल 3/1", 4),
    SATIN_5("Satin 5", "साटन 5 (Satin 5)", 5),
    SATIN_8("Satin 8", "साटन 8 (Satin 8)", 8),
    BASKET_2_2("Basket 2/2", "बास्केट (Basket)", 4),
    COLOR_ONLY("Solid Color", "ठोस रंग (No Weave)", 1);

    /**
     * Returns true if warp yarn is UP (over weft) at (x, y) relative to weave pattern.
     */
    fun isWarpUp(x: Int, y: Int): Boolean {
        return when (this) {
            PLAIN_1_1 -> (x + y) % 2 == 0
            TWILL_2_2 -> {
                val modX = (x % 4 + 4) % 4
                val modY = (y % 4 + 4) % 4
                val p = (modX + modY) % 4
                p == 0 || p == 1
            }
            TWILL_3_1 -> {
                val modX = (x % 4 + 4) % 4
                val modY = (y % 4 + 4) % 4
                val p = (modX + modY) % 4
                p < 3
            }
            SATIN_5 -> {
                // 5-end satin step = 2
                val modX = (x % 5 + 5) % 5
                val modY = (y % 5 + 5) % 5
                (modX == (modY * 2) % 5)
            }
            SATIN_8 -> {
                // 8-end satin step = 3
                val modX = (x % 8 + 8) % 8
                val modY = (y % 8 + 8) % 8
                (modX == (modY * 3) % 8)
            }
            BASKET_2_2 -> {
                val bx = (x / 2) % 2
                val by = (y / 2) % 2
                bx == by
            }
            COLOR_ONLY -> true
        }
    }
}

data class YarnColor(
    val id: Int,
    val name: String,
    val color: Color,
    val pixelCount: Int = 0,
    val percentage: Float = 0f,
    val assignedWeave: WeaveType = WeaveType.PLAIN_1_1
) {
    val argb: Int
        get() {
            val a = (color.alpha * 255).toInt().coerceIn(0, 255)
            val r = (color.red * 255).toInt().coerceIn(0, 255)
            val g = (color.green * 255).toInt().coerceIn(0, 255)
            val b = (color.blue * 255).toInt().coerceIn(0, 255)
            return (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    val hexCode: String
        get() {
            val a = (color.alpha * 255).toInt()
            val r = (color.red * 255).toInt()
            val g = (color.green * 255).toInt()
            val b = (color.blue * 255).toInt()
            return String.format("#%02X%02X%02X", r, g, b)
        }
}

data class JacquardConfig(
    val hooks: Int = 960,             // Warp ends / hooks (width)
    val picks: Int = 1200,            // Weft picks (height)
    val colorCount: Int = 4,          // Number of distinct colors
    val epi: Int = 60,                // Ends Per Inch (warp density)
    val ppi: Int = 80,                // Picks Per Inch (weft density)
    val isAspectLocked: Boolean = true,
    val cleanStrayPixels: Boolean = true,
    val bmpFormat: BmpFormat = BmpFormat.INDEXED_8BIT
) {
    val widthInches: Float get() = hooks.toFloat() / epi.coerceAtLeast(1)
    val heightInches: Float get() = picks.toFloat() / ppi.coerceAtLeast(1)
    val widthCm: Float get() = widthInches * 2.54f
    val heightCm: Float get() = heightInches * 2.54f
}

data class JacquardDesign(
    val title: String,
    val widthHooks: Int,
    val heightPicks: Int,
    val pixelIndices: IntArray,       // Each entry is index into palette (0 until palette.size)
    val palette: List<YarnColor>,
    val config: JacquardConfig
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JacquardDesign

        if (title != other.title) return false
        if (widthHooks != other.widthHooks) return false
        if (heightPicks != other.heightPicks) return false
        if (!pixelIndices.contentEquals(other.pixelIndices)) return false
        if (palette != other.palette) return false
        if (config != other.config) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + widthHooks
        result = 31 * result + heightPicks
        result = 31 * result + pixelIndices.contentHashCode()
        result = 31 * result + palette.hashCode()
        result = 31 * result + config.hashCode()
        return result
    }
}
