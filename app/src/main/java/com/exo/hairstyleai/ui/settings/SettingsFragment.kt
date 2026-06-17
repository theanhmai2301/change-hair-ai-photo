package com.exo.hairstyleai.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.FragmentSettingsBinding
import com.exo.hairstyleai.firstopen.language.LanguageFO2Activity
import com.exo.hairstyleai.ui.widget.ThemeDialog
import com.exo.hairstyleai.util.AppLinks
import com.exo.hairstyleai.util.Prefs

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.rowLanguage.setOnClickListener {
            LanguageFO2Activity.startFromMain(requireContext())
        }
        binding.rowTheme.setOnClickListener {
            val prefs = Prefs(requireContext())
            ThemeDialog.show(requireContext(), prefs.themeMode) { mode ->
                if (prefs.themeMode != mode) {
                    prefs.themeMode = mode
                    AppCompatDelegate.setDefaultNightMode(mode) // recreates the activity
                }
            }
        }
        binding.rowShare.setOnClickListener {
            AppLinks.shareApp(requireContext(), getString(R.string.share_text))
        }
        binding.rowRate.setOnClickListener {
            AppLinks.rateApp(requireContext())
        }
        binding.rowFeedback.setOnClickListener {
            AppLinks.email(requireContext(), getString(R.string.feedback_subject), "")
        }
        binding.rowPrivacy.setOnClickListener {
            AppLinks.openUrl(requireContext(), AppLinks.PRIVACY_POLICY_URL)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
