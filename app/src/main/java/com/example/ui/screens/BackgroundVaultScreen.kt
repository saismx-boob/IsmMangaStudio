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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Terrain
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
import com.example.data.model.BackgroundProfile
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
fun BackgroundVaultScreen(
    viewModel: MangaStudioViewModel,
    onBackgroundSelectedForStudio: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgrounds by viewModel.allBackgrounds.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    var showEditorDialog by remember { mutableStateOf(false) }
    var backgroundToEdit by remember { mutableStateOf<BackgroundProfile?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    backgroundToEdit = null
                    showEditorDialog = true
                },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_background")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un Décor")
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
                    text = "BIBLE DES DÉCORS & ENVIRONNEMENTS",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cohérence spatiale & architecturale",
                    color = MangaPaperWhite,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Les décors créés ici conservent leur esthétique, leur météo et leur éclairage d'une planche à l'autre.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }

            items(backgrounds) { background ->
                val isActive = background.id == editorState.selectedBackgroundId
                BackgroundCard(
                    background = background,
                    isActiveInStudio = isActive,
                    onSelect = {
                        viewModel.selectBackground(background.id)
                        onBackgroundSelectedForStudio(background.id)
                    },
                    onEdit = {
                        backgroundToEdit = background
                        showEditorDialog = true
                    },
                    onDelete = {
                        viewModel.deleteBackground(background)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showEditorDialog) {
        BackgroundEditDialog(
            existing = backgroundToEdit,
            onSave = { updated ->
                viewModel.saveBackground(updated)
                showEditorDialog = false
            },
            onDismiss = { showEditorDialog = false }
        )
    }
}

@Composable
fun BackgroundCard(
    background: BackgroundProfile,
    isActiveInStudio: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (isActiveInStudio) Color(0xFF10B981) else InkBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("background_card_${background.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(InkSurfaceVariant)
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (background.referenceImagePath != null && File(background.referenceImagePath).exists()) {
                        AsyncImage(
                            model = File(background.referenceImagePath),
                            contentDescription = background.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = background.name,
                        color = MangaPaperWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = background.category,
                        color = Color(0xFF34D399),
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161A28), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Éclairage & Ambiance: ${background.lightingMood}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )
                Text(
                    text = "Détails architecturaux: ${background.architectureDetails}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActiveInStudio) InkSurfaceVariant else Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("use_bg_${background.id}_btn")
            ) {
                if (isActiveInStudio) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Décor actif dans le Studio", color = MangaPaperWhite, fontSize = 12.sp)
                } else {
                    Text("Utiliser ce décor pour le Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BackgroundEditDialog(
    existing: BackgroundProfile?,
    onSave: (BackgroundProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "Urbain / Ville Manga") }
    var lighting by remember { mutableStateOf(existing?.lightingMood ?: "Nuit sous néons et pluie") }
    var architecture by remember { mutableStateOf(existing?.architectureDetails ?: "Bâtiments néo-japonais, enseignes lumineuses") }
    var refImagePath by remember { mutableStateOf(existing?.referenceImagePath) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "bg_ref_${System.currentTimeMillis()}.jpg")
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
                text = if (existing == null) "Nouveau Décor Récurrent" else "Modifier le Décor",
                color = MangaPaperWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du lieu (ex: Temple de Jade, Ruelle Cyber)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("bg_name_input")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie (Extérieur, Intérieur, Fantaisie)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lighting,
                    onValueChange = { lighting = it },
                    label = { Text("Ambiance & Éclairage (ex: Crépuscule doré, Brume mystique)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = architecture,
                    onValueChange = { architecture = it },
                    label = { Text("Détails architecturaux et environnement") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF34D399)),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (refImagePath != null) "Changer l'image de référence" else "Associer une image de décor")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val promptAnchor = "Manga scenery $name, $category, atmospheric $lighting, detailed architecture: $architecture."
                        val profile = (existing ?: BackgroundProfile(name = name)).copy(
                            name = name,
                            category = category,
                            lightingMood = lighting,
                            architectureDetails = architecture,
                            referenceImagePath = refImagePath,
                            promptAnchor = promptAnchor
                        )
                        onSave(profile)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.testTag("save_bg_dialog_btn")
            ) {
                Text("Enregistrer le Décor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        }
    )
}
