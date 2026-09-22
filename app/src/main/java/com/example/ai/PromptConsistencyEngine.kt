package com.example.ai

import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile

/**
 * Builds deterministic, coherent prompts for AI manga/comic image generation
 * ensuring visual consistency for characters, backgrounds, and art styles.
 */
object PromptConsistencyEngine {

    /**
     * Constructs a comprehensive prompt linking character DNA, environment anchors,
     * perspective, and art style parameters.
     */
    fun buildConsistentPrompt(
        userAction: String,
        artStyle: MangaArtStyle,
        colorMode: ColorRenderingMode,
        lineStyle: LineWeightStyle,
        camera: CameraPerspective = CameraPerspective.MEDIUM_SHOT,
        character: CharacterProfile? = null,
        background: BackgroundProfile? = null,
        hasReferenceImage: Boolean = false,
        lineThickness: Float = 0.7f,
        contrastLevel: Float = 0.7f
    ): String {
        val promptBuilder = StringBuilder()

        // 1. Primary Scene & Perspective
        promptBuilder.append("Manga panel illustration: ")
        promptBuilder.append(userAction.trim().ifEmpty { "Dynamic character action scene" })
        promptBuilder.append(", ${camera.promptModifier}. ")

        // 2. Character Visual Consistency Anchor
        if (character != null) {
            promptBuilder.append("\n[STRICT CHARACTER CONSISTENCY DNA]: ")
            promptBuilder.append("Character Visual Identity Token: #${character.visualUid}. ")
            promptBuilder.append("Visual Consistency Seed: ${character.canonicalSeed}. ")
            promptBuilder.append("Character Name: ${character.name}. ")
            promptBuilder.append("Role: ${character.role}. ")
            promptBuilder.append("Hair: ${character.hairStyleColor}. ")
            promptBuilder.append("Eyes: ${character.eyeDescription}. ")
            promptBuilder.append("Attire: ${character.clothingDescription}. ")
            if (character.ageCategory.isNotBlank()) {
                promptBuilder.append("Age/Maturity: ${character.ageCategory}. ")
            }
            if (character.personalityMood.isNotBlank()) {
                promptBuilder.append("Demeanor/Personality: ${character.personalityMood}. ")
            }
            if (character.defaultExpression.isNotBlank()) {
                promptBuilder.append("Default Expression: ${character.defaultExpression}. ")
            }
            if (character.distinctiveFeatures.isNotBlank()) {
                promptBuilder.append("Key Features: ${character.distinctiveFeatures}. ")
            }
            if (character.promptAnchor.isNotBlank()) {
                promptBuilder.append("Visual Reference Anchor: ${character.promptAnchor}. ")
            }
            if (character.firstAppearanceImagePath != null) {
                promptBuilder.append("MANDATORY FIRST-APPEARANCE REPRODUCTION: Character UID #${character.visualUid} has an established canonical First Appearance reference. You must strictly preserve the identical facial structure, eye geometry, hair strands, and outfit proportions established in that first appearance anchor. ")
            }
            val totalRefImages = character.getReferenceImagesList().size + (if (hasReferenceImage) 1 else 0)
            if (totalRefImages > 0) {
                promptBuilder.append("CRITICAL ($totalRefImages visual reference image(s) attached): Strictly maintain exact character likeness, face topology, hairstyle, silhouette, and costume details from the provided character reference images. ")
            }
        }

        // 3. Background Visual Consistency Anchor
        if (background != null) {
            promptBuilder.append("\n[STRICT BACKGROUND CONSISTENCY DNA]: ")
            promptBuilder.append("Location: ${background.name}. ")
            promptBuilder.append("Category: ${background.category}. ")
            promptBuilder.append("Atmosphere/Lighting: ${background.lightingMood}. ")
            promptBuilder.append("Environment Details: ${background.architectureDetails}. ")
            if (background.promptAnchor.isNotBlank()) {
                promptBuilder.append("Background Anchor: ${background.promptAnchor}. ")
            }
        }

        // 4. User-Adjustable Art Style, Line Weight & Contrast
        promptBuilder.append("\n[ARTISTIC STYLE & INK SPECIFICATIONS]: ")
        promptBuilder.append(artStyle.promptKeywords).append(". ")
        promptBuilder.append(colorMode.promptModifier).append(". ")
        promptBuilder.append(lineStyle.promptModifier).append(". ")
        promptBuilder.append("Line thickness calibration: ").append(MangaStyleSettingsHelper.getStrokePrompt(lineThickness)).append(". ")
        promptBuilder.append("Tonal contrast calibration: ").append(MangaStyleSettingsHelper.getContrastPrompt(contrastLevel)).append(". ")

        // 5. Quality & Formatting Mandates
        promptBuilder.append(
            "Masterpiece quality, cohesive graphic novel panel, clean borders, highly detailed, crisp focus. " +
            "Avoid: photorealism, western 3D render, warped anatomy, inconsistent face, blurry lines."
        )

        return promptBuilder.toString()
    }

    /**
     * Formats a short consistency summary badge for the UI.
     */
    fun formatConsistencySummary(
        character: CharacterProfile?,
        background: BackgroundProfile?,
        artStyle: MangaArtStyle
    ): String {
        val parts = mutableListOf<String>()
        if (character != null) {
            val anchorStatus = if (character.firstAppearanceImagePath != null) " ⭐ Ancré" else ""
            parts.add("Héros: ${character.name} [${character.visualUid}]$anchorStatus")
        }
        if (background != null) parts.add("Décor: ${background.name}")
        parts.add(artStyle.displayName)
        return parts.joinToString(" • ")
    }
}
