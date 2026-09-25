#!/bin/sh
exec java -cp "$(dirname "$0")/gradle/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
