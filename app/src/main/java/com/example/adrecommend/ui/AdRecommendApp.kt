package com.example.adrecommend.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.adrecommend.data.AdRepository
import com.example.adrecommend.data.LocalAdRepository
import com.example.adrecommend.data.PreviewAdRepository
import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.state.FeedUiState
import com.example.adrecommend.ui.feed.AdFeedScreen
import androidx.compose.ui.platform.LocalContext

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
    val selectedChannel = channels.firstOrNull { it.id == selectedChannelId } ?: AdChannel.Featured
    val ads = remember(resolvedRepository, selectedChannel) {
        resolvedRepository.getAds(selectedChannel)
    }

    AdFeedScreen(
        state = FeedUiState(
            channels = channels,
            selectedChannel = selectedChannel,
            ads = ads,
            errorMessage = resolvedRepository.getLoadErrorMessage()
        ),
        onChannelSelected = { channel ->
            selectedChannelId = channel.id
        }
    )
}
