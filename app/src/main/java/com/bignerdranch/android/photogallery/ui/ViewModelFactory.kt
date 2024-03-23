package com.bignerdranch.android.photogallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.photogallery.data.PhotoRepository

class ViewModelFactory(
    private val photoRepository: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoGalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoGalleryViewModel(photoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}