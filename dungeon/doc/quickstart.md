---
title: "Dungeon: Quickstart"
---

Diese Anleitung beschreibt Schritt für Schritt, wie Dungeon: Learning by Questing heruntergeladen, konfiguriert und gestartet werden kann.

## Schritt 1: Herunterladen der Ressourcen 


Stellen Sie sicher, dass Sie Java in der Version 17 oder höher installiert haben und Java über Ihre Kommandozeile verwendbar ist. Dies können Sie testen, indem Sie ein Terminal öffnen und den Befehl `java --version` ausführen.

Laden Sie entweder die [Demo](https://github.com/Programmiermethoden/Dungeon/releases/tag/Demo) mit bereits vordefinierten Aufgaben herunter oder das [StarterKit](https://github.com/Programmiermethoden/Dungeon/releases/tag/StarterKit), um eigene Aufgaben zu definieren.

Legen Sie die `Starter.jar` im selben Verzeichnis ab wie die mitgelieferte `.dng`.

## Schritt 2: Eigene Aufgaben definieren

*Falls Sie die Demo heruntergeladen haben, können Sie diesen Schritt überspringen.*

Öffnen Sie die Datei `template.dng` mit einem Editor Ihrer Wahl.

Sie können Ihre eigenen [Aufgaben](https://github.com/Programmiermethoden/Dungeon/blob/Demo/dungeon/doc/readme.md#definition-einer-aufgabe) und [Aufgabengraphen](https://github.com/Programmiermethoden/Dungeon/blob/Demo/dungeon/doc/readme.md#definition-von-aufgabenabh%C3%A4ngigkeiten) im Abschnitt 
```
// +++++++++++++ AUFGABEN DEFINITION+++++++++++++++
IHRE AUFGABEN DEFINITION
// ++++++++++++++++++++ ENDE AUFGABEN DEFINITION ++++++++++++++++++++
```
definieren.

Eine einfache Aufgabendefinition für eine Single-Choice- und eine Multiple-Choice-Frage:

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

Öffnen Sie ein Terminalfenster und navigieren Sie in das Verzeichnis mit der `Starter.jar` und der `.dng`-Datei.

Unter Linux und Windows geben Sie den Befehl
`java -jar Starter.jar --args "MY_FILE.dng"`
ein. Ersetzen Sie dabei "MY_FILE" durch den Namen des `.dng`-Files.

Unter macOS geben Sie den Befehl
`java -XstartOnFirstThread -jar Starter.jar --args "MY_FILE.dng"`
ein. Ersetzen Sie dabei "MY_FILE" durch den Namen des `.dng`-Files.

Das Spiel startet sich nun, und Sie können die Aufgaben im Spiel lösen.
Lesen Sie die [Spielanleitung](https://github.com/Programmiermethoden/Dungeon/blob/Demo/dungeon/doc/how_to_play.md), wenn Sie Hilfe benötigen.

## Schritt 4: Verbreitung

Wenn Sie das Spiel mit den Aufgaben an andere verbreiten möchten, müssen Sie die `Starter.jar` und die `.dng`-Datei an die betroffene Person schicken.
Die Person kann das Spiel genau wie in Schritt 3 beschrieben starten.

