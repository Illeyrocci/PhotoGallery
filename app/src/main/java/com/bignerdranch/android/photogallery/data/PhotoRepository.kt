package com.bignerdranch.android.photogallery.data

import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.model.GalleryItem

class PhotoRepository(
    private val flickrApi: FlickrApi
) {

    suspend fun fetchPhotos(): List<GalleryItem> =
        flickrApi.fetchPhotos().photos.galleryItems
}