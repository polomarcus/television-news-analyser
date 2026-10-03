// --- Catastrophe sans le climat -----------------------------------------
// Quand un JT montre un incendie, une inondation, une canicule…, le changement
// climatique est-il nommé ? Trois cas : dans le sujet lui-même, ailleurs dans la
// même édition, ou jamais.
//
// France 2 + France 3 seulement : leur champ urlTvNews identifie le JT du jour
// (11,5 sujets par édition). Pour TF1 ce champ est la page de pagination
// (380 valeurs pour 54 980 sujets), l'édition n'est donc pas reconstituable.
//
// `exemple` : un sujet de cette famille dont le JT entier ne nomme jamais le
// climat — vérifié édition par édition. Pour en régénérer une liste :
//   python3 scripts/catastrophes_sans_climat.py --exemples
// Chiffres : scripts/catastrophes_sans_climat.py (2019 → 2 octobre 2026).
const CATASTROPHES = [
  // Plotly dessine la première ligne en bas : l'ordre est inversé à l'affichage.
  { label: 'Canicule, forte chaleur', n: 1810, sujet: 15.1, edition: 45.7,
    exemple: { titre: '« C’est compliqué à vivre, on subit » : une nouvelle semaine éprouvante de canicule',
      date: 'France 2, 10 août 2026',
      url: 'https://www.francetvinfo.fr/environnement/evenements-meteorologiques-extremes/vagues-de-chaleur-canicules/c-est-complique-a-vivre-on-subit-une-nouvelle-semaine-eprouvante-de-canicule_8142341.html' } },
  { label: 'Sécheresse', n: 1623, sujet: 21.8, edition: 41.4,
    exemple: { titre: 'Cours d’eau à sec, canicules, incendies… 98 départements en alerte sécheresse',
      date: 'France 2, 19 juillet 2026',
      url: 'https://www.francetvinfo.fr/environnement/evenements-meteorologiques-extremes/secheresse/cours-d-eau-a-sec-canicules-incendies-98-departements-en-alerte-secheresse_8114384.html' } },
  { label: 'Inondation, crue', n: 2058, sujet: 7.9, edition: 33.5,
    exemple: { titre: 'Les intempéries et les orages ont fait deux morts en Haute-Vienne',
      date: 'France 2, 17 juillet 2026',
      url: 'https://www.francetvinfo.fr/replay-jt/france-2/13-heures/les-intemperies-et-les-orages-ont-fait-deux-morts-en-haute-vienne-a-la-maison_8111666.html' } },
  { label: 'Tempête, cyclone', n: 1988, sujet: 8.8, edition: 29.1,
    exemple: { titre: 'Orages : 93 000 éclairs en une nuit, de lourds dégâts dans plusieurs régions',
      date: 'France 2, 16 juillet 2026',
      url: 'https://www.francetvinfo.fr/environnement/evenements-meteorologiques-extremes/orages/orages-93-000-eclairs-en-une-nuit-de-lourds-degats-dans-plusieurs-regions_8110253.html' } },
  { label: 'Incendie, feux de forêt', n: 4249, sujet: 4.2, edition: 29.1,
    exemple: { titre: '« Ce n’est même pas descriptible » : la Belgique face au pire incendie de son histoire récente',
      date: 'France 2, 13h du 16 août 2026 — ce JT contenait un second incendie, 1 700 ha dans les Landes',
      url: 'https://www.francetvinfo.fr/environnement/evenements-meteorologiques-extremes/incendies-et-feux-de-foret/ce-n-est-meme-pas-descriptible-la-belgique-face-au-pire-incendie-de-son-histoire-recente_8149859.html' } },
  { label: 'Tous événements confondus', n: 10660, sujet: 8.0, edition: 32.7 },
];

(function drawCatastrophes() {
  const el = document.getElementById('catastrophesSansClimat');
  if (!el || typeof Plotly === 'undefined') return;

  const pct = n => n.toFixed(1).replace('.', ',');
  const wrap = (s, w) => {            // coupe un titre long en lignes pour le survol
    const out = []; let line = '';
    s.split(' ').forEach(mot => {
      if ((line + ' ' + mot).trim().length > w) { out.push(line.trim()); line = mot; }
      else { line += ' ' + mot; }
    });
    if (line.trim()) out.push(line.trim());
    return out.join('<br>');
  };

  const ys = CATASTROPHES.map(c => c.label);
  // Le nom de la famille est le lien vers son exemple : pas de pavé de texte sous le graphe.
  const ticks = CATASTROPHES.map(c => c.exemple
    ? `<a href="${c.exemple.url}" target="_blank" rel="noopener">${c.label}</a>`
    : `<b>${c.label}</b>`);
  const dans     = CATASTROPHES.map(c => c.sujet);
  const ailleurs = CATASTROPHES.map(c => +(c.edition - c.sujet).toFixed(1));
  const jamais   = CATASTROPHES.map(c => +(100 - c.edition).toFixed(1));

  const seg = (name, vals, color, textColor, withExample) => ({
    type: 'bar', orientation: 'h', name,
    x: vals, y: ys,
    marker: { color },
    text: vals.map(v => (v >= 4 ? pct(v) + ' %' : '')),
    textposition: 'inside',
    insidetextanchor: 'middle',
    textfont: { color: textColor, size: 11 },
    customdata: CATASTROPHES.map(c => {
      const base = `${c.n.toLocaleString('fr-FR')} sujets`;
      if (!withExample || !c.exemple) return base;
      return `${base}<br><br><i>Exemple où le climat n’est nommé nulle part dans le JT :</i><br>`
           + `${wrap(c.exemple.titre, 58)}<br><i>${wrap(c.exemple.date, 58)}</i>`
           + '<br><i>(cliquez le nom de la famille pour l’ouvrir)</i>';
    }),
    hovertemplate: `<b>%{y}</b> — ${name} : %{x}<br>%{customdata}<extra></extra>`   // %{x} porte deja le suffixe de l axe
  });

  const data = [
    seg('nommé dans le sujet',            dans,     '#1a7f45', '#fff',    false),
    seg('nommé ailleurs dans le même JT', ailleurs, '#a8d5ba', '#1a3d28', false),
    seg('jamais nommé',                   jamais,   '#e6e8ec', '#555',    true)
  ];

  const layout = {
    title: {
      text: 'Quand le JT montre une catastrophe, nomme-t-il le climat ?',
      font: { size: 16 }
    },
    separators: ', ',   // virgule decimale, espace pour les milliers
    barmode: 'stack',
    bargap: 0.35,
    xaxis: { ticksuffix: ' %', range: [0, 100], fixedrange: true },
    yaxis: { automargin: true, fixedrange: true, tickmode: 'array', tickvals: ys, ticktext: ticks },
    hoverlabel: { align: 'left', bgcolor: '#fff', bordercolor: '#c9ccd4',
                  font: { size: 11, color: '#222' } },
    legend: { orientation: 'h', traceorder: 'normal', y: -0.18, x: 0, font: { size: 11 } },
    margin: { l: 10, r: 20, t: 50, b: 150 },
    height: 420,
    annotations: [{
      xref: 'paper', yref: 'paper', x: 0, y: -0.26,
      xanchor: 'left', yanchor: 'top', showarrow: false,
      font: { size: 11, color: '#6b7078' },
      text: 'France 2 et France 3, 2019 → 2 octobre 2026 · 10 660 sujets d’événement extrême'
          + '<br>L’édition est le JT du jour entier : « jamais » signifie que le mot n’a été'
          + ' prononcé dans aucun sujet de ce JT.'
          + '<br>Les cinq familles se recoupent — 1 068 sujets citent deux événements —'
          + ' elles ne s’additionnent donc pas à la première ligne.'
          + '<br><b>Survolez la barre grise</b> pour un exemple, <b>cliquez le nom d’une famille</b>'
          + ' pour ouvrir le reportage.'
    }]
  };

  Plotly.newPlot(el, data, layout, { responsive: true, displayModeBar: false });
})();
