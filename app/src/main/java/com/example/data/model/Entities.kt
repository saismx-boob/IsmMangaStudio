package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Character profile ensuring visual consistency across manga/comic panels.
 * Features a unique Visual UID (DNA token) and first appearance anchor snapshot.
 */
@Entity(tableName = "character_profiles")
data class CharacterProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String = "Protagoniste",
    val hairStyleColor: String = "Cheveux noirs hérissés",
    val eyeDescription: String = "Yeux dorés perçants",
    val clothingDescription: String = "Uniforme sombre avec manteau long",
    val distinctiveFeatures: String = "Cicatrice fine sur la joue gauche",
    val signatureColor: String = "#FF3366",
    val referenceImagePath: String? = null,
    val promptAnchor: String = "",
    val visualUid: String = generateCharacterUid(name),
    val firstAppearanceImagePath: String? = null,
    val firstAppearancePanelId: Long? = null,
    val appearanceCount: Int = 0,
    val canonicalSeed: Long = generateCharacterSeed(name),
    val createdAt: Long = System.currentTimeMillis()
)

fun generateCharacterUid(name: String): String {
    val cleanName = name.filter { it.isLetter() }.take(4).uppercase().ifEmpty { "HERO" }
    val randomHex = java.util.UUID.randomUUID().toString().take(6).uppercase()
    return "CHR-$cleanName-$randomHex"
}

fun generateCharacterSeed(name: String): Long {
    return kotlin.math.abs(name.hashCode().toLong() * 37L + 777L) % 1_000_000_000L
}

/**
 * Background / Setting profile ensuring environment consistency across panels.
 */
@Entity(tableName = "background_profiles")
data class BackgroundProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Urbain / Ville",
    val lightingMood: String = "Crépuscule doré et néons",
    val architectureDetails: String = "Bâtiments néo-Tokyo avec câbles et enseignes lumineuses",
    val referenceImagePath: String? = null,
    val promptAnchor: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Manga / Manhua / Comic Project containing multiple pages.
 */
@Entity(tableName = "manga_projects")
data class MangaProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val synopsis: String = "",
    val artStyle: String = "MANGA_SHONEN", // MANGA_SHONEN, MANGA_SEINEN, MANHUA_XIANXIA, MANHWA_WEBTOON, WESTERN_COMIC
    val colorMode: String = "BLACK_AND_WHITE", // BLACK_AND_WHITE, DIGITAL_COLOR, WATERCOLOR
    val lineStyle: String = "DYNAMIC_INK", // DYNAMIC_INK, FINE_PEN, BRUSH_WASH
    val coverImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Layout presets for the manga blank page grid.
 */
enum class GridPageLayout(
    val id: String,
    val title: String,
    val panelCount: Int,
    val description: String
) {
    TWO_VERTICAL("TWO_PANELS_VERTICAL", "2 Cases (Vertical)", 2, "Découpage classique équilibré haut/bas"),
    THREE_MANGA("THREE_PANELS_COMBO", "3 Cases (Manhua)", 3, "1 grande case d'impact + 2 cases secondaires"),
    FOUR_YONKOMA("FOUR_PANEL_YONKOMA", "4-Koma (Yon-koma)", 4, "Quatre cases verticales pour rythme cadencé"),
    SPLASH_FULL("SINGLE_SPLASH", "Page Pleine (Splash)", 1, "Grande case unique pleine page pour révélation majeure")
}

/**
 * A Page within a Manga Project with panel grid configuration.
 */
@Entity(tableName = "manga_pages")
data class MangaPage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val pageNumber: Int = 1,
    val layoutType: String = "TWO_PANELS_VERTICAL", // SINGLE_SPLASH, TWO_PANELS_VERTICAL, THREE_PANELS, FOUR_PANEL_YONKOMA, WEBTOON_FLOW
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * An individual Panel within a Manga Page.
 */
@Entity(tableName = "manga_panels")
data class MangaPanel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pageId: Long,
    val panelIndex: Int = 0,
    val userPrompt: String = "",
    val enrichedPrompt: String = "",
    val characterId: Long? = null,
    val backgroundId: Long? = null,
    val imagePath: String? = null,
    val dialogueText: String? = null,
    val dialogueSpeaker: String? = null,
    val bubbleType: String = "SPEECH", // SPEECH, SHOUT, THOUGHT, WHISPER, NARRATION, ONOMATOPOEIA
    val bubbleFont: String = "COMIC_NEUE", // COMIC_NEUE, BANGERS, PERMANENT_MARKER, CAVEAT, DEFAULT
    val bubbleFontSize: Int = 14,
    val bubbleTailDirection: String = "BOTTOM_LEFT", // BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT, NONE
    val bubbleBgColor: Long = 0xFFFFFFFF,
    val bubbleTextColor: Long = 0xFF000000,
    val bubbleBorderColor: Long = 0xFF000000,
    val bubbleNormalizedX: Float = 0.5f,
    val bubbleNormalizedY: Float = 0.8f,
    val createdAt: Long = System.currentTimeMillis()
)
