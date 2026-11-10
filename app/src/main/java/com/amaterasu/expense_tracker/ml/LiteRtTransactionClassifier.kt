package com.amaterasu.expense_tracker.ml

import android.content.Context
import com.amaterasu.expense_tracker.logging.AppLogger
import classifier.RuleBasedSmsClassifier
import classifier.SmsIntentClassifier
import domain.ClassificationResult
import domain.MessagePurpose
import domain.MoneyDirection
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * LiteRT (TensorFlow Lite) On-Device SMS Intent Classifier.
 *
 * Implements [SmsIntentClassifier] to provide efficient, private on-device inference
 * using a quantized LiteRT model.
 *
 * Architecture & Safety Guarantees:
 * 1. Safe Fallback: If `sms_intent_classifier.tflite` is not yet exported, safely falls back
 *    to [RuleBasedSmsClassifier]. The app never crashes if the model is absent.
 * 2. Deterministic Guardrail: Statements, payment due reminders, and OTPs identified by
 *    high-confidence deterministic rules are vetoed before ML inference, ensuring zero false-positive
 *    expense leakage.
 * 3. Bit-for-bit Preprocessing: Tokenization, vocab mapping, and token length match
 *    the offline Python training pipeline (`train_litert_intent_classifier.py`) exactly.
 * 4. Confidence Thresholding: Any prediction with confidence < [confidenceThreshold] is marked as
 *    [MessagePurpose.UNKNOWN] to prevent low-confidence messages from entering expense totals.
 */
