package com.spacewalls.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.components.EmptyState
import com.spacewalls.app.ui.components.NoticeBanner
import com.spacewalls.app.ui.components.SectionHeader
import com.spacewalls.app.ui.components.ShimmerBox
import com.spacewalls.app.ui.components.WallpaperCard
import com.spacewalls.app.ui.theme.SpaceColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenWallpaper: (String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenExplore: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Live-first: every visit to Home re-fetches from Wikimedia and NASA.
    LaunchedEffect(Unit) { viewModel.refreshIfStale() }

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceColors.Black)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item { HomeHeader() }
            item { SearchEntry(onClick = onOpenSearch) }

            state.notice?.let { notice ->
                item {
                    Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                        NoticeBanner(text = notice)
                    }
                }
            }

            if (state.loading) {
                item { LoadingSkeleton() }
            }

            state.error?.let { message ->
                item {
                    EmptyState(title = "Nothing loaded yet", message = message)
                }
            }

            state.daily?.let { daily ->
                item {
                    DailyHero(
                        wallpaper = daily,
                        onClick = { onOpenWallpaper(daily.id) }
                    )
                }
            }

            items(state.sections) { section ->
                Column(modifier = Modifier.padding(bottom = 18.dp)) {
                    SectionHeader(
                        title = section.title,
                        subtitle = section.subtitle,
                        actionLabel = "See all",
                        onAction = { onOpenCategory(section.categoryId) },
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(section.items, key = { it.id }) { wallpaper ->
                            WallpaperCard(
                                wallpaper = wallpaper,
                                onClick = { onOpenWallpaper(wallpaper.id) },
                                modifier = Modifier.width(158.dp)
                            )
                        }
                    }
                }
            }

            if (state.recentlyAdded.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(bottom = 18.dp)) {
                        SectionHeader(
                            title = "Recently added",
                            subtitle = "Fresh in your library",
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.recentlyAdded, key = { it.id }) { wallpaper ->
                                WallpaperCard(
                                    wallpaper = wallpaper,
                                    onClick = { onOpenWallpaper(wallpaper.id) },
                                    modifier = Modifier.width(140.dp),
                                    aspectRatio = 0.78f,
                                    compact = true
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Live imagery from Wikimedia Commons and the NASA Image and Video Library · " +
                        "updated ${timeLabel(state.lastUpdated)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.TextTertiary,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, SpaceColors.Border, RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenExplore)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Browse all six collections",
                        style = MaterialTheme.typography.titleMedium,
                        color = SpaceColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleLarge,
                        color = SpaceColors.CosmicBlue
                    )
                }
            }
        }
    }
}

private fun timeLabel(timestamp: Long): String {
    if (timestamp <= 0L) return "on open"
    return SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SpaceColors.CosmicBlue.copy(alpha = 0.16f))
                    .border(1.dp, SpaceColors.Silver.copy(alpha = 0.5f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SpaceColors.Silver)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "SPACEWALLS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp
                ),
                color = SpaceColors.TextPrimary
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Premium space & galaxy wallpapers",
            style = MaterialTheme.typography.bodyMedium,
            color = SpaceColors.TextTertiary
        )
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SpaceColors.Glass)
            .border(1.dp, SpaceColors.Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = SpaceColors.TextTertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Search galaxies, nebulae, planets…",
            style = MaterialTheme.typography.bodyLarge,
            color = SpaceColors.TextTertiary
        )
    }
}

@Composable
private fun DailyHero(wallpaper: Wallpaper, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)) {
        Text(
            text = "DAILY SPACE WALLPAPER",
            style = MaterialTheme.typography.labelSmall,
            color = SpaceColors.CosmicBlue,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .aspectRatio(1.45f)
                .clip(RoundedCornerShape(24.dp))
                .background(SpaceColors.SurfaceElevated)
                .border(1.dp, SpaceColors.Border, RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model = wallpaper.previewUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.88f)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = SpaceColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${wallpaper.creator} · ${wallpaper.resolution}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpaceColors.Silver.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LoadingSkeleton() {
    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.45f),
            cornerRadius = 24
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .width(158.dp)
                        .aspectRatio(0.72f),
                    cornerRadius = 20
                )
            }
        }
    }
}
