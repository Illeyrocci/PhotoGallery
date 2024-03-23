package com.bignerdranch.android.photogallery

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.data.PhotoRepository
import com.bignerdranch.android.photogallery.ui.ViewModelFactory

object Injection {

    //the context will be needed when Room break into
    private fun provideGithubRepository(context: Context): PhotoRepository {
        return PhotoRepository(FlickrApi.create())
    }

    fun provideViewModelFactory(
        context: Context
    ): ViewModelProvider.Factory {
        return ViewModelFactory(provideGithubRepository(context))
    }
}