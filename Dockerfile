FROM mozilla/sbt:8u292_1.5.7

COPY . .

CMD ["sbt", "-Dsbt.rootdir=true", "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"]