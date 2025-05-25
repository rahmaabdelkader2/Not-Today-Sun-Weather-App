package com.example.not_today_sun.fav.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.not_today_sun.databinding.ItemFavoriteLocationBinding
import com.example.not_today_sun.model.pojo.FavoriteLocation

class FavAdapter(
    private val onDeleteClick: (FavoriteLocation) -> Unit,
    private val onItemClick: (FavoriteLocation) -> Unit
) : ListAdapter<FavoriteLocation, FavAdapter.FavViewHolder>(FavoriteLocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = ItemFavoriteLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavViewHolder(binding, onDeleteClick, onItemClick)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavViewHolder(
        private val binding: ItemFavoriteLocationBinding,
        private val onDeleteClick: (FavoriteLocation) -> Unit,
        private val onItemClick: (FavoriteLocation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: FavoriteLocation) {
            binding.cityNameTextView.text = location.cityName
            binding.coordinatesTextView.text = "Lat: ${location.latitude}, Lon: ${location.longitude}"

            // Display min/max temps if available
            location.minTemp?.let { min ->
                location.maxTemp?.let { max ->
                    binding.tempTextView.text = "Min: ${min}°C / Max: ${max}°C"
                }
            } ?: run {
                binding.tempTextView.text = "Temp data not available"
            }

            binding.deleteButton.setOnClickListener {
                // Show confirmation dialog
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Location")
                    .setMessage("Are you sure you want to delete ${location.cityName} from your favorites?")
                    .setPositiveButton("Yes") { _, _ ->
                        onDeleteClick(location) // Proceed with deletion
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss() // Cancel deletion
                    }
                    .setCancelable(true)
                    .show()
            }
            binding.root.setOnClickListener {
                onItemClick(location)
            }
        }
    }

    class FavoriteLocationDiffCallback : DiffUtil.ItemCallback<FavoriteLocation>() {
        override fun areItemsTheSame(oldItem: FavoriteLocation, newItem: FavoriteLocation): Boolean {
            return oldItem.cityName == newItem.cityName
        }

        override fun areContentsTheSame(oldItem: FavoriteLocation, newItem: FavoriteLocation): Boolean {
            return oldItem == newItem
        }
    }
}