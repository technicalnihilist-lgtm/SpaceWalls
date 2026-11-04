package com.spacewalls.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Wallpaper> = emptyList(),
    val downloads: List<Wallpaper> = emptyList(),
    val recentlyViewed: List<Wallpaper> = emptyList(),
    val loading: Boolean = true
)

class FavoritesViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeFavorites().collect { items ->
                _state.update { it.copy(favorites = items, loading = false) }
            }
        }
        viewModelScope.launch {
            repository.observeDownloads().collect { items ->
                _state.update { it.copy(downloads = items) }
            }
        }
        viewModelScope.launch {
            repository.observeRecentlyViewed(12).collect { items ->
                _state.update { it.copy(recentlyViewed = items) }
            }
        }
    }

    fun toggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch { repository.setFavorite(wallpaper.id, !wallpaper.isFavorite) }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { FavoritesViewModel(container.repository) }
        }
    }
}
