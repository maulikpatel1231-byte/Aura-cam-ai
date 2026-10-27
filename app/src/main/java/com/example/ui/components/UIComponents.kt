package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AIAnalysisResult
import com.example.model.FilterPreset
import com.example.model.ImageAdjustments
import com.example.processing.ColorMatrixHelper
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BeforeAfterImageViewer(
    bitmap: Bitmap,
    adjustments: ImageAdjustments,
    modifier: Modifier = Modifier,
    isSplitMode: Boolean = false
) {
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    var isHoldingOriginal by remember { mutableStateOf(false) }

    val editedComposeMatrix = remember(adjustments) {
        ColorMatrixHelper.buildComposeColorMatrix(adjustments)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(isSplitMode) {
                if (isSplitMode) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        splitFraction = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    }
                } else {
                    detectTapGestures(
                        onPress = {
                            isHoldingOriginal = true
                            tryAwaitRelease()
                            isHoldingOriginal = false
                        }
                    )
                }
            }
            .testTag("before_after_viewer")
    ) {
        val totalWidth = maxWidth
        val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

        if (isSplitMode) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * splitFraction)
                    .clipToBounds()
            ) {
                ComposeImage(
                    bitmap = imageBitmap,
                    contentDescription = "Original Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(totalWidth)
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "ORIGINAL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * (1f - splitFraction))
                    .align(Alignment.TopEnd)
                    .clipToBounds()
            ) {
                ComposeImage(
                    bitmap = imageBitmap,
                    contentDescription = "Edited Photo",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.colorMatrix(editedComposeMatrix),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(totalWidth)
                        .align(Alignment.TopEnd)
                )
                Surface(
                    color = ElectricIndigo.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "AI EDITED",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .padding(start = totalWidth * splitFraction - 1.dp)
                    .background(Color.White)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = totalWidth * splitFraction - 18.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, ElectricIndigo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Compare,
                    contentDescription = "Slider Handle",
                    tint = DeepBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            ComposeImage(
                bitmap = imageBitmap,
                contentDescription = if (isHoldingOriginal) "Original Image" else "Edited Image",
                contentScale = ContentScale.Fit,
                colorFilter = if (isHoldingOriginal) null else ColorFilter.colorMatrix(editedComposeMatrix),
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = isHoldingOriginal,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    border = borderShape(Color.White.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Viewing Original Photo",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

private fun borderShape(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

@Composable
fun FilterPresetCarousel(
    selectedPreset: FilterPreset,
    onSelectPreset: (FilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("filter_carousel"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(FilterPreset.values()) { preset ->
            val isSelected = preset == selectedPreset
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectPreset(preset) }
                    .padding(4.dp)
                    .testTag("preset_${preset.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    preset.accentColor.copy(alpha = 0.8f),
                                    preset.accentColor.copy(alpha = 0.2f),
                                    DarkSurfaceElevated
                                )
                            )
                        )
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) ElectricIndigo else DarkSurfaceBorder,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (preset == FilterPreset.AI_AUTO) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(preset.accentColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = preset.displayName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AIInsightBanner(
    analysis: AIAnalysisResult,
    onReanalyzeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_insight_banner"),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceElevated.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = borderShape(ElectricViolet.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                Brush.linearGradient(listOf(ElectricIndigo, CyberCyan)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ChatGPT AI Color Grade",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = analysis.sceneType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberCyan
                        )
                    }
                }

                IconButton(
                    onClick = onReanalyzeClick,
                    modifier = Modifier.size(32.dp).testTag("reanalyze_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-analyze",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = analysis.commentary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AdjustmentSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = -50f..50f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("adjustment_slider_${label.lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = if (value > 0) "+${value.toInt()}" else "${value.toInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (value != 0f) ElectricIndigo else TextMuted
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = ElectricIndigo,
                activeTrackColor = ElectricIndigo,
                inactiveTrackColor = DarkSurfaceBorder
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun AIPromptDialog(
    initialPrompt: String = "",
    onDismiss: () -> Unit,
    onSubmitPrompt: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialPrompt) }
    val quickPresets = listOf(
        "90s Vintage Film",
        "Hollywood Cinema Teal",
        "Cyberpunk Tokyo Night",
        "Golden Hour Glow",
        "Moody Dark Noir B&W",
        "Clean Minimalist Portrait"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceElevated,
            border = borderShape(DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("ai_prompt_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Photo Style Prompt",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Describe how ChatGPT should edit your photo (e.g. vintage warm glow, cinematic mood):",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("E.g., Make it look like an indie film with soft shadows...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("ai_prompt_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Presets:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickPresets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSurface,
                            border = borderShape(ElectricIndigo.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { text = preset }
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) onSubmitPrompt(text)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("apply_prompt_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply AI Edit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
