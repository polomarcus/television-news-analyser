#!/bin/bash
set -e

# Init values
container_name='tests'

## Create stack
docker-compose -f src/test/docker/docker-compose.yml up -d
sleep 3

# Insert data from 2013 to 2021 to PG
echo 'Insert data from 2013 to 2021 to PG'
sbt "runMain com.github.polomarcus.main.SaveTVNewsToPostgres"