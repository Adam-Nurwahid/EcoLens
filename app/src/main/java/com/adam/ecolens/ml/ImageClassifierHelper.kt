package com.adam.ecolens.ml

import android.content.Context
import android.graphics.Bitmap
import com.adam.ecolens.TFLiteClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageClassifierHelper(context: Context) {

    private val classifier: TFLiteClassifier = TFLiteClassifier(context)

    suspend fun classifyImage(bitmap: Bitmap): TFLiteClassifier.Result = withContext(Dispatchers.Default) {
        classifier.classify(bitmap)
    }

    fun close() {
        classifier.close()
    }
}
