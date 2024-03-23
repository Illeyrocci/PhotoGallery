package com.bignerdranch.android.photogallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bignerdranch.android.photogallery.data.PhotoRepository
import com.bignerdranch.android.photogallery.model.GalleryItem
import kotlinx.coroutines.flow.Flow

class PhotoGalleryViewModel(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    val pagingDataFlow: Flow<PagingData<GalleryItem>>

    init {
        pagingDataFlow = fetchPhoto().cachedIn(viewModelScope)
    }

    private fun fetchPhoto(): Flow<PagingData<GalleryItem>> =
        photoRepository.getPhotoStream()
}