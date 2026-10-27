package com.example.ui

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AIEnhanceService
import com.example.model.AIAnalysisResult
import com.example.model.FilterPreset
import com.example.model.ImageAdjustments
import com.example.processing.ImageProcessor
import com.example.ui.components.AIInsightBanner
import com.example.ui.components.AIPromptDialog
import com.example.ui.components.AdjustmentSliderRow
import com.example.ui.components.BeforeAfterImageViewer
import com.example.ui.components.FilterPresetCarousel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    bitmap: Bitmap,
    initialPreset: FilterPreset = FilterPreset.AI_AUTO,
    openAiKey: String = "",
    onBackToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var adjustments by remember { mutableStateOf(ImageAdjustments(filterPreset = initialPreset)) }
    var aiAnalysis by remember { mutableStateOf<AIAnalysisResult?>(null) }
    var isAnalyzingAI by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var isSplitMode by remember { mutableStateOf(false) }
    var showAdjustmentsSheet by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }

    BackHandler {
        onBackToCamera()
    }

    LaunchedEffect(bitmap) {
        isAnalyzingAI = true
        val result = AIEnhanceService.autoEnhancePhoto(bitmap, null, openAiKey)
        aiAnalysis = result
        adjustments = result.recommendedAdjustments
        isAnalyzingAI = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("editor_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToCamera,
                    modifier = Modifier.testTag("back_to_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = ElectricIndigo.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showPromptDialog = true }
                            .testTag("ai_prompt_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Prompt",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = { isSplitMode = !isSplitMode },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isSplitMode) ElectricIndigo else DarkSurfaceElevated,
                                CircleShape
                            )
                            .testTag("split_mode_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Split View",
                            tint = if (isSplitMode) Color.White else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val editedBitmap = ImageProcessor.applyAdjustments(bitmap, adjustments)
                                val uri = ImageProcessor.saveToGallery(context, editedBitmap)
                                if (uri != null) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share AI Photo"))
                                }
                            }
                        },
                        modifier = Modifier.testTag("share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                coroutineScope.launch {
                                    val editedBitmap = ImageProcessor.applyAdjustments(bitmap, adjustments)
                                    val uri = ImageProcessor.saveToGallery(context, editedBitmap)
                                    isSaving = false
                                    if (uri != null) {
                                        Toast.makeText(context, "Saved to Gallery (Pictures/AuraCam AI)", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Error saving photo", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(listOf(ElectricIndigo, ElectricViolet)),
                                CircleShape
                            )
                            .testTag("save_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Save to Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                BeforeAfterImageViewer(
                    bitmap = bitmap,
                    adjustments = adjustments,
                    isSplitMode = isSplitMode,
                    modifier = Modifier.fillMaxSize()
                )

                if (isAnalyzingAI) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = CyberCyan,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ChatGPT Auto-Enhancing...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                aiAnalysis?.let { analysis ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        AIInsightBanner(
                            analysis = analysis,
                            onReanalyzeClick = {
                                coroutineScope.launch {
                                    isAnalyzingAI = true
                                    val reResult = AIEnhanceService.autoEnhancePhoto(bitmap, null, openAiKey)
                                    aiAnalysis = reResult
                                    adjustments = reResult.recommendedAdjustments
                                    isAnalyzingAI = false
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instant Filter Presets",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAdjustmentsSheet = !showAdjustmentsSheet }
                            .padding(4.dp)
                            .testTag("fine_tune_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Fine Tune",
                            tint = if (showAdjustmentsSheet) ElectricIndigo else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAdjustmentsSheet) "Hide Sliders" else "Fine Tune",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (showAdjustmentsSheet) ElectricIndigo else TextSecondary
                        )
                    }
                }

                FilterPresetCarousel(
                    selectedPreset = adjustments.filterPreset,
                    onSelectPreset = { preset ->
                        adjustments = if (preset == FilterPreset.AI_AUTO && aiAnalysis != null) {
                            aiAnalysis!!.recommendedAdjustments
                        } else {
                            adjustments.copy(filterPreset = preset)
                        }
                    }
                )

                AnimatedVisibility(
                    visible = showAdjustmentsSheet,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manual Tone & Color",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        adjustments = ImageAdjustments(filterPreset = adjustments.filterPreset)
                                    }
                                    .padding(4.dp)
                                    .testTag("reset_adjustments_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            AdjustmentSliderRow(
                                label = "Exposure",
                                value = adjustments.exposure,
                                onValueChange = { adjustments = adjustments.copy(exposure = it) }
                            )
                            AdjustmentSliderRow(
                                label = "Contrast",
                                value = adjustments.contrast,
                                onValueChange = { adjustments = adjustments.copy(contrast = it) }
                            )
                            AdjustmentSliderRow(
                                label = "Saturation",
                                value = adjustments.saturation,
                                onValueChange = { adjustments = adjustments.copy(saturation = it) }
                            )
                            AdjustmentSliderRow(
                                label = "Warmth",
                                value = adjustments.warmth,
                                onValueChange = { adjustments = adjustments.copy(warmth = it) }
                            )
                            AdjustmentSliderRow(
                                label = "Tint",
                                value = adjustments.tint,
                                onValueChange = { adjustments = adjustments.copy(tint = it) }
                            )
                            AdjustmentSliderRow(
                                label = "Vignette",
                                value = adjustments.vignette,
                                valueRange = 0f..50f,
                                onValueChange = { adjustments = adjustments.copy(vignette = it) }
                            )
                        }
                    }
                }
            }
        }

        if (showPromptDialog) {
            AIPromptDialog(
                initialPrompt = aiAnalysis?.promptUsed ?: "",
                onDismiss = { showPromptDialog = false },
                onSubmitPrompt = { prompt ->
                    coroutineScope.launch {
                        isAnalyzingAI = true
                        val newResult = AIEnhanceService.autoEnhancePhoto(bitmap, prompt, openAiKey)
                        aiAnalysis = newResult
                        adjustments = newResult.recommendedAdjustments
                        isAnalyzingAI = false
                    }
                }
            )
        }
    }
}
