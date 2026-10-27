package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

enum class FilterPreset(
    val id: String,
    val displayName: String,
    val description: String,
    val accentColor: Color,
    val category: String
) {
    ORIGINAL(
        id = "original",
        displayName = "Original",
        description = "Natural raw capture without alterations",
        accentColor = Color(0xFF94A3B8),
        category = "Standard"
    ),
    AI_AUTO(
        id = "ai_auto",
        displayName = "AI Auto-Master",
        description = "Intelligent scene-aware exposure and tonal balancing",
        accentColor = Color(0xFF8B5CF6),
        category = "AI Magic"
    ),
    CINEMA_TEAL_ORANGE(
        id = "cinema_teal_orange",
        displayName = "Cinema Teal",
        description = "Hollywood blockbuster teal shadows and warm skin tones",
        accentColor = Color(0xFF06B6D4),
        category = "Cinematic"
    ),
    GOLDEN_HOUR(
        id = "golden_hour",
        displayName = "Golden Glow",
        description = "Warm amber glow with soft sunset highlights",
        accentColor = Color(0xFFF59E0B),
        category = "Atmosphere"
    ),
    LEICA_NOIR(
        id = "leica_noir",
        displayName = "Leica Noir",
        description = "High contrast monochrome with velvety deep blacks",
        accentColor = Color(0xFFE2E8F0),
        category = "Monochrome"
    ),
    PORTRA_35MM(
        id = "portra_35mm",
        displayName = "Portra 35mm",
        description = "Classic analog film tones with gentle contrast",
        accentColor = Color(0xFFFB923C),
        category = "Film"
    ),
    CYBERPUNK(
        id = "cyberpunk",
        displayName = "Cyberpunk",
        description = "Electric neon violets, blues and punchy shadows",
        accentColor = Color(0xFFA855F7),
        category = "Creative"
    ),
    PASTEL_DREAM(
        id = "pastel_dream",
        displayName = "Pastel Dream",
        description = "Soft luminous highlights and gentle pastel tones",
        accentColor = Color(0xFFF472B6),
        category = "Dreamy"
    ),
    EMERALD_MOOD(
        id = "emerald_mood",
        displayName = "Emerald Mood",
        description = "Deep forest greens, rich shadows and calm tones",
        accentColor = Color(0xFF10B981),
        category = "Nature"
    ),
    VIVID_STREET(
        id = "vivid_street",
        displayName = "Vivid Street",
        description = "Punchy high-contrast pop for urban street photography",
        accentColor = Color(0xFFEF4444),
        category = "Urban"
    ),
    RETRO_70S(
        id = "retro_70s",
        displayName = "Retro 70s",
        description = "Warm nostalgic fade with soft warm vignette",
        accentColor = Color(0xFFD97706),
        category = "Vintage"
    ),
    DRAMATIC_HDR(
        id = "dramatic_hdr",
        displayName = "Dynamic HDR",
        description = "Boosted dynamic range, crisp texture and shadow details",
        accentColor = Color(0xFF38BDF8),
        category = "HDR"
    )
}

data class ImageAdjustments(
    val exposure: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val warmth: Float = 0f,
    val tint: Float = 0f,
    val vignette: Float = 0f,
    val filterPreset: FilterPreset = FilterPreset.ORIGINAL
) {
    val isDefault: Boolean
        get() = exposure == 0f &&
                contrast == 0f &&
                saturation == 0f &&
                warmth == 0f &&
                tint == 0f &&
                vignette == 0f &&
                filterPreset == FilterPreset.ORIGINAL
}

data class AIAnalysisResult(
    val sceneType: String,
    val commentary: String,
    val suggestedFilter: FilterPreset,
    val recommendedAdjustments: ImageAdjustments,
    val isAIGenerated: Boolean = true,
    val promptUsed: String? = null
)

data class CapturedPhoto(
    val id: String,
    val bitmap: Bitmap,
    val adjustments: ImageAdjustments = ImageAdjustments(),
    val aiAnalysis: AIAnalysisResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DemoPhotoSample(
    val id: String,
    val title: String,
    val category: String,
    val baseColorHex: Long
)
