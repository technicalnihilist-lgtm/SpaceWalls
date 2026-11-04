package com.spacewalls.app.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.model.WallpaperTarget
import com.spacewalls.app.data.repo.DownloadRepository
import com.spacewalls.app.data.repo.WallpaperRepository
import com.spacewalls.app.data.wallpaper.WallpaperSetter
import com.spacewalls.app.data.work.DownloadWorker
import com.spacewalls.app.data.work.WorkScheduler
import com.spacewalls.app.di.AppContainer
import com.spacewalls.app.util.ShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DownloadUiState {
    data object Idle : DownloadUiState
    data class Running(val progress: Int) : DownloadUiState
    data object Done : DownloadUiState
    data object Failed : DownloadUiState
}

data class DetailUiState(
    val wallpaper: Wallpaper? = null,
    val download: DownloadUiState = DownloadUiState.Idle,
    val applyingWallpaper: Boolean = false,
    val message: String? = null
)

class DetailViewModel(
    private val context: Context,
    private val repository: WallpaperRepository,
    private val downloadRepository: DownloadRepository,
    private val wallpaperSetter: WallpaperSetter,
    private val wallpaperId: String
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWallpaper(wallpaperId).collect { wallpaper ->
                _state.update { current ->
                    current.copy(
                        wallpaper = wallpaper,
                        download = if (wallpaper?.localPath != null) {
                            DownloadUiState.Done
                        } else {
                            current.download
                        }
                    )
                }
            }
        }
        viewModelScope.launch { repository.markViewed(wallpaperId) }
        observeDownload()
    }

    private fun observeDownload() {
        viewModelScope.launch {
            WorkScheduler.observeDownload(context, wallpaperId).collect { infos ->
                val info: WorkInfo = infos.firstOrNull() ?: return@collect
                when (info.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> {
                        val progress = info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
                        _state.update { it.copy(download = DownloadUiState.Running(progress)) }
                    }
                    WorkInfo.State.SUCCEEDED -> _state.update {
                        it.copy(download = DownloadUiState.Done, message = "Saved to your gallery")
                    }
                    WorkInfo.State.FAILED -> _state.update {
                        it.copy(download = DownloadUiState.Failed, message = "Download failed")
                    }
                    else -> Unit
                }
            }
        }
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            repository.setFavorite(wallpaper.id, !wallpaper.isFavorite)
            _state.update {
                it.copy(
                    message = if (wallpaper.isFavorite) "Removed from favourites" else "Added to favourites"
                )
            }
        }
    }

    fun download() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.download is DownloadUiState.Running) return
        if (downloadRepository.cachedFile(wallpaper) != null) {
            _state.update { it.copy(download = DownloadUiState.Done, message = "Already downloaded") }
            return
        }
        WorkScheduler.enqueueDownload(context, wallpaper.id)
        _state.update { it.copy(download = DownloadUiState.Running(0)) }
    }

    fun applyWallpaper(target: WallpaperTarget) {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.applyingWallpaper) return
        viewModelScope.launch {
            _state.update { it.copy(applyingWallpaper = true) }
            val result = wallpaperSetter.apply(wallpaper, target)
            val label = when (target) {
                WallpaperTarget.HOME -> "home screen"
                WallpaperTarget.LOCK -> "lock screen"
                WallpaperTarget.BOTH -> "home and lock screen"
            }
            _state.update {
                it.copy(
                    applyingWallpaper = false,
                    message = result.fold(
                        onSuccess = { "Wallpaper set on your $label" },
                        onFailure = { "Could not set wallpaper: ${it.message ?: "unknown error"}" }
                    )
                )
            }
        }
    }

    fun share() {
        _state.value.wallpaper?.let { ShareHelper.share(context, it) }
    }

    fun openSourcePage() {
        _state.value.wallpaper?.let { ShareHelper.openUrl(context, it.sourcePageUrl) }
    }

    fun openLicensePage() {
        _state.value.wallpaper?.let { ShareHelper.openUrl(context, it.licenseUrl) }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    companion object {
        fun factory(container: AppContainer, wallpaperId: String) = viewModelFactory {
            initializer {
                DetailViewModel(
                    context = container.appContext,
                    repository = container.repository,
                    downloadRepository = container.downloadRepository,
                    wallpaperSetter = container.wallpaperSetter,
                    wallpaperId = wallpaperId
                )
            }
        }
    }
}
