package com.example.processing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.example.model.FilterPreset
import com.example.model.ImageAdjustments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ImageProcessor {

    suspend fun applyAdjustments(
        source: Bitmap,
        adjustments: ImageAdjustments
    ): Bitmap = withContext(Dispatchers.Default) {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val matrix = ColorMatrixHelper.buildAndroidColorMatrix(adjustments)
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        if (adjustments.vignette > 0f) {
            val vignetteAlpha = ((adjustments.vignette / 100f) * 180f).toInt().coerceIn(0, 255)
            val cx = source.width / 2f
            val cy = source.height / 2f
            val radius = max(cx, cy) * 1.3f

            val vignetteShader = RadialGradient(
                cx, cy, radius,
                intArrayOf(AndroidColor.TRANSPARENT, AndroidColor.argb(vignetteAlpha, 0, 0, 0)),
                floatArrayOf(0.45f, 1.0f),
                Shader.TileMode.CLAMP
            )

            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = vignetteShader
            }
            canvas.drawRect(0f, 0f, source.width.toFloat(), source.height.toFloat(), vignettePaint)
        }

        output
    }

    suspend fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 640): String = withContext(Dispatchers.Default) {
        val scaled = scaleBitmap(bitmap, maxDimension)
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun scaleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= maxDimension && height <= maxDimension) {
            return source
        }
        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    suspend fun saveToGallery(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val filename = "AuraCam_${System.currentTimeMillis()}.jpg"
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AuraCam AI")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext null

                resolver.openOutputStream(imageUri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
                return@withContext imageUri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val auraDir = File(picturesDir, "AuraCam AI")
                if (!auraDir.exists()) auraDir.mkdirs()
                val file = File(auraDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
                }
                return@withContext Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun analyzeHistogramLocally(bitmap: Bitmap): ImageAdjustments {
        val sample = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val pixels = IntArray(sample.width * sample.height)
        sample.getPixels(pixels, 0, sample.width, 0, 0, sample.width, sample.height)

        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var totalLum = 0.0

        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            totalR += r
            totalG += g
            totalB += b
            totalLum += 0.299 * r + 0.587 * g + 0.114 * b
        }

        val count = pixels.size
        val avgR = totalR / count
        val avgG = totalG / count
        val avgB = totalB / count
        val avgLum = totalLum / count

        var exposureAdj: Float
        var contrastAdj: Float
        val saturationAdj: Float = 10f
        var warmthAdj: Float

        if (avgLum < 90) {
            exposureAdj = ((115 - avgLum) / 115f * 35f).toFloat()
            contrastAdj = 10f
        } else if (avgLum > 175) {
            exposureAdj = -((avgLum - 170) / 85f * 25f).toFloat()
            contrastAdj = 20f
        } else {
            exposureAdj = 8f
            contrastAdj = 14f
        }

        if (avgB > avgR + 15) {
            warmthAdj = 18f
        } else if (avgR > avgB + 25) {
            warmthAdj = -10f
        } else {
            warmthAdj = 12f
        }

        return ImageAdjustments(
            exposure = exposureAdj.coerceIn(-50f, 50f),
            contrast = contrastAdj.coerceIn(-50f, 50f),
            saturation = saturationAdj.coerceIn(-50f, 50f),
            warmth = warmthAdj.coerceIn(-50f, 50f),
            tint = 2f,
            vignette = 12f,
            filterPreset = FilterPreset.AI_AUTO
        )
    }

    fun createDemoPhoto(type: Int): Bitmap {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            0 -> {
                val skyGradient = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        AndroidColor.parseColor("#1E1B4B"),
                        AndroidColor.parseColor("#4338CA"),
                        AndroidColor.parseColor("#BE185D"),
                        AndroidColor.parseColor("#F97316"),
                        AndroidColor.parseColor("#FDE047")
                    ),
                    floatArrayOf(0f, 0.25f, 0.55f, 0.8f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyGradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.shader = null
                paint.color = AndroidColor.parseColor("#FFFBEB")
                paint.setShadowLayer(40f, 0f, 0f, AndroidColor.parseColor("#F59E0B"))
                canvas.drawCircle(width * 0.5f, height * 0.72f, 110f, paint)
                paint.clearShadowLayer()

                paint.color = AndroidColor.parseColor("#0F172A")
                val path = android.graphics.Path().apply {
                    moveTo(0f, height * 0.82f)
                    lineTo(width * 0.3f, height * 0.75f)
                    lineTo(width * 0.6f, height * 0.84f)
                    lineTo(width * 0.85f, height * 0.78f)
                    lineTo(width.toFloat(), height * 0.85f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(path, paint)
            }
            1 -> {
                val bg = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(AndroidColor.parseColor("#18181B"), AndroidColor.parseColor("#27272A"), AndroidColor.parseColor("#3F3F46")),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = bg
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.shader = null
                paint.color = AndroidColor.parseColor("#E4D5B7")
                canvas.drawCircle(width * 0.5f, height * 0.42f, 180f, paint)
                val body = android.graphics.Path().apply {
                    moveTo(width * 0.15f, height.toFloat())
                    quadTo(width * 0.25f, height * 0.62f, width * 0.5f, height * 0.62f)
                    quadTo(width * 0.75f, height * 0.62f, width * 0.85f, height.toFloat())
                    close()
                }
                paint.color = AndroidColor.parseColor("#1E293B")
                canvas.drawPath(body, paint)

                val bokehColors = intArrayOf(
                    AndroidColor.parseColor("#44F59E0B"),
                    AndroidColor.parseColor("#33EC4899"),
                    AndroidColor.parseColor("#3338BDF8")
                )
                paint.color = bokehColors[0]
                canvas.drawCircle(width * 0.2f, height * 0.25f, 130f, paint)
                paint.color = bokehColors[1]
                canvas.drawCircle(width * 0.82f, height * 0.35f, 160f, paint)
                paint.color = bokehColors[2]
                canvas.drawCircle(width * 0.75f, height * 0.18f, 90f, paint)
            }
            else -> {
                val bg = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(AndroidColor.parseColor("#090A0F"), AndroidColor.parseColor("#150A26"), AndroidColor.parseColor("#051923")),
                    null, Shader.TileMode.CLAMP
                )
                paint.shader = bg
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.shader = null
                paint.color = AndroidColor.parseColor("#E11D48")
                paint.strokeWidth = 14f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(width * 0.1f, height * 0.85f, width * 0.45f, height * 0.55f, paint)

                paint.color = AndroidColor.parseColor("#06B6D4")
                paint.strokeWidth = 20f
                canvas.drawLine(width * 0.9f, height * 0.9f, width * 0.55f, height * 0.55f, paint)

                paint.color = AndroidColor.parseColor("#A855F7")
                paint.strokeWidth = 12f
                canvas.drawLine(width * 0.5f, height * 0.95f, width * 0.5f, height * 0.6f, paint)

                paint.style = Paint.Style.FILL
            }
        }

        return bitmap
    }
}
