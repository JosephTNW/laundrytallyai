package com.example.laundrytallyai.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun dateFormatter(originalString: String): String {
    // Define the formatter for parsing the original date string
    val inputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")

    // Parse the date string into a ZonedDateTime object
    val parsedDate = ZonedDateTime.parse(originalString, inputFormatter)

    // Define the formatter for the desired output format
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Format the ZonedDateTime object into the desired format
    return parsedDate.format(outputFormatter)
}