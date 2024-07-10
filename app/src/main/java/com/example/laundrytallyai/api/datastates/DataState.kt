package com.example.laundrytallyai.api.datastates

import com.example.laundrytallyai.api.dataschemes.HomeData

sealed class DataState {
    object Loading : DataState()
    data class Success(val data: String) : DataState()
    data class Error(val message: String) : DataState()
}