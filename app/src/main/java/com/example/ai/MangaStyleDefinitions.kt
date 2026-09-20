package com.example.ai

/**
 * Art style options tailored for Manga, Manhua, and Comics with customizable attributes.
 */
enum class MangaArtStyle(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val promptKeywords: String,
    val defaultAspect: String,
    val recommendedStrokeWeight: Float = 0.8f,
    val recommendedContrast: Float = 0.75f,
    val penTool: String = "G-Pen dynamique",
    val tags: List<String> = listOf("Manga", "Encrage"),
    val isPrimaryStyle: Boolean = false
) {
    MANGA_SHONEN(
        id = "MANGA_SHONEN",
        displayName = "Manga Shōnen",
        subtitle = "Trames dynamiques, encrage G-Pen intense, lignes d'action",
        promptKeywords = "authentic Japanese shonen manga style, professional G-pen dip pen lineart with bold dynamic stroke pressure variation, crisp screentone halftone dots, dramatic dynamic speedlines, high contrast black ink, sharp expressive facial features, weekly shonen jump printed manga page aesthetic",
        defaultAspect = "3:4",
        recommendedStrokeWeight = 0.8f,
        recommendedContrast = 0.75f,
        penTool = "G-Pen dynamique & Trames de vitesse",
        tags = listOf("Action", "G-Pen", "Speedlines", "Impact"),
        isPrimaryStyle = true
    ),
    MANGA_SEINEN(
        id = "MANGA_SEINEN",
        displayName = "Manga Seinen",
        subtitle = "Hachures détaillées (Kakeami), ambiance sombre et mature",
        promptKeywords = "gritty high-detail seinen manga, intricate fine cross-hatching, deep shadows, cinematic noir contrast, atmospheric screentone, realism infused Japanese manga linework, Berserk and Vagabond manga aesthetic",
        defaultAspect = "3:4",
        recommendedStrokeWeight = 0.4f,
        recommendedContrast = 0.85f,
        penTool = "Plume Maru & Hachures Kakeami",
        tags = listOf("Mature", "Kakeami", "Ombres denses", "Réalisme"),
        isPrimaryStyle = true
    ),
    MANGA_SHOJO(
        id = "MANGA_SHOJO",
        displayName = "Manga Shōjo",
        subtitle = "Traits fins et aériens, regards étincelants & trames poétiques",
        promptKeywords = "refined shojo manga art style, delicate ethereal fine lineart, oversized expressive sparkling eyes with luminous multi-layered highlights, floating flower petals, screentone sparkles, soft romantic atmosphere, Margaret and Ribon magazine classic shojo aesthetic",
        defaultAspect = "3:4",
        recommendedStrokeWeight = 0.3f,
        recommendedContrast = 0.45f,
        penTool = "Plume fine & Trames scintillantes",
        tags = listOf("Émotion", "Plume fine", "Scintillements", "Trames florales"),
        isPrimaryStyle = true
    ),
    CLASSIC_BLACK_AND_WHITE(
        id = "CLASSIC_BLACK_AND_WHITE",
        displayName = "Noir & Blanc classique",
        subtitle = "Encrage traditionnel à la plume, aplats purs & trames d'époque",
        promptKeywords = "classic vintage black and white manga, pure black sumi ink dip pen linework, bold solid black fills, traditional cross-hatching, retro 1980s manga aesthetic, high contrast monochrome print, no grayscale wash, vintage Tezuka and Toriyama ink aesthetic",
        defaultAspect = "3:4",
        recommendedStrokeWeight = 0.6f,
        recommendedContrast = 1.0f,
        penTool = "Plume d'encre de Chine pure",
        tags = listOf("Vintage", "Encre de Chine", "Aplats purs", "Rétro"),
        isPrimaryStyle = true
    ),
    MANHUA_XIANXIA(
        id = "MANHUA_XIANXIA",
        displayName = "Manhua Xianxia",
        subtitle = "Couleurs célestes, auras de Qi, calligraphie & soie",
        promptKeywords = "traditional Chinese manhua Xianxia cultivation art style, luminous celestial colors, glowing spiritual Qi energy wisps, fluid silk robes, ink wash brush painting blended with vibrant digital coloring, ethereal fantasy atmosphere",
        defaultAspect = "9:16",
        recommendedStrokeWeight = 0.9f,
        recommendedContrast = 0.65f,
        penTool = "Pinceau calligraphique chinois",
        tags = listOf("Xianxia", "Qi", "Pinceau", "Couleurs"),
        isPrimaryStyle = false
    ),
    MANHWA_WEBTOON(
        id = "MANHWA_WEBTOON",
        displayName = "Manhwa Webtoon",
        subtitle = "Cell-shading numérique, éclairage néon, format vertical",
        promptKeywords = "modern Korean manhwa webtoon style, crisp clean digital vector lineart, smooth cell shading with soft gradient light, high saturation rim lighting, modern Solo Leveling webtoon aesthetic",
        defaultAspect = "9:16",
        recommendedStrokeWeight = 0.5f,
        recommendedContrast = 0.70f,
        penTool = "Vector numérique & Cell-shading",
        tags = listOf("Webtoon", "Numérique", "Néon", "Vertical"),
        isPrimaryStyle = false
    ),
    WESTERN_COMIC(
        id = "WESTERN_COMIC",
        displayName = "Comics US Moderne",
        subtitle = "Encrage au pinceau lourd, aplats vifs, pop-art",
        promptKeywords = "American graphic novel comic book style, bold dynamic brush ink outlines, deep chiseled shadows, vibrant four-color offset printing halftone dot shading, heroic dynamic framing, Marvel/DC modern comic book art",
        defaultAspect = "3:4",
        recommendedStrokeWeight = 1.1f,
        recommendedContrast = 0.85f,
        penTool = "Pinceau lourd & Ombres taillées",
        tags = listOf("Comics", "Pinceau gras", "Aplats", "Héroïque"),
        isPrimaryStyle = false
    ),
    CHIBI_ANIME(
        id = "CHIBI_ANIME",
        displayName = "Anime Chibi / Tranche de vie",
        subtitle = "Proportions mignonnes, traits doux et expressifs",
        promptKeywords = "super cute chibi anime manga style, expressive oversized eyes, rounded soft lineart, playful dynamic expressions, clean pastel color tones",
        defaultAspect = "1:1",
        recommendedStrokeWeight = 0.7f,
        recommendedContrast = 0.50f,
        penTool = "Trait arrondi & Doux",
        tags = listOf("Chibi", "Mignon", "Pastel", "Arrondi"),
        isPrimaryStyle = false
    );

    companion object {
        fun fromId(id: String): MangaArtStyle {
            return entries.firstOrNull { it.id == id } ?: MANGA_SHONEN
        }
    }
}

