fetch( "https://polomarcus.github.io/television-news-analyser/data-aggregated-news-json/latest-news.json/latestNews.json" )
   .then(async r=> {
    const rawData = await r.text();
    const parsedData = '[' + rawData.split("\n{").join(',{') + ']'
    const latestNews = JSON.parse(parsedData);
    console.log("latestNews" , latestNews)

    //create Tabulator on DOM element with id "example-table"
    var table = new Tabulator("#example-table", {
       // height:205, // set height of table (in CSS or here), this enables the Virtual DOM and improves render speed dramatically (can be any valid css height value)
        data: latestNews, //assign data to table
        layout: "fitDataStretch",
        paginationSize:30,
        pagination:true, //enable.
        columns:[ //Define Table Columns
            {title:"Date", field:"date", sorter:"date"},
            {title:"Media", field:"media", formatter:"plaintext"},
            {title:"Titre", field:"url",  formatter:"link",formatterParams:{labelField:"title",target:"_blank"}},
        ],
    });

    var setupFilter = function(fieldName) {
                var filterEl = document.getElementById(fieldName + "-filter-value");
                // Trigger setFilter function with correct parameters
                function updateFilter(){
                        table.setFilter(fieldName,'like', filterEl.value.toLowerCase());
                }
                filterEl.addEventListener("keyup", updateFilter);
            };
        setupFilter("title");
});
