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

Konzepte gesammelt:
- Graphdefintion
  - Geplant: Attributierung von Knoten,Kanten
- Game-Objekt definition
- Funktionsdefinition
- Objekt-Defintion
  - Propertydefinition
- Ausdrücke
  - Number
  - String
  - FunctionAsValue
- Level-Konfig / Quest-Konfig
- **geplant, noch nicht realisiert:**
  - Arrays
  - control flow
    - if/else
    - while
  - Variablen deklaration
  - Instanziierung von Entities per nativem Funktionsaufruf (dng.xyz)
  - Import von anderen DSL-Files (um Funktionen, Entity-Definition zu importieren)
  - Task-Definition
  - Enum-Variant Binding
  - Member-Zugriff über `.`-Operator

### Kommentare

Kommentare können an jeder Stelle in einer Dungeon DSL Datei eingefügt werden,
um das DSL Programm mit erklärenden Informationen zu versehen, die vom `DSLInterpreter`
ignoriert werden.

Kommentare können auf zwei unterschiedliche Weisen ausgeführt werden.

Einzeilig:
```
// einzeiliger Kommentar
```

Und mehrzeilig:
```
/*
mehrzeiliger
Kommentar
*/
```

### Allgemein: Datentypen

TODO: wo unterbringen?

### Objektdefinition

Note: Geht aktuell nur für `quest_config`.

Die Dungeon DSL erlaubt die Definition von Objekten. Dazu muss zuerst der Name des Objektdatentyps, anschließend
der Name des erstellten Objekts und abschließend eine Liste von Eigenschaftsdefinitionen angegeben werden.
Die Liste der Eigenschaftsdefinitionen muss von zwei geschweiften Klammern (`{` und `}`) umgeben sein und kann
leer sein.

Beispiel:
```
type_id object_id {}
```

Welche Eigenschaften ein Objekt hat und dementsprechend zwischen den geschweiften Klammern
definiert werden können, ist abhängig vom verwendeten Objektdatentyp.

Eigenschaften, welche nicht explizit per DSL definiert werden, werden auf den Defaultwert ihres Datentyps
gesetzt (siehe dazu [Defaultwerte](typsystem.md#defaultwerte)).

Mehrere Eigenschaftsdefinitionen müssen mit einem `,` getrennt werden.

```
type id {
    property1: value,
    property2: other_value
}
```

#### Eigenschaftsdefinition (Propertydefinition)

Eine Eigenschaftsdefinition besteht aus dem Namen der Eigenschaft, welche zugewiesen werden soll auf der
linken Seite (im Folgenden `property_name`) und dem Wert, der der Eigenschaft zugewiesen werden soll auf
der rechten Seite. Die linke und rechte Seite werden durch `:` getrennt.

```
// in Objekt-definition:
...
    property_name: zuzuweisender_ausdruck
```

Auf der rechten Seite muss ein Ausdruck stehen, welcher vom `DSLInterpreter` zu einem Wert evaluiert
werden kann (gültige Ausdrücke sind im Kapitel [Ausdrücke](#ausdrücke) dargestellt.)

### Ausdrücke

```
// ganzzahliger Wert
property_name: 42

// Dezimalwert
property_name: 3.14

// String (Zeichenkette)
property_name: "Hello, World!"

// Funktionsaufruf
property_name: function(param1, param2)

// Identifier
property_name: id

// Funktion als Wert
property_name: function
```

### Quest-Config

Die `quest_config`-Definition ist die zentrale Objektdefinition für ein DungeonDSL-Program. über die
Eigenschaften des `quest_config`-Objekts werden dem Dungeon-Framework alle nötigen Informationen übergeben,
die benötigt werden, um ein Level im Dungeon zu erstellen.

### Entitätsdefinition

Über eine Objektdefinition mit dem Datentypname `game_object` können [Entitäten](../ecs/create_own_content.md)
definiert werden.

```
game_object object_name {
    component1: {
        component1_property1: value,
        component1_property2: other_value
    },
    component2: {
        component2_property1: yet_another_value,
        component2_property2: completely_different_value
    }
}
```

Eine Entitätsdefinition verhält sich anders als eine Objektdefinition. Aus ihr wird nicht direkt ein Objekt
erzeugt (wie bei Objektdefinitionen). Eine Entitätsdefinition stellt zunächst nur eine "Blaupause" für ein
Objekt dar, welche explizit instanziiert werden muss. "Instanziierung" beschreibt das Erstellen eines konkreten
Objekts aus so einer Blaupause.

### Graphendefinition

Warum?
- Die Graphen-Datenstruktur bietet sich an, um bspw. die Raumaufteilung des Dungeons teilweise zu definieren

Wie sieht das aus?
- DOT syntax in einer `graph`-Umgebung

```
graph g {
    A -- B
    B -- C
    A -- D
}
```


### Taskdefinition

### Funktionsdefinition

Funktionen bieten die Möglichkeit, Anweisungen zu kapseln und in Abhängigkeit von Parametern
bestimmtes Verhalten zu implementieren.
