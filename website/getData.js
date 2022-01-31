fetch( "https://polomarcus.github.io/television-news-analyser/website/data-aggregated-news-json/latest-news.json/latestNews.json" )
   .then(async r=> {
    const rawData = await r.text();
    const parsedData = '[' + rawData.split("\n{").join(',{') + ']'
    console.log("parsedData" , parsedData)
    const latestNews = JSON.parse(parsedData);
    console.log("latestNews" , latestNews)

    //create Tabulator on DOM element with id "example-table"
    var table = new Tabulator("#example-table", {
       // height:205, // set height of table (in CSS or here), this enables the Virtual DOM and improves render speed dramatically (can be any valid css height value)
        data:latestNews, //assign data to table
        layout:"fitColumns", //fit columns to width of table (optional)
        columns:[ //Define Table Columns
            {title:"Date", field:"date", sorter:"date"},
            {title:"Media", field:"media", formatter:"plaintext"},
            {title:"Titre", field:"url",  formatter:"link",formatterParams:{labelField:"title",target:"_blank"}},
        ],
    });
});
