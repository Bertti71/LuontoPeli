package com.example.luontopelibertti71.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlantClassifier {

    //hylkää tulokset joiden varmuus alle 50%
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    //avainsanat joilla suodatetaan luontoon liittyvät tunnisteet
    private val natureKeywords = setOf(
        "plant", "flower", "tree", "shrub", "leaf", "fern", "moss",
        "mushroom", "fungus", "grass", "herb", "bush", "berry",
        "pine", "birch", "spruce", "algae", "lichen", "bark",
        "nature", "forest", "woodland", "botanical", "flora",
        "green", "outdoor", "sky", "ground", "soil", "wood"
    )

    //analysoi kuvan ja palauttaa tunnistustuloksen
    //suspendCancellableCoroutine muuntaa ML Kitin callback-kutsun coroutineksi
    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        //suodatetaan vain luontoon liittyvät tunnisteet
                        val natureLabels = labels.filter { label ->
                            natureKeywords.any { label.text.contains(it, ignoreCase = true) }
                        }
                        val result = if (natureLabels.isNotEmpty()) {
                            //valitsee parhaan osuman
                            val best = natureLabels.maxByOrNull { it.confidence }!!
                            ClassificationResult.Success(best.text, best.confidence, labels.take(5))
                        } else {
                            ClassificationResult.NotNature(labels.take(3))
                        }
                        continuation.resume(result)
                    }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    fun close() = labeler.close()
}

sealed class ClassificationResult {
    //kuva tunnistettiin luontokohteeksi
    data class Success(
        val label: String,       //tunnistettu laji
        val confidence: Float,   //varmuus 0.0-1.0
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    //kuva tunnistettiin mutta ei ole luontokohde
    data class NotNature(val allLabels: List<ImageLabel>) : ClassificationResult()

    //tunnistus epäonnistui
    data class Error(val message: String) : ClassificationResult()
}