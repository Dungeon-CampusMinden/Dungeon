---
title: "DSL Überblick"
---

## Überblick: Ideen und Ziele

Die DSL ermöglicht…

- textuelle Beschreibung von abstrakten Aufgaben aus dem Umfeld der Lehre für Lehrpersonen,
  welche durch DSL-Interpreter in eine konkretes Szenario in einem Dungeon Level übersetzt
  werden
- die Organisation der Aufgabenfolge und Aufgabenverschachtelung
  - aufeinanderfolgende und parallele (Teil-)Aufgaben
- Konfiguration der Bewertung von Aufgaben
- (in einem festgelegten Rahmen) Einflussnahme auf das erstellte Szenario
- die Definition von Entitätstypen und Ausprägungen dieser Typen
- die Definition von Event-Handler Funktionen, um auf Events aus dem Dungeon-Umfeld zu
  reagieren und das Verhalten einzelner Komponenten zu bestimmen
- das Einbinden von Funktionen und Entitätstypen aus externen DSL-Dateien und einer
  Standardbibliothek

Ziel der DSL ist, dass Lehrpersonen ohne große Hürden ihre bereits vorhandenen Aufgaben in
ein Dungeon-Level übersetzen können. Ziel dabei ist es, dass sich Lehrpersonen nicht
klassischen Spieleentwicklungs-Problemen auseinandersetzen müssen und nicht jede Entität im
Dungeon selbst beschreiben müssen, sondern auf eine Reihe vorgefertigter Szenarien zugreifen
können. Diese Szenarien sind ebenfalls in der DSL umgesetzt und können aus der
Standardbibliothek eingebunden werden.

### Beispiel

Die folgende DSL-Eingabe zeigt eine Aufgabendefinition für eine Single Choice Frage mit drei
Antwortmöglichkeiten:

```
// file: single_choice_task.ds

task single_choice_task {
    text: "Welche Laufzeitkomplexität hat Heapsort?",
    answers: {
        A: O(n^2),
        B: O(n log n),
        C: O(n)
    },
    correct_answer: answers.B,
    type: SINGLE_CHOICE
}

level_config my_config {
    task: single_choice_task
}
```

Die eigentliche Aufgabendefinition wird in dem `task`-Objekt vorgenommen. Je nach
Aufgabentyp unterscheiden sich die hier nötigen Definitionen.

Über das `level_config`-Objekt wird die Aufgabendefinition dem DSL-Interpreter übergeben.
Dieser erstellt anschließend aus der Aufgabendefinition ein konkretes Szenario in einem
Dungeon-Level.

Die Aufgabenbeschreibung könnte von einem Zauberer-NPC vorgelesen werden, sobald der
Spielcharakter mit ihm interagiert. Hierbei könnten die Antwortmöglichkeiten
(`answers {A:..., B:..., C:...}`) auf “Schriftrollen” abgebildet sein, welche im Level
versteckt sind und erst vom Spielenden gefunden werden müssen. Die Beantwortung der Frage
erfolgt durch das Übergeben der Schriftrolle mit der korrekten Antwort an den Zauberer-NPC.
Dieser gibt anschließend Feedback über die Korrektheit der Antwort.

## Quickstart Guide und User-Dokumentation

Der [Quickstart Guide](quickstart.md) gibt Hinweise für die Erstellung der ersten Aufgaben
mit der DungeonDSL.

Die folgenden Dokumentationsseiten beleuchten einzelne Aspekte der Nutzung der DungeonDSL
detaillierter als der Quickstart Guide:

- [Sprachkonzepte der DungeonDSL im Detail](sprachkonzepte.md)
- [Typsystem der DungeonDSL](typsystem.md)

## Technische Dokumentation

- [Einordnung der DSL-Interpreters in das Gesamtsystem](schnittstellen.md)
- [Typebuilding - Erweiterung des DungeonDSL
  Typsystems](https://github.com/Programmiermethoden/Dungeon/wiki/Typebuilding)
- [Interpretation und Laufzeit eines DungeonDSL Programms](interpretation-laufzeit.md)
- [Aktueller Entwicklungsstand der DungeonDSL](status.md)
