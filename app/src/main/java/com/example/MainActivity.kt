package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.model.FilterPreset
import com.example.ui.CameraScreen
import com.example.ui.EditorScreen
import com.example.ui.SettingsDialog
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    CAMERA,
    EDITOR
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AuraCamApp()
            }
        }
    }
}

@Composable
fun AuraCamApp() {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("auracam_prefs", Context.MODE_PRIVATE)
    }

    var currentScreen by remember { mutableStateOf(AppScreen.CAMERA) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var initialPreset by remember { mutableStateOf(FilterPreset.AI_AUTO) }
    var showSettings by remember { mutableStateOf(false) }

    var openAiKey by remember {
        mutableStateOf(sharedPrefs.getString("openai_key", "") ?: "")
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        when (currentScreen) {
            AppScreen.CAMERA -> {
                CameraScreen(
                    hasCameraPermission = hasCameraPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onPhotoCaptured = { bitmap, preset ->
                        capturedBitmap = bitmap
                        initialPreset = preset
                        currentScreen = AppScreen.EDITOR
                    },
                    onOpenSettings = { showSettings = true }
                )
            }

            AppScreen.EDITOR -> {
                capturedBitmap?.let { bitmap ->
                    EditorScreen(
                        bitmap = bitmap,
                        initialPreset = initialPreset,
                        openAiKey = openAiKey,
                        onBackToCamera = {
                            currentScreen = AppScreen.CAMERA
                        }
                    )
                } ?: run {
                    currentScreen = AppScreen.CAMERA
                }
            }
        }

        if (showSettings) {
            SettingsDialog(
                currentOpenAiKey = openAiKey,
                onSaveOpenAiKey = { newKey ->
                    openAiKey = newKey
                    sharedPrefs.edit().putString("openai_key", newKey).apply()
                },
                onDismiss = { showSettings = false }
            )
        }
    }
}