enum class ColorRenderingMode(
    val id: String,
    val displayName: String,
    val promptModifier: String
) {
    BLACK_AND_WHITE(
        id = "BLACK_AND_WHITE",
        displayName = "Noir & Blanc + Trames",
        promptModifier = "pure black and white monochrome manga print, fine halftone screentone patterns, no color, traditional paper manga printing ink"
    ),
    DIGITAL_COLOR(
        id = "DIGITAL_COLOR",
        displayName = "Couleur Numérique",
        promptModifier = "full digital vibrant colors, volumetric lighting, rich color palette, cinematic color grading"
    ),
    WATERCOLOR_WASH(
        id = "WATERCOLOR_WASH",
        displayName = "Aquarelle & Encre de Chine",
        promptModifier = "traditional sumi-e ink wash and delicate watercolor coloring, textured art paper grain, artistic pigment bleeding"
    ),
    POP_ART_HALFTONE(
        id = "POP_ART_HALFTONE",
        displayName = "Pop-Art & Points Benday",
        promptModifier = "vintage comic book printing, retro CMYK dot registration, visible newsprint texture, high saturated pop primary colors"
    );

    companion object {
        fun fromId(id: String): ColorRenderingMode {
            return entries.firstOrNull { it.id == id } ?: BLACK_AND_WHITE
        }
    }
}

enum class LineWeightStyle(
    val id: String,
    val displayName: String,
    val promptModifier: String
) {
    DYNAMIC_INK(
        id = "DYNAMIC_INK",
        displayName = "Encre Dynamique (G-Pen)",
        promptModifier = "variable pressure G-pen ink strokes, sharp tapers, bold accent lines, intense action weighting"
    ),
    FINE_PEN(
        id = "FINE_PEN",
        displayName = "Plume Fine & Précise (Maru-pen)",
        promptModifier = "ultra delicate fine maru-pen lines, meticulous micro-detailing, crisp architectural precision"
    ),
    BRUSH_XIANXIA(
        id = "BRUSH_XIANXIA",
        displayName = "Pinceau Calligraphique",
        promptModifier = "expressive flowing Chinese calligraphy brush strokes, organic ink splatters, fluid martial arts motion trails"
    );

    companion object {
        fun fromId(id: String): LineWeightStyle {
            return entries.firstOrNull { it.id == id } ?: DYNAMIC_INK
        }
    }
}

