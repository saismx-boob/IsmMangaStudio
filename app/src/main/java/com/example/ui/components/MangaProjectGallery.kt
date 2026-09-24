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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.ColorRenderingMode
import com.example.ai.MangaArtStyle
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.data.model.MangaProject
import com.example.ui.MangaStudioViewModel
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
import java.io.File

/**
 * MangaProjectGallery:
 * Rich, responsive manga gallery displaying saved manga projects from Room database
 * with real page thumbnails, visual panel previews, characters metadata, and action affordances.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MangaProjectGallery(
    viewModel: MangaStudioViewModel,
    projects: List<MangaProject>,
    selectedProject: MangaProject?,
    onSelectProject: (MangaProject) -> Unit,
    onOpenProjectInStudio: (MangaProject) -> Unit,
    onOpenProjectInReader: (MangaProject) -> Unit,
    onCreateNewProject: () -> Unit,
    onDeleteProject: (MangaProject) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterStyleId by remember { mutableStateOf<String?>(null) }
    var projectToDelete by remember { mutableStateOf<MangaProject?>(null) }

    val filteredProjects = remember(projects, filterStyleId) {
        if (filterStyleId == null) projects
        else projects.filter { it.artStyle == filterStyleId }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("manga_project_gallery")
    ) {
        // Gallery Header & Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        tint = MangaCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BIBLIOTHÈQUE DE PROJETS MANGA",
                        color = MangaCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${projects.size} œuvre(s) enregistrée(s)",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onCreateNewProject,
                colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("gallery_create_project_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nouveau Projet",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Art Style Quick Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = filterStyleId == null,
                onClick = { filterStyleId = null },
                label = { Text("Tous (${projects.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MangaCrimson.copy(alpha = 0.2f),
                    selectedLabelColor = MangaCrimson,
                    containerColor = InkSurface,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (filterStyleId == null) MangaCrimson else InkBorder,
                    enabled = true,
                    selected = filterStyleId == null
                )
            )

            MangaArtStyle.values().forEach { style ->
                val count = projects.count { it.artStyle == style.id }
                if (count > 0 || filterStyleId == style.id) {
                    val isSelected = filterStyleId == style.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            filterStyleId = if (isSelected) null else style.id
                        },
                        label = {
                            Text(
                                text = "${style.displayName.substringBefore(" ")} ($count)",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ManhuaCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ManhuaCyan,
                            containerColor = InkSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) ManhuaCyan else InkBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Projects Grid / Cards
        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InkSurface)
                    .border(1.dp, InkBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Aucun projet trouvé pour ce filtre",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = { filterStyleId = null },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ManhuaCyan),
                        border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.5f))
                    ) {
                        Text("Afficher tous les projets", fontSize = 12.sp)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                filteredProjects.forEach { project ->
                    val isSelected = project.id == selectedProject?.id
                    MangaProjectCard(
                        project = project,
                        isSelected = isSelected,
                        viewModel = viewModel,
                        onSelect = { onSelectProject(project) },
                        onOpenStudio = {
                            onSelectProject(project)
                            onOpenProjectInStudio(project)
                        },
                        onOpenReader = {
                            onSelectProject(project)
                            onOpenProjectInReader(project)
                        },
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (projectToDelete != null) {
        val proj = projectToDelete!!
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            containerColor = InkSurface,
            title = {
                Text(
                    text = "Supprimer ce projet ?",
                    color = MangaPaperWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Voulez-vous vraiment supprimer « ${proj.title} » ainsi que toutes ses planches et cases générées ? Cette action est irréversible.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(proj)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson)
                ) {
                    Text("Supprimer définitivement", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Annuler", color = TextSecondary)
                }
            }
        )
    }
}

/**
 * MangaProjectCard:
 * A presentation card for a single manga project displaying:
 * - Cover art / First panel preview
 * - Thumbnails row of all generated pages & panels
 * - Art style, color mode, line style badges
 * - Title and synopsis
 * - Actions: "Ouvrir dans le Studio", "Lire en Webtoon", "Supprimer"
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MangaProjectCard(
    project: MangaProject,
    isSelected: Boolean,
    viewModel: MangaStudioViewModel,
    onSelect: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenReader: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pagesWithPanels by remember { mutableStateOf<List<Pair<MangaPage, List<MangaPanel>>>>(emptyList()) }

    // Fetch pages and panels for thumbnail generation
    LaunchedEffect(project.id) {
        pagesWithPanels = viewModel.getPagesWithPanelsForProject(project.id)
    }

    val style = MangaArtStyle.fromId(project.artStyle)
    val colorMode = ColorRenderingMode.fromId(project.colorMode)

    // Find all panel thumbnails across pages that have generated images
    val allThumbnails = remember(pagesWithPanels) {
        pagesWithPanels.flatMap { it.second }.filter { !it.imagePath.isNullOrBlank() }
    }

    val coverThumbnail = remember(project.coverImagePath, allThumbnails) {
        project.coverImagePath ?: allThumbnails.firstOrNull()?.imagePath
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF191D2C) else InkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MangaCrimson else InkBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("gallery_project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Top Row: Cover Thumbnail + Info + Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Project Main Thumbnail / Cover
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 114.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F121C))
                        .border(
                            1.dp,
                            if (isSelected) MangaCrimson.copy(alpha = 0.6f) else InkBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .testTag("project_cover_${project.id}")
                ) {
                    if (!coverThumbnail.isNullOrBlank() && File(coverThumbnail).exists()) {
                        AsyncImage(
                            model = File(coverThumbnail),
                            contentDescription = "Couverture de ${project.title}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient bottom vignette
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    } else {
                        // Illustrated artistic placeholder for empty projects
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = if (isSelected) MangaCrimson else ManhuaCyan.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Brouillon",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Art style badge in top corner of the cover
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = style.displayName.substringBefore(" "),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (isSelected) {
                        Surface(
                            color = MangaCrimson,
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Actif",
                                tint = Color.White,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Metadata Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.title,
                            color = MangaPaperWhite,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Surface(
                                color = MangaCrimson.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, MangaCrimson)
                            ) {
                                Text(
                                    text = "EN COURS",
                                    color = MangaCrimson,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (project.synopsis.isNotBlank()) project.synopsis else "Aucun synopsis renseigné.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges: Color Mode, Line Style, Pages count
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = InkMidnight,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, InkBorder)
                        ) {
                            Text(
                                text = "${pagesWithPanels.size} planche(s)",
                                color = QiGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = InkMidnight,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, InkBorder)
                        ) {
                            Text(
                                text = colorMode.displayName.substringBefore(" "),
                                color = ManhuaCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = InkMidnight,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, InkBorder)
                        ) {
                            Text(
                                text = "${allThumbnails.size} image(s) générée(s)",
                                color = if (allThumbnails.isNotEmpty()) MangaPaperWhite else TextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Generated Page / Panel Thumbnails Reel
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F121A))
                    .border(0.5.dp, InkBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIGNETTES DES PLANCHES & CASES",
                        color = ManhuaCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${pagesWithPanels.size} p. • ${pagesWithPanels.sumOf { it.second.size }} cases",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (pagesWithPanels.isEmpty()) {
                    Text(
                        text = "Aucune planche créée pour le moment.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(pagesWithPanels) { (page, panelsList) ->
                            PageMiniatureThumbnail(
                                page = page,
                                panels = panelsList,
                                isProjectSelected = isSelected,
                                onSetAsCover = { imagePath ->
                                    viewModel.setProjectCoverImage(project, imagePath)
                                }
                            )
                        }
                    }
                }
            }

            // Action Buttons Bar: Studio, Webtoon Reader, Delete
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenStudio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MangaCrimson else InkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("open_studio_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ouvrir Studio",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenReader,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ManhuaCyan
                    ),
                    border = BorderStroke(1.dp, ManhuaCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("open_reader_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = ManhuaCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lire Webtoon",
                        color = ManhuaCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("delete_project_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer le projet",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * PageMiniatureThumbnail:
 * Displays an authentic mini layout sheet of a manga page showing the layout of panels,
 * rendered images or prompt outlines.
 */
@Composable
fun PageMiniatureThumbnail(
    page: MangaPage,
    panels: List<MangaPanel>,
    isProjectSelected: Boolean,
    onSetAsCover: (String) -> Unit
) {
    Surface(
        color = Color(0xFF141722),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, InkBorder),
        modifier = Modifier
            .width(100.dp)
            .testTag("page_thumb_${page.id}")
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            // Header: Page number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page ${page.pageNumber}",
                    color = MangaPaperWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${panels.size} c.",
                    color = TextMuted,
                    fontSize = 8.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Miniature manga sheet representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF090B10))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            ) {
                if (panels.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Vierge",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                } else {
                    // Render miniature panels layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        panels.take(3).forEach { panel ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF1B1F2D))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                            ) {
                                if (!panel.imagePath.isNullOrBlank() && File(panel.imagePath).exists()) {
                                    AsyncImage(
                                        model = File(panel.imagePath),
                                        contentDescription = "Miniature case",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = panel.userPrompt.take(15).ifBlank { "Case" },
                                        color = TextMuted,
                                        fontSize = 7.sp,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(1.dp)
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
