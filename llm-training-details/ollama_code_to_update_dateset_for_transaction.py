import json
import re
import requests
import time
from tqdm import tqdm
from requests import RequestException

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

TRANSACTION_PATTERNS = [
    r"\bdebited\b",
    r"\bcredited\b",
    r"\bspent\b",
    r"\bwithdrawn\b",
    r"\bpaid\b",
    r"\breceived\b",
    r"\brefund(?:ed)?\b",
    r"\bsent\b",
    r"\bdr\b",
    r"\bcr\b",
    r"\bupi\/p2[am]\b",
]

NON_TRANSACTION_PATTERNS = [
    r"\botp\b",
    r"\boffer\b",
    r"\bsale\b",
    r"\bpromo\b",
    r"\bpre-?approved\b",
    r"\bloan offer\b",
    r"\bapply now\b",
    r"\bbill due\b",
    r"\bminimum amount due\b",
    r"\bstatement generated\b",
    r"\bdue date\b",
    r"\breward points?\b",
    r"\bcredit limit\b",
    r"\bavailable limit\b",
]

AMBIGUOUS_TRANSACTION_PATTERNS = [
    r"\btransaction\b",
    r"\bpayment\b",
    r"\bemi\b",
    r"\bautopay\b",
    r"\bbalance\b",
    r"\bcredit card\b",
]

DEBUG = True
DEBUG_SMS_PREVIEW_LEN = 160

def debug_log(message):
    if DEBUG:
        print(f"[DEBUG] {message}")

def preview_text(text, limit=DEBUG_SMS_PREVIEW_LEN):
    compact = re.sub(r"\s+", " ", text).strip()
    if len(compact) <= limit:
        return compact
    return compact[:limit] + "..."

def ask_ollama(sms_text, timeout=120, max_retries=3, base_delay=2.0):
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
    last_error = None

    for attempt in range(1, max_retries + 1):
        start = time.time()
        try:
            debug_log(
                f"Ollama request start | attempt={attempt}/{max_retries} | "
                f"sms='{preview_text(sms_text)}'"
            )
            r = requests.post(OLLAMA_URL, json=payload, timeout=timeout)
            r.raise_for_status()
            data = r.json()
            elapsed = time.time() - start
            debug_log(
                f"Ollama request success | attempt={attempt} | elapsed={elapsed:.2f}s | "
                f"raw='{preview_text(data.get('response', ''))}'"
            )
            return data.get("response", "").strip(), elapsed, None
        except (RequestException, ValueError) as exc:
            elapsed = time.time() - start
            last_error = f"{type(exc).__name__}: {exc}"
            debug_log(
                f"Ollama request failed | attempt={attempt} | elapsed={elapsed:.2f}s | "
                f"error={last_error}"
            )
            if attempt < max_retries:
                time.sleep(base_delay * attempt)

    return "", elapsed, last_error

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

def heuristic_label(sms_text):
    normalized = sms_text.lower()

    matched_txn = [pattern for pattern in TRANSACTION_PATTERNS if re.search(pattern, normalized)]
    matched_non_txn = [pattern for pattern in NON_TRANSACTION_PATTERNS if re.search(pattern, normalized)]
    matched_ambiguous = [pattern for pattern in AMBIGUOUS_TRANSACTION_PATTERNS if re.search(pattern, normalized)]

    has_txn = bool(matched_txn)
    has_non_txn = bool(matched_non_txn)
    has_ambiguous = bool(matched_ambiguous)

    if has_txn and not has_non_txn:
        return True, "heuristic_transaction", {
            "matched_transaction_patterns": matched_txn,
            "matched_non_transaction_patterns": matched_non_txn,
            "matched_ambiguous_patterns": matched_ambiguous,
        }

    if has_non_txn and not has_txn and not has_ambiguous:
        return False, "heuristic_non_transaction", {
            "matched_transaction_patterns": matched_txn,
            "matched_non_transaction_patterns": matched_non_txn,
            "matched_ambiguous_patterns": matched_ambiguous,
        }

    return None, "needs_llm", {
        "matched_transaction_patterns": matched_txn,
        "matched_non_transaction_patterns": matched_non_txn,
        "matched_ambiguous_patterns": matched_ambiguous,
    }

