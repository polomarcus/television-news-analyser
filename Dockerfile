FROM sbtscala/scala-sbt:graalvm-ce-22.3.3-b1-java11_1.9.7_3.3.1

COPY . .

CMD ["sbt", "-Dsbt.rootdir=true", "runMain com.github.polomarcus.main.SaveTVNewsToPostgres filter"]