package com.example.not_today_sun

import android.app.Application
import android.util.Log
import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.local.WeatherDatabase
import com.example.not_today_sun.model.remote.RemoteDataSource
import com.example.not_today_sun.model.remote.RetrofitClient
import com.example.not_today_sun.model.repo.WeatherRepository

class MyApplication : Application() {
    companion object {
        private const val TAG = "AlarmDebug"
    }

    lateinit var weatherRepository: WeatherRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyApplication.onCreate called")
        weatherRepository= WeatherRepository.getInstance(
            remoteDataSource = RemoteDataSource(),
            localDataSource = LocalDataSource(WeatherDatabase.getDatabase(this).weatherDao())
        )    }
}