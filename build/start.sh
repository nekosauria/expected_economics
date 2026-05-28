#!/bin/bash

APP_NAME="ee"
JAR_PATH="/opt/ee/ee-1.0.0.jar"
LOG_PATH="/opt/ee/logs/app.log"
JAVA_OPTS="-Xms256m -Xmx512m"
PROFILE="prod"

mkdir -p /opt/ee/logs

PID=$(pgrep -f "$JAR_PATH")
if [ -n "$PID" ]; then
    echo "[$APP_NAME] 已在執行中 (PID: $PID)，請先執行 stop.sh"
    exit 1
fi

nohup java $JAVA_OPTS \
    -jar "$JAR_PATH" \
    --spring.profiles.active=$PROFILE \
    > "$LOG_PATH" 2>&1 &

echo "[$APP_NAME] 已啟動，PID: $!, Log: $LOG_PATH"