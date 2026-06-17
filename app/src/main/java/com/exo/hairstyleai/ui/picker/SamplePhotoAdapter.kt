package com.exo.hairstyleai.ui.picker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.ItemSamplePhotoBinding

/** 3-column grid of backend sample photos with single selection. */
class SamplePhotoAdapter(
    private val urls: List<String>,
    private val onSelect: (String) -> Unit,
) : RecyclerView.Adapter<SamplePhotoAdapter.VH>() {

    var selectedUrl: String? = null
        set(value) {
            if (field == value) return
            val old = field
            field = value
            indexOf(old)?.let { notifyItemChanged(it) }
            indexOf(value)?.let { notifyItemChanged(it) }
        }

    private fun indexOf(url: String?): Int? =
        url?.let { urls.indexOf(it).takeIf { i -> i >= 0 } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSamplePhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = urls.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = urls[position]
        val selected = url == selectedUrl

        holder.binding.sampleImage.load(url) { crossfade(true) }
        holder.binding.sampleOverlay.isVisible = selected
        holder.binding.tileRoot.setBackgroundResource(
            if (selected) R.drawable.bg_preset_tile_selected else R.drawable.bg_preset_tile,
        )
        holder.binding.root.setOnClickListener { onSelect(url) }
    }

    class VH(val binding: ItemSamplePhotoBinding) : RecyclerView.ViewHolder(binding.root)
}