def label_file(input_jsonl, output_jsonl, review_jsonl=None, print_every=25):
    total = count_lines(input_jsonl)
    print(f"🚀 Starting labeling with model={MODEL}")
    print(f"📦 Total SMS to process: {total}")

    processed = 0
    txn_count = 0
    non_txn_count = 0
    parse_failures = 0
    heuristic_count = 0
    llm_count = 0
    review_count = 0

    if review_jsonl is None:
        review_jsonl = output_jsonl.replace(".jsonl", "_needs_review.jsonl")

    with open(input_jsonl, "r", encoding="utf-8") as fin, \
         open(output_jsonl, "w", encoding="utf-8") as fout, \
         open(review_jsonl, "w", encoding="utf-8") as freview:
        for line in tqdm(fin, total=total, desc=f"Labeling ({MODEL})"):
            obj = json.loads(line)
            sms = obj.get("body") or obj.get("text") or ""
            sender = obj.get("sender") or obj.get("address")
            timestamp = obj.get("date") or obj.get("timestamp")

            label, source, heuristic_meta = heuristic_label(sms)
            resp = ""
            elapsed = 0.0
            error = None

            debug_log(
                f"SMS start | sender={sender} | timestamp={timestamp} | "
                f"sms='{preview_text(sms)}'"
            )
            debug_log(
                f"Heuristic decision | source={source} | "
                f"txn_patterns={heuristic_meta['matched_transaction_patterns']} | "
                f"non_txn_patterns={heuristic_meta['matched_non_transaction_patterns']} | "
                f"ambiguous_patterns={heuristic_meta['matched_ambiguous_patterns']}"
            )

            if label is None:
                resp, elapsed, error = ask_ollama(sms)
                label = parse_json_bool(resp)
                source = "ollama"

                if label is None:
                    debug_log(
                        f"Review needed | sender={sender} | timestamp={timestamp} | "
                        f"error={error} | llm_raw='{preview_text(resp)}'"
                    )
                    parse_failures += 1
                    review_count += 1
                    freview.write(json.dumps({
                        "text": sms,
                        "sender": sender,
                        "timestamp": timestamp,
                        "llm_raw": resp,
                        "error": error,
                        "reason_source": "parse_failure",
                        "heuristic_meta": heuristic_meta
                    }, ensure_ascii=False) + "\n")
                    processed += 1
                    if processed % print_every == 0 or processed == total:
                        print(
                            f"🧮 {processed}/{total} | "
                            f"txn={txn_count}, non_txn={non_txn_count}, "
                            f"heuristic={heuristic_count}, llm={llm_count}, "
                            f"review={review_count}, parse_fail={parse_failures} | "
                            f"last_req={elapsed:.2f}s"
                        )
                    continue

                llm_count += 1
                debug_log(
                    f"LLM decision | label={label} | sender={sender} | "
                    f"timestamp={timestamp} | llm_raw='{preview_text(resp)}'"
                )
            else:
                heuristic_count += 1
                debug_log(
                    f"Heuristic final label | label={label} | sender={sender} | "
                    f"timestamp={timestamp}"
                )

            label_int = 1 if label else 0

            if label_int == 1:
                txn_count += 1
            else:
                non_txn_count += 1

            processed += 1

            fout.write(json.dumps({
                "text": sms,
                "label": label_int,
                "sender": sender,
                "timestamp": timestamp,
                "reason_source": source,
                "heuristic_meta": heuristic_meta,
                "llm_raw": resp
            }, ensure_ascii=False) + "\n")

            # Periodic progress prints
            if processed % print_every == 0 or processed == total:
                print(
                    f"🧮 {processed}/{total} | "
                    f"txn={txn_count}, non_txn={non_txn_count}, "
                    f"heuristic={heuristic_count}, llm={llm_count}, "
                    f"review={review_count}, parse_fail={parse_failures} | "
                    f"last_req={elapsed:.2f}s"
                )

    print("✅ Done!")
    print(
        f"📊 Final counts: txn={txn_count}, non_txn={non_txn_count}, "
        f"heuristic={heuristic_count}, llm={llm_count}, review={review_count}, "
        f"parse_fail={parse_failures}"
    )
    print(f"💾 Output written to: {output_jsonl}")
    print(f"📝 Review file written to: {review_jsonl}")

if __name__ == "__main__":
    INPUT = "./files/sms_dataset_full.jsonl"   # your exported file
    OUTPUT = "./output/sms_txn_labeled_llama3.2_3b_today.jsonl"
    REVIEW = "./output/sms_txn_labeled_llama3.2_3b_today_needs_review.jsonl"
    label_file(INPUT, OUTPUT, review_jsonl=REVIEW, print_every=25)
