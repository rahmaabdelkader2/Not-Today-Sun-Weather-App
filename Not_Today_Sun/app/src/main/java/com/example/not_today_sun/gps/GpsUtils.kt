package com.example.not_today_sun.gps

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GpsUtils(
    private val context: Context,
    private val locationPermissionRequest: ActivityResultLauncher<Array<String>>,
    private val onLocationSuccess: (Double, Double) -> Unit,
    private val onLocationFailure: () -> Unit
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun showPermissionRationale() {
        AlertDialog.Builder(context)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location permission to show weather for your current location.")
            .setPositiveButton("OK") { _, _ ->
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onLocationFailure()
            }
            .create()
            .show()
    }

    fun getCurrentLocation() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showEnableGpsDialog()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationSuccess(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    onLocationSuccess(it.latitude, it.longitude)
                }
            }.addOnFailureListener { exception ->
                if (exception is SecurityException) {
                    onLocationFailure()
                }
            }
        } catch (e: SecurityException) {
            onLocationFailure()
        }
    }

    private fun showEnableGpsDialog() {
        AlertDialog.Builder(context)
            .setTitle("Enable GPS")
            .setMessage("GPS is required for accurate location")
            .setPositiveButton("Settings") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onLocationFailure()
            }
            .show()
    }
}