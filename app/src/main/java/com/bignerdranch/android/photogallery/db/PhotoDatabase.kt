package com.bignerdranch.android.photogallery.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bignerdranch.android.photogallery.model.GalleryItem
import com.bignerdranch.android.photogallery.model.RemoteKeys

@Database(
    entities = [GalleryItem::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class PhotoDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {


        @Volatile
        private var INSTANCE: PhotoDatabase? = null

        fun getInstance(context: Context): PhotoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                PhotoDatabase::class.java, "Gallery.db"
            )
                .build()
    }
}