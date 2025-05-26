package com.example.not_today_sun

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.not_today_sun.databinding.ActivityMainBinding
import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.local.WeatherDatabase
import com.example.not_today_sun.model.remote.RemoteDataSource
import com.example.not_today_sun.model.remote.RetrofitClient
import com.example.not_today_sun.model.repo.WeatherRepository

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "AlarmDebug"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val database: WeatherDatabase by lazy {
        Log.d(TAG, "Initializing WeatherDatabase")
        WeatherDatabase.getDatabase(this)
    }
    val weatherRepository: WeatherRepository by lazy {
        Log.d(TAG, "Initializing WeatherRepository")
        val remoteDataSource = RemoteDataSource(RetrofitClient.weatherApiService)
        val localDataSource = LocalDataSource(database.weatherDao())
        WeatherRepository(remoteDataSource, localDataSource)
    }

    private val requestExactAlarmPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d(TAG, "SCHEDULE_EXACT_ALARM permission request result")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms()) {
            Log.w(TAG, "SCHEDULE_EXACT_ALARM permission not granted")
        }
    }

    private val requestBatteryOptimization = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d(TAG, "Battery optimization exemption request result")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                Log.d(TAG, "Battery optimization disabled")
            } else {
                Log.w(TAG, "Battery optimization still enabled")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity.onCreate called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val sharedPrefs = getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
        val isFirstBoot = sharedPrefs.getBoolean("is_first_boot", true)
        Log.d(TAG, "isFirstBoot=$isFirstBoot, intent extras=${intent.extras?.toString()}")
        if (isFirstBoot) {
            Log.d(TAG, "Navigating to initialSetupFragment")
            navController.navigate(R.id.initialSetupFragment)
        } else if (intent.getBooleanExtra("navigate_to_home", false)) {
            Log.d(TAG, "Navigating to nav_home")
            navController.navigate(R.id.nav_home)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_fav, R.id.nav_notification, R.id.settingsFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        createNotificationChannel()
        requestExactAlarmPermission()
        requestBatteryOptimizationExemption()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "MainActivity.onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity.onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity.onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity.onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity.onDestroy called, isFinishing=$isFinishing")
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "onSupportNavigateUp called")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed called, drawerOpen=${binding.drawerLayout.isDrawerOpen(GravityCompat.START)}")
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm notifications"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Requesting SCHEDULE_EXACT_ALARM permission")
                requestExactAlarmPermission.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } else {
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission already granted")
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Log.d(TAG, "Requesting battery optimization exemption")
                requestBatteryOptimization.launch(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                )
            } else {
                Log.d(TAG, "Battery optimization already disabled")
            }
        }
    }
}