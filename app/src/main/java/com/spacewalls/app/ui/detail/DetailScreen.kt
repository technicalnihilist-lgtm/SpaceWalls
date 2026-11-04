package com.spacewalls.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spacewalls.app.data.model.SpaceCatalog
import com.spacewalls.app.data.model.Wallpaper
import com.spacewalls.app.data.model.WallpaperTarget
import com.spacewalls.app.di.LocalAppContainer
import com.spacewalls.app.ui.components.FilterPill
import com.spacewalls.app.ui.components.GlassCard
import com.spacewalls.app.ui.components.InfoRow
import com.spacewalls.app.ui.components.NoticeBanner
import com.spacewalls.app.ui.components.SourceBadge
import com.spacewalls.app.ui.theme.SpaceColors
import com.spacewalls.app.ui.theme.SpaceIcons
import java.io.File

@Composable
fun DetailScreen(
    wallpaperId: String,
    onBack: () -> Unit,
    onOpenCategory: (String) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: DetailViewModel = viewModel(
        key = "detail_$wallpaperId",
        factory = DetailViewModel.factory(container, wallpaperId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var previewOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = SpaceColors.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val wallpaper = state.wallpaper
        if (wallpaper == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SpaceColors.CosmicBlue, strokeWidth = 2.dp)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(SpaceColors.SurfaceElevated)
                    .clickable { previewOpen = true }
            ) {
                AsyncImage(
                    model = wallpaper.localPath?.let { File(it) } ?: wallpaper.previewUrl,
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.55f),
                                0.28f to Color.Transparent,
                                0.75f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.75f)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RoundIconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = SpaceColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoundIconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                imageVector = if (wallpaper.isFavorite) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = "Favourite",
                                tint = if (wallpaper.isFavorite) SpaceColors.CosmicBlue else SpaceColors.TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        RoundIconButton(onClick = viewModel::share) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share",
                                tint = SpaceColors.TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SourceBadge(source = wallpaper.source.displayName)
                    FilterPill(
                        text = wallpaper.resolution,
                        selected = false,
                        onClick = {}
                    )
                    if (wallpaper.localPath != null) {
                        FilterPill(text = "Offline", selected = true, onClick = {})
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpaceColors.TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = wallpaper.creator,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpaceColors.Silver
                )

                val collectionName = SpaceCatalog.subcategory(wallpaper.collectionId)?.name
                    ?: SpaceCatalog.group(wallpaper.collectionId)?.name
                if (!collectionName.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = collectionName,
                        style = MaterialTheme.typography.labelLarge,
                        color = SpaceColors.CosmicBlue,
                        modifier = Modifier.clickable { onOpenCategory(wallpaper.collectionId) }
                    )
                }

                Spacer(Modifier.height(18.dp))

                DownloadRow(
                    wallpaper = wallpaper,
                    download = state.download,
                    onDownload = viewModel::download
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "SET AS WALLPAPER",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.TextTertiary
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WallpaperTargetButton(
                        label = "Home",
                        enabled = !state.applyingWallpaper,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.applyWallpaper(WallpaperTarget.HOME) }
                    WallpaperTargetButton(
                        label = "Lock",
                        enabled = !state.applyingWallpaper,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.applyWallpaper(WallpaperTarget.LOCK) }
                    WallpaperTargetButton(
                        label = "Both",
                        enabled = !state.applyingWallpaper,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.applyWallpaper(WallpaperTarget.BOTH) }
                }
                if (state.applyingWallpaper) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = SpaceColors.CosmicBlue,
                        trackColor = SpaceColors.Glass
                    )
                }

                if (wallpaper.rightsStatus == com.spacewalls.app.data.model.RightsStatus.THIRD_PARTY_CREDIT) {
                    Spacer(Modifier.height(14.dp))
                    NoticeBanner(
                        text = wallpaper.rightsNote
                            ?: "Credit names a partner institution — check their terms."
                    )
                }

                Spacer(Modifier.height(22.dp))
                Text(
                    text = "ABOUT THIS IMAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.TextTertiary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = wallpaper.description.ifBlank { "No description was published for this image." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = SpaceColors.TextSecondary
                )

                Spacer(Modifier.height(22.dp))
                CreditsCard(wallpaper = wallpaper, onOpenSource = viewModel::openSourcePage, onOpenLicense = viewModel::openLicensePage)

                Spacer(Modifier.height(28.dp))
            }
        }

        if (previewOpen) {
            FullScreenPreview(wallpaper = wallpaper, onDismiss = { previewOpen = false })
        }
    }
}

@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, SpaceColors.BorderStrong, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun DownloadRow(
    wallpaper: Wallpaper,
    download: DownloadUiState,
    onDownload: () -> Unit
) {
    val label = when (download) {
        is DownloadUiState.Running -> "Downloading ${download.progress}%"
        DownloadUiState.Done -> if (wallpaper.localPath != null) "Saved offline" else "Downloaded"
        DownloadUiState.Failed -> "Retry download"
        DownloadUiState.Idle -> "Download full resolution"
    }

    Column {
        Button(
            onClick = onDownload,
            enabled = download !is DownloadUiState.Running,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpaceColors.CosmicBlue,
                contentColor = Color.White,
                disabledContainerColor = SpaceColors.CosmicBlue.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = SpaceIcons.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
        if (download is DownloadUiState.Running) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { download.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = SpaceColors.CosmicBlue,
                trackColor = SpaceColors.Glass
            )
        }
    }
}

@Composable
private fun WallpaperTargetButton(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpaceColors.TextPrimary),
        modifier = modifier.height(44.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun CreditsCard(
    wallpaper: Wallpaper,
    onOpenSource: () -> Unit,
    onOpenLicense: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "IMAGE CREDITS",
            style = MaterialTheme.typography.labelSmall,
            color = SpaceColors.CosmicBlue
        )
        Spacer(Modifier.height(10.dp))

        InfoRow(label = "Title", value = wallpaper.title)
        InfoRow(label = "Creator / photographer", value = wallpaper.creator)
        if (wallpaper.credit.isNotBlank() && wallpaper.credit != wallpaper.creator) {
            InfoRow(label = "Credit line", value = wallpaper.credit)
        }
        InfoRow(label = "Original source", value = wallpaper.sourceName)
        InfoRow(label = "Licence", value = wallpaper.licenseName)
        InfoRow(label = "Licence terms", value = wallpaper.usageTerms)
        InfoRow(label = "Resolution", value = wallpaper.resolution)
        InfoRow(label = "Date", value = wallpaper.dateCreated ?: "")
        InfoRow(label = "Modifications", value = wallpaper.modifications)
        wallpaper.rightsNote?.let { InfoRow(label = "Rights note", value = it) }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LinkChip(text = "Image page", onClick = onOpenSource)
            LinkChip(text = "Licence", onClick = onOpenLicense)
        }
    }
}

@Composable
private fun LinkChip(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SpaceColors.Glass)
            .border(1.dp, SpaceColors.Border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = SpaceColors.Silver
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = SpaceIcons.OpenInNew,
            contentDescription = null,
            tint = SpaceColors.TextTertiary,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun FullScreenPreview(wallpaper: Wallpaper, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = wallpaper.localPath?.let { File(it) } ?: wallpaper.originalUrl,
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                RoundIconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close preview",
                        tint = SpaceColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = SpaceColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wallpaper.creator} · ${wallpaper.resolution}",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.2.sp),
                    color = SpaceColors.Silver
                )
            }
        }
    }
}
