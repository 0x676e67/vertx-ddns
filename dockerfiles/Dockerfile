# Ubuntu ---- Openj9-jre-complete
FROM adoptopenjdk:16-jre-openj9 as build
WORKDIR /root
USER root

ARG JAR_FILE=build/libs/vertx-ddns-latest-all.jar
COPY ${JAR_FILE} vertx-ddns.jar
RUN chmod +x vertx-ddns.jar
MAINTAINER zf1976 <verticle@foxmail.com>
EXPOSE 	8080
################################
LABEL name=vertx-ddns
LABEL url=https://github.com/zf1976/vertx-ddns

RUN mkdir /root/logs
ENV JVM_XMS="256m" \
    JVM_XMX="256m" \
    JVM_OPTS="-Xmx256m -Xms256m" \
    TZ=Asia/Shanghai

CMD exec java -Xms${JVM_XMS} -Xmx${JVM_XMX} ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar vertx-ddns.jar


# Debian ---- Openj9-jlink
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

# Continue with your application deployment
RUN mkdir /root/logs
ARG JAR_FILE=build/libs/vertx-ddns-latest-all.jar
COPY ${JAR_FILE} /root/vertx-ddns.jar
EXPOSE 	8080
ENV JVM_OPTS="-Xms128m -Xmx256m" \
    TZ=Asia/Shanghai

CMD exec java ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /root/vertx-ddns.jar
