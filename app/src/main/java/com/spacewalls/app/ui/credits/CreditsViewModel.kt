package com.spacewalls.app.ui.credits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spacewalls.app.data.model.ImageSource
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreditsUiState(
    val wikimedia: List<Wallpaper> = emptyList(),
    val nasa: List<Wallpaper> = emptyList(),
    val loading: Boolean = true,
    val query: String = ""
) {
    val total: Int get() = wikimedia.size + nasa.size

    val filtered: List<Wallpaper>
        get() {
            val all = wikimedia + nasa
            if (query.isBlank()) return all
            val term = query.trim().lowercase()
            return all.filter {
                it.title.lowercase().contains(term) || it.creator.lowercase().contains(term)
            }
        }
}

class CreditsViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreditsUiState())
    val state: StateFlow<CreditsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { items ->
                _state.update {
                    it.copy(
                        wikimedia = items.filter { w -> w.source == ImageSource.WIKIMEDIA },
                        nasa = items.filter { w -> w.source == ImageSource.NASA },
                        loading = false
                    )
                }
            }
        }
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { CreditsViewModel(container.repository) }
        }
    }
}
