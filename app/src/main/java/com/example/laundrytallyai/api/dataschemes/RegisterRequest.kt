package com.example.laundrytallyai.api.dataschemes

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String
)
