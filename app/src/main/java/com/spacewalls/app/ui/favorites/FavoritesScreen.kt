package com.spacewalls.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.components.EmptyState
import com.spacewalls.app.ui.components.SectionHeader
import com.spacewalls.app.ui.components.WallpaperCard
import com.spacewalls.app.ui.theme.SpaceColors

@Composable
fun FavoritesScreen(onOpenWallpaper: (String) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceColors.Black),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 6.dp)
            ) {
                Text(
                    text = "SAVED",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.CosmicBlue
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Your collection",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpaceColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${state.favorites.size} favourites · ${state.downloads.size} downloaded",
                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.2.sp),
                    color = SpaceColors.TextTertiary
                )
            }
        }

        if (state.favorites.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    title = "No favourites yet",
                    message = "Tap the heart on any wallpaper to keep it here — it survives restarts."
                )
            }
        }

        items(state.favorites, key = { it.id }) { wallpaper ->
            WallpaperCard(
                wallpaper = wallpaper,
                onClick = { onOpenWallpaper(wallpaper.id) },
                onFavoriteToggle = { viewModel.toggleFavorite(wallpaper) },
                aspectRatio = 0.68f
            )
        }

        if (state.downloads.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    SectionHeader(
                        title = "Offline downloads",
                        subtitle = "Stored on this device, no network needed"
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.downloads, key = { it.id }) { wallpaper ->
                            WallpaperCard(
                                wallpaper = wallpaper,
                                onClick = { onOpenWallpaper(wallpaper.id) },
                                modifier = Modifier.height(190.dp),
                                aspectRatio = 0.74f,
                                compact = true
                            )
                        }
                    }
                }
            }
        }

        if (state.recentlyViewed.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    SectionHeader(
                        title = "Recently viewed",
                        subtitle = "Where you left off"
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            items(state.recentlyViewed, key = { "recent_${it.id}" }) { wallpaper ->
                WallpaperCard(
                    wallpaper = wallpaper,
                    onClick = { onOpenWallpaper(wallpaper.id) },
                    aspectRatio = 0.68f,
                    compact = true
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.height(12.dp))
        }
    }
}
