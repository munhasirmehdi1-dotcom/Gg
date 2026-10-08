package com.example

import androidx.compose.ui.graphics.Color
import com.example.engine.BmpEncoder
import com.example.engine.JacquardEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testJacquardConfigDimensionCalculations() {
        val config = JacquardConfig(
            hooks = 960,
            picks = 1200,
            epi = 60,
            ppi = 80
        )
        // 960 / 60 = 16.0 inches
        assertEquals(16.0f, config.widthInches, 0.01f)
        // 1200 / 80 = 15.0 inches
        assertEquals(15.0f, config.heightInches, 0.01f)
        // 16.0 * 2.54 = 40.64 cm
        assertEquals(40.64f, config.widthCm, 0.05f)
        // 15.0 * 2.54 = 38.1 cm
        assertEquals(38.1f, config.heightCm, 0.05f)
    }

    @Test
    fun testWeaveTypePatterns() {
        // Plain weave: checkerboard (x + y) % 2 == 0
        assertTrue(WeaveType.PLAIN_1_1.isWarpUp(0, 0))
        assertFalse(WeaveType.PLAIN_1_1.isWarpUp(0, 1))
        assertTrue(WeaveType.PLAIN_1_1.isWarpUp(1, 1))

        // Twill 2/2 has 2 warp up, 2 weft up
        assertTrue(WeaveType.TWILL_2_2.isWarpUp(0, 0))
        assertTrue(WeaveType.TWILL_2_2.isWarpUp(1, 0))
        assertFalse(WeaveType.TWILL_2_2.isWarpUp(2, 0))
        assertFalse(WeaveType.TWILL_2_2.isWarpUp(3, 0))
    }

    @Test
    fun testBmpEncodingHeaders() {
        val width = 16
        val height = 16
        val indices = IntArray(width * height) { 0 }
        val palette = listOf(
            YarnColor(0, "Gold", Color(0xFFFFD700)),
            YarnColor(1, "Maroon", Color(0xFF800000))
        )
        val config = JacquardConfig(hooks = width, picks = height, colorCount = 2)
        val design = JacquardDesign("Test Motif", width, height, indices, palette, config)

        // 8-Bit Indexed BMP test
        val bmp8Bytes = BmpEncoder.encodeJacquardToBmp(design, BmpFormat.INDEXED_8BIT)
        assertTrue(bmp8Bytes.size > 54 + 256 * 4)
        assertEquals('B'.code.toByte(), bmp8Bytes[0])
        assertEquals('M'.code.toByte(), bmp8Bytes[1])

        // 24-Bit RGB BMP test
        val bmp24Bytes = BmpEncoder.encodeJacquardToBmp(design, BmpFormat.TRUECOLOR_24BIT)
        assertTrue(bmp24Bytes.size > 54)
        assertEquals('B'.code.toByte(), bmp24Bytes[0])
        assertEquals('M'.code.toByte(), bmp24Bytes[1])
    }

    @Test
    fun testStrayPixelCleaning() {
        // Create 5x5 grid with all 0s, except center pixel at (2,2) is 1 (isolated noise)
        val w = 5
        val h = 5
        val grid = IntArray(w * h) { 0 }
        grid[2 * w + 2] = 1 // stray noise pixel

        val cleaned = JacquardEngine.cleanStrayPixels(grid, w, h)
        // The isolated noise pixel should be replaced by dominant neighbor (0)
        assertEquals(0, cleaned[2 * w + 2])
    }
}
