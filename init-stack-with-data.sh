#!/bin/bash
set -e

screen -dmS "server test" http-server

# Init values
container_name='tests'

## Create stack
docker-compose -f src/test/docker/docker-compose.yml up -d
sleep 3