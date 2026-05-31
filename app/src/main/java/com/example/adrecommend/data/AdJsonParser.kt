package com.example.adrecommend.data

import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdItem
import com.example.adrecommend.model.AdType
import org.json.JSONArray
import org.json.JSONObject

object AdJsonParser {
    fun parseAds(json: String): List<AdItem> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(item.toAdItem())
            }
        }
    }

    private fun JSONObject.toAdItem(): AdItem {
        val description = requiredString("description")
        val rawTags = optStringList("rawTags")
        return AdItem(
            id = requiredString("id"),
            title = requiredString("title"),
            brand = requiredString("brand"),
            description = description,
            imageUrl = requiredString("imageUrl"),
            landingUrl = requiredString("landingUrl"),
            channel = AdChannel.fromId(requiredString("channel")),
            category = requiredString("category"),
            priceText = requiredString("priceText"),
            materialType = AdType.fromId(requiredString("materialType")),
            aiSummary = optString("aiSummary").ifBlank {
                description.take(MAX_FALLBACK_SUMMARY_LENGTH)
            },
            aiTags = optStringList("aiTags").ifEmpty {
                rawTags.ifEmpty { listOf(requiredString("category")) }
            },
            audience = optStringList("audience"),
            sellingPoints = optStringList("sellingPoints")
        )
    }

    private fun JSONObject.requiredString(name: String): String {
        val value = optString(name).trim()
        require(value.isNotEmpty()) { "Missing required field: $name" }
        return value
    }

    private fun JSONObject.optStringList(name: String): List<String> {
        val array = optJSONArray(name) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optString(index).trim()
                if (value.isNotEmpty()) {
                    add(value)
                }
            }
        }
    }

    private fun AdChannel.Companion.fromId(id: String): AdChannel {
        return AdChannel.entries.firstOrNull { it.id == id } ?: AdChannel.Featured
    }

    private fun AdType.Companion.fromId(id: String): AdType {
        return AdType.entries.firstOrNull { it.id == id } ?: AdType.LargeImage
    }

    private const val MAX_FALLBACK_SUMMARY_LENGTH = 96
}
