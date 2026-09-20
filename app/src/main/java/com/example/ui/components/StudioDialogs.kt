package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextSecondary

@Composable
fun CharacterSelectorDialog(
    characters: List<CharacterProfile>,
    selectedCharacterId: Long?,
    onSelectCharacter: (Long?) -> Unit,
    onOpenCharacterVault: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MangaCrimson
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Choisir le Personnage Clé",
                    color = MangaPaperWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "La sélection garantit la cohérence visuelle de son visage, ses cheveux et sa tenue dans toutes les cases générées.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            color = if (selectedCharacterId == null) MangaCrimson.copy(alpha = 0.2f) else InkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedCharacterId == null) MangaCrimson else InkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCharacter(null)
                                    onDismiss()
                                }
                                .testTag("select_no_character")
                        ) {
                            Text(
                                text = "Aucun personnage spécifique (Scène libre)",
                                color = if (selectedCharacterId == null) MangaCrimson else MangaPaperWhite,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    items(characters) { char ->
                        val isSelected = char.id == selectedCharacterId
                        Surface(
                            color = if (isSelected) MangaCrimson.copy(alpha = 0.2f) else InkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) MangaCrimson else InkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCharacter(char.id)
                                    onDismiss()
                                }
                                .testTag("select_character_${char.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = char.name,
                                        color = MangaPaperWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = char.role,
                                            color = QiGold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${char.hairStyleColor} • ${char.clothingDescription}",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
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
                    onDismiss()
                    onOpenCharacterVault()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                modifier = Modifier.testTag("open_vault_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gérer la Bible des Héros")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = TextSecondary)
            }
        }
    )
}

