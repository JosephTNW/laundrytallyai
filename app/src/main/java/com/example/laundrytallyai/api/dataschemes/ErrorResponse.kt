package com.example.laundrytallyai.api.dataschemes

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message") val message: String,
    val code: String
)