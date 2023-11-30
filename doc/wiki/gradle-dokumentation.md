---
title: "Gradle"
---


Folgende Gradle-Tasks sind nützlich:

- `./gradlew run` => Startet einen Gradle-Daemon, kompiliert das Projekt und startet das Spiel.
- `./gradlew assemble` => Kompiliert das Projekt.
- `./gradlew test` => Startet alle JUnit-Tests.
- `./gradlew build` => Kompiliert das Projekt und startet alle (JUnit-)Tests.
- `./gradlew clean` => Räumt die kompilierten Dateien wieder auf.

Alle gestarteten Gradle-Prozesse und -Dämonen können mit `./gradlew --stop` wieder gestoppt werden.
