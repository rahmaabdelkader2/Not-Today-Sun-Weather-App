////package com.example.not_today_sun.OSM
////
////import android.os.Bundle
////import androidx.activity.enableEdgeToEdge
////import androidx.appcompat.app.AppCompatActivity
////import androidx.core.view.ViewCompat
////import androidx.core.view.WindowInsetsCompat
////import androidx.fragment.app.commit
////import com.example.not_today_sun.databinding.ActivityMapBinding
////
////class MapActivity : AppCompatActivity() {
////
////    private lateinit var binding: ActivityMapBinding
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        binding = ActivityMapBinding.inflate(layoutInflater)
////        setContentView(binding.root)
////        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
////            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
////            insets
////        }
////
////        if (savedInstanceState == null) {
////            supportFragmentManager.commit {
////                replace(binding.mapContainer.id, MapMarkerFragment.newInstance())
////            }
////        }
////    }
////
////    fun onLocationSaved(latitude: Double, longitude: Double, placeName: String) {
////        val result = Bundle().apply {
////            putDouble("latitude", latitude)
////            putDouble("longitude", longitude)
////            putString("placeName", placeName)
////        }
////        supportFragmentManager.setFragmentResult("requestKey", result)
////        finish()
////    }
////}
//package com.example.not_today_sun.OSM
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.fragment.app.commit
//import com.example.not_today_sun.databinding.ActivityMapBinding
//
//class MapActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMapBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityMapBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        if (savedInstanceState == null) {
//            supportFragmentManager.commit {
//                replace(binding.mapContainer.id, SimpleMapFragment.newInstance())
//            }
//        }
//    }
//}