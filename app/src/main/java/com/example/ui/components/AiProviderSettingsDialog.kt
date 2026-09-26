package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.ai.provider.AiPreferencesManager
import com.example.ai.provider.AiProvider
import com.example.ai.provider.AiProviderConfig
import com.example.ai.provider.MultiAiImageService
import com.example.ui.theme.InkBorder
import com.example.ui.theme.InkMidnight
import com.example.ui.theme.InkSurface
import com.example.ui.theme.InkSurfaceVariant
import com.example.ui.theme.MangaCrimson
import com.example.ui.theme.MangaPaperWhite
import com.example.ui.theme.ManhuaCyan
import com.example.ui.theme.QiGold
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiProviderSettingsDialog(
    preferencesManager: AiPreferencesManager,
    multiAiService: MultiAiImageService,
    activeProvider: AiProvider,
    onProviderChanged: (AiProvider) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedProviderTab by remember { mutableStateOf(activeProvider) }
    var currentConfig by remember(selectedProviderTab) {
        mutableStateOf(preferencesManager.loadConfigFor(selectedProviderTab))
    }

    var apiKeyInput by remember(selectedProviderTab) {
        mutableStateOf(currentConfig.apiKey)
    }
    var selectedModel by remember(selectedProviderTab) {
        mutableStateOf(currentConfig.selectedModel)
    }
    var customBaseUrlInput by remember(selectedProviderTab) {
        mutableStateOf(currentConfig.customBaseUrl)
    }
    var isKeyVisible by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var showAdvancedOptions by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = InkSurface,
                border = BorderStroke(1.dp, InkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MangaCrimson.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MangaCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MOTEURS D'IA DE DESSIN",
                                    color = MangaPaperWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Meta, GPT, Grok, IA Chinoises & Gemini",
                                    color = ManhuaCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp).testTag("close_ai_settings_dialog_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = InkBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 520.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section 1: Selector of Providers (Horizontal Chips/Badges)
                        item {
                            Text(
                                text = "CHOISISSEZ L'IA À CONFIGURER OU ACTIVER",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("ai_provider_chips_row")
                            ) {
                                items(AiProvider.entries) { provider ->
                                    val isCurrentTab = provider == selectedProviderTab
                                    val isAppActive = provider == activeProvider

                                    Surface(
                                        color = if (isCurrentTab) Color(provider.badgeColor).copy(alpha = 0.22f) else InkMidnight,
                                        border = BorderStroke(
                                            width = if (isCurrentTab) 2.dp else 1.dp,
                                            color = if (isCurrentTab) Color(provider.badgeColor) else InkBorder
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .clickable {
                                                selectedProviderTab = provider
                                                testResultMessage = null
                                            }
                                            .testTag("ai_chip_${provider.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(provider.badgeColor))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = provider.shortName,
                                                color = if (isCurrentTab) MangaPaperWhite else Color(0xFFCBD5E1),
                                                fontWeight = if (isCurrentTab) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                            if (isAppActive) {
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Surface(
                                                    color = MangaCrimson,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIF",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Selected Provider Details & Key configuration
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = InkMidnight),
                                border = BorderStroke(1.dp, Color(selectedProviderTab.badgeColor).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = selectedProviderTab.displayName,
                                            color = MangaPaperWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (selectedProviderTab == activeProvider) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF34D399),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Moteur actuel",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = selectedProviderTab.subtitle,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                                    )

                                    // Special badge for Free mode
                                    if (selectedProviderTab == AiProvider.POLLINATIONS_FREE) {
                                        Surface(
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Aucune clé requise ! Modèle FLUX & Anime direct disponible pour tout le monde.",
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    } else {
                                        // API Key Field
                                        Text(
                                            text = "CLÉ API (${selectedProviderTab.shortName.uppercase()})",
                                            color = QiGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = apiKeyInput,
                                            onValueChange = {
                                                apiKeyInput = it
                                                testResultMessage = null
                                            },
                                            placeholder = {
                                                Text(
                                                    text = selectedProviderTab.keyPlaceholder,
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                                        Icon(
                                                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = "Afficher/Masquer",
                                                            tint = TextSecondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                            val clip = clipboard.primaryClip
                                                            if (clip != null && clip.itemCount > 0) {
                                                                val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                                                if (pasted.isNotBlank()) {
                                                                    apiKeyInput = pasted.trim()
                                                                    Toast.makeText(context, "Clé collée !", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ContentPaste,
                                                            contentDescription = "Coller depuis le presse-papier",
                                                            tint = ManhuaCyan,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(selectedProviderTab.badgeColor),
                                                unfocusedBorderColor = InkBorder,
                                                focusedTextColor = MangaPaperWhite,
                                                unfocusedTextColor = MangaPaperWhite
                                            ),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("api_key_input_field")
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = selectedProviderTab.documentationHint,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Model Selection Chips
                                    Text(
                                        text = "MODÈLE D'IMAGE RECOMMANDÉ",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        selectedProviderTab.availableModels.forEach { modelName ->
                                            val isSelectedModel = selectedModel == modelName
                                            Surface(
                                                color = if (isSelectedModel) MangaCrimson.copy(alpha = 0.25f) else InkSurfaceVariant,
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (isSelectedModel) MangaCrimson else InkBorder
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .clickable { selectedModel = modelName }
                                                    .testTag("model_chip_$modelName")
                                            ) {
                                                Text(
                                                    text = modelName,
                                                    color = if (isSelectedModel) MangaPaperWhite else TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelectedModel) FontWeight.Bold else FontWeight.Normal,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Advanced Options Accordion (Custom URL, quality)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showAdvancedOptions = !showAdvancedOptions },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = ManhuaCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (showAdvancedOptions) "Masquer les options réseau" else "Options avancées (URL personnalisée / Proxy)",
                                            color = ManhuaCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    AnimatedVisibility(visible = showAdvancedOptions) {
                                        Column(modifier = Modifier.padding(top = 8.dp)) {
                                            Text(
                                                text = "URL DE BASE (Optionnel pour proxy ou OneAPI)",
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = customBaseUrlInput,
                                                onValueChange = { customBaseUrlInput = it },
                                                placeholder = {
                                                    Text(
                                                        text = preferencesManager.defaultBaseUrlFor(selectedProviderTab),
                                                        color = TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ManhuaCyan,
                                                    unfocusedBorderColor = InkBorder,
                                                    focusedTextColor = MangaPaperWhite,
                                                    unfocusedTextColor = MangaPaperWhite
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Test Connection Button & Result
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                isTestingConnection = true
                                                testResultMessage = null
                                                coroutineScope.launch {
                                                    val testConfig = AiProviderConfig(
                                                        provider = selectedProviderTab,
                                                        apiKey = apiKeyInput,
                                                        selectedModel = selectedModel,
                                                        customBaseUrl = customBaseUrlInput
                                                    )
                                                    val res = multiAiService.testConnection(testConfig)
                                                    isTestingConnection = false
                                                    testResultMessage = if (res.isSuccess) {
                                                        Pair(true, res.getOrNull() ?: "Connexion établie avec succès !")
                                                    } else {
                                                        Pair(false, res.exceptionOrNull()?.message ?: "Erreur de connexion")
                                                    }
                                                }
                                            },
                                            enabled = !isTestingConnection,
                                            border = BorderStroke(1.dp, QiGold.copy(alpha = 0.6f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = QiGold),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("test_ai_connection_btn")
                                        ) {
                                            if (isTestingConnection) {
                                                CircularProgressIndicator(
                                                    color = QiGold,
                                                    strokeWidth = 2.dp,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Vérification...", fontSize = 11.sp)
                                            } else {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Tester la Clé", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Test result notification
                                    testResultMessage?.let { (success, msg) ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = if (success) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                                            border = BorderStroke(1.dp, if (success) Color(0xFF10B981) else Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                                    contentDescription = null,
                                                    tint = if (success) Color(0xFF34D399) else Color(0xFFF87171),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = msg,
                                                    color = Color.White,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = InkBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialog bottom action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Fermer")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Save configuration for the selected provider tab
                                val newConfig = AiProviderConfig(
                                    provider = selectedProviderTab,
                                    apiKey = apiKeyInput.trim(),
                                    selectedModel = selectedModel.trim(),
                                    customBaseUrl = customBaseUrlInput.trim()
                                )
                                preferencesManager.saveConfig(newConfig)
                                preferencesManager.setActiveProvider(selectedProviderTab)
                                onProviderChanged(selectedProviderTab)
                                Toast.makeText(
                                    context,
                                    "Moteur IA activé : ${selectedProviderTab.displayName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MangaCrimson),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_and_activate_ai_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Activer ${selectedProviderTab.shortName}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    )
}