enum class CameraPerspective(
    val id: String,
    val displayName: String,
    val promptModifier: String
) {
    MEDIUM_SHOT("MEDIUM_SHOT", "Plan Moyen (Buste)", "medium shot framing from waist up"),
    CLOSE_UP("CLOSE_UP", "Gros Plan (Visage)", "intense dramatic close-up on character face and expressive eyes"),
    WIDE_ESTABLISHING("WIDE_ESTABLISHING", "Plan Large (Décor)", "epic cinematic wide angle shot establishing the majestic scenery and character in environment"),
    DUTCH_ANGLE("DUTCH_ANGLE", "Plongée / Angle Incliné", "dynamic tilted dutch angle, high action perspective, foreshortened perspective lines"),
    LOW_ANGLE("LOW_ANGLE", "Contre-plongée Héroïque", "heroic low-angle perspective looking up, imposing and powerful stance");

    companion object {
        fun fromId(id: String): CameraPerspective {
            return entries.firstOrNull { it.id == id } ?: MEDIUM_SHOT
        }
    }
}

/**
 * Technical presets and prompts for line thickness (épaisseur du trait) and contrast.
 */
object MangaStyleSettingsHelper {

    val strokePresets = listOf(
        StrokePreset("Ultra-fin", 0.2f, "0.2 mm", "Plume Maru / Détails & Shōjo"),
        StrokePreset("Fin", 0.4f, "0.4 mm", "Plume fine / Hachures Seinen"),
        StrokePreset("Standard", 0.7f, "0.7 mm", "G-Pen classique Shōnen"),
        StrokePreset("Épais", 1.0f, "1.0 mm", "Encrage d'action soutenu"),
        StrokePreset("Dynamique", 1.4f, "1.4 mm", "Pinceau Fude calligraphique")
    )

    val contrastPresets = listOf(
        ContrastPreset("Doux", 0.3f, "30%", "Nuances pastel & voilées"),
        ContrastPreset("Équilibré", 0.6f, "60%", "Trames standards d'impression"),
        ContrastPreset("Contrasté", 0.8f, "80%", "Ombrages marqués & reflets"),
        ContrastPreset("Noir Profond", 1.0f, "100%", "Aplats purs & N&B extrême")
    )

    fun getStrokeDescription(weight: Float): String = when {
        weight < 0.35f -> "Ultra-fin (${String.format(java.util.Locale.US, "%.1f", weight)} mm) - Plume Maru délicate"
        weight < 0.60f -> "Fin (${String.format(java.util.Locale.US, "%.1f", weight)} mm) - Plume précise & hachures"
        weight < 0.90f -> "Standard (${String.format(java.util.Locale.US, "%.1f", weight)} mm) - G-Pen dynamique Shōnen"
        weight < 1.20f -> "Épais (${String.format(java.util.Locale.US, "%.1f", weight)} mm) - Contours d'action accentués"
        else -> "Extra-gras (${String.format(java.util.Locale.US, "%.1f", weight)} mm) - Pinceau fude calligraphique"
    }

    fun getStrokePrompt(weight: Float): String = when {
        weight < 0.35f -> "ultra-fine delicate 0.2mm needle lineart, ethereal micro-pen strokes, delicate hair strands"
        weight < 0.60f -> "fine crisp 0.4mm maru-pen lines, controlled intricate hatching, architectural precision"
        weight < 0.90f -> "dynamic variable-pressure 0.7mm G-pen inking, bold expressive manga contour lines"
        weight < 1.20f -> "heavy bold 1.0mm ink outline strokes, punchy inked action silhouettes"
        else -> "very thick 1.4mm brush pen inking, intense heavy black calligraphy outlines"
    }

    fun getContrastDescription(contrast: Float): String = when {
        contrast < 0.40f -> "Doux (${(contrast * 100).toInt()}%) - Trames estompées et nuances subtiles"
        contrast < 0.70f -> "Équilibré (${(contrast * 100).toInt()}%) - Dégradés classiques d'imprimerie"
        contrast < 0.90f -> "Contrasté (${(contrast * 100).toInt()}%) - Ombres denses et éclats de lumière"
        else -> "Noir Profond (${(contrast * 100).toInt()}%) - Aplats d'encre noire pure et blancs aveuglants"
    }

    fun getContrastPrompt(contrast: Float): String = when {
        contrast < 0.40f -> "soft delicate grayscale screentone shading, gentle midtone gradients, luminous pastel depth"
        contrast < 0.70f -> "standard balanced manga halftone contrast, clear screentone dots, harmonious shadow blocks"
        contrast < 0.90f -> "high dramatic manga contrast, rich velvety black shadows, crisp striking highlights"
        else -> "extreme pure black and white contrast, deep solid pitch-black ink fills, stark blinding white highlights, zero color wash, pure stark sumi ink chiaroscuro"
    }
}

data class StrokePreset(
    val label: String,
    val value: Float,
    val mmDisplay: String,
    val description: String
)

data class ContrastPreset(
    val label: String,
    val value: Float,
    val percentDisplay: String,
    val description: String
)
