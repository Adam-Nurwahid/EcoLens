package com.adam.ecolens.data.model

data class EncyclopediaItem(
    val id: String,
    val category: WasteCategory,
    val title: String,
    val shortDesc: String,
    val fullContent: String,
    val iconEmoji: String
)
