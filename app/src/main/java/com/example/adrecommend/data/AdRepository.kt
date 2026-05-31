package com.example.adrecommend.data

import com.example.adrecommend.model.AdChannel
import com.example.adrecommend.model.AdItem

interface AdRepository {
    fun getChannels(): List<AdChannel>

    fun getAds(channel: AdChannel): List<AdItem>
}
