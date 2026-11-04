package com.spacewalls.app.ui.explore

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacewalls.app.data.model.SpaceCatalog
import com.spacewalls.app.ui.components.SectionHeader
import com.spacewalls.app.ui.theme.SpaceColors

@Composable
fun ExploreScreen(
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit
) {
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
                    text = "EXPLORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpaceColors.CosmicBlue
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Six collections,\nforty-three skies",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SpaceColors.TextPrimary
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SpaceColors.Glass)
                        .border(1.dp, SpaceColors.Border, RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenSearch)
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
                        text = "Search the whole library",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SpaceColors.TextTertiary
                    )
                }
            }
        }

        items(SpaceCatalog.groups) { group ->
            Column(modifier = Modifier.padding(bottom = 22.dp)) {
                SectionHeader(
                    title = group.name,
                    subtitle = group.blurb,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(group.items) { item ->
                        Box(
                            modifier = Modifier
                                .width(168.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SpaceColors.Glass)
                                .border(1.dp, SpaceColors.Border, RoundedCornerShape(18.dp))
                                .clickable { onOpenCategory(item.id) }
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = SpaceColors.TextPrimary
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = item.blurb,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SpaceColors.TextTertiary,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Every collection is a live query against Wikimedia Commons and NASA — " +
                    "nothing is bundled in the app.",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                color = SpaceColors.TextTertiary,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }
    }
}
