package com.example.laundrytallyai.api.datastates

import com.example.laundrytallyai.api.dataschemes.HomeData

sealed class HomeDataState {
    data class Success(val data: HomeData) : HomeDataState()
    object Loading : HomeDataState()
    data class Error(val message: String) : HomeDataState()
}
