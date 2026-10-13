FROM eclipse-temurin:25.0.4.1_1-jdk-noble@sha256:589ff4cc3f71aab462e7048a47a0d10edf57fbccde3fceea2281e610bf5880b4 AS build

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

FROM eclipse-temurin:25.0.4.1_1-jre-noble@sha256:d9a39a23634650173f1e2bbc176227af9728587ecf0f4b62d53e9355cd7a19ab

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
