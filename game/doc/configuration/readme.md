---
title: Configuration Overview
---

## Intro

System zum Speichern und Laden von Konfigurationsdateien. Die Konfigurationsdateien werden im JSON-Format gespeichert.
Der Zugriff auf die in der Konfigurationsdatei gespeicherten Werte erfolgt über statische Felder in ConfigMap-Klassen.
Diese Klassen *KÖNNEN* mit der Annotation `@ConfigMap` annotiert werden, um eine Prefix im Key-Pfad zu definieren.

## Beschreibung der einzelnen Komponenten

- [Configuration](./Configuration.md)
- [ConfigKey\<T>](./ConfigKey.md)
- [ConfigValue\<T>](./ConfigValue.md)
