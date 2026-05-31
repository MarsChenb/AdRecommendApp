package com.example.adrecommend.model

enum class AdChannel(val id: String, val displayName: String) {
    Featured("featured", "Featured"),
    Ecommerce("ecommerce", "E-commerce"),
    Local("local", "Local");

    companion object
}
