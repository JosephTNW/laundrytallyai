package com.example.laundrytallyai.api.dataschemes

import android.graphics.Bitmap

data class PredictionsData(
    val bitmap: Bitmap,
    val className: String
)
