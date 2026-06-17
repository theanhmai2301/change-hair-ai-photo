package com.exo.hairstyleai.firstopen.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.ItemLanguageBinding

/** Radio-style single-select language list (flag + name + radio). */
class LanguageAdapter(
    private val items: List<Language>,
    initialSelected: String?,
    private val onSelect: (Language) -> Unit,
) : RecyclerView.Adapter<LanguageAdapter.VH>() {

    private var selectedCode: String? = initialSelected

    fun setSelected(code: String?) {
        if (selectedCode == code) return
        val old = indexOf(selectedCode)
        selectedCode = code
        old?.let { notifyItemChanged(it) }
        indexOf(code)?.let { notifyItemChanged(it) }
    }

    private fun indexOf(code: String?): Int? =
        code?.let { items.indexOfFirst { l -> l.code == it }.takeIf { i -> i >= 0 } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.binding.root.context
        val selected = item.code == selectedCode

        holder.binding.languageFlag.text = item.flag
        holder.binding.languageName.text = item.name
        holder.binding.languageRadio.setImageResource(
            if (selected) R.drawable.ic_check_circle else R.drawable.ic_radio_off,
        )
        holder.binding.languageRadio.setColorFilter(
            ContextCompat.getColor(ctx, if (selected) R.color.accent else R.color.text_disabled),
        )
        holder.binding.languageRow.setBackgroundResource(
            if (selected) R.drawable.bg_preset_tile_selected else R.drawable.bg_preset_tile,
        )
        holder.binding.root.setOnClickListener { onSelect(item) }
    }

    class VH(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)
}
