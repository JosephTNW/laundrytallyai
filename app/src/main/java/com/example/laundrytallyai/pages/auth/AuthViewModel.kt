package com.example.laundrytallyai.pages.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.datastates.DataState
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.LoginRequest
import com.example.laundrytallyai.api.dataschemes.RegisterRequest
import com.example.laundrytallyai.api.datastates.HomeDataState
import com.example.laundrytallyai.api.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _authState = MutableStateFlow<DataState>(DataState.Loading)
    val authState: StateFlow<DataState> = _authState
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = DataState.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()?.string() ?: ""
                    _authState.value = DataState.Success(body)

                    val jsonObject = JSONObject(body)
                    saveToken(jsonObject.getString("token"))
                } else {
                    _authState.value = DataState.Error(parseError(response)["code"] ?: "Unknown error")
                }
            } catch (e: Exception) {
                _authState.value = DataState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = DataState.Loading
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        name = username,
                        email = email,
                        password = password
                    )
                )
                if (response.isSuccessful) {
                    _authState.value = DataState.Success("Successfully registered")
                } else {
                    _authState.value = DataState.Error(parseError(response)["code"] ?: "Unknown error")
                }
            } catch (e: Exception) {
                _authState.value = DataState.Error(e.message ?: "Unknown error")
            }
        }

    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }
}