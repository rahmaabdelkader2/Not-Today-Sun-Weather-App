//package com.example.not_today_sun.InitialSetup
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.not_today_sun.MainActivity
//import com.example.not_today_sun.databinding.ActivityInitialSetupBinding
////import com.example.not_today_sun.OSM.MapActivity
//
//class InitialSetup : AppCompatActivity() {
//    private lateinit var binding: ActivityInitialSetupBinding
//    private val settingsPrefs by lazy {
//        getSharedPreferences("WeatherSettings", MODE_PRIVATE)
//    }
//
//    private val mapLocationResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            result.data?.let { data ->
//                val latitude = data.getDoubleExtra("latitude", 0.0)
//                val longitude = data.getDoubleExtra("longitude", 0.0)
//                val locationName = data.getStringExtra("placeName") ?: "Unknown"
//                with(settingsPrefs.edit()) {
//                    putFloat("map_lat", latitude.toFloat())
//                    putFloat("map_lon", longitude.toFloat())
//                    putString("map_location_name", locationName)
//                    apply()
//                }
//                // Proceed to MainActivity after saving map location
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        } else {
//            // If map selection is canceled, stay in InitialSetup
//            binding.mapCheckBox.isChecked = false
//            binding.gpsCheckBox.isChecked = true
//            with(settingsPrefs.edit()) {
//                putBoolean("use_gps", true)
//                putBoolean("use_map", false)
//                remove("map_lat")
//                remove("map_lon")
//                remove("map_location_name")
//                apply()
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // Handle GPS CheckBox
//        binding.gpsCheckBox.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                binding.mapCheckBox.isChecked = false
//                with(settingsPrefs.edit()) {
//                    putBoolean("use_gps", true)
//                    putBoolean("use_map", false)
//                    remove("map_lat")
//                    remove("map_lon")
//                    remove("map_location_name")
//                    apply()
//                }
//            }
//        }
//
//        // Handle Map CheckBox
//        binding.mapCheckBox.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                binding.gpsCheckBox.isChecked = false
//                with(settingsPrefs.edit()) {
//                    putBoolean("use_gps", false)
//                    putBoolean("use_map", true)
//                    apply()
//                }
//            } else {
//                with(settingsPrefs.edit()) {
//                    putBoolean("use_map", false)
//                    remove("map_lat")
//                    remove("map_lon")
//                    remove("map_location_name")
//                    apply()
//                }
//            }
//        }
//
//        // Handle Notification CheckBox
//        binding.NotificationCheckBox.setOnCheckedChangeListener { _, isChecked ->
//            with(settingsPrefs.edit()) {
//                putBoolean("notifications_enabled", isChecked)
//                apply()
//            }
//        }
//
//        // Handle OK Button
//        binding.okButton.setOnClickListener {
//            if (binding.mapCheckBox.isChecked) {
//                // Launch MapActivity to select location
//                val intent = Intent(this, MapActivity::class.java)
//                mapLocationResult.launch(intent)
//            } else {
//                // Proceed to MainActivity
//                startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        }
//
//        // Restore saved state
//        val useGps = settingsPrefs.getBoolean("use_gps", true) // Default to GPS
//        val useMap = settingsPrefs.getBoolean("use_map", false)
//        binding.gpsCheckBox.isChecked = useGps
//        binding.mapCheckBox.isChecked = useMap
//        binding.NotificationCheckBox.isChecked = settingsPrefs.getBoolean("notifications_enabled", false)
//    }
//}