FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.21_9_1.9.8_3.3.1

WORKDIR /app
RUN mkdir /app/.sbt

COPY . .

EXPOSE 4040

CMD ["sbt", "--batch", "-Dsbt.server.forcestart=true", "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"]
# CMD ["sbt", "--batch", "-Dsbt.server.forcestart=true", "runMain com.github.polomarcus.main.TelevisionNewsAnalyser 2 tf1"]