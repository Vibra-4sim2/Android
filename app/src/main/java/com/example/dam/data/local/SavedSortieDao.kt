package com.example.dam.data.local

import androidx.room.*
import com.example.dam.models.SavedSortieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSortieDao {

    @Query("SELECT * FROM saved_sorties WHERE userId = :userId ORDER BY savedAt DESC")
    fun getSavedSorties(userId: String): Flow<List<SavedSortieEntity>>

    @Query("SELECT * FROM saved_sorties WHERE userId = :userId AND isSyncedWithServer = 0")
    suspend fun getUnsyncedSorties(userId: String): List<SavedSortieEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_sorties WHERE sortieId = :sortieId AND userId = :userId)")
    suspend fun isSortieSaved(sortieId: String, userId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSortie(sortie: SavedSortieEntity)

    @Query("DELETE FROM saved_sorties WHERE sortieId = :sortieId AND userId = :userId")
    suspend fun deleteSavedSortie(sortieId: String, userId: String)

    @Query("UPDATE saved_sorties SET isSyncedWithServer = 1 WHERE sortieId = :sortieId")
    suspend fun markAsSynced(sortieId: String)

    @Query("DELETE FROM saved_sorties WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}

