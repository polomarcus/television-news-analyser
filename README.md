# [TV news analyser](https://polomarcus.github.io/television-news-analyser/) | https://observatoire.climatmedias.org/ 📺 🔬 🛢️
[![List news urls containing global warming everyday at 5am](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml/badge.svg)](https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml)

[Scrap](https://en.wikipedia.org/wiki/Data_scraping) France 2, France 3, and TF1 Tv news to analyse humanity's biggest challenge : fossil energies and **climate change** and [analyse the data on a website](https://polomarcus.github.io/television-news-analyser/).

![metabaseexample](https://user-images.githubusercontent.com/4059615/149955122-89642ba8-fb45-4369-956c-5854c14bfdd1.png)

**Data source:** HTMLs pages :
* https://www.francetvinfo.fr/replay-jt/france-2/20-heures/jt-de-20h-du-jeudi-30-decembre-2021_4876025.html
* https://www.tf1info.fr/emission/le-20h-11001/extraits/
* https://www.francetvinfo.fr/replay-jt/france-3/19-20/jt-de-19-20-du-vendredi-15-avril-2022_5045866.html

* **JSON data :** ➡️ https://github.com/polomarcus/television-news-analyser/tree/main/data-news-json/

* **Data sink:** JSON data to be stored inside Postgres and displayed on a metabase dashboard (see "Run" on this readme), or [this website](https://observatoire.climatmedias.org/) :

## Can I have a look at the results ?
Every day, last replays with URLs from France 2 and TF1 are analysed with Github Actions if they contain ["global warming"](https://github.com/polomarcus/television-news-analyser/blob/main/src/main/scala/com/github/polomarcus/utils/TextService.scala#L9) :

Some results can be found on this repo's website : https://polomarcus.github.io/television-news-analyser/ | https://observatoire.climatmedias.org/

You can also check GitHub Actions workflows logs : 
1. Click here : https://github.com/polomarcus/television-news-analyser/actions/workflows/save-data.yml
2. Click on the last workflow ran called "Get news from websites", then on "click-here-to-see-data"
3. Click on "List France 2 news urls containing global warming (see end)" to see France 2's urls
4. Click on "List TF1 news urls containing global warming (see end)" to see TF1's urls :

![Urls are listed on the github action workflow](https://user-images.githubusercontent.com/4059615/151147733-3313174a-e2fd-486e-85e7-81272ec0957c.png)
## Requirements
* [docker compose](https://docs.docker.com/compose/install/)
* Optional: if you want to code you have to use Scala build tool (SBT)

## Run
###  Spin up 1 Postgres, Metabase, nginx and load data to PG using SBT
#### Docker Compose without SBT
```
# with docker compose - no need of sbt
docker-compose -f src/test/docker/docker-compose.yml up -d
```

#### With SBT
```
# OR with scala built tool : sbt
./init-stack-with-data.sh
```

#### Init Metabase to explore with SQL
After you ran the project, you can check metabase here http://localhost:3000/
1. configure an account
2. configure PostgreSQL data source: (user/password - host : postgres - database name : metabase) (see docker-compose for details)
3. You're good to go : "Ask a simple question", then select your data source and the "Aa_News" table

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

#### Jupyter Notebook
Some examples are inside [example.ipynb](https://github.com/polomarcus/television-news-analyser/blob/main/example.ipynb), but I preferred to use Metabase dashboard and visualisation using SQL

## Checkout the project website locally (https://observatoire.climatmedias.org/)
Go to http://localhost:8080
The source are inside the `docs` folder

## Test
```
# ./init-stack-with-data.sh
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
