FROM eclipse-temurin:25.0.4.1_1-jdk-noble@sha256:589ff4cc3f71aab462e7048a47a0d10edf57fbccde3fceea2281e610bf5880b4 AS build

WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle build.gradle dependencies.gradle gradle.properties .checkstyle.xml ./
COPY gradle/ gradle/
COPY .github/allowed-licenses.json .github/allowed-licenses.json
COPY LICENSE*.md ./
COPY audience/relay/ audience/relay/
COPY game/assets/fonts/ game/assets/fonts/

RUN sed -i 's/\r$//' gradlew \
    && chmod 0755 gradlew \
    && mkdir -p game wizard tracking/core tracking/backend \
    && ./gradlew --no-daemon :audience:relay:installDist

FROM eclipse-temurin:25.0.4.1_1-jre-noble@sha256:d9a39a23634650173f1e2bbc176227af9728587ecf0f4b62d53e9355cd7a19ab

LABEL org.opencontainers.image.title="Dungeon audience relay" \
      org.opencontainers.image.description="Relay between a Dungeon game in audience mode and the audience's devices" \
      org.opencontainers.image.version="1" \
      org.opencontainers.image.source="https://github.com/Dungeon-CampusMinden/Dungeon"

RUN groupadd --gid 10001 dungeon \
    && useradd --uid 10001 --gid dungeon --no-create-home --home-dir /nonexistent dungeon

WORKDIR /opt/dungeon-relay
COPY --from=build --chown=10001:10001 /workspace/audience/relay/build/install/relay/ ./

USER 10001:10001
ENV DUNGEON_RELAY_BIND_HOST=0.0.0.0
EXPOSE 8090
ENTRYPOINT ["/opt/dungeon-relay/bin/relay"]
