package com.example.adrecommend.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.adrecommend.data.AdRepository
import com.example.adrecommend.data.LocalAdRepository
import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdInteractionState
import com.example.adrecommend.model.AdItem
import com.example.adrecommend.state.FeedUiState
import com.example.adrecommend.ui.detail.AdDetailScreen
import com.example.adrecommend.ui.feed.AdFeedScreen

@Composable
fun AdRecommendApp(
    repository: AdRepository? = null
) {
    val context = LocalContext.current
    val resolvedRepository = remember(repository, context) {
        repository ?: LocalAdRepository(context)
    }
    val channels = remember(resolvedRepository) { resolvedRepository.getChannels() }
    var selectedChannelId by rememberSaveable { mutableStateOf(AdChannel.Featured.id) }
    var selectedAdId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val interactions = remember { mutableStateMapOf<String, AdInteractionState>() }
    val selectedChannel = channels.firstOrNull { it.id == selectedChannelId } ?: AdChannel.Featured
    val ads = remember(resolvedRepository, selectedChannel) {
        resolvedRepository.getAds(selectedChannel)
    }
    val filteredAds = remember(ads, selectedTag, searchQuery) {
        ads.filterByTagAndSearch(selectedTag, searchQuery)
    }
    val selectedAd = selectedAdId?.let { adId ->
        channels.asSequence()
            .flatMap { channel -> resolvedRepository.getAds(channel).asSequence() }
            .firstOrNull { ad -> ad.id == adId }
    }

    fun interactionOf(adId: String): AdInteractionState {
        return interactions[adId] ?: AdInteractionState()
    }

    fun updateInteraction(
        adId: String,
        transform: (AdInteractionState) -> AdInteractionState
    ) {
        interactions[adId] = transform(interactionOf(adId))
    }

    if (selectedAd != null) {
        BackHandler {
            selectedAdId = null
        }
        AdDetailScreen(
            ad = selectedAd,
            interaction = interactionOf(selectedAd.id),
            onBack = { selectedAdId = null },
            onLike = {
                updateInteraction(selectedAd.id) { state ->
                    state.copy(liked = !state.liked)
                }
            },
            onFavorite = {
                updateInteraction(selectedAd.id) { state ->
                    state.copy(favorited = !state.favorited)
                }
            },
            onShare = {
                updateInteraction(selectedAd.id) { state ->
                    state.copy(sharedCount = state.sharedCount + 1)
                }
            }
        )
    } else {
        AdFeedScreen(
            state = FeedUiState(
                channels = channels,
                selectedChannel = selectedChannel,
                ads = filteredAds,
                interactions = interactions.toMap(),
                selectedTag = selectedTag,
                searchQuery = searchQuery,
                errorMessage = resolvedRepository.getLoadErrorMessage()
            ),
            onChannelSelected = { channel ->
                selectedChannelId = channel.id
            },
            onSearchQueryChanged = { query ->
                searchQuery = query
            },
            onTagSelected = { tag ->
                selectedTag = tag
            },
            onClearFilters = {
                selectedTag = null
                searchQuery = ""
            },
            onAdSelected = { ad ->
                updateInteraction(ad.id) { state ->
                    state.copy(clickCount = state.clickCount + 1)
                }
                selectedAdId = ad.id
            },
            onLike = { ad ->
                updateInteraction(ad.id) { state ->
                    state.copy(liked = !state.liked)
                }
            },
            onFavorite = { ad ->
                updateInteraction(ad.id) { state ->
                    state.copy(favorited = !state.favorited)
                }
            },
            onShare = { ad ->
                updateInteraction(ad.id) { state ->
                    state.copy(sharedCount = state.sharedCount + 1)
                }
            }
        )
    }
}

private fun List<AdItem>.filterByTagAndSearch(
    selectedTag: String?,
    searchQuery: String
): List<AdItem> {
    val normalizedQuery = searchQuery.trim()
    return filter { ad ->
        val matchesTag = selectedTag == null || ad.aiTags.any { tag -> tag == selectedTag }
        val matchesSearch = normalizedQuery.isBlank() || ad.matchesSearch(normalizedQuery)
        matchesTag && matchesSearch
    }
}

private fun AdItem.matchesSearch(query: String): Boolean {
    return listOf(
        title,
        brand,
        description,
        category,
        aiSummary,
        aiTags.joinToString(" "),
        audience.joinToString(" "),
        sellingPoints.joinToString(" ")
    ).any { field ->
        field.contains(query, ignoreCase = true)
    }
}
