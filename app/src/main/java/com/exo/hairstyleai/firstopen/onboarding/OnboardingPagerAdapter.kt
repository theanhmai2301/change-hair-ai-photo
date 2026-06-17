package com.exo.hairstyleai.firstopen.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exo.hairstyleai.databinding.ItemOnboardingPageBinding

class OnboardingPagerAdapter(
    private val pages: List<Page>,
) : RecyclerView.Adapter<OnboardingPagerAdapter.VH>() {

    data class Page(val imageRes: Int, val titleRes: Int, val descRes: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = pages.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val page = pages[position]
        holder.binding.pageImage.setImageResource(page.imageRes)
        holder.binding.pageTitle.setText(page.titleRes)
        holder.binding.pageDesc.setText(page.descRes)
    }

    class VH(val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root)
}
