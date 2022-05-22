FROM eclipse-temurin:17.0.2_8-jdk as jre-build
WORKDIR /vertx-ddns
USER root
MAINTAINER gngpp <verticle@foxmail.com>
LABEL name=vertx-ddns
LABEL url=https://github.com/gngpp/vertx-ddns

RUN $JAVA_HOME/bin/jlink \
         --add-modules jdk.crypto.ec,java.base,java.compiler,java.logging,java.desktop,java.management,java.naming,java.net.http,java.rmi,java.scripting,java.security.jgss,java.sql,java.xml,jdk.jdi,jdk.unsupported \
         --output /javaruntime

FROM ubuntu:bionic-20220315
WORKDIR /vertx-ddns
USER root
MAINTAINER gngpp <verticle@foxmail.com>
LABEL name=vertx-ddns
LABEL url=https://github.com/gngpp/vertx-ddns

ENV LANG C.UTF-8
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
ARG JAR=./build/libs/vertx-ddns-latest-all.jar
COPY ${JAR} /vertx-ddns/vertx-ddns.jar
COPY --from=jre-build /javaruntime $JAVA_HOME

# Continue with your application deployment
RUN mkdir /vertx-ddns/logs
EXPOSE 	8080
ENV JVM_OPTS="-Xms128m -Xmx128m" \
    TZ=Asia/Shanghai

CMD exec java ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /vertx-ddns/vertx-ddns.jar
