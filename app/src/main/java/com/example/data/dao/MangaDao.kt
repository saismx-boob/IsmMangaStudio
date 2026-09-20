package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.data.model.MangaProject
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterProfileDao {
    @Query("SELECT * FROM character_profiles ORDER BY createdAt DESC")
    fun getAllCharacters(): Flow<List<CharacterProfile>>

    @Query("SELECT * FROM character_profiles WHERE id = :id")
    suspend fun getCharacterById(id: Long): CharacterProfile?

    @Query("SELECT * FROM character_profiles WHERE visualUid = :visualUid LIMIT 1")
    suspend fun getCharacterByVisualUid(visualUid: String): CharacterProfile?

    @Query("UPDATE character_profiles SET firstAppearanceImagePath = :imagePath, firstAppearancePanelId = :panelId, appearanceCount = appearanceCount + 1 WHERE id = :charId")
    suspend fun updateFirstAppearance(charId: Long, imagePath: String, panelId: Long?)

    @Query("UPDATE character_profiles SET firstAppearanceImagePath = NULL, firstAppearancePanelId = NULL WHERE id = :charId")
    suspend fun clearFirstAppearance(charId: Long)

    @Query("UPDATE character_profiles SET appearanceCount = appearanceCount + 1 WHERE id = :charId")
    suspend fun incrementAppearanceCount(charId: Long)

    @Query("UPDATE character_profiles SET visualUid = :newUid, canonicalSeed = :newSeed WHERE id = :charId")
    suspend fun updateVisualUid(charId: Long, newUid: String, newSeed: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterProfile): Long

    @Update
    suspend fun updateCharacter(character: CharacterProfile)

    @Delete
    suspend fun deleteCharacter(character: CharacterProfile)
}

@Dao
interface BackgroundProfileDao {
    @Query("SELECT * FROM background_profiles ORDER BY createdAt DESC")
    fun getAllBackgrounds(): Flow<List<BackgroundProfile>>

    @Query("SELECT * FROM background_profiles WHERE id = :id")
    suspend fun getBackgroundById(id: Long): BackgroundProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackground(background: BackgroundProfile): Long

    @Update
    suspend fun updateBackground(background: BackgroundProfile)

    @Delete
    suspend fun deleteBackground(background: BackgroundProfile)
}

@Dao
interface MangaProjectDao {
    @Query("SELECT * FROM manga_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<MangaProject>>

    @Query("SELECT * FROM manga_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): MangaProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: MangaProject): Long

    @Update
    suspend fun updateProject(project: MangaProject)

    @Delete
    suspend fun deleteProject(project: MangaProject)
}

@Dao
interface MangaPageDao {
    @Query("SELECT * FROM manga_pages WHERE projectId = :projectId ORDER BY pageNumber ASC")
    fun getPagesForProject(projectId: Long): Flow<List<MangaPage>>

    @Query("SELECT * FROM manga_pages WHERE id = :id")
    suspend fun getPageById(id: Long): MangaPage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: MangaPage): Long

    @Update
    suspend fun updatePage(page: MangaPage)

    @Delete
    suspend fun deletePage(page: MangaPage)
}

@Dao
interface MangaPanelDao {
    @Query("SELECT * FROM manga_panels WHERE pageId = :pageId ORDER BY panelIndex ASC")
    fun getPanelsForPage(pageId: Long): Flow<List<MangaPanel>>

    @Query("SELECT * FROM manga_panels WHERE id = :id")
    suspend fun getPanelById(id: Long): MangaPanel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanel(panel: MangaPanel): Long

    @Update
    suspend fun updatePanel(panel: MangaPanel)

    @Delete
    suspend fun deletePanel(panel: MangaPanel)

    @Query("DELETE FROM manga_panels WHERE pageId = :pageId")
    suspend fun deletePanelsForPage(pageId: Long)
}
