package org.kivy.chequescan

import android.graphics.BitmapFactory
import android.content.Context
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MLKitTextHelper(private val context: Context) {
    fun recognizeImage(path: String, onResult: (String) -> Unit) {
        try {
            val bmp = BitmapFactory.decodeFile(path) ?: run {
                onResult("")
                return
            }
            val image = InputImage.fromBitmap(bmp, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onResult(visionText.text ?: "")
                }
                .addOnFailureListener { e ->
                    onResult("")
                }
        } catch (e: Exception) {
            onResult("")
        }
    }
}
