FROM eclipse-temurin:26.0.2_10-jdk-noble@sha256:091b6640864939942cd9d7ddd16576f31112d5a56a595b566ce561d1c0e07c6b AS build

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

FROM eclipse-temurin:26.0.2_10-jre-noble@sha256:c12a27c567c4ce00b0caef14900c1bf2f5e997524c3ec73463ae695352d5f34d

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
