---
title: "JDK Kompatibilät"
---

(Bitte ergänzen:)

### JDKs, die mit LibGdx funktionieren:

| Arch. | OS               | JDK                         | Quelle                                                                                | Fehler |
| ----- | ---------------- | --------------------------- | ------------------------------------------------------------------------------------- | ------ |
| amd64 | Ubuntu           | Temurin 17 (LTS) / Adoptium | [URL](https://adoptium.net/)                                                          | --     |
| amd64 | Kali Linux       | Temurin 17 (LTS) / Adoptium | [URL](https://adoptium.net/)                                                          | --     |
| x64   | Windows 10/11    | Oracle JDK 17 (LTS)         | [URL](https://https://www.oracle.com/java/)                                           | --     |
| x64   | Windows 11       | Amazon Corretto 17          | [URL](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html) | --     |
| x64   | Pop-OS! 21.10    | Temurin 17 (LTS) / Adoptium | [URL](https://adoptium.net/)                                                          | --     |
| x64   | macOS 11 Big Sur | Java SE, JDK 17             | [URL](https://https://www.oracle.com/java/)                                           | --     |

### JDKs, die mit LibGdx \*nicht\* funktionieren:

| Arch. | OS            | JDK                                                                 | Quelle                                                                 | Fehler |
| ----- | ------------- | ------------------------------------------------------------------- | ---------------------------------------------------------------------- | ------ |
| amd64 | Ubuntu        | openjdk-17-jdk/focal-updates,focal-security 17.0.1+12-1~20.04 amd64 | Paketquelle                                                            | 1      |
| amd64 | Kali Linux    | openjdk-17-jdk/kali-rolling 17.0.2+8-1 amd64                        | Paketquelle                                                            | 1      |
| amd64 | Kubuntu 21.10 | openjdk-17-jdk 17.0.1+12-1~21.10                                    | Paketquelle                                                            | 2      |
| amd64 | Kubuntu 21.10 | jdk-17.0.1 17.0.1+12-LTS-39                                         | [URL](https://www.oracle.com/java/technologies/downloads/#jdk17-linux) | 2      |
| amd64 | Kubuntu 21.10 | Temurin 17 (LTS) / Adoptium                                         | [URL](https://adoptium.net/)                                           | 2      |
| amd64 | Pop-OS! 21.10 | OpenJDK 17.0.2                                                      | Paketquelle                                                            | 1      |

incl. aller 32Bit JDKs.

1:

```
Inconsistency detected by ld.so: dl-lookup.c: 105: check_match: Assertion `version->filename == NULL || ! _dl_name_match_p (version->filename, map)' failed!
```

2:

```
Could not initialize class org.lwjgl.Sys
```

### Kompatibilität getestet in der GH-Actions Pipeline:

| JDK                                          | Windows | Ubuntu | MacOS |
| -------------------------------------------- | ------- | ------ | ----- |
| [AdoptOpenJDK 17](https://adoptopenjdk.net/) | OK      | OK     | OK    |

- Alle direkt in GH-Actions verwendbaren JDKs sind unter folgendem Link einsehbar.
  https://github.com/actions/setup-java#supported-distributions

- Manuell können weitere JDKs hinzugefügt werden.
  https://github.com/actions/setup-java/blob/main/docs/advanced-usage.md#installing-java-from-local-file

### Das Dungeon auf dem Pi 3B(+) starten:

Das Dungeon funktioniert auch auf dem Pi 3B(+). Es sollte aus Speichergründen aber die einzige geöffnete Anwendung sein. Folgende Schritte sind dafür notwendig:

- Downloaden oder clonen sie das [dungeon-starter](https://github.com/PM-Dungeon/dungeon-starter) Repo und entpacken Sie es.
- Entfernen Sie die Optionen `-Xms128m -Xmx2048m` aus `code/gradle.properties`.
- Installieren Sie das JDK 17: `sudo apt update` und `sudo apt install openjdk-17-jdk`.
- Wechseln Sie in das Verzeichnis `dungeon-starter-master/code` und starten Sie `./gradlew run`, dann startet Gradle, javac, libGDX und das Spiel. :)

Getestet mit:

```
Raspberry Pi OS with desktop
Release date: April 4th 2022
System: 64-bit
Kernel version: 5.15
Debian version: 11 (bullseye)
```
