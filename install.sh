#!/bin/bash
JAVA_COMMAND_PATH="/bin/java"
JAR_PATH="/vertx-ddns.jar"
PORT="8080"
echo "Port 8080 is used by default. Are you sure you want to change the port?"
# shellcheck disable=SC2162
read -p "Please enter the Y/N:" yesNo
case $yesNo in
[yY])
  echo -n "Input server port:"
  # shellcheck disable=SC2162
  read PORT
  ;;
[nN])
  PORT="8080"
  ;;
 *)
   echo "Invalid input ..."
   # shellcheck disable=SC2034
   # shellcheck disable=SC2162
   read -p "Please enter any key to exit"
   exit 1
   ;;
esac

# shellcheck disable=SC2154
ABS_COMMAND_PATH=$(pwd)$JAVA_COMMAND_PATH
ABS_JAR_PATH=$(pwd)$JAR_PATH
C="$"
MAINPID="MAINPID"
VERTX_SERVICE_CONFIG="[Unit]
Description=Vertx-DDNS Service
Documentation=https://github.com/gngpp/vertx-ddns/edit/main/README.md
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
ExecStart=$ABS_COMMAND_PATH -server -Xms128m -Xmx128m -jar $ABS_JAR_PATH $PORT
ExecStop=/bin/kill -s QUIT $C$MAINPID
Restart=always
StandOutput=syslog

StandError=inherit

[Install]
WantedBy=multi-user.target"

# shellcheck disable=SC2162
echo "Does it show the input service configuration?"
# shellcheck disable=SC2162
read -p "Please enter the Y/N:" isShow
case $isShow in
[yY])
 echo -e "Service config: $VERTX_SERVICE_CONFIG"
  ;;
[nN])
  ;;
*)
  ;;
esac

echo "Create a directory: /etc/systemd/system"
# shellcheck disable=SC2093
mkdir -p -v /etc/systemd/system
echo -e "$VERTX_SERVICE_CONFIG" > "/etc/systemd/system/vertx-ddns.service"
echo "Service configuration write to: /etc/systemd/system/vertx-ddns.service Complete!"
