package com.example.laundrytallyai.api.datastates

import com.example.laundrytallyai.api.dataschemes.LaundryData

sealed class LaundryDataState {
    data class Success(val data: List<LaundryData>) : LaundryDataState()
    object Loading : LaundryDataState()
    data class Error(val code: String, val error: String) : LaundryDataState()
}
