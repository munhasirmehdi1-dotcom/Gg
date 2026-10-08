package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class SampleMotifItem(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val drawableResId: Int? = null,
    val isProcedural: Boolean = false
)

val SAMPLE_MOTIFS = listOf(
    SampleMotifItem("paisley", "Paisley (Kalka)", "पारम्परिक कालका", R.drawable.sample_paisley),
    SampleMotifItem("damask", "Floral Damask", "फूलदार दमास्क", R.drawable.sample_damask),
    SampleMotifItem("geometric", "Diamond Weave", "ज्यामितीय जाली", R.drawable.sample_geometric),
    SampleMotifItem("border", "Saree Border", "साड़ी बॉर्डर", null, isProcedural = true),
    SampleMotifItem("peacock", "Peacock Motif", "मयूर डिज़ाइन", null, isProcedural = true)
)

@Composable
fun SampleMotifSelector(
    onSelectSample: (Bitmap, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Or Choose Sample Textile Sketch / तैयार स्केच चुनें:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(SAMPLE_MOTIFS) { item ->
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val bmp = loadSampleBitmap(context, item)
                            onSelectSample(bmp, item.titleEn)
                        }
                        .testTag("sample_item_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(118.dp, 80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.drawableResId != null) {
                                Image(
                                    painter = painterResource(item.drawableResId),
                                    contentDescription = item.titleEn,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    if (item.id == "border") "⚜️ Border" else "🦚 Peacock",
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(item.titleEn, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        Text(item.titleHi, style = MaterialTheme.typography.labelSmall, maxLines = 1, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

fun loadSampleBitmap(context: Context, item: SampleMotifItem): Bitmap {
    if (item.drawableResId != null) {
        try {
            val bmp = BitmapFactory.decodeResource(context.resources, item.drawableResId)
            if (bmp != null) return bmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Procedural generation fallback for border and peacock motifs
    val size = 600
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(AndroidColor.WHITE)

    val paint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.BLACK
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    val fillPaint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.rgb(180, 40, 40)
        style = Paint.Style.FILL
    }

    val goldPaint = Paint().apply {
        isAntiAlias = true
        color = AndroidColor.rgb(212, 175, 55)
        style = Paint.Style.FILL
    }

    if (item.id == "border") {
        // Draw ornate border pattern
        for (i in 0 until 5) {
            val y = 60f + i * 110f
            canvas.drawRect(40f, y, 560f, y + 80f, paint)
            canvas.drawCircle(300f, y + 40f, 30f, fillPaint)
            canvas.drawCircle(180f, y + 40f, 20f, goldPaint)
            canvas.drawCircle(420f, y + 40f, 20f, goldPaint)
        }
    } else {
        // Draw ornate peacock feather motif
        canvas.drawCircle(300f, 240f, 120f, paint)
        canvas.drawCircle(300f, 240f, 90f, fillPaint)
        canvas.drawCircle(300f, 240f, 60f, goldPaint)
        canvas.drawCircle(300f, 240f, 30f, Paint().apply { color = AndroidColor.rgb(20, 60, 160) })
        canvas.drawLine(300f, 360f, 300f, 560f, paint.apply { strokeWidth = 14f })
    }

    return bmp
}
