import json
import re
from datetime import datetime

AMOUNT_REGEX = re.compile(r'(?:INR|Rs\.?)\s*([0-9]+(?:\.[0-9]+)?)', re.IGNORECASE)
DATE_REGEX = re.compile(r'(\d{2}[-/]\d{2}[-/]\d{2,4}\s*\d{2}:\d{2}:\d{2})')

def parse_amount(text):
    m = AMOUNT_REGEX.search(text)
    return float(m.group(1)) if m else None

def parse_date(text):
    m = DATE_REGEX.search(text)
    return m.group(1) if m else None

def parse_type(text):
    t = text.lower()
    if any(x in t for x in ["spent", "debit", "paid", "purchase"]):
        return "DEBIT"
    if any(x in t for x in ["credited", "received", "credit"]):
        return "CREDIT"
    return None

def parse_bank(sender):
    if "AXIS" in sender: return "Axis Bank"
    if "HDFC" in sender: return "HDFC Bank"
    if "ICICI" in sender: return "ICICI Bank"
    return "Unknown"

def parse_account_hint(text):
    m = re.search(r'XX(\d{2,4})', text)
    return m.group(1) if m else None

out = []

with open("./files/sms_dataset_1771914955475.jsonl", "r", encoding="utf-8") as f:
    for line in f:
        obj = json.loads(line)
        body = obj["body"]
        sender = obj.get("address", "")

        labeled = {
            "id": obj["id"],
            "text": body,
            "amount": parse_amount(body),
            "type": parse_type(body),
            "bank": parse_bank(sender),
            "accountHint": parse_account_hint(body),
            "date": parse_date(body)
        }

        out.append(labeled)

with open("./files/sms_dataset_1771914955475_output.jsonl", "w", encoding="utf-8") as f:
    for o in out:
        f.write(json.dumps(o, ensure_ascii=False) + "\n")

print("Done. Wrote sms_dataset_1771914955475.jsonl")