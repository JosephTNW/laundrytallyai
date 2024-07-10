package com.example.laundrytallyai.api.datastates

import com.example.laundrytallyai.api.dataschemes.LaundererData

sealed class LaundererDataState {
    data class Success(val data: List<LaundererData>) : LaundererDataState()
    object Loading : LaundererDataState()
    data class Error(val message: String) : LaundererDataState()
}
