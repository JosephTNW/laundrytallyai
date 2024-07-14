package com.example.laundrytallyai.api.datastates

sealed class ModifyDataState {
    object Idle : ModifyDataState()
    object Loading : ModifyDataState()
    data class Success(val message: String) : ModifyDataState()
    data class Error(val code: String, val error: String) : ModifyDataState()
}