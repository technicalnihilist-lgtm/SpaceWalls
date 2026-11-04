package com.spacewalls.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeSectionUi(
    val key: String,
    val title: String,
    val subtitle: String,
    val categoryId: String,
    val items: List<Wallpaper>
)

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val daily: Wallpaper? = null,
    val sections: List<HomeSectionUi> = emptyList(),
    val recentlyAdded: List<Wallpaper> = emptyList(),
    val notice: String? = null,
    val error: String? = null,
    val lastUpdated: Long = 0L
)

class HomeViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private data class SectionDef(
        val key: String,
        val title: String,
        val subtitle: String,
        val categoryId: String,
        val wikimediaQuery: String,
        val nasaQuery: String
    )

    private val definitions = listOf(
        SectionDef(
            "featured", "Featured cosmic wallpapers", "Pulled live from the archives",
            "collections", "nebula galaxy hubble", "nebula galaxy"
        ),
        SectionDef(
            "trending", "Trending galaxies", "Spirals, ellipticals and clusters",
            "galaxies", "spiral galaxy", "galaxy"
        ),
        SectionDef(
            "planets", "Popular planets", "Real spacecraft imagery",
            "planets", "planet solar system", "planet"
        ),
        SectionDef(
            "nebulae", "Nebula collection", "Where stars are born",
            "nebulae", "emission nebula", "nebula"
        ),
        SectionDef(
            "deepspace", "Deep space", "Emptiness, stars and distance",
            "collections", "deep space star field", "deep space"
        ),
        SectionDef(
            "exploration", "Space exploration", "Humans and machines outward",
            "exploration", "astronaut rocket launch", "rocket launch"
        )
    )

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.observeRecentlyAdded(14).collect { items ->
                _state.update { it.copy(recentlyAdded = items) }
            }
        }
        load(isRefresh = false)
    }

    fun refresh() = load(isRefresh = true)

    /** Live-first, but avoids hammering the APIs when the user bounces back. */
    fun refreshIfStale(maxAgeMs: Long = 30_000L) {
        val last = _state.value.lastUpdated
        if (System.currentTimeMillis() - last > maxAgeMs) {
            load(isRefresh = _state.value.sections.isNotEmpty())
        }
    }

    private fun load(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(loading = it.sections.isEmpty(), refreshing = isRefresh, error = null)
            }

            val notices = mutableListOf<String>()
            val sections = coroutineScope {
                definitions.map { def ->
                    async {
                        val outcome = runCatching {
                            repository.loadPage(
                                queryKey = def.key,
                                categoryKey = def.categoryId,
                                wikimediaQuery = def.wikimediaQuery,
                                nasaQuery = def.nasaQuery,
                                page = 1,
                                pageSize = 14
                            )
                        }.getOrNull()

                        outcome?.warnings?.let { notices += it }
                        HomeSectionUi(
                            key = def.key,
                            title = def.title,
                            subtitle = def.subtitle,
                            categoryId = def.categoryId,
                            items = outcome?.items.orEmpty()
                        )
                    }
                }.awaitAll()
            }

            val daily = runCatching { repository.ensureDailyWallpaper() }.getOrNull()
            val usable = sections.filter { it.items.isNotEmpty() }

            _state.update {
                it.copy(
                    loading = false,
                    refreshing = false,
                    daily = daily,
                    sections = usable,
                    notice = notices.distinct().takeIf { list -> list.isNotEmpty() }?.joinToString(" "),
                    error = if (usable.isEmpty()) {
                        "Could not reach Wikimedia Commons or NASA. Check your connection and pull to refresh."
                    } else {
                        null
                    },
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HomeViewModel(container.repository) }
        }
    }
}
