package com.example.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.CameraPerspective
import com.example.ai.ColorRenderingMode
import com.example.ai.LineWeightStyle
import com.example.ai.MangaArtStyle
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

/**
 * Trait categories for constructing detailed character specifications.
 */
object TraitPresets {
    val EXPRESSIONS = listOf(
        "Regard intense & déterminé",
        "Sourire confiant en coin",
        "Cri de rage combatif",
        "Expression stoïque & calme",
        "Regard surpris écarquillé",
        "Sourire doux et bienveillant"
    )

    val POSES_ACTIONS = listOf(
        "Dégaine son arme en plein saut",
        "Posture d'arts martiaux offensive",
        "Marche résolue sous la pluie",
        "Projette une rafale d'énergie spectaculaire",
        "Haletant avec vêtements déchirés",
        "Assis en tailleur en méditation profonde",
        "Regard par-dessus l'épaule"
    )

    val CLOTHING_ALTERATIONS = listOf(
        "Tenue de combat complète",
        "Veste flottant au vent",
        "Cape en lambeaux héroïque",
        "Kimono orné traditionnel",
        "Uniforme scolaire moderne",
        "Armure cybernétique étincelante"
    )

    val DISTINCTIVE_EFFECTS = listOf(
        "Aura de Qi incandescente",
        "Étincelles électriques crépitantes",
        "Pétales de cerisier virevoltants",
        "Flammes ténébreuses",
        "Lumière divine céleste",
        "Vapeur de sueur et d'effort"
    )

    val SCENE_ENVIRONMENTS = listOf(
        "Toits de Tokyo sous une pluie battante et néons",
        "Temple ancien au sommet d'une montagne embrumée",
        "Ruelle sombre et étroite aux briques usées",
        "Forêt de bambous mystique baignée de clair de lune",
        "Arène de tournoi en ruines couverte de poussière",
        "Salle de classe au crépuscule doré"
    )

    val LIGHTING_MOODS = listOf(
        "Contre-jour dramatique avec rim light vif",
        "Lumière rasante dorée de fin de journée",
        "Néons cyan et pourpre futuristes",
        "Clair de lune froid et ombres denses",
        "Éclairage d'orage avec éclairs violents",
        "Plein soleil zénithal écrasant"
    )

    val WEATHER_EFFECTS = listOf(
        "Pluie battante & reflets aqueux",
        "Vent violent soulevant poussière et feuilles",
        "Chute de neige fine et brume blanche",
        "Tempête d'étincelles ou de cendres",
        "Ciel dégagé sans turbulences",
        "Brouillard épais et mystérieux"
    )

    val COMPOSITION_FRAMINGS = listOf(
        "Plongée vertigineuse dramatique",
        "Contre-plongée héroïque imposante",
        "Plan moyen dynamique à hauteur d'yeux",
        "Gros plan intense centré sur le regard",
        "Plan large d'ambiance avec décor immersif",
        "Cadrage hollandais penché dynamique"
    )
}

