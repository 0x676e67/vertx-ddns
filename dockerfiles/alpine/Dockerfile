# Alpine ---- OpenJDK-jlink
FROM gradle:jdk16-openj9 as gradle-build
USER root
WORKDIR /vertx-ddns
ARG GRADLE_SETTINGS=./settings.gradle
ARG GRADLE_BUILD=./build.gradle
# dependencies cache
COPY ${GRADLE_SETTINGS} /vertx-ddns/settings.gradle
COPY ${GRADLE_BUILD} /vertx-ddns/build.gradle
RUN gradle assemble --info

# Build project jar
ARG SOURCE_FILE=./
COPY ${SOURCE_FILE} /vertx-ddns
#RUN gradle assemble --info
RUN gradle shadowJar

FROM openjdk:16-jdk-alpine as jre-build
# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules jdk.crypto.ec,java.base,java.compiler,java.logging,java.desktop,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.sql,java.xml,jdk.jdi,jdk.unsupported \
         --output /javaruntime

# Define your base image
FROM alpine
WORKDIR /root
USER root
MAINTAINER zf1976 <verticle@foxmail.com>
LABEL name=vertx-ddns
LABEL url=https://github.com/zf1976/vertx-ddns

ENV LANG C.UTF-8
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
ENV JAR_FILE=/vertx-ddns/build/libs/vertx-ddns-latest-all.jar
COPY --from=jre-build /javaruntime $JAVA_HOME
COPY --from=gradle-build ${JAR_FILE} /root/vertx-ddns.jar

# Continue with your application deployment
RUN mkdir /root/logs
EXPOSE 	8080
ENV JVM_OPTS="-Xms128m -Xmx256m" \
    TZ=Asia/Shanghai

CMD exec java ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /root/vertx-ddns.jar