# Usage: make start profiles=local
PROFILES=$(profiles)

ifndef profiles
PROFILES=dev
endif

ifeq ($(filter $(PROFILES), dev prod local),)
$(error Invalid environment variable: $(profiles))
endif

.PHONY: build stop clean

APP_COMPOSE_FILE := "src/main/docker/app.yml"

start-test-db:
	@(docker-compose -f $(APP_COMPOSE_FILE) up -d db_test)

run-test: start-test-db
	@(./gradlew test);

clean:
	@(docker-compose -f $(APP_COMPOSE_FILE) down --remove-orphan)

stop: clean

build:
	$(info Make: Building $(PROFILES) environment images.)
	@(./gradlew -PspringProfiles=$(PROFILES) clean bootJar jibDockerBuild)

run-start: build
	@(docker-compose -f $(APP_COMPOSE_FILE) up 1wa-app)

test: clean
	bash -c "trap 'docker-compose -f $(APP_COMPOSE_FILE) down' EXIT; $(MAKE) run-test"

start: clean
	bash -c "trap 'docker-compose -f $(APP_COMPOSE_FILE) down' EXIT; $(MAKE) run-start"
