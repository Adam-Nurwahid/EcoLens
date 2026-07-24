package com.adam.ecolens.data.model

data class EncyclopediaItem(
    val id: String,
    val category: WasteCategory,
    val title: String,
    val shortDesc: String,
    val fullContent: String,
    val iconEmoji: String
)

data class NewsItem(
    val id: String,
    val title: String,
    val excerpt: String,
    val date: String,
    val readTime: String,
    val author: String
)
