#!/usr/bin/env python3
"""Generate docs/data-aggregated-news-json/yearly.json for docs/annees.html.

For each year: per-channel climate coverage, theme volumes (keyword lexicons),
and top-3 worst/best climate-treatment picks. Picks are detected automatically
by the rules below, then curated: PINNED prepends hand-verified entries and
BLACKLIST drops reviewed false positives (consumer-protection Black Friday
pieces, critical cruise coverage, drug busts…). Re-run after new data lands;
review any new automatic picks before publishing.

Usage: python3 scripts/generate_yearly_stats.py   (from the repo root)
"""
import json, glob, unicodedata

def norm(s):
    s = unicodedata.normalize("NFD", (s or "").lower())
    return "".join(c for c in s if unicodedata.category(c) != "Mn")

def has(r, kws): return any(k in r["text"] for k in kws)
def thas(r, kws): return any(k in norm(r["title"]) for k in kws)

THEMES = {
    "Météo extrême": ["canicule", "vague de chaleur", "secheresse", "inondation", "crue ", "tempete", "cyclone", "ouragan", "incendie", "feux de foret", "grele", "megafeu"],
    "Guerre / international": ["guerre", "ukraine", "gaza", "israel", "syrie", "attentat", "terroriste", "otan"],
    "Santé": ["hopital", "cancer", "epidemie", "covid", "coronavirus", "vaccin", "grippe", "urgences"],
    "Tourisme / vacances": ["vacances", "tourisme", "touriste", "station de ski", "plage", "camping", "croisiere", "chasse-croise"],
    "Voiture / carburant": ["automobile", "voiture", "autoroute", "carburant", "essence", "diesel"],
    "Agriculture / alimentation": ["agriculteur", "agricole", "elevage", "eleveur", "viande", "recolte", "vendange", "pesticide"],
    "Consommation / fêtes": ["soldes", "black friday", "pouvoir d'achat", "supermarche", "noel", "jouet", "cadeaux"],
    "Énergie": ["nucleaire", "eolien", "photovoltaique", "petrole", "electricite", "edf", "hydrogene"],
    "Politique climatique": ["cop2", "cop 2", "giec", "accord de paris", "transition ecologique", "neutralite carbone", "taxe carbone"],
}
TOURIST_TITLE = ["decouverte", "a la decouverte", "vacances", "tourisme", "voyage", "paradis", "ile ", "l'ile", "plage", "croisiere", "paquebot", "polynesie", "maldives", "seychelles", "zanzibar", "copacabana", "santorin", "dubai", "marrakech", "week-end au large", "joyau", "exception"]
CAUSES = ["gaz a effet de serre", "co2", "dioxyde de carbone", "energies fossiles", "empreinte carbone", "emissions de", "combustibles fossiles", "activites humaines", "giec", "petrole"]
WARM_JOY = ["profiter du soleil", "comme en ete", "en terrasse", "un air d'ete", "on trinque", "douceur agreable", "records de douceur", "meteo clemente"]
# titles that look touristic but are in fact critical/news coverage
EXCLUDE_TITLE = ["poubelle", "pollution", "naufrage", "epave", "dechet", "menace", "rechauffement", "montee des eaux", "surtourisme", "erosion", "noie", "faillite", "trafiquant", "arnaque", "contrefacon"]

# Hand-verified picks, prepended to the automatic list (year -> entries).
PINNED = {
    "worst": {
        "2013": [("2013-09-23", "France 2", "Saint-Nazaire : construire le plus grand paquebot du monde", "https://www.francetvinfo.fr/saint-nazaire-construire-le-plus-grand-paquebot-du-monde_418443.html", "promo croisière")],
        "2014": [("2014-03-01", "France 2", "Découverte : deux jours à Marrakech", "https://www.francetvinfo.fr/decouverte-deux-jours-a-marrakech_542431.html", "promo long-courrier en plein hiver")],
        "2017": [("2017-05-27", "France 2", "Monoï : l'huile qui vaut de l'or", "https://www.francetvinfo.fr/economie/industrie/monoi-l-huile-qui-vaut-de-l-or_2209730.html", "promo long-courrier"),
                 ("2017-07-28", "France 2", "Saint-Eustache : l'île des Caraïbes aux perles bleues", "https://www.francetvinfo.fr/france/polynesie-francaise/saint-eustache-lile-des-caraibes-aux-perles-bleues_2304343.html", "carte postale pendant canicule")],
        "2022": [("2022-11-25", "France 2", "Black Friday : beaucoup de Français misent sur les promotions pour faire leurs achats de Noël", "https://www.francetvinfo.fr/economie/pouvoir-achat/black-friday-beaucoup-de-francais-misent-sur-les-promotions-pour-faire-leurs-achats-de-noel_5501157.html", "black friday en fête, année des mégafeux")],
        "2023": [("2023-09-22", "France 2", "Croisière : le plus grand paquebot du monde embarquera 10 000 personnes", "https://www.francetvinfo.fr/economie/tourisme/croisiere-le-plus-grand-paquebot-du-monde-embarquera-10-000-personnes_6078288.html", "promo croisière, année la plus chaude jamais mesurée alors")],
        "2024": [("2024-11-29", "TF1", "Nos petits commerçants se mettent au Black Friday", "https://www.tf1info.fr/conso/videos/video-nos-petits-commercants-se-mettent-au-black-friday-4567-2336832.html", "black friday en fête")],
        "2025": [("2025-03-27", "TF1", "Six piscines, 33 bars restaurants... à bord d'un géant", "https://www.tf1info.fr/regions/videos/video-six-piscines-33-bars-restaurants-a-bord-d-un-geant-80337-2361817.html", "promo croisière")],
        "2026": [("2026-05-30", "France 2", "Croisières fluviales : de plus en plus de touristes se laissent tenter", "https://www.francetvinfo.fr/replay-jt/france-2/13-heures/croisieres-fluviales-de-plus-en-plus-de-touristes-se-laissent-tenter_8037215.html", "promo croisière, un mois avant la canicule")],
    },
    "best": {
        "2024": [("2024-04-16", "TF1", "Aux origines du réchauffement climatique : la vidéo exceptionnelle de TF1 pour tout comprendre en 6 minutes", "https://www.tf1info.fr/environnement-ecologie/aux-origines-du-rechauffement-climatique-la-video-exceptionnelle-en-realite-augmentee-de-tf1-pour-tout-comprendre-en-6-minutes-2277936.html", "pédagogie des causes"),
                 ("2024-12-11", "France 2", "Réchauffement climatique : l'explosion des émissions de gaz à effet de serre liées au tourisme", "https://www.francetvinfo.fr/monde/environnement/crise-climatique/rechauffement-climatique-l-explosion-des-emissions-de-gaz-a-effet-de-serre-liees-au-tourisme_6949730.html", "nomme les causes, y compris celles du JT"),
                 ("2024-02-20", "France 2", "Réchauffement climatique : les Français sont-ils prêts à manger moins de viande ?", "https://www.francetvinfo.fr/monde/environnement/rechauffement-climatique-les-francais-sont-ils-prets-a-manger-moins-de-viande_6377740.html", "leviers de demande (GIEC WG3)")],
    },
}
# reviewed automatic picks to drop (title substrings, normalized/accent-free):
# drug busts, economy pieces, listing-page artifacts, critical/eco-guilt angles
BLACKLIST = ["drogue", "saisie record", "vent en poupe", "a suivre :", "si je voulais partir"]

# The all-years top 10, fully hand-curated. Entries referencing a yearly pick
# use ("ref", year, kind, title_prefix) so the URL stays single-sourced;
# extras carry their own verified URL.
ALL_TIME = {
    "worst": [
        ("ref", "2023", "worst", "Croisière : le plus grand paquebot"),
        ("2026-06-27", "France 2", "Paysages à couper le souffle, thé noir… À la découverte du train bleu du Sri Lanka", "https://www.francetvinfo.fr/replay-jt/france-2/13-heures/paysages-a-couper-le-souffle-the-noir-a-la-decouverte-du-train-bleu-du-sri-lanka_8082641.html", "diffusé le lendemain de l'interview de Jancovici au 20H, en pleine canicule"),
        ("ref", "2026", "worst", "Les Maldives : à la découverte"),
        ("ref", "2025", "worst", "Six piscines, 33 bars restaurants"),
        ("ref", "2021", "worst", "Paquebots de croisière : la folie"),
        ("2026-08-08", "France 2", "En Allemagne, l'île de Rügen offre un petit coin de paradis sur la Baltique", "https://www.francetvinfo.fr/replay-jt/france-2/13-heures/en-allemagne-l-ile-de-rugen-offre-un-petit-coin-de-paradis-sur-la-baltique_8139836.html", "dans la même édition que l'angoisse des agriculteurs face à la sécheresse"),
        ("ref", "2018", "worst", "Seychelles, se marier"),
        ("ref", "2016", "worst", "Le plus gros paquebot de croisière"),
        ("ref", "2024", "worst", "Même sans neige, cette station de ski"),
        ("ref", "2022", "worst", "Black Friday : beaucoup de Français"),
    ],
    "best": [
        ("2026-06-26", "France 2", "Environnement : malgré 50 ans d'alerte, un dérèglement climatique qui s'accentue inexorablement", "https://www.francetvinfo.fr/environnement/evenements-meteorologiques-extremes/vagues-de-chaleur-canicules/environnement-malgre-50-ans-d-alerte-un-dereglement-climatique-qui-s-accentue-inexorablement_8081801.html", "le soir de l'interview de Jancovici, nommer le dérèglement en pleine canicule"),
        ("ref", "2024", "best", "Aux origines du réchauffement climatique"),
        ("ref", "2026", "best", "Le retour de la neige en basse altitude"),
        ("ref", "2024", "best", "Réchauffement climatique : l'explosion des émissions"),
        ("ref", "2023", "best", "Réchauffement climatique : les scientifiques du Giec"),
        ("ref", "2026", "best", "\"Les solutions sont déjà là\""),
        ("ref", "2022", "best", "Climat : les recommandations du Giec"),
        ("ref", "2024", "best", "Réchauffement climatique : les Français sont-ils prêts"),
        ("ref", "2021", "best", "Environnement : les émissions de carbone"),
        ("ref", "2018", "best", "Climat : le scénario catastrophe"),
    ],
}

def edition(tv):
    if "20-heures" in tv: return "20H"
    if "13-heures" in tv: return "13H"
    return None

def load_records():
    recs = []
    for f in glob.glob("data-news-json/media=*/year=*/month=*/day=*/*.json"):
        media = f.split("media=")[1].split("/")[0]
        for line in open(f):
            line = line.strip()
            if not line:
                continue
            r = json.loads(line)
            recs.append({"media": media, "date": r["date"][:10], "year": int(r["date"][:4]),
                         "month": int(r["date"][5:7]), "order": r.get("order", 0),
                         "title": r.get("title") or "", "url": r.get("url") or "",
                         "edition": edition(r.get("urlTvNews") or "") if media == "France 2" else None,
                         "clim": bool(r.get("containsWordGlobalWarming")),
                         "text": norm((r.get("title") or "") + " " + (r.get("description") or ""))})
    return recs

def score_worst(r, extreme_months):
    score, why = 0, []
    if thas(r, ["plus grand paquebot", "geant des mers"]) or (thas(r, ["croisiere"]) and has(r, ["succes", "record", "luxe", "embarquera"])):
        score += 4; why.append("promo croisière")
    if thas(r, TOURIST_TITLE) and r["month"] in extreme_months and r["month"] in (6, 7, 8):
        score += 3; why.append("carte postale pendant canicule/sécheresse")
    if thas(r, ["polynesie", "maldives", "seychelles", "zanzibar", "bora"]) and has(r, ["decouverte", "paradis", "reve", "perle", "exception"]):
        score += 3; why.append("promo long-courrier")
    if has(r, WARM_JOY) and r["month"] in (11, 12, 1, 2):
        score += 3; why.append("douceur hivernale célébrée")
    if thas(r, ["black friday"]) and has(r, ["bonnes affaires", "bons plans", "records", "rue vers"]):
        score += 3; why.append("black friday en fête")
    return score, ", ".join(why)

def score_best(r):
    score, why = 0, []
    if has(r, CAUSES): score += 2; why.append("nomme les causes")
    if r["order"] == 1 and r["media"] != "TF1": score += 2; why.append("ouverture du JT")
    if thas(r, ["rechauffement", "dereglement", "changement climatique", "giec", "crise climatique"]):
        score += 1; why.append("climat dans le titre")
    if has(r, ["energies fossiles", "combustibles fossiles"]): score += 1; why.append("énergies fossiles nommées")
    if has(r, ["adaptation au changement climatique", "s'adapter au"]): score += 1; why.append("adaptation")
    return score, ", ".join(why)

def top3(candidates):
    seen, res = set(), []
    for s, r, w in sorted(candidates, key=lambda x: (-x[0], x[1]["date"])):
        k = norm(r["title"])[:40]
        if k in seen or any(b in norm(r["title"]) for b in BLACKLIST):
            continue
        seen.add(k)
        res.append({"date": r["date"], "media": r["media"], "title": r["title"], "url": r["url"], "why": w})
        if len(res) == 3:
            break
    return res

def main():
    recs = load_records()
    out = {}
    for y in sorted({r["year"] for r in recs}):
        yr = [r for r in recs if r["year"] == y]
        extreme_months = {r["month"] for r in yr if has(r, ["canicule", "secheresse historique", "vague de chaleur"])}
        media_stats = {}
        for m in ["France 2", "France 3", "TF1"]:
            sub = [r for r in yr if r["media"] == m]
            if sub:
                c = sum(1 for r in sub if r["clim"])
                media_stats[m] = {"total": len(sub), "climat": c, "pct": round(100 * c / len(sub), 2)}
        worst, best = [], []
        for r in yr:
            if not r["title"].strip():
                continue
            if not r["clim"] and not thas(r, EXCLUDE_TITLE):
                s, w = score_worst(r, extreme_months)
                if s >= 3:
                    worst.append((s, r, w))
            if r["clim"]:
                s, w = score_best(r)
                if s >= 3:
                    best.append((s, r, w))
        themes = {t: sum(1 for r in yr if has(r, kws)) for t, kws in THEMES.items()}
        themes["Climat (nommé)"] = sum(1 for r in yr if r["clim"])
        editions = {}
        for ed in ("13H", "20H"):
            sub = [r for r in yr if r.get("edition") == ed]
            if sub:
                c = sum(1 for r in sub if r["clim"])
                editions[ed] = {"total": len(sub), "climat": c, "pct": round(100 * c / len(sub), 2)}
        out[str(y)] = {"media": media_stats, "themes": themes, "worst": top3(worst), "best": top3(best)}
        if editions:
            out[str(y)]["editions"] = editions

    for kind in ("worst", "best"):
        for y, pins in PINNED[kind].items():
            if y not in out:
                continue
            pinned = [{"date": d_, "media": m_, "title": t_, "url": u_, "why": w_} for d_, m_, t_, u_, w_ in pins]
            pinned_keys = {norm(p["title"])[:40] for p in pinned}
            rest = [e for e in out[y][kind] if norm(e["title"])[:40] not in pinned_keys]
            out[y][kind] = (pinned + rest)[:3]

    # all-years tab: aggregated stats + hand-curated top 10
    all_media = {}
    for m in ["France 2", "France 3", "TF1"]:
        sub = [r for r in recs if r["media"] == m]
        if sub:
            c = sum(1 for r in sub if r["clim"])
            all_media[m] = {"total": len(sub), "climat": c, "pct": round(100 * c / len(sub), 2)}
    all_themes = {t: sum(1 for r in recs if has(r, kws)) for t, kws in THEMES.items()}
    all_themes["Climat (nommé)"] = sum(1 for r in recs if r["clim"])
    all_editions = {}
    for ed in ("13H", "20H"):
        sub = [r for r in recs if r.get("edition") == ed]
        if sub:
            c = sum(1 for r in sub if r["clim"])
            all_editions[ed] = {"total": len(sub), "climat": c, "pct": round(100 * c / len(sub), 2)}

    def resolve_all(entries):
        res = []
        for e in entries:
            if e[0] == "ref":
                _, y, kind, prefix = e
                match = next((p for p in out[y][kind] if norm(p["title"]).startswith(norm(prefix))), None)
                if match is None:
                    print(f"WARNING: all-time ref not found: {y}/{kind}/{prefix!r}")
                    continue
                res.append(match)
            else:
                date, media, title, url, why = e
                res.append({"date": date, "media": media, "title": title, "url": url, "why": why})
        return res

    out["all"] = {"media": all_media, "themes": all_themes,
                  "editions": all_editions,
                  "worst": resolve_all(ALL_TIME["worst"]),
                  "best": resolve_all(ALL_TIME["best"])}

    # join the LLM classification of climate reports (see classification-climat.README.md)
    try:
        classif = json.load(open("docs/data-aggregated-news-json/classification-climat.json"))
    except FileNotFoundError:
        classif = []
    by_year_cl = {}
    for c in classif:
        by_year_cl.setdefault(c["date"][:4], []).append(c)

    def classif_agg(rows):
        n = len(rows)
        if not n:
            return None
        cats = {}
        for r in rows:
            if r.get("cat"):
                cats[r["cat"]] = cats.get(r["cat"], 0) + 1
        qui, sec = {}, {}
        aucun_secteur = 0
        for r in rows:
            for q in r.get("qui", []):
                qui[q] = qui.get(q, 0) + 1
            if not r.get("secteurs"):
                aucun_secteur += 1
            for s in r.get("secteurs", []):
                sec[s] = sec.get(s, 0) + 1
        conseq = {}
        for r in rows:
            if r.get("conseq"):
                conseq[r["conseq"]] = conseq.get(r["conseq"], 0) + 1
        fond = cats.get("causes", 0) + cats.get("politique_negociations", 0) + cats.get("science_rapports", 0)
        out_agg = {"n": n, "cats": cats, "fond_pct": round(100 * fond / n, 1),
                   "fossile_pct": round(100 * sum(1 for r in rows if r.get("fossile")) / n, 1),
                   "qui": qui, "secteurs": sec,
                   "aucun_secteur_pct": round(100 * aucun_secteur / n, 1)}
        if conseq:
            out_agg["conseq"] = conseq
        return out_agg

    for y in out:
        rows = classif if y == "all" else by_year_cl.get(y, [])
        agg = classif_agg(rows)
        if agg:
            out[y]["classif"] = agg

    path = "docs/data-aggregated-news-json/yearly.json"
    with open(path, "w") as fp:
        json.dump(out, fp, ensure_ascii=False, indent=1)
    print(f"wrote {path}: {len(out)} years")

if __name__ == "__main__":
    main()
