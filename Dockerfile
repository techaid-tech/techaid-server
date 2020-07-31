FROM adoptopenjdk/openjdk11:alpine-jre
COPY ./build/libs/*.jar /app/app.jar
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
ENTRYPOINT [ "/sbin/tini", "--"]
CMD ["java", "-jar", "/app/app.jar"]