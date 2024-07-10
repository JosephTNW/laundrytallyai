package com.example.laundrytallyai.pages.clothes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

const val NUM_DETECTIONS = 13125
const val NUM_CLASSES = 5

class YoloDetector(context: Context) {
    private val interpreter: Interpreter

    init {
        // Loaded the TFLite model successfully
        val model = FileUtil.loadMappedFile(context, "clothes_float16.tflite")
        interpreter = Interpreter(model)
    }

    fun detect(image: Bitmap): List<Detection> {
        val inputBuffer = preprocessImage(image)
        val outputBuffer = Array(1) { Array(4 + NUM_CLASSES) { FloatArray(NUM_DETECTIONS) } }

        interpreter.run(inputBuffer, outputBuffer)

        return postprocessResults(outputBuffer[0])
    }

    private fun preprocessImage(image: Bitmap): ByteBuffer {
        val inputSize = 800 // Based on your model's input size
        val scaledBitmap = Bitmap.createScaledBitmap(image, inputSize, inputSize, true)

        val byteBuffer =
            ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4) // 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(
            intValues,
            0,
            scaledBitmap.width,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height
        )

        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) / 255.0f))
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        byteBuffer.rewind()

        for (i in 0 until 10) {
            Log.d("PreprocessImage", byteBuffer.getFloat(i).toString())
        }
        Log.d("PreprocessImage", "Finished preprocessing")

        byteBuffer.rewind()

        return byteBuffer
    }

    private fun postprocessResults(outputBuffer: Array<FloatArray>): List<Detection> {
        val detections = mutableListOf<Detection>()
        val NUM_CLASSES = 5
        val confidenceThreshold = 0f
        val imageSize = 800f

        outputBuffer.forEach { detection ->
            val confidence = detection[4]
            if (confidence > confidenceThreshold) {
                val classScores = detection.slice(5 until 5 + NUM_CLASSES)
                val maxScore = classScores.maxOrNull() ?: 0f
                val classIndex = classScores.indexOf(maxScore)

                if (maxScore > confidenceThreshold) {
                    val x = detection[0]
                    val y = detection[1]
                    val w = detection[2]
                    val h = detection[3]

                    val left = (x - w / 2) * imageSize
                    val top = (y - h / 2) * imageSize
                    val right = (x + w / 2) * imageSize
                    val bottom = (y + h / 2) * imageSize

                    detections.add(
                        Detection(
                            bbox = RectF(left, top, right, bottom),
                            label = getClassLabel(classIndex),
                            confidence = maxScore
                        )
                    )
                }
            }
        }

        return nonMaxSuppression(detections)
    }

    private fun getClassLabel(index: Int): String {
        return when (index) {
            0 -> "black"
            1 -> "blue"
            2 -> "brown"
            3 -> "green"
            4 -> "white"
            else -> "unknown"
        }
    }

    private fun nonMaxSuppression(detections: List<Detection>): List<Detection> {
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val selectedDetections = mutableListOf<Detection>()

        for (detection in sortedDetections) {
            var overlap = false
            for (selectedDetection in selectedDetections) {
                if (calculateIoU(detection.bbox, selectedDetection.bbox) > 0.5) {
                    overlap = true
                    break
                }
            }
            if (!overlap) {
                selectedDetections.add(detection)
            }
        }

        return selectedDetections
    }

    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionArea = RectF().apply {
            setIntersect(box1, box2)
        }.let { if (it.isEmpty) 0f else it.width() * it.height() }

        val unionArea =
            box1.width() * box1.height() + box2.width() * box2.height() - intersectionArea

        return intersectionArea / unionArea
    }
}