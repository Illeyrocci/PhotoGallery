package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.model.GalleryItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoResponse(
    @field:Json(name = "photo") val galleryItems: List<GalleryItem>
)