package com.adam.ecolens.ui.scan

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.adam.ecolens.TFLiteClassifier
import com.adam.ecolens.data.model.WasteCategory
import com.adam.ecolens.databinding.BottomSheetScanResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScanResultBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetScanResultBinding? = null
    private val binding get() = _binding!!

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

        val probsDetail = TFLiteClassifier.CLASS_NAMES
            .zip(allProbabilities.toList())
            .joinToString(" • ") { (lbl, prob) ->
                "${lbl.replaceFirstChar { c -> c.uppercase() }}: %.1f%%".format(prob * 100f)
            }
        binding.tvProbabilitiesDetail.text = "Probabilitas Kelas: $probsDetail"

        binding.btnScanAgain.setOnClickListener {
            dismiss()
            onScanAgainClickListener?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
