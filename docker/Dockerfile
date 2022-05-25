FROM eclipse-temurin:17-jre-focal
WORKDIR /vertx-ddns
USER root
MAINTAINER gngpp <verticle@foxmail.com>
LABEL name=vertx-ddns
LABEL url=https://github.com/gngpp/vertx-ddns

ENV LANG C.UTF-8
ARG JAR=./build/libs/vertx-ddns-latest-all.jar
COPY ${JAR} /vertx-ddns/vertx-ddns.jar

# Continue with your application deployment
RUN mkdir /vertx-ddns/logs
EXPOSE 	8080
ENV JVM_OPTS="-Xms128m -Xmx128m" \
    TZ=Asia/Shanghai

CMD exec java ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /vertx-ddns/vertx-ddns.jar
