FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.21_9_1.9.8_3.3.1

COPY . .

CMD ["sbt", "-Dsbt.rootdir=true", "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"]
