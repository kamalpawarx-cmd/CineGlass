package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Query(
        """
        SELECT m.* FROM media_items m
        LEFT JOIN addon_repositories r ON m.repoId = r.id
        WHERE m.isUserUploaded = 1 OR r.isEnabled = 1 OR r.id IS NULL
        ORDER BY m.createdAt DESC
        """
    )
    fun observeActiveCatalog(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isUserUploaded = 1 ORDER BY createdAt DESC")
    fun observeUserUploads(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MediaItemEntity?

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MediaItemEntity)

    @Query("UPDATE media_items SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Query("UPDATE media_items SET watchProgressMs = :positionMs, totalDurationMs = :durationMs WHERE id = :id")
    suspend fun updateWatchProgress(id: String, positionMs: Long, durationMs: Long)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM media_items WHERE repoId = :repoId AND isUserUploaded = 0")
    suspend fun deleteByRepoId(repoId: String)
}

@Dao
interface AddonRepositoryDao {
    @Query("SELECT * FROM addon_repositories ORDER BY isBuiltIn DESC, lastSyncedAt DESC")
    fun observeRepositories(): Flow<List<AddonRepositoryEntity>>

    @Query("SELECT * FROM addon_repositories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AddonRepositoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(repos: List<AddonRepositoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(repo: AddonRepositoryEntity)

    @Query("UPDATE addon_repositories SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE addon_repositories SET itemCount = :count, lastSyncedAt = :syncedAt, lastError = :error WHERE id = :id")
    suspend fun updateSyncMeta(id: String, count: Int, syncedAt: Long, error: String?)

    @Query("DELETE FROM addon_repositories WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM offline_downloads ORDER BY updatedAt DESC")
    fun observeDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM offline_downloads WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(download: DownloadEntity)

    @Query(
        """
        UPDATE offline_downloads
        SET status = :status, progress = :progress, bytesDownloaded = :bytesDownloaded,
            totalBytes = :totalBytes, updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateProgress(
        id: String,
        status: String,
        progress: Int,
        bytesDownloaded: Long,
        totalBytes: Long,
        updatedAt: Long
    )

    @Query("DELETE FROM offline_downloads WHERE id = :id")
    suspend fun deleteById(id: String)
}
