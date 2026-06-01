package com.example.adrecommend.ui.feed

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adrecommend.data.PreviewAdRepository
import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdInteractionState
import com.example.adrecommend.model.AdItem
import com.example.adrecommend.model.AdType
import com.example.adrecommend.state.FeedUiState
import com.example.adrecommend.ui.theme.AdRecommendAppTheme

@Composable
fun AdFeedScreen(
    state: FeedUiState,
    onChannelSelected: (AdChannel) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onClearFilters: () -> Unit,
    onAdSelected: (AdItem) -> Unit,
    onLike: (AdItem) -> Unit,
    onFavorite: (AdItem) -> Unit,
    onShare: (AdItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeedHeader(
                    selectedChannel = state.selectedChannel,
                    visibleCount = state.ads.size,
                    searchQuery = state.searchQuery,
                    selectedTag = state.selectedTag,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onClearFilters = onClearFilters
                )
            }

            item {
                ChannelTabs(
                    channels = state.channels,
                    selectedChannel = state.selectedChannel,
                    onChannelSelected = onChannelSelected
                )
            }

            state.errorMessage?.let { message ->
                item {
                    ErrorBanner(message = message)
                }
            }

            if (state.ads.isEmpty()) {
                item {
                    EmptyResult(
                        selectedTag = state.selectedTag,
                        searchQuery = state.searchQuery,
                        onClearFilters = onClearFilters
                    )
                }
            }

            items(
                items = state.ads,
                key = { ad -> ad.id }
            ) { ad ->
                AdCard(
                    ad = ad,
                    interaction = state.interactions[ad.id] ?: AdInteractionState(),
                    onClick = { onAdSelected(ad) },
                    onLike = { onLike(ad) },
                    onFavorite = { onFavorite(ad) },
                    onShare = { onShare(ad) },
                    onTagSelected = onTagSelected,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedHeader(
    selectedChannel: AdChannel,
    visibleCount: Int,
    searchQuery: String,
    selectedTag: String?,
    onSearchQueryChanged: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ad Intelligence Feed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${selectedChannel.displayName} · $visibleCount AI enriched ads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BadgeLabel(text = "AI")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            label = {
                Text(text = "Search")
            },
            placeholder = {
                Text(
                    text = "Try: 通勤、健身、外卖、兼职",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
            )
        )

        if (selectedTag != null || searchQuery.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeFilterText(selectedTag, searchQuery),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = onClearFilters) {
                    Text(text = "Clear")
                }
            }
        }
    }
}

private fun activeFilterText(
    selectedTag: String?,
    searchQuery: String
): String {
    return buildList {
        if (searchQuery.isNotBlank()) {
            add("Search: ${searchQuery.trim()}")
        }
        if (selectedTag != null) {
            add("Tag: $selectedTag")
        }
    }.joinToString(" · ")
}

@Composable
private fun EmptyResult(
    selectedTag: String?,
    searchQuery: String,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "No matching ads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = activeFilterText(selectedTag, searchQuery).ifBlank {
                    "Try another channel or keyword."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onClearFilters) {
                Text(text = "Clear filters")
            }
        }
    }
}

