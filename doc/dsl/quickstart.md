---
title: "DSL Quickstart Guide"
---

## Definition einer Aufgabe

- Beispiel für das “Hello World” Äquivalent für die DSL, Erstellung einer einfachen Aufgabe

```
// file: my_simple_task.ds

task my_task {
    text: "Das ist der Aufgabentext",
    answers: {
        A: 1,
        B: 2,
        C: 3
    },
    correct_answer: answers.B,
    type: SINGLE_CHOICE
}

level_config my_config {
    task: my_task
}
```

- Hier folgen einige Bilder, die zeigen, wie die Übersetzung der Definition im Dungeon Level
  aussieht

- Hier folgt die Beschreibung der einzelnen Komponenten dieses Beispiels:

  - Task-Definition
  - Level-Config

- Hier wird beschrieben, wie man die obige Definition in den Dungeon lädt und startet, da
  ist aktuell noch unklar, wie das funktioniert

## Konfiguration der Bewertung von Aufgaben

TODO, noch kein klares Konzept

## Definition von komplexen Aufgaben

- komplexe Aufgabe = Aufgabe mit Unteraufgaben

Die Organisation der Aufgaben wird per Petri-Netz modelliert. Zur Definition des
Petri-Netzes wird die eingebettete dot-Umgebung genutzt.

```
task master_task {
    description: "Hier steht der Aufgabentext der übergeordneten Aufgabe"
}

task subtask1 {
    description: "Das ist der Aufgabentext",
    answers: {
        A: 1,
        B: 2,
        C: 3
    },
    correct_answers: answers.B,
    type: SINGLE_CHOICE
}

task subtask2 {
    description: "Das ist ein anderer Aufgabentext",
    answers: {
        A: 1,
        B: 2,
        C: 3
    },
    correct_answers: [answers.B, answers.C],
    type: MULTIPLE_CHOICE
}

task subtask3 {
    description: "Das ist ein ganz anderer Aufgabentext",
    text: "Dies ist ein Text mit <Lücken>, die gefüllt werden müssen."
    correct_answers: "REGEX", // diese Regex wird zur Überprüfung der Antwort in der Lücke ('<...>') verwendet
    type: TEXT
}

task subtask4 {
    description: "Das ist ein ganz anderer Aufgabentext",
    text: "Dies ist ein <Text> mit mehreren <Lücken>, die <gefüllt> werden müssen."
    correct_answers: ["REGEX", "OTHER_REGEX", "THIRD_REGEX"], // die erste Regex wird zur Überprüfung der ersten Lücke genutzt, die zweit für die zweite Lücke, usw.
    type: TEXT
}

// Definition von Task-Organisation über dot-Umgebung
graph task_order {
    TODO: Abbildung von Petri-Netzen über dot (wie genau?)
}

level_config my_config {
    task: task_order
}
```

## Definition von verschiedenen Szenarien

- Ziel: Randomisierung der Szenarien und verwendeten Spielmechaniken für einen bestimmten
  Aufgabentyp
- Mithilfe der
  [Taskbuilder-Methoden](https://github.com/Programmiermethoden/Dungeon/issues/197) können
  die Szenarien zusammengebaut werden (das ist allerdings bisher nur ein Konzept).