@Composable
fun BackgroundSelectorDialog(
    backgrounds: List<BackgroundProfile>,
    selectedBackgroundId: Long?,
    onSelectBackground: (Long?) -> Unit,
    onOpenBackgroundVault: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = null,
                    tint = Color(0xFF10B981)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Choisir le Décor / Lieu",
                    color = MangaPaperWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "L'arrière-plan sélectionné conservera son architecture et son ambiance lumineuse d'une case à l'autre.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            color = if (selectedBackgroundId == null) Color(0xFF10B981).copy(alpha = 0.2f) else InkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedBackgroundId == null) Color(0xFF10B981) else InkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectBackground(null)
                                    onDismiss()
                                }
                                .testTag("select_no_background")
                        ) {
                            Text(
                                text = "Arrière-plan libre (Non restreint)",
                                color = if (selectedBackgroundId == null) Color(0xFF10B981) else MangaPaperWhite,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    items(backgrounds) { bg ->
                        val isSelected = bg.id == selectedBackgroundId
                        Surface(
                            color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.2f) else InkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else InkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectBackground(bg.id)
                                    onDismiss()
                                }
                                .testTag("select_bg_${bg.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = bg.name,
                                        color = MangaPaperWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = bg.category,
                                            color = Color(0xFF34D399),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${bg.lightingMood} • ${bg.architectureDetails}",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
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
                    onDismiss()
                    onOpenBackgroundVault()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.testTag("open_bg_vault_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gérer les Décors")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DialogueBubbleEditorDialog(
    initialText: String,
    initialType: String,
    initialFont: String = "COMIC_NEUE",
    initialFontSize: Int = 14,
    initialTailDirection: String = "BOTTOM_LEFT",
    initialBgColor: Long = 0xFFFFFFFF,
    initialTextColor: Long = 0xFF000000,
    initialBorderColor: Long = 0xFF000000,
    onSave: (
        text: String,
        type: String,
        font: String,
        fontSize: Int,
        tailDirection: String,
        bgColor: Long,
        textColor: Long,
        borderColor: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedFont by remember { mutableStateOf(initialFont) }
    var selectedFontSize by remember { mutableStateOf(initialFontSize) }
    var selectedTailDir by remember { mutableStateOf(initialTailDirection) }
    var selectedBgColor by remember { mutableStateOf(initialBgColor) }
    var selectedTextColor by remember { mutableStateOf(initialTextColor) }
    var selectedBorderColor by remember { mutableStateOf(initialBorderColor) }

    val quickMangaPhrases = listOf(
        "BAM !",
        "DOKI DOKI",
        "NANI ?!",
        "CRASH !",
        "SHHH...",
        "Pendant ce temps..."
    )

    val colorThemes = listOf(
        Triple("Manga Blanc", 0xFFFFFFFF, 0xFF000000),
        Triple("Ombre Noire", 0xFF111827, 0xFFFFFFFF),
        Triple("Or Shonen", 0xFFFEF08A, 0xFF000000),
        Triple("Cyan Magique", 0xFF0891B2, 0xFFFFFFFF),
        Triple("Furie Rouge", 0xFFDC2626, 0xFFFFFFFF)
    )

    val currentFontOption = com.example.ui.theme.BubbleFontOption.fromId(selectedFont)
    val currentShapeType = BubbleShapeType.fromId(selectedType)
    val currentTailDir = TailDirection.fromId(selectedTailDir)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = MangaCrimson,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bulle Vectorielle & Typographie",
                    color = MangaPaperWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Vector Preview
                item {
                    Text(
                        text = "APERÇU VECTORIEL EN TEMPS RÉEL",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(InkMidnight, RoundedCornerShape(8.dp))
                            .border(1.dp, InkBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        VectorSpeechBubble(
                            text = text.ifBlank { "Votre texte ici..." },
                            shapeType = currentShapeType,
                            tailDirection = currentTailDir,
                            fontOption = currentFontOption,
                            fontSizeSp = selectedFontSize,
                            backgroundColor = Color(selectedBgColor),
                            textColor = Color(selectedTextColor),
                            borderColor = Color(selectedBorderColor)
                        )
                    }
                }

                // Text Input Field
                item {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Texte de la réplique ou onomatopée") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MangaCrimson,
                            unfocusedBorderColor = InkBorder,
                            focusedTextColor = MangaPaperWhite,
                            unfocusedTextColor = MangaPaperWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bubble_text_input")
                    )

                    // Quick SFX Chips
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickMangaPhrases.forEach { phrase ->
                            Surface(
                                color = InkSurfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, InkBorder),
                                modifier = Modifier.clickable { text = phrase }
                            ) {
                                Text(
                                    text = phrase,
                                    color = QiGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Bubble Shape Selector
                item {
                    Text(
                        text = "FORME DE LA BULLE VECTORIELLE :",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BubbleShapeType.entries.forEach { shape ->
                            val isSelected = selectedType.equals(shape.id, ignoreCase = true)
                            Surface(
                                color = if (isSelected) MangaCrimson else InkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) MangaCrimson else InkBorder),
                                modifier = Modifier
                                    .clickable {
                                        selectedType = shape.id
                                        // Auto-tune font and tail for specific shapes
                                        when (shape) {
                                            BubbleShapeType.SHOUT -> {
                                                selectedFont = "BANGERS"
                                                selectedTailDir = "NONE"
                                            }
                                            BubbleShapeType.THOUGHT -> {
                                                selectedFont = "CAVEAT"
                                            }
                                            BubbleShapeType.ONOMATOPOEIA -> {
                                                selectedFont = "PERMANENT_MARKER"
                                                selectedTailDir = "NONE"
                                            }
                                            BubbleShapeType.NARRATION -> {
                                                selectedTailDir = "NONE"
                                            }
                                            else -> {}
                                        }
                                    }
                                    .testTag("bubble_shape_${shape.id}")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = shape.displayName,
                                        color = if (isSelected) Color.White else MangaPaperWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = shape.description,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Typography Font Selector
                item {
                    Text(
                        text = "POLICE TYPOGRAPHIQUE MANGA :",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.ui.theme.BubbleFontOption.entries.forEach { fontOption ->
                            val isSelected = selectedFont.equals(fontOption.id, ignoreCase = true)
                            Surface(
                                color = if (isSelected) Color(0xFF3B82F6) else InkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF60A5FA) else InkBorder),
                                modifier = Modifier
                                    .clickable { selectedFont = fontOption.id }
                                    .testTag("bubble_font_${fontOption.id}")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = fontOption.displayName,
                                        fontFamily = fontOption.fontFamily,
                                        color = if (isSelected) Color.White else MangaPaperWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = fontOption.description,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Font Size & Tail Direction
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Font Size selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TAILLE : ${selectedFontSize}sp",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(12, 14, 16, 20, 26).forEach { sizeSp ->
                                    val isSelected = selectedFontSize == sizeSp
                                    Surface(
                                        color = if (isSelected) MangaCrimson else InkSurfaceVariant,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .clickable { selectedFontSize = sizeSp }
                                    ) {
                                        Text(
                                            text = "${sizeSp}",
                                            color = MangaPaperWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Tail Direction selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "POINTE :",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TailDirection.entries.forEach { dir ->
                                    val isSelected = selectedTailDir.equals(dir.id, ignoreCase = true)
                                    Surface(
                                        color = if (isSelected) Color(0xFF10B981) else InkSurfaceVariant,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { selectedTailDir = dir.id }
                                    ) {
                                        Text(
                                            text = dir.displayName,
                                            color = MangaPaperWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Color Themes
                item {
                    Text(
                        text = "PALETTE DE COULEURS :",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorThemes.forEach { (name, bg, fg) ->
                            val isSelected = selectedBgColor == bg
                            Surface(
                                color = Color(bg),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    2.dp,
                                    if (isSelected) MangaCrimson else Color.DarkGray
                                ),
                                modifier = Modifier
                                    .clickable {
                                        selectedBgColor = bg
                                        selectedTextColor = fg
                                        selectedBorderColor = if (bg == 0xFF111827L) 0xFF4B5563L else 0xFF000000L
                                    }
                            ) {
                                Text(
                                    text = name,
                                    color = Color(fg),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
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
                    onSave(
                        text,
                        selectedType,
                        selectedFont,
                        selectedFontSize,
                        selectedTailDir,
                        selectedBgColor,
                        selectedTextColor,
                        selectedBorderColor
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                modifier = Modifier.testTag("save_bubble_btn")
            ) {
                Text("Appliquer la bulle vectorielle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextSecondary)
            }
        }
    )
}

/**
 * Dialog to preview and export the Manga Panel as high-resolution PNG (Save to gallery or Share).
 */
@Composable
fun ExportPanelDialog(
    panelNumber: Int,
    previewBitmap: android.graphics.Bitmap?,
    isExporting: Boolean,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = QiGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exporter la Case #$panelNumber en HD",
                    color = MangaPaperWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rendu haute résolution (1200 x 1040 px) avec encrage manga, bulle vectorielle, typographie et effets d'ombres.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(InkMidnight)
                        .border(1.5.dp, InkBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Aperçu de la case HD exportée",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        CircularProgressIndicator(
                            color = MangaCrimson,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = InkSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, InkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Format : PNG 32-bit sans compression • Prêt pour impression / partage",
                            color = QiGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShare,
                    enabled = !isExporting && previewBitmap != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.testTag("share_panel_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Partager")
                }

                Button(
                    onClick = onSaveToGallery,
                    enabled = !isExporting && previewBitmap != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                    modifier = Modifier.testTag("save_gallery_panel_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enregistrer")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_export_btn")
            ) {
                Text("Fermer", color = TextSecondary)
            }
        }
    )
}

