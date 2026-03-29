import json
import re
import requests
import time
from tqdm import tqdm

OLLAMA_URL = "http://127.0.0.1:11434/api/generate"
MODEL = "llama3.2:3b"

SYSTEM_PROMPT = """You are classifying SMS messages.

Decide whether the SMS is a financial transaction notification (money debited/credited).
Return ONLY strict JSON with this schema:
{"is_transaction": true|false}

Rules:
- true if money was debited/credited/paid/received/refunded.
- false for OTPs, offers, promos, balance info, reminders, limits, statements.
- Do NOT include any extra text.
- Return false for reminders like ‘payment due’, ‘minimum amount due’, ‘statement generated’, ‘bill due’ unless the message clearly states money was debited/credited.
"""

def ask_ollama(sms_text, timeout=120):
    prompt = f"""{SYSTEM_PROMPT}

SMS:
\"\"\"{sms_text}\"\"\"
"""
    payload = {
        "model": MODEL,
        "prompt": prompt,
        "stream": False,
        "options": {
            "temperature": 0.0,
            "num_ctx": 2048
        }
    }
    start = time.time()
    r = requests.post(OLLAMA_URL, json=payload, timeout=timeout)
    r.raise_for_status()
    data = r.json()
    elapsed = time.time() - start
    return data.get("response", "").strip(), elapsed

def parse_json_bool(s):
    m = re.search(r"\{.*?\}", s, re.DOTALL)
    if not m:
        return None
    try:
        obj = json.loads(m.group(0))
        return bool(obj.get("is_transaction"))
    except Exception:
        return None

def count_lines(path):
    with open(path, "r", encoding="utf-8") as f:
        return sum(1 for _ in f)

def label_file(input_jsonl, output_jsonl, print_every=25):
    total = count_lines(input_jsonl)
    print(f"🚀 Starting labeling with model={MODEL}")
    print(f"📦 Total SMS to process: {total}")

    processed = 0
    txn_count = 0
    non_txn_count = 0
    parse_failures = 0

    with open(input_jsonl, "r", encoding="utf-8") as fin, \
         open(output_jsonl, "w", encoding="utf-8") as fout:
        for line in tqdm(fin, total=total, desc=f"Labeling ({MODEL})"):
            obj = json.loads(line)
            sms = obj.get("body") or obj.get("text") or ""

            resp, elapsed = ask_ollama(sms)
            label = parse_json_bool(resp)

            if label is None:
                parse_failures += 1
                label_int = 0  # safe fallback
            else:
                label_int = 1 if label else 0

            if label_int == 1:
                txn_count += 1
            else:
                non_txn_count += 1

            processed += 1

            fout.write(json.dumps({
                "text": sms,
                "label": label_int,
                "llm_raw": resp
            }, ensure_ascii=False) + "\n")

            # Periodic progress prints
            if processed % print_every == 0 or processed == total:
                print(
                    f"🧮 {processed}/{total} | "
                    f"txn={txn_count}, non_txn={non_txn_count}, "
                    f"parse_fail={parse_failures} | "
                    f"last_req={elapsed:.2f}s"
                )

    print("✅ Done!")
    print(f"📊 Final counts: txn={txn_count}, non_txn={non_txn_count}, parse_fail={parse_failures}")
    print(f"💾 Output written to: {output_jsonl}")

if __name__ == "__main__":
    INPUT = "sms_dataset.jsonl"   # your exported file
    OUTPUT = "sms_txn_labeled_llama3.2_3b.jsonl"
    label_file(INPUT, OUTPUT, print_every=25)