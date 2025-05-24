//package com.example.not_today_sun.OSM
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import com.example.not_today_sun.databinding.FragmentMapMarkerBinding
//import org.osmdroid.config.Configuration
//import org.osmdroid.events.MapEventsReceiver
//import org.osmdroid.tileprovider.tilesource.TileSourceFactory
//import org.osmdroid.util.GeoPoint
//import org.osmdroid.views.MapView
//import org.osmdroid.views.overlay.MapEventsOverlay
//import org.osmdroid.views.overlay.Marker
//import java.util.concurrent.TimeUnit
//
//class MapMarkerFragment : Fragment() {
//
//    private var _binding: FragmentMapMarkerBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: MapMarkerViewModel by viewModels()
//    private lateinit var map: MapView
//    private var currentMarker: Marker? = null
//    private var lastInvalidateTime = 0L
//    private val invalidateDebounceMs = TimeUnit.MILLISECONDS.toMillis(500)
//
//    companion object {
//        fun newInstance(): MapMarkerFragment {
//            return MapMarkerFragment()
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentMapMarkerBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        Configuration.getInstance().load(
//            requireContext(),
//            requireContext().getSharedPreferences("osm_prefs", 0)
//        )
//
//        setupMap()
//        setupClickListeners()
//        observeViewModel()
//    }
//
//    private fun setupMap() {
//        map = binding.mapView
//        map.setTileSource(TileSourceFactory.MAPNIK)
//        map.setMultiTouchControls(true)
//        map.controller.setZoom(12.0)
//        map.controller.setCenter(GeoPoint(51.5074, -0.1278))
//
//        val mapEventsReceiver = object : MapEventsReceiver {
//            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
//                addMarkerAtPosition(p)
//                return true
//            }
//
//            override fun longPressHelper(p: GeoPoint): Boolean {
//                return false
//            }
//        }
//        map.overlays.add(MapEventsOverlay(mapEventsReceiver))
//    }
//
//    private fun addMarkerAtPosition(position: GeoPoint) {
//        if (currentMarker == null) {
//            currentMarker = Marker(map)
//            map.overlays.add(currentMarker)
//        }
//
//        currentMarker?.apply {
//            this.position = position
//            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//            title = "Selected Location"
//            snippet = "Lat: ${position.latitude}, Lon: ${position.longitude}"
//        }
//
//        viewModel.setLocation(position, requireContext())
//        map.controller.animateTo(position)
//        debouncedInvalidate()
//    }
//
//    private fun debouncedInvalidate() {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastInvalidateTime >= invalidateDebounceMs) {
//            lastInvalidateTime = currentTime
//            map.invalidate()
//        }
//    }
//
//    private fun setupClickListeners() {
//        binding.btnSaveLocation.setOnClickListener {
//            viewModel.saveLocation(
//                binding.placeNameEditText.text.toString().trim(),
//                binding.latEditText.text.toString().trim(),
//                binding.lngEditText.text.toString().trim()
//            )
//        }
//    }
//
//    private fun observeViewModel() {
//        viewModel.locationData.observe(viewLifecycleOwner) { locationData ->
//            binding.latEditText.setText(locationData.latitude?.toString() ?: "")
//            binding.lngEditText.setText(locationData.longitude?.toString() ?: "")
//            binding.placeNameEditText.setText(locationData.placeName ?: "")
//        }
//
//        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
//            if (message.isNotEmpty()) {
//                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
//                if (message.startsWith("Saved:")) {
//                    val locationData = viewModel.locationData.value
//                    if (locationData?.latitude != null && locationData.longitude != null) {
//                        (requireActivity() as MapActivity).onLocationSaved(
//                            latitude = locationData.latitude,
//                            longitude = locationData.longitude,
//                            placeName = locationData.placeName ?: "Unknown"
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        map.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        map.onPause()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        map.overlays.clear()
//        _binding = null
//    }
//}