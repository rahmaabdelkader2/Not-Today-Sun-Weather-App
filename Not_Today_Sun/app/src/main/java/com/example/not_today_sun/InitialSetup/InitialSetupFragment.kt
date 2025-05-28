package com.example.not_today_sun.InitialSetup

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentInitialSetupBinding
import com.google.android.material.snackbar.Snackbar

class InitialSetupFragment : Fragment() {

    private var _binding: FragmentInitialSetupBinding? = null
    private val binding get() = _binding!!

    private val settingsPrefs by lazy {
        requireContext().getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitialSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Check network availability
        if (!isNetworkAvailable()) {
            showNoInternetConnectionScreen()
        } else {
            binding.noInternetAnimation.visibility = View.GONE
            binding.noInternetAnimation.cancelAnimation()
            binding.initSetupLayout.visibility = View.VISIBLE
        }

        // Fragment result listener for map location
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            try {
                val latitude = bundle.getDouble("lat")
                val longitude = bundle.getDouble("lon")
                with(settingsPrefs.edit()) {
                    putFloat("map_lat", latitude.toFloat())
                    putFloat("map_lon", longitude.toFloat())
                    putString("map_location_name", "Selected Location")
                    putBoolean("is_first_boot", false)
                    putBoolean("use_gps", false)
                    putBoolean("use_map", true)
                    apply()
                }
                Toast.makeText(requireContext(), "Location saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                    putExtra("navigate_to_home", true)
                })
                requireActivity().finish()
            } catch (e: Exception) {
                binding.mapCheckBox.isChecked = false
                binding.gpsCheckBox.isChecked = true
                with(settingsPrefs.edit()) {
                    putBoolean("use_gps", true)
                    putBoolean("use_map", false)
                    remove("map_lat")
                    remove("map_lon")
                    remove("map_location_name")
                    putBoolean("is_first_boot", false)
                    apply()
                }
                Toast.makeText(requireContext(), "Failed to select location, defaulting to GPS", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                    putExtra("navigate_to_home", true)
                })
                requireActivity().finish()
            }
        }

        // CheckBoxes listeners
        binding.gpsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.mapCheckBox.isChecked = false
                with(settingsPrefs.edit()) {
                    putBoolean("use_gps", true)
                    putBoolean("use_map", false)
                    remove("map_lat")
                    remove("map_lon")
                    remove("map_location_name")
                    apply()
                }
            } else if (!binding.mapCheckBox.isChecked) {
                binding.gpsCheckBox.isChecked = true
            }
        }

        binding.mapCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.gpsCheckBox.isChecked = false
                with(settingsPrefs.edit()) {
                    putBoolean("use_gps", false)
                    putBoolean("use_map", true)
                    apply()
                }
            } else if (!binding.gpsCheckBox.isChecked) {
                binding.gpsCheckBox.isChecked = true
                with(settingsPrefs.edit()) {
                    putBoolean("use_gps", true)
                    putBoolean("use_map", false)
                    remove("map_lat")
                    remove("map_lon")
                    remove("map_location_name")
                    apply()
                }
            }
        }

        binding.NotificationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            with(settingsPrefs.edit()) {
                putBoolean("notifications_enabled", isChecked)
                apply()
            }
        }

        // OK Button click: check permissions if needed
        binding.okButton.setOnClickListener {
            if (!binding.gpsCheckBox.isChecked && !binding.mapCheckBox.isChecked) {
                Toast.makeText(requireContext(), "Please select a location option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If map is selected, no location permission needed here; navigate directly
            if (binding.mapCheckBox.isChecked) {
                findNavController().navigate(R.id.action_initialSetupFragment_to_simpleMapFragment)
                return@setOnClickListener
            }

            // GPS selected: check location permission first
            if (binding.gpsCheckBox.isChecked && !hasLocationPermission()) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                return@setOnClickListener
            }

            // If notifications enabled, check notification permission on Android 13+
            if (binding.NotificationCheckBox.isChecked && !hasNotificationPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                    return@setOnClickListener
                }
            }

            // All permissions granted or not needed, proceed
            proceedAfterPermissions()
        }

        // Restore saved state
        val useGps = settingsPrefs.getBoolean("use_gps", true)
        val useMap = settingsPrefs.getBoolean("use_map", false)
        binding.gpsCheckBox.isChecked = useGps
        binding.mapCheckBox.isChecked = useMap
        binding.NotificationCheckBox.isChecked = settingsPrefs.getBoolean("notifications_enabled", false)
    }

    // Permission result handler
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted, check notification if enabled
                    if (binding.NotificationCheckBox.isChecked && !hasNotificationPermission()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissions(
                                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                NOTIFICATION_PERMISSION_REQUEST_CODE
                            )
                        } else {
                            proceedAfterPermissions()
                        }
                    } else {
                        proceedAfterPermissions()
                    }
                } else {
                    Toast.makeText(requireContext(), "Location permission is required.", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceedAfterPermissions()
                } else {
                    Toast.makeText(requireContext(), "Notification permission is required for alerts.", Toast.LENGTH_SHORT).show()
                    // Proceed anyway or force user to allow - your choice here:
                    proceedAfterPermissions()
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return requireContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun proceedAfterPermissions() {
        with(settingsPrefs.edit()) {
            putBoolean("is_first_boot", false)
            apply()
        }
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            putExtra("navigate_to_home", true)
        })
        requireActivity().finish()
    }

    fun showNoInternetConnectionScreen() {
        binding.noInternetAnimation.visibility = View.VISIBLE
        binding.noInternetAnimation.playAnimation()
        binding.initSetupLayout.visibility = View.GONE

        Snackbar.make(
            binding.root,
            "No internet connection. Please check your settings.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Retry") {
            if (isNetworkAvailable()) {
                binding.noInternetAnimation.cancelAnimation()
                binding.noInternetAnimation.visibility = View.GONE
                binding.initSetupLayout.visibility = View.VISIBLE
            } else {
                showNoInternetConnectionScreen()
            }
        }.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}