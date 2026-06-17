package com.exo.hairstyleai.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.DialogAppBinding

/** A small, app-themed confirmation dialog (dark card, accent / danger CTA). */
object AppDialog {

    fun show(
        context: Context,
        title: String,
        message: String,
        positiveText: String,
        negativeText: String,
        iconRes: Int? = null,
        iconTintRes: Int = R.color.accent,
        positiveDanger: Boolean = false,
        onPositive: () -> Unit,
        onNegative: () -> Unit = {},
    ) {
        val binding = DialogAppBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (iconRes != null) {
            binding.dialogIcon.isVisible = true
            binding.dialogIcon.setImageResource(iconRes)
            binding.dialogIcon.setColorFilter(ContextCompat.getColor(context, iconTintRes))
        } else {
            binding.dialogIcon.isVisible = false
        }

        binding.dialogTitle.text = title
        binding.dialogMessage.text = message
        binding.dialogPositive.text = positiveText
        binding.dialogNegative.text = negativeText
        if (positiveDanger) {
            binding.dialogPositive.setBackgroundResource(R.drawable.bg_dialog_btn_danger)
        }

        binding.dialogPositive.setOnClickListener {
            dialog.dismiss()
            onPositive()
        }
        binding.dialogNegative.setOnClickListener {
            dialog.dismiss()
            onNegative()
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.86f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
}
