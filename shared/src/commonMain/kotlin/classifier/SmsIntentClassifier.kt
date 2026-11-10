package classifier

import domain.ClassificationResult

/**
 * Interface for SMS intent classification.
 * Implementations may use on-device LiteRT ML models, deterministic rule engines,
 * or hybrid rule-guarded ML pipelines.
 */
interface SmsIntentClassifier {
    /**
     * Classify an incoming SMS message to determine its intent (purpose) and money direction.
     *
     * @param sms The raw or normalized SMS body text.
     * @param sender The SMS sender address / alphanumeric header (e.g., "VM-HDFCBK-S").
     * @return ClassificationResult containing purpose, direction, and confidence.
     */
    fun classify(sms: String, sender: String = ""): ClassificationResult
}
