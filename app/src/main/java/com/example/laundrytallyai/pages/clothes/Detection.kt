package com.example.laundrytallyai.pages.clothes

import android.graphics.RectF

data class Detection(val bbox: RectF, val label: String, val confidence: Float)
