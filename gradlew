#!/usr/bin/env sh

# Gradle Wrapper

set -e

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd)

# Download Gradle if not found
if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    GRADLE_VERSION="8.10"
    GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    
    if command -v curl >/dev/null 2>&1; then
        curl -s -L "$GRADLE_URL" -o "$APP_HOME/gradle/wrapper/gradle.zip"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$GRADLE_URL" -O "$APP_HOME/gradle/wrapper/gradle.zip"
    else
        echo "Error: Neither curl nor wget is available to download Gradle"
        exit 1
    fi
    
    unzip -q "$APP_HOME/gradle/wrapper/gradle.zip" -d "$APP_HOME/gradle"
    cp "$APP_HOME/gradle/gradle-${GRADLE_VERSION}/lib/gradle-wrapper.jar" "$APP_HOME/gradle/wrapper/"
    rm -rf "$APP_HOME/gradle/gradle-${GRADLE_VERSION}" "$APP_HOME/gradle/wrapper/gradle.zip"
fi

# Run Gradle
"$JAVA_HOME/bin/java" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
