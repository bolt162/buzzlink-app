#!/bin/bash

# BuzzLink Backend Runner
# This script ensures the correct Java version (17) is used

export JAVA_HOME=$(/usr/libexec/java_home -v 17)

echo "Using Java version:"
java -version

echo ""
echo "Starting BuzzLink Backend..."
echo ""

./gradlew bootRun --args='--spring.profiles.active=dev'
