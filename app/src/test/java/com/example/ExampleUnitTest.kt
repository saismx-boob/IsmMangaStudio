package com.example

import com.example.ai.CameraPerspective
import com.example.ai.ColorRenderingMode
import com.example.ai.LineWeightStyle
import com.example.ai.MangaArtStyle
import com.example.ai.MangaStyleSettingsHelper
import com.example.ai.PromptConsistencyEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRequiredArtStylePresetsExist() {
    val shonen = MangaArtStyle.MANGA_SHONEN
    val seinen = MangaArtStyle.MANGA_SEINEN
    val shojo = MangaArtStyle.MANGA_SHOJO
    val classicBw = MangaArtStyle.CLASSIC_BLACK_AND_WHITE

    assertEquals("Shōnen", shonen.displayName)
    assertEquals("Seinen", seinen.displayName)
    assertEquals("Shōjo", shojo.displayName)
    assertEquals("Noir et Blanc classique", classicBw.displayName)

    assertTrue(shonen.recommendedStrokeWeight > 0f)
    assertTrue(seinen.recommendedStrokeWeight > 0f)
    assertTrue(shojo.recommendedStrokeWeight > 0f)
    assertTrue(classicBw.recommendedStrokeWeight > 0f)

    assertTrue(shonen.recommendedContrast in 0f..1f)
    assertTrue(seinen.recommendedContrast in 0f..1f)
    assertTrue(shojo.recommendedContrast in 0f..1f)
    assertEquals(1.0f, classicBw.recommendedContrast, 0.01f)
  }

  @Test
  fun testStrokeAndContrastPresets() {
    val strokePresets = MangaStyleSettingsHelper.strokePresets
    assertEquals(5, strokePresets.size)
    assertEquals(0.2f, strokePresets[0].value, 0.01f)
    assertEquals(0.7f, strokePresets[2].value, 0.01f)
    assertEquals(1.4f, strokePresets[4].value, 0.01f)

    val contrastPresets = MangaStyleSettingsHelper.contrastPresets
    assertEquals(4, contrastPresets.size)
    assertEquals(0.3f, contrastPresets[0].value, 0.01f)
    assertEquals(1.0f, contrastPresets[3].value, 0.01f)
  }

  @Test
  fun testInkingPromptFragmentGeneration() {
    val promptFragment = MangaStyleSettingsHelper.buildInkingPromptFragment(
      thickness = 0.8f,
      contrast = 0.75f
    )
    assertTrue(promptFragment.contains("0.8mm"))
    assertTrue(promptFragment.contains("75%"))
    assertTrue(promptFragment.contains("screentone") || promptFragment.contains("halftone"))
  }

  @Test
  fun testPromptConsistencyEngineWithPresets() {
    val prompt = PromptConsistencyEngine.buildConsistentPrompt(
      userAction = "Combat sur le toit du temple",
      artStyle = MangaArtStyle.MANGA_SHONEN,
      colorMode = ColorRenderingMode.BLACK_AND_WHITE,
      lineStyle = LineWeightStyle.DYNAMIC_G_PEN,
      camera = CameraPerspective.DYNAMIC_DUTCH_ANGLE,
      lineThickness = 0.8f,
      contrastLevel = 0.75f
    )
    assertTrue(prompt.contains("Shōnen"))
    assertTrue(prompt.contains("0.8mm"))
    assertTrue(prompt.contains("75%"))
    assertTrue(prompt.contains("Combat sur le toit du temple"))
  }
}