/**
 * Interactive Prompt Builder UI Component.
 * Allows creators to assemble high-fidelity generation prompts by picking
 * art styles, fine-tuning character traits (expressions, poses, attire, visual effects),
 * configuring scene settings (environment, lighting, weather, camera angle), and
 * seamlessly applying the resulting prompt to the Studio editor or copying it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptBuilderCard(
    currentPrompt: String,
    selectedArtStyle: MangaArtStyle,
    characters: List<CharacterProfile>,
    backgrounds: List<BackgroundProfile>,
    selectedCharacterId: Long?,
    selectedBackgroundId: Long?,
    selectedCamera: CameraPerspective,
    selectedColorMode: ColorRenderingMode,
    selectedLineStyle: LineWeightStyle,
    onPromptConstructed: (String) -> Unit,
    onArtStyleSelected: (MangaArtStyle) -> Unit,
    onCharacterSelected: (Long?) -> Unit,
    onBackgroundSelected: (Long?) -> Unit,
    onCameraSelected: (CameraPerspective) -> Unit,
    onColorModeSelected: (ColorRenderingMode) -> Unit,
    onLineStyleSelected: (LineWeightStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Builder Selection State
    var activeArtStyle by remember(selectedArtStyle) { mutableStateOf(selectedArtStyle) }
    var activeCharacterId by remember(selectedCharacterId) { mutableStateOf(selectedCharacterId) }
    var activeBackgroundId by remember(selectedBackgroundId) { mutableStateOf(selectedBackgroundId) }
    var activeCamera by remember(selectedCamera) { mutableStateOf(selectedCamera) }

    // Character traits
    var selectedExpression by remember { mutableStateOf("") }
    var selectedPoseAction by remember { mutableStateOf("") }
    var selectedClothing by remember { mutableStateOf("") }
    var selectedEffect by remember { mutableStateOf("") }
    var customActionInput by remember { mutableStateOf("") }

    // Scene settings
    var selectedEnvironmentPreset by remember { mutableStateOf("") }
    var selectedLighting by remember { mutableStateOf("") }
    var selectedWeather by remember { mutableStateOf("") }
    var customSceneDetails by remember { mutableStateOf("") }

    // Active entities resolved
    val activeCharacter = remember(characters, activeCharacterId) {
        characters.firstOrNull { it.id == activeCharacterId }
    }
    val activeBackground = remember(backgrounds, activeBackgroundId) {
        backgrounds.firstOrNull { it.id == activeBackgroundId }
    }

    // Dynamic prompt construction preview
    val assembledPrompt by remember(
        activeArtStyle,
        activeCharacter,
        activeBackground,
        activeCamera,
        selectedExpression,
        selectedPoseAction,
        selectedClothing,
        selectedEffect,
        customActionInput,
        selectedEnvironmentPreset,
        selectedLighting,
        selectedWeather,
        customSceneDetails,
        selectedColorMode,
        selectedLineStyle
    ) {
        derivedStateOf {
            val sb = StringBuilder()

            // 1. Character part
            if (activeCharacter != null) {
                sb.append("${activeCharacter.name}")
                val traits = mutableListOf<String>()
                if (selectedExpression.isNotBlank()) traits.add(selectedExpression)
                if (selectedPoseAction.isNotBlank()) traits.add(selectedPoseAction)
                if (customActionInput.isNotBlank()) traits.add(customActionInput.trim())
                if (selectedClothing.isNotBlank()) traits.add(selectedClothing)
                if (selectedEffect.isNotBlank()) traits.add(selectedEffect)

                if (traits.isNotEmpty()) {
                    sb.append(" (${traits.joinToString(", ")})")
                } else if (activeCharacter.role.isNotBlank()) {
                    sb.append(" (${activeCharacter.role})")
                }
                sb.append(". ")
            } else if (customActionInput.isNotBlank() || selectedPoseAction.isNotBlank()) {
                val action = listOf(selectedPoseAction, customActionInput.trim()).filter { it.isNotBlank() }.joinToString(" - ")
                sb.append("$action. ")
            }

            // 2. Scene / Environment part
            val sceneParts = mutableListOf<String>()
            if (activeBackground != null) {
                sceneParts.add("Lieu: ${activeBackground.name}")
            }
            if (selectedEnvironmentPreset.isNotBlank()) {
                sceneParts.add(selectedEnvironmentPreset)
            }
            if (customSceneDetails.isNotBlank()) {
                sceneParts.add(customSceneDetails.trim())
            }
            if (selectedLighting.isNotBlank()) {
                sceneParts.add("Éclairage: $selectedLighting")
            }
            if (selectedWeather.isNotBlank()) {
                sceneParts.add("Météo/Ambiance: $selectedWeather")
            }

            if (sceneParts.isNotEmpty()) {
                sb.append("Décor: ${sceneParts.joinToString(", ")}. ")
            }

            // 3. Camera Framing
            sb.append("Cadrage: ${activeCamera.displayName}. ")

            // 4. Style & Technical Inking tags
            sb.append("Style: ${activeArtStyle.displayName} (${activeArtStyle.subtitle}), ${selectedColorMode.displayName}, ${selectedLineStyle.displayName}.")

            sb.toString()
        }
    }

    val clipboardManager = LocalClipboardManager.current

    Card(
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("prompt_builder_component")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row with collapse toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp)
                    .testTag("toggle_prompt_builder_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ManhuaCyan.copy(alpha = 0.15f))
                        .border(1.dp, ManhuaCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ManhuaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CONSTRUCTEUR DE PROMPT MODULAIRE",
                            color = ManhuaCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = QiGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "STYLES • TRAITS • DÉCORS",
                                color = QiGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isExpanded) "Sélectionnez les styles artistiques, traits du héros et paramètres de scène" else "Toucher pour ouvrir les sélecteurs de style, traits et scène",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Réduire" else "Dérouler",
                        tint = ManhuaCyan
                    )
                }
            }

            // Expanded interactive builder controls
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // -------------------------------------------------------------
                    // SECTION 1: ART STYLES SELECTION
                    // -------------------------------------------------------------
                    SectionHeader(
                        icon = Icons.Default.Palette,
                        title = "1. STYLE ARTISTIQUE & ENCRAGE",
                        badge = activeArtStyle.displayName
                    )

                    // Art Style Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MangaArtStyle.values().forEach { style ->
                            val isSelected = style == activeArtStyle
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeArtStyle = style
                                    onArtStyleSelected(style)
                                },
                                label = {
                                    Text(
                                        text = style.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MangaCrimson
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MangaCrimson.copy(alpha = 0.2f),
                                    selectedLabelColor = MangaPaperWhite,
                                    containerColor = InkMidnight,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = InkBorder,
                                    selectedBorderColor = MangaCrimson
                                ),
                                modifier = Modifier.testTag("style_chip_${style.id}")
                            )
                        }
                    }

                    // -------------------------------------------------------------
                    // SECTION 2: CHARACTER & CHARACTER TRAITS
                    // -------------------------------------------------------------
                    SectionHeader(
                        icon = Icons.Default.Person,
                        title = "2. PERSONNAGE & TRAITS PHYSIQUES",
                        badge = activeCharacter?.name ?: "Aucun sélectionné"
                    )

                    // Hero Selector Row
                    if (characters.isNotEmpty()) {
                        Text(
                            text = "Sélectionner un personnage du projet :",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            characters.forEach { char ->
                                val isSelected = char.id == activeCharacterId
                                Surface(
                                    color = if (isSelected) ManhuaCyan.copy(alpha = 0.15f) else InkMidnight,
                                    border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else InkBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable {
                                            activeCharacterId = char.id
                                            onCharacterSelected(char.id)
                                        }
                                        .testTag("builder_char_chip_${char.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    runCatching {
                                                        Color(android.graphics.Color.parseColor(char.signatureColor))
                                                    }.getOrDefault(ManhuaCyan)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = char.name,
                                                color = if (isSelected) ManhuaCyan else MangaPaperWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = char.role,
                                                color = TextMuted,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Expressions & Moods Chips
                    TraitCategoryChips(
                        categoryName = "Expression faciale",
                        presets = TraitPresets.EXPRESSIONS,
                        selectedTrait = selectedExpression,
                        onSelectTrait = { selectedExpression = if (selectedExpression == it) "" else it },
                        accentColor = ManhuaCyan
                    )

                    // Poses & Actions Chips
                    TraitCategoryChips(
                        categoryName = "Pose & Action corporelle",
                        presets = TraitPresets.POSES_ACTIONS,
                        selectedTrait = selectedPoseAction,
                        onSelectTrait = { selectedPoseAction = if (selectedPoseAction == it) "" else it },
                        accentColor = MangaCrimson
                    )

                    // Clothing / Alterations Chips
                    TraitCategoryChips(
                        categoryName = "Tenue vestimentaire",
                        presets = TraitPresets.CLOTHING_ALTERATIONS,
                        selectedTrait = selectedClothing,
                        onSelectTrait = { selectedClothing = if (selectedClothing == it) "" else it },
                        accentColor = QiGold
                    )

                    // Distinctive visual effects
                    TraitCategoryChips(
                        categoryName = "Effet d'aura ou particule",
                        presets = TraitPresets.DISTINCTIVE_EFFECTS,
                        selectedTrait = selectedEffect,
                        onSelectTrait = { selectedEffect = if (selectedEffect == it) "" else it },
                        accentColor = ManhuaCyan
                    )

                    // Custom user action input
                    OutlinedTextField(
                        value = customActionInput,
                        onValueChange = { customActionInput = it },
                        placeholder = { Text("Précision libre sur l'action du personnage (ex: épée brandie vers le ciel...)") },
                        label = { Text("Détail libre d'action du personnage", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ManhuaCyan,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_action_input_field")
                    )

                    // -------------------------------------------------------------
                    // SECTION 3: SCENE SETTINGS & ENVIRONMENT
                    // -------------------------------------------------------------
                    SectionHeader(
                        icon = Icons.Default.Landscape,
                        title = "3. PARAMÈTRES DE SCÈNE & DÉCORS",
                        badge = activeBackground?.name ?: "Ambiance libre"
                    )

                    // Preset Backgrounds from DB
                    if (backgrounds.isNotEmpty()) {
                        Text(
                            text = "Lieu sauvegardé du projet :",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            backgrounds.forEach { bg ->
                                val isSelected = bg.id == activeBackgroundId
                                Surface(
                                    color = if (isSelected) QiGold.copy(alpha = 0.15f) else InkMidnight,
                                    border = BorderStroke(1.dp, if (isSelected) QiGold else InkBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable {
                                            activeBackgroundId = if (activeBackgroundId == bg.id) null else bg.id
                                            onBackgroundSelected(activeBackgroundId)
                                        }
                                        .testTag("builder_bg_chip_${bg.id}")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            text = bg.name,
                                            color = if (isSelected) QiGold else MangaPaperWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = bg.category,
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Environment Presets
                    TraitCategoryChips(
                        categoryName = "Environnement / Décor type",
                        presets = TraitPresets.SCENE_ENVIRONMENTS,
                        selectedTrait = selectedEnvironmentPreset,
                        onSelectTrait = { selectedEnvironmentPreset = if (selectedEnvironmentPreset == it) "" else it },
                        accentColor = QiGold
                    )

                    // Lighting Moods
                    TraitCategoryChips(
                        categoryName = "Éclairage & Ambiance lumineuse",
                        presets = TraitPresets.LIGHTING_MOODS,
                        selectedTrait = selectedLighting,
                        onSelectTrait = { selectedLighting = if (selectedLighting == it) "" else it },
                        accentColor = ManhuaCyan
                    )

                    // Weather Effects
                    TraitCategoryChips(
                        categoryName = "Conditions météo & atmosphère",
                        presets = TraitPresets.WEATHER_EFFECTS,
                        selectedTrait = selectedWeather,
                        onSelectTrait = { selectedWeather = if (selectedWeather == it) "" else it },
                        accentColor = TextSecondary
                    )

                    // Custom scene notes
                    OutlinedTextField(
                        value = customSceneDetails,
                        onValueChange = { customSceneDetails = it },
                        placeholder = { Text("Détails d'arrière-plan supplémentaires (ex: débris volants, coucher de soleil rouge...)") },
                        label = { Text("Précisions supplémentaires sur le décor", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QiGold,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_scene_input_field")
                    )

                    // -------------------------------------------------------------
                    // SECTION 4: CAMERA FRAMING
                    // -------------------------------------------------------------
                    SectionHeader(
                        icon = Icons.Default.Videocam,
                        title = "4. CADRAGE & PERSPECTIVE CAMÉRA",
                        badge = activeCamera.displayName
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CameraPerspective.values().forEach { cam ->
                            val isSelected = cam == activeCamera
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeCamera = cam
                                    onCameraSelected(cam)
                                },
                                label = {
                                    Text(
                                        text = cam.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ManhuaCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = MangaPaperWhite,
                                    containerColor = InkMidnight,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = InkBorder,
                                    selectedBorderColor = ManhuaCyan
                                ),
                                modifier = Modifier.testTag("camera_chip_${cam.id}")
                            )
                        }
                    }

                    // Reset selection button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                selectedExpression = ""
                                selectedPoseAction = ""
                                selectedClothing = ""
                                selectedEffect = ""
                                customActionInput = ""
                                selectedEnvironmentPreset = ""
                                selectedLighting = ""
                                selectedWeather = ""
                                customSceneDetails = ""
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Réinitialiser les options", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Live Prompt Assembled Preview Box
            Surface(
                color = InkMidnight,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (assembledPrompt.isNotBlank()) ManhuaCyan.copy(alpha = 0.6f) else InkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PROMPT CONSTRUIT RÉSULTANT :",
                            color = ManhuaCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${assembledPrompt.length} car.",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = assembledPrompt.ifBlank { "Sélectionnez des options ci-dessus pour assembler un prompt..." },
                        color = if (assembledPrompt.isNotBlank()) MangaPaperWhite else TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Action Buttons: Apply to Editor & Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(assembledPrompt))
                    },
                    border = BorderStroke(1.dp, InkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MangaPaperWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_constructed_prompt_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onPromptConstructed(assembledPrompt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("apply_constructed_prompt_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Appliquer à la case en cours",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Section title row with icon and status badge
 */
@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = ManhuaCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = MangaPaperWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            color = InkMidnight,
            border = BorderStroke(1.dp, InkBorder),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = badge,
                color = ManhuaCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Category chips group for trait selection
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TraitCategoryChips(
    categoryName: String,
    presets: List<String>,
    selectedTrait: String,
    onSelectTrait: (String) -> Unit,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = categoryName,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { trait ->
                val isSelected = trait == selectedTrait
                Surface(
                    color = if (isSelected) accentColor.copy(alpha = 0.2f) else InkMidnight,
                    border = BorderStroke(1.dp, if (isSelected) accentColor else InkBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clickable { onSelectTrait(trait) }
                        .testTag("trait_chip_${trait.take(10)}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = trait,
                            color = if (isSelected) MangaPaperWhite else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
