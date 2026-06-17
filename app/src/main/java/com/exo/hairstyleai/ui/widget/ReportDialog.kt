package com.exo.hairstyleai.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.DialogReportBinding
import com.exo.hairstyleai.util.AppLinks

/** Report a generated image: pick a reason, then compose an email to support. */
object ReportDialog {

    fun show(context: Context, imageUrl: String?) {
        val binding = DialogReportBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val reasons = listOf(
            binding.reason1 to R.string.report_reason_inappropriate,
            binding.reason2 to R.string.report_reason_face,
            binding.reason3 to R.string.report_reason_quality,
            binding.reason4 to R.string.report_reason_other,
        )
        val views = reasons.map { it.first }
        var selectedReason: String? = null

        reasons.forEach { (tv, resId) ->
            tv.setOnClickListener {
                selectedReason = context.getString(resId)
                views.forEach { v ->
                    val sel = v === tv
                    v.setBackgroundResource(
                        if (sel) R.drawable.bg_preset_tile_selected else R.drawable.bg_preset_tile,
                    )
                    v.setTextColor(color(context, if (sel) R.color.accent else R.color.text_primary))
                }
                binding.sendButton.isEnabled = true
                binding.sendButton.setTextColor(color(context, R.color.white))
            }
        }

        binding.sendButton.setOnClickListener {
            val reason = selectedReason ?: return@setOnClickListener
            val body = buildString {
                append(context.getString(R.string.report_body_prefix))
                append("\n\n• Reason: ").append(reason)
                if (!imageUrl.isNullOrBlank()) append("\n• Image: ").append(imageUrl)
            }
            AppLinks.email(context, context.getString(R.string.report_subject), body)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.88f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    private fun color(context: Context, resId: Int): Int =
        ContextCompat.getColor(context, resId)
}
