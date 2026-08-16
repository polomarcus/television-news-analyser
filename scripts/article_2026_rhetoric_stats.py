#!/usr/bin/env python3
"""Lexical measures used by the 2026 article (rhetoric & juxtaposition stats).

Publishes the exact lexicons behind: interrogative titles, nomination
vocabulary, agentless constructions, écogestes vs fossil fuels, and the
same-broadcast climate + travel-postcard juxtaposition.
Run from the repo root: python3 scripts/article_2026_rhetoric_stats.py
"""
import json, glob, re, collections, unicodedata

def norm(s):
    s = unicodedata.normalize("NFD", (s or "").lower())
    return "".join(c for c in s if unicodedata.category(c) != "Mn")

recs = []
for f in glob.glob("data-news-json/media=*/year=*/month=*/day=*/*.json"):
    media = f.split("media=")[1].split("/")[0]
    for line in open(f):
        line = line.strip()
        if not line:
            continue
        r = json.loads(line)
        recs.append({"media": media, "date": r["date"][:10], "title": r.get("title") or "",
                     "tv": r.get("urlTvNews") or "",
                     "clim": bool(r.get("containsWordGlobalWarming")),
                     "text": norm((r.get("title") or "") + " " + (r.get("description") or ""))})
clim = [r for r in recs if r["clim"]]
print(f"{len(recs)} reportages, {len(clim)} climat")

def has(r, kws): return any(k in r["text"] for k in kws)

# --- titres interrogatifs ---
climt = [r for r in clim if r["title"].strip()]
othert = [r for r in recs if not r["clim"] and r["title"].strip()]
print("\nTitres interrogatifs ('?'):")
print(f"  climat: {100*sum('?' in r['title'] for r in climt)/len(climt):.1f}% | reste: {100*sum('?' in r['title'] for r in othert)/len(othert):.1f}%")
for lo, hi in [("2013", "2021"), ("2022", "2026")]:
    sub = [r for r in climt if lo <= r["date"][:4] <= hi]
    print(f"  climat {lo}-{hi}: {100*sum('?' in r['title'] for r in sub)/len(sub):.1f}%")

# --- modalisation ---
for lo, hi in [("2013", "2021"), ("2022", "2026")]:
    sub = [r for r in clim if lo <= r["date"][:4] <= hi]
    m = sum(1 for r in sub if "pourrait" in r["text"] or "pourraient" in r["text"])
    print(f"pourrait/pourraient {lo}-{hi}: {100*m/len(sub):.1f}%")

# --- nomination ---
NOMINATION = {"rechauffement climatique": None, "dereglement climatique": None,
              "crise climatique": None, "urgence climatique": None,
              "catastrophe(s) naturelle(s)": ["catastrophe naturelle", "catastrophes naturelles"],
              "aleas climatiques": ["aleas climatiques", "alea climatique"],
              "episode(s) caniculaire(s)": ["episode caniculaire", "episodes caniculaires"]}
print("\nNomination:")
for label, kws in NOMINATION.items():
    kws = kws or [label]
    print(f"  {label:28s} {sum(1 for r in recs if has(r, kws))}")

# --- effacement de l'agent ---
se = sum(1 for r in recs if "se rechauffe" in r["text"] or "se deregle" in r["text"])
AGENT = ["cause par les activites humaines", "d'origine humaine", "cause par l'homme",
         "responsabilite humaine", "du fait des activites humaines", "emissions humaines"]
print(f"\nSans agent ('se réchauffe/se dérègle'): {se} | attribution humaine explicite: {sum(1 for r in recs if has(r, AGENT))}")

# --- écogestes vs fossiles ---
GESTES = ["ecogeste", "eco-geste", "gestes simples", "bons gestes", "chaque geste compte", "les bons reflexes"]
FOSSILES = ["energies fossiles", "combustibles fossiles", "industrie petroliere", "lobby petrolier"]
print(f"écogestes: {sum(1 for r in recs if has(r, GESTES))} | énergies fossiles/lobbies: {sum(1 for r in recs if has(r, FOSSILES))}")

# --- même JT : climat + carte postale ---
POSTCARD = ["a la decouverte de", "paysages grandioses", "paysages a couper le souffle",
            "ile paradisiaque", "iles d'exception", "joyau", "carte postale", "depaysement",
            "bout du monde", "petit coin de paradis"]
by_tv = collections.defaultdict(list)
for r in recs:
    if r["tv"]:
        by_tv[r["tv"]].append(r)
jt_clim = sum(1 for rows in by_tv.values() if any(r["clim"] for r in rows))
juxta = sum(1 for rows in by_tv.values()
            if any(r["clim"] for r in rows) and any((not r["clim"]) and has(r, POSTCARD) for r in rows))
print(f"\nJT avec sujet climat: {jt_clim} | dont avec carte postale dans la même édition: {juxta} ({100*juxta/jt_clim:.0f}%)")
