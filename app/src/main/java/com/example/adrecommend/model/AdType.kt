package com.example.adrecommend.model

enum class AdType(val id: String, val displayName: String) {
    LargeImage("large_image", "Large Image"),
    SmallImage("small_image", "Small Image"),
    Video("video", "Video");

    companion object
}
