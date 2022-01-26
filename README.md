# TV news analyser 📺 🔬 🛢️
[![List news urls containing global warming everyday at 5am](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml/badge.svg)](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml)
Scrap France 2 Tv news to analyse humanity's biggest challenge : fossil energies and **climate change**.

![metabaseexample](https://user-images.githubusercontent.com/4059615/149955122-89642ba8-fb45-4369-956c-5854c14bfdd1.png)

**Data source:** HTMLs pages : example https://www.francetvinfo.fr/replay-jt/france-2/20-heures/jt-de-20h-du-jeudi-30-decembre-2021_4876025.html

**Data sink:** JSON data to be store inside MySQL and displayed on a metabase dashboard :

* **JSON data :** ➡️ https://github.com/polomarcus/television-news-analyser/tree/main/data-news-json/

Everyday last replays with URLs from France 2 and TF1 are analysed with Github Actions if they contain "global warming" :
* https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml

## Requirements
* scala built tool : sbt
* [docker compose](https://docs.docker.com/compose/install/)

## Run
###  Spin up 1 Postgres, Metabase, and load data to PG via SBT
```
./init-stack-with-data.sh
```

You can check metabase here
* http://localhost:3000/
* configure an account
* configure PostgreSQL data source: (user/password - host : postgres - database name : metabase)
* You're good to go : "Ask a simple question", then select your data source and the "News" table

#### To scrap data from 3 pages from France 2 website
```
sbt "runMain com.github.polomarcus.main.TelevisionNewsAnalyser 3"
```

#### To store the JSON data to PG and explore it with Metabase 
```
sbt "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"
```

#### Jupyter Notebook
Some examples are inside [example.ipynb](https://github.com/polomarcus/television-news-analyser/blob/main/example.ipynb), but I prefered to use Metabase dashboard and visualisation using SQL

## Test
```
# ./init-stack-with-data.sh
sbt test # it will parsed some localhost pages from test/resources/
```

### Test only one method
```
sbt> testOnly ParserTest -- -z parseFrance2Home
```

## Libraries documentation
* https://github.com/ruippeixotog/scala-scraper
* https://circe.github.io/circe/parsing.html
* [Have multiple threads to handle future](http://stackoverflow.com/questions/15285284/how-to-configure-a-fine-tuned-thread-pool-for-futures)
