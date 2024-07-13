package com.example.laundrytallyai.api.datastates

import com.example.laundrytallyai.api.dataschemes.ClothesData

sealed class ClothesDataState {
    data class Success(val data: List<ClothesData>) : ClothesDataState()
    object Loading : ClothesDataState()
    data class Error(val code: String, val error: String) : ClothesDataState()
}
