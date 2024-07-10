package com.example.laundrytallyai.api.dataschemes

data class ClothesData(
    val cloth_pic: String,
    val color: String,
    val id: Int,
    val desc: String,
    val inputted_at: String,
    val type: String
)
