#!/bin/sh

# to build for multiple platforms see https://docs.docker.com/build/building/multi-platform/#getting-started
# specifically docker buildx create --name mybuilder --driver docker-container --bootstrap --use
docker buildx build --platform linux/amd64,linux/arm64 -t mtomady/hapi --push .

