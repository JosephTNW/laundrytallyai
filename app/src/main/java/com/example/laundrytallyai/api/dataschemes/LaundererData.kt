package com.example.laundrytallyai.api.dataschemes

data class LaundererData(
    val address: String,
    val has_delivery: Boolean,
    val has_whatsapp: Boolean,
    val id: Int,
    val launderer_pic: String,
    val inputted_at: String,
    val name: String,
    val desc: String,
    val phone_num: String
)
