package com.spacewalls.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spacewalls.app.data.local.PrefsStore
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val submitted: String = "",
    val results: List<Wallpaper> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val page: Int = 0,
    val error: String? = null,
    val recentSearches: List<String> = emptyList(),
    val searched: Boolean = false
)

class SearchViewModel(
    private val repository: WallpaperRepository,
    private val prefs: PrefsStore
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var job: Job? = null

    init {
        viewModelScope.launch {
            prefs.recentSearches.collect { terms ->
                _state.update { it.copy(recentSearches = terms) }
            }
        }
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }

    fun submit(query: String = _state.value.query) {
        val clean = query.trim()
        if (clean.length < 2) return
        _state.update { it.copy(query = clean, submitted = clean, results = emptyList(), page = 0) }
        viewModelScope.launch { prefs.addRecentSearch(clean) }
        search(reset = true)
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        search(reset = false)
    }

    fun clearRecent() {
        viewModelScope.launch { prefs.clearRecentSearches() }
    }

    private fun search(reset: Boolean) {
        val term = _state.value.submitted
        if (term.isBlank()) return
        job?.cancel()
        job = viewModelScope.launch {
            val nextPage = if (reset) 1 else _state.value.page + 1
            _state.update {
                it.copy(loading = reset, loadingMore = !reset, error = null, searched = true)
            }

            val outcome = runCatching {
                repository.loadPage(
                    queryKey = "search:${term.lowercase()}",
                    categoryKey = "search",
                    wikimediaQuery = term,
                    nasaQuery = term,
                    page = nextPage,
                    pageSize = 24
                )
            }.getOrNull()

            if (outcome == null) {
                val cached = repository.searchLocal(term)
                _state.update {
                    it.copy(
                        loading = false,
                        loadingMore = false,
                        results = if (reset) cached else it.results,
                        hasMore = false,
                        error = "Search failed — showing cached matches only."
                    )
                }
                return@launch
            }

            _state.update { current ->
                val merged = if (reset) {
                    outcome.items
                } else {
                    (current.results + outcome.items).distinctBy { it.id }
                }
                current.copy(
                    results = merged,
                    loading = false,
                    loadingMore = false,
                    hasMore = outcome.hasMore,
                    page = nextPage,
                    error = if (merged.isEmpty()) {
                        "No verified imagery for \"$term\". Try a broader term."
                    } else {
                        null
                    }
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SearchViewModel(container.repository, container.prefs) }
        }
    }
}
