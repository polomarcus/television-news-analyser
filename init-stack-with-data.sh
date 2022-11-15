#!/bin/bash
set -e

## Create stack
docker-compose -f src/test/docker/docker-compose.yml up -d
sleep 3

echo 'You can access Metabase to explore data with SQL : http://localhost:3000'
# Insert data from 2013 to 2021 to PG without Docker-compose
# sbt "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"