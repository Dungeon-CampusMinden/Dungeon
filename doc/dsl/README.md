---
title: "DSL Überblick"
---

## Überblick: Ideen und Ziele

Die DSL ermöglicht...
- textuelle Beschreibung von abstrakten Aufgaben aus dem Umfeld der Lehre für Lehrpersonen, welche durch DSL-Interpreter in eine konkretes Szenario in einem Dungeon Level übersetzt werden
- die Organisation der Aufgabenfolge und Aufgabenverschachtelung
    - aufeinanderfolgende und parallele (Teil-)Aufgaben
- Konfiguration der Bewertung von Aufgaben
- (in einem festgelegten Rahmen) Einflussnahme auf das erstellte Szenario
- die Definition von Entitätstypen und Ausprägungen dieser Typen
- die Definition von Event-Handler Funktionen, um auf Events aus dem Dungeon-Umfeld
    zu reagieren und das Verhalten einzelner Komponenten zu bestimmen
- das Einbinden von Funktionen und Entitätstypen aus externen DSL-Dateien und einer Standardbibliothek

Ziel der DSL ist, dass Lehrpersonen ohne große Hürden ihre bereits vorhandenen
Aufgaben in ein Dungeon-Level übersetzen können. Ziel dabei ist es, dass sich
Lehrpersonen nicht klassischen Spieleentwicklungs-Problemen auseinandersetzen müssen und
nicht jede Entität im Dungeon selbst beschreiben müssen, sondern auf eine Reihe
vorgefertigter Szenarien zugreifen können. Diese Szenarien sind ebenfalls in der DSL
umgesetzt und können aus der Standardbibliothek eingebunden werden.

### Beispiel

Die folgende DSL-Eingabe zeigt eine Aufgabendefinition für eine Single Choice Frage mit
drei Antwortmöglichkeiten:

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
Dieser erstellt anschließend aus der Aufgabendefinition ein konkretes Szenario in
einem Dungeon-Level.

**Ein Beispiel für ein konkretes Szenario für die obige Aufgabe:**

Die Aufgabenbeschreibung könnte von einem Zauberer-NPC vorgelesen werden, sobald der Spielcharakter mit
ihm interagiert.
Hierbei könnten die Antwortmöglichkeiten (`answers {A:..., B:..., C:...}`)
auf "Schriftrollen" abgebildet sein, welche im Level versteckt sind und erst vom
Spielenden gefunden werden müssen. Die Beantwortung der Frage erfolgt durch das Übergeben
der Schriftrolle mit der korrekten Antwort an den Zauberer-NPC. Dieser gibt anschließend
Feedback über die Korrektheit der Antwort.

## Konzepte und Begriffe

### Taskdefinition

Die Taskdefinition...
- ist die zentrale Definition für eine Aufgabe aus dem Umfeld der Lehre
- enthält die Aufgabenbeschreibung
- hat einen Aufgabentyp (siehe [Aufgabentyp](doc/tasks/README.md#Aufgabentypen), der einen großen Einfluss auf die Bewertung der Aufgabe // Note: Link ist von [PR #444](https://github.com/Programmiermethoden/Dungeon/pull/444) abhängig

### Taskorganisation

Eine Taskorganisation...
- modelliert die Organisation mehrerer Aufgaben untereinander (Sequenz, Parallelismus, Verschachtelung)
- orientiert sich konzeptionell an Petri-Netzen
- legt Bedingungen für die Freischaltung von Aufgaben (Aufgabe kann bearbeitet werden) und den
    Abschluss von Aufgaben fest (falls eine Aufgabe Unteraufgaben hat, gilt sie erst
    als abgeschlossen, falls eine bestimmte Anzahl an Unteraufgaben abgeschlossen ist)

### Level/Quest-Config

Eine Level/Quest-Config (der Name ist noch nicht final festgelegt):
- beschreibt, welche Aufgaben in einem Level des Dungeons geladen werden
- beschreibt, welche Szenarien für die Umsetzung der Aufgaben im Level verwendet
    werden (vgl. [Szenarien](doc/tasks/README.md#Szenarien) // Note: Link ist von [PR #444](https://github.com/Programmiermethoden/Dungeon/pull/444) abhängig


### Entitätstyp-Definition

Eine Entitätstyp-Definition:
- legt fest, welche [Komponenten](../doc/ecs/components/readme.md) in einem Entitätstyp enthalten sind
- bietet die Möglichkeit, die Werte der Komponenten zu definieren
- kann weitergehen konfiguriert werden, d.h. die Werte der Komponenten einer bestehende Entitätstyp-Definition verändert und überschrieben werden, sodass eine konkrete Ausprägung eines Entitätstyps entsteht

### Event-Handler-Funktion

Event-Handler-Funktionen bieten die Möglichkeit:
- auf eine bestimmtes Ereignis aus dem Kontext des Dungeon-Levels oder einer Komponente zu reagieren (bspw.: "eine andere Hitbox kollidiert mit der Hitbox des `CollisionComponent` dieser Entität")
- Parameter anzunehmen, welche den Kontext des Ereignisses beschreiben
- im Rumpf Logik zu implementieren, welche native Funktionen aufrufen, bedingte Programmverzweigungen realisieren und auf Objekte des Dungeons zugreifen kann, um bestimmtes Verhalten zu erzeugen (bspw.: "Wenn die Entität der Hitbox mit der ich kollidiere ein Item ist, dann hebe ich das Item auf")

### Taskbuilder-Methode

Die Taskbuilder-Methoden sind dafür zuständig, eine abstrakte Aufgabendefinition in ein konkretes Szenario im Dungeon Level zu übersetzen. Sie bekommen als Parameter die Taskdefinition übergeben und müssen eine Menge Entitäten zurückgeben, welche an dem Szenario beteiligt sind.

## Quickstart Guide und User-Dokumentation

Der [Quickstart Guide](quickstart.md) gibt Hinweise für die Erstellung der ersten Aufgaben mit der DungeonDSL.

Die folgenden Dokumentationsseiten beleuchten einzelne Aspekte der Nutzung der DungeonDSL detaillierter als der Quickstart Guide:
- [Sprachkonzepte der DungeonDSL im Detail](sprachkonzepte.md)
- [Typsystem der DungeonDSL](typsystem.md)

## Technische Dokumentation

- [Einordnung der DSL-Interpreters in das Gesamtsystem](schnittstellen.md)
- [Typebuilding - Erweiterung des DungeonDSL Typsystems](https://github.com/Programmiermethoden/Dungeon/wiki/Typebuilding)
- [Interpretation und Laufzeit eines DungeonDSL Programms](interpretation-laufzeit.md)
- [Aktueller Entwicklungsstand der DungeonDSL](status.md)
