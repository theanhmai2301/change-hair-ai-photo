package com.exo.hairstyleai.ui.edit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.exo.hairstyleai.R
import com.exo.hairstyleai.data.HairRepository
import com.exo.hairstyleai.data.model.HairCategory
import com.exo.hairstyleai.data.model.HairPreset
import com.exo.hairstyleai.databinding.FragmentEditHairBinding
import com.exo.hairstyleai.ui.widget.AppDialog
import com.exo.hairstyleai.ui.widget.ReportDialog
import com.exo.hairstyleai.util.ImageSaver
import com.exo.hairstyleai.util.PhotoSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

/** Screen 2 — apply hair cut / color presets, chaining results so layers stack. */
class EditHairFragment : Fragment() {

    companion object {
        // Keys must match the <argument> names in nav_graph.xml.
        const val ARG_URL = "photoUrl"
        const val ARG_PATH = "photoPath"
        const val ARG_FROM_CAMERA = "fromCamera"

        // Result flag handed back to PickerFragment: clear its selection so the
        // user starts fresh after a save.
        const val RESULT_RESET_PICKER = "reset_picker"

        private val STEPS = intArrayOf(
            R.string.step_analyze, R.string.step_face,
            R.string.step_apply, R.string.step_finish,
        )
    }

    private var _binding: FragmentEditHairBinding? = null
    private val binding get() = _binding!!

    private lateinit var presetAdapter: PresetAdapter

    private var source: PhotoSource? = null
    private var allPresets: List<HairPreset> = emptyList()
    private var hairTab = HairCategory.CUT
    private var selectedPreset: HairPreset? = null

    // The latest AI-generated image. Subsequent applies chain on top of it so a
    // color layer and a cut layer stack instead of each resetting to the original.
    private var resultUrl: String? = null

    // Decoded result bytes (from the response's base64, or one download of its URL),
    // reused for both preview and save so neither pays a repeat download.
    private var resultBytes: ByteArray? = null
    private var appliedColorName: String? = null
    private var appliedCutName: String? = null

    // True once the current result has been saved — lets us skip the
    // "you'll lose your edit" warning when nothing would actually be lost.
    private var resultSaved = false

    private var isProcessing = false
    private var saved = false
    private var stepIndex = 0

    private var applyJob: Job? = null
    private var stepJob: Job? = null
    private var savedResetJob: Job? = null

    private val dots = mutableListOf<View>()

