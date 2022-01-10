#!/bin/bash
set -e

# Init values
container_name='tests'

## Create stack
docker-compose -f src/test/docker/docker-compose.yml up -d
sleep 3