package com.spacewalls.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spacewalls.app.data.model.ImageSource
import com.spacewalls.app.data.model.SpaceCatalog
import com.spacewalls.app.data.model.SpaceSubcategory
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SourceFilter(val label: String) {
    ALL("All sources"), WIKIMEDIA("Wikimedia"), NASA("NASA")
}

data class CategoryUiState(
    val title: String = "",
    val blurb: String = "",
    val items: List<Wallpaper> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val notice: String? = null,
    val filter: SourceFilter = SourceFilter.ALL,
    val page: Int = 0
) {
    val visible: List<Wallpaper>
        get() = when (filter) {
            SourceFilter.ALL -> items
            SourceFilter.WIKIMEDIA -> items.filter { it.source == ImageSource.WIKIMEDIA }
            SourceFilter.NASA -> items.filter { it.source == ImageSource.NASA }
        }
}

class CategoryViewModel(
    private val repository: WallpaperRepository,
    private val subcategoryId: String
) : ViewModel() {

    private val subcategory: SpaceSubcategory? = SpaceCatalog.subcategory(subcategoryId)
    private val queryKey = "cat:$subcategoryId"

    private val _state = MutableStateFlow(
        CategoryUiState(
            title = subcategory?.name.orEmpty(),
            blurb = subcategory?.blurb.orEmpty()
        )
    )
    val state: StateFlow<CategoryUiState> = _state.asStateFlow()

    private var job: Job? = null

    init {
        load(reset = true)
    }

    fun setFilter(filter: SourceFilter) = _state.update { it.copy(filter = filter) }

    fun refresh() = load(reset = true)

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        load(reset = false)
    }

    private fun load(reset: Boolean) {
        val sub = subcategory ?: return
        job?.cancel()
        job = viewModelScope.launch {
            val nextPage = if (reset) 1 else _state.value.page + 1
            _state.update {
                it.copy(
                    loading = reset,
                    loadingMore = !reset,
                    error = null
                )
            }

            val outcome = runCatching {
                repository.loadPage(
                    queryKey = queryKey,
                    categoryKey = sub.id,
                    wikimediaQuery = sub.wikimediaQuery,
                    nasaQuery = sub.nasaQuery,
                    page = nextPage
                )
            }.getOrNull()

            if (outcome == null) {
                _state.update {
                    it.copy(
                        loading = false,
                        loadingMore = false,
                        error = "Could not load this collection. Pull to refresh."
                    )
                }
                return@launch
            }

            _state.update { current ->
                val merged = if (reset) {
                    outcome.items
                } else {
                    (current.items + outcome.items).distinctBy { it.id }
                }
                current.copy(
                    items = merged,
                    loading = false,
                    loadingMore = false,
                    hasMore = outcome.hasMore,
                    notice = outcome.warnings.firstOrNull(),
                    page = nextPage,
                    error = if (merged.isEmpty()) {
                        "No verified imagery found for this collection."
                    } else {
                        null
                    }
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, subcategoryId: String) = viewModelFactory {
            initializer { CategoryViewModel(container.repository, subcategoryId) }
        }
    }
}
