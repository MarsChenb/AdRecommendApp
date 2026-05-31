package com.example.adrecommend.model

data class AdInteractionState(
    val liked: Boolean = false,
    val favorited: Boolean = false,
    val sharedCount: Int = 0,
    val clickCount: Int = 0,
    val exposureCount: Int = 0
)
