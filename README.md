# [TV news analyser](https://polomarcus.github.io/television-news-analyser/) | https://observatoire.climatmedias.org/ 📺 🔬 🛢️
[![List news urls containing global warming everyday at 5am](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml/badge.svg)](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml)

[Scrap](https://en.wikipedia.org/wiki/Data_scraping) France 2, France 3, and TF1 Tv news to analyse humanity's biggest challenge : fossil energies and **climate change** and [analyse the data on a website](https://polomarcus.github.io/television-news-analyser/).

![metabaseexample](https://user-images.githubusercontent.com/4059615/203794161-12fa4267-252f-41a5-af26-d0cad55eceed.png)

### Data sources - HTMLs pages :
* TF1 : https://www.tf1info.fr/emission/le-20h-11001/extraits/
* France 2 : https://www.francetvinfo.fr/replay-jt/france-2/20-heures/jt-de-20h-du-jeudi-30-decembre-2021_4876025.html
* France 3 : https://www.francetvinfo.fr/replay-jt/france-3/19-20/jt-de-19-20-du-vendredi-15-avril-2022_5045866.html

### Data sinks
* JSON ➡️ https://github.com/polomarcus/television-news-analyser/tree/main/data-news-json/
* CSV compressed ([if you don't know how to uncompressed these data](https://www.wikihow.com/Extract-a-Gz-File)) ➡️️ https://github.com/polomarcus/television-news-analyser/tree/main/data-news-csv/

JSON data can be stored inside Postgres and displayed on a Metabase dashboard (read "Run" on this readme), or can be found on [this website](https://observatoire.climatmedias.org/) :

## Run

### Requirements
* [docker compose](https://docs.docker.com/compose/install/)
* Optional: if you want to code you have to use Scala build tool (SBT)

###  Spin up 1 Postgres, Metabase, nginx and load data to PG using SBT
#### Docker Compose without SBT (Scala build tool)
```
# with docker compose - no need of sbt
./init-stack-with-data.sh
# this script does this : docker-compose -f src/test/docker/docker-compose.yml up -d --build app
```

#### Init Metabase to explore with SQL
After you ran the project with docker compose, you can check metabase here http://localhost:3000 with a few steps :
1. configure an account
2. configure PostgreSQL data source: (user/password - host : postgres - database name : metabase) (see docker-compose for details)
3. You're good to go : "Ask a simple question", then select your data source and the "Aa_News" table

#### Jupyter Notebook
Some examples are inside [example.ipynb](https://github.com/polomarcus/television-news-analyser/blob/main/example.ipynb), but I preferred to use Metabase dashboard and visualisation using SQL

### To scrap data from 3 pages from France 2 website
```
sbt "runMain com.github.polomarcus.main.TelevisionNewsAnalyser 3"
```

### To store the JSON data to PG and explore it with Metabase
```
sbt "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"
```

### To update data for the website alone
```
sbt "runMain com.github.polomarcus.main.UpdateNews"
```

## How does it run automatically every day ?
Last replays France 2, 3 and TF1 are scrapped with [a GitHub Action](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml), then this news are stored inside [this folder](️https://github.com/polomarcus/television-news-analyser/tree/main/data-news-json/) partitioned by media and by date.

If news title or description contains a ["global warming key word"](https://github.com/polomarcus/television-news-analyser/blob/main/src/main/scala/com/github/polomarcus/utils/TextService.scala#L9) : they are marked as such with `containsWordGlobalWarming: Boolean`.

Some results can be found on this repo's website : https://polomarcus.github.io/television-news-analyser/ | https://observatoire.climatmedias.org/

### To check the GitHub Action
1. Click here : https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml
2. Click on the last workflow ran called "Get news from websites", then on "click-here-to-see-data"
3. Click on "List France 2 news urls containing global warming (see end)" to see France 2's urls
4. Click on "List TF1 news urls containing global warming (see end)" to see TF1's urls :

![Urls are listed on the github action workflow](https://user-images.githubusercontent.com/4059615/151147733-3313174a-e2fd-486e-85e7-81272ec0957c.png)

## Checkout the project website locally (https://observatoire.climatmedias.org/)
Go to http://localhost:8080
The source are inside the `docs` folder

## Test
```
# first, be sure to have docker compose up with ./init-stack-with-data.sh
sbt test # it will parsed some localhost pages from test/resources/
```

### Test only one method
```
sbt> testOnly ParserTest -- -z parseFranceTelevisionHome
```

## Libraries documentation
* https://github.com/ruippeixotog/scala-scraper
* https://circe.github.io/circe/parsing.html
* [Have multiple threads to handle future](http://stackoverflow.com/questions/15285284/how-to-configure-a-fine-tuned-thread-pool-for-futures)
