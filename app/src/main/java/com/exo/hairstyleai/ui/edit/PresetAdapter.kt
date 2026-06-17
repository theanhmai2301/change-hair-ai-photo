package com.exo.hairstyleai.ui.edit

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.exo.hairstyleai.R
import com.exo.hairstyleai.data.model.HairPreset
import com.exo.hairstyleai.data.model.Swatch
import com.exo.hairstyleai.databinding.ItemPresetBinding

/** 3-column grid of hair presets (filtered by category) with single selection. */
class PresetAdapter(
    private val onSelect: (HairPreset) -> Unit,
) : RecyclerView.Adapter<PresetAdapter.VH>() {

    private val items = mutableListOf<HairPreset>()

    var selectedId: String? = null
        set(value) {
            if (field == value) return
            val old = field
            field = value
            indexOf(old)?.let { notifyItemChanged(it) }
            indexOf(value)?.let { notifyItemChanged(it) }
        }

    private fun indexOf(id: String?): Int? =
        id?.let { items.indexOfFirst { p -> p.id == it }.takeIf { i -> i >= 0 } }

    fun submit(list: List<HairPreset>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPresetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val preset = items[position]
        val ctx = holder.binding.root.context
        val selected = preset.id == selectedId

        holder.binding.presetImage.load(preset.previewUrl) { crossfade(true) }
        holder.binding.presetName.text = preset.name
        holder.binding.presetName.setTextColor(
            ContextCompat.getColor(ctx, if (selected) R.color.accent else R.color.text_muted),
        )

        holder.binding.presetCard.setBackgroundResource(
            if (selected) R.drawable.bg_preset_tile_selected else R.drawable.bg_preset_tile,
        )
        holder.binding.presetOverlay.isVisible = selected

        // swatch ring (colors only)
        val swatch = preset.swatch
        if (swatch != null) {
            holder.binding.presetSwatch.isVisible = true
            holder.binding.presetSwatch.background = buildSwatch(holder, swatch)
        } else {
            holder.binding.presetSwatch.isVisible = false
        }

        holder.binding.root.setOnClickListener { onSelect(preset) }
    }

    private fun buildSwatch(holder: VH, swatch: Swatch): GradientDrawable {
        val strokePx = dp(holder, 1f)
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            when (swatch) {
                is Swatch.Solid -> setColor(swatch.color)
                is Swatch.Rainbow -> {
                    gradientType = GradientDrawable.SWEEP_GRADIENT
                    colors = intArrayOf(
                        Color.parseColor("#F4A6C0"), Color.parseColor("#B39DDB"),
                        Color.parseColor("#7EC8E3"), Color.parseColor("#9BE8B0"),
                        Color.parseColor("#FFE08A"), Color.parseColor("#F4A6C0"),
                    )
                }
            }
            setStroke(strokePx, Color.parseColor("#66FFFFFF"))
        }
    }

    private fun dp(holder: VH, value: Float): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, holder.binding.root.resources.displayMetrics,
    ).toInt()

    class VH(val binding: ItemPresetBinding) : RecyclerView.ViewHolder(binding.root)
}
