FROM gradle:6.0.1-jdk11  as builder
USER root
COPY ./src /app/src
COPY ./build.gradle settings.gradle /app/
RUN gradle -p /app clean build -x test

FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=builder /app/build/libs/*.jar /app/app.jar
RUN apk add --no-cache --update \
    openssl \
    curl \
    bash \
    tini \
    wget \
    tzdata
ENV TZ=UTC
RUN cp /usr/share/zoneinfo/UTC /etc/localtime
WORKDIR /app
COPY ./CHECKS /app
COPY ./Procfile /app
# COPY ./DOKKU_SCALE /app
ENTRYPOINT [ "/sbin/tini", "--"]
CMD ["java", "-jar", "/app/app.jar"]