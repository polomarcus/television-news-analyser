fetch( "https://polomarcus.github.io/television-news-analyser/website/data-aggregated-news-json/aggPercent.json/aggPercent.json" )
   .then(async r=> {
    const rawData = await r.text();
    const parsedData = '[' + rawData.split("\n{").join(',{') + ']'
    const aggData = JSON.parse(parsedData);

    const TF1GlobalwarmingPercent = aggData.filter(agg => agg.media == "TF1").map ( x => {
        return { date: x.date, percent: x.percent }
    })

    const FR2GlobalwarmingPercent = aggData.filter(agg => agg.media == "France 2").map ( x => {
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
        width: 2
      }
    };

    var newsFR2 = {
      x: FR2GlobalwarmingPercent.map ( x => x.date),
      y: FR2GlobalwarmingPercent.map ( x => x.percent),
      type: 'lines',
     mode: 'solid',
     name: 'France 2',
     line: {
       color: 'red',
       width: 2
     }
    };

    console.log("FR2GlobalwarmingPercent", FR2GlobalwarmingPercent)
    var data = [newsTF1, newsFR2];
    var layout = {
      title: '% de reportage par mois parlant des changements climatiques',
    };
    var config = {responsive: true}

    Plotly.newPlot('newsGlobalwarmingOnlyByMonthPercent', data, layout, config);
});
