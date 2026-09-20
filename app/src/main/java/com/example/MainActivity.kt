package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MangaStudioViewModel
import com.example.ui.screens.BackgroundVaultScreen
import com.example.ui.screens.CharacterVaultScreen
import com.example.ui.screens.ReaderGalleryScreen
import com.example.ui.screens.StoryboardScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.MangaStudioTheme
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: MangaStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MangaStudioTheme {
                MangaStudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MangaStudioApp(viewModel: MangaStudioViewModel) {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }

    val navItems = listOf(
        NavigationItem("Studio", Icons.Default.Palette, "nav_studio"),
        NavigationItem("Storyboard", Icons.Default.GridView, "nav_storyboard"),
        NavigationItem("Personnages", Icons.Default.Person, "nav_characters"),
        NavigationItem("Décors", Icons.Default.Terrain, "nav_backgrounds"),
        NavigationItem("Lecteur BD", Icons.Default.Book, "nav_reader")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = InkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MangaCrimson else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) MangaPaperWhite else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MangaCrimson.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        containerColor = InkMidnight,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(InkMidnight)
        ) {
            when (currentTab) {
                0 -> StudioScreen(
                    viewModel = viewModel,
                    onNavigateToCharacters = { currentTab = 2 },
                    onNavigateToBackgrounds = { currentTab = 3 },
                    onNavigateToReader = { currentTab = 4 },
                    onNavigateToStoryboard = { currentTab = 1 }
                )
                1 -> StoryboardScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentTab = 0 },
                    onGenerateFromStoryboard = { panelIndex ->
                        currentTab = 0
                        viewModel.generatePanel(panelIndex)
                    }
                )
                2 -> CharacterVaultScreen(
                    viewModel = viewModel,
                    onCharacterSelectedForStudio = { currentTab = 0 }
                )
                3 -> BackgroundVaultScreen(
                    viewModel = viewModel,
                    onBackgroundSelectedForStudio = { currentTab = 0 }
                )
                4 -> ReaderGalleryScreen(
                    viewModel = viewModel,
                    onNavigateBackToStudio = { currentTab = 0 }
                )
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
