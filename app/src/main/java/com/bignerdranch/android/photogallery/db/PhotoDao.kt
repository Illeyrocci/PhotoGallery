package com.bignerdranch.android.photogallery.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bignerdranch.android.photogallery.model.GalleryItem

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(repos: List<GalleryItem>)

    @Query("SELECT * FROM photos")
    fun photos(): PagingSource<Int, GalleryItem>

    @Query("DELETE FROM photos")
    suspend fun clearPhotos()
}