package com.example.laundrytallyai.api.datastates

sealed class ModifyClothesState {
    object Idle : ModifyClothesState()
    object Loading : ModifyClothesState()
    data class Success(val message: String) : ModifyClothesState()
    data class Error(val code: String, val error: String) : ModifyClothesState()
}