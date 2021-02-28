ENV=$(env)
ifndef env
ENV=dev
endif

ifeq ($(filter $(ENV), dev prod),)
$(error Invalid environment variable: $(env))
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
	$(info Make: Building $(ENV) environment images.)
	@(./gradlew -P$(ENV) clean bootJar jibDockerBuild)

run-start: build
	@(docker-compose -f $(APP_COMPOSE_FILE) up 1wa-app)

test: clean
	bash -c "trap 'docker-compose -f $(APP_COMPOSE_FILE) down' EXIT; $(MAKE) run-test"

start: clean
	bash -c "trap 'docker-compose -f $(APP_COMPOSE_FILE) down' EXIT; $(MAKE) run-start"
