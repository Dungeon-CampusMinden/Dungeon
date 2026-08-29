FROM eclipse-temurin:25.0.4_7-jdk-noble@sha256:534968c051301957beae735e7ba1db54d99ddecf08746d3b9d4f318cc132dbc3 AS build

WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle dependencies.gradle gradle.properties .checkstyle.xml ./
COPY gradle/ gradle/
COPY .github/allowed-licenses.json .github/allowed-licenses.json
COPY LICENSE*.md ./
COPY tracking/core/ tracking/core/
COPY tracking/backend/ tracking/backend/

RUN sed -i 's/\r$//' gradlew \
    && chmod 0755 gradlew \
    && mkdir -p game wizard \
    && ./gradlew --no-daemon :tracking:backend:installDist

FROM eclipse-temurin:25.0.4_7-jre-noble@sha256:b4c93a50fc67612798db73d68ca3b0ee4ebdd51736e59cca370e689b9797037e

LABEL org.opencontainers.image.title="Dungeon tracking backend" \
      org.opencontainers.image.description="Self-hosted Dungeon tracking backend" \
      org.opencontainers.image.version="1" \
      org.opencontainers.image.source="https://github.com/Dungeon-CampusMinden/Dungeon"

RUN groupadd --gid 10001 dungeon \
    && useradd --uid 10001 --gid dungeon --no-create-home --home-dir /nonexistent dungeon

WORKDIR /opt/dungeon-tracking
COPY --from=build --chown=10001:10001 /workspace/tracking/backend/build/install/backend/ ./

USER 10001:10001
EXPOSE 8088
ENTRYPOINT ["/opt/dungeon-tracking/bin/backend"]
