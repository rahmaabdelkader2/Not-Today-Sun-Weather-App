package com.example.not_today_sun.OSM

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
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
import java.util.Locale

class SimpleMapFragment : Fragment() {
    private var _binding: FragmentSimpleMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var marker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
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

        // Initialize location client and geocoder
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        geocoder = Geocoder(requireContext(), Locale.getDefault())

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
                Log.d("SimpleMapFragment", "Map tap: lat=$selectedLatitude, lon=$selectedLongitude")
                map.invalidate()
            }
            false
        }

        // Search button listener
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchPlace(query)
            } else {
                binding.etSearch.error = "Please enter a place name"
            }
        }

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            if (selectedLatitude != null && selectedLongitude != null) {
                Log.d("SimpleMapFragment", "Confirm: lat=$selectedLatitude, lon=$selectedLongitude")
                setFragmentResult("locationRequestKey", Bundle().apply {
                    putDouble("lat", selectedLatitude!!)
                    putDouble("lon", selectedLongitude!!)
                })
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show()
            }
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

    private fun searchPlace(query: String) {
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val location = GeoPoint(address.latitude, address.longitude)
                marker.position = location
                selectedLatitude = address.latitude
                selectedLongitude = address.longitude
                map.controller.setCenter(location)
                map.controller.setZoom(15.0)
                map.invalidate()
                Log.d("SimpleMapFragment", "Search result: lat=$selectedLatitude, lon=$selectedLongitude")
                Toast.makeText(requireContext(), "Location set to ${address.getAddressLine(0)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Place not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error searching place: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Log.d("SimpleMapFragment", "Current location: lat=$selectedLatitude, lon=$selectedLongitude")
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