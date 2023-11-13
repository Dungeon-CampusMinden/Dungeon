---
title: "DSL Quickstart Guide"
---

## Definition einer Aufgabe

Im folgenden Beispiel wird ein Beispiel für die Definition einer Single Choice Frage dargestellt:

```
// datei: example.dng

// Aufgabendefinition
single_choice_task meine_aufgabe {
    description: "Das ist der Aufgabentext",
    answers: [ "1", "2", "3" ],
    correct_answer_index: 2,
    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird",
    grading_function: single_choice_grading
}

// Definition von Aufgabenabhängigkeiten
graph task_graph {
    meine_aufgabe;
}

// Übergabe der Aufgabenabhängigkeit an das Dungeon-System
dungeon_config meine_config {
    dependency_graph: task_graph
}
```

Mit der `single_choice_task`-Definition wird eine neue Aufgabe definiert.
`single_choice_task` ist dabei der Typ der Aufgabendefinition und legt fest, welche weiteren Informationen
konfiguriert werden müssen (für weitere Aufgabentypen: siehe Doku zu Daten für Aufgabentypen - TODO).
`meine_aufgabe` ist ein frei wählbarer Name für die Definition und dient dazu,
die Aufgabendefinition im weiteren Verlauf der `.dng`-Datei zu referenzieren.
Dabei werden der Aufgabentext (`description`), die möglichen Antworten (als Liste von Strings, `answers`)
und die korrekte Antwort als
Index in die `answers`-Liste (`correct_answer_index`) übergeben, diese Informationen sind zwingend
für Aufgabendefinition nötig. Es kann außerdem eine Erklärung (`explanation`) angegeben werden, welche
den Spielenden gezeigt wird, falls sie die Aufgabe falsch beantworten. Per `grading_function` wird
die Bewertungsfunktion angegeben, welche die Bewertung der Aufgabe umsetzt. Für jede Aufgabenart
ist eine default-Bewertungsfunktion vorhanden, die verwendet wird, falls keine Bewertungsfunktion
explizit in der Aufgabendefinition durch Nutzende konfiguriert wird.

Die einzelne Aufgabendefinition wird noch nicht vom Dungeon-System eingelesen. Um eine Aufgabendefinition
an das Dungeon-System zu übergeben, muss sie in einem Abhängigkeitsgraph (`graph`) referenziert werden.
In einem Abhängigkeitsgraph können mehrere Aufgaben miteinander in Abhängigkeit zueinander gesetzt werden
(siehe dafür Doku Abhängigkeitsgraph - TODO). Soll eine Aufgabe keine Abhängigkeiten zu anderen Aufgaben
haben, reicht es aus, sie einfach mit ihrem Namen in der `graph`-Definition zu referenzieren.

Der Abhängigkeitsgraph muss anschließend noch in einer `dungeon_config`-Definition referenziert werden.
Diesen Definitionen stellen den "Einstiegspunkt" für das Dungeon-System dar.

TODO:
- Bilder, wie das dann nachher aussieht


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
