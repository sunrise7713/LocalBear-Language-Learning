package com.rojdaurper.ingilizcemobil



data class WordModel(
    val eng: String = "",
    val tr: String = "",
    val level: String = "",
    val category: String = "",
    val imageUrl: String = "",
    // Hatanın sebebi bu satırın eksik olması:
    var isFavorite: Boolean = false
)

