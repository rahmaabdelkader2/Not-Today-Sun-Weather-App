package com.example.not_today_sun.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.not_today_sun.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup your UI components here
        setupRadioGroups()
    }

    private fun setupRadioGroups() {
        // Example setup for location radio group
        binding.locationRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.gpsEnglish.id -> {
                    // Handle GPS English selection
                }
                binding.mapArabic.id -> {
                    // Handle Map Arabic selection
                }
            }
        }

        // Example setup for temperature radio group
        binding.temperatureRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.celsius.id -> {
                    // Handle Celsius selection
                }
                binding.fahrenheit.id -> {
                    // Handle Fahrenheit selection
                }
                binding.kelvin.id -> {
                    // Handle Kelvin selection
                }
            }
        }

        // Add similar setups for other radio groups
        binding.languageRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Handle language selection
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Handle wind speed unit selection
        }

        binding.notificationsRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Handle notifications toggle
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the binding reference to avoid memory leaks
        _binding = null
    }
}