class LiteRtTransactionClassifier(
    private val context: Context,
    private val modelAssetPath: String = "ml/sms_intent_classifier.tflite",
    private val vocabAssetPath: String = "ml/vocab.json",
    private val confidenceThreshold: Float = 0.65f
) : SmsIntentClassifier, AutoCloseable {

    private val ruleFallback = RuleBasedSmsClassifier()
    private val vocab: Map<String, Int>? by lazy { loadVocabOrNull() }

    private val interpreter: Interpreter? by lazy {
        loadModelBuffer()?.let { modelBuffer ->
            try {
                val options = Interpreter.Options().apply {
                    setNumThreads(2)
                }
                Interpreter(modelBuffer, options)
            } catch (t: Throwable) {
                AppLogger.w("LiteRtClassifier", "Failed to initialize LiteRT interpreter: ${t.message}")
                null
            }
        }
    }

    private val intentLabels = listOf(
        MessagePurpose.PURCHASE,        // 0
        MessagePurpose.CARD_PAYMENT,     // 1
        MessagePurpose.TRANSFER,         // 2
        MessagePurpose.INCOME,           // 3
        MessagePurpose.REFUND,           // 4
        MessagePurpose.STATEMENT,        // 5
        MessagePurpose.DUE_REMINDER,     // 6
        MessagePurpose.OTP_OR_MARKETING, // 7
        MessagePurpose.UNKNOWN           // 8
    )

    override fun classify(sms: String, sender: String): ClassificationResult {
        // 1. Fast-path safety guardrail: statements, due reminders, and OTPs must never bypass deterministic safety
        val ruleResult = ruleFallback.classify(sms, sender)
        if (ruleResult.purpose in setOf(
                MessagePurpose.STATEMENT,
                MessagePurpose.DUE_REMINDER,
                MessagePurpose.OTP_OR_MARKETING
            )
        ) {
            return ruleResult
        }

        val activeInterpreter = interpreter
        val activeVocab = vocab

        // 2. If model or vocab asset is absent, use safe deterministic rule baseline
        if (activeInterpreter == null || activeVocab == null) {
            return ruleResult
        }

        // 3. LiteRT On-Device Inference
        return try {
            val tokens = tokenize(sms)
            val inputBuffer = encodeTokensToBuffer(tokens, activeVocab, maxLen = 64)

            // Output buffer for 9 classes
            val outputBuffer = Array(1) { FloatArray(intentLabels.size) }

            activeInterpreter.run(inputBuffer, outputBuffer)

            val probabilities = outputBuffer[0]
            var maxIdx = 0
            var maxProb = probabilities[0]
            for (i in 1 until probabilities.size) {
                if (probabilities[i] > maxProb) {
                    maxProb = probabilities[i]
                    maxIdx = i
                }
            }

            val predictedPurpose = if (maxProb >= confidenceThreshold) {
                intentLabels.getOrElse(maxIdx) { MessagePurpose.UNKNOWN }
            } else {
                MessagePurpose.UNKNOWN
            }

            // Direction inference (guarded by rule-based direction analysis)
            val direction = if (predictedPurpose == MessagePurpose.PURCHASE) {
                MoneyDirection.DEBIT
            } else if (predictedPurpose in setOf(MessagePurpose.INCOME, MessagePurpose.REFUND)) {
                MoneyDirection.CREDIT
            } else if (predictedPurpose in setOf(MessagePurpose.STATEMENT, MessagePurpose.DUE_REMINDER, MessagePurpose.OTP_OR_MARKETING)) {
                MoneyDirection.NONE
            } else {
                ruleResult.direction
            }

            ClassificationResult(
                purpose = predictedPurpose,
                direction = direction,
                confidence = maxProb,
                explanation = "LiteRT prediction (class=$predictedPurpose, prob=$maxProb)"
            )
        } catch (t: Throwable) {
            AppLogger.w("LiteRtClassifier", "Inference error: ${t.message}, falling back to rules")
            ruleResult
        }
    }

    /**
     * Deterministic tokenization matching train_litert_intent_classifier.py bit-for-bit.
     */
    fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9₹.]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    private fun encodeTokensToBuffer(tokens: List<String>, vocabMap: Map<String, Int>, maxLen: Int = 64): ByteBuffer {
        // [1, maxLen] int32 tensor
        val buffer = ByteBuffer.allocateDirect(maxLen * 4).order(ByteOrder.nativeOrder())
        val unkIdx = vocabMap["<UNK>"] ?: 1
        val padIdx = vocabMap["<PAD>"] ?: 0

        for (i in 0 until maxLen) {
            val token = tokens.getOrNull(i)
            val tokenId = if (token != null) (vocabMap[token] ?: unkIdx) else padIdx
            buffer.putInt(tokenId)
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModelBuffer(): ByteBuffer? {
        // 1. Standard Android AssetManager
        try {
            context.assets.openFd(modelAssetPath).use { fileDescriptor ->
                FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
                    val fileChannel = inputStream.channel
                    val startOffset = fileDescriptor.startOffset
                    val declaredLength = fileDescriptor.declaredLength
                    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
                }
            }
        } catch (_: Exception) {}

        // 2. JVM test fallback: read directly from assets directory on disk
        try {
            val possiblePaths = listOf(
                "src/main/assets/$modelAssetPath",
                "app/src/main/assets/$modelAssetPath"
            )
            for (path in possiblePaths) {
                val file = java.io.File(path)
                if (file.exists()) {
                    FileInputStream(file).use { inputStream ->
                        val fileChannel = inputStream.channel
                        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                    }
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun loadVocabOrNull(): Map<String, Int>? {
        // 1. Standard Android AssetManager
        try {
            context.assets.open(vocabAssetPath).use { stream ->
                return parseVocabStream(stream)
            }
        } catch (_: Exception) {}

        // 2. JVM test fallback
        try {
            val possiblePaths = listOf(
                "src/main/assets/$vocabAssetPath",
                "app/src/main/assets/$vocabAssetPath"
            )
            for (path in possiblePaths) {
                val file = java.io.File(path)
                if (file.exists()) {
                    FileInputStream(file).use { stream ->
                        return parseVocabStream(stream)
                    }
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun parseVocabStream(stream: java.io.InputStream): Map<String, Int> {
        val json = stream.bufferedReader().use(BufferedReader::readText)
        val obj = JSONObject(json)
        val map = HashMap<String, Int>(obj.length())
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = obj.getInt(k)
        }
        return map
    }

    override fun close() {
        try {
            interpreter?.close()
        } catch (_: Exception) {}
    }
}
