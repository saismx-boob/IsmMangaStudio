package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.CameraPerspective
import com.example.ai.ColorRenderingMode
import com.example.ai.LineWeightStyle
import com.example.ai.MangaArtStyle
import com.example.ai.PromptConsistencyEngine
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.ui.screens.GridPageLayout
import com.example.ui.screens.POSE_PRESETS
import com.example.ui.components.BubbleShapeType
import com.example.ui.components.TailDirection
import com.example.data.database.AppDatabase
import com.example.data.repository.MangaRepository
import com.example.ui.theme.BubbleFontOption
import com.example.util.MangaPanelExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        assertEquals("Manga Studio AI", appName)
    }

    @Test
    fun `prompt consistency engine generates coherent character and background anchors`() {
        val hero = CharacterProfile(
            name = "Ren",
            role = "Guerrier Shonen",
            hairStyleColor = "Cheveux noirs hérissés",
            eyeDescription = "Yeux ambrés",
            clothingDescription = "Haori rouge et noir"
        )
        val bg = BackgroundProfile(
            name = "Dojo Antique",
            category = "Intérieur",
            lightingMood = "Lumière dorée du soir",
            architectureDetails = "Tatami en paille et portes coulissantes shoji"
        )

        val prompt = PromptConsistencyEngine.buildConsistentPrompt(
            userAction = "Ren se prépare au combat",
            artStyle = MangaArtStyle.MANGA_SHONEN,
            colorMode = ColorRenderingMode.BLACK_AND_WHITE,
            lineStyle = LineWeightStyle.DYNAMIC_INK,
            camera = CameraPerspective.MEDIUM_SHOT,
            character = hero,
            background = bg
        )

        assertTrue(prompt.contains("Ren"))
        assertTrue(prompt.contains("Dojo Antique"))
        assertTrue(prompt.contains("authentic Japanese shonen manga style"))
        assertTrue(prompt.contains("black and white monochrome"))
    }

    @Test
    fun `vector speech bubble shapes and fonts resolve properly`() {
        assertEquals(BubbleShapeType.SHOUT, BubbleShapeType.fromId("SHOUT"))
        assertEquals(BubbleShapeType.THOUGHT, BubbleShapeType.fromId("thought"))
        assertEquals(BubbleShapeType.SPEECH, BubbleShapeType.fromId("UNKNOWN"))

        assertEquals(BubbleFontOption.BANGERS, BubbleFontOption.fromId("BANGERS"))
        assertEquals(BubbleFontOption.COMIC_NEUE, BubbleFontOption.fromId("comic_neue"))
        assertEquals(BubbleFontOption.COMIC_NEUE, BubbleFontOption.fromId("invalid"))

        assertEquals(TailDirection.BOTTOM_LEFT, TailDirection.fromId("BOTTOM_LEFT"))
        assertEquals(TailDirection.NONE, TailDirection.fromId("NONE"))

        assertNotNull(BubbleFontOption.BANGERS.fontFamily)
        assertNotNull(BubbleFontOption.COMIC_NEUE.fontFamily)
        assertNotNull(BubbleFontOption.PERMANENT_MARKER.fontFamily)
        assertNotNull(BubbleFontOption.CAVEAT.fontFamily)
    }

    @Test
    fun `high-res manga panel bitmap renders with vector speech bubble and exports`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val panel = MangaPanel(
            id = 101L,
            pageId = 1L,
            panelIndex = 0,
            userPrompt = "Ren active son attaque spéciale",
            dialogueText = "RASENGAN IMPÉRIAL!",
            bubbleType = "SHOUT",
            bubbleFont = "BANGERS",
            bubbleFontSize = 18,
            bubbleTailDirection = "BOTTOM_LEFT",
            bubbleBgColor = 0xFFFFFFFF,
            bubbleTextColor = 0xFF000000,
            bubbleBorderColor = 0xFF000000,
            bubbleNormalizedX = 0.5f,
            bubbleNormalizedY = 0.4f
        )

        val bitmap = MangaPanelExporter.renderHighResBitmap(
            context = context,
            panel = panel,
            panelNumber = 1,
            targetWidth = 600,
            targetHeight = 520
        )

        assertNotNull(bitmap)
        assertEquals(600, bitmap.width)
        assertEquals(520, bitmap.height)

        val shareResult = MangaPanelExporter.createShareIntent(
            context = context,
            bitmap = bitmap,
            panelNumber = 1
        )

        assertTrue(shareResult.isSuccess)
        assertNotNull(shareResult.getOrNull())
    }

    @Test
    fun `character profile room database crud and consistency anchor operations`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repository = MangaRepository(db)

        val character = CharacterProfile(
            name = "Kageyama Akira",
            role = "Rival Ténébreux",
            hairStyleColor = "Cheveux argentés mi-longs",
            eyeDescription = "Yeux violacés glaciaux",
            clothingDescription = "Manteau long noir à col montant avec boucle argentée",
            distinctiveFeatures = "Bague runique à la main droite",
            promptAnchor = "Anime male character Kageyama Akira, silver medium hair, purple eyes, black long coat"
        )

        // Save
        val insertedId = repository.saveCharacter(character)
        assertTrue(insertedId > 0)

        // Read by ID
        val retrieved = repository.getCharacterById(insertedId)
        assertNotNull(retrieved)
        assertEquals("Kageyama Akira", retrieved?.name)
        assertEquals("Cheveux argentés mi-longs", retrieved?.hairStyleColor)
        assertEquals("Yeux violacés glaciaux", retrieved?.eyeDescription)
        assertEquals("Manteau long noir à col montant avec boucle argentée", retrieved?.clothingDescription)
        assertEquals("Bague runique à la main droite", retrieved?.distinctiveFeatures)

        // Update First Appearance Anchor
        repository.updateCharacterFirstAppearance(insertedId, "/data/user/0/com.example/files/panel_1.jpg", 1L)
        val withAnchor = repository.getCharacterById(insertedId)
        assertEquals("/data/user/0/com.example/files/panel_1.jpg", withAnchor?.firstAppearanceImagePath)
        assertEquals(1L, withAnchor?.firstAppearancePanelId)
        assertEquals(1, withAnchor?.appearanceCount)

        // Clear First Appearance Anchor
        repository.clearCharacterFirstAppearance(insertedId)
        val clearedAnchor = repository.getCharacterById(insertedId)
        assertNull(clearedAnchor?.firstAppearanceImagePath)
        assertNull(clearedAnchor?.firstAppearancePanelId)

        // Update Character Profile
        val updated = retrieved!!.copy(
            hairStyleColor = "Cheveux argentés courts en épis",
            clothingDescription = "Armure de combat tactique"
        )
        repository.saveCharacter(updated)
        val afterUpdate = repository.getCharacterById(insertedId)
        assertEquals("Cheveux argentés courts en épis", afterUpdate?.hairStyleColor)
        assertEquals("Armure de combat tactique", afterUpdate?.clothingDescription)

        // Delete Character
        repository.deleteCharacter(afterUpdate!!)
        val afterDelete = repository.getCharacterById(insertedId)
        assertNull(afterDelete)
    }

    @Test
    fun `storyboard grid presets and drag-and-drop pose assignment logic`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repository = MangaRepository(db)

        // Validate preset poses
        assertTrue(POSE_PRESETS.isNotEmpty())
        val combatPose = POSE_PRESETS.first { it.id == "pose_strike" }
        assertEquals("Combat", combatPose.category)
        assertTrue(combatPose.actionPrompt.contains("frappe de paume"))

        // Create a test page and panel
        val pageId = repository.createPage(
            MangaPage(projectId = 1L, pageNumber = 1, layoutType = GridPageLayout.THREE_MANGA.id)
        )
        assertTrue(pageId > 0)

        val panelId = repository.savePanel(
            MangaPanel(pageId = pageId, panelIndex = 0, userPrompt = "Vue d'ensemble")
        )

        // Assign character and pose as simulated drag-and-drop
        val hero = CharacterProfile(
            name = "Mei Ling",
            role = "Maître Qi",
            visualUid = "#CHR-MEI-8800",
            hairStyleColor = "Noirs nattés",
            eyeDescription = "Dorés",
            clothingDescription = "Robe traditionnelle blanche et jade"
        )
        val heroId = repository.saveCharacter(hero)

        val retrievedPanel = repository.getPanelById(panelId)
        assertNotNull(retrievedPanel)

        // Apply drag-and-drop payload: Hero + Pose
        val dropActionPrompt = "${hero.name} ${combatPose.actionPrompt}"
        val updatedPanel = retrievedPanel!!.copy(
            characterId = heroId,
            userPrompt = dropActionPrompt
        )
        repository.savePanel(updatedPanel)

        val finalPanel = repository.getPanelById(panelId)
        assertEquals(heroId, finalPanel?.characterId)
        assertTrue(finalPanel?.userPrompt?.contains("Mei Ling") == true)
        assertTrue(finalPanel?.userPrompt?.contains("frappe de paume") == true)
    }
}
