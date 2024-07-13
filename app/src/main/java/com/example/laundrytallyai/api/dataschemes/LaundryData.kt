package com.example.laundrytallyai.api.dataschemes

data class LaundryData(
    val bill_pic: String,
    val id: Int,
    val laundered_at: String,
    val laundry_days: Int,
    val status: String,
    val launderer: LaundererData,
    val clothes: List<ClothesData>
)
