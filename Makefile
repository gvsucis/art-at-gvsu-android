SHELL:=/usr/bin/env bash

FASTLANE ?= bundle exec fastlane
SECRETS_DIR = ../art-at-gvsu-secrets/android

.PHONY: ci-test ci-secrets release-secrets production-secrets deploy-beta deploy-production secrets

.PHONY: py-deps
py-deps:
	pip install bumpver==2023.1129

.PHONY: deps
deps: py-deps
	bundle install

.PHONY: changelog
changelog:
	./scripts/changelog

test:
	$(FASTLANE) test

secrets:
	cp -v $(SECRETS_DIR)/android-firebase-secret-key.json ./android-firebase-secret-key.json
	cp -v $(SECRETS_DIR)/secrets.properties ./secrets.properties
	cp -v $(SECRETS_DIR)/project.properties ./project.properties
	cp -v $(SECRETS_DIR)/app/google-services.json ./app/google-services.json
	cp -v $(SECRETS_DIR)/release.keystore ./release.keystore
	cp -v $(SECRETS_DIR)/app/src/release/res/values/google_analytics_api.xml ./app/src/release/res/values/google_analytics_api.xml
	cp -v $(SECRETS_DIR)/app/src/release/res/values/google_maps_api.xml ./app/src/release/res/values/google_maps_api.xml

ci-test: ci-secrets
	$(FASTLANE) test

deploy-beta: ci-secrets release-secrets
	$(FASTLANE) beta

deploy-production: ci-secrets release-secrets production-secrets
	$(FASTLANE) production

.SILENT:
production-secrets:
	echo ${ENCODED_GOOGLE_PLAY_CREDENTIALS} | base64 --decode > ./google-play-service-account.json

.SILENT:
release-secrets:
	echo ${ENCODED_GOOGLE_APPLICATION_CREDENTIALS} | base64 --decode > ./android-firebase-secret-key.json
	echo ${ENCODED_RELEASE_KEYSTORE} | base64 --decode > ./release.keystore

.SILENT:
ci-secrets:
	echo ${ENCODED_GOOGLE_ANALYTICS_KEY} | base64 --decode > ./app/src/release/res/values/google_analytics_api.xml
	echo ${ENCODED_GOOGLE_MAPS_KEY} | base64 --decode > ./app/src/release/res/values/google_analytics_api.xml
	echo ${ENCODED_GOOGLE_SERVICES} | base64 --decode > ./app/google-services.json
	echo ${ENCODED_PROJECT_PROPERTIES} | base64 --decode > ./project.properties
	echo ${ENCODED_SECRETS_PROPERTIES} | base64 --decode > ./secrets.properties
