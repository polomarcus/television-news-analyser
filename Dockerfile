FROM mozilla/sbt

COPY . .

CMD ["sbt", "-Dsbt.rootdir=true", "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"]