    private val requestStorage = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) doSave() else showError(getString(R.string.save_failed)) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditHairBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        source = parseSource()
        val src = source
        if (src == null) {
            navigateBack()
            return
        }
        binding.previewImage.load(src.model) {
            crossfade(true)
            allowHardware(false)
        }

        initDots()

        presetAdapter = PresetAdapter { preset -> onPresetSelected(preset) }
        binding.presetGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.presetGrid.adapter = presetAdapter

        binding.backButton.setOnClickListener { attemptExit() }
        // Intercept the system back button / gesture (tied to this view's lifecycle).
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = attemptExit()
            },
        )

        binding.tabColor.setOnClickListener { setHairTab(HairCategory.COLOR) }
        binding.tabCut.setOnClickListener { setHairTab(HairCategory.CUT) }
        binding.restoreButton.setOnClickListener { handleRestore() }
        binding.saveButton.setOnClickListener { handleSave() }
        binding.applyButton.setOnClickListener { handleApply() }
        binding.reportButton.setOnClickListener { ReportDialog.show(requireContext(), resultUrl) }

        render()
        loadPresets()
    }

    private fun parseSource(): PhotoSource? {
        val args = arguments ?: return null
        args.getString(ARG_URL)?.let { return PhotoSource.Remote(it) }
        args.getString(ARG_PATH)?.let {
            return PhotoSource.LocalFile(File(it), args.getBoolean(ARG_FROM_CAMERA, false))
        }
        return null
    }

    /* ── presets ── */

    private fun loadPresets() {
        val b = _binding ?: return
        b.presetsLoading.isVisible = true
        b.presetsError.isVisible = false
        b.presetGrid.isVisible = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val presets = HairRepository.loadPresets()
                allPresets = presets
                val bb = _binding ?: return@launch
                bb.presetsLoading.isVisible = false
                bb.presetGrid.isVisible = true
                updateGrid()
            } catch (c: CancellationException) {
                throw c
            } catch (e: Exception) {
                val bb = _binding ?: return@launch
                bb.presetsLoading.isVisible = false
                bb.presetsError.isVisible = true
                // Localized message only — never surface the raw exception string to users.
                bb.presetsError.text = getString(R.string.presets_error)
            }
        }
    }

    private fun updateGrid() {
        presetAdapter.submit(allPresets.filter { it.category == hairTab })
        presetAdapter.selectedId = selectedPreset?.id
    }

    private fun setHairTab(tab: HairCategory) {
        if (isProcessing) return
        if (hairTab == tab) return
        hairTab = tab
        // Force a fresh pick in the new category so Apply targets what's on screen.
        selectedPreset = null
        updateGrid()
        render()
    }

    private fun onPresetSelected(preset: HairPreset) {
        if (isProcessing) return
        selectedPreset = preset
        presetAdapter.selectedId = preset.id
        render()
    }

    /* ── apply / restore / save ── */

    private fun handleApply() {
        val preset = selectedPreset ?: return
        if (isProcessing) return

        // Chain on top of the last result if there is one, otherwise the original.
        val input: PhotoSource = resultUrl?.let { PhotoSource.Remote(it) } ?: source ?: return

        hideError()
        saved = false
        isProcessing = true
        render()
        startStepTicker()

        applyJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = HairRepository.applyHair(input, preset.id, quality = "medium")
                // The backend returns the image inline (base64) alongside a hosted URL.
                // Grab the bytes once — base64 needs no download; a URL is fetched a single
                // time here — then reuse them for both the preview and the save, so neither
                // step pays a second (slow) network round-trip.
                val bytes = ImageSaver.fetchBytes(result.image_base64, result.url)
                    ?: throw IllegalStateException(getString(R.string.apply_error))
                resultBytes = bytes
                resultUrl = result.url // kept so the next layer can chain on the server
                resultSaved = false
                if (preset.category == HairCategory.COLOR) {
                    appliedColorName = preset.name
                } else {
                    appliedCutName = preset.name
                }
                _binding?.previewImage?.load(ByteBuffer.wrap(bytes)) {
                    crossfade(true)
                    allowHardware(false)
                }
            } catch (c: CancellationException) {
                throw c
            } catch (e: Exception) {
                // Show the localized message, not the raw (English/technical) exception text.
                showError(getString(R.string.apply_error))
            } finally {
                isProcessing = false
                stopStepTicker()
                render()
            }
        }
    }

    private fun handleRestore() {
        if (isProcessing) return
        if (resultBytes == null) return
        // Restore sits next to Save — confirm so an accidental tap can't wipe the edit.
        showRestoreDialog()
    }

    private fun showRestoreDialog() {
        AppDialog.show(
            context = requireContext(),
            iconRes = R.drawable.ic_alert,
            iconTintRes = R.color.danger,
            title = getString(R.string.restore_title),
            message = getString(R.string.restore_message),
            positiveText = getString(R.string.restore_confirm),
            negativeText = getString(R.string.stay_here),
            positiveDanger = true,
            onPositive = { doRestore() },
        )
    }

    private fun doRestore() {
        applyJob?.cancel()
        stopStepTicker()
        resultUrl = null
        resultBytes = null
        appliedColorName = null
        appliedCutName = null
        saved = false
        resultSaved = false
        isProcessing = false
        source?.let { s ->
            _binding?.previewImage?.load(s.model) {
                crossfade(true)
                allowHardware(false)
            }
        }
        render()
    }

    private fun handleSave() {
        if (isProcessing) return
        if (resultBytes == null) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission()) {
            requestStorage.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        doSave()
    }

    private fun doSave() {
        val bytes = resultBytes ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val name = "hairstyle_${selectedPreset?.id ?: "ai"}_${System.currentTimeMillis()}.png"
            val ok = ImageSaver.saveBytes(requireContext(), bytes, name)
            if (_binding == null) return@launch
            if (ok) {
                saved = true
                resultSaved = true
                render()
                showSavedDialog()
                savedResetJob?.cancel()
                savedResetJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(2500)
                    saved = false
                    render()
                }
            } else {
                showError(getString(R.string.save_failed))
            }
        }
    }

    private fun hasStoragePermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED

    /* ── navigation / dialogs ── */

    private fun navigateBack() {
        // If the result was already saved, tell the picker to reset so the user
        // can pick a different photo instead of seeing the old selection.
        if (resultSaved) {
            findNavController().previousBackStackEntry
                ?.savedStateHandle?.set(RESULT_RESET_PICKER, true)
        }
        findNavController().popBackStack()
    }

    /** Back / exit guard: warn before discarding an unsaved generated result. */
    private fun attemptExit() {
        if (isProcessing) return // locked: can't leave / change the photo mid-generation
        if (resultBytes != null && !resultSaved) showDiscardDialog() else navigateBack()
    }

    private fun showSavedDialog() {
        AppDialog.show(
            context = requireContext(),
            iconRes = R.drawable.ic_check_circle,
            iconTintRes = R.color.accent,
            title = getString(R.string.saved_title),
            message = getString(R.string.saved_message),
            positiveText = getString(R.string.saved_pick_another),
            negativeText = getString(R.string.stay_here),
            onPositive = { navigateBack() },
        )
    }

    private fun showDiscardDialog() {
        AppDialog.show(
            context = requireContext(),
            iconRes = R.drawable.ic_alert,
            iconTintRes = R.color.danger,
            title = getString(R.string.discard_title),
            message = getString(R.string.discard_message),
            positiveText = getString(R.string.discard_leave),
            negativeText = getString(R.string.stay_here),
            positiveDanger = true,
            onPositive = { navigateBack() },
        )
    }

    /* ── processing step ticker ── */

    private fun startStepTicker() {
        stepJob?.cancel()
        stepJob = viewLifecycleOwner.lifecycleScope.launch {
            var i = 0
            setStep(0)
            while (isActive) {
                delay(1100)
                i = minOf(i + 1, STEPS.lastIndex)
                setStep(i)
            }
        }
    }

    private fun stopStepTicker() {
        stepJob?.cancel()
        stepJob = null
    }

    private fun setStep(i: Int) {
        val b = _binding ?: return
        stepIndex = i
        val text = getString(STEPS[i])
        b.processingStep.text = text
        if (isProcessing) b.applyText.text = text
        updateDots(i)
    }

    /* ── render ── */

    private fun render() {
        val b = _binding ?: return
        val isEdited = resultBytes != null
        val canEdit = isEdited && !isProcessing

        b.previewImage.alpha = if (isProcessing) 0.45f else 1f
        b.processingOverlay.isVisible = isProcessing
        b.reportButton.isVisible = isEdited && !isProcessing

        // While a result is generating, lock the screen: block leaving / changing /
        // clearing the photo. The only thing that should move is the spinner.
        b.backButton.isEnabled = !isProcessing
        b.backButton.alpha = if (isProcessing) 0.4f else 1f

        // AI badge
        if (isEdited) {
            b.aiBadge.setBackgroundResource(R.drawable.bg_badge_accent)
            b.aiBadgeText.setText(R.string.badge_ai)
            b.aiBadgeText.setTextColor(color(R.color.accent))
            b.aiBadgeIcon.setColorFilter(color(R.color.accent))
        } else {
            b.aiBadge.setBackgroundResource(R.drawable.bg_badge_glass)
            b.aiBadgeText.setText(R.string.badge_unedited)
            b.aiBadgeText.setTextColor(color(R.color.text_muted))
            b.aiBadgeIcon.setColorFilter(color(R.color.text_muted))
        }

        // applied-layer tags (one per generated layer, so color + cut both show)
        b.colorTag.isVisible = appliedColorName != null
        appliedColorName?.let { b.colorTag.text = "🎨 $it" }
        b.cutTag.isVisible = appliedCutName != null
        appliedCutName?.let { b.cutTag.text = "✂️ $it" }

        // restore button (also locked while generating)
        b.restoreButton.isEnabled = canEdit
        b.restoreButton.setBackgroundResource(
            if (canEdit) R.drawable.bg_icon_button_danger else R.drawable.bg_icon_button,
        )
        b.restoreIcon.setColorFilter(color(if (canEdit) R.color.danger else R.color.text_disabled))

        // save button (also locked while generating)
        b.saveButton.isEnabled = canEdit
        b.saveButton.setBackgroundResource(
            if (canEdit) R.drawable.bg_icon_button_accent else R.drawable.bg_icon_button,
        )
        b.saveIcon.setImageResource(if (saved) R.drawable.ic_check else R.drawable.ic_download)
        b.saveIcon.setColorFilter(color(if (canEdit) R.color.white else R.color.text_disabled))

        // hair tabs + hint
        styleHairTab(b.tabColor, hairTab == HairCategory.COLOR)
        styleHairTab(b.tabCut, hairTab == HairCategory.CUT)
        b.hintText.setText(
            if (hairTab == HairCategory.COLOR) R.string.hint_color else R.string.hint_cut,
        )

        // tabs + preset grid are locked (and dimmed) while generating
        b.tabCut.isEnabled = !isProcessing
        b.tabColor.isEnabled = !isProcessing
        b.tabCut.alpha = if (isProcessing) 0.5f else 1f
        b.tabColor.alpha = if (isProcessing) 0.5f else 1f
        b.presetGrid.alpha = if (isProcessing) 0.5f else 1f

        // apply button
        val hasSelection = selectedPreset != null
        b.applyButton.isEnabled = hasSelection
        b.applySpinner.isVisible = isProcessing
        b.applyIcon.isVisible = !isProcessing
        val applyTint = color(if (hasSelection) R.color.white else R.color.text_disabled)
        b.applyText.setTextColor(applyTint)
        b.applyIcon.setColorFilter(applyTint)
        b.applyText.text =
            if (isProcessing) getString(STEPS[stepIndex]) else getString(R.string.apply_hair)
    }

    private fun styleHairTab(tab: TextView, selected: Boolean) {
        tab.setBackgroundResource(
            if (selected) R.drawable.bg_segment_selected else android.R.color.transparent,
        )
        tab.setTextColor(color(if (selected) R.color.white else R.color.text_muted))
    }

    /* ── processing dots ── */

    private fun initDots() {
        val b = _binding ?: return
        b.processingDots.removeAllViews()
        dots.clear()
        repeat(STEPS.size) {
            val view = View(requireContext())
            val lp = LinearLayout.LayoutParams(dp(6), dp(6))
            lp.marginStart = dp(3)
            lp.marginEnd = dp(3)
            view.layoutParams = lp
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(3).toFloat()
                setColor(color(R.color.text_disabled))
            }
            b.processingDots.addView(view)
            dots.add(view)
        }
    }

    private fun updateDots(active: Int) {
        dots.forEachIndexed { i, view ->
            val lp = view.layoutParams as LinearLayout.LayoutParams
            lp.width = dp(if (i == active) 20 else 6)
            view.layoutParams = lp
            (view.background as GradientDrawable).setColor(
                color(if (i <= active) R.color.accent else R.color.text_disabled),
            )
        }
    }

    /* ── error banner ── */

    private fun showError(message: String) {
        val b = _binding ?: return
        b.errorText.text = message
        b.errorBanner.isVisible = true
    }

    private fun hideError() {
        _binding?.errorBanner?.isVisible = false
    }

    /* ── helpers ── */

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics,
    ).toInt()

    override fun onDestroyView() {
        applyJob?.cancel()
        stepJob?.cancel()
        savedResetJob?.cancel()
        dots.clear()
        _binding = null
        super.onDestroyView()
    }
}
