fetch( "https://polomarcus.github.io/television-news-analyser/website/data-aggregated-news-json/agg.json/agg.json" )
   .then(async r=> {
    const rawData = await r.text();
    const parsedData = '[' + rawData.split("\n{").join(',{') + ']'
    const aggData = JSON.parse(parsedData);

    const TF1Globalwarming = aggData.filter(agg => agg.media == "TF1" && agg.containsWordGlobalWarming).map ( x => {
        return { date: x.datecharts, number_of_news: x.number_of_news }
    })
    const TF1NotGlobalwarming = aggData.filter(agg => agg.media == "TF1" && !agg.containsWordGlobalWarming).map ( x => {
        return { date: x.datecharts, number_of_news: x.number_of_news }
    })

    const FR2Globalwarming = aggData.filter(agg => agg.media == "France 2" && agg.containsWordGlobalWarming).map ( x => {
        return { date: x.datecharts, number_of_news: x.number_of_news }
    })
    const FR2NotGlobalwarming = aggData.filter(agg => agg.media == "France 2" && !agg.containsWordGlobalWarming).map ( x => {
        return { date: x.datecharts, number_of_news: x.number_of_news }
    })

    var newsTF1 = {
      x: TF1NotGlobalwarming.map ( x => x.date),
      y: TF1NotGlobalwarming.map ( x => x.number_of_news),
      type: 'scatter',
      mode: 'lines',
      name: 'TF1',
      line: {
        color: 'blue',
        width: 1
      }
    };
    var newsTF1Globalwarming = {
      x: TF1Globalwarming.map ( x => x.date),
      y: TF1Globalwarming.map ( x => x.number_of_news),
      type: 'scatter',
        mode: 'lines',
        name: 'TF1 Changement climatique',
        line: {
          color: 'purple',
          width: 1
        }
    };

    var newsFR2 = {
      x: FR2NotGlobalwarming.map ( x => x.date),
      y: FR2NotGlobalwarming.map ( x => x.number_of_news),
      type: 'scatter',
     mode: 'lines',
     name: 'France 2',
     line: {
       color: 'red',
       width: 1
     }

    };
    var newsFR2Globalwarming = {
      x: FR2Globalwarming.map ( x => x.date),
      y: FR2Globalwarming.map ( x => x.number_of_news),
      type: 'scatter',
     mode: 'lines',
     name: 'France 2 Changement climatique',
     line: {
       color: 'green',
       width: 1
     }
    };

    console.log("newsFR2Globalwarming", newsFR2Globalwarming)
    var data = [newsTF1, newsFR2, newsTF1Globalwarming, newsFR2Globalwarming];
    var dataOnlyGlobalWarming = [newsTF1Globalwarming, newsFR2Globalwarming];
    var layout = {
      title: 'Nombre de reportages par mois',
    };

    var layoutGlobalWarming = {
      title: 'Nombre de reportage parlant seulement des changements climatiques par mois',
    };
    var config = {responsive: true}
    Plotly.newPlot('newsByMonth', data, layout, config);

    Plotly.newPlot('newsGlobalwarmingOnlyByMonth', dataOnlyGlobalWarming, layoutGlobalWarming, config);
});
