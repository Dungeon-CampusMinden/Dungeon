---
title: "Sprachkonzepte der DungeonDSL"
---

## High-Level Konzepte und Begriffe

### Taskdefinition

Die Taskdefinition…

- ist die zentrale Definition für eine Aufgabe aus dem Umfeld der Lehre
- enthält die Aufgabenbeschreibung
- hat einen Aufgabentyp (siehe [Aufgabentyp](doc/tasks/README.md#Aufgabentypen), der einen
  großen Einfluss auf die Bewertung der Aufgabe // Note: Link ist von [PR
  \#444](https://github.com/Programmiermethoden/Dungeon/pull/444) abhängig

### Taskorganisation

Eine Taskorganisation…

- modelliert die Organisation mehrerer Aufgaben untereinander (Sequenz, Parallelismus,
  Verschachtelung)
- orientiert sich konzeptionell an Petri-Netzen
- legt Bedingungen für die Freischaltung von Aufgaben (Aufgabe kann bearbeitet werden) und
  den Abschluss von Aufgaben fest (falls eine Aufgabe Unteraufgaben hat, gilt sie erst als
  abgeschlossen, falls eine bestimmte Anzahl an Unteraufgaben abgeschlossen ist)

### Level/Quest-Config

Eine Level/Quest-Config (der Name ist noch nicht final festgelegt):

- beschreibt, welche Aufgaben in einem Level des Dungeons geladen werden
- beschreibt, welche Szenarien für die Umsetzung der Aufgaben im Level verwendet werden
  (vgl. [Szenarien](doc/tasks/README.md#Szenarien) // Note: Link ist von [PR
  \#444](https://github.com/Programmiermethoden/Dungeon/pull/444) abhängig

### Entitätstyp-Definition

Eine Entitätstyp-Definition:

- legt fest, welche [Komponenten](../doc/ecs/components/readme.md) in einem Entitätstyp
  enthalten sind
- bietet die Möglichkeit, die Werte der Komponenten zu definieren
- kann weitergehen konfiguriert werden, d.h. die Werte der Komponenten einer bestehende
  Entitätstyp-Definition verändert und überschrieben werden, sodass eine konkrete Ausprägung
  eines Entitätstyps entsteht

### Event-Handler-Funktion

Event-Handler-Funktionen bieten die Möglichkeit:

- auf eine bestimmtes Ereignis aus dem Kontext des Dungeon-Levels oder einer Komponente zu
  reagieren (bspw.: “eine andere Hitbox kollidiert mit der Hitbox des `CollisionComponent`
  dieser Entität”)
- Parameter anzunehmen, welche den Kontext des Ereignisses beschreiben
- im Rumpf Logik zu implementieren, welche native Funktionen aufrufen, bedingte
  Programmverzweigungen realisieren und auf Objekte des Dungeons zugreifen kann, um
  bestimmtes Verhalten zu erzeugen (bspw.: “Wenn die Entität der Hitbox mit der ich
  kollidiere ein Item ist, dann hebe ich das Item auf”)

### Taskbuilder-Methode

Die Taskbuilder-Methoden sind dafür zuständig, eine abstrakte Aufgabendefinition in ein
konkretes Szenario im Dungeon Level zu übersetzen. Sie bekommen als Parameter die
Taskdefinition übergeben und müssen eine Menge Entitäten zurückgeben, welche an dem Szenario
beteiligt sind.

## Sprachkonzepte

- Für jedes Konzept:
  - Was genau beschreibt das Konzept (was *tut* die Sprache)?
  - Warum ist das wichtig?
  - Wie sieht das in der DSL-Syntax aus?

## Graphendefinition

## Propertydefinition

## Entitätsdefinition

## Level-Config

## Taskdefinition

## Funktionsdefinition

