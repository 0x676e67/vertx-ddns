#!/bin/bash
service vertx-ddns stop
rm -rf /etc/systemd/system/vertx-ddns.service
systemctl daemon-reload
