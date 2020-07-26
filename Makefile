.PHONY: all build release

REGISTRY := eu.gcr.io/retinue-io
REPOSITORY_NAME := stretech
PREFIX := prod-
COMMIT_HASH := $(shell git rev-parse --short HEAD)
DOCKER_IMAGE := $(REGISTRY)/$(REPOSITORY_NAME):$(PREFIX)$(COMMIT_HASH)
ECR_REGION := us-east-1

all: release

build:
	./gradlew clean build -x test
	docker build --rm --force-rm -t $(DOCKER_IMAGE) .

push:
	docker push $(DOCKER_IMAGE)
	docker tag $(DOCKER_IMAGE) $(REGISTRY)/$(REPOSITORY_NAME):latest
	docker push $(REGISTRY)/$(REPOSITORY_NAME):latest

release: build push