# TV news analyser
Scrap France 2 Tv news to analyse humanity's biggest challenge : fossil energies and climate change.

Data source: HTMLs pages : example https://www.francetvinfo.fr/replay-jt/france-2/20-heures/jt-de-20h-du-jeudi-30-decembre-2021_4876025.html
Data sink: JSON data to be store inside MySQL and displayed on a metabase dashboard

## Requirements
* [docker compose](https://docs.docker.com/compose/install/)
* for test: npm `npm install http-server -g`
* screen, `sudo apt-get install screen`

## Run
###  Spin up 1 Postgres, Metabase
```dtd
./init-stack-with-data.sh
```

You can check metabase here
* http://localhost:3000/#/cluster/default/topic/n/songs/

To scrap data from 3 pages from France 2 website
```
sbt "runMain com.github.polomarcus.main.RadioStationsApp 3"
```

## Test
```
killall screen # sorry if you have already running screen
screen -dmS "server" http-server # server src/test/resources/radionova.html to localhost
sbt test
```

## Libraries documentation
* https://github.com/ruippeixotog/scala-scraper
* https://circe.github.io/circe/parsing.html
* [Have multiple threads to handle future](http://stackoverflow.com/questions/15285284/how-to-configure-a-fine-tuned-thread-pool-for-futures)