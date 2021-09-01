# Debian ---- Openj9-jlink
FROM gradle:jdk16-openj9 as gradle-build
USER root
WORKDIR /vertx-ddns

# dependencies cache
COPY ./settings.gradle /vertx-ddns/settings.gradle
COPY ./build.gradle /vertx-ddns/build.gradle
RUN gradle assemble --info

# Build project jar
ARG SOURCE_FILE=./
COPY ${SOURCE_FILE} /vertx-ddns
#RUN gradle assemble --info
RUN gradle shadowJar

FROM adoptopenjdk:16-jdk-openj9 as jre-build
# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules jdk.crypto.ec,java.base,java.compiler,java.logging,java.desktop,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.sql,java.xml,jdk.jdi,jdk.unsupported \
         --output /javaruntime

# Define your base image
FROM debian:buster-slim
MAINTAINER zf1976 <verticle@foxmail.com>
WORKDIR /root
USER root
LABEL name=vertx-ddns
LABEL url=https://github.com/zf1976/vertx-ddns

ENV LANG C.UTF-8
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME
COPY --from=gradle-build /vertx-ddns/build/libs/vertx-ddns-latest-all.jar /root/vertx-ddns.jar
# Continue with your application deployment
RUN mkdir /root/logs
#ARG JAR_FILE=build/libs/vertx-ddns-latest-all.jar
#COPY ${JAR_FILE} /root/vertx-ddns.jar
EXPOSE 	8080
ENV JVM_OPTS="-Xms128m -Xmx256m" \
    TZ=Asia/Shanghai

CMD exec java ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /root/vertx-ddns.jar
