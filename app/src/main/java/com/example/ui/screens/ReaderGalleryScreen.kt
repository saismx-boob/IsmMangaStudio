package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import com.example.ai.MangaArtStyle
import com.example.data.model.MangaProject
import com.example.ui.MangaStudioViewModel
import com.example.ui.components.MangaSpeechBubble
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

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.MenuBook
import com.example.ui.components.GeneratedMangaPanelsGallery
import com.example.ui.components.MangaProjectGallery

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReaderGalleryScreen(
    viewModel: MangaStudioViewModel,
    onNavigateBackToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.allProjects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val pages by viewModel.currentPages.collectAsState()
    val panels by viewModel.currentPanels.collectAsState()
    val projectCharacters by viewModel.projectCharacters.collectAsState()
    val allGeneratedPanels by viewModel.allGeneratedPanels.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var currentViewMode by remember { mutableStateOf("GALLERY") } // "GALLERY", "PANELS", or "READER"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = MangaCrimson,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_project")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouveau Projet")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LECTEUR & BIBLIOTHÈQUE",
                            color = MangaCrimson,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = selectedProject?.title ?: "Mes Mangas & Manhuas",
                            color = MangaPaperWhite,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, selectedProject?.title ?: "Manga Studio")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Découvrez mon manga créé avec Manga Studio AI : ${selectedProject?.title} (${selectedProject?.synopsis})"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager mon Manga"))
                        },
                        modifier = Modifier.testTag("share_manga_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Partager", tint = MangaCrimson)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mode Selector Bar: Galerie des Projets vs Cases Coil vs Lecteur Webtoon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(InkSurface)
                        .border(1.dp, InkBorder, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = if (currentViewMode == "GALLERY") MangaCrimson else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentViewMode = "GALLERY" }
                            .testTag("tab_gallery_mode")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = if (currentViewMode == "GALLERY") Color.White else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Projets (${projects.size})",
                                color = if (currentViewMode == "GALLERY") Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = if (currentViewMode == "PANELS") MangaCrimson else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentViewMode = "PANELS" }
                            .testTag("tab_panels_gallery_mode")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (currentViewMode == "PANELS") Color.White else ManhuaCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cases Coil (${allGeneratedPanels.size})",
                                color = if (currentViewMode == "PANELS") Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = if (currentViewMode == "READER") MangaCrimson else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentViewMode = "READER" }
                            .testTag("tab_reader_mode")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = if (currentViewMode == "READER") Color.White else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Webtoon",
                                color = if (currentViewMode == "READER") Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Display Generated Manga Panels Gallery (Coil based with local save, delete, export)
            if (currentViewMode == "PANELS") {
                item {
                    GeneratedMangaPanelsGallery(
                        viewModel = viewModel,
                        onOpenInStudio = { panel ->
                            viewModel.selectPanelForEditing(panel.panelIndex)
                            onNavigateBackToStudio()
                        }
                    )
                }
            }

            // Display Project Gallery
            if (currentViewMode == "GALLERY") {
                item {
                    MangaProjectGallery(
                        viewModel = viewModel,
                        projects = projects,
                        selectedProject = selectedProject,
                        onSelectProject = { project ->
                            viewModel.selectProject(project)
                        },
                        onOpenProjectInStudio = { project ->
                            viewModel.selectProject(project)
                            onNavigateBackToStudio()
                        },
                        onOpenProjectInReader = { project ->
                            viewModel.selectProject(project)
                            currentViewMode = "READER"
                        },
                        onCreateNewProject = { showNewProjectDialog = true },
                        onDeleteProject = { project ->
                            viewModel.deleteProject(project)
                        }
                    )
                }
            }

            // Reader Display: Sequential Webtoon / Manga Page Flow
            if (currentViewMode == "READER") {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PLANCHE EN COURS (DÉFILEMENT WEBTOON)",
                            color = ManhuaCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onNavigateBackToStudio) {
                            Text("Modifier dans le Studio", color = MangaCrimson, fontSize = 12.sp)
                        }
                    }
                }

                if (panels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(InkSurface)
                                .border(1.dp, InkBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Aucune case générée pour ce projet.",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                OutlinedButton(
                                    onClick = onNavigateBackToStudio,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MangaCrimson),
                                    border = BorderStroke(1.dp, MangaCrimson)
                                ) {
                                    Text("Créer des cases dans le Studio")
                                }
                            }
                        }
                    }
                }

                // Continuous Webtoon / Manga flow of panels
                items(panels) { panel ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .border(3.dp, Color.White, RoundedCornerShape(6.dp))
                ) {
                    if (panel.imagePath != null && File(panel.imagePath).exists()) {
                        AsyncImage(
                            model = File(panel.imagePath),
                            contentDescription = "Case",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.15f)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.15f)
                                .background(Color(0xFF141724)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = panel.userPrompt.ifBlank { "Case en cours de création" },
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Speech bubble in reader view
                    if (!panel.dialogueText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            MangaSpeechBubble(text = panel.dialogueText, type = panel.bubbleType)
                        }
                    }
                }
            }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showNewProjectDialog) {
        NewMangaProjectDialog(
            onCreate = { title, synopsis, style, colorMode ->
                viewModel.createProject(title, synopsis, style, colorMode)
                showNewProjectDialog = false
            },
            onDismiss = { showNewProjectDialog = false }
        )
    }
}

@Composable
fun NewMangaProjectDialog(
    onCreate: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(MangaArtStyle.MANGA_SHONEN.id) }
    var selectedColor by remember { mutableStateOf("BLACK_AND_WHITE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Text(
                text = "Créer une Nouvelle BD / Manga",
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'œuvre") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MangaCrimson,
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("new_project_title_input")
                )

                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Synopsis / Idée principale") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MangaCrimson,
                        unfocusedBorderColor = InkBorder,
                        focusedTextColor = MangaPaperWhite,
                        unfocusedTextColor = MangaPaperWhite
                    ),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Style graphique :",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        MangaArtStyle.MANGA_SHONEN,
                        MangaArtStyle.MANHUA_XIANXIA,
                        MangaArtStyle.WESTERN_COMIC
                    ).forEach { style ->
                        val isSel = selectedStyle == style.id
                        Surface(
                            color = if (isSel) MangaCrimson else InkSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSel) MangaCrimson else InkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStyle = style.id }
                        ) {
                            Text(
                                text = style.displayName.substringBefore(" "),
                                color = if (isSel) Color.White else MangaPaperWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, synopsis, selectedStyle, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                modifier = Modifier.testTag("confirm_create_project_btn")
            ) {
                Text("Créer le Projet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        }
    )
}
