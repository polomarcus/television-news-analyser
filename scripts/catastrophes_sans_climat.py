#!/usr/bin/env python3
"""Feed docs/getDataCatastrophes.js : quand un JT montre une catastrophe, nomme-t-il le climat ?

Trois cas pour chaque sujet d'événement extrême :
  - le sujet lui-même est détecté comme climatique ;
  - le climat est nommé ailleurs dans la même édition ;
  - jamais, dans aucun sujet de ce JT.

France 2 et France 3 seulement : leur urlTvNews identifie le JT du jour
(11,5 sujets par édition). Pour TF1 ce champ est la page de pagination
(380 valeurs distinctes pour 54 980 sujets) et 20 368 sujets n'ont pas d'heure,
l'édition n'est donc pas reconstituable.

Usage : python3 scripts/catastrophes_sans_climat.py   (depuis la racine du dépôt)
"""
import json, glob, re, sys, collections, unicodedata

def norm(s):
    s = unicodedata.normalize("NFD", (s or "").lower())
    return "".join(c for c in s if unicodedata.category(c) != "Mn")

EXTREME = re.compile(
    r"canicule|vague de chaleur|secheresse|inondation|crue |tempete|cyclone|"
    r"ouragan|incendie|feux de foret|feu de foret|megafeu|grele")
FAMILLES = [
    ("Canicule, forte chaleur", r"canicule|vague de chaleur"),
    ("Sécheresse",              r"secheresse"),
    ("Inondation, crue",        r"inondation|crue "),
    ("Tempête, cyclone",        r"tempete|cyclone|ouragan"),
    ("Incendie, feux de forêt", r"incendie|feux de foret|feu de foret|megafeu"),
]
MEDIAS = ("France 2", "France 3")
SINCE = "2019"

def load():
    recs = []
    for f in glob.glob("data-news-json/media=*/year=*/month=*/day=*/*.json"):
        media = f.split("media=")[1].split("/")[0]
        if media not in MEDIAS:
            continue
        for line in open(f, encoding="utf-8"):
            line = line.strip()
            if not line:
                continue
            r = json.loads(line)
            if r["date"][:4] < SINCE:
                continue
            recs.append({
                "date": r["date"][:10],
                "title": r.get("title") or "",
                "url": r.get("url") or "",
                "edition": r.get("urlTvNews") or "",
                "clim": bool(r.get("containsWordGlobalWarming")),
                "text": norm((r.get("title") or "") + " . " + (r.get("description") or "")),
            })
    return recs

def main():
    recs = load()
    clim_par_edition = collections.Counter(r["edition"] for r in recs if r["clim"])
    ext = [r for r in recs if EXTREME.search(r["text"])]
    print(f"{len(recs)} sujets, {len({r['edition'] for r in recs})} éditions")
    print(f"{len(ext)} sujets d'événement extrême "
          f"({100 * len(ext) / len(recs):.1f} % du corpus)\n")
    print(f"{'famille':26s} {'n':>6} {'sujet':>8} {'édition':>9} {'jamais':>8}")
    lignes = []
    for label, pattern in FAMILLES + [("Ensemble", EXTREME.pattern)]:
        rx = re.compile(pattern)
        g = [r for r in ext if rx.search(r["text"])]
        sujet = 100 * sum(1 for r in g if r["clim"]) / len(g)
        edition = 100 * sum(1 for r in g if clim_par_edition[r["edition"]]) / len(g)
        print(f"{label:26s} {len(g):6d} {sujet:7.1f}% {edition:8.1f}% {100 - edition:7.1f}%")
        lignes.append((label, len(g), sujet, edition))
    print("\n// à recopier dans docs/getDataCatastrophes.js")
    for label, n, sujet, edition in lignes:
        print(f"  {{ label: '{label}', n: {n}, sujet: {sujet:.1f}, edition: {edition:.1f} }},")

def exemples(n=5):
    """Liste, par famille, des sujets dont le JT entier ne nomme jamais le climat.

    Sert à rafraîchir le bloc d'exemples sous le graphique dans docs/index.html.
    """
    recs = load()
    clim_par_edition = collections.Counter(r["edition"] for r in recs if r["clim"])
    for label, pattern in FAMILLES:
        rx = re.compile(pattern)
        g = [r for r in recs
             if rx.search(norm(r["title"]))
             and not clim_par_edition[r["edition"]]
             and r["url"].startswith("http")
             and 40 < len(r["title"]) < 100]
        g.sort(key=lambda r: r["date"], reverse=True)
        print(f"\n### {label} — {len(g)} sujets candidats")
        for r in g[:n]:
            print(f"   {r['date']} | {r['title']}\n      {r['url']}")

if __name__ == "__main__":
    if "--exemples" in sys.argv:
        exemples()
    else:
        main()
