FROM eclipse-temurin:25.0.4.1_1-jdk-noble@sha256:ba0f0e1a33864105e1d454e88105c617ab40f71062db1f306bf58be32a3f8e5e AS build

WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle dependencies.gradle gradle.properties .checkstyle.xml ./
COPY gradle/ gradle/
COPY .github/allowed-licenses.json .github/allowed-licenses.json
COPY LICENSE*.md ./
COPY tracking/core/ tracking/core/
COPY tracking/backend/ tracking/backend/

RUN sed -i 's/\r$//' gradlew \
    && chmod 0755 gradlew \
    && mkdir -p game wizard audience/relay \
    && ./gradlew --no-daemon :tracking:backend:installDist

FROM eclipse-temurin:25.0.4.1_1-jre-noble@sha256:3f2223fe04b750aa5a8179e29b5c9a1ddf7665fc9bb08bfe11ad01d143bc13a5

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
