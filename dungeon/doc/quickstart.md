---
title: "Dungeon: Quickstart"
---

Diese Anleitung beschreibt Schritt für Schritt, wie Dungeon: Learning by Questing heruntergeladen, konfiguriert und gestartet werden kann.

## Schritt 1: Herunterladen der Ressourcen

- [ ] Überprüfen Sie die Java-Version: `java -version` sollte 'java version  "21.0.2" 2024-01-16 LTS' oder ähnlich liefern.

### Option 1: Arbeiten mit dem Starter-Kit

- [ ] [StarterKit](https://github.com/Dungeon-CampusMinden/Dungeon-StarterKit) herunterladen, um eigene Aufgaben zu definieren.
- [ ] Legen Sie die `Starter.jar` im selben Verzeichnis wie die mitgelieferte `.dng` ab.

### Option 2: Repository Klonen

- [ ] Klonen Sie dieses Repository mit dem Befehl `git clone https://github.com/Dungeon-CampusMinden/Dungeon.git`.
- [ ] Navigieren Sie mit einem Terminal in das Verzeichnis.
- [ ] Bauen Sie die `Starter.jar` mit dem Befehl `gradlew starterJar`.
- [ ] Kopieren Sie die `Starter.jar` aus `build/libs/` und das `template-dng` aus `dungeon/assets/scripts/` in ein Verzeichnis.


## Schritt 2: Eigene Aufgaben definieren

- [ ] Öffnen Sie die Datei `template.dng` mit einem Editor Ihrer Wahl.
- [ ] Definieren Sie Ihre eigenen [Aufgaben](https://github.com/Dungeon-CampusMinden/Dungeon/blob/Demo/dungeon/doc/readme.md#definition-einer-aufgabe) und [Aufgabengraphen](https://github.com/Dungeon-CampusMinden/Dungeon/blob/Demo/dungeon/doc/readme.md#definition-von-aufgabenabh%C3%A4ngigkeiten) im Abschnitt
```
// +++++++++++++ AUFGABEN DEFINITION+++++++++++++++
IHRE AUFGABEN DEFINITION
// ++++++++++++++++++++ ENDE AUFGABEN DEFINITION ++++++++++++++++++++
```

Beispiel:

```
single_choice_task single {
  description: "Bitte suchen Sie die eine richtige Ausage:",
  answers: [
  "Man kann nicht auf den Betriebssystemen Windows, MACOS und Linux in Python programmieren.",
  "Man kann auf externen Datenträgern schnell sortieren.",
  "Schlüsselfelder eignen sich nicht zum Indizieren von Daten.",
  "Keine davon ist richtig."],
  correct_answer_index: 3
}

multiple_choice_task multi {
  description: "Bitte suchen Sie die richtige Ausage:",
  answers: [
  "Eine Ordnungsrelation erlaubt gleichen Schlüssel.",
  "Man kann Datensätze nach dem Datum sortieren lassen.",
  "Sortierung ist immer aufsteigend.",
  "Keine davon ist richtig."],
  correct_answer_indices: [0, 1]
}

graph example_graph {
    single -> multi [type=seq];
}

dungeon_config example {
    dependency_graph: example_graph
}
```

In diesem Beispiel wird zuerst die Single-Choice-Aufgabe gestellt, und nach der Bearbeitung dieser Aufgabe wird die Multiple-Choice-Frage im Spiel freigeschaltet.

## Schritt 3: Starten

- [ ] Öffnen Sie ein Terminalfenster und navigieren Sie in das Verzeichnis mit der `Starter.jar` und der `.dng`-Datei.

- [ ] Unter Linux und Windows geben Sie den Befehl
  `java -jar Starter.jar --args "MY_FILE.dng"`
  ein. Ersetzen Sie dabei "MY_FILE" durch den Namen des `.dng`-Files.

- [ ] Unter macOS geben Sie den Befehl
  `java -XstartOnFirstThread -jar Starter.jar --args "MY_FILE.dng"`
  ein. Ersetzen Sie dabei "MY_FILE" durch den Namen des `.dng`-Files.

- [ ] Lesen Sie die [Spielanleitung](https://github.com/Dungeon-CampusMinden/Dungeon/blob/Demo/dungeon/doc/how_to_play.md), wenn Sie Hilfe benötigen.

## Schritt 4: Verbreitung

- [ ] `Starter.jar` und die `.dng`-Datei versenden.
- [ ] Starten wie in Schritt 3 beschrieben oder per Skript (siehe unten).

## Start-Skript

Sie können ein einfaches Bash-Skript (Linux/MacOS) oder ein Batch-Skript (Windows) erstellen, um den Start des Spiels zu vereinfachen. Sie können diese Skripte auch verbreiten.

### Bash-Skript (Windows)

- [ ] Datei `starter.sh` erstellen
- [ ] Datei in einem Editor Ihrer Wahl öffnen
- [ ] Skript (Ersetzen Sie dabei "MY_FILE" durch den Namen des `.dng`-Files):
  ```bash
  #!/bin/bash
  java -XstartOnFirstThread -jar Starter.jar --args "MYFILE.dng"
  ```
- [ ] Mit `chmod +x starter.sh` machen Sie das Skript ausführbar
- [ ] Mit `bash starter.sh` können Sie das Dungeon nun starten.

### Batch-Skript (Linux/MacOS)

- [ ] Datei `starter.bat` erstellen
- [ ] Datei in einem Editor Ihrer Wahl öffnen
- [ ] Skript (Ersetzen Sie dabei "MY_FILE" durch den Namen des `.dng`-Files):
  ```bat
  @echo off
  java -XstartOnFirstThread -jar Starter.jar --args "MYFILE.dng"
  ```
- [ ] Mit `starter.bat` können Sie das Dungeon nun starten.
