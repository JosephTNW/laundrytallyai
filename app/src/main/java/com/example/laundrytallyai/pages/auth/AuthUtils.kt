package com.example.laundrytallyai.pages.auth

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.laundrytallyai.R

object AuthUtils {
    fun checkEmail(email: String): Boolean {
        return !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @Composable
    fun LaundryLogoIcon() {
        val context = LocalContext.current
        val imageBitmap = remember {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_laundry_logo)
            drawable?.let {
                val bitmap = Bitmap.createBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
                bitmap.asImageBitmap()
            }
        }

        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Laundry Logo",
                modifier = Modifier.size(128.dp) // Adjust size as needed
            )
        }
    }
}
