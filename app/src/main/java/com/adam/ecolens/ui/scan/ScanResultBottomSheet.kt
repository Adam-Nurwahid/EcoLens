package com.adam.ecolens.ui.scan

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.adam.ecolens.TFLiteClassifier
import com.adam.ecolens.data.model.WasteCategory
import com.adam.ecolens.databinding.BottomSheetScanResultBinding
import com.adam.ecolens.ui.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScanResultBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetScanResultBinding? = null
    private val binding get() = _binding!!

    // Share the ScanViewModel that belongs to the parent ScanFragment.
    private val scanViewModel: ScanViewModel by lazy {
        val parent = requireParentFragment()
        androidx.lifecycle.ViewModelProvider(parent, ViewModelFactory(parent.requireContext()))
            .get(ScanViewModel::class.java)
    }

    var onScanAgainClickListener: (() -> Unit)? = null

    companion object {
        fun newInstance(
            bitmap: Bitmap,
            label: String,
            confidence: Float,
            probabilities: FloatArray
        ): ScanResultBottomSheet {
            val sheet = ScanResultBottomSheet()
            sheet.currentBitmap = bitmap
            sheet.predictedLabel = label
            sheet.confidenceScore = confidence
            sheet.allProbabilities = probabilities
            return sheet
        }
    }

    private var currentBitmap: Bitmap? = null
    private var predictedLabel: String = ""
    private var confidenceScore: Float = 0f
    private var allProbabilities: FloatArray = floatArrayOf()

    // ---------------------------------------------------------------------------
    // Force expanded state so the button is always visible
    // ---------------------------------------------------------------------------

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBitmap?.let {
            binding.imgResultThumbnail.setImageBitmap(it)
        }

        val category = WasteCategory.fromLabel(predictedLabel)
        binding.tvResultLabel.text = category.displayName
        binding.tvResultLabel.setTextColor(ContextCompat.getColor(requireContext(), category.colorResId))
        binding.cardResultCategory.setCardBackgroundColor(ContextCompat.getColor(requireContext(), category.lightBgResId))

        binding.tvConfidence.text = "Tingkat Akurasi AI: %.2f%%".format(confidenceScore)
        binding.tvDisposalTip.text = category.disposalTip

        // Sort class probabilities descending before display
        val probsDetail = TFLiteClassifier.CLASS_NAMES
            .zip(allProbabilities.toList())
            .sortedByDescending { it.second }
            .joinToString(" • ") { (lbl, prob) ->
                "${lbl.replaceFirstChar { c -> c.uppercase() }}: %.1f%%".format(prob * 100f)
            }
        binding.tvProbabilitiesDetail.text = "Probabilitas Kelas: $probsDetail"

        binding.btnScanAgain.setOnClickListener {
            dismiss()
            onScanAgainClickListener?.invoke()
        }

        // Feedback button
        binding.btnReportFeedback.setOnClickListener {
            showFeedbackDialog()
        }
    }

    // ---------------------------------------------------------------------------
    // Feedback dialog
    // ---------------------------------------------------------------------------

    private fun showFeedbackDialog() {
        val context = requireContext()

        // Build a compact inline layout programmatically to keep the dialog self-contained.
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvTitle = TextView(context).apply {
            text = "Pilih kategori yang benar:"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
        container.addView(tvTitle)

        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
            val topMargin = (8 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = topMargin }
        }

        val categories = listOf(
            WasteCategory.ORGANIK.displayName   to WasteCategory.ORGANIK.id,
            WasteCategory.ANORGANIK.displayName to WasteCategory.ANORGANIK.id,
            WasteCategory.B3.displayName        to WasteCategory.B3.id
        )
        val radioButtons = categories.map { (label, _) ->
            RadioButton(context).apply {
                text = label
                textSize = 14f
            }
        }
        radioButtons.forEach { radioGroup.addView(it) }
        container.addView(radioGroup)

        val etNote = EditText(context).apply {
            hint = "Catatan tambahan (opsional)"
            textSize = 13f
            val topMargin = (12 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = topMargin }
        }
        container.addView(etNote)

        AlertDialog.Builder(context)
            .setTitle("Laporkan Prediksi Salah")
            .setView(container)
            .setPositiveButton("Kirim") { dialog, _ ->
                val selectedIndex = radioGroup.indexOfChild(
                    container.findViewById(radioGroup.checkedRadioButtonId)
                ).takeIf { it >= 0 }
                val selectedCategoryId = selectedIndex?.let { categories[it].second }
                val note = etNote.text.toString().trim().ifEmpty { null }

                scanViewModel.saveFeedback(
                    predictedCategory = predictedLabel,
                    correctCategory = selectedCategoryId,
                    note = note,
                    imageUri = null   // bitmap URI not available in this scope
                )

                dialog.dismiss()
                Toast.makeText(context, "Terima kasih atas masukannya! 🙏", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
