package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

// Google Fonts installed via font-util
val ComicNeueFontFamily = FontFamily(
    Font(R.font.comic_neue, FontWeight.Normal),
    Font(R.font.comic_neue, FontWeight.Bold)
)

val BangersFontFamily = FontFamily(
    Font(R.font.bangers, FontWeight.Normal)
)

val PermanentMarkerFontFamily = FontFamily(
    Font(R.font.permanent_marker, FontWeight.Normal)
)

val CaveatFontFamily = FontFamily(
    Font(R.font.caveat, FontWeight.Normal),
    Font(R.font.caveat, FontWeight.Bold)
)

enum class BubbleFontOption(
    val id: String,
    val displayName: String,
    val description: String,
    val fontFamily: FontFamily
) {
    COMIC_NEUE(
        id = "COMIC_NEUE",
        displayName = "Comic Neue",
        description = "Manga & Comic classique",
        fontFamily = ComicNeueFontFamily
    ),
    BANGERS(
        id = "BANGERS",
        displayName = "Bangers",
        description = "Cri Shonen & Action",
        fontFamily = BangersFontFamily
    ),
    PERMANENT_MARKER(
        id = "PERMANENT_MARKER",
        displayName = "Marker",
        description = "Onomatopées & Impact",
        fontFamily = PermanentMarkerFontFamily
    ),
    CAVEAT(
        id = "CAVEAT",
        displayName = "Caveat",
        description = "Pensée & Murmure intime",
        fontFamily = CaveatFontFamily
    ),
    DEFAULT(
        id = "DEFAULT",
        displayName = "Système",
        description = "Police sans-serif neutre",
        fontFamily = FontFamily.SansSerif
    );

    companion object {
        fun fromId(id: String?): BubbleFontOption {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: COMIC_NEUE
        }
    }
}
