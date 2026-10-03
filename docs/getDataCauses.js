// --- Quelle cause les sujets climat nomment-ils ? ------------------------
// Illustre le propos de Jean-Baptiste Comby : la catégorie « activités humaines »
// n'ouvre pas de discussion sur les logiques productives, industrielles ou
// financières.
//
// Chaque sujet est classé dans UN SEUL registre, le plus exigeant d'abord : un
// sujet qui cite à la fois un industriel et le CO2 compte comme « acteur
// industriel ». Les parts totalisent donc 100 %, et 1,7 % est un plafond, pas
// un plancher — impossible de nous reprocher d'avoir sous-compté.
//
// `exemple` : un sujet représentatif du registre, cliquable depuis l'axe.
// Lexiques, règle de priorité et calcul : scripts/causes_nommees.py · 2019 → 2 octobre 2026.
const CAUSES_REGISTRES = [
  // Plotly dessine la première ligne en bas : l'ordre est inversé à l'affichage.
  { label: '« Les activités humaines », sans plus', pct: 1.6, n: 45,
    exemple: { titre: 'Léopards, renards, ours, ces animaux sauvages qui envahissent les villes',
      date: 'France 2, 24 mars 2026',
      url: 'https://www.francetvinfo.fr/replay-jt/france-2/20-heures/leopards-renards-ours-ces-animaux-sauvages-qui-envahissent-les-villes_7891805.html' } },
  { label: 'Un acteur industriel ou financier', pct: 1.7, n: 49, accent: true,
    exemple: { titre: 'Fruits et légumes emballés, bouteilles… Le plastique reste omniprésent dans les supermarchés',
      date: 'France 2, 5 mai 2026',
      url: 'https://www.francetvinfo.fr/economie/fruits-et-legumes-emballes-bouteilles-le-plastique-reste-omnipresent-dans-les-supermarches_7985456.html' } },
  { label: 'Un geste individuel à faire', pct: 2.3, n: 66,
    exemple: { titre: 'Sécheresse : des astuces pour faire baisser sa facture d’eau',
      date: 'France 2, 8 juillet 2025',
      url: 'https://www.franceinfo.fr/environnement/evenements-meteorologiques-extremes/secheresse/secheresse-des-astuces-pour-faire-baisser-sa-facture-d-eau_7363428.html' } },
  { label: 'Un mécanisme physique (CO2, fossiles)', pct: 10.8, n: 313,
    exemple: { titre: 'Environnement : malgré 50 ans d’alerte, un dérèglement climatique qui s’accentue inexorablement',
      date: 'France 2, 26 juin 2026',
      url: 'https://www.francetvinfo.fr/environnement/evenements-meteorologiques-extremes/vagues-de-chaleur-canicules/environnement-malgre-50-ans-d-alerte-un-dereglement-climatique-qui-s-accentue-inexorablement_8081801.html' } },
  { label: 'Aucune cause, aucun responsable', pct: 83.7, n: 2422,
    exemple: { titre: 'Alpes : les éboulements, un phénomène qui devient plus fréquent à cause du changement climatique',
      date: 'France 2, 14 août 2026 — le climat est donné comme cause de l’éboulement, mais rien ne dit ce qui cause le dérèglement',
      url: 'https://www.francetvinfo.fr/environnement/crise-climatique/alpes-les-eboulements-un-phenomene-qui-devient-plus-frequent-a-cause-du-changement-climatique_8148017.html' } },
];
const CAUSES_TOTAL = 2895;

(function drawCauses() {
  const el = document.getElementById('causesNommees');
  if (!el || typeof Plotly === 'undefined') return;
  const fr = n => n.toFixed(1).replace('.', ',');
  const wrap = (s, w) => {
    const out = []; let line = '';
    s.split(' ').forEach(mot => {
      if ((line + ' ' + mot).trim().length > w) { out.push(line.trim()); line = mot; }
      else { line += ' ' + mot; }
    });
    if (line.trim()) out.push(line.trim());
    return out.join('<br>');
  };

  const ys = CAUSES_REGISTRES.map(c => c.label);
  // Le nom du registre est le lien vers son exemple : aucun pavé de texte sous le graphe.
  const ticks = CAUSES_REGISTRES.map(c =>
    `<a href="${c.exemple.url}" target="_blank" rel="noopener">${c.label}</a>`);

  const data = [{
    type: 'bar', orientation: 'h',
    x: CAUSES_REGISTRES.map(c => c.pct),
    y: ys,
    marker: { color: CAUSES_REGISTRES.map(c => (c.accent ? '#b4352e' : '#c9ccd4')) },
    text: CAUSES_REGISTRES.map(c => fr(c.pct) + ' %'),
    textposition: 'outside',
    textfont: { size: 12 },
    customdata: CAUSES_REGISTRES.map(c =>
      `${c.n.toLocaleString('fr-FR')} sujets sur ${CAUSES_TOTAL.toLocaleString('fr-FR')}`
      + `<br><br><i>Exemple :</i><br>${wrap(c.exemple.titre, 58)}`
      + `<br><i>${wrap(c.exemple.date, 58)}</i>`
      + '<br><i>(cliquez le nom du registre pour l’ouvrir)</i>'),
    hovertemplate: '<b>%{y}</b> : %{x}<br>%{customdata}<extra></extra>'
  }];

  const layout = {
    title: {
      text: 'Huit sujets climat sur dix ne nomment ni cause ni responsable',
      font: { size: 15 }
    },
    separators: ', ',   // virgule décimale, espace pour les milliers
    xaxis: { ticksuffix: ' %', range: [0, 100], fixedrange: true, zeroline: false },
    yaxis: { automargin: true, fixedrange: true, tickmode: 'array', tickvals: ys, ticktext: ticks },
    hoverlabel: { align: 'left', bgcolor: '#fff', bordercolor: '#c9ccd4',
                  font: { size: 11, color: '#222' } },
    margin: { l: 10, r: 40, t: 50, b: 110 },
    height: 345,
    annotations: [{
      xref: 'paper', yref: 'paper', x: 0, y: -0.2,
      xanchor: 'left', yanchor: 'top', showarrow: false,
      font: { size: 11, color: '#6b7078' },
      text: `Les ${CAUSES_TOTAL.toLocaleString('fr-FR')} sujets sur le changement climatique`
          + ' (2019 → 2 octobre 2026), répartis en cinq registres exclusifs : total 100 %.'
          + '<br>Un sujet qui cite plusieurs registres est compté dans le plus exigeant,'
          + ' donc 1,7 % est un maximum.'
          + '<br><b>Survolez une barre</b> pour un exemple, <b>cliquez le nom d’un registre</b>'
          + ' pour ouvrir le reportage.'
    }]
  };

  Plotly.newPlot(el, data, layout, { responsive: true, displayModeBar: false });
})();
