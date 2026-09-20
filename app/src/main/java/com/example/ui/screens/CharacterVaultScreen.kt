package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import java.io.File
import java.io.FileOutputStream

@Composable
fun CharacterVaultScreen(
    viewModel: MangaStudioViewModel,
    onCharacterSelectedForStudio: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val characters by viewModel.allCharacters.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    var showEditorDialog by remember { mutableStateOf(false) }
    var characterToEdit by remember { mutableStateOf<CharacterProfile?>(null) }

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
                Icon(Icons.Default.Add, contentDescription = "Créer un Héros")
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
                Text(
                    text = "BIBLE DES PERSONNAGES (ADN VISUEL)",
                    color = MangaCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestion de la cohérence visuelle",
                    color = MangaPaperWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Définissez ici l'anatomie, les cheveux, la tenue et les images de référence pour que l'IA conserve une ressemblance parfaite entre les cases.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }

            items(characters) { character ->
                val isActive = character.id == editorState.selectedCharacterId
                CharacterCard(
                    character = character,
                    isActiveInStudio = isActive,
                    onSelect = {
                        viewModel.selectCharacter(character.id)
                        onCharacterSelectedForStudio(character.id)
                    },
                    onEdit = {
                        characterToEdit = character
                        showEditorDialog = true
                    },
                    onDelete = {
                        viewModel.deleteCharacter(character)
                    },
                    onRegenerateUid = {
                        viewModel.regenerateCharacterUid(character.id)
                    },
                    onClearFirstAppearance = {
                        viewModel.clearFirstAppearanceAnchor(character.id)
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
}

@Composable
fun CharacterCard(
    character: CharacterProfile,
    isActiveInStudio: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRegenerateUid: () -> Unit,
    onClearFirstAppearance: () -> Unit,
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

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = TextSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444))
                }
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActiveInStudio) InkSurfaceVariant else MangaCrimson
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("use_character_${character.id}_btn")
            ) {
                if (isActiveInStudio) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MangaCrimson)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actif pour les prochaines cases", color = MangaPaperWhite, fontSize = 12.sp)
                } else {
                    Text("Verrouiller ce personnage pour le Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    var hair by remember { mutableStateOf(existing?.hairStyleColor ?: "Cheveux noirs hérissés avec mèches") }
    var eyes by remember { mutableStateOf(existing?.eyeDescription ?: "Yeux dorés perçants") }
    var clothing by remember { mutableStateOf(existing?.clothingDescription ?: "Veste noire et bandages") }
    var distinctive by remember { mutableStateOf(existing?.distinctiveFeatures ?: "") }
    var visualUid by remember { mutableStateOf(existing?.visualUid ?: com.example.data.model.generateCharacterUid(name)) }
    var refImagePath by remember { mutableStateOf(existing?.referenceImagePath) }

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
                refImagePath = targetFile.absolutePath
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
                }

                item {
                    // Reference image attachment button
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
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (refImagePath != null) "Changer l'image de référence" else "Associer une image de référence"
                        )
                    }

                    val currentRef = refImagePath
                    if (currentRef != null && File(currentRef).exists()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = File(currentRef),
                                contentDescription = "Aperçu de l'image de référence",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Image de référence active",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { refImagePath = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Supprimer la référence",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
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
                    if (name.isNotBlank()) {
                        val promptAnchor = "Anime character $name, $role, $hair, $eyes, wearing $clothing. $distinctive."
                        val profile = (existing ?: CharacterProfile(name = name)).copy(
                            name = name,
                            role = role,
                            hairStyleColor = hair,
                            eyeDescription = eyes,
                            clothingDescription = clothing,
                            distinctiveFeatures = distinctive,
                            visualUid = visualUid.ifBlank { com.example.data.model.generateCharacterUid(name) },
                            referenceImagePath = refImagePath,
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
