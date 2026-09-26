package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ai.CameraPerspective
import com.example.ai.GeminiMangaService
import com.example.ai.MangaArtStyle
import com.example.ai.ScenePromptDraft
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPanel
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File

/**
 * AI Prompt Generator Interface integrated within the Manga Canvas Editor.
 * Enables creators to draft highly descriptive scene prompts tailored to their
 * selected character profiles, visual DNA (#UID), and chosen manga artistic styles.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CanvasAiPromptGeneratorDialog(
    initialPanelIndex: Int,
    panels: List<MangaPanel>,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    initialArtStyle: MangaArtStyle = MangaArtStyle.MANGA_SHONEN,
    onDismiss: () -> Unit,
    onApplyPrompt: (panelIndex: Int, prompt: String, characterId: Long?, backgroundId: Long?, soundEffect: String?) -> Unit,
    onGenerateScenePrompts: (suspend (CharacterProfile?, MangaArtStyle, String, String, String, String) -> List<ScenePromptDraft>)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiMangaService(context) }

    // Target panel selection
    var targetPanelIndex by remember { mutableIntStateOf(initialPanelIndex.coerceIn(0, (panels.size - 1).coerceAtLeast(0))) }

    // Active selection states
    var selectedCharacterId by remember {
        val initialCharId = panels.getOrNull(targetPanelIndex)?.characterId ?: characters.firstOrNull()?.id
        mutableStateOf(initialCharId)
    }
    var selectedArtStyle by remember { mutableStateOf(initialArtStyle) }
    var selectedBackgroundId by remember {
        val initialBgId = panels.getOrNull(targetPanelIndex)?.backgroundId ?: backgrounds.firstOrNull()?.id
        mutableStateOf(initialBgId)
    }
    var selectedCamera by remember { mutableStateOf<CameraPerspective>(CameraPerspective.LOW_ANGLE) }

    // Character dynamic trait pickers
    var selectedExpression by remember { mutableStateOf("Regard intense & déterminé") }
    var selectedActionBeat by remember { mutableStateOf("Dégaine son arme en plein saut") }
    var selectedAuraEffect by remember { mutableStateOf("Aura d'énergie incandescente") }

    // Scene & Mood settings
    var selectedMoodLighting by remember { mutableStateOf("Contre-jour dramatique avec rim light vif") }
    var userCustomIdea by remember { mutableStateOf("") }

    // Final draft prompt state
    var draftedPromptText by remember {
        val currentPanelPrompt = panels.getOrNull(targetPanelIndex)?.userPrompt ?: ""
        mutableStateOf(currentPanelPrompt)
    }
    var detectedSoundEffect by remember { mutableStateOf("💥 BAM !!") }

    // AI Generation states
    var isGeneratingAiDrafts by remember { mutableStateOf(false) }
    var aiStatusMessage by remember { mutableStateOf<String?>(null) }
    val generatedDrafts = remember { mutableStateListOf<ScenePromptDraft>() }

    // Character DNA Card expansion toggle
    var isCharacterDnaExpanded by remember { mutableStateOf(false) }

    // Tab state: 0 = Composition & Paramètres, 1 = Suggestions IA générées (3)
    var selectedTab by remember { mutableIntStateOf(0) }

    val activeCharacter = remember(characters, selectedCharacterId) {
        characters.firstOrNull { it.id == selectedCharacterId }
    }
    val activeBackground = remember(backgrounds, selectedBackgroundId) {
        backgrounds.firstOrNull { it.id == selectedBackgroundId }
    }

    // Function to assemble live deterministic structured prompt
    fun assembleLivePrompt(): String {
        val sb = StringBuilder()
        sb.append("Plan manga illustré : ")

        if (activeCharacter != null) {
            sb.append("${activeCharacter.name} (#${activeCharacter.visualUid})")
            val traits = listOfNotNull(
                selectedExpression.takeIf { it.isNotBlank() },
                selectedActionBeat.takeIf { it.isNotBlank() },
                userCustomIdea.trim().takeIf { it.isNotBlank() },
                selectedAuraEffect.takeIf { it.isNotBlank() }
            )
            if (traits.isNotEmpty()) {
                sb.append(" [${traits.joinToString(", ")}]")
            }
            sb.append(". ")
        } else if (userCustomIdea.isNotBlank()) {
            sb.append("${userCustomIdea.trim()}. ")
        } else {
            sb.append("Scène dynamique d'action. ")
        }

        if (activeBackground != null) {
            sb.append("Décor: ${activeBackground.name}, ")
        }
        sb.append("Éclairage: $selectedMoodLighting. ")
        sb.append("Cadrage: ${selectedCamera.displayName}. ")
        sb.append("Style: ${selectedArtStyle.displayName} (${selectedArtStyle.subtitle}), encrage professionnel haute résolution.")

        return sb.toString()
    }

    // Auto-update draftedPromptText when starting if empty
    LaunchedEffect(Unit) {
        if (draftedPromptText.isBlank()) {
            draftedPromptText = assembleLivePrompt()
        }
    }

    // Coroutine helper to trigger AI draft suggestions
    fun triggerAiDraftGeneration() {
        isGeneratingAiDrafts = true
        aiStatusMessage = "Gemini 3.5 Flash analyse l'ADN du personnage et le style ${selectedArtStyle.displayName}..."
        coroutineScope.launch {
            try {
                val drafts = if (onGenerateScenePrompts != null) {
                    onGenerateScenePrompts(
                        activeCharacter,
                        selectedArtStyle,
                        userCustomIdea.ifBlank { selectedActionBeat },
                        activeBackground?.name ?: "Environnement stylisé",
                        selectedMoodLighting,
                        selectedCamera.displayName
                    )
                } else {
                    geminiService.generateScenePromptDrafts(
                        character = activeCharacter,
                        artStyle = selectedArtStyle,
                        sceneTheme = userCustomIdea.ifBlank { selectedActionBeat },
                        setting = activeBackground?.name ?: "Environnement stylisé",
                        mood = selectedMoodLighting,
                        cameraAngle = selectedCamera.displayName
                    )
                }

                generatedDrafts.clear()
                generatedDrafts.addAll(drafts)
                if (drafts.isNotEmpty()) {
                    selectedTab = 1 // Switch to suggestions tab
                    val first = drafts.first()
                    draftedPromptText = first.formattedPrompt
                    detectedSoundEffect = first.soundEffect
                    aiStatusMessage = "${drafts.size} suggestions de scènes générées avec succès !"
                } else {
                    aiStatusMessage = "Aucune proposition retournée, utilisation de la composition manuelle."
                }
            } catch (e: Exception) {
                aiStatusMessage = "Erreur IA: ${e.localizedMessage ?: "Tentative de repli local"}"
            } finally {
                isGeneratingAiDrafts = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = InkMidnight),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("canvas_ai_prompt_generator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF6B21A8).copy(alpha = 0.3f))
                                .border(1.dp, Color(0xFFC084FC), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "RÉDACTEUR DE PROMPT IA DE SCÈNE",
                                    color = MangaPaperWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF9333EA),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "GEMINI 3.5 FLASH",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Brouillons cinématiques basés sur le personnage & le style graphique",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Panel Selector Row
                Surface(
                    color = Color(0xFF141824),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF263047)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Case cible :",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                panels.forEachIndexed { idx, _ ->
                                    val isSelected = idx == targetPanelIndex
                                    Surface(
                                        color = if (isSelected) Color(0xFF9333EA) else Color(0xFF1E2433),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFC084FC) else Color(0xFF333E56)),
                                        modifier = Modifier
                                            .clickable { targetPanelIndex = idx }
                                            .testTag("target_panel_chip_$idx")
                                    ) {
                                        Text(
                                            text = "Case #${idx + 1}",
                                            color = if (isSelected) Color.White else MangaPaperWhite,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Dimensions indicator of target panel
                        panels.getOrNull(targetPanelIndex)?.let { p ->
                            Text(
                                text = "${(p.widthFraction * 100).toInt()}% • ${p.heightDp}dp",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Switcher: [Paramètres & Composition] vs [Suggestions IA (N)]
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F131D),
                    contentColor = Color(0xFFC084FC),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFC084FC)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paramètres & Style", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (generatedDrafts.isEmpty()) "Suggestions IA" else "Suggestions IA (${generatedDrafts.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        // TAB 0: Character selection, Traits, Style, Setting
                        CompositionParametersSection(
                            characters = characters,
                            backgrounds = backgrounds,
                            selectedCharacterId = selectedCharacterId,
                            selectedArtStyle = selectedArtStyle,
                            selectedBackgroundId = selectedBackgroundId,
                            selectedCamera = selectedCamera,
                            selectedExpression = selectedExpression,
                            selectedActionBeat = selectedActionBeat,
                            selectedAuraEffect = selectedAuraEffect,
                            selectedMoodLighting = selectedMoodLighting,
                            userCustomIdea = userCustomIdea,
                            isCharacterDnaExpanded = isCharacterDnaExpanded,
                            onCharacterSelected = {
                                selectedCharacterId = it
                                val c = characters.firstOrNull { char -> char.id == it }
                                if (c != null && c.defaultExpression.isNotBlank()) {
                                    selectedExpression = c.defaultExpression
                                }
                                draftedPromptText = assembleLivePrompt()
                            },
                            onArtStyleSelected = {
                                selectedArtStyle = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onBackgroundSelected = {
                                selectedBackgroundId = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onCameraSelected = {
                                selectedCamera = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onExpressionSelected = {
                                selectedExpression = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onActionBeatSelected = {
                                selectedActionBeat = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onAuraEffectSelected = {
                                selectedAuraEffect = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onMoodLightingSelected = {
                                selectedMoodLighting = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onUserCustomIdeaChanged = {
                                userCustomIdea = it
                                draftedPromptText = assembleLivePrompt()
                            },
                            onToggleCharacterDna = { isCharacterDnaExpanded = !isCharacterDnaExpanded },
                            onAssembleLivePrompt = { draftedPromptText = assembleLivePrompt() }
                        )
                    } else {
                        // TAB 1: Generated AI Scene Drafts
                        AiDraftsSuggestionsSection(
                            drafts = generatedDrafts,
                            isGenerating = isGeneratingAiDrafts,
                            statusMessage = aiStatusMessage,
                            onSelectDraft = { draft ->
                                draftedPromptText = draft.formattedPrompt
                                detectedSoundEffect = draft.soundEffect
                                Toast.makeText(context, "Brouillon '${draft.title}' chargé dans l'éditeur", Toast.LENGTH_SHORT).show()
                            },
                            onApplyDraftDirectly = { draft ->
                                onApplyPrompt(
                                    targetPanelIndex,
                                    draft.formattedPrompt,
                                    selectedCharacterId,
                                    selectedBackgroundId,
                                    draft.soundEffect
                                )
                                Toast.makeText(context, "Prompt appliqué à la Case #${targetPanelIndex + 1} !", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            onRegenerate = { triggerAiDraftGeneration() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Active Prompt Editor & Final Actions Bar
                Surface(
                    color = Color(0xFF10141F),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF333E56)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PROMPT ACTIF POUR LA CASE #${targetPanelIndex + 1}",
                                    color = Color(0xFFC084FC),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                if (activeCharacter != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = QiGold.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "#${activeCharacter.visualUid}",
                                            color = QiGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Quick copy prompt button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(draftedPromptText))
                                        Toast.makeText(context, "Prompt copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copier", color = TextMuted, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = draftedPromptText,
                            onValueChange = { draftedPromptText = it },
                            placeholder = { Text("Le prompt descriptif apparaîtra ici...", fontSize = 12.sp, color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InkMidnight,
                                unfocusedContainerColor = InkMidnight,
                                focusedBorderColor = Color(0xFFC084FC),
                                unfocusedBorderColor = Color(0xFF333E56),
                                focusedTextColor = MangaPaperWhite,
                                unfocusedTextColor = MangaPaperWhite
                            ),
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("canvas_ai_drafted_prompt_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons Row: Generate with AI, Cancel, Apply to Panel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // AI Generate Drafts Button
                            Button(
                                onClick = { triggerAiDraftGeneration() },
                                enabled = !isGeneratingAiDrafts,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("btn_generate_ai_scene_prompts")
                            ) {
                                if (isGeneratingAiDrafts) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Génération IA...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Générer par IA (Gemini)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Annuler", color = TextMuted, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        if (draftedPromptText.isNotBlank()) {
                                            onApplyPrompt(
                                                targetPanelIndex,
                                                draftedPromptText,
                                                selectedCharacterId,
                                                selectedBackgroundId,
                                                detectedSoundEffect
                                            )
                                            Toast.makeText(context, "Prompt appliqué à la Case #${targetPanelIndex + 1} !", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("btn_apply_ai_prompt_to_panel")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Appliquer à la Case #${targetPanelIndex + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 0: Parameters & Composition Section.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompositionParametersSection(
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    selectedCharacterId: Long?,
    selectedArtStyle: MangaArtStyle,
    selectedBackgroundId: Long?,
    selectedCamera: CameraPerspective,
    selectedExpression: String,
    selectedActionBeat: String,
    selectedAuraEffect: String,
    selectedMoodLighting: String,
    userCustomIdea: String,
    isCharacterDnaExpanded: Boolean,
    onCharacterSelected: (Long?) -> Unit,
    onArtStyleSelected: (MangaArtStyle) -> Unit,
    onBackgroundSelected: (Long?) -> Unit,
    onCameraSelected: (CameraPerspective) -> Unit,
    onExpressionSelected: (String) -> Unit,
    onActionBeatSelected: (String) -> Unit,
    onAuraEffectSelected: (String) -> Unit,
    onMoodLightingSelected: (String) -> Unit,
    onUserCustomIdeaChanged: (String) -> Unit,
    onToggleCharacterDna: () -> Unit,
    onAssembleLivePrompt: () -> Unit
) {
    val activeCharacter = remember(characters, selectedCharacterId) {
        characters.firstOrNull { it.id == selectedCharacterId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Character Library Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = InkSurface),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, InkBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PERSONNAGE & COHÉRENCE VISUELLE",
                            color = ManhuaCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (activeCharacter != null) {
                        Surface(
                            color = QiGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onToggleCharacterDna() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ADN #${activeCharacter.visualUid}",
                                    color = QiGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (isCharacterDnaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = QiGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Character Horizontal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // None option
                    val isNoneSelected = selectedCharacterId == null
                    Surface(
                        color = if (isNoneSelected) ManhuaCyan.copy(alpha = 0.2f) else Color(0xFF1B2233),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isNoneSelected) ManhuaCyan else Color(0xFF333E56)),
                        modifier = Modifier
                            .clickable { onCharacterSelected(null) }
                            .testTag("char_chip_none")
                    ) {
                        Text(
                            text = "Aucun personnage",
                            color = if (isNoneSelected) ManhuaCyan else MangaPaperWhite,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Characters list
                    characters.forEach { char ->
                        val isSelected = char.id == selectedCharacterId
                        Surface(
                            color = if (isSelected) ManhuaCyan.copy(alpha = 0.2f) else Color(0xFF1B2233),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else Color(0xFF333E56)),
                            modifier = Modifier
                                .clickable { onCharacterSelected(char.id) }
                                .testTag("char_chip_${char.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatarFile = char.referenceImagePath?.let { File(it) }
                                if (avatarFile != null && avatarFile.exists()) {
                                    AsyncImage(
                                        model = avatarFile,
                                        contentDescription = char.name,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Column {
                                    Text(
                                        text = char.name,
                                        color = if (isSelected) ManhuaCyan else MangaPaperWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "#${char.visualUid}",
                                        color = if (isSelected) QiGold else Color(0xFF94A3B8),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Expanded DNA details
                AnimatedVisibility(visible = isCharacterDnaExpanded && activeCharacter != null) {
                    activeCharacter?.let { char ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(Color(0xFF0F131D), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("Visual Consistency Anchor DNA:", color = QiGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("• Cheveux: ${char.hairStyleColor.ifBlank { "N/A" }}", color = MangaPaperWhite, fontSize = 10.sp)
                            Text("• Yeux: ${char.eyeDescription.ifBlank { "N/A" }}", color = MangaPaperWhite, fontSize = 10.sp)
                            Text("• Tenue: ${char.clothingDescription.ifBlank { "N/A" }}", color = MangaPaperWhite, fontSize = 10.sp)
                            if (char.distinctiveFeatures.isNotBlank()) {
                                Text("• Traits distinctifs: ${char.distinctiveFeatures}", color = MangaPaperWhite, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Dynamic Character Traits (Expression, Action beat, Aura effect)
                if (activeCharacter != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Expression faciale du personnage :", color = TextMuted, fontSize = 10.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        val expressionPresets = listOf(
                            "Regard intense & déterminé",
                            "Sourire narquois en coin",
                            "Cri de rage combatif",
                            "Calme & stoïque",
                            "Regard choqué / ému"
                        )
                        expressionPresets.forEach { expr ->
                            val isSel = selectedExpression == expr
                            Surface(
                                color = if (isSel) QiGold.copy(alpha = 0.25f) else Color(0xFF151A27),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (isSel) QiGold else Color(0xFF2A3449)),
                                modifier = Modifier.clickable { onExpressionSelected(expr) }
                            ) {
                                Text(
                                    text = expr,
                                    color = if (isSel) QiGold else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Action / Pose dynamique :", color = TextMuted, fontSize = 10.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        val actionPresets = listOf(
                            "Dégaine son arme en plein saut",
                            "Posture offensive martiale",
                            "Concentration d'attaque spéciale",
                            "Marche résolue sous la pluie",
                            "Haletant après l'impact"
                        )
                        actionPresets.forEach { act ->
                            val isSel = selectedActionBeat == act
                            Surface(
                                color = if (isSel) MangaCrimson.copy(alpha = 0.25f) else Color(0xFF151A27),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (isSel) MangaCrimson else Color(0xFF2A3449)),
                                modifier = Modifier.clickable { onActionBeatSelected(act) }
                            ) {
                                Text(
                                    text = act,
                                    color = if (isSel) MangaCrimson else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Artistic Style & Ink Specifications
        Card(
            colors = CardDefaults.cardColors(containerColor = InkSurface),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, InkBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = QiGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "STYLE ARTISTIQUE & TECHNIQUE D'ENCRAGE",
                        color = QiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Manga Styles horizontal scrollable cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MangaArtStyle.values().forEach { style ->
                        val isSelected = style == selectedArtStyle
                        Surface(
                            color = if (isSelected) QiGold.copy(alpha = 0.15f) else Color(0xFF141926),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) QiGold else Color(0xFF2F3B52)),
                            modifier = Modifier
                                .clickable { onArtStyleSelected(style) }
                                .testTag("style_chip_${style.name}")
                        ) {
                            Column(modifier = Modifier.padding(8.dp).width(135.dp)) {
                                Text(
                                    text = style.displayName,
                                    color = if (isSelected) QiGold else MangaPaperWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = style.subtitle,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🖋️ ${style.penTool}",
                                    color = if (isSelected) Color(0xFFFDE68A) else TextMuted,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Camera Perspectives
                Text("Angle de caméra cinématique :", color = TextMuted, fontSize = 10.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    CameraPerspective.values().forEach { cam ->
                        val isSel = cam == selectedCamera
                        Surface(
                            color = if (isSel) Color(0xFF9333EA).copy(alpha = 0.25f) else Color(0xFF151A27),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isSel) Color(0xFFC084FC) else Color(0xFF2A3449)),
                            modifier = Modifier.clickable { onCameraSelected(cam) }
                        ) {
                            Text(
                                text = cam.displayName,
                                color = if (isSel) Color(0xFFE9D5FF) else Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Setting, Mood & Custom Action Seed
        Card(
            colors = CardDefaults.cardColors(containerColor = InkSurface),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, InkBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Landscape, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DÉCOR, AMBIANCE & MOT-CLÉ DE DÉPART",
                        color = MangaCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Backgrounds list
                Text("Décor / Emplacement :", color = TextMuted, fontSize = 10.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isNoneBg = selectedBackgroundId == null
                    Surface(
                        color = if (isNoneBg) MangaCrimson.copy(alpha = 0.2f) else Color(0xFF151A27),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (isNoneBg) MangaCrimson else Color(0xFF2A3449)),
                        modifier = Modifier.clickable { onBackgroundSelected(null) }
                    ) {
                        Text("Décor générique", color = if (isNoneBg) MangaCrimson else Color(0xFFCBD5E1), fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                    }

                    backgrounds.forEach { bg ->
                        val isSel = bg.id == selectedBackgroundId
                        Surface(
                            color = if (isSel) MangaCrimson.copy(alpha = 0.2f) else Color(0xFF151A27),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isSel) MangaCrimson else Color(0xFF2A3449)),
                            modifier = Modifier.clickable { onBackgroundSelected(bg.id) }
                        ) {
                            Text(bg.name, color = if (isSel) MangaCrimson else Color(0xFFCBD5E1), fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lighting & Mood
                Text("Ambiance & Éclairage :", color = TextMuted, fontSize = 10.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    val moodPresets = listOf(
                        "Contre-jour dramatique avec rim light vif",
                        "Néons cyan et pourpre futuristes",
                        "Clair de lune froid et ombres denses",
                        "Lumière rasante dorée de fin de journée",
                        "Éclairage d'orage avec éclairs violents"
                    )
                    moodPresets.forEach { mood ->
                        val isSel = selectedMoodLighting == mood
                        Surface(
                            color = if (isSel) QiGold.copy(alpha = 0.2f) else Color(0xFF151A27),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isSel) QiGold else Color(0xFF2A3449)),
                            modifier = Modifier.clickable { onMoodLightingSelected(mood) }
                        ) {
                            Text(
                                text = mood,
                                color = if (isSel) QiGold else Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional seed input
                OutlinedTextField(
                    value = userCustomIdea,
                    onValueChange = onUserCustomIdeaChanged,
                    placeholder = { Text("Idée libre (ex: Il pare l'assaut surprise au dernier instant)", fontSize = 11.sp, color = TextMuted) },
                    label = { Text("Idée ou action libre (optionnel)", fontSize = 10.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InkMidnight,
                        unfocusedContainerColor = InkMidnight,
                        focusedBorderColor = ManhuaCyan,
                        unfocusedBorderColor = Color(0xFF333E56),
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Tab 1: AI Generated Scene Drafts Section.
 */