@Composable
private fun ChannelTabs(
    channels: List<AdChannel>,
    selectedChannel: AdChannel,
    onChannelSelected: (AdChannel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        channels.forEach { channel ->
            FilterChip(
                selected = channel == selectedChannel,
                onClick = { onChannelSelected(channel) },
                label = {
                    Text(
                        text = channel.displayName,
                        maxLines = 1
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = channel == selectedChannel,
                    borderColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                )
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun AdCard(
    ad: AdItem,
    interaction: AdInteractionState,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (ad.materialType) {
        AdType.LargeImage -> LargeImageAdCard(ad, interaction, onClick, onLike, onFavorite, onShare, onTagSelected, modifier)
        AdType.SmallImage -> SmallImageAdCard(ad, interaction, onClick, onLike, onFavorite, onShare, onTagSelected, modifier)
        AdType.Video -> VideoAdCard(ad, interaction, onClick, onLike, onFavorite, onShare, onTagSelected, modifier)
    }
}

@Composable
private fun LargeImageAdCard(
    ad: AdItem,
    interaction: AdInteractionState,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CardFrame(modifier = modifier) {
        Box(
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            RemoteImage(
                imageUrl = ad.imageUrl,
                contentDescription = ad.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                fallbackLabel = ad.category,
                contentScale = ContentScale.Fit
            )
            GradientScrim()
            SponsoredBadge(modifier = Modifier.align(Alignment.TopStart))
            Text(
                text = ad.category,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Column {
                BrandLine(ad = ad)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                SummaryText(text = ad.aiSummary, maxLines = 3)
            }
            Spacer(modifier = Modifier.height(14.dp))
            TagRow(tags = ad.aiTags.take(3), onTagSelected = onTagSelected)
            Spacer(modifier = Modifier.height(14.dp))
            InteractionRow(interaction, onLike, onFavorite, onShare)
        }
    }
}

@Composable
private fun SmallImageAdCard(
    ad: AdItem,
    interaction: AdInteractionState,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CardFrame(modifier = modifier) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            RemoteImage(
                imageUrl = ad.imageUrl,
                contentDescription = ad.title,
                modifier = Modifier
                    .size(112.dp)
                    .clickable(onClick = onClick)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                fallbackLabel = "AD",
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Column {
                    BrandLine(ad = ad)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SummaryText(text = ad.aiSummary, maxLines = 3)
                }
                Spacer(modifier = Modifier.height(10.dp))
                TagRow(tags = ad.aiTags.take(2), onTagSelected = onTagSelected)
                Spacer(modifier = Modifier.height(10.dp))
                InteractionRow(interaction, onLike, onFavorite, onShare)
            }
        }
    }
}

@Composable
private fun VideoAdCard(
    ad: AdItem,
    interaction: AdInteractionState,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CardFrame(modifier = modifier) {
        Box(
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            RemoteImage(
                imageUrl = ad.imageUrl,
                contentDescription = ad.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                fallbackLabel = ad.category,
                contentScale = ContentScale.Fit
            )
            GradientScrim()
            SponsoredBadge(modifier = Modifier.align(Alignment.TopStart))
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.56f)
            ) {
                Text(
                    text = "Play",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.58f)
            ) {
                Text(
                    text = "Video Ad",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Column {
                BrandLine(ad = ad)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                SummaryText(text = ad.aiSummary, maxLines = 3)
            }
            Spacer(modifier = Modifier.height(14.dp))
            TagRow(tags = ad.aiTags.take(3), onTagSelected = onTagSelected)
            Spacer(modifier = Modifier.height(14.dp))
            InteractionRow(interaction, onLike, onFavorite, onShare)
        }
    }
}

@Composable
private fun CardFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        content()
    }
}

@Composable
private fun BrandLine(ad: AdItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ad.brand,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = ad.priceText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun InteractionRow(
    interaction: AdInteractionState,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactAction(
            text = if (interaction.liked) "Liked" else "Like",
            active = interaction.liked,
            onClick = onLike
        )
        CompactAction(
            text = if (interaction.favorited) "Saved" else "Save",
            active = interaction.favorited,
            onClick = onFavorite
        )
        CompactAction(
            text = "Share ${interaction.sharedCount}",
            active = false,
            onClick = onShare
        )
    }
}

@Composable
private fun CompactAction(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (active) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f)
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
    }
}

@Composable
private fun SummaryText(
    text: String,
    maxLines: Int
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TagRow(
    tags: List<String>,
    onTagSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Surface(
                modifier = Modifier.clickable { onTagSelected(tag) },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BadgeLabel(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SponsoredBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(14.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.54f)
    ) {
        Text(
            text = "Sponsored",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

@Composable
private fun GradientScrim() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.14f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.52f)
                    )
                )
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun AdFeedScreenPreview() {
    val repository = PreviewAdRepository
    AdRecommendAppTheme {
        AdFeedScreen(
            state = FeedUiState(
                channels = repository.getChannels(),
                selectedChannel = AdChannel.Featured,
                ads = repository.getAds(AdChannel.Featured)
            ),
            onChannelSelected = {},
            onSearchQueryChanged = {},
            onTagSelected = {},
            onClearFilters = {},
            onAdSelected = {},
            onLike = {},
            onFavorite = {},
            onShare = {}
        )
    }
}
