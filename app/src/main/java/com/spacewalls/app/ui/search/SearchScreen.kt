package com.spacewalls.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.components.EmptyState
import com.spacewalls.app.ui.components.ScreenTopBar
import com.spacewalls.app.ui.components.WallpaperCard
import com.spacewalls.app.ui.theme.SpaceColors

@Composable
fun SearchScreen(
    onOpenWallpaper: (String) -> Unit,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember(state.results.size) {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.results.size - 4 && state.results.isNotEmpty()
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadMore() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceColors.Black)
    ) {
        ScreenTopBar(title = "Search", onBack = onBack)

        TextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = {
                Text(
                    text = "Galaxies, nebulae, planets, missions…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpaceColors.TextTertiary
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = SpaceColors.TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = SpaceColors.TextTertiary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.onQueryChange("") }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboard?.hide()
                viewModel.submit()
            }),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SpaceColors.Glass,
                unfocusedContainerColor = SpaceColors.Glass,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SpaceColors.TextPrimary,
                unfocusedTextColor = SpaceColors.TextPrimary,
                cursorColor = SpaceColors.CosmicBlue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .border(1.dp, SpaceColors.Border, RoundedCornerShape(16.dp))
        )

        if (!state.searched && state.recentSearches.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RECENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.TextTertiary
                )
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelLarge,
                    color = SpaceColors.CosmicBlue,
                    modifier = Modifier.clickable { viewModel.clearRecent() }
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.recentSearches) { term ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SpaceColors.Glass)
                            .border(1.dp, SpaceColors.Border, RoundedCornerShape(50))
                            .clickable {
                                viewModel.onQueryChange(term)
                                viewModel.submit(term)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = term,
                            style = MaterialTheme.typography.labelLarge,
                            color = SpaceColors.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .size(24.dp),
                    color = SpaceColors.CosmicBlue,
                    strokeWidth = 2.dp
                )
            }
            state.error != null && state.results.isEmpty() -> EmptyState(
                title = if (state.searched) "No results" else "Search the sky",
                message = state.error ?: ""
            )
            !state.searched -> EmptyState(
                title = "Search the sky",
                message = "Try “Orion Nebula”, “Saturn”, “aurora” or “Apollo”."
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.results, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.id) },
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
            }
        }
    }
}
