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

    assertEquals("Manga Shōnen", shonen.displayName)
    assertEquals("Manga Seinen", seinen.displayName)
    assertEquals("Manga Shōjo", shojo.displayName)
    assertEquals("Noir & Blanc classique", classicBw.displayName)

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
    val strokePrompt = MangaStyleSettingsHelper.getStrokePrompt(0.8f)
    val contrastPrompt = MangaStyleSettingsHelper.getContrastPrompt(0.75f)

    assertTrue(strokePrompt.contains("0.7mm") || strokePrompt.contains("G-pen") || strokePrompt.contains("ink"))
    assertTrue(contrastPrompt.contains("manga contrast") || contrastPrompt.contains("shadows"))
  }

  @Test
  fun testPromptConsistencyEngineWithPresets() {
    val prompt = PromptConsistencyEngine.buildConsistentPrompt(
      userAction = "Combat sur le toit du temple",
      artStyle = MangaArtStyle.MANGA_SHONEN,
      colorMode = ColorRenderingMode.BLACK_AND_WHITE,
      lineStyle = LineWeightStyle.DYNAMIC_INK,
      camera = CameraPerspective.DUTCH_ANGLE,
      lineThickness = 0.8f,
      contrastLevel = 0.75f
    )
    assertTrue(prompt.contains("shonen manga style"))
    assertTrue(prompt.contains("Combat sur le toit du temple"))
    assertTrue(prompt.contains("Line thickness calibration"))
    assertTrue(prompt.contains("Tonal contrast calibration"))
  }

  @Test
  fun testRealTimePageCompositionLayoutPresets() {
    val layouts = com.example.data.model.GridPageLayout.values()
    assertTrue(layouts.any { it.id == "SINGLE_SPLASH" && it.panelCount == 1 })
    assertTrue(layouts.any { it.id == "TWO_PANELS_VERTICAL" && it.panelCount == 2 })
    assertTrue(layouts.any { it.id == "THREE_PANELS_COMBO" && it.panelCount == 3 })
    assertTrue(layouts.any { it.id == "FOUR_PANEL_YONKOMA" && it.panelCount == 4 })
  }

  @Test
  fun testActivePanelIndexCoercion() {
    val panels = listOf(
      com.example.data.model.MangaPanel(pageId = 1L, panelIndex = 0),
      com.example.data.model.MangaPanel(pageId = 1L, panelIndex = 1),
      com.example.data.model.MangaPanel(pageId = 1L, panelIndex = 2)
    )
    val invalidHighIndex = 5
    val safeIndex = invalidHighIndex.coerceIn(0, panels.size - 1)
    assertEquals(2, safeIndex)

    val invalidLowIndex = -1
    val safeLowIndex = invalidLowIndex.coerceIn(0, panels.size - 1)
    assertEquals(0, safeLowIndex)
  }

  @Test
  fun testCharacterProfileModelConsistency() {
    val profile = com.example.data.model.CharacterProfile(
      id = 1L,
      name = "Ren",
      role = "Héros Shonen",
      hairStyleColor = "Cheveux noirs hérissés",
      eyeDescription = "Yeux dorés",
      clothingDescription = "Kimono rouge et bandages",
      distinctiveFeatures = "Cicatrice joue gauche",
      visualUid = "UID-REN-SHONEN-99"
    )

    assertEquals("Ren", profile.name)
    assertEquals("Kimono rouge et bandages", profile.clothingDescription)
    assertEquals("Cicatrice joue gauche", profile.distinctiveFeatures)
    assertEquals("UID-REN-SHONEN-99", profile.visualUid)
  }
}

