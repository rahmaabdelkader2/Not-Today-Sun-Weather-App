package com.example.not_today_sun.OSM

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.not_today_sun.databinding.FragmentSimpleMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class SimpleMapFragment : Fragment() {
    private var _binding: FragmentSimpleMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var marker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure osmdroid
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        Configuration.getInstance().userAgentValue = "WeatherApp/1.0"

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize map
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        val startPoint = GeoPoint(30.0444, 31.2357) // Default: Cairo
        mapController.setZoom(8.0)
        mapController.setCenter(startPoint)

        // Initialize marker
        marker = Marker(map)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.isDraggable = true
        map.overlays.add(marker)

        // Store initial position
        selectedLatitude = startPoint.latitude
        selectedLongitude = startPoint.longitude

        // Handle map tap to move marker
        map.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val proj = map.projection
                val geoPoint = proj.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                marker.position = geoPoint
                selectedLatitude = geoPoint.latitude
                selectedLongitude = geoPoint.longitude
                map.invalidate()
            }
            false
        }

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            selectedLatitude?.let { lat ->
                selectedLongitude?.let { lon ->
                    setFragmentResult("locationRequestKey", Bundle().apply {
                        putDouble("lat", lat)
                        putDouble("lon", lon)
                    })
                    findNavController().navigateUp()
                }
            } ?: Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show()
        }

        // Current location button
        binding.btnCurrentLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentPoint = GeoPoint(location.latitude, location.longitude)
                    marker.position = currentPoint
                    selectedLatitude = location.latitude
                    selectedLongitude = location.longitude
                    map.controller.setCenter(currentPoint)
                    map.controller.setZoom(15.0)
                    map.invalidate()
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.overlays.clear()
        _binding = null
    }
}