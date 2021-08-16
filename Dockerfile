# from base images
FROM adoptopenjdk:16-jre-openj9 as builder
WORKDIR /app
ARG JAR_FILE=build/libs/vertx-ddns-1.0-all.jar
COPY ${JAR_FILE} vertx-ddns.jar
RUN chmod +x vertx-ddns.jar
MAINTAINER zf1976 <verticle@foxmail.com>
EXPOSE 	8080
################################
LABEL name=vertx-ddns

ENV JVM_XMS="256m" \
    JVM_XMX="256m" \
    JVM_OPTS="-Xmx256m -Xms256m" \
    TZ=Asia/Shanghai

CMD exec java -Xms${JVM_XMS} -Xmx${JVM_XMX} ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar vertx-ddns.jar