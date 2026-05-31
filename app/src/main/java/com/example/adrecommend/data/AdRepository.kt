package com.example.adrecommend.data

import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdItem

interface AdRepository {
    fun getChannels(): List<AdChannel>

    fun getAds(
        channel: AdChannel,
        page: Int = 0,
        pageSize: Int = Int.MAX_VALUE
    ): List<AdItem>

    fun getLoadErrorMessage(): String? = null
}
