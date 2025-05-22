package com.example.not_today_sun

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

        setSupportActionBar(binding.appBarMain.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)

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