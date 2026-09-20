package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.CameraPerspective
import com.example.ai.ColorRenderingMode
import com.example.ai.GeminiMangaService
import com.example.ai.LineWeightStyle
import com.example.ai.MangaArtStyle
import com.example.ai.MangaGenerationResult
import com.example.ai.PromptConsistencyEngine
import com.example.data.database.AppDatabase
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.GridPageLayout
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.data.model.MangaProject
import com.example.data.repository.MangaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StudioEditorState(
    val activePrompt: String = "Ren bondit au-dessus des toits avec son katana scintillant, le vent tourbillonne.",
    val selectedArtStyle: MangaArtStyle = MangaArtStyle.MANGA_SHONEN,
    val lineThickness: Float = 0.8f,
    val contrastLevel: Float = 0.75f,
    val selectedColorMode: ColorRenderingMode = ColorRenderingMode.BLACK_AND_WHITE,
    val selectedLineStyle: LineWeightStyle = LineWeightStyle.DYNAMIC_INK,
    val selectedCamera: CameraPerspective = CameraPerspective.MEDIUM_SHOT,
    val selectedCharacterId: Long? = 1L,
    val selectedBackgroundId: Long? = 1L,
    val activePanelIndex: Int = 0,
    val referenceImagePath: String? = null,
    val dialogueText: String = "Je ne reculerai jamais !",
    val bubbleType: String = "SHOUT",
    val bubbleFont: String = "BANGERS",
    val bubbleFontSize: Int = 16,
    val bubbleTailDirection: String = "BOTTOM_LEFT",
    val bubbleBgColor: Long = 0xFFFFFFFF,
    val bubbleTextColor: Long = 0xFF000000,
    val bubbleBorderColor: Long = 0xFF000000,
    val isGenerating: Boolean = false,
    val generationStage: String = "",
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class MangaStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MangaRepository(AppDatabase.getInstance(application))
    private val geminiService = GeminiMangaService(application)

    // Flow states
    val allProjects: StateFlow<List<MangaProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCharacters: StateFlow<List<CharacterProfile>> = repository.allCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBackgrounds: StateFlow<List<BackgroundProfile>> = repository.allBackgrounds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProject = MutableStateFlow<MangaProject?>(null)
    val selectedProject: StateFlow<MangaProject?> = _selectedProject.asStateFlow()

    private val _currentPages = MutableStateFlow<List<MangaPage>>(emptyList())
    val currentPages: StateFlow<List<MangaPage>> = _currentPages.asStateFlow()

    private val _selectedPage = MutableStateFlow<MangaPage?>(null)
    val selectedPage: StateFlow<MangaPage?> = _selectedPage.asStateFlow()

    private val _currentPanels = MutableStateFlow<List<MangaPanel>>(emptyList())
    val currentPanels: StateFlow<List<MangaPanel>> = _currentPanels.asStateFlow()

    private val _editorState = MutableStateFlow(StudioEditorState())
    val editorState: StateFlow<StudioEditorState> = _editorState.asStateFlow()

    init {
        // Automatically select the first project and page when available
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (_selectedProject.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first())
                }
            }
        }
    }

    fun selectProject(project: MangaProject) {
        _selectedProject.value = project
        _editorState.value = _editorState.value.copy(
            selectedArtStyle = MangaArtStyle.fromId(project.artStyle),
            selectedColorMode = ColorRenderingMode.fromId(project.colorMode),
            selectedLineStyle = LineWeightStyle.fromId(project.lineStyle)
        )
        viewModelScope.launch {
            repository.getPagesForProject(project.id).collect { pages ->
                _currentPages.value = pages
                if (pages.isNotEmpty()) {
                    selectPage(pages.first())
                } else {
                    // Create default first page
                    val newPageId = repository.createPage(MangaPage(projectId = project.id, pageNumber = 1))
                    repository.savePanel(MangaPanel(pageId = newPageId, panelIndex = 0, userPrompt = "Vue d'ensemble"))
                }
            }
        }
    }

    fun selectPage(page: MangaPage) {
        _selectedPage.value = page
        viewModelScope.launch {
            repository.getPanelsForPage(page.id).collect { panels ->
                _currentPanels.value = panels
                if (panels.isNotEmpty()) {
                    val currentIdx = _editorState.value.activePanelIndex.coerceIn(0, panels.size - 1)
                    val activePanel = panels.getOrNull(currentIdx) ?: panels.first()
                    _editorState.value = _editorState.value.copy(
                        activePanelIndex = currentIdx,
                        activePrompt = activePanel.userPrompt.ifBlank { _editorState.value.activePrompt },
                        selectedCharacterId = activePanel.characterId ?: _editorState.value.selectedCharacterId,
                        selectedBackgroundId = activePanel.backgroundId ?: _editorState.value.selectedBackgroundId,
                        dialogueText = activePanel.dialogueText ?: "",
                        bubbleType = activePanel.bubbleType
                    )
                }
            }
        }
    }

    fun selectPanelForEditing(panelIndex: Int) {
        val panels = _currentPanels.value
        val safeIndex = panelIndex.coerceIn(0, (panels.size - 1).coerceAtLeast(0))
        val targetPanel = panels.getOrNull(safeIndex)
        _editorState.value = _editorState.value.copy(
            activePanelIndex = safeIndex,
            activePrompt = targetPanel?.userPrompt?.ifBlank { _editorState.value.activePrompt } ?: _editorState.value.activePrompt,
            selectedCharacterId = targetPanel?.characterId ?: _editorState.value.selectedCharacterId,
            selectedBackgroundId = targetPanel?.backgroundId ?: _editorState.value.selectedBackgroundId,
            dialogueText = targetPanel?.dialogueText ?: "",
            bubbleType = targetPanel?.bubbleType ?: _editorState.value.bubbleType,
            statusMessage = "Édition active : Case #${safeIndex + 1}"
        )
    }

    // Input handlers
    fun updatePrompt(prompt: String) {
        _editorState.value = _editorState.value.copy(activePrompt = prompt)
    }

    fun updateArtStyle(style: MangaArtStyle) {
        _editorState.value = _editorState.value.copy(selectedArtStyle = style)
    }

    fun updateLineThickness(thickness: Float) {
        _editorState.value = _editorState.value.copy(lineThickness = thickness.coerceIn(0.1f, 1.5f))
    }

    fun updateContrastLevel(contrast: Float) {
        _editorState.value = _editorState.value.copy(contrastLevel = contrast.coerceIn(0.0f, 1.0f))
    }

    fun selectPresetArtStyle(style: MangaArtStyle, applyRecommendedSettings: Boolean = true) {
        val newThickness = if (applyRecommendedSettings) style.recommendedStrokeWeight else _editorState.value.lineThickness
        val newContrast = if (applyRecommendedSettings) style.recommendedContrast else _editorState.value.contrastLevel
        _editorState.value = _editorState.value.copy(
            selectedArtStyle = style,
            lineThickness = newThickness,
            contrastLevel = newContrast,
            statusMessage = "Style ${style.displayName} sélectionné"
        )
    }

    fun updateColorMode(mode: ColorRenderingMode) {
        _editorState.value = _editorState.value.copy(selectedColorMode = mode)
    }

    fun updateLineStyle(style: LineWeightStyle) {
        _editorState.value = _editorState.value.copy(selectedLineStyle = style)
    }

    fun updateCamera(camera: CameraPerspective) {
        _editorState.value = _editorState.value.copy(selectedCamera = camera)
    }

    fun selectCharacter(charId: Long?) {
        _editorState.value = _editorState.value.copy(selectedCharacterId = charId)
    }

    fun selectBackground(bgId: Long?) {
        _editorState.value = _editorState.value.copy(selectedBackgroundId = bgId)
    }

    fun setReferenceImagePath(path: String?) {
        _editorState.value = _editorState.value.copy(referenceImagePath = path)
    }

    fun updateDialogue(
        text: String,
        bubbleType: String,
        font: String = _editorState.value.bubbleFont,
        fontSize: Int = _editorState.value.bubbleFontSize,
        tailDirection: String = _editorState.value.bubbleTailDirection,
        bgColor: Long = _editorState.value.bubbleBgColor,
        textColor: Long = _editorState.value.bubbleTextColor,
        borderColor: Long = _editorState.value.bubbleBorderColor
    ) {
        _editorState.value = _editorState.value.copy(
            dialogueText = text,
            bubbleType = bubbleType,
            bubbleFont = font,
            bubbleFontSize = fontSize,
            bubbleTailDirection = tailDirection,
            bubbleBgColor = bgColor,
            bubbleTextColor = textColor,
            bubbleBorderColor = borderColor
        )
    }

    fun updatePanelSpeechBubble(
        panel: MangaPanel,
        text: String,
        type: String,
        font: String,
        fontSize: Int,
        tailDirection: String,
        bgColor: Long,
        textColor: Long,
        borderColor: Long
    ) {
        viewModelScope.launch {
            val updated = panel.copy(
                dialogueText = text.ifBlank { null },
                bubbleType = type,
                bubbleFont = font,
                bubbleFontSize = fontSize,
                bubbleTailDirection = tailDirection,
                bubbleBgColor = bgColor,
                bubbleTextColor = textColor,
                bubbleBorderColor = borderColor
            )
            repository.savePanel(updated)
            _editorState.value = _editorState.value.copy(
                statusMessage = "Bulle vectorielle mise à jour sur la case !"
            )
        }
    }

    fun updateBubblePosition(panel: MangaPanel, normX: Float, normY: Float) {
        viewModelScope.launch {
            val updated = panel.copy(
                bubbleNormalizedX = normX.coerceIn(0.08f, 0.92f),
                bubbleNormalizedY = normY.coerceIn(0.08f, 0.92f)
            )
            repository.savePanel(updated)
        }
    }

    /**
     * Enhances prompt using Gemini 3.5 Flash Storyboard assistant.
     */
    fun enhancePromptWithAI() {
        val currentPrompt = _editorState.value.activePrompt
        if (currentPrompt.isBlank()) return

        viewModelScope.launch {
            _editorState.value = _editorState.value.copy(
                isGenerating = true,
                generationStage = "Amélioration du scénario par l'IA..."
            )
            val enhanced = geminiService.storyboardAssistant(currentPrompt, _editorState.value.selectedArtStyle)
            _editorState.value = _editorState.value.copy(
                activePrompt = enhanced,
                isGenerating = false,
                generationStage = "",
                statusMessage = "Prompt enrichi avec cadrage & onomatopée !"
            )
        }
    }

    /**
     * Generates or regenerates an individual panel with strict character & background coherence.
     */
    fun generatePanel(panelIndex: Int = 0) {
        val page = _selectedPage.value ?: return
        val state = _editorState.value

        viewModelScope.launch {
            _editorState.value = state.copy(
                isGenerating = true,
                generationStage = "Application de l'ADN du personnage & décor...",
                errorMessage = null,
                statusMessage = null
            )

            val character = state.selectedCharacterId?.let { repository.getCharacterById(it) }
            val background = state.selectedBackgroundId?.let { repository.getBackgroundById(it) }

            // Reference images list for multimodal coherence - prioritize first appearance anchor
            val refImages = mutableListOf<String>()
            character?.firstAppearanceImagePath?.let {
                if (java.io.File(it).exists()) refImages.add(it)
            }
            character?.referenceImagePath?.let {
                if (java.io.File(it).exists() && it != character.firstAppearanceImagePath) refImages.add(it)
            }
            state.referenceImagePath?.let {
                if (java.io.File(it).exists() && !refImages.contains(it)) refImages.add(it)
            }
            background?.referenceImagePath?.let {
                if (java.io.File(it).exists() && !refImages.contains(it)) refImages.add(it)
            }

            // Construct prompt with PromptConsistencyEngine
            val coherentPrompt = PromptConsistencyEngine.buildConsistentPrompt(
                userAction = state.activePrompt,
                artStyle = state.selectedArtStyle,
                colorMode = state.selectedColorMode,
                lineStyle = state.selectedLineStyle,
                camera = state.selectedCamera,
                character = character,
                background = background,
                hasReferenceImage = refImages.isNotEmpty(),
                lineThickness = state.lineThickness,
                contrastLevel = state.contrastLevel
            )

            val consistencyStageDesc = if (character != null) {
                if (character.firstAppearanceImagePath != null) {
                    "Application de l'Ancre #${character.visualUid} (1ère apparition)..."
                } else {
                    "Établissement de la 1ère apparition (#${character.visualUid})..."
                }
            } else {
                "Génération IA (${state.selectedArtStyle.displayName})..."
            }

            _editorState.value = _editorState.value.copy(
                generationStage = consistencyStageDesc
            )

            val aspect = state.selectedArtStyle.defaultAspect
            val result = geminiService.generateMangaPanel(
                prompt = coherentPrompt,
                aspectRatio = if (aspect == "9:16") "9:16" else if (aspect == "3:4") "3:4" else "1:1",
                referenceImagePaths = refImages
            )

            when (result) {
                is MangaGenerationResult.Success -> {
                    // Update or insert panel in DB
                    val currentPanelList = _currentPanels.value
                    val existingPanel = currentPanelList.getOrNull(panelIndex)

                    val updatedPanel = (existingPanel ?: MangaPanel(pageId = page.id, panelIndex = panelIndex)).copy(
                        userPrompt = state.activePrompt,
                        enrichedPrompt = coherentPrompt,
                        characterId = state.selectedCharacterId,
                        backgroundId = state.selectedBackgroundId,
                        imagePath = result.imagePath,
                        dialogueText = state.dialogueText.ifBlank { null },
                        bubbleType = state.bubbleType,
                        bubbleFont = state.bubbleFont,
                        bubbleFontSize = state.bubbleFontSize,
                        bubbleTailDirection = state.bubbleTailDirection,
                        bubbleBgColor = state.bubbleBgColor,
                        bubbleTextColor = state.bubbleTextColor,
                        bubbleBorderColor = state.bubbleBorderColor
                    )

                    val savedPanelId = repository.savePanel(updatedPanel)

                    var successMsg = if (result.isAiGenerated) "Case générée avec succès !" else "Aperçu artistique généré"

                    // Character visual consistency auto-anchoring on first appearance
                    if (character != null) {
                        if (character.firstAppearanceImagePath.isNullOrBlank() && result.imagePath.isNotBlank()) {
                            repository.updateCharacterFirstAppearance(
                                charId = character.id,
                                imagePath = result.imagePath,
                                panelId = savedPanelId
                            )
                            successMsg = "Première apparition de ${character.name} enregistrée ! Ancre UID #${character.visualUid} activée."
                        } else {
                            repository.incrementCharacterAppearance(character.id)
                            successMsg = "Case générée avec cohérence #${character.visualUid} (${character.appearanceCount + 1}e apparition)."
                        }
                    }

                    _editorState.value = _editorState.value.copy(
                        isGenerating = false,
                        generationStage = "",
                        statusMessage = successMsg
                    )
                }
                is MangaGenerationResult.Error -> {
                    if (result.fallbackImagePath != null) {
                        val currentPanelList = _currentPanels.value
                        val existingPanel = currentPanelList.getOrNull(panelIndex)
                        val updatedPanel = (existingPanel ?: MangaPanel(pageId = page.id, panelIndex = panelIndex)).copy(
                            userPrompt = state.activePrompt,
                            enrichedPrompt = coherentPrompt,
                            characterId = state.selectedCharacterId,
                            backgroundId = state.selectedBackgroundId,
                            imagePath = result.fallbackImagePath,
                            dialogueText = state.dialogueText.ifBlank { null },
                            bubbleType = state.bubbleType,
                            bubbleFont = state.bubbleFont,
                            bubbleFontSize = state.bubbleFontSize,
                            bubbleTailDirection = state.bubbleTailDirection,
                            bubbleBgColor = state.bubbleBgColor,
                            bubbleTextColor = state.bubbleTextColor,
                            bubbleBorderColor = state.bubbleBorderColor
                        )
                        val savedPanelId = repository.savePanel(updatedPanel)

                        if (character != null && character.firstAppearanceImagePath.isNullOrBlank()) {
                            repository.updateCharacterFirstAppearance(
                                charId = character.id,
                                imagePath = result.fallbackImagePath,
                                panelId = savedPanelId
                            )
                        }
                    }

                    _editorState.value = _editorState.value.copy(
                        isGenerating = false,
                        generationStage = "",
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun addPanelToCurrentPage() {
        val page = _selectedPage.value ?: return
        val currentCount = _currentPanels.value.size
        viewModelScope.launch {
            repository.savePanel(
                MangaPanel(
                    pageId = page.id,
                    panelIndex = currentCount,
                    userPrompt = "Action suivante...",
                    characterId = _editorState.value.selectedCharacterId,
                    backgroundId = _editorState.value.selectedBackgroundId
                )
            )
        }
    }

    fun addNewPage() {
        val project = _selectedProject.value ?: return
        val nextNumber = (_currentPages.value.maxOfOrNull { it.pageNumber } ?: 0) + 1
        viewModelScope.launch {
            val pageId = repository.createPage(
                MangaPage(
                    projectId = project.id,
                    pageNumber = nextNumber,
                    layoutType = "TWO_PANELS_VERTICAL"
                )
            )
            repository.savePanel(
                MangaPanel(
                    pageId = pageId,
                    panelIndex = 0,
                    userPrompt = "Début de la page $nextNumber",
                    characterId = _editorState.value.selectedCharacterId,
                    backgroundId = _editorState.value.selectedBackgroundId
                )
            )
        }
    }

    fun createProject(title: String, synopsis: String, artStyle: String, colorMode: String) {
        viewModelScope.launch {
            val projectId = repository.saveProject(
                MangaProject(
                    title = title.ifBlank { "Nouveau Projet Manga" },
                    synopsis = synopsis,
                    artStyle = artStyle,
                    colorMode = colorMode
                )
            )
            val pageId = repository.createPage(MangaPage(projectId = projectId, pageNumber = 1))
            repository.savePanel(
                MangaPanel(
                    pageId = pageId,
                    panelIndex = 0,
                    userPrompt = "Première case d'ouverture",
                    characterId = _editorState.value.selectedCharacterId,
                    backgroundId = _editorState.value.selectedBackgroundId
                )
            )
            val created = repository.getProjectById(projectId)
            if (created != null) {
                selectProject(created)
            }
        }
    }

    fun deleteProject(project: MangaProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // Character Vault & Visual Consistency methods
    fun lockFirstAppearanceAnchor(characterId: Long, imagePath: String, panelId: Long?) {
        viewModelScope.launch {
            repository.updateCharacterFirstAppearance(characterId, imagePath, panelId)
            val char = repository.getCharacterById(characterId)
            _editorState.value = _editorState.value.copy(
                statusMessage = "Ancre de première apparition verrouillée pour ${char?.name ?: "le personnage"} !"
            )
        }
    }

    fun clearFirstAppearanceAnchor(characterId: Long) {
        viewModelScope.launch {
            repository.clearCharacterFirstAppearance(characterId)
            _editorState.value = _editorState.value.copy(
                statusMessage = "Ancre de première apparition réinitialisée."
            )
        }
    }

    fun regenerateCharacterUid(characterId: Long) {
        viewModelScope.launch {
            val char = repository.getCharacterById(characterId) ?: return@launch
            val newUid = com.example.data.model.generateCharacterUid(char.name)
            val newSeed = com.example.data.model.generateCharacterSeed(char.name + System.currentTimeMillis())
            repository.updateCharacterVisualUid(characterId, newUid, newSeed)
            _editorState.value = _editorState.value.copy(
                statusMessage = "Nouvel identifiant de cohérence généré : $newUid"
            )
        }
    }

    fun saveCharacter(character: CharacterProfile) {
        viewModelScope.launch {
            val id = repository.saveCharacter(character)
            _editorState.value = _editorState.value.copy(
                selectedCharacterId = id,
                statusMessage = "Fiche personnage ${character.name} enregistrée avec succès !"
            )
        }
    }

    fun deleteCharacter(character: CharacterProfile) {
        viewModelScope.launch {
            repository.deleteCharacter(character)
            if (_editorState.value.selectedCharacterId == character.id) {
                _editorState.value = _editorState.value.copy(selectedCharacterId = null)
            }
        }
    }

    // Background Vault methods
    fun saveBackground(background: BackgroundProfile) {
        viewModelScope.launch {
            val id = repository.saveBackground(background)
            _editorState.value = _editorState.value.copy(
                selectedBackgroundId = id,
                statusMessage = "Décor ${background.name} enregistré avec succès !"
            )
        }
    }

    fun deleteBackground(background: BackgroundProfile) {
        viewModelScope.launch {
            repository.deleteBackground(background)
            if (_editorState.value.selectedBackgroundId == background.id) {
                _editorState.value = _editorState.value.copy(selectedBackgroundId = null)
            }
        }
    }

    fun clearStatusMessage() {
        _editorState.value = _editorState.value.copy(statusMessage = null, errorMessage = null)
    }

    // Storyboard Grid Drag-and-Drop & Layout Methods
    fun applyCharacterAndPoseToPanel(
        panel: MangaPanel,
        character: CharacterProfile?,
        poseAction: String?,
        camera: CameraPerspective? = null,
        background: BackgroundProfile? = null
    ) {
        viewModelScope.launch {
            var newPrompt = panel.userPrompt
            if (!poseAction.isNullOrBlank()) {
                val charName = character?.name ?: "Le personnage"
                newPrompt = "$charName $poseAction"
            }
            val updated = panel.copy(
                characterId = character?.id ?: panel.characterId,
                backgroundId = background?.id ?: panel.backgroundId,
                userPrompt = newPrompt
            )
            repository.savePanel(updated)
            _editorState.value = _editorState.value.copy(
                statusMessage = "Case #${panel.panelIndex + 1} configurée avec ${character?.name ?: "le personnage"} !"
            )
        }
    }

    fun applyLayoutToCurrentPage(layoutType: String, panelCount: Int) {
        val page = _selectedPage.value ?: return
        viewModelScope.launch {
            repository.savePage(page.copy(layoutType = layoutType))
            val existingPanels = _currentPanels.value
            val diff = panelCount - existingPanels.size
            if (diff > 0) {
                for (i in 0 until diff) {
                    val nextIndex = existingPanels.size + i
                    repository.savePanel(
                        MangaPanel(
                            pageId = page.id,
                            panelIndex = nextIndex,
                            userPrompt = "Case $nextIndex",
                            characterId = _editorState.value.selectedCharacterId,
                            backgroundId = _editorState.value.selectedBackgroundId
                        )
                    )
                }
            } else if (diff < 0) {
                // Keep the first panelCount panels, delete the excess
                val toDelete = existingPanels.drop(panelCount)
                toDelete.forEach { repository.deletePanel(it) }
            }
            _editorState.value = _editorState.value.copy(
                statusMessage = "Grille de page mise à jour ($layoutType) !"
            )
        }
    }

    fun clearPanelImage(panel: MangaPanel) {
        viewModelScope.launch {
            repository.savePanel(panel.copy(imagePath = null))
            _editorState.value = _editorState.value.copy(
                statusMessage = "Case #${panel.panelIndex + 1} réinitialisée pour le storyboard !"
            )
        }
    }

    /**
     * Splits a wide panel into dynamic sequential sub-panels (e.g. Action -> Reaction -> Impact)
     * facilitating manga sequential storytelling.
     */
    fun splitPanelIntoSubPanels(targetPanel: MangaPanel, subCount: Int = 2) {
        val page = _selectedPage.value ?: return
        viewModelScope.launch {
            val existingPanels = _currentPanels.value
            val targetIdx = targetPanel.panelIndex
            val basePrompt = targetPanel.userPrompt.ifBlank { "Séquence d'action" }

            // Sequential storytelling stage presets for manga dynamism
            val sequentialPrompts = when (subCount) {
                3 -> listOf(
                    "$basePrompt - [Étape 1: Élan & Préparation]",
                    "$basePrompt - [Étape 2: Climax & Tension]",
                    "$basePrompt - [Étape 3: Impact & Réaction]"
                )
                else -> listOf(
                    "$basePrompt - [Étape 1: Action initiale]",
                    "$basePrompt - [Étape 2: Réaction / Contre-coup]"
                )
            }

            // Update the target panel as the first sub-panel
            val firstSubPanel = targetPanel.copy(
                userPrompt = sequentialPrompts[0]
            )
            repository.savePanel(firstSubPanel)

            // Shift later panels up to make space for the new sub-panels
            val laterPanels = existingPanels.filter { it.panelIndex > targetIdx }
            val shift = subCount - 1
            for (panel in laterPanels.sortedByDescending { it.panelIndex }) {
                repository.savePanel(panel.copy(panelIndex = panel.panelIndex + shift))
            }

            // Insert new sub-panels right after targetPanel
            for (i in 1 until subCount) {
                val newSubPanel = MangaPanel(
                    pageId = page.id,
                    panelIndex = targetIdx + i,
                    userPrompt = sequentialPrompts.getOrElse(i) { "$basePrompt - [Sous-case ${i + 1}]" },
                    characterId = targetPanel.characterId,
                    backgroundId = targetPanel.backgroundId,
                    dialogueText = null,
                    imagePath = null
                )
                repository.savePanel(newSubPanel)
            }

            // Also update page layout type to custom dynamic or 3-panels if applicable
            val newTotalCount = existingPanels.size + (subCount - 1)
            val updatedLayout = when {
                newTotalCount >= 4 -> GridPageLayout.FOUR_YONKOMA.id
                newTotalCount == 3 -> GridPageLayout.THREE_MANGA.id
                else -> GridPageLayout.TWO_VERTICAL.id
            }
            repository.savePage(page.copy(layoutType = updatedLayout))

            _editorState.value = _editorState.value.copy(
                statusMessage = "Case #${targetIdx + 1} divisée en $subCount sous-cases séquentielles dynamiques !"
            )
        }
    }
}
