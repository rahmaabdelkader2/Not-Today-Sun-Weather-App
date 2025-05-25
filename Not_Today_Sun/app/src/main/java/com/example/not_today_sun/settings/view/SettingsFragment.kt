package com.example.not_today_sun.settings.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.not_today_sun.databinding.FragmentSettingsBinding
import com.example.not_today_sun.settings.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel
    private val TAG = "SettingsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        Log.d(TAG, "Fragment view created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Fragment view created")
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        loadCurrentSettings()
        setupRadioGroups()
    }

    private fun loadCurrentSettings() {
        Log.d(TAG, "Loading current settings from SharedPreferences")
        val context = requireContext()

        // Location settings
        when (viewModel.getLocationSetting(context)) {
            "gps" -> binding.locationRadioGroup.check(binding.gpsEnglish.id)
            "map" -> binding.locationRadioGroup.check(binding.mapArabic.id)
        }

        // Temperature settings
        when (viewModel.getTemperatureSetting(context)) {
            "celsius" -> binding.temperatureRadioGroup.check(binding.celsius.id)
            "fahrenheit" -> binding.temperatureRadioGroup.check(binding.fahrenheit.id)
            "kelvin" -> binding.temperatureRadioGroup.check(binding.kelvin.id)
        }

        // Language settings
        when (viewModel.getLanguageSetting(context)) {
            "english" -> binding.languageRadioGroup.check(binding.english.id)
            "arabic" -> binding.languageRadioGroup.check(binding.arabic.id)
        }

        // Wind speed settings
        when (viewModel.getWindSpeedSetting(context)) {
            "meter_sec" -> binding.windSpeedRadioGroup.check(binding.meterSec.id)
            "mile_hour" -> binding.windSpeedRadioGroup.check(binding.mileHour.id)
        }

        // Notifications settings
        if (viewModel.getNotificationsSetting(context)) {
            binding.notificationsRadioGroup.check(binding.notificationsOn.id)
        } else {
            binding.notificationsRadioGroup.check(binding.notificationsOff.id)
        }
    }

    private fun setupRadioGroups() {
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            saveSettings()
        }

        binding.temperatureRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            saveSettings()
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            saveSettings()
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            saveSettings()
        }

        binding.notificationsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            saveSettings()
        }
    }

    private fun saveSettings() {
        Log.d(TAG, "Preparing to save settings")

        val location = when (binding.locationRadioGroup.checkedRadioButtonId) {
            binding.gpsEnglish.id -> "gps"
            binding.mapArabic.id -> "map"
            else -> "gps"
        }

        val temperature = when (binding.temperatureRadioGroup.checkedRadioButtonId) {
            binding.celsius.id -> "celsius"
            binding.fahrenheit.id -> "fahrenheit"
            binding.kelvin.id -> "kelvin"
            else -> "celsius"
        }

        val language = when (binding.languageRadioGroup.checkedRadioButtonId) {
            binding.english.id -> "english"
            binding.arabic.id -> "arabic"
            else -> "english"
        }

        val windSpeed = when (binding.windSpeedRadioGroup.checkedRadioButtonId) {
            binding.meterSec.id -> "meter_sec"
            binding.mileHour.id -> "mile_hour"
            else -> "meter_sec"
        }

        val notifications = binding.notificationsRadioGroup.checkedRadioButtonId == binding.notificationsOn.id

        viewModel.saveSettings(
            requireContext(),
            location,
            temperature,
            language,
            windSpeed,
            notifications
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "Fragment view destroyed")
        _binding = null
    }
}