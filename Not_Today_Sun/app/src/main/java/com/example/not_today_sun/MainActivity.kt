//package com.example.not_today_sun
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.GravityCompat
//import androidx.navigation.NavController
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.navigateUp
//import androidx.navigation.ui.setupActionBarWithNavController
//import androidx.navigation.ui.setupWithNavController
//import com.example.not_today_sun.databinding.ActivityMainBinding
//import com.example.not_today_sun.model.local.LocalDataSource
//import com.example.not_today_sun.model.local.WeatherDatabase
//import com.example.not_today_sun.model.remote.RemoteDataSource
//import com.example.not_today_sun.model.remote.RetrofitClient
//import com.example.not_today_sun.model.repo.WeatherRepository
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var navController: NavController
//    private lateinit var appBarConfiguration: AppBarConfiguration
//
//    private val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }
//    val weatherRepository: WeatherRepository by lazy {
//        val remoteDataSource = RemoteDataSource(RetrofitClient.weatherApiService)
//        val localDataSource = LocalDataSource(database.weatherDao())
//        WeatherRepository(remoteDataSource, localDataSource)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Set the toolbar as the ActionBar
//        setSupportActionBar(binding.appBarMain.toolbar)
//
//        // Initialize NavController
//        navController = findNavController(R.id.nav_host_fragment_content_main)
//
//        // Configure AppBar with top-level destinations and drawer
//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.nav_home, R.id.nav_fav, R.id.nav_settings),
//            binding.drawerLayout
//        )
//
//        // Set up ActionBar with NavController
//        setupActionBarWithNavController(navController, appBarConfiguration)
//
//        // Set up NavigationView with NavController
//        binding.navView.setupWithNavController(navController)
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
//
//    override fun onBackPressed() {
//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }
//}

package com.example.not_today_sun

import android.content.Context
import android.os.Bundle
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
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }
    val weatherRepository: WeatherRepository by lazy {
        val remoteDataSource = RemoteDataSource(RetrofitClient.weatherApiService)
        val localDataSource = LocalDataSource(database.weatherDao())
        WeatherRepository(remoteDataSource, localDataSource)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.appBarMain.toolbar)
        setSupportActionBar(binding.appBarMain.toolbar)
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val sharedPrefs = getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
        val isFirstBoot = sharedPrefs.getBoolean("is_first_boot", true)
        if (isFirstBoot) {
            navController.navigate(R.id.initialSetupFragment)
        } else if (intent.getBooleanExtra("navigate_to_home", false)) {
            navController.navigate(R.id.nav_home)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_fav, R.id.nav_settings),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}