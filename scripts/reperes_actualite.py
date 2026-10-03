#!/usr/bin/env python3
"""Compute the "repères d'actualité" shown under the home chart (docs/getDataPercent.js).

For each event, finds the month where it peaks and compares its share of reports
with the share of climate-change reports that same month, all channels pooled.

An event is only kept when it beats climate by at least a factor 2 at its peak:
bed bugs (October 2023) famously do NOT — 0.6 % against 2.8 % for climate.

Usage: python3 scripts/reperes_actualite.py   (from the repo root)
"""
import json, glob, re, collections, unicodedata

def norm(s):
    s = unicodedata.normalize("NFD", (s or "").lower())
    return "".join(c for c in s if unicodedata.category(c) != "Mn")

# "coupe du monde" is matched on the accented text: stripping accents turns
# "coupé du monde" (cut off from the world) into a false positive.
EVENTS = {
    "Tour de France":        (r"tour de france", False),
    "Mort de Chirac":        (r"jacques chirac", False),
    "Rentrée scolaire":      (r"rentree scolaire|rentree des classes", False),
    "Noël":                  (r"marche de noel|pere noel|sapin de noel|reveillon|cadeaux de noel", False),
    "Mort d’Elizabeth II":   (r"elizabeth ii|reine elisabeth|reine elizabeth|buckingham|charles iii|famille royale|couronnement|windsor", False),
    "Coupe du monde":        (r"coupe du monde|mondial de football", True),
    "JO de Paris":           (r"jeux olympiques|jeux paralympiques|flamme olympique|village olympique", False),
    "Mort du pape":          (r"pape francois|conclave|obseques du pape", False),
    # kept as a counter-example, never displayed: it loses to climate
    "Punaises de lit":       (r"punaises? de lit", False),
}
MIN_RATIO = 2.0

def load(since="2019"):
    recs = []
    for f in glob.glob("data-news-json/media=*/year=*/month=*/day=*/*.json"):
        for line in open(f, encoding="utf-8"):
            line = line.strip()
            if not line:
                continue
            r = json.loads(line)
            if r["date"][:4] < since:
                continue
            raw = ((r.get("title") or "") + " . " + (r.get("description") or "")).lower()
            recs.append((r["date"][:7], norm(raw), raw, bool(r.get("containsWordGlobalWarming"))))
    return recs

def main():
    recs = load()
    months = sorted({r[0] for r in recs})
    den = collections.Counter(r[0] for r in recs)
    clim = collections.Counter(r[0] for r in recs if r[3])
    out = []
    for label, (pattern, use_raw) in EVENTS.items():
        rx = re.compile(pattern)
        cnt = collections.Counter(r[0] for r in recs if rx.search(r[2] if use_raw else r[1]))
        peak = max(months, key=lambda m: cnt[m] / den[m] if den[m] else 0)
        pct, cl = 100 * cnt[peak] / den[peak], 100 * clim[peak] / den[peak]
        ratio = pct / cl if cl else float("inf")
        out.append((peak, label, pct, cl, ratio, cnt[peak], den[peak]))
    for peak, label, pct, cl, ratio, n, tot in sorted(out):
        keep = "KEEP  " if ratio >= MIN_RATIO else "drop  "
        print(f"{keep}{peak}  {label:24s} {pct:5.2f} % vs climat {cl:4.2f} %  "
              f"ratio {ratio:5.2f}  ({n}/{tot} sujets)")
    print("\n// à recopier dans docs/getDataPercent.js")
    for peak, label, pct, cl, ratio, n, tot in sorted(out):
        if ratio < MIN_RATIO:
            continue
        print(f"  {{ date: '{peak}', label: '{label}', pct: {pct:.1f}, "
              f"climat: {cl:.1f}, ratio: {ratio:.1f} }},")

if __name__ == "__main__":
    main()
