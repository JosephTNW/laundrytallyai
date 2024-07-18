package com.example.laundrytallyai.api.datastates

sealed class TypePredictionDataState {
    data class Success(val data: List<String>) : TypePredictionDataState()
    object Loading : TypePredictionDataState()
    data class Error(val code: String, val error: String) : TypePredictionDataState()
}