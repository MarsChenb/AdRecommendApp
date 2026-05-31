package com.example.adrecommend.model

data class AdItem(
    val id: String,
    val title: String,
    val brand: String,
    val description: String,
    val imageUrl: String,
    val landingUrl: String,
    val channel: AdChannel,
    val category: String,
    val priceText: String,
    val materialType: AdType,
    val aiSummary: String,
    val aiTags: List<String>,
    val audience: List<String>,
    val sellingPoints: List<String>
)
