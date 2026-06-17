package com.exo.hairstyleai.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.DialogThemeBinding

/** Light / Dark / System theme picker. */
object ThemeDialog {

    fun show(context: Context, currentMode: Int, onPick: (Int) -> Unit) {
        val binding = DialogThemeBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val options = listOf(
            binding.themeSystem to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            binding.themeLight to AppCompatDelegate.MODE_NIGHT_NO,
            binding.themeDark to AppCompatDelegate.MODE_NIGHT_YES,
        )
        options.forEach { (row, mode) ->
            val selected = mode == currentMode
            row.setBackgroundResource(
                if (selected) R.drawable.bg_preset_tile_selected else R.drawable.bg_preset_tile,
            )
            row.setTextColor(
                ContextCompat.getColor(context, if (selected) R.color.accent else R.color.text_primary),
            )
            row.setOnClickListener {
                dialog.dismiss()
                onPick(mode)
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.86f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
}
