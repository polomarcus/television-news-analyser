fetch( "https://observatoire.climatmedias.org/data-aggregated-news-json/agg.json/agg.json" )
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

    console.log("aggData", aggData)
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

    const FR3Globalwarming = aggData.filter(agg => agg.media == "France 3" && agg.containsWordGlobalWarming).map ( x => {
        return { date: x.datecharts, number_of_news: x.number_of_news }
    })
    const FR3NotGlobalwarming = aggData.filter(agg => agg.media == "France 3" && !agg.containsWordGlobalWarming).map ( x => {
        return { date: x.datecharts, number_of_news: x.number_of_news }
    })

    var newsTF1 = {
      x: TF1NotGlobalwarming.map ( x => x.date),
      y: TF1NotGlobalwarming.map ( x => x.number_of_news),
      type: 'lines',
      mode: 'solid',
      name: 'TF1',
      line: {
        color: 'blue',
        width: 3,
       shape: 'spline'
      }
    };
    var newsTF1Globalwarming = {
      x: TF1Globalwarming.map ( x => x.date),
      y: TF1Globalwarming.map ( x => x.number_of_news),
      type: 'lines',
        mode: 'solid',
        name: 'TF1 Climat',
        line: {
          color: 'purple',
          width: 1,
       shape: 'spline'
        }
    };

    var newsFR2 = {
      x: FR2NotGlobalwarming.map ( x => x.date),
      y: FR2NotGlobalwarming.map ( x => x.number_of_news),
      type: 'lines',
     mode: 'solid',
     name: 'France 2',
     line: {
       color: 'red',
       width: 3,
       shape: 'spline'
     }

    };
    var newsFR2Globalwarming = {
      x: FR2Globalwarming.map ( x => x.date),
      y: FR2Globalwarming.map ( x => x.number_of_news),
      type: 'lines',
     mode: 'solid',
     name: 'FR2 Climat',
     line: {
       color: 'green',
       width: 1,
       shape: 'spline'
     }
    };
    var newsFR3 = {
      x: FR3NotGlobalwarming.map ( x => x.date),
      y: FR3NotGlobalwarming.map ( x => x.number_of_news),
      type: 'lines',
     mode: 'solid',
     name: 'France 3',
     line: {
       color: '#42b6f5',
       width: 3,
       shape: 'spline'
     }

    };
    var newsFR3Globalwarming = {
      x: FR3Globalwarming.map ( x => x.date),
      y: FR3Globalwarming.map ( x => x.number_of_news),
      type: 'lines',
     mode: 'solid',
     name: 'FR3 Climat',
     line: {
       color: 'orange',
       width: 1,
       shape: 'spline'
     }
    };

    var data = [newsTF1, newsFR2, newsFR3, newsTF1Globalwarming, newsFR2Globalwarming, newsFR3Globalwarming];
    var dataOnlyGlobalWarming = [newsTF1Globalwarming, newsFR2Globalwarming, newsFR3Globalwarming];
    var layout = {
      title: 'Nombre de reportages par mois',
    };

    var layoutGlobalWarming = {
      title: 'Nombre de reportage parlant seulement des changements climatiques par mois',
    };
    var config = {responsive: true}
    Plotly.newPlot('newsByMonth', data, layout, config);

    // Plotly.newPlot('newsGlobalwarmingOnlyByMonth', dataOnlyGlobalWarming, layoutGlobalWarming, config);
});
