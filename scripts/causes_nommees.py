#!/usr/bin/env python3
"""Feed docs/getDataCauses.js : quelle cause les sujets climat nomment-ils ?

Illustre le propos de Jean-Baptiste Comby cité sur la page d'accueil : la
catégorie « activités humaines » n'ouvre pas de discussion sur les logiques
productives, industrielles ou financières. On compte, parmi les sujets détectés
comme climatiques, ceux qui citent chacun des quatre registres ci-dessous.

Chaque sujet est classé dans UN SEUL registre, dans l'ordre de la liste ci-dessous :
le plus exigeant l'emporte, pour ne jamais sous-estimer la part des sujets qui
nomment un responsable. Les parts totalisent donc 100 %.

Usage : python3 scripts/causes_nommees.py   (depuis la racine du dépôt)
"""
import json, glob, re, collections, unicodedata

def norm(s):
    s = unicodedata.normalize("NFD", (s or "").lower())
    return "".join(c for c in s if unicodedata.category(c) != "Mn")

AUCUN = "Aucune cause, aucun responsable"
# L'ordre vaut priorité : le premier registre trouvé l'emporte. Il va du plus
# précis au plus vague (responsable > geste à faire > mécanisme > « activités
# humaines »), de sorte que la part des sujets nommant un responsable soit un
# plafond et non un plancher.
REGISTRES = [
    ("Un acteur industriel ou financier",
     r"industrie petroliere|lobby petrolier|multinationale|totalenergies|total energies|"
     r"compagnie petroliere|actionnaire|exxon|shell|bp |major petroliere|profits record|"
     r"groupe industriel|industriels|patronat|cimentier|agro-industrie|secteur aerien|"
     r"compagnies aeriennes"),
    ("Un geste individuel à faire",
     r"ecogeste|eco-geste|gestes simples|bons gestes|chaque geste compte|bons reflexes|"
     r"trier ses dechets|moins de viande|covoiturage|eteindre|consommer moins|sobriete"),
    ("Un mécanisme physique (CO2, fossiles)",
     r"gaz a effet de serre|\bco2\b|dioxyde de carbone|energies fossiles|"
     r"combustibles fossiles|effet de serre"),
    ("« Les activités humaines », sans plus",
     r"activites humaines|activite humaine|l'homme est responsable|a cause de l'homme|"
     r"notre mode de vie|comportement humain"),
]
SINCE = "2019"

def main():
    clim = []
    for f in glob.glob("data-news-json/media=*/year=*/month=*/day=*/*.json"):
        for line in open(f, encoding="utf-8"):
            line = line.strip()
            if not line:
                continue
            r = json.loads(line)
            if r["date"][:4] < SINCE or not r.get("containsWordGlobalWarming"):
                continue
            clim.append(norm((r.get("title") or "") + " . " + (r.get("description") or "")))
    print(f"{len(clim)} sujets climat depuis {SINCE}\n")
    rxs = [(label, re.compile(p)) for label, p in REGISTRES]
    compte = collections.Counter()
    for t in clim:
        for label, rx in rxs:
            if rx.search(t):
                compte[label] += 1
                break
        else:
            compte[AUCUN] += 1
    total = 0.0
    for label in [l for l, _ in rxs] + [AUCUN]:
        part = 100 * compte[label] / len(clim)
        total += part
        print(f"  {label:42s} {compte[label]:5d} = {part:5.1f} %")
    print(f"\n  TOTAL = {total:.1f} %  ({sum(compte.values())} / {len(clim)} sujets)")
    print("\n// à recopier dans docs/getDataCauses.js (ordre inversé : Plotly dessine le 1er en bas)")
    for label in reversed([l for l, _ in rxs] + [AUCUN]):
        print(f"  {{ label: '{label}', pct: {100 * compte[label] / len(clim):.1f}, "
              f"n: {compte[label]} }},")

if __name__ == "__main__":
    main()
