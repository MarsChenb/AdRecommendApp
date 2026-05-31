package com.example.adrecommend.data

import android.content.Context
import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdItem

class LocalAdRepository(
    context: Context,
    private val enrichedAssetName: String = "ads_ai_enriched.json",
    private val rawAssetName: String = "ads_raw.json"
) : AdRepository {
    private val appContext = context.applicationContext
    private var loadErrorMessage: String? = null

    private val ads: List<AdItem> by lazy {
        loadAds()
    }

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

    override fun getLoadErrorMessage(): String? = loadErrorMessage

    private fun loadAds(): List<AdItem> {
        val enrichedResult = runCatching {
            AdJsonParser.parseAds(readAsset(enrichedAssetName))
        }
        if (enrichedResult.isSuccess) {
            loadErrorMessage = null
            return enrichedResult.getOrThrow()
        }

        val rawResult = runCatching {
            AdJsonParser.parseAds(readAsset(rawAssetName))
        }
        if (rawResult.isSuccess) {
            loadErrorMessage = "AI data failed to load; using raw ad data."
            return rawResult.getOrThrow()
        }

        loadErrorMessage = listOfNotNull(
            enrichedResult.exceptionOrNull()?.message,
            rawResult.exceptionOrNull()?.message
        ).joinToString(separator = "\n").ifBlank {
            "No local ad data could be loaded."
        }
        return emptyList()
    }

    private fun readAsset(assetName: String): String {
        return appContext.assets.open(assetName).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }
}
