package com.example.carshowroom.data.models

data class CarModel(
    val id: String,
    val name: String,
    val modelPath: String, // Path in assets/models/
    val thumbnail: Int, // Drawable resource ID (placeholder)
    val paintMaterialNames: List<String> = emptyList(),
    val rimMaterialNames: List<String> = emptyList()
)
