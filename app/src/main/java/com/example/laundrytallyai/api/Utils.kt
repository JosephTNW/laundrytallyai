package com.example.laundrytallyai.api

import com.example.laundrytallyai.api.dataschemes.ErrorResponse
import com.google.gson.Gson
import retrofit2.Response
import android.os.Build

fun parseError(response: Response<*>): Map<String, String> {
    val errorBody = response.errorBody()?.string()
    val errorMessage = if (errorBody != null){
        try {
            Gson().fromJson(errorBody, ErrorResponse::class.java).message
        } catch (e: Exception) {
            "Error parsing message: ${e.message}"
        }
    } else {
        "Unknown error"
    }

    return mapOf(
        "code" to response.code().toString(),
        "message" to response.message().toString(),
        "error" to errorMessage
    )
}

object DeviceUtils {
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Google")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.BOARD == "goldfish"
                || Build.HOST.startsWith("tnbp")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }
}