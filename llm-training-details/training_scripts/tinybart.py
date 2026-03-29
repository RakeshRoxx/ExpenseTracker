import json
import numpy as np
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report

# Load labeled data
texts, labels = [], []
with open("../output/sms_txn_labeled_llama3.2_3b.jsonl", "r", encoding="utf-8") as f:
    for line in f:
        obj = json.loads(line)
        texts.append(obj["text"])
        labels.append(obj["label"])

# Split
X_train, X_val, y_train, y_val = train_test_split(
    texts, labels, test_size=0.2, random_state=42, stratify=labels
)

# Vectorize
vectorizer = TfidfVectorizer(
    ngram_range=(1, 2),
    max_features=30000,
    min_df=2
)
X_train_vec = vectorizer.fit_transform(X_train)
X_val_vec = vectorizer.transform(X_val)

# Train
clf = LogisticRegression(max_iter=1000, class_weight="balanced")
clf.fit(X_train_vec, y_train)

# Evaluate (default threshold 0.5)
y_pred = clf.predict(X_val_vec)
print("=== Default threshold (0.5) ===")
print(classification_report(y_val, y_pred, digits=4))

# Optional: Evaluate custom threshold for better precision
y_proba = clf.predict_proba(X_val_vec)[:, 1]
custom_threshold = 0.65
y_pred_custom = (y_proba > custom_threshold).astype(int)
print(f"\n=== Custom threshold ({custom_threshold}) ===")
print(classification_report(y_val, y_pred_custom, digits=4))

# -------------------------------
# Save sklearn models (optional)
# -------------------------------
joblib.dump(vectorizer, "tfidf_vectorizer.joblib")
joblib.dump(clf, "lr_model.joblib")
print("Saved sklearn models: tfidf_vectorizer.joblib, lr_model.joblib")

# -------------------------------
# Export to Android-friendly JSON
# -------------------------------

# 1) Vocabulary (token -> index)
vocab = vectorizer.vocabulary_
vocab_py = {k: int(v) for k, v in vocab.items()}

with open("vocab.json", "w", encoding="utf-8") as f:
    json.dump(vocab_py, f, ensure_ascii=False)

# 2) IDF values
idf = vectorizer.idf_
with open("idf.json", "w", encoding="utf-8") as f:
    json.dump(idf.tolist(), f)

# 3) Logistic Regression weights + bias
weights = clf.coef_[0]
bias = float(clf.intercept_[0])

with open("weights.json", "w", encoding="utf-8") as f:
    json.dump(weights.tolist(), f)

with open("bias.json", "w", encoding="utf-8") as f:
    json.dump(bias, f)

print("✅ Exported Android assets:")
print(" - vocab.json")
print(" - idf.json")
print(" - weights.json")
print(" - bias.json")

# -------------------------------
# Quick sanity check (single SMS)
# -------------------------------
def predict_text(text, threshold=0.65):
    vec = vectorizer.transform([text])
    prob = clf.predict_proba(vec)[0, 1]
    return prob, int(prob >= threshold)

sample = "INR 50 debited from your account via UPI"
prob, pred = predict_text(sample)
print(f"\nSanity check: '{sample}'")
print(f"Prob(transaction)={prob:.4f}, Pred={pred}")