package com.bignerdranch.android.photogallery.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "photos")
data class GalleryItem(
    @field:Json(name = "title") val title: String,
    @PrimaryKey val id: String,
    @field:Json(name = "url_s") val url: String,
)