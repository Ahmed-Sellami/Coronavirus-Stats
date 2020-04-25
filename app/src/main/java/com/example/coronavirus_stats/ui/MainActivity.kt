package com.example.coronavirus_stats.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.example.coronavirus_stats.util.Constants.COUNTRY_NAME
import com.example.coronavirus_stats.util.Constants.NOTIFICATION_INTENT_ACTION
import com.example.coronavirus_stats.util.Constants.REMINDER_HOUR
import com.example.coronavirus_stats.util.Constants.REMINDER_MINUTE
import com.example.coronavirus_stats.R
import com.example.coronavirus_stats.util.SharedVariables.country
import com.example.coronavirus_stats.receiver.AlarmReceiver
import com.example.coronavirus_stats.util.Preferences.getPrefs
import com.example.coronavirus_stats.util.Location.initializeLocationClients
import com.example.coronavirus_stats.util.Location.getLocation
import com.example.coronavirus_stats.util.isNetworkAvailable
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Default)

    private lateinit var prefs: SharedPreferences

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNetworkAvailable(this)){

            createChannel(
                getString(R.string.summary_channel_id),
                getString(R.string.summary_channel_name)
            )

            prefs = getPrefs(this)

            initializeLocationClients(this, prefs)

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                activityScope.launch {
                    val countryName = prefs.getString(COUNTRY_NAME, null)
                    country = countryName

                    if(country == null){
                        getLocation(this@MainActivity, this@MainActivity, prefs)
                    }
                }

            }

            setupAlarmManager()

        } else {
            Snackbar.make(findViewById(android.R.id.content), "You are offline :(", Snackbar.LENGTH_LONG).show()
        }

    }

    private fun setupAlarmManager(){
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val REQUEST_CODE = 0
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
        }

        val notifyIntent = Intent(this, AlarmReceiver::class.java)
        notifyIntent.action = NOTIFICATION_INTENT_ACTION

        val notifyPendingIntent = PendingIntent.getBroadcast(
            application,
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            notifyPendingIntent
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        getLocation(this@MainActivity, this@MainActivity, prefs)
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            this, ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

    private fun startLocationPermissionRequest() =
        ActivityCompat.requestPermissions(
            this, arrayOf(ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )

    private fun requestPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            startLocationPermissionRequest()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Time for today's summary"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }
}
