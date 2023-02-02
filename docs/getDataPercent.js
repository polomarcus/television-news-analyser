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
          title: 'Par mois'
       },
       yaxis: {
          title: '% de reportage'
       },
    };
    var config = {responsive: true}

    Plotly.newPlot('newsGlobalwarmingOnlyByMonthPercent', data, layout, config);
});
