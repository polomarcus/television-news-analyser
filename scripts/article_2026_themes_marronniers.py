#!/usr/bin/env python3
"""Advanced categorization of the 237k JT reports:
- thematic categories (lexicon-based)
- 'problematic' reports: promoting GHG-intensive activities/against IPCC demand-side
  recommendations, WITHOUT naming climate
- marronniers (seasonal evergreens): recurrence + month concentration
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
            "media": media, "date": r["date"][:10], "year": r["date"][:4],
            "month": r["date"][5:7], "order": r.get("order"),
            "title": r.get("title") or "", "url": r.get("url") or "",
            "clim": bool(r.get("containsWordGlobalWarming")),
            "text": norm((r.get("title") or "") + " " + (r.get("description") or "")),
        })
print(f"{len(recs)} reports loaded")

# ---------- 1. THEMES ----------
THEMES = {
    "meteo-extreme": ["canicule", "vague de chaleur", "secheresse", "inondation", "crue ", "tempete", "cyclone", "ouragan", "incendie", "feux de foret", "feu de foret", "grele", "orage", "vague de froid", "megafeu"],
    "energie": ["nucleaire", "eolien", "photovoltaique", "panneau solaire", "petrole", "gaz naturel", "carburant", "essence", "diesel", "electricite", "edf", "centrale a charbon", "hydrogene", "prix de l'energie", "facture d'energie"],
    "transport-voiture": ["automobile", "voiture", "suv", "autoroute", "permis de conduire", "vehicule electrique", "diesel", "carburant"],
    "aerien-croisiere": ["avion", "aeroport", "compagnie aerienne", "long-courrier", "croisiere", "paquebot", "jet prive"],
    "agriculture-alimentation": ["agriculteur", "agricole", "elevage", "eleveur", "viande", "cereale", "recolte", "vendange", "pesticide", "glyphosate", "bio ", "vegetarien", "vegan"],
    "consommation": ["soldes", "black friday", "pouvoir d'achat", "supermarche", "noel", "jouet", "cadeaux", "promotion"],
    "tourisme-vacances": ["vacances", "tourisme", "touriste", "station de ski", "ski ", "plage", "camping", "croisiere", "chasse-croise", "chasses-croises"],
    "biodiversite": ["biodiversite", "espece menacee", "abeille", "coraux", "foret amazonienne", "deforestation", "ours polaire", "extinction"],
    "politique-climat": ["cop2", "cop 2", "giec", "accord de paris", "transition ecologique", "neutralite carbone", "taxe carbone", "convention citoyenne", "ministre de la transition"],
    "sante": ["hopital", "cancer", "epidemie", "covid", "coronavirus", "vaccin", "grippe", "urgences"],
    "guerre-international": ["guerre", "ukraine", "gaza", "israel", "syrie", "attentat", "terroriste", "otan"],
}
theme_counts = collections.defaultdict(lambda: collections.Counter())
theme_clim = collections.Counter(); theme_tot = collections.Counter()
for r in recs:
    for th, kws in THEMES.items():
        if any(k in r["text"] for k in kws):
            theme_tot[th] += 1
            theme_counts[th][r["year"]] += 1
            if r["clim"]: theme_clim[th] += 1

print("\n== THEMES (tout le corpus) : volume, et % nommant le climat ==")
for th, t in theme_tot.most_common():
    c = theme_clim[th]
    print(f"{th:26s} {t:6d} reportages | {100*c/t:5.1f}% nomment le climat")

# ---------- 2. PROBLEMATIC (against IPCC demand-side recommendations) ----------
# AR6 WG3 ch.5: demand-side mitigation = less flying, less driving/SUV,
# less meat, sufficiency vs consumerism. 'Problematic' = report PROMOTES
# such activity (lexicon of promo terms) and never names climate.
PROBLEMATIC = {
    "promo-croisiere": (["croisiere", "paquebot"], ["embarquez", "reve", "geant des mers", "plus grand paquebot", "luxe", "escale", "a bord du"]),
    "promo-aviation": (["avion", "compagnie aerienne", "aeroport", "long-courrier"], ["billets a prix", "low cost", "vols pas chers", "nouvelle ligne", "destination de reve", "partir loin"]),
    "promo-auto": (["voiture", "suv", "automobile", "4x4"], ["salon de l'auto", "nouveau modele", "succes des suv", "vente record", "essayer le nouveau"]),
    "promo-ski-neige-artificielle": (["station de ski", "canon a neige", "neige artificielle", "neige de culture"], ["sauver la saison", "ouverture des pistes", "enneigement garanti"]),
    "promo-conso": (["black friday", "soldes"], ["bonnes affaires", "records", "rue vers", "profiter des promotions"]),
    "promo-viande": (["viande", "barbecue", "foie gras"], ["tradition", "plaisir", "roi de la table", "incontournable des fetes"]),
}
prob = collections.defaultdict(list)
for r in recs:
    if r["clim"]: continue
    for cat, (subject_kws, promo_kws) in PROBLEMATIC.items():
        if any(k in r["text"] for k in subject_kws) and any(p in r["text"] for p in promo_kws):
            prob[cat].append(r)

print("\n== REPORTAGES PROBLEMATIQUES (promotion d'activites carbonees, sans nommer le climat) ==")
tot_prob = 0
for cat, rs in sorted(prob.items(), key=lambda kv: -len(kv[1])):
    tot_prob += len(rs)
    byyear = collections.Counter(r["year"] for r in rs)
    trend = " ".join(f"{y}:{byyear[y]}" for y in sorted(byyear))
    print(f"\n{cat}: {len(rs)} reportages | {trend}")
    for r in sorted(rs, key=lambda r: r["date"], reverse=True)[:3]:
        print(f"   {r['date']} {r['media']} | {r['title'][:90]}")
print(f"\nTOTAL problematiques: {tot_prob}")

# subject coverage without promo filter, for context
print("\n== SUJETS CARBONES : part qui ne nomme JAMAIS le climat ==")
for label, kws in [("croisiere/paquebot", ["croisiere", "paquebot"]),
                   ("aviation", ["compagnie aerienne", "aeroport", "long-courrier"]),
                   ("SUV", ["suv"]),
                   ("ski/neige artificielle", ["station de ski", "neige artificielle", "canon a neige", "neige de culture"]),
                   ("black friday/soldes", ["black friday", "soldes"])]:
    sub = [r for r in recs if any(k in r["text"] for k in kws)]
    c = sum(1 for r in sub if r["clim"])
    if sub:
        print(f"{label:24s} {len(sub):5d} reportages, {100*(1-c/len(sub)):5.1f}% sans le mot climat")

# ---------- 3. MARRONNIERS ----------
# candidate seasonal topics; measure: multi-year recurrence + month concentration
MARRONNIERS = {
    "chasse-croise juillettistes/aoutiens": ["chasse-croise", "chasses-croises", "juilletiste", "aoutien", "bison fute"],
    "rentree scolaire": ["rentree scolaire", "rentree des classes", "fournitures scolaires"],
    "beaujolais nouveau": ["beaujolais nouveau"],
    "marches de noel": ["marche de noel", "marches de noel"],
    "jouets de noel": ["jouets", "pere noel"],
    "soldes": ["soldes d'hiver", "soldes d'ete", "premier jour des soldes"],
    "muguet du 1er mai": ["muguet"],
    "galette des rois": ["galette des rois"],
    "chocolats de paques": ["chocolat de paques", "chocolats de paques", "chasse aux oeufs"],
    "vendanges": ["vendange"],
    "bac/examens": ["baccalaureat", "epreuve du bac", "resultats du bac"],
    "depart en vacances/plages": ["premiers departs en vacances", "aoutiens", "plages bondees"],
}
print("\n== MARRONNIERS : recurrence annuelle et saisonnalite ==")
print(f"{'marronnier':38s} {'total':>5s} {'annees':>6s} {'mois pic (part)':>18s} {'% climat':>9s}")
for name, kws in MARRONNIERS.items():
    sub = [r for r in recs if any(k in r["text"] for k in kws)]
    if not sub: continue
    years = len({r['year'] for r in sub})
    months = collections.Counter(r["month"] for r in sub)
    top_m, top_c = months.most_common(1)[0]
    clim = sum(1 for r in sub if r["clim"])
    print(f"{name:38s} {len(sub):5d} {years:6d} {top_m}: {100*top_c/len(sub):5.1f}% {100*clim/len(sub):8.1f}%")

# discovery: title bigrams highly seasonal, >=8 years
print("\n== MARRONNIERS DECOUVERTS (bigrammes de titres, >=8 ans, >=60% sur un mois) ==")
bg = collections.defaultdict(list)
stop = set("de la le les des du une un et a au aux en dans pour sur qui que ce cette ces son sa ses plus d l est sont avec ne pas".split())
for r in recs:
    words = [w for w in re.findall(r"[a-z']{3,}", norm(r["title"])) if w not in stop]
    for i in range(len(words)-1):
        bg[(words[i], words[i+1])].append((r["year"], r["month"]))
found = []
for k, occs in bg.items():
    if len(occs) < 25: continue
    years = len({y for y, m in occs})
    months = collections.Counter(m for y, m in occs)
    top_m, top_c = months.most_common(1)[0]
    if years >= 8 and top_c/len(occs) >= 0.6:
        found.append((len(occs), " ".join(k), years, top_m, 100*top_c/len(occs)))
for n, phrase, years, m, share in sorted(found, reverse=True)[:20]:
    print(f"{phrase:35s} {n:4d} occ., {years} ans, mois {m} ({share:.0f}%)")
