package com.example.not_today_sun.InitialSetup

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.databinding.ActivityInitialSetupBinding

class InitialSetup : AppCompatActivity() {
    private lateinit var binding: ActivityInitialSetupBinding
    private val sharedPref by lazy {
        getSharedPreferences("InitialSetupPrefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle GPS CheckBox click to save flag
        binding.gpsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("gps_enabled", isChecked)
                apply()
            }
        }

        // Handle OK button click to navigate to MainActivity
        binding.okButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}