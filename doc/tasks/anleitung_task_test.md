---
title: "Anleitung für die Definition von Aufgaben"
---

## Aufgaben per DSL definieren

Aktuell werden die Aufgaben-Definitionen aus `dungeon.assets.scripts.task_test.dng` eingelesen und
in ein Spielszenario übersetzt.

Es sind zwei unterschiedliche Aufgabentypen verfügbar

### Single Choice Frage

Eine Single Choice Frage benötigt eine textuelle Beschreibung (`description`),
eine Liste an Antwortmöglichkeiten (`answers`) und den 0-basierten
Index der korrekten Antwortmöglichkeit in `answers` (`correct_answer_index`).

`my_sc_task` ist im Folgenden der Name der Aufgabendefinition:

```
single_choice_task my_sc_task {
    description: "Hier kommt die Aufgabenbeschreibung hin",
    answers: ["antwort1", "antwort2", "antwort3"],
    correct_answer_index: 2
}
```

### Multiple Choice Frage

Eine Multiple Choice Frage benötigt eine textuelle Beschreibung (`description`),
eine Liste an Antwortmöglichkeiten (`answers`) und eine Liste von 0-basierten
Indizes der korrekten Antwortmöglichkeiten in `answers` (`correct_answer_indices`).

`my_mc_task` ist im Folgenden der Name der Aufgabendefinition:
```
multiple_choice_task my_mc_task {
    description: "Hier kommt die Aufgabenbeschreibung hin",
    answers: ["antwort1", "antwort2", "antwort3"],
    correct_answer_indices: [1,2]
}
```

### Einbindung der Aufgaben in den Dungeon

Über die `quest_config`-Definition können die definierten Aufgaben in das Dungeon integriert werden:

```
quest_config c {
    tasks: [my_sc_task, my_mc_task]
}
```

## Starten des Dungeons

Die Version des Dungeons, welche die Aufgaben enthält, kann mit dem gradle-Target `TaskGenerationTest`
gestartet werden, also: `./gradlew TaskGenerationTest`.
