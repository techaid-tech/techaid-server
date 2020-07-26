FROM gradle:6.0.1-jdk11  as builder
USER root
COPY ./.git /app/.git
COPY ./src /app/src
COPY ./build.gradle settings.gradle /app/
RUN cd /app; echo "$(git rev-parse --short HEAD)" > /app/git-commit.hash
RUN gradle -p /app clean build -x test

FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=builder /app/build/libs/*.jar /app/app.jar
COPY --from=builder /app/git-commit.hash /app/git-commit.hash

# Add an init system
RUN apk add --no-cache --update \
    openssl \
    curl \
    bash \
    tini \
    wget \
    tzdata
ENV TZ=UTC
RUN cp /usr/share/zoneinfo/UTC /etc/localtime
RUN date > /app/build.date
WORKDIR /app
ENTRYPOINT [ "/sbin/tini", "--"]
CMD ["java", "-jar", "/app/app.jar"]