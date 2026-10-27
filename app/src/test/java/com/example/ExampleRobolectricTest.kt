package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AuraCam AI", appName)
  }

  @Test
  fun `verify filter presets exist`() {
    val presets = com.example.model.FilterPreset.values()
    assertTrue(presets.isNotEmpty())
    assertTrue(presets.any { it == com.example.model.FilterPreset.AI_AUTO })
    assertTrue(presets.any { it == com.example.model.FilterPreset.CINEMA_TEAL_ORANGE })
  }
}
