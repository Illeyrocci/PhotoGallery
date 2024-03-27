package com.bignerdranch.android.photogallery.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.bignerdranch.android.photogallery.Injection
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoGalleryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PhotoGalleryFragment : Fragment() {
    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels {
        Injection.provideViewModelFactory(requireContext())
    }

    private val adapter = PhotoListAdapter()
    private val headerAdapter = PhotoLoadStateAdapter { adapter.retry() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)
        binding.photoGrid.adapter = adapter.withLoadStateHeaderAndFooter(
            header = headerAdapter,
            footer = PhotoLoadStateAdapter { adapter.retry() }
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            retryButton.setOnClickListener { adapter.retry() }
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    adapter.loadStateFlow.collect { loadState ->
                        emptyList.isVisible =
                            loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
                        photoGrid.isVisible =
                            loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
                        progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
                        retryButton.isVisible =
                            loadState.mediator?.refresh is LoadState.Error && adapter.itemCount == 0

                        // Show a retry header if there was an error refreshing, and items were previously
                        // cached OR default to the default prepend state
                        headerAdapter.loadState = loadState.mediator
                            ?.refresh
                            ?.takeIf { it is LoadState.Error && adapter.itemCount > 0 }
                            ?: loadState.prepend

                        // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
                        val errorState = loadState.source.append as? LoadState.Error
                            ?: loadState.source.prepend as? LoadState.Error
                            ?: loadState.append as? LoadState.Error
                            ?: loadState.prepend as? LoadState.Error
                        errorState?.let {
                            Toast.makeText(
                                requireContext(),
                                "\uD83D\uDE28 Wooops ${it.error}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.pagingDataFlow.collectLatest(adapter::submitData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}