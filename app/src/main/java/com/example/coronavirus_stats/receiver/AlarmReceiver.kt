package com.example.coronavirus_stats.receiver


import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.coronavirus_stats.util.Constants.COUNTRY_NAME
import com.example.coronavirus_stats.util.Constants.NOTIFICATION_INTENT_ACTION
import com.example.coronavirus_stats.api.CoronavirusMonitorApi
import com.example.coronavirus_stats.util.Preferences
import com.example.coronavirus_stats.util.isNetworkAvailable
import com.example.coronavirus_stats.util.sendNotification
import com.example.coronavirus_stats.util.toStringWithCommas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val prefs = Preferences.getPrefs(context)

        if(intent.action == NOTIFICATION_INTENT_ACTION && isNetworkAvailable(context)){

            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            CoroutineScope(Dispatchers.IO).launch {
                val countryName = prefs.getString(COUNTRY_NAME, null)

                if (countryName != null) {
                    try {
                        val latestStat = CoronavirusMonitorApi.retrofitService.getCountryStat(
                            countryName
                        ).await()

                        val stat = latestStat[0]
                        val summary = "Confirmed: ${stat.confirmed.toInt().toStringWithCommas()} | " +
                                "Deaths: ${stat.deaths.toInt().toStringWithCommas()} | " +
                                "Recovered: ${stat.recovered.toInt().toStringWithCommas()}"

                        notificationManager.sendNotification(
                            summary,
                            context
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }

        }

    }

}

