package com.spacewalls.app.ui.credits

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.components.EmptyState
import com.spacewalls.app.ui.components.GlassCard
import com.spacewalls.app.ui.components.NoticeBanner
import com.spacewalls.app.ui.components.ScreenTopBar
import com.spacewalls.app.ui.theme.SpaceColors
import com.spacewalls.app.util.ShareHelper

@Composable
fun CreditsScreen(onOpenWallpaper: (String) -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: CreditsViewModel = viewModel(factory = CreditsViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceColors.Black),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "CREDITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.CosmicBlue
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Attribution, in full",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpaceColors.TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Every image in this library is credited to its creator with its licence, " +
                        "source page and modification status. ${state.total} images catalogued.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpaceColors.TextTertiary
                )
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                Text(
                    text = "HOW IMAGES ARE VERIFIED",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.CosmicBlue
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "SpaceWalls only offers a wallpaper when the licence is machine-readable " +
                        "and recognised. Wikimedia Commons files need a public-domain or CC BY / CC BY-SA " +
                        "licence URL plus a named creator; files flagged with usage restrictions are " +
                        "excluded. NASA items need an attributable NASA centre and no explicit " +
                        "third-party copyright notice. Anything unverifiable never reaches the gallery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpaceColors.TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceLink(
                        label = "Wikimedia Commons",
                        onClick = {
                            ShareHelper.openUrl(context, "https://commons.wikimedia.org/")
                        }
                    )
                    SourceLink(
                        label = "NASA Images",
                        onClick = {
                            ShareHelper.openUrl(context, "https://images.nasa.gov/")
                        }
                    )
                }
            }
        }

        item {
            Box(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                NoticeBanner(
                    text = "NASA content is generally not copyrighted, but individual items may " +
                        "include third-party material. NASA does not endorse this app.",
                    accent = SpaceColors.Silver
                )
            }
        }

        items(state.filtered, key = { it.id }) { wallpaper ->
            CreditRow(
                wallpaper = wallpaper,
                onClick = { onOpenWallpaper(wallpaper.id) },
                onOpenLicense = { ShareHelper.openUrl(context, wallpaper.licenseUrl) }
            )
        }

        if (!state.loading && state.filtered.isEmpty()) {
            item {
                EmptyState(
                    title = "No credited images yet",
                    message = "Browse a collection and every image you see will be listed here with its credits."
                )
            }
        }
    }
}

@Composable
private fun SourceLink(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SpaceColors.Glass)
            .border(1.dp, SpaceColors.Border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = SpaceColors.Silver
        )
    }
}

@Composable
private fun CreditRow(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    onOpenLicense: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = wallpaper.thumbUrl,
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SpaceColors.SurfaceElevated)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.titleMedium,
                color = SpaceColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = wallpaper.creator,
                style = MaterialTheme.typography.bodyMedium,
                color = SpaceColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${wallpaper.licenseName} · ${wallpaper.sourceName}",
                style = MaterialTheme.typography.labelSmall,
                color = SpaceColors.CosmicBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onOpenLicense)
            )
        }
    }
}
