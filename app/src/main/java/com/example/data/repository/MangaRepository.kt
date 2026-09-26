package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.BackgroundProfile
import com.example.data.model.CharacterProfile
import com.example.data.model.MangaPage
import com.example.data.model.MangaPanel
import com.example.data.model.MangaProject
import kotlinx.coroutines.flow.Flow

class MangaRepository(private val database: AppDatabase) {

    // Characters
    val allCharacters: Flow<List<CharacterProfile>> = database.characterDao().getAllCharacters()
    suspend fun getCharacterById(id: Long) = database.characterDao().getCharacterById(id)
    suspend fun getCharacterByVisualUid(visualUid: String) = database.characterDao().getCharacterByVisualUid(visualUid)
    suspend fun updateCharacterFirstAppearance(charId: Long, imagePath: String, panelId: Long?) =
        database.characterDao().updateFirstAppearance(charId, imagePath, panelId)
    suspend fun clearCharacterFirstAppearance(charId: Long) =
        database.characterDao().clearFirstAppearance(charId)
    suspend fun incrementCharacterAppearance(charId: Long) =
        database.characterDao().incrementAppearanceCount(charId)
    suspend fun updateCharacterVisualUid(charId: Long, newUid: String, newSeed: Long) =
        database.characterDao().updateVisualUid(charId, newUid, newSeed)
    suspend fun saveCharacter(character: CharacterProfile): Long {
        return if (character.id == 0L) {
            database.characterDao().insertCharacter(character)
        } else {
            database.characterDao().updateCharacter(character)
            character.id
        }
    }
    suspend fun deleteCharacter(character: CharacterProfile) = database.characterDao().deleteCharacter(character)

    // Backgrounds
    val allBackgrounds: Flow<List<BackgroundProfile>> = database.backgroundDao().getAllBackgrounds()
    suspend fun getBackgroundById(id: Long) = database.backgroundDao().getBackgroundById(id)
    suspend fun saveBackground(background: BackgroundProfile): Long {
        return if (background.id == 0L) {
            database.backgroundDao().insertBackground(background)
        } else {
            database.backgroundDao().updateBackground(background)
            background.id
        }
    }
    suspend fun deleteBackground(background: BackgroundProfile) = database.backgroundDao().deleteBackground(background)

    // Projects
    val allProjects: Flow<List<MangaProject>> = database.projectDao().getAllProjects()
    suspend fun getProjectById(id: Long) = database.projectDao().getProjectById(id)
    suspend fun saveProject(project: MangaProject): Long {
        return if (project.id == 0L) {
            database.projectDao().insertProject(project)
        } else {
            database.projectDao().updateProject(project.copy(updatedAt = System.currentTimeMillis()))
            project.id
        }
    }
    suspend fun deleteProject(project: MangaProject) = database.projectDao().deleteProject(project)

    // Pages
    fun getPagesForProject(projectId: Long): Flow<List<MangaPage>> = database.pageDao().getPagesForProject(projectId)
    suspend fun getPageById(id: Long) = database.pageDao().getPageById(id)
    suspend fun createPage(page: MangaPage): Long = database.pageDao().insertPage(page)
    suspend fun savePage(page: MangaPage): Long {
        return if (page.id == 0L) {
            database.pageDao().insertPage(page)
        } else {
            database.pageDao().updatePage(page)
            page.id
        }
    }
    suspend fun deletePage(page: MangaPage) {
        database.panelDao().deletePanelsForPage(page.id)
        database.pageDao().deletePage(page)
    }

    // Panels
    val allGeneratedPanels: Flow<List<MangaPanel>> = database.panelDao().getAllGeneratedPanels()
    fun getPanelsForPage(pageId: Long): Flow<List<MangaPanel>> = database.panelDao().getPanelsForPage(pageId)
    fun getPanelsForProject(projectId: Long): Flow<List<MangaPanel>> = database.panelDao().getPanelsForProject(projectId)
    suspend fun getPanelById(id: Long) = database.panelDao().getPanelById(id)
    suspend fun savePanel(panel: MangaPanel): Long {
        return if (panel.id == 0L) {
            database.panelDao().insertPanel(panel)
        } else {
            database.panelDao().updatePanel(panel)
            panel.id
        }
    }
    suspend fun deletePanel(panel: MangaPanel) = database.panelDao().deletePanel(panel)
}
