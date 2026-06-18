package com.exo.hairstyleai.ui.picker

import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.FragmentPickerBinding
import com.exo.hairstyleai.ui.edit.EditHairFragment
import com.exo.hairstyleai.util.FileUtils
import java.io.File

/**
 * Home — pick a photo (gallery upload or camera). The pick is previewed in the
 * showcase first with a "Next" button, so the user can swap it before editing.
 */
class PickerFragment : Fragment() {

    private var _binding: FragmentPickerBinding? = null
    private val binding get() = _binding!!

    private var pendingCameraFile: File? = null
    private var selectedFile: File? = null
    private var selectedFromCamera = false

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        runCatching { FileUtils.copyToCache(requireContext(), uri) }
            .onSuccess { file -> setSelected(file, fromCamera = false) }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success: Boolean ->
        val file = pendingCameraFile
        if (success && file != null && file.length() > 0) {
            setSelected(file, fromCamera = true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadButton.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
        binding.captureButton.setOnClickListener { launchCamera() }
        binding.nextButton.setOnClickListener {
            selectedFile?.let { goToEdit(it, selectedFromCamera) }
        }
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_picker_to_settings)
        }

        setHeroTitle()
        observeResetSignal()
        render()
    }

    /** A picked photo waits in the showcase with a Next button; tapping Upload/Camera swaps it. */
    private fun setSelected(file: File, fromCamera: Boolean) {
        selectedFile = file
        selectedFromCamera = fromCamera
        render()
    }

    private fun render() {
        val b = _binding ?: return
        val file = selectedFile
        if (file != null) {
            b.showcaseImage.load(file) { crossfade(true) }
        } else {
            b.showcaseImage.load(R.drawable.ai_photo_change_hair)
        }
        b.nextButton.isVisible = file != null
    }

    /** After a saved edit the picker resets so the user starts fresh. */
    private fun observeResetSignal() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle.getLiveData<Boolean>(EditHairFragment.RESULT_RESET_PICKER)
            .observe(viewLifecycleOwner) { reset ->
                if (reset == true) {
                    handle.remove<Boolean>(EditHairFragment.RESULT_RESET_PICKER)
                    selectedFile = null
                    selectedFromCamera = false
                    render()
                }
            }
    }

    private fun launchCamera() {
        val (file, uri) = FileUtils.newCaptureTarget(requireContext())
        pendingCameraFile = file
        takePicture.launch(uri)
    }

    private fun goToEdit(file: File, fromCamera: Boolean) {
        val args = Bundle().apply {
            putString(EditHairFragment.ARG_PATH, file.absolutePath)
            putBoolean(EditHairFragment.ARG_FROM_CAMERA, fromCamera)
        }
        findNavController().navigate(R.id.action_picker_to_edit, args)
    }

    /** One-line headline: "<title> <accent>" with the accent part in the brand colour. */
    private fun setHeroTitle() {
        val sb = SpannableStringBuilder()
            .append(getString(R.string.home_title))
            .append(" ")
        val start = sb.length
        sb.append(getString(R.string.home_title_accent))
        sb.setSpan(
            ForegroundColorSpan(color(R.color.accent)),
            start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        binding.heroTitle.text = sb
    }

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
