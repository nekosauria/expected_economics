# 清除舊的 build 並打包
mvn clean package

# test 
java -jar target/ee-1.0.0.jar --server.port=8080

# scp 
...... (skip)

# project tree (以 ubuntu 24.04 lts 為例)
```
/opt/ee/
├── ee-1.0.0.jar
├── start.sh
├── stop.sh
└── logs/app.log
```

# add start.sh & stop.sh （單純測試用的,啟動還是用 systemctl）
```
chmod +x start.sh stop.sh
./start.sh

# 查看即時 log
tail -f /opt/ee/logs/app.log
```

# add systemd
sudo vi /etc/systemd/system/ee.service
```
[Unit]
Description=Expected Economics Service
After=network.target

[Service]
Type=simple
User=teddylai
WorkingDirectory=/opt/ee
ExecStart=java -Xms256m -Xmx512m \
-jar /opt/ee/ee-1.0.0.jar \
--spring.profiles.active=prod
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/ee/logs/app.log
StandardError=append:/opt/ee/logs/app.log

[Install]
WantedBy=multi-user.target
```

```
# 重新載入 systemd 設定
sudo systemctl daemon-reload

# 設定開機自動啟動
sudo systemctl enable ee

# 立即啟動服務
sudo systemctl start ee

# 查看服務狀態
sudo systemctl status ee

# 查看即時 log
sudo journalctl -u ee -f
```