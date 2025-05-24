
package com.example.not_today_sun.fav.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentFavoriteLocationsBinding
import com.example.not_today_sun.fav.viewmodel.FavViewModel
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.google.android.material.snackbar.Snackbar

class FavFragment : Fragment() {

    private var _binding: FragmentFavoriteLocationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavViewModel
    private lateinit var adapter: FavAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = (requireActivity() as MainActivity).weatherRepository
        viewModel = ViewModelProvider(
            this,
            FavViewModelFactory(repository)
        ).get(FavViewModel::class.java)

        setupRecyclerView()
        setupObservers()
        setupFab()
        setupResultListener()

        viewModel.getAllFavoriteLocations()
    }

    private fun setupRecyclerView() {
        adapter = FavAdapter(
            onDeleteClick = { location ->
                viewModel.deleteLocation(location)
            },
            onItemClick = { location ->
                openLocationWeatherFragment(location)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavFragment.adapter
        }
    }
    private fun openLocationWeatherFragment(location: FavoriteLocation) {
        val fragment = LocationWeatherFragment.newInstance(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = location.cityName
        )
        findNavController().navigate(
            R.id.action_nav_fav_to_locationWeatherFragment,
            fragment.arguments // Pass the Bundle from the factory
        )
    }

    private fun setupObservers() {
        viewModel.favoriteLocations.observe(viewLifecycleOwner) { locations ->
            adapter.submitList(locations.toList())
        }

        viewModel.navigateToMap.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate == true) {
                try {
                    findNavController().navigate(R.id.action_nav_fav_to_mapFragment)
                    viewModel.onNavigationComplete()
                } catch (e: Exception) {
                    Log.e("FavFragment", "Navigation failed", e)
                    Snackbar.make(
                        binding.coordinatorLayout,
                        "Navigation failed: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(
                    binding.coordinatorLayout,
                    message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Snackbar.make(
                    binding.coordinatorLayout,
                    message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupFab() {
        binding.fabAddLocation.setOnClickListener {
            viewModel.addNewLocation()
        }
    }

    private fun setupResultListener() {
        setFragmentResultListener("locationRequestKey") { _, bundle ->
            try {
                val latitude = bundle.getDouble("lat").also {
                    Log.d("FavFragment", "Received latitude: $it")
                }
                val longitude = bundle.getDouble("lon").also {
                    Log.d("FavFragment", "Received longitude: $it")
                }
                viewModel.addLocationToFavorites(latitude = latitude, longitude = longitude)
            } catch (e: Exception) {
                Log.e("FavFragment", "Error processing result", e)
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Error: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}