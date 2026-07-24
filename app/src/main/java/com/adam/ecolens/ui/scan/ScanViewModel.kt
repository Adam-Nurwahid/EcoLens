package com.adam.ecolens.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adam.ecolens.TFLiteClassifier
import com.adam.ecolens.data.model.WasteCategory
import com.adam.ecolens.data.repository.AuthRepository
import com.adam.ecolens.data.repository.ScanRepository
import com.adam.ecolens.ml.ImageClassifierHelper
import kotlinx.coroutines.launch

data class ScanResultUiState(
    val bitmap: Bitmap,
    val result: TFLiteClassifier.Result,
    val wasteCategory: WasteCategory
)

class ScanViewModel(
    private val authRepository: AuthRepository,
    private val scanRepository: ScanRepository,
    private val imageClassifierHelper: ImageClassifierHelper
) : ViewModel() {

    private val _scanResultState = MutableLiveData<ScanResultUiState?>()
    val scanResultState: LiveData<ScanResultUiState?> = _scanResultState

    private val _isClassifying = MutableLiveData<Boolean>(false)
    val isClassifying: LiveData<Boolean> = _isClassifying

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun processImage(bitmap: Bitmap) {
        _isClassifying.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = imageClassifierHelper.classifyImage(bitmap)
                val category = WasteCategory.fromLabel(result.label)

                _scanResultState.value = ScanResultUiState(
                    bitmap = bitmap,
                    result = result,
                    wasteCategory = category
                )

                // Save scan to database tied to active user
                val username = authRepository.getActiveUsername() ?: "guest"
                scanRepository.saveScan(
                    username = username,
                    categoryLabel = result.label,
                    confidence = result.confidence
                )
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memproses gambar: ${e.localizedMessage}"
            } finally {
                _isClassifying.value = false
            }
        }
    }

    fun resetResult() {
        _scanResultState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Do not close imageClassifierHelper if shared singleton, or handle cleanup cleanly
    }
}
