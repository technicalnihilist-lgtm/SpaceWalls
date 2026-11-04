package com.spacewalls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WallpaperEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpaceWallsDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        fun build(context: Context): SpaceWallsDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                SpaceWallsDatabase::class.java,
                "spacewalls.db"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
