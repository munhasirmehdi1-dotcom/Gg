package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.model.BmpFormat
import com.example.model.JacquardDesign
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object BmpEncoder {

    /**
     * Encodes a JacquardDesign into standard Windows BMP file bytes.
     * Supports both 8-Bit Indexed BMP (with color palette table) and 24-Bit RGB BMP.
     */
    fun encodeJacquardToBmp(design: JacquardDesign, format: BmpFormat): ByteArray {
        val width = design.widthHooks
        val height = design.heightPicks
        val bos = ByteArrayOutputStream()

        if (format == BmpFormat.INDEXED_8BIT && design.palette.size <= 256) {
            writeIndexed8BitBmp(design, bos)
        } else {
            writeTrueColor24BitBmp(design, bos)
        }

        return bos.toByteArray()
    }

    /**
     * Encodes any Android Bitmap into 24-bit Windows BMP bytes.
     */
    fun encodeBitmapToBmp(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val bos = ByteArrayOutputStream()

        val rowStride = (width * 3 + 3) / 4 * 4
        val imageSize = rowStride * height
        val fileSize = 54 + imageSize

        // 14-byte File Header
        writeLittleEndianShort(bos, 0x4D42) // "BM"
        writeLittleEndianInt(bos, fileSize)
        writeLittleEndianShort(bos, 0)      // Reserved 1
        writeLittleEndianShort(bos, 0)      // Reserved 2
        writeLittleEndianInt(bos, 54)        // Offset to image data

        // 40-byte Info Header
        writeLittleEndianInt(bos, 40)        // biSize
        writeLittleEndianInt(bos, width)     // biWidth
        writeLittleEndianInt(bos, height)    // biHeight
        writeLittleEndianShort(bos, 1)       // biPlanes
        writeLittleEndianShort(bos, 24)      // biBitCount
        writeLittleEndianInt(bos, 0)         // biCompression (BI_RGB)
        writeLittleEndianInt(bos, imageSize) // biSizeImage
        writeLittleEndianInt(bos, 2835)      // biXPelsPerMeter (72 dpi)
        writeLittleEndianInt(bos, 2835)      // biYPelsPerMeter (72 dpi)
        writeLittleEndianInt(bos, 0)         // biClrUsed
        writeLittleEndianInt(bos, 0)         // biClrImportant

        val pixels = IntArray(width)
        val padLength = rowStride - width * 3
        val padding = ByteArray(padLength)

        // BMP rows are stored bottom-to-top
        for (y in height - 1 downTo 0) {
            bitmap.getPixels(pixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val color = pixels[x]
                bos.write(color and 0xFF)         // Blue
                bos.write((color shr 8) and 0xFF)  // Green
                bos.write((color shr 16) and 0xFF) // Red
            }
            if (padLength > 0) {
                bos.write(padding)
            }
        }

        return bos.toByteArray()
    }

    private fun writeIndexed8BitBmp(design: JacquardDesign, out: OutputStream) {
        val width = design.widthHooks
        val height = design.heightPicks
        val palette = design.palette

        val rowStride = (width + 3) / 4 * 4
        val imageSize = rowStride * height
        val paletteEntries = 256
        val paletteSize = paletteEntries * 4
        val dataOffset = 14 + 40 + paletteSize
        val fileSize = dataOffset + imageSize

        val xPelsPerMeter = (design.config.epi * 39.3701f).toInt().coerceAtLeast(2835)
        val yPelsPerMeter = (design.config.ppi * 39.3701f).toInt().coerceAtLeast(2835)

        // 14-byte File Header
        writeLittleEndianShort(out, 0x4D42) // "BM"
        writeLittleEndianInt(out, fileSize)
        writeLittleEndianShort(out, 0)
        writeLittleEndianShort(out, 0)
        writeLittleEndianInt(out, dataOffset)

        // 40-byte Info Header
        writeLittleEndianInt(out, 40)
        writeLittleEndianInt(out, width)
        writeLittleEndianInt(out, height)
        writeLittleEndianShort(out, 1)
        writeLittleEndianShort(out, 8)       // 8 bits per pixel
        writeLittleEndianInt(out, 0)         // BI_RGB
        writeLittleEndianInt(out, imageSize)
        writeLittleEndianInt(out, xPelsPerMeter)
        writeLittleEndianInt(out, yPelsPerMeter)
        writeLittleEndianInt(out, paletteEntries)
        writeLittleEndianInt(out, palette.size)

        // Write 256 Color Palette Table (B, G, R, 0)
        for (i in 0 until paletteEntries) {
            if (i < palette.size) {
                val argb = palette[i].argb
                out.write(argb and 0xFF)         // Blue
                out.write((argb shr 8) and 0xFF)  // Green
                out.write((argb shr 16) and 0xFF) // Red
                out.write(0) // reserved
            } else {
                // Unused palette entries padded with black
                out.write(0)
                out.write(0)
                out.write(0)
                out.write(0)
            }
        }

        // Write pixel data (bottom to top)
        val padLength = rowStride - width
        val padding = ByteArray(padLength)
        val indices = design.pixelIndices

        for (y in height - 1 downTo 0) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val colorIndex = indices[rowOffset + x].coerceIn(0, 255)
                out.write(colorIndex)
            }
            if (padLength > 0) {
                out.write(padding)
            }
        }
    }

    private fun writeTrueColor24BitBmp(design: JacquardDesign, out: OutputStream) {
        val width = design.widthHooks
        val height = design.heightPicks
        val palette = design.palette
        val indices = design.pixelIndices

        val rowStride = (width * 3 + 3) / 4 * 4
        val imageSize = rowStride * height
        val fileSize = 54 + imageSize

        val xPelsPerMeter = (design.config.epi * 39.3701f).toInt().coerceAtLeast(2835)
        val yPelsPerMeter = (design.config.ppi * 39.3701f).toInt().coerceAtLeast(2835)

        // 14-byte File Header
        writeLittleEndianShort(out, 0x4D42)
        writeLittleEndianInt(out, fileSize)
        writeLittleEndianShort(out, 0)
        writeLittleEndianShort(out, 0)
        writeLittleEndianInt(out, 54)

        // 40-byte Info Header
        writeLittleEndianInt(out, 40)
        writeLittleEndianInt(out, width)
        writeLittleEndianInt(out, height)
        writeLittleEndianShort(out, 1)
        writeLittleEndianShort(out, 24)      // 24-bit RGB
        writeLittleEndianInt(out, 0)
        writeLittleEndianInt(out, imageSize)
        writeLittleEndianInt(out, xPelsPerMeter)
        writeLittleEndianInt(out, yPelsPerMeter)
        writeLittleEndianInt(out, 0)
        writeLittleEndianInt(out, 0)

        // Pre-extract palette argb
        val paletteArgb = IntArray(palette.size) { palette[it].argb }
        val padLength = rowStride - width * 3
        val padding = ByteArray(padLength)

        // Bottom to top
        for (y in height - 1 downTo 0) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val pIdx = indices[rowOffset + x].coerceIn(0, palette.size - 1)
                val argb = paletteArgb[pIdx]
                out.write(argb and 0xFF)         // Blue
                out.write((argb shr 8) and 0xFF)  // Green
                out.write((argb shr 16) and 0xFF) // Red
            }
            if (padLength > 0) {
                out.write(padding)
            }
        }
    }

    fun saveBmpToFile(design: JacquardDesign, format: BmpFormat, targetFile: File) {
        FileOutputStream(targetFile).use { fos ->
            val bytes = encodeJacquardToBmp(design, format)
            fos.write(bytes)
            fos.flush()
        }
    }

    private fun writeLittleEndianShort(out: OutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
    }

    private fun writeLittleEndianInt(out: OutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value shr 8) and 0xFF)
        out.write((value shr 16) and 0xFF)
        out.write((value shr 24) and 0xFF)
    }
}
