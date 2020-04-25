package com.example.coronavirus_stats.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build


fun isNetworkAvailable(context: Context) : Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.activeNetwork != null
        } else {
                connectivityManager.activeNetworkInfo != null
        }

}