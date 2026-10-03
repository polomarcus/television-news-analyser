// --- Repères d'actualité -------------------------------------------------
// Mois où un marronnier a écrasé le changement climatique à l'antenne.
// pct    : % des sujets du mois consacrés à l'événement (TF1 + France 2 + France 3)
// climat : % des sujets du même mois parlant de changement climatique
// Mesuré sur data-news-json, lexiques dans scripts/reperes_actualite.py
const REPERES = [
  { date: '2019-07', label: 'Tour de France',        pct: 4.7,  climat: 1.9, ratio: 2.4 },
  { date: '2019-09', label: 'Mort de Chirac',        pct: 10.3, climat: 2.9, ratio: 3.6 },
  { date: '2020-08', label: 'Rentr\u00e9e scolaire',     pct: 3.8,  climat: 1.2, ratio: 3.1 },
  { date: '2021-12', label: 'No\u00ebl',                 pct: 11.7, climat: 1.0, ratio: 12.2 },
  { date: '2022-09', label: 'Mort d\u2019Elizabeth II',  pct: 13.4, climat: 2.2, ratio: 6.2 },
  { date: '2022-12', label: 'Coupe du monde',        pct: 8.6,  climat: 1.1, ratio: 8.2 },
  { date: '2024-08', label: 'JO de Paris',           pct: 18.7, climat: 0.9, ratio: 20.3 },
  { date: '2025-04', label: 'Mort du pape',          pct: 6.7,  climat: 1.5, ratio: 4.6 },
];

const fr1 = n => n.toFixed(1).replace('.', ',');
const ratioTxt = r => (r < 10 ? fr1(r) : r.toFixed(0));

// Deux rangées en quinconce : les repères proches ne se chevauchent pas.
const ROWS = [-0.17, -0.29];

const repereShapes = REPERES.map(r => ({
  type: 'line', xref: 'x', yref: 'paper',
  x0: r.date, x1: r.date, y0: 0, y1: 1,
  line: { color: '#c9ccd4', width: 1, dash: 'dot' },
  layer: 'below'
}));

const repereAnnotations = REPERES.map((r, i) => ({
  xref: 'x', yref: 'paper',
  x: r.date, y: ROWS[i % 2],
  xanchor: 'center', yanchor: 'top',
  showarrow: false,
  align: 'center',
  text: `<b>${r.label}</b><br><span style="color:#8a8f98">\u00d7${ratioTxt(r.ratio)} le climat</span>`,
  font: { size: 10, color: '#444' },
  hovertext: `${r.date} \u2014 ${r.label} : ${fr1(r.pct)} % des sujets du mois,`
           + ` contre ${fr1(r.climat)} % pour le changement climatique.`,
  captureevents: true
}));

fetch( "https://observatoire.climatmedias.org/data-aggregated-news-json/aggPercent.json/aggPercent.json" )
   .then(async r=> {
    const rawData = await r.text();
    const parsedData = '[' + rawData.split("\n{").join(',{') + ']'
    const aggDataTmp = JSON.parse(parsedData);

    const aggData = aggDataTmp.filter(agg => !agg.date.includes("2013") &&
        !agg.date.includes("2014") &&
        !agg.date.includes("2015") &&
        !agg.date.includes("2016") &&
        !agg.date.includes("2017") &&
        !agg.date.includes("2018")
    )

    const TF1GlobalwarmingPercent = aggData.filter(agg => agg.media == "TF1").map ( x => {
        return { date: x.date, percent: x.percent }
    })

    const FR2GlobalwarmingPercent = aggData.filter(agg => agg.media == "France 2").map ( x => {
        return { date: x.date, percent: x.percent }
    })

    const FR3GlobalwarmingPercent = aggData.filter(agg => agg.media == "France 3").map ( x => {
        return { date: x.date, percent: x.percent }
    })

    var newsTF1 = {
      x: TF1GlobalwarmingPercent.map ( x => x.date),
      y: TF1GlobalwarmingPercent.map ( x => x.percent),
      type: 'lines',
      mode: 'solid',
      name: 'TF1',
      line: {
        color: 'blue',
        width: 2,
        shape: 'spline'
      }
    };

    var newsFR2 = {
      x: FR2GlobalwarmingPercent.map ( x => x.date),
      y: FR2GlobalwarmingPercent.map ( x => x.percent),
      type: 'lines',
     mode: 'solid',
     name: 'FR2',
     line: {
       color: 'red',
       width: 2,
       shape: 'spline'
     }
    };

    var newsFR3 = {
      x: FR3GlobalwarmingPercent.map ( x => x.date),
      y: FR3GlobalwarmingPercent.map ( x => x.percent),
      type: 'lines',
     mode: 'solid',
     name: 'FR3',
     line: {
       color: '#42b6f5',
       width: 2,
       shape: 'spline'
     }
    };

    console.log("FR2GlobalwarmingPercent", FR2GlobalwarmingPercent)
    var data = [newsTF1, newsFR2, newsFR3];
    var layout = {
      title: 'Reportage sur le changement climatique',
       xaxis: { // all "layout.xaxis" attributes: #layout-xaxis
          title: { text: 'Par mois', standoff: 8 }
       },
       yaxis: {
          title: '% de reportage'
       },
       shapes: [],
       annotations: [],
       updatemenus: [{
         type: 'buttons',
         direction: 'left',
         showactive: true,
         x: 1, xanchor: 'right',
         y: 1.12, yanchor: 'bottom',
         pad: { r: 4, t: 4, b: 4, l: 4 },
         bgcolor: '#fff',
         bordercolor: '#c9ccd4',
         font: { size: 11 },
         buttons: [{
           label: "Repères d'actualité",
           method: 'relayout',
           // The bottom margin grows only while the repères are shown, so the
           // default view keeps exactly the height it had before.
           args:  [{ shapes: repereShapes, annotations: repereAnnotations,
                      'margin.b': 150, 'xaxis.title.text': '' }],
           args2: [{ shapes: [],           annotations: [],
                      'margin.b': 80,  'xaxis.title.text': 'Par mois' }]
         }]
       }]
    };
    var config = {responsive: true}

    Plotly.newPlot('newsGlobalwarmingOnlyByMonthPercent', data, layout, config);
});
