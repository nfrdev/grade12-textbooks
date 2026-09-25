#!/bin/sh
exec java -jar "$(dirname "$0")/gradle/gradle-wrapper.jar" "$@"
