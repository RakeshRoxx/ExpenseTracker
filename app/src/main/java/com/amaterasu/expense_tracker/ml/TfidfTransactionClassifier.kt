package com.amaterasu.expense_tracker.ml

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import kotlin.math.exp

class TfidfTransactionClassifier(context: Context) {
    private val vocab: Map<String, Int>
    private val idf: FloatArray
    private val weights: FloatArray
    private val bias: Float

    init {
        val assets = context.assets
        vocab = loadVocab(assets.open("ml/vocab.json"))
        idf = loadFloatArray(assets.open("ml/idf.json"))
        weights = loadFloatArray(assets.open("ml/weights.json"))
        bias = loadFloat(assets.open("ml/bias.json"))
    }

    /**
     * Returns true if SMS is predicted as a transaction.
     * @param text SMS body
     * @param threshold Probability threshold (e.g., 0.65f)
     */
    fun isTransaction(text: String, threshold: Float = 0.65f): Boolean {
        val prob = predictProbability(text)
        return prob >= threshold
    }

    /**
     * Returns probability that SMS is a transaction.
     */
    fun predictProbability(text: String): Float {
        val features = tfidfVector(text)
        var dot = bias
        for (i in features.indices) {
            val x = features[i]
            if (x != 0f) {
                dot += x * weights[i]
            }
        }
        return sigmoid(dot)
    }

    // ---------------------------
    // TF-IDF
    // ---------------------------

    private fun tfidfVector(text: String): FloatArray {
        val vec = FloatArray(weights.size)
        val tokens = tokenize(text)

        // term frequency per index
        val tf = HashMap<Int, Int>()
        for (t in tokens) {
            val idx = vocab[t] ?: continue
            tf[idx] = (tf[idx] ?: 0) + 1
        }

        val maxTf = tf.values.maxOrNull() ?: 1
        for ((idx, count) in tf) {
            val tfNorm = count.toFloat() / maxTf
            vec[idx] = tfNorm * idf[idx]
        }
        return vec
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9₹.]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    // ---------------------------
    // Utils
    // ---------------------------

    private fun sigmoid(x: Float): Float {
        return (1.0f / (1.0f + exp(-x)))
    }

    private fun loadVocab(input: java.io.InputStream): Map<String, Int> {
        val json = input.bufferedReader().use(BufferedReader::readText)
        val obj = JSONObject(json)
        val map = HashMap<String, Int>(obj.length())
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = obj.getInt(k)
        }
        return map
    }

    private fun loadFloatArray(input: java.io.InputStream): FloatArray {
        val json = input.bufferedReader().use(BufferedReader::readText)
        val arr = JSONArray(json)
        val out = FloatArray(arr.length())
        for (i in 0 until arr.length()) {
            out[i] = arr.getDouble(i).toFloat()
        }
        return out
    }

    private fun loadFloat(input: java.io.InputStream): Float {
        val json = input.bufferedReader().use(BufferedReader::readText)
        return json.toFloat()
    }
}