package com.example.laundrytallyai.api.dataschemes

import java.io.File

data class ClothesPostData(
    val cloth_pic: File,
    val color: String,
    val desc: String,
    val c_type: String
)
