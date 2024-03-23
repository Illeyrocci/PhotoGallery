package com.bignerdranch.android.photogallery.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.data.PhotoRepository.Companion.NETWORK_PAGE_SIZE
import com.bignerdranch.android.photogallery.model.GalleryItem
import retrofit2.HttpException
import java.io.IOException

private const val PHOTO_STARTING_PAGE_INDEX = 1
private const val PHOTO_LAST_PAGE_INDEX = 5

class PhotoPagingSource(
    private val service: FlickrApi
) : PagingSource<Int, GalleryItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        val pageNumber = params.key ?: PHOTO_STARTING_PAGE_INDEX
        return try {
            val response = service.fetchPhotos(pageNumber).photos
            val photos = response.galleryItems
            //flickr offers only 5 pages 100 photos each for free
            val nextKey = if (pageNumber == PHOTO_LAST_PAGE_INDEX) {
                null
            } else {
                // initial load size = 3 * NETWORK_PAGE_SIZE
                // ensure we're not requesting duplicating items, at the 2nd request
                pageNumber + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = photos,
                prevKey = if (pageNumber == PHOTO_STARTING_PAGE_INDEX) null else pageNumber - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}