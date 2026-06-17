package com.exo.hairstyleai.ui.picker

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.exo.hairstyleai.R
import com.exo.hairstyleai.data.api.ApiClient
import com.exo.hairstyleai.databinding.FragmentPickerBinding
import com.exo.hairstyleai.ui.edit.EditHairFragment
import com.exo.hairstyleai.util.FileUtils
import com.exo.hairstyleai.util.PhotoSource
import java.io.File

/** Screen 1 — pick a photo (library upload, sample, or camera capture). */
class PickerFragment : Fragment() {

    private enum class Tab { LIBRARY, CAMERA }

    private var _binding: FragmentPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var sampleAdapter: SamplePhotoAdapter

    private var activeTab = Tab.LIBRARY
    private var source: PhotoSource? = null
    private var cameraFile: File? = null
    private var pendingCameraFile: File? = null

    /** Sample selfies hosted by the backend (the "before" previews). */
    private val sampleUrls: List<String> = listOf(
        "afro_curls_b", "baby_blue_b", "curtain_bangs_b",
        "half_up_b", "modern_mullet_b", "pixie_cut_b",
    ).map { "${ApiClient.BASE_URL}static/previews/hair/$it.jpg" }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        runCatching { FileUtils.copyToCache(requireContext(), uri) }
            .onSuccess { file ->
                cameraFile = null
                setSource(PhotoSource.LocalFile(file, fromCamera = false))
            }
    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success: Boolean ->
        val file = pendingCameraFile
        if (success && file != null && file.length() > 0) {
            cameraFile = file
            setSource(PhotoSource.LocalFile(file, fromCamera = true))
        }
        render()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sampleAdapter = SamplePhotoAdapter(sampleUrls) { url ->
            cameraFile = null
            setSource(PhotoSource.Remote(url))
        }
        binding.sampleGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.sampleGrid.adapter = sampleAdapter

        binding.tabLibrary.setOnClickListener { switchTab(Tab.LIBRARY) }
        binding.tabCamera.setOnClickListener { switchTab(Tab.CAMERA) }

        binding.uploadButton.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
        binding.captureButton.setOnClickListener { launchCamera() }
        binding.retakeButton.setOnClickListener { launchCamera() }
        binding.nextButton.setOnClickListener { goToEdit() }
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_picker_to_settings)
        }

        observeResetSignal()
        render()
    }

    /** When returning from a saved edit, clear everything so the user picks anew. */
    private fun observeResetSignal() {
        val handle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        handle.getLiveData<Boolean>(EditHairFragment.RESULT_RESET_PICKER)
            .observe(viewLifecycleOwner) { reset ->
                if (reset == true) {
                    handle.remove<Boolean>(EditHairFragment.RESULT_RESET_PICKER)
                    resetSelection()
                }
            }
    }

    private fun resetSelection() {
        source = null
        cameraFile = null
        pendingCameraFile = null
        activeTab = Tab.LIBRARY
        render()
    }

    private fun launchCamera() {
        val (file, uri) = FileUtils.newCaptureTarget(requireContext())
        pendingCameraFile = file
        takePicture.launch(uri)
    }

    private fun switchTab(tab: Tab) {
        if (activeTab == tab) return
        activeTab = tab
        render()
    }

    private fun setSource(next: PhotoSource?) {
        source = next
        render()
    }

    private fun goToEdit() {
        val s = source ?: return
        val args = Bundle()
        when (s) {
            is PhotoSource.Remote -> args.putString(EditHairFragment.ARG_URL, s.url)
            is PhotoSource.LocalFile -> {
                args.putString(EditHairFragment.ARG_PATH, s.file.absolutePath)
                args.putBoolean(EditHairFragment.ARG_FROM_CAMERA, s.fromCamera)
            }
        }
        findNavController().navigate(R.id.action_picker_to_edit, args)
    }

    private fun render() {
        val b = _binding ?: return

        // tabs
        styleTab(b.tabLibrary, b.tabLibraryIcon, b.tabLibraryText, activeTab == Tab.LIBRARY)
        styleTab(b.tabCamera, b.tabCameraIcon, b.tabCameraText, activeTab == Tab.CAMERA)
        b.libraryView.isVisible = activeTab == Tab.LIBRARY
        b.cameraView.isVisible = activeTab == Tab.CAMERA

        // library content — show the uploaded photo only when it's the active source
        val gallery = (source as? PhotoSource.LocalFile)?.takeIf { !it.fromCamera }
        b.uploadPreviewWrap.isVisible = gallery != null
        gallery?.let { b.uploadPreview.load(it.file) { crossfade(true) } }
        sampleAdapter.selectedUrl = (source as? PhotoSource.Remote)?.url

        // camera content
        val hasShot = cameraFile != null
        b.cameraShot.isVisible = hasShot
        b.cameraPlaceholder.isVisible = !hasShot
        b.captureButton.isVisible = !hasShot
        b.retakeButton.isVisible = hasShot
        if (hasShot) b.cameraShot.load(cameraFile) { crossfade(true) }

        // next button
        val ready = source != null
        b.nextButton.isEnabled = ready
        val tint = color(if (ready) R.color.white else R.color.text_disabled)
        b.nextButtonText.setTextColor(tint)
        b.nextButtonIcon.setColorFilter(tint)
    }

    private fun styleTab(container: LinearLayout, icon: ImageView, text: TextView, selected: Boolean) {
        container.setBackgroundResource(
            if (selected) R.drawable.bg_segment_selected else android.R.color.transparent,
        )
        val tint = color(if (selected) R.color.white else R.color.text_muted)
        icon.setColorFilter(tint)
        text.setTextColor(tint)
    }

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