@Composable
private fun AiDraftsSuggestionsSection(
    drafts: List<ScenePromptDraft>,
    isGenerating: Boolean,
    statusMessage: String?,
    onSelectDraft: (ScenePromptDraft) -> Unit,
    onApplyDraftDirectly: (ScenePromptDraft) -> Unit,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFC084FC), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = statusMessage ?: "Génération des suggestions de scènes...",
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gemini analyse les traits visuels et l'encrage...",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else if (drafts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFC084FC).copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aucune suggestion générée pour le moment",
                        color = MangaPaperWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cliquez sur 'Générer par IA (Gemini)' pour obtenir 3 propositions cinématiques adaptées au personnage et style.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRegenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE))
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lancer la génération IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "3 PROPOSITIONS CINÉMATIQUES PAR GEMINI",
                            color = Color(0xFFC084FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Régénérer", tint = Color(0xFFC084FC), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                itemsIndexed(drafts) { index, draft ->
                    SceneDraftCard(
                        draft = draft,
                        draftIndex = index + 1,
                        onSelect = { onSelectDraft(draft) },
                        onApply = { onApplyDraftDirectly(draft) }
                    )
                }
            }
        }
    }
}

/**
 * Visual card displaying an individual AI Scene Prompt Draft proposition.
 */
@Composable
private fun SceneDraftCard(
    draft: ScenePromptDraft,
    draftIndex: Int,
    onSelect: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141825)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF4A346C)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scene_draft_card_$draftIndex")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Title & SFX Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF7E22CE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "#$draftIndex",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = draft.title,
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (draft.soundEffect.isNotBlank()) {
                    Surface(
                        color = MangaCrimson.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MangaCrimson),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = draft.soundEffect,
                            color = MangaCrimson,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Visual Description
            Text(
                text = draft.description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row: Camera, Lighting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (draft.camera.isNotBlank()) {
                    Surface(
                        color = Color(0xFF1E2536),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(draft.camera, color = Color(0xFF94A3B8), fontSize = 9.sp)
                        }
                    }
                }

                if (draft.lightingMood.isNotBlank()) {
                    Surface(
                        color = Color(0xFF1E2536),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = QiGold, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(draft.lightingMood, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC084FC)),
                    border = BorderStroke(1.dp, Color(0xFFC084FC).copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Charger & Ajuster", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Appliquer direct", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
