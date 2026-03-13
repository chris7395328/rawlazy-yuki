#!/usr/bin/env sh

executable="$0"
while [ -h "$executable" ]; do
  dir="$(cd -P "$(dirname "$executable")" && pwd)"
  executable="$(readlink "$executable")"
  [[ $executable != /* ]] && executable="$dir/$executable"
done

DIR="$(cd -P "$(dirname "$executable")" && pwd)"

if [ -d "$DIR/gradle/wrapper" ]; then
  GRADLE_HOME="$DIR/gradle"
  export GRADLE_HOME
  exec "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
else
  echo "Error: Gradle wrapper not found"
  exit 1
fi
