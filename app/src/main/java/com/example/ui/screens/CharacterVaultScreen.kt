package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ai.CameraPerspective
import com.example.ai.MangaArtStyle
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.ui.MangaStudioViewModel
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextSecondary
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

@Composable
fun CharacterVaultScreen(
    viewModel: MangaStudioViewModel,
    onCharacterSelectedForStudio: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val characters by viewModel.allCharacters.collectAsState()
    val backgrounds by viewModel.allBackgrounds.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("Tous") }
    var showEditorDialog by remember { mutableStateOf(false) }
    var characterToEdit by remember { mutableStateOf<CharacterProfile?>(null) }

    // Dialog & Flow States for Reference Upload, AI Reference Generation, and Scene Generation
    var characterForUpload by remember { mutableStateOf<CharacterProfile?>(null) }
    var characterForAiRefGen by remember { mutableStateOf<CharacterProfile?>(null) }
    var characterForSceneGen by remember { mutableStateOf<CharacterProfile?>(null) }
    var inspectingImage by remember { mutableStateOf<Pair<CharacterProfile, String>?>(null) }

    // Google Play compliant zero-permission Photo Picker for uploading reference images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val targetChar = characterForUpload
        if (uri != null && targetChar != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "char_ref_${targetChar.id}_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.addReferenceImageToCharacter(targetChar.id, targetFile.absolutePath)
                Toast.makeText(context, "Image de référence ajoutée pour ${targetChar.name} !", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erreur lors de l'import : ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        characterForUpload = null
    }

    val filterOptions = listOf("Tous", "Shonen", "Manhua", "Cyber/Comics", "Seinen")

    val filteredCharacters = remember(characters, searchQuery, selectedRoleFilter) {
        characters.filter { char ->
            val matchesQuery = searchQuery.isBlank() ||
                char.name.contains(searchQuery, ignoreCase = true) ||
                char.role.contains(searchQuery, ignoreCase = true) ||
                char.preferredArtStyle.contains(searchQuery, ignoreCase = true) ||
                char.hairStyleColor.contains(searchQuery, ignoreCase = true) ||
                char.clothingDescription.contains(searchQuery, ignoreCase = true) ||
                char.distinctiveFeatures.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedRoleFilter) {
                "Shonen" -> char.role.contains("Shonen", ignoreCase = true) || char.preferredArtStyle.contains("SHONEN", ignoreCase = true)
                "Manhua" -> char.role.contains("Manhua", ignoreCase = true) || char.role.contains("Céleste", ignoreCase = true) || char.preferredArtStyle.contains("MANHUA", ignoreCase = true)
                "Cyber/Comics" -> char.role.contains("Cyber", ignoreCase = true) || char.role.contains("Comics", ignoreCase = true) || char.preferredArtStyle.contains("COMIC", ignoreCase = true)
                "Seinen" -> char.role.contains("Seinen", ignoreCase = true) || char.preferredArtStyle.contains("SEINEN", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    characterToEdit = null
                    showEditorDialog = true
                },
                containerColor = MangaCrimson,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_character")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un Modèle de Personnage")
            }
        },
        containerColor = InkMidnight,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MangaCrimson,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "CHARACTER LIBRARY",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = QiGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, QiGold.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "COHÉRENCE & IDENTIFIANTS UNIQUE (#UID)",
                            color = QiGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bibliothèque de Personnages",
                    color = MangaPaperWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Uploadez ou générez des images de référence IA pour chaque héros. Les identifiants uniques (#UID) et l'ADN visuel garantissent une ressemblance parfaite lors de la génération de scènes dans le Studio.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher un modèle (nom, visage, tenue...)", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer la recherche", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InkSurface,
                        unfocusedContainerColor = InkSurface,
                        focusedBorderColor = MangaCrimson,
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("character_search_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Archetype / Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { option ->
                        val isSelected = selectedRoleFilter == option
                        Surface(
                            color = if (isSelected) MangaCrimson.copy(alpha = 0.25f) else InkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) MangaCrimson else InkBorder),
                            modifier = Modifier
                                .clickable { selectedRoleFilter = option }
                                .testTag("filter_chip_$option")
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else MangaPaperWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (filteredCharacters.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = InkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, InkBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = QiGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Aucun modèle ne correspond à la recherche" else "Aucun modèle dans cette catégorie",
                                color = MangaPaperWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Créez un nouveau modèle de personnage réutilisable avec le bouton ci-dessous.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(filteredCharacters) { character ->
                val isActive = character.id == editorState.selectedCharacterId
                CharacterCard(
                    character = character,
                    isActiveInStudio = isActive,
                    viewModel = viewModel,
                    onSelect = {
                        viewModel.selectCharacter(character.id)
                        onCharacterSelectedForStudio(character.id)
                    },
                    onEdit = {
                        characterToEdit = character
                        showEditorDialog = true
                    },
                    onDuplicate = {
                        val copy = character.copy(
                            id = 0,
                            name = "${character.name} (Variante)",
                            visualUid = com.example.data.model.generateCharacterUid(character.name),
                            appearanceCount = 0
                        )
                        viewModel.saveCharacter(copy)
                    },
                    onDelete = {
                        viewModel.deleteCharacter(character)
                    },
                    onRegenerateUid = {
                        viewModel.regenerateCharacterUid(character.id)
                    },
                    onClearFirstAppearance = {
                        viewModel.clearFirstAppearanceAnchor(character.id)
                    },
                    onUploadRef = {
                        characterForUpload = character
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onGenerateAiRef = {
                        characterForAiRefGen = character
                    },
                    onGenerateScene = {
                        characterForSceneGen = character
                    },
                    onInspectImage = { path ->
                        inspectingImage = Pair(character, path)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showEditorDialog) {
        CharacterEditDialog(
            existing = characterToEdit,
            onSave = { updated ->
                viewModel.saveCharacter(updated)
                showEditorDialog = false
            },
            onDismiss = { showEditorDialog = false }
        )
    }

    // Dialog for AI Reference Generation (portrait, turnaround, action pose, expressions)
    characterForAiRefGen?.let { char ->
        AiReferenceGeneratorDialog(
            character = char,
            viewModel = viewModel,
            onDismiss = { characterForAiRefGen = null }
        )
    }

    // Dialog for Scene Generation linking character #UID and visual traits
    characterForSceneGen?.let { char ->
        CharacterSceneGenerationDialog(
            character = char,
            viewModel = viewModel,
            backgrounds = backgrounds,
            onDismiss = { characterForSceneGen = null },
            onLaunchInStudio = {
                characterForSceneGen = null
                onCharacterSelectedForStudio(char.id)
            }
        )
    }

    // Dialog for High-Res Inspection of Reference Images
    inspectingImage?.let { (char, path) ->
        ImageInspectionDialog(
            character = char,
            imagePath = path,
            viewModel = viewModel,
            onDismiss = { inspectingImage = null }
        )
    }
}

@Composable
fun CharacterCard(
    character: CharacterProfile,
    isActiveInStudio: Boolean,
    viewModel: MangaStudioViewModel,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit = {},
    onDelete: () -> Unit,
    onRegenerateUid: () -> Unit,
    onClearFirstAppearance: () -> Unit,
    onUploadRef: () -> Unit,
    onGenerateAiRef: () -> Unit,
    onGenerateScene: () -> Unit,
    onInspectImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (isActiveInStudio) MangaCrimson else InkBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("character_card_${character.id}")
    ) {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current

        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Character Avatar / Ref Image or Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(InkSurfaceVariant)
                        .border(2.dp, if (isActiveInStudio) MangaCrimson else ManhuaCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarPath = character.firstAppearanceImagePath ?: character.referenceImagePath
                    if (avatarPath != null && File(avatarPath).exists()) {
                        AsyncImage(
                            model = File(avatarPath),
                            contentDescription = character.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MangaCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.name,
                            color = MangaPaperWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActiveInStudio) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MangaCrimson,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIF DANS LE STUDIO",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = character.role,
                        color = QiGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.testTag("duplicate_character_${character.id}")
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Dupliquer comme nouveau modèle",
                        tint = QiGold
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = TextSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action: Aperçu Prompt Structuré Gemini
            var showPromptDialog by remember { mutableStateOf(false) }
            val structuredPrompt = remember(character) {
                viewModel.buildStructuredCharacterPrompt(character)
            }

            OutlinedButton(
                onClick = { showPromptDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = QiGold),
                border = BorderStroke(1.dp, QiGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_gemini_prompt_${character.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = QiGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aperçu Prompt Structuré Gemini (Nom + Description + Tags)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (showPromptDialog) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                AlertDialog(
                    onDismissRequest = { showPromptDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = QiGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prompt Structuré Gemini : ${character.name}", color = MangaPaperWhite, fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "Ce prompt concatène l'identité, les descriptions physiques et les tags de style pour garantir la cohérence visuelle dans l'API Gemini :",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Surface(
                                color = InkMidnight,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, InkBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                LazyColumn(modifier = Modifier.padding(10.dp)) {
                                    item {
                                        Text(
                                            text = structuredPrompt,
                                            color = QiGold,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(structuredPrompt))
                                showPromptDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson)
                        ) {
                            Text("Copier le Prompt", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPromptDialog = false }) {
                            Text("Fermer", color = TextSecondary)
                        }
                    },
                    containerColor = InkSurface,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Consistency Identifier Bar
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = QiGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IDENTIFIANT ADN COHÉRENCE",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = character.visualUid,
                            color = QiGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${character.appearanceCount} case(s)",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("#${character.visualUid}"))
                            Toast.makeText(context, "UID #${character.visualUid} copié !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_uid_${character.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copier UID",
                            tint = QiGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onRegenerateUid,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Régénérer UID",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canonical First Appearance Anchor Status
            Surface(
                color = if (character.firstAppearanceImagePath != null) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFF451A03).copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (character.firstAppearanceImagePath != null) Color(0xFF059669) else Color(0xFFD97706)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (character.firstAppearanceImagePath != null && File(character.firstAppearanceImagePath).exists()) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                        ) {
                            AsyncImage(
                                model = File(character.firstAppearanceImagePath),
                                contentDescription = "Ancre 1ère apparition",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⭐ Ancre 1ère Apparition Verrouillée",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cette case guide les traits, yeux et tenue pour toutes les futures générations.",
                                color = Color(0xFFD1FAE5),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                        IconButton(
                            onClick = onClearFirstAppearance,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Réinitialiser l'ancre",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⏳ 1ère Apparition en attente",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "La première case générée dans le Studio capturera l'apparence de référence.",
                                color = Color(0xFFFEF3C7),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DNA Attributes tags
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161A28), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, InkBorder)
                    ) {
                        Text(
                            text = "Âge: ${character.ageCategory}",
                            color = QiGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, InkBorder)
                    ) {
                        Text(
                            text = character.personalityMood,
                            color = ManhuaCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, MangaCrimson.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = when (character.preferredArtStyle) {
                                "MANGA_SHONEN" -> "Shonen"
                                "MANHUA_QI" -> "Manhua Qi"
                                "SEINEN_DARK" -> "Seinen"
                                "CYBER_COMIC" -> "Cyber Comic"
                                "SHOJO_ETHEREAL" -> "Shojo"
                                else -> character.preferredArtStyle
                            },
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Expression: ${character.defaultExpression}",
                    color = Color(0xFFF1F5F9),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Cheveux: ${character.hairStyleColor}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )
                Text(
                    text = "Yeux: ${character.eyeDescription}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )
                Text(
                    text = "Tenue: ${character.clothingDescription}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )
                if (character.distinctiveFeatures.isNotBlank()) {
                    Text(
                        text = "Signe distinctif: ${character.distinctiveFeatures}",
                        color = ManhuaCyan,
                        fontSize = 12.sp
                    )
                }

                val allRefImages = character.getReferenceImagesList()
                if (allRefImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Galerie Références (${allRefImages.size}) :",
                        color = QiGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allRefImages.forEachIndexed { idx, path ->
                            val file = File(path)
                            val isPrimary = idx == 0
                            val isAnchor = path == character.firstAppearanceImagePath
                            if (file.exists()) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (isPrimary || isAnchor) 2.dp else 1.dp,
                                            color = if (isAnchor) Color(0xFF10B981) else if (isPrimary) QiGold else ManhuaCyan.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onInspectImage(path) }
                                ) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Réf #${idx + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isPrimary) {
                                        Surface(
                                            color = QiGold,
                                            shape = RoundedCornerShape(bottomEnd = 4.dp),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "⭐",
                                                color = Color.Black,
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    } else if (isAnchor) {
                                        Surface(
                                            color = Color(0xFF10B981),
                                            shape = RoundedCornerShape(bottomEnd = 4.dp),
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "⚓",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aucune image de référence attachée. Uploadez ou générez un concept IA ci-dessous.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions: Upload Reference and Generate AI Reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onUploadRef,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_ref_btn_${character.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ManhuaCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uploader Réf", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onGenerateAiRef,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_ai_ref_btn_${character.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = QiGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Générer Réf IA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Scene Generation Button with #UID
            Button(
                onClick = onGenerateScene,
                colors = ButtonDefaults.buttonColors(containerColor = QiGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_scene_btn_${character.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Générer une Scène avec ce Héros (#${character.visualUid})",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onSelect,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isActiveInStudio) MangaCrimson else MangaPaperWhite
                ),
                border = BorderStroke(1.dp, if (isActiveInStudio) MangaCrimson else InkBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("use_character_${character.id}_btn")
            ) {
                if (isActiveInStudio) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MangaCrimson, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actif dans le Studio", color = MangaPaperWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Sélectionner pour le Studio", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CharacterEditDialog(
    existing: CharacterProfile?,
    onSave: (CharacterProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var role by remember { mutableStateOf(existing?.role ?: "Protagoniste Shonen") }
    var ageCategory by remember { mutableStateOf(existing?.ageCategory ?: "Adolescent (16-18 ans)") }
    var personalityMood by remember { mutableStateOf(existing?.personalityMood ?: "Déterminé & Calme") }
    var defaultExpression by remember { mutableStateOf(existing?.defaultExpression ?: "Regard intense") }
    var preferredArtStyle by remember { mutableStateOf(existing?.preferredArtStyle ?: "MANGA_SHONEN") }
    var hair by remember { mutableStateOf(existing?.hairStyleColor ?: "Cheveux noirs hérissés avec mèches") }
    var eyes by remember { mutableStateOf(existing?.eyeDescription ?: "Yeux dorés perçants") }
    var clothing by remember { mutableStateOf(existing?.clothingDescription ?: "Veste noire et bandages") }
    var distinctive by remember { mutableStateOf(existing?.distinctiveFeatures ?: "") }
    var visualUid by remember { mutableStateOf(existing?.visualUid ?: com.example.data.model.generateCharacterUid(name)) }
    
    // Multi-reference images management
    var referenceImages by remember {
        mutableStateOf<List<String>>(existing?.getReferenceImagesList() ?: emptyList())
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "char_ref_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val path = targetFile.absolutePath
                if (!referenceImages.contains(path)) {
                    referenceImages = referenceImages + path
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Text(
                text = if (existing == null) "Créer un Héros (ADN Cohérence)" else "Modifier la Fiche Personnage",
                color = MangaPaperWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom du personnage") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("char_name_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = visualUid,
                            onValueChange = { visualUid = it },
                            label = { Text("Identifiant Unique Visuel (UID)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = QiGold,
                                unfocusedBorderColor = InkBorder,
                                focusedTextColor = QiGold,
                                unfocusedTextColor = QiGold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                visualUid = com.example.data.model.generateCharacterUid(name.ifBlank { "HERO" })
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Régénérer UID", tint = QiGold)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Rôle / Archétype (ex: Cultivateur Manhua, Héros Shonen)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // New fields: Tranche d'Âge, Personnalité, Expression faciale type
                item {
                    Text("Tranche d'Âge / Maturité :", color = QiGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Enfant (8-12 ans)",
                            "Adolescent (15-18 ans)",
                            "Jeune adulte (20-25 ans)",
                            "Adulte vétéran (30-40 ans)",
                            "Maître ancien (60+ ans)"
                        ).forEach { preset ->
                            val isSelected = ageCategory == preset
                            Surface(
                                color = if (isSelected) QiGold.copy(alpha = 0.2f) else InkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSelected) QiGold else InkBorder),
                                modifier = Modifier.clickable { ageCategory = preset }
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) QiGold else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = personalityMood,
                        onValueChange = { personalityMood = it },
                        label = { Text("Tempérament / Personnalité (ex: Froid & Stratège, Énergique)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Déterminé & Calme",
                            "Énergique & Rebelle",
                            "Froid & Mystérieux",
                            "Noble & Bienveillant",
                            "Sombre & Vengeur"
                        ).forEach { preset ->
                            Surface(
                                color = InkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, InkBorder),
                                modifier = Modifier.clickable { personalityMood = preset }
                            ) {
                                Text(
                                    text = "+ $preset",
                                    color = ManhuaCyan,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = defaultExpression,
                        onValueChange = { defaultExpression = it },
                        label = { Text("Expression Faciale Type (ex: Regard intense, Sourire narquois)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Style Artistique Associé (Tag) :", color = QiGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "MANGA_SHONEN" to "Shonen Manga",
                            "MANHUA_QI" to "Manhua Céleste (Qi)",
                            "SEINEN_DARK" to "Seinen Sombre",
                            "CYBER_COMIC" to "Cyber / Western Comic",
                            "SHOJO_ETHEREAL" to "Shojo Éthéré"
                        ).forEach { (styleKey, styleLabel) ->
                            val isSelected = preferredArtStyle == styleKey
                            Surface(
                                color = if (isSelected) MangaCrimson.copy(alpha = 0.25f) else InkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSelected) MangaCrimson else InkBorder),
                                modifier = Modifier.clickable { preferredArtStyle = styleKey }
                            ) {
                                Text(
                                    text = styleLabel,
                                    color = if (isSelected) MangaPaperWhite else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = hair,
                        onValueChange = { hair = it },
                        label = { Text("Cheveux (Couleur, coupe, longueur)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = eyes,
                        onValueChange = { eyes = it },
                        label = { Text("Yeux (Couleur, forme, regard)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = clothing,
                        onValueChange = { clothing = it },
                        label = { Text("Tenue vestimentaire récurrente") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Outfit Preset Suggestions
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Exemples de tenues réutilisables :",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Veste noire et bandages",
                            "Kimono de combat blanc/rouge",
                            "Uniforme scolaire manga",
                            "Robe Hanfu en soie brodée",
                            "Armure tactique cybernétique"
                        ).forEach { outfitPreset ->
                            Surface(
                                color = InkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, InkBorder),
                                modifier = Modifier.clickable { clothing = outfitPreset }
                            ) {
                                Text(
                                    text = outfitPreset,
                                    color = QiGold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = distinctive,
                        onValueChange = { distinctive = it },
                        label = { Text("Signes distinctifs (Arme, cicatrice, aura)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Distinctive Features Suggestions
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Traits distinctifs fréquents :",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Katana spectrale à la ceinture",
                            "Cicatrice sur la joue gauche",
                            "Aura spirituelle dorée",
                            "Tatouage tribal sur le bras",
                            "Visière holographique lumineuse"
                        ).forEach { traitPreset ->
                            Surface(
                                color = InkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, InkBorder),
                                modifier = Modifier.clickable {
                                    distinctive = if (distinctive.isBlank()) traitPreset else "$distinctive, $traitPreset"
                                }
                            ) {
                                Text(
                                    text = "+ $traitPreset",
                                    color = ManhuaCyan,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Reference images section: multi-image introduction & management
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = ManhuaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Images de Référence (${referenceImages.size})",
                                    color = MangaPaperWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "Pour guidage visuel IA",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add new reference image button
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                            border = BorderStroke(1.dp, ManhuaCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter une image de référence (visage / tenue)")
                        }

                        // List of attached reference images
                        if (referenceImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                referenceImages.forEachIndexed { index, path ->
                                    val file = File(path)
                                    if (file.exists()) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, QiGold, RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = file,
                                                contentDescription = "Réf #${index + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            IconButton(
                                                onClick = {
                                                    referenceImages = referenceImages.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .align(Alignment.TopEnd)
                                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Supprimer",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val promptAnchor = "Anime character $name, $role, $ageCategory, temperament $personalityMood with $defaultExpression, $hair, $eyes, wearing $clothing. $distinctive."
                        val primaryRef = referenceImages.firstOrNull()
                        val secondaryRefs = if (referenceImages.size > 1) {
                            referenceImages.drop(1).joinToString("||")
                        } else {
                            ""
                        }
                        val profile = (existing ?: CharacterProfile(name = name)).copy(
                            name = name,
                            role = role,
                            ageCategory = ageCategory,
                            personalityMood = personalityMood,
                            defaultExpression = defaultExpression,
                            preferredArtStyle = preferredArtStyle,
                            hairStyleColor = hair,
                            eyeDescription = eyes,
                            clothingDescription = clothing,
                            distinctiveFeatures = distinctive,
                            visualUid = visualUid.ifBlank { com.example.data.model.generateCharacterUid(name) },
                            referenceImagePath = primaryRef,
                            secondaryReferenceImages = secondaryRefs,
                            promptAnchor = promptAnchor
                        )
                        onSave(profile)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                modifier = Modifier.testTag("save_char_dialog_btn")
            ) {
                Text("Enregistrer le Personnage")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AiReferenceGeneratorDialog(
    character: CharacterProfile,
    viewModel: MangaStudioViewModel,
    onDismiss: () -> Unit
) {
    val editorState by viewModel.editorState.collectAsState()
    var selectedSheetType by remember { mutableStateOf("PORTRAIT") }
    val isGenerating = editorState.isGenerating

    val sheetOptions = listOf(
        Triple("PORTRAIT", "Portrait Officiel (HD)", "Gros plan face & buste, haute précision des traits et du regard"),
        Triple("TURNAROUND", "Fiche Turnaround (3 Angles)", "Vues face, 3/4 et profil pour modèle 3D / dessinateur"),
        Triple("ACTION_POSE", "Pose de Combat & Silhouette", "Posture d'action dynamique, gestuelle et tenue en mouvement"),
        Triple("EXPRESSIONS", "Planche d'Expressions", "Variations émotionnelles (déterminé, sourire, choc, cri de combat)")
    )

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = QiGold)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Générer Réf IA : ${character.name}",
                        color = MangaPaperWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Identifiant ADN : #${character.visualUid}",
                        color = QiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Type de Fiche de Référence :",
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(sheetOptions) { (typeKey, title, desc) ->
                    val isSelected = selectedSheetType == typeKey
                    Surface(
                        color = if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.5f) else InkMidnight,
                        border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else InkBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isGenerating) { selectedSheetType = typeKey }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (!isGenerating) selectedSheetType = typeKey },
                                    colors = RadioButtonDefaults.colors(selectedColor = ManhuaCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else MangaPaperWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = desc,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ADN Visuel injecté dans la génération :",
                        color = QiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, InkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("• Rôle / Archétype : ${character.role}", color = TextSecondary, fontSize = 10.sp)
                            Text("• Cheveux : ${character.hairStyleColor}", color = TextSecondary, fontSize = 10.sp)
                            Text("• Yeux : ${character.eyeDescription}", color = TextSecondary, fontSize = 10.sp)
                            Text("• Tenue : ${character.clothingDescription}", color = TextSecondary, fontSize = 10.sp)
                            Text("• Style : ${character.preferredArtStyle}", color = TextSecondary, fontSize = 10.sp)
                            Text("• UID Cohérence : #${character.visualUid}", color = QiGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isGenerating) {
                    item {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ManhuaCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = ManhuaCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = editorState.generationStage.ifBlank { "Génération du modèle par Gemini AI..." },
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.generateCharacterReferenceSheet(character, selectedSheetType)
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = ManhuaCyan),
                modifier = Modifier.testTag("submit_generate_ai_ref")
            ) {
                if (isGenerating) {
                    Text("Génération en cours...", color = Color.Black)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lancer la Génération", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isGenerating
            ) {
                Text("Fermer", color = TextSecondary)
            }
        }
    )
}

@Composable
fun CharacterSceneGenerationDialog(
    character: CharacterProfile,
    viewModel: MangaStudioViewModel,
    backgrounds: List<BackgroundProfile>,
    onDismiss: () -> Unit,
    onLaunchInStudio: () -> Unit
) {
    val context = LocalContext.current
    val editorState by viewModel.editorState.collectAsState()
    val currentPanels by viewModel.currentPanels.collectAsState()

    var sceneAction by remember {
        mutableStateOf("En plein combat intense, déclenche sa technique ultime avec détermination")
    }
    var selectedCamera by remember { mutableStateOf(CameraPerspective.LOW_ANGLE) }
    var selectedLighting by remember { mutableStateOf("Ombres dramatiques et éclairs d'énergie") }
    var selectedBackgroundId by remember { mutableStateOf<Long?>(null) }
    var targetPanelIndex by remember { mutableIntStateOf(editorState.activePanelIndex) }

    val lightingPresets = listOf(
        "Ombres dramatiques et éclairs",
        "Plein jour & reflets d'action",
        "Néon cyberpunk sous la pluie",
        "Coucher de soleil écarlate"
    )

    // Build structured prompt for display and execution
    val structuredScenePrompt = remember(sceneAction, selectedCamera, selectedLighting, selectedBackgroundId) {
        val bgDesc = backgrounds.firstOrNull { it.id == selectedBackgroundId }?.let { "Background: ${it.name} (${it.architectureDetails}, ${it.lightingMood}). " } ?: ""
        "#[${character.visualUid}] Character ${character.name}, ${character.role}, ${character.hairStyleColor}, ${character.eyeDescription}, wearing ${character.clothingDescription}. Situation: $sceneAction. Camera: ${selectedCamera.promptModifier}. Lighting: $selectedLighting. ${bgDesc}Style: [${character.preferredArtStyle}]. High consistency with reference #${character.visualUid}."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = QiGold)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Scène avec : ${character.name}",
                        color = MangaPaperWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "UID Cohérence : #${character.visualUid}",
                        color = QiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Character summary chip
                item {
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarPath = character.firstAppearanceImagePath ?: character.referenceImagePath
                            if (avatarPath != null && File(avatarPath).exists()) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, QiGold, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = File(avatarPath),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column {
                                Text(
                                    text = "${character.name} • ${character.getReferenceImagesList().size} image(s) de référence liées",
                                    color = MangaPaperWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Les traits de visage et la tenue seront synchronisés par l'IA",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Situation / Action input
                item {
                    Text(
                        text = "Action ou Situation dans la Scène :",
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = sceneAction,
                        onValueChange = { sceneAction = it },
                        placeholder = { Text("Que fait le personnage dans cette case ?", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scene_action_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InkMidnight,
                            unfocusedContainerColor = InkMidnight,
                            focusedBorderColor = QiGold,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        maxLines = 3
                    )
                }

                // Camera Angle / Perspective Selection
                item {
                    Text(
                        text = "Angle de Caméra & Cadrage :",
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CameraPerspective.entries.forEach { camera ->
                            val isSelected = selectedCamera == camera
                            Surface(
                                color = if (isSelected) MangaCrimson else InkMidnight,
                                border = BorderStroke(1.dp, if (isSelected) MangaCrimson else InkBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { selectedCamera = camera }
                            ) {
                                Text(
                                    text = camera.displayName,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Lighting selection
                item {
                    Text(
                        text = "Ambiance & Éclairage :",
                        color = MangaPaperWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        lightingPresets.forEach { lighting ->
                            val isSelected = selectedLighting == lighting
                            Surface(
                                color = if (isSelected) Color(0xFF1E3A8A) else InkMidnight,
                                border = BorderStroke(1.dp, if (isSelected) ManhuaCyan else InkBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { selectedLighting = lighting }
                            ) {
                                Text(
                                    text = lighting,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Target Panel Selector
                if (currentPanels.isNotEmpty()) {
                    item {
                        Text(
                            text = "Générer dans la Case :",
                            color = MangaPaperWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentPanels.forEachIndexed { idx: Int, _ ->
                                val isSelected = targetPanelIndex == idx
                                Surface(
                                    color = if (isSelected) QiGold else InkMidnight,
                                    border = BorderStroke(1.dp, if (isSelected) QiGold else InkBorder),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { targetPanelIndex = idx }
                                ) {
                                    Text(
                                        text = "Case #${idx + 1}",
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Prompt preview
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aperçu du Prompt Structuré (IA) :",
                        color = QiGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = InkMidnight,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, InkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = structuredScenePrompt,
                            color = Color(0xFFCBD5E1),
                            fontSize = 9.sp,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.selectCharacter(character.id)
                    viewModel.updatePrompt(structuredScenePrompt)
                    viewModel.updateCamera(selectedCamera)
                    selectedBackgroundId?.let { viewModel.selectBackground(it) }
                    viewModel.selectPanelForEditing(targetPanelIndex)
                    Toast.makeText(context, "Scène configurée avec #${character.visualUid} !", Toast.LENGTH_SHORT).show()
                    onLaunchInStudio()
                },
                colors = ButtonDefaults.buttonColors(containerColor = QiGold),
                modifier = Modifier.testTag("confirm_generate_scene_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lancer dans le Studio", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        }
    )
}

@Composable
fun ImageInspectionDialog(
    character: CharacterProfile,
    imagePath: String,
    viewModel: MangaStudioViewModel,
    onDismiss: () -> Unit
) {
    val file = File(imagePath)
    val isPrimary = character.referenceImagePath == imagePath
    val isAnchor = character.firstAppearanceImagePath == imagePath

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = ManhuaCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Image de Référence • ${character.name}",
                    color = MangaPaperWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (file.exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, if (isAnchor) Color(0xFF10B981) else if (isPrimary) QiGold else InkBorder, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Inspection de référence",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(InkMidnight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Image introuvable localement", color = TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPrimary) {
                        Surface(
                            color = QiGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, QiGold)
                        ) {
                            Text(
                                text = "⭐ Référence Principale",
                                color = QiGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (isAnchor) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "⚓ Ancre 1ère Apparition",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Actions: Set Primary, Lock Anchor, Delete
                if (!isPrimary) {
                    OutlinedButton(
                        onClick = {
                            viewModel.setPrimaryReferenceImage(character.id, imagePath)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QiGold),
                        border = BorderStroke(1.dp, QiGold.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = QiGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Définir comme Réf. Principale", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (!isAnchor) {
                    OutlinedButton(
                        onClick = {
                            viewModel.lockFirstAppearanceAnchor(character.id, imagePath, null)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verrouiller comme Ancre 1ère Apparition", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        viewModel.removeReferenceImageFromCharacter(character.id, imagePath)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Supprimer de la Fiche", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = InkSurfaceVariant)
            ) {
                Text("Fermer", color = MangaPaperWhite)
            }
        }
    )
}
