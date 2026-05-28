#!/bin/bash

JAR_PATH="/opt/ee/ee-1.0.0.jar"

PID=$(pgrep -f "$JAR_PATH")
if [ -z "$PID" ]; then
    echo "服務未在執行"
    exit 0
fi

kill "$PID"
echo "已停止 PID: $PID"