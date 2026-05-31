package com.example.adrecommend.state

import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdInteractionState
import com.example.adrecommend.model.AdItem

data class FeedUiState(
    val channels: List<AdChannel> = emptyList(),
    val selectedChannel: AdChannel = AdChannel.Featured,
    val ads: List<AdItem> = emptyList(),
    val interactions: Map<String, AdInteractionState> = emptyMap(),
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
