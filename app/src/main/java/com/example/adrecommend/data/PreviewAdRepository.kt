package com.example.adrecommend.data

import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdItem
import com.example.adrecommend.model.AdType

object PreviewAdRepository : AdRepository {
    private val ads = listOf(
        AdItem(
            id = "preview_001",
            title = "Noise-canceling earbuds for daily commuting",
            brand = "SoundWay",
            description = "A placeholder ad used while the real data pipeline is being built.",
            imageUrl = "",
            landingUrl = "https://example.com/soundway",
            channel = AdChannel.Featured,
            category = "Digital",
            priceText = "$79",
            materialType = AdType.LargeImage,
            aiSummary = "A compact audio product aimed at commuters who value comfort and focus.",
            aiTags = listOf("Commuting", "Audio", "Productivity"),
            audience = listOf("Students", "Office workers"),
            sellingPoints = listOf("Noise canceling", "Long battery life")
        ),
        AdItem(
            id = "preview_002",
            title = "Lightweight running shoes with city style",
            brand = "MoveLab",
            description = "A placeholder sports ad for validating card type and channel structure.",
            imageUrl = "",
            landingUrl = "https://example.com/movelab",
            channel = AdChannel.Ecommerce,
            category = "Sports",
            priceText = "$59",
            materialType = AdType.SmallImage,
            aiSummary = "A value-oriented shoe recommendation for light workouts and daily wear.",
            aiTags = listOf("Sports", "Value", "Daily wear"),
            audience = listOf("Students", "Runners"),
            sellingPoints = listOf("Lightweight", "Breathable")
        ),
        AdItem(
            id = "preview_003",
            title = "Weekend brunch set near your office",
            brand = "Urban Table",
            description = "A placeholder local-life ad for validating the local channel.",
            imageUrl = "",
            landingUrl = "https://example.com/urbantable",
            channel = AdChannel.Local,
            category = "Local life",
            priceText = "$19",
            materialType = AdType.Video,
            aiSummary = "A short-form local offer designed for nearby weekend dining decisions.",
            aiTags = listOf("Local", "Food", "Weekend"),
            audience = listOf("Nearby users", "Young professionals"),
            sellingPoints = listOf("Limited offer", "Convenient location")
        )
    )

    override fun getChannels(): List<AdChannel> = AdChannel.entries

    override fun getAds(
        channel: AdChannel,
        page: Int,
        pageSize: Int
    ): List<AdItem> {
        val filteredAds = ads.filter { it.channel == channel }
        if (pageSize == Int.MAX_VALUE) {
            return filteredAds
        }
        val startIndex = (page.coerceAtLeast(0) * pageSize.coerceAtLeast(1))
            .coerceAtMost(filteredAds.size)
        val endIndex = (startIndex + pageSize.coerceAtLeast(1)).coerceAtMost(filteredAds.size)
        return filteredAds.subList(startIndex, endIndex)
    }
}
