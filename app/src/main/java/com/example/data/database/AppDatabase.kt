package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BackgroundProfileDao
import com.example.data.dao.CharacterProfileDao
import com.example.data.dao.MangaPageDao
import com.example.data.dao.MangaPanelDao
import com.example.data.dao.MangaProjectDao
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.data.model.MangaProject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CharacterProfile::class,
        BackgroundProfile::class,
        MangaProject::class,
        MangaPage::class,
        MangaPanel::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterProfileDao
    abstract fun backgroundDao(): BackgroundProfileDao
    abstract fun projectDao(): MangaProjectDao
    abstract fun pageDao(): MangaPageDao
    abstract fun panelDao(): MangaPanelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "manga_studio.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial characters, backgrounds and a sample manga project
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val char1Id = db.characterDao().insertCharacter(
                CharacterProfile(
                    name = "Ren Kurosaki",
                    role = "Protagoniste Shonen",
                    hairStyleColor = "Cheveux noirs en pics avec mèches rubis",
                    eyeDescription = "Yeux ambrés déterminés et perçants",
                    clothingDescription = "Veste noire dézippée avec doublure rouge et bandages aux poignets",
                    distinctiveFeatures = "Katana d'encre spectrale fixé à la ceinture",
                    signatureColor = "#EF4444",
                    promptAnchor = "Young anime male protagonist Ren Kurosaki, spiky black hair with ruby red tips, fierce amber eyes, black open high-collar jacket with red lining, athletic build, iconic manga shonen aesthetic",
                    visualUid = "CHR-REN-9F2A",
                    canonicalSeed = 101010L,
                    appearanceCount = 2
                )
            )

            val char2Id = db.characterDao().insertCharacter(
                CharacterProfile(
                    name = "Mei Ling (Fée de Jade)",
                    role = "Cultivatrice Céleste Manhua",
                    hairStyleColor = "Longs cheveux d'ébène soyeux flottant au vent avec épingle en jade céleste",
                    eyeDescription = "Yeux vert émeraude mystiques et calmes",
                    clothingDescription = "Robe traditionnelle Hanfu en soie blanche et cyan avec broderies de dragon argenté",
                    distinctiveFeatures = "Aura spirituelle dorée scintillante autour des paumes",
                    signatureColor = "#06B6D4",
                    promptAnchor = "Ethereal Chinese Manhua heroine Mei Ling, flowing raven hair with jade hairpin, luminous jade green eyes, elegant flowing cyan and white silk cultivation Hanfu robes, celestial glowing qi energy",
                    visualUid = "CHR-MEI-7C4B",
                    canonicalSeed = 202020L,
                    appearanceCount = 0
                )
            )

            val char3Id = db.characterDao().insertCharacter(
                CharacterProfile(
                    name = "Viper - Nova",
                    role = "Justicière Cyber Comics",
                    hairStyleColor = "Coupe asymétrique violet néon électrique",
                    eyeDescription = "Visière cybernétique holographique bleue",
                    clothingDescription = "Armure tactique en fibre de carbone sombre et holster laser",
                    distinctiveFeatures = "Tatouage cybernétique lumineux sur le cou",
                    signatureColor = "#8B5CF6",
                    promptAnchor = "Modern western comic superhero Viper Nova, electric purple angled bob hair, tactical dark carbon fiber armored suit with glowing cyan circuits, bold heavy ink comic book shadows",
                    visualUid = "CHR-VIP-3E8D",
                    canonicalSeed = 303030L,
                    appearanceCount = 0
                )
            )

            val bg1Id = db.backgroundDao().insertBackground(
                BackgroundProfile(
                    name = "Toit Néo-Tokyo sous la Lune",
                    category = "Urbain / Nuit Manga",
                    lightingMood = "Clair de lune froid et lueurs de néons bleus",
                    architectureDetails = "Antennes satellites, climatiseurs industriels, gratte-ciels néon en contrebas",
                    promptAnchor = "Cinematic night rooftop in Neo-Tokyo, towering cyberpunk skyline, oversized glowing moon, industrial vents and cables, high contrast screentone manga ink style"
                )
            )

            val bg2Id = db.backgroundDao().insertBackground(
                BackgroundProfile(
                    name = "Pic Céleste des Nuages Immortels",
                    category = "Fantaisie Manhua",
                    lightingMood = "Aurore dorée avec brume flottante lumineuse",
                    architectureDetails = "Falaises rocheuses flottantes, pins anciens tordus, pavillon taoïste ancien en bois sculpté",
                    promptAnchor = "Mystical floating mountain peaks enveloped in glowing clouds, ancient twisting pine trees, Xianxia cultivation pavilion, Chinese manhua celestial landscape with gold light rays"
                )
            )

            val projectId = db.projectDao().insertProject(
                MangaProject(
                    title = "Les Chroniques de l'Encre Céleste",
                    synopsis = "Un jeune combattant découvre un pinceau ancien capable de matérialiser les esprits légendaires à travers les dimensions du manga et du manhua.",
                    artStyle = "MANGA_SHONEN",
                    colorMode = "BLACK_AND_WHITE",
                    lineStyle = "DYNAMIC_INK"
                )
            )

            val pageId = db.pageDao().insertPage(
                MangaPage(
                    projectId = projectId,
                    pageNumber = 1,
                    layoutType = "TWO_PANELS_VERTICAL"
                )
            )

            db.panelDao().insertPanel(
                MangaPanel(
                    pageId = pageId,
                    panelIndex = 0,
                    userPrompt = "Ren regarde l'horizon depuis le toit de la ville, le vent faisant battre sa veste.",
                    characterId = char1Id,
                    backgroundId = bg1Id,
                    dialogueText = "L'encre commence à s'agiter... Quelque chose approche.",
                    bubbleType = "THOUGHT",
                    bubbleFont = "CAVEAT",
                    bubbleFontSize = 15,
                    bubbleTailDirection = "BOTTOM_LEFT",
                    bubbleNormalizedX = 0.35f,
                    bubbleNormalizedY = 0.30f
                )
            )

            db.panelDao().insertPanel(
                MangaPanel(
                    pageId = pageId,
                    panelIndex = 1,
                    userPrompt = "Gros plan sur son regard intense, son katana d'encre commence à briller.",
                    characterId = char1Id,
                    backgroundId = bg1Id,
                    dialogueText = "À mon tour d'écrire l'histoire !",
                    bubbleType = "SHOUT",
                    bubbleFont = "BANGERS",
                    bubbleFontSize = 18,
                    bubbleTailDirection = "NONE",
                    bubbleNormalizedX = 0.5f,
                    bubbleNormalizedY = 0.70f
                )
            )
        }
    }
}
