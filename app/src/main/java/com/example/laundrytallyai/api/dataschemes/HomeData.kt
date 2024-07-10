package com.example.laundrytallyai.api.dataschemes

data class HomeData(
    val clothes: List<ClothesData>,
    val launderers: List<LaundererData>,
    val laundries: List<LaundryData>,
    val user: String
)
