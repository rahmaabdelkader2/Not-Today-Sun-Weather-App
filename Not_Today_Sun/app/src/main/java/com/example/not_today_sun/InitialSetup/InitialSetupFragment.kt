package com.example.not_today_sun.InitialSetup

import android.content.Context
import android.content.Intent
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

class InitialSetupFragment : Fragment() {

    private var _binding: FragmentInitialSetupBinding? = null
    private val binding get() = _binding!!

    private val settingsPrefs by lazy {
        requireContext().getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
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

        // Set up fragment result listener for map location
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
                // Proceed to MainActivity with HomeFragment
                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                    putExtra("navigate_to_home", true)
                })
                requireActivity().finish()
            } catch (e: Exception) {
                // Handle error
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
                // Proceed to MainActivity with HomeFragment
                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                    putExtra("navigate_to_home", true)
                })
                requireActivity().finish()
            }
        }

        // Handle GPS CheckBox
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
                // Ensure at least one is checked
                binding.gpsCheckBox.isChecked = true
            }
        }

        // Handle Map CheckBox
        binding.mapCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.gpsCheckBox.isChecked = false
                with(settingsPrefs.edit()) {
                    putBoolean("use_gps", false)
                    putBoolean("use_map", true)
                    apply()
                }
            } else if (!binding.gpsCheckBox.isChecked) {
                // Ensure at least one is checked
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

        // Handle Notification CheckBox
        binding.NotificationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            with(settingsPrefs.edit()) {
                putBoolean("notifications_enabled", isChecked)
                apply()
            }
        }

        // Handle OK Button
        binding.okButton.setOnClickListener {
            if (!binding.gpsCheckBox.isChecked && !binding.mapCheckBox.isChecked) {
                Toast.makeText(requireContext(), "Please select a location option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.mapCheckBox.isChecked) {
                // Navigate to SimpleMapFragment
                findNavController().navigate(R.id.action_initialSetupFragment_to_simpleMapFragment)
            } else {
                // Mark setup as complete and proceed to MainActivity
                with(settingsPrefs.edit()) {
                    putBoolean("is_first_boot", false)
                    apply()
                }
                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                    putExtra("navigate_to_home", true)
                })
                requireActivity().finish()
            }
        }

        // Restore saved state
        val useGps = settingsPrefs.getBoolean("use_gps", true)
        val useMap = settingsPrefs.getBoolean("use_map", false)
        binding.gpsCheckBox.isChecked = useGps
        binding.mapCheckBox.isChecked = useMap
        binding.NotificationCheckBox.isChecked = settingsPrefs.getBoolean("notifications_enabled", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}