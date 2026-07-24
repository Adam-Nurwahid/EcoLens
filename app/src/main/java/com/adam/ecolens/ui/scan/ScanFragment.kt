package com.adam.ecolens.ui.scan

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adam.ecolens.databinding.FragmentScanBinding
import com.adam.ecolens.ui.ViewModelFactory

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    // Ambil gambar dari galeri (Preserved from verified MainActivity implementation)
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { loadBitmapFromUri(it) }
        }

    // Ambil foto langsung dari kamera (thumbnail, preserved from verified MainActivity implementation)
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { onImageReady(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCamera.setOnClickListener {
            takePictureLauncher.launch(null)
        }

        observeViewModel()
    }

    private fun loadBitmapFromUri(uri: Uri) {
        val bitmap = if (android.os.Build.VERSION.SDK_INT >= 28) {
            val source = android.graphics.ImageDecoder.createSource(requireContext().contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
        onImageReady(bitmap.copy(Bitmap.Config.ARGB_8888, false))
    }

    private fun onImageReady(bitmap: Bitmap) {
        binding.imgPreview.setImageBitmap(bitmap)
        binding.layoutPlaceholder.visibility = View.GONE
        viewModel.processImage(bitmap)
    }

    private fun observeViewModel() {
        viewModel.isClassifying.observe(viewLifecycleOwner) { isBusy ->
            binding.progressBarScan.visibility = if (isBusy) View.VISIBLE else View.GONE
            binding.btnCamera.isEnabled = !isBusy
            binding.btnGallery.isEnabled = !isBusy
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { err ->
            err?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.scanResultState.observe(viewLifecycleOwner) { state ->
            state?.let {
                val bottomSheet = ScanResultBottomSheet.newInstance(
                    bitmap = it.bitmap,
                    label = it.result.label,
                    confidence = it.result.confidence,
                    probabilities = it.result.allProbabilities
                )
                bottomSheet.onScanAgainClickListener = {
                    viewModel.resetResult()
                    binding.imgPreview.setImageDrawable(null)
                    binding.layoutPlaceholder.visibility = View.VISIBLE
                }
                bottomSheet.show(childFragmentManager, "ScanResultBottomSheet")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
