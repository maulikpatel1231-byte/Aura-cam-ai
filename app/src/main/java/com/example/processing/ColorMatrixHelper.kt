package com.example.processing

import android.graphics.ColorMatrix as AndroidColorMatrix
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import com.example.model.FilterPreset
import com.example.model.ImageAdjustments

object ColorMatrixHelper {

    fun getPresetMatrix(preset: FilterPreset): FloatArray {
        return when (preset) {
            FilterPreset.ORIGINAL -> identityMatrix()

            FilterPreset.AI_AUTO -> floatArrayOf(
                1.08f, 0.00f, 0.00f, 0f, 6f,
                0.00f, 1.05f, 0.00f, 0f, 4f,
                0.00f, 0.00f, 1.02f, 0f, 2f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterPreset.CINEMA_TEAL_ORANGE -> floatArrayOf(
                1.15f, 0.05f, -0.10f, 0f, 10f,
                0.02f, 1.08f, 0.05f,  0f, 0f,
                -0.10f, 0.12f, 1.25f, 0f, 14f,
                0.00f, 0.00f, 0.00f,  1f, 0f
            )

            FilterPreset.GOLDEN_HOUR -> floatArrayOf(
                1.22f, 0.05f, -0.05f, 0f, 18f,
                0.05f, 1.10f, 0.00f,  0f, 8f,
                -0.10f, -0.05f, 0.85f, 0f, -12f,
                0.00f, 0.00f, 0.00f,  1f, 0f
            )

            FilterPreset.LEICA_NOIR -> {
                val r = 0.299f * 1.25f
                val g = 0.587f * 1.25f
                val b = 0.114f * 1.25f
                floatArrayOf(
                    r, g, b, 0f, -15f,
                    r, g, b, 0f, -15f,
                    r, g, b, 0f, -15f,
                    0f, 0f, 0f, 1f, 0f
                )
            }

            FilterPreset.PORTRA_35MM -> floatArrayOf(
                1.10f, 0.02f, -0.02f, 0f, 12f,
                0.00f, 1.06f, 0.02f,  0f, 6f,
                -0.04f, 0.00f, 0.95f, 0f, 8f,
                0.00f, 0.00f, 0.00f,  1f, 0f
            )

            FilterPreset.CYBERPUNK -> floatArrayOf(
                1.25f, -0.10f, 0.20f, 0f, 15f,
                -0.15f, 0.90f, 0.35f, 0f, -10f,
                0.10f, 0.20f, 1.45f,  0f, 25f,
                0.00f, 0.00f, 0.00f,  1f, 0f
            )

            FilterPreset.PASTEL_DREAM -> floatArrayOf(
                0.95f, 0.05f, 0.05f, 0f, 24f,
                0.02f, 0.98f, 0.05f, 0f, 22f,
                0.05f, 0.02f, 1.05f, 0f, 28f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterPreset.EMERALD_MOOD -> floatArrayOf(
                0.90f, 0.05f, -0.05f, 0f, -5f,
                0.05f, 1.20f, 0.05f,  0f, 10f,
                -0.05f, 0.05f, 0.95f, 0f, 4f,
                0.00f, 0.00f, 0.00f,  1f, 0f
            )

            FilterPreset.VIVID_STREET -> floatArrayOf(
                1.20f, -0.05f, -0.05f, 0f, 0f,
                -0.05f, 1.20f, -0.05f, 0f, 0f,
                -0.05f, -0.05f, 1.20f, 0f, 0f,
                0.00f, 0.00f, 0.00f,   1f, 0f
            )

            FilterPreset.RETRO_70S -> floatArrayOf(
                1.12f, 0.10f, 0.00f, 0f, 20f,
                0.08f, 1.02f, 0.00f, 0f, 15f,
                0.00f, 0.05f, 0.80f, 0f, 5f,
                0.00f, 0.00f, 0.00f, 1f, 0f
            )

            FilterPreset.DRAMATIC_HDR -> floatArrayOf(
                1.22f, -0.08f, -0.08f, 0f, 8f,
                -0.08f, 1.22f, -0.08f, 0f, 8f,
                -0.08f, -0.08f, 1.22f, 0f, 8f,
                0.00f, 0.00f, 0.00f,   1f, 0f
            )
        }
    }

    fun buildAndroidColorMatrix(adjustments: ImageAdjustments): AndroidColorMatrix {
        val result = AndroidColorMatrix()
        result.set(getPresetMatrix(adjustments.filterPreset))

        if (adjustments.exposure != 0f) {
            val brightnessShift = (adjustments.exposure / 100f) * 100f
            val expMatrix = AndroidColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightnessShift,
                    0f, 1f, 0f, 0f, brightnessShift,
                    0f, 0f, 1f, 0f, brightnessShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.postConcat(expMatrix)
        }

        if (adjustments.contrast != 0f) {
            val scale = if (adjustments.contrast > 0) {
                1f + (adjustments.contrast / 100f) * 1.5f
            } else {
                1f + (adjustments.contrast / 100f) * 0.7f
            }
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = AndroidColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.postConcat(contrastMatrix)
        }

        if (adjustments.saturation != 0f) {
            val satVal = if (adjustments.saturation >= 0) {
                1f + (adjustments.saturation / 100f) * 1.2f
            } else {
                (100f + adjustments.saturation) / 100f
            }
            val satMatrix = AndroidColorMatrix()
            satMatrix.setSaturation(satVal.coerceAtLeast(0f))
            result.postConcat(satMatrix)
        }

        if (adjustments.warmth != 0f) {
            val w = adjustments.warmth / 100f
            val redScale = 1f + w * 0.25f
            val blueScale = 1f - w * 0.25f
            val warmthMatrix = AndroidColorMatrix(
                floatArrayOf(
                    redScale, 0f, 0f, 0f, w * 15f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, blueScale, 0f, -w * 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.postConcat(warmthMatrix)
        }

        if (adjustments.tint != 0f) {
            val t = adjustments.tint / 100f
            val greenScale = 1f - t * 0.2f
            val tintMatrix = AndroidColorMatrix(
                floatArrayOf(
                    1f + t * 0.1f, 0f, 0f, 0f, t * 10f,
                    0f, greenScale, 0f, 0f, -t * 10f,
                    0f, 0f, 1f + t * 0.1f, 0f, t * 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            result.postConcat(tintMatrix)
        }

        return result
    }

    fun buildComposeColorMatrix(adjustments: ImageAdjustments): ComposeColorMatrix {
        val androidMatrix = buildAndroidColorMatrix(adjustments)
        return ComposeColorMatrix(androidMatrix.array)
    }

    private fun identityMatrix(): FloatArray {
        return floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    }
}
