package com.example.not_today_sun.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentSettingsBinding
import com.example.not_today_sun.settings.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI based on saved preferences
        initializeUI()

        // Setup listeners for radio groups
        setupRadioGroups()

        // Listen for map location selection result
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            try {
                val latitude = bundle.getDouble("lat")
                val longitude = bundle.getDouble("lon")
                viewModel.saveMapLocation(
                    requireContext(),
                    latitude.toFloat(),
                    longitude.toFloat(),
                    "Selected Location" // Consider adding reverse geocoding for a proper name
                )
                // Update UI to reflect map selection
                binding.mapArabic.isChecked = true
                binding.gpsEnglish.isChecked = false
                Toast.makeText(requireContext(), "Location saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.gpsEnglish.isChecked = true
                binding.mapArabic.isChecked = false
                viewModel.saveLocationPreference(requireContext(), true, false)
                Toast.makeText(requireContext(), "Failed to select location, defaulting to GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeUI() {
        // Set location radio group based on preferences
        if (viewModel.getLocationPreference(requireContext())) {
            binding.gpsEnglish.isChecked = true
            binding.mapArabic.isChecked = false
        } else if (viewModel.getMapPreference(requireContext())) {
            binding.mapArabic.isChecked = true
            binding.gpsEnglish.isChecked = false
        }

        // Set notification radio group based on preference
        if (viewModel.getNotificationPreference(requireContext())) {
            binding.notificationsEnabled.isChecked = true
            binding.notificationsDisabled.isChecked = false
        } else {
            binding.notificationsDisabled.isChecked = true
            binding.notificationsEnabled.isChecked = false
        }
    }

    private fun setupRadioGroups() {
        // Location radio group
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.gpsEnglish.id -> {
                    viewModel.saveLocationPreference(requireContext(), true, false)
                }
                binding.mapArabic.id -> {
                    viewModel.saveLocationPreference(requireContext(), false, true)
                    // Navigate to SimpleMapFragment
                    findNavController().navigate(R.id.action_settingsFragment_to_simpleMapFragment)
                }
            }
        }

        // Notifications radio group
        binding.notificationsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.notificationsEnabled.id -> {
                    viewModel.saveNotificationPreference(requireContext(), true)
                }
                binding.notificationsDisabled.id -> {
                    viewModel.saveNotificationPreference(requireContext(), false)
                }
            }
        }

        // Setup other radio groups (temperature, language, wind speed)
        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Handle temperature unit selection (implement as needed)
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Handle language selection (implement as needed)
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Handle wind speed unit selection (implement as needed)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}