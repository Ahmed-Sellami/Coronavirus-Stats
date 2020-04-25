package com.example.coronavirus_stats.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun getTheSixPreviousDays(dateFormat: String, addToday: Boolean): MutableList<String>{
    val calendar = Calendar.getInstance()
    val currentDate: Date = calendar.time
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    val formattedDate = formatter.format(currentDate)
    calendar.time = formatter.parse(formattedDate)!!

    val list: MutableList<String> = ArrayList()
    if(addToday){
        calendar.add(Calendar.DATE, 0)
        list.add(formatter.format(calendar.time))
        Log.i("TopLevelFunc", formatter.format(calendar.time))
    }
    for (i in 1..6){
        calendar.add(Calendar.DATE, -1)
        list.add(formatter.format(calendar.time))
        Log.i("TopLevelFunc", formatter.format(calendar.time))
    }
    return list
}

fun setCountriesFlagEmoji() : MutableMap<String, String> {
    val countries: MutableMap<String, String> = HashMap()

    for (iso in Locale.getISOCountries()) {
        val l = Locale("", iso)
        val countryName = when(l.displayCountry){
            "United States" -> "USA"
            "United Kingdom" -> "UK"
            "United Arab Emirates" -> "UAE"
            else -> l.displayCountry
        }
        countries[countryName] = l.flagEmoji
    }
    return countries
}

private val Locale.flagEmoji: String
    get() {
        val firstLetter = Character.codePointAt(country, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(country, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }