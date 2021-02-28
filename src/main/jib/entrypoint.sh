#!/bin/sh

echo "Starting application"
exec java ${JAVA_OPTS} -noverify -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -cp /app/resources/:/app/classes/:/app/libs/* "org.oneworldaccuracy.demo.DemoApplication"  "$@"
