#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${1:-}" ]]; then
  echo "Usage: $0 <new-version>"
  exit 1
fi
new_version="$1"

# Maven
perl -pi -e 's#<version>[^<]+</version>#<version>'"$new_version"'</version>#' maven-app/pom.xml

# Gradle
perl -pi -e 's#version = "[^"]+"#version = "'"$new_version"'"#' gradle-app/build.gradle

# Ant
perl -pi -e 's#<property name="project.version" value="[^"]+"\s*/>#<property name="project.version" value="'"$new_version"'" />#' ant-app/build.xml

git add maven-app/pom.xml gradle-app/build.gradle ant-app/build.xml
