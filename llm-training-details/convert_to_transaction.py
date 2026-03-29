import json
import random
import re

def looks_like_txn(text, sender):
    t = text.lower()
    if sender and ("-s" in sender.lower() or "-t" in sender.lower()):
        return True
    return any(k in t for k in ["debit", "debited", "credit", "credited", "spent", "₹", "rs.", "inr"])

pos, neg = [], []

with open("./files/sms_dataset_full.jsonl", "r", encoding="utf-8") as f:
    for line in f:
        obj = json.loads(line)
        text = obj["body"]
        sender = obj.get("address", "")
        if looks_like_txn(text, sender):
            pos.append(text)
        else:
            neg.append(text)

# Balance dataset
random.shuffle(neg)
neg = neg[:len(pos)]

out = []
for t in pos:
    out.append({"text": t, "label": 1})
for t in neg:
    out.append({"text": t, "label": 0})

random.shuffle(out)

with open("./output/sms_txn_classifier_for_transaction.jsonl", "w", encoding="utf-8") as f:
    for o in out:
        f.write(json.dumps(o, ensure_ascii=False) + "\n")

print(f"Done. Positives={len(pos)}, Negatives={len(neg)}")