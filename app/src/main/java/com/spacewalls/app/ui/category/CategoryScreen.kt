package com.spacewalls.app.ui.category

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.components.EmptyState
import com.spacewalls.app.ui.components.FilterPill
import com.spacewalls.app.ui.components.NoticeBanner
import com.spacewalls.app.ui.components.ScreenTopBar
import com.spacewalls.app.ui.components.ShimmerBox
import com.spacewalls.app.ui.components.WallpaperCard
import com.spacewalls.app.ui.theme.SpaceColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String,
    onOpenWallpaper: (String) -> Unit,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: CategoryViewModel = viewModel(
        key = "category_$categoryId",
        factory = CategoryViewModel.factory(container, categoryId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember(state.items.size) {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.items.size - 4 && state.items.isNotEmpty()
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceColors.Black)
    ) {
        ScreenTopBar(
            title = state.title.ifBlank { "Collection" },
            subtitle = state.blurb,
            onBack = onBack
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            items(SourceFilter.entries) { filter ->
                FilterPill(
                    text = filter.label,
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) }
                )
            }
        }

        state.notice?.let { notice ->
            Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                NoticeBanner(text = notice)
            }
        }

        PullToRefreshBox(
            isRefreshing = state.loading && state.items.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.loading && state.items.isEmpty() -> GridSkeleton()
                state.error != null && state.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        EmptyState(
                            title = "Nothing to show",
                            message = state.error ?: "Try again in a moment."
                        )
                    }
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.visible, key = { it.id }) { wallpaper ->
                        WallpaperCard(
                            wallpaper = wallpaper,
                            onClick = { onOpenWallpaper(wallpaper.id) },
                            onFavoriteToggle = null,
                            aspectRatio = 0.68f
                        )
                    }
                    if (state.loadingMore) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SpaceColors.CosmicBlue,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    if (!state.hasMore && state.visible.isNotEmpty()) {
                        item {
                            Text(
                                text = "End of collection",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpaceColors.TextTertiary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridSkeleton() {
    Column(modifier = Modifier.padding(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(230.dp),
                    cornerRadius = 20
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(230.dp),
                    cornerRadius = 20
                )
            }
        }
    }
}
