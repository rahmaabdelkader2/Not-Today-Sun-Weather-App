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
import com.example.not_today_sun.MainActivity
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
        initializeUI()
        setupRadioGroups()
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            try {
                val latitude = bundle.getDouble("lat")
                val longitude = bundle.getDouble("lon")
                viewModel.saveMapLocation(
                    requireContext(),
                    latitude.toFloat(),
                    longitude.toFloat(),
                    "Selected Location"
                )
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
        if (viewModel.getLocationPreference(requireContext())) {
            binding.gpsEnglish.isChecked = true
            binding.mapArabic.isChecked = false
        } else if (viewModel.getMapPreference(requireContext())) {
            binding.mapArabic.isChecked = true
            binding.gpsEnglish.isChecked = false
        }

        if (viewModel.getNotificationPreference(requireContext())) {
            binding.notificationsEnabled.isChecked = true
            binding.notificationsDisabled.isChecked = false
        } else {
            binding.notificationsDisabled.isChecked = true
            binding.notificationsEnabled.isChecked = false
        }

        when (viewModel.getTemperatureUnit(requireContext())) {
            "metric" -> binding.radioCelsius.isChecked = true
            "imperial" -> binding.radioFahrenheit.isChecked = true
            "standard" -> binding.radioKelvin.isChecked = true
            else -> binding.radioCelsius.isChecked = true
        }

        when (viewModel.getWindSpeedUnit(requireContext())) {
            "m/s" -> binding.radioMetersPerSecond.isChecked = true
            "mph" -> binding.radioMilesPerHour.isChecked = true
            else -> binding.radioMetersPerSecond.isChecked = true
        }

        when (viewModel.getLanguage(requireContext())) {
            "en" -> binding.radioEnglish.isChecked = true
            "ar" -> binding.radioArabic.isChecked = true
            else -> binding.radioEnglish.isChecked = true
        }
    }

    private fun setupRadioGroups() {
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.gpsEnglish.id -> {
                    viewModel.saveLocationPreference(requireContext(), true, false)
                }
                binding.mapArabic.id -> {
                    viewModel.saveLocationPreference(requireContext(), false, true)
                    findNavController().navigate(R.id.action_settingsFragment_to_simpleMapFragment)
                }
            }
        }

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

        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioCelsius.id -> {
                    viewModel.saveTemperatureUnit(requireContext(), "metric")
                }
                binding.radioFahrenheit.id -> {
                    viewModel.saveTemperatureUnit(requireContext(), "imperial")
                }
                binding.radioKelvin.id -> {
                    viewModel.saveTemperatureUnit(requireContext(), "standard")
                }
            }
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioMetersPerSecond.id -> {
                    viewModel.saveWindSpeedUnit(requireContext(), "m/s")
                }
                binding.radioMilesPerHour.id -> {
                    viewModel.saveWindSpeedUnit(requireContext(), "mph")
                }
            }
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioEnglish.id -> {
                    viewModel.saveLanguage(requireContext(), "en")
                    (requireActivity() as MainActivity).updateLocale("en")
                    (requireActivity() as MainActivity).restartActivity(findNavController().currentDestination?.id ?: R.id.settingsFragment)
                }
                binding.radioArabic.id -> {
                    viewModel.saveLanguage(requireContext(), "ar")
                    (requireActivity() as MainActivity).updateLocale("ar")
                    (requireActivity() as MainActivity).restartActivity(findNavController().currentDestination?.id ?: R.id.settingsFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}