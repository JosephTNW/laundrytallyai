package com.example.laundrytallyai.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun dateFormatterDMY(originalString: String): String {
    // Define the formatter for parsing the original date string
    val inputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")

    // Parse the date string into a ZonedDateTime object
    val parsedDate = ZonedDateTime.parse(originalString, inputFormatter)

    // Define the formatter for the desired output format
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Format the ZonedDateTime object into the desired format
    return parsedDate.format(outputFormatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun dateFormatterDMYHms(originalString: String): String {
    // Define the formatter for parsing the original date string
    val inputFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")

    // Parse the date string into a ZonedDateTime object
    val parsedDate = ZonedDateTime.parse(originalString, inputFormatter)

    // Define the formatter for the desired output format
    val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    // Format the ZonedDateTime object into the desired format
    return parsedDate.format(outputFormatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun addDaysToFormattedDate(dateString: String, numberOfDays: Int): String {
    // Define the formatter for parsing the input date string
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Parse the date string into a LocalDate object
    val parsedDate = LocalDate.parse(dateString, formatter)

    // Add the specified number of days to the parsed date
    val newDate = parsedDate.plusDays(numberOfDays.toLong())

    // Format the new date into the desired format
    return newDate.format(formatter)
}