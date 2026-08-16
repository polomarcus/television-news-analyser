#!/usr/bin/env python3
"""Deep content analysis:
1. LE SPECTACLE: in extreme-weather reports, emotional/victim framing vs naming causes
2. Fun comparative stats (Tour de France vs urgent issues, etc.)
3. Stricter 'problematic' detection with printable samples for manual verification
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
        if not line: continue
        r = json.loads(line)
        recs.append({
            "media": media, "date": r["date"][:10], "year": int(r["date"][:4]),
            "title": r.get("title") or "", "url": r.get("url") or "",
            "clim": bool(r.get("containsWordGlobalWarming")),
            "text": norm((r.get("title") or "") + " . " + (r.get("description") or "")),
        })
print(f"{len(recs)} reports loaded\n")

def has(r, kws): return any(k in r["text"] for k in kws)

# ============ 1. LE SPECTACLE ============
EXTREME = ["canicule", "vague de chaleur", "secheresse", "inondation", "crue ", "tempete",
           "cyclone", "ouragan", "incendie", "feux de foret", "feu de foret", "megafeu", "grele"]
EMOTION = ["en larmes", "en pleurs", "tout perdu", "desarroi", "detresse", "desolation",
           "traumatis", "choc pour les habitants", "sinistre", "temoignage", "temoigne",
           "bouleversant", "devaste", "ravage", "cauchemar", "apocalyps", "impuissant",
           "les degats", "tout reconstruire", "sa maison detruite", "ont tout perdu"]
CAUSES = ["gaz a effet de serre", "co2", "dioxyde de carbone", "energies fossiles",
          "empreinte carbone", "emissions de", "petrole", "charbon", "industrie petroliere",
          "combustibles fossiles", "activites humaines", "giec"]

extreme = [r for r in recs if has(r, EXTREME)]
emo = [r for r in extreme if has(r, EMOTION)]
cause = [r for r in extreme if has(r, CAUSES)]
climnamed = [r for r in extreme if r["clim"]]
print("== LE SPECTACLE : reportages meteo extreme (canicule/secheresse/inondation/incendie/tempete) ==")
print(f"total: {len(extreme)}")
print(f"registre emotionnel/victime (larmes, tout perdu, detresse, temoignage...): {len(emo)} = {100*len(emo)/len(extreme):.1f}%")
print(f"nomment le climat: {len(climnamed)} = {100*len(climnamed)/len(extreme):.1f}%")
print(f"nomment une CAUSE (GES, CO2, fossiles, GIEC...): {len(cause)} = {100*len(cause)/len(extreme):.1f}%")
both = [r for r in extreme if has(r, EMOTION) and has(r, CAUSES)]
print(f"les deux (emotion ET cause): {len(both)} = {100*len(both)/len(extreme):.1f}%")
# trend by period
for lo, hi in [(2013, 2021), (2022, 2026)]:
    sub = [r for r in extreme if lo <= r["year"] <= hi]
    e = sum(1 for r in sub if has(r, EMOTION)); c = sum(1 for r in sub if has(r, CAUSES))
    print(f"  {lo}-{hi}: emotion {100*e/len(sub):.1f}% | causes {100*c/len(sub):.1f}% (n={len(sub)})")
print("\nexemples recents emotion sans cause :")
for r in sorted([r for r in extreme if has(r, EMOTION) and not has(r, CAUSES)], key=lambda r: r["date"], reverse=True)[:6]:
    print(f"  {r['date']} {r['media']} | {r['title'][:95]}")

# ============ 2. FUN COMPARATIVE STATS ============
print("\n== COMPARAISONS ==")
TOPICS = {
    "Tour de France (cyclisme)": ["tour de france"],
    "Festival de Cannes": ["festival de cannes"],
    "Johnny Hallyday": ["johnny hallyday"],
    "galette des rois": ["galette des rois"],
    "beaujolais nouveau": ["beaujolais nouveau"],
    "muguet du 1er mai": ["muguet"],
    "GIEC": ["giec"],
    "secheresse & agriculture/alimentation": None,  # computed below
    "adaptation au changement climatique": ["adaptation au changement climatique", "s'adapter au changement climatique", "s'adapter au rechauffement"],
    "energies fossiles / sortie du petrole": ["energies fossiles", "sortie du petrole", "combustibles fossiles"],
    "sobriete": ["sobriete"],
    "renovation thermique/energetique": ["renovation energetique", "renovation thermique", "passoire thermique", "passoires thermiques"],
    "voiture electrique": ["voiture electrique", "vehicule electrique"],
    "eco-anxiete": ["eco-anxiete", "ecoanxiete", "eco anxiete"],
    "montee des eaux / submersion": ["montee des eaux", "submersion", "recul du trait de cote", "erosion du littoral"],
}
results = {}
for name, kws in TOPICS.items():
    if kws is None: continue
    sub = [r for r in recs if has(r, kws)]
    results[name] = sub
# secheresse impacting food production in France
sech_agri = [r for r in recs if "secheresse" in r["text"] and has(r, ["recolte", "agricult", "eleveur", "elevage", "rendement", "cereale", "mais ", "ble ", "fourrage", "irrigation", "restriction d'eau"])]
results["secheresse & agriculture/alimentation"] = sech_agri
for name in TOPICS:
    sub = results[name]
    byyear = collections.Counter(r["year"] for r in sub)
    peak = max(byyear.items(), key=lambda kv: kv[1]) if byyear else ("-", 0)
    print(f"{name:42s} {len(sub):5d} reportages (pic {peak[0]}: {peak[1]})")

print("\nRatios parlants :")
tdf = len(results["Tour de France (cyclisme)"])
print(f"  Tour de France ({tdf}) vs GIEC ({len(results['GIEC'])}) : ratio {tdf/max(1,len(results['GIEC'])):.1f}x")
print(f"  Tour de France vs adaptation au chgt climatique ({len(results['adaptation au changement climatique'])}) : {tdf/max(1,len(results['adaptation au changement climatique'])):.1f}x")
print(f"  Tour de France vs energies fossiles ({len(results['energies fossiles / sortie du petrole'])}) : {tdf/max(1,len(results['energies fossiles / sortie du petrole'])):.1f}x")
print(f"  Tour de France vs secheresse&agri ({len(sech_agri)}) : {tdf/max(1,len(sech_agri)):.1f}x")
print(f"  Johnny Hallyday ({len(results['Johnny Hallyday'])}) vs sobriete ({len(results['sobriete'])})")
print(f"  Festival de Cannes ({len(results['Festival de Cannes'])}) vs montee des eaux ({len(results['montee des eaux / submersion'])})")

# same in *titles* only (stronger claim: subject of the report)
print("\nEn se limitant aux TITRES (le sujet du reportage, pas une mention) :")
def title_has(r, kws): return any(k in norm(r["title"]) for k in kws)
tdf_t = [r for r in recs if title_has(r, ["tour de france"])]
giec_t = [r for r in recs if title_has(r, ["giec"])]
sech_agri_t = [r for r in recs if title_has(r, ["secheresse"]) and has(r, ["recolte", "agricult", "eleveur", "elevage", "rendement", "cereale", "fourrage", "irrigation"])]
print(f"  titres Tour de France: {len(tdf_t)} | titres GIEC: {len(giec_t)} | titres secheresse (contexte agricole): {len(sech_agri_t)}")

# ============ 3. STRICT PROBLEMATIC ============
print("\n== PROBLEMATIQUES (regles resserrees) : echantillons a verifier ==")
STRICT = {
    "croisieres-vitrine": lambda r: (not r["clim"]) and has(r, ["croisiere", "paquebot"]) and has(r, ["plus grand paquebot", "geant des mers", "a bord du plus", "paquebot de luxe", "succes des croisieres", "croisieres ont la cote", "engouement pour les croisieres", "reve de croisiere"]),
    "black-friday-fete": lambda r: (not r["clim"]) and has(r, ["black friday"]) and has(r, ["bonnes affaires", "bons plans", "rabais", "profiter des promotions", "records"]),
    "suv-succes": lambda r: (not r["clim"]) and has(r, ["suv"]) and has(r, ["succes", "vente record", "en plein boom", "plebiscite", "cartonne"]),
    "neige-artificielle-salut": lambda r: (not r["clim"]) and has(r, ["neige artificielle", "canon a neige", "canons a neige", "neige de culture"]) and has(r, ["sauver la saison", "sauver les vacances", "garantir l'enneigement", "produire de la neige"]),
    "avion-bas-cout-aubaine": lambda r: (not r["clim"]) and has(r, ["low cost", "vols pas chers", "billets d'avion a prix casse"]) and has(r, ["avion", "compagnie", "aerien"]) and has(r, ["aubaine", "bonne affaire", "bons plans", "prix imbattable", "voyager moins cher"]),
}
for cat, rule in STRICT.items():
    matches = [r for r in recs if rule(r)]
    byyear = collections.Counter(r["year"] for r in matches)
    print(f"\n{cat}: {len(matches)} | " + " ".join(f"{y}:{c}" for y, c in sorted(byyear.items())))
    for r in sorted(matches, key=lambda r: r["date"], reverse=True)[:5]:
        print(f"   {r['date']} {r['media']} | {r['title'][:90]}")
        print(f"     {r['url'][:100]}")
