package com.adam.ecolens

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Wrapper untuk model mobilenetv2_dynamic.tflite (EcoLens).
 *
 * Detail model (dari hasil uji notebook):
 * - Input : 224x224x3, dtype FLOAT32, RAW pixel value [0..255] (JANGAN dibagi 255,
 *           karena layer Rescaling(1/127.5, offset=-1) sudah ada di dalam graph model)
 * - Output: FLOAT32 [1,3] -> sudah berupa probabilitas per kelas
 * - Kelas : ["anorganik", "b3", "organik"]  (urutan alfabetis sesuai training)
 */
class TFLiteClassifier(context: Context) {

    private val interpreter: Interpreter
    private val inputWidth: Int
    private val inputHeight: Int

    companion object {
        private const val MODEL_FILE = "waste_classifier.tflite"
        val CLASS_NAMES = arrayOf("anorganik", "b3", "organik")
    }

    data class Result(
        val label: String,
        val confidence: Float,          // 0..100
        val allProbabilities: FloatArray
    )

    init {
        val model = loadModelFile(context, MODEL_FILE)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)

        val inputShape = interpreter.getInputTensor(0).shape() // [1, H, W, 3]
        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
    }

    private fun loadModelFile(context: Context, fileName: String): ByteBuffer {
        val fd = context.assets.openFd(fileName)
        FileInputStream(fd.fileDescriptor).use { input ->
            val channel = input.channel
            return channel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.startOffset,
                fd.declaredLength
            )
        }
    }

    /**
     * Ubah Bitmap jadi ByteBuffer float32 NHWC, RAW pixel value (0-255),
     * urutan channel R,G,B. Sesuai preprocessing di notebook (bukan dibagi 255).
     */
    private fun bitmapToInputBuffer(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

        val buffer = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            buffer.putFloat(r.toFloat())
            buffer.putFloat(g.toFloat())
            buffer.putFloat(b.toFloat())
        }

        buffer.rewind()
        return buffer
    }

    /**
     * Jalankan inference. Sebaiknya dipanggil dari background thread/coroutine,
     * jangan di main thread.
     */
    fun classify(bitmap: Bitmap): Result {
        val inputBuffer = bitmapToInputBuffer(bitmap)

        // output shape [1, 3]
        val output = Array(1) { FloatArray(CLASS_NAMES.size) }
        interpreter.run(inputBuffer, output)

        val probs = output[0]
        var maxIdx = 0
        for (i in probs.indices) {
            if (probs[i] > probs[maxIdx]) maxIdx = i
        }

        return Result(
            label = CLASS_NAMES[maxIdx],
            confidence = probs[maxIdx] * 100f,
            allProbabilities = probs
        )
    }

    fun close() {
        interpreter.close()
    }
}