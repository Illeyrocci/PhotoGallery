package com.bignerdranch.android.photogallery.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.model.GalleryItem
import kotlinx.coroutines.flow.Flow

class PhotoRepository(
    private val flickrApi: FlickrApi
) {
    fun getPhotoStream(): Flow<PagingData<GalleryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = 300
            ),
            pagingSourceFactory = { PhotoPagingSource(flickrApi) }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 100
    }
}