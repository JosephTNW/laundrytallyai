package com.example.laundrytallyai.pages.launderers

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.LaundererData
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.api.datastates.LaundererDataState
import com.example.laundrytallyai.api.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaundererViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<LaundererDataState>(LaundererDataState.Loading)
    val dataState: StateFlow<LaundererDataState> = _dataState
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun fetchData(query: String) {
        viewModelScope.launch {
            _dataState.value = LaundererDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _dataState.value = LaundererDataState.Error("No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.searchLaundererData(
                    token = token,
                    query = query
                )
                if (response.isSuccessful) {
                    val laundererData = response.body() ?: throw (Exception("No data found"))
                    _dataState.value = LaundererDataState.Success(laundererData)
                } else {
                    _dataState.value =
                        LaundererDataState.Error(parseError(response)["code"] ?: "Unknown error")
                }
            } catch (e: Exception) {
                _dataState.value = LaundererDataState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _selectedLaunderer = MutableStateFlow<LaundererData?>(null)
    val selectedLaunderer: StateFlow<LaundererData?> = _selectedLaunderer.asStateFlow()

    fun setSelectedLaunderer(launderer: LaundererData) {
        _selectedLaunderer.value = launderer
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun deleteToken() {
        sharedPreferences.edit().remove("token").apply()
    }
}