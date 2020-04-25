package com.example.coronavirus_stats.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

object Location {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationCallback: LocationCallback


    fun initializeLocationClients(context: Context, prefs: SharedPreferences){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        settingsClient = LocationServices.getSettingsClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val currentLocation = locationResult?.lastLocation
                getCountry(currentLocation, context, prefs)
            }
        }
    }

    fun getLocation(activity: Activity, context: Context, prefs: SharedPreferences) {

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

            getCountry(location, context, prefs)

            if(SharedVariables.country == null){
                statusCheck(activity)
                startLocationUpdates()
            }
        }
    }

    private fun statusCheck(activity: Activity) {
        val manager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Snackbar.make(activity.findViewById(android.R.id.content),
                "Your GPS seems to be disabled, enable it and restart the app",
                Snackbar.LENGTH_LONG).show()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            numUpdates = 1
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationSettingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
    }

    private fun saveCountry(prefs: SharedPreferences){
        CoroutineScope(Dispatchers.IO).launch {
            prefs.edit().putString(Constants.COUNTRY_NAME, SharedVariables.country).apply()
        }
    }

    private fun getCountry(location: Location?, context: Context, prefs: SharedPreferences){
        val addresses: List<Address>?
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            addresses =
                location?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }
                    ?.toList()
            if (addresses != null) {
                SharedVariables.country = addresses[0].countryName.toString()

                saveCountry(prefs)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}