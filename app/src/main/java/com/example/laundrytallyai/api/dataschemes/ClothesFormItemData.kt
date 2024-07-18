package com.example.laundrytallyai.api.dataschemes

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState

data class ClothesFormItemData(
    val c_type: MutableState<String>,
    val color: MutableState<String>,
    val desc: MutableState<String>,
    val cloth_pic: MutableState<Bitmap>,
)
