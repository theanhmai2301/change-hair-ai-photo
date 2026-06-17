package com.exo.hairstyleai.ui.picker

import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.exo.hairstyleai.R
import com.exo.hairstyleai.databinding.FragmentPickerBinding
import com.exo.hairstyleai.ui.edit.EditHairFragment
import com.exo.hairstyleai.util.FileUtils
import java.io.File

/** Home — pick a photo (gallery upload or camera) and go straight to the editor. */
class PickerFragment : Fragment() {

    private var _binding: FragmentPickerBinding? = null
    private val binding get() = _binding!!

    private var pendingCameraFile: File? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        runCatching { FileUtils.copyToCache(requireContext(), uri) }
            .onSuccess { file -> goToEdit(file, fromCamera = false) }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success: Boolean ->
        val file = pendingCameraFile
        if (success && file != null && file.length() > 0) {
            goToEdit(file, fromCamera = true)
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
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_picker_to_settings)
        }

        applyAccentGradient()
    }

    /** Paint the "with AI" headline with a left-to-right accent gradient. */
    private fun applyAccentGradient() {
        binding.heroAccent.post {
            val tv = _binding?.heroAccent ?: return@post
            val width = tv.paint.measureText(tv.text.toString())
            if (width <= 0f) return@post
            tv.paint.shader = LinearGradient(
                0f, 0f, width, tv.textSize,
                intArrayOf(color(R.color.accent), color(R.color.accent_dark)),
                null, Shader.TileMode.CLAMP,
            )
            tv.invalidate()
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

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
