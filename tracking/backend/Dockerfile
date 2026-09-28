FROM eclipse-temurin:25.0.4_7-jdk-noble@sha256:2feab631bffce6236d8bb5261a4abe19a8d6f85bad1c01166f74686c983d011f AS build

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

FROM eclipse-temurin:25.0.4_7-jre-noble@sha256:b573af9e331196fbc42e246da4df24df9b6c556c73e7efddfde0511f1c9508c5

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
