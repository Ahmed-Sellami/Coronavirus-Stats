package com.example.coronavirus_stats.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object Preferences {
    fun getPrefs(context: Context) : SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
}