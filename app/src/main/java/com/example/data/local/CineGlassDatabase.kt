package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MediaItemEntity::class,
        AddonRepositoryEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CineGlassDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun addonRepositoryDao(): AddonRepositoryDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: CineGlassDatabase? = null

        fun getInstance(context: Context): CineGlassDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CineGlassDatabase::class.java,
                    "cineglass_media.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
