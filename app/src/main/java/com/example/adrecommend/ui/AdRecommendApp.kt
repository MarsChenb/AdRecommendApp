package com.example.adrecommend.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.adrecommend.data.AdRepository
import com.example.adrecommend.data.PreviewAdRepository
import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.state.FeedUiState
import com.example.adrecommend.ui.feed.AdFeedScreen

@Composable
fun AdRecommendApp(
    repository: AdRepository = PreviewAdRepository
) {
    val channels = remember { repository.getChannels() }
    var selectedChannelId by rememberSaveable { mutableStateOf(AdChannel.Featured.id) }
    val selectedChannel = channels.firstOrNull { it.id == selectedChannelId } ?: AdChannel.Featured
    val ads = remember(selectedChannel) { repository.getAds(selectedChannel) }

    AdFeedScreen(
        state = FeedUiState(
            channels = channels,
            selectedChannel = selectedChannel,
            ads = ads
        ),
        onChannelSelected = { channel ->
            selectedChannelId = channel.id
        }
    )
}
