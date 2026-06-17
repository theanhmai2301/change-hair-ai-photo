package com.exo.hairstyleai.firstopen.survey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.ItemSurveyBinding

/**
 * Multi-select grid of survey topics. Seeded with [initialSelected] so the selection
 * carries over when the survey hands off from one step-activity to the next, and reports
 * the full selected set on every tap so the host can advance / enable Continue.
 */
class SurveyAdapter(
    private val topics: List<SurveyTopic>,
    initialSelected: IntArray,
    private val onSelectionChanged: (IntArray) -> Unit,
) : RecyclerView.Adapter<SurveyAdapter.VH>() {

    private val selected = linkedSetOf<Int>().apply { initialSelected.forEach { add(it) } }

    fun selected(): IntArray = selected.toIntArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSurveyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = topics.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val topic = topics[position]
        holder.binding.surveyEmoji.text = topic.emoji
        holder.binding.surveyName.setText(topic.nameRes)
        bindSelection(holder, selected.contains(position))
        holder.binding.surveyCard.setOnClickListener {
            if (!selected.add(position)) selected.remove(position)
            bindSelection(holder, selected.contains(position))
            onSelectionChanged(selected.toIntArray())
        }
    }

    private fun bindSelection(holder: VH, selected: Boolean) {
        holder.binding.surveyCard.setBackgroundResource(
            if (selected) R.drawable.bg_survey_tile_selected else R.drawable.bg_survey_tile,
        )
        holder.binding.surveyCheck.visibility = if (selected) View.VISIBLE else View.GONE
    }

    class VH(val binding: ItemSurveyBinding) : RecyclerView.ViewHolder(binding.root)
}
