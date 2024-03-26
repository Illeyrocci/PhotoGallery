package com.bignerdranch.android.photogallery.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.db.PhotoDatabase
import com.bignerdranch.android.photogallery.model.RemoteKeys
import com.bignerdranch.android.photogallery.model.GalleryItem
import retrofit2.HttpException
import java.io.IOException

private const val PHOTO_STARTING_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class PhotoRemoteMediator(
    private val service: FlickrApi,
    private val database: PhotoDatabase
) : RemoteMediator<Int, GalleryItem>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, GalleryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: PHOTO_STARTING_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val apiResponse = service.fetchPhotos(page)

            val photos = apiResponse.photos.galleryItems
            val endOfPaginationReached = photos.isEmpty()
            database.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
                    database.photoDao().clearPhotos()
                }
                val prevKey = if (page == PHOTO_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = photos.map {
                    RemoteKeys(photoId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeysDao().insertAll(keys)
                database.photoDao().insertAll(photos)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, GalleryItem>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { galleryItem ->
                // Get the remote keys of the last item retrieved
                database.remoteKeysDao().remoteKeysByPhotoId(galleryItem.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, GalleryItem>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { galleryItem ->
                // Get the remote keys of the first items retrieved
                database.remoteKeysDao().remoteKeysByPhotoId(galleryItem.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, GalleryItem>
    ): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { photoId ->
                database.remoteKeysDao().remoteKeysByPhotoId(photoId)
            }
        }
    }
}