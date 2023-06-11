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

### Variablendefinition

Variablen haben einen Namen und können einen Wert annehmen. Variablen haben einen festen Datentyp, der ihnen
bei der Deklaration zugewiesen wird. Dieser Datentyp bestimmt, welche Werte eine Variable annehmen kann.
"Deklaration" bezeichnet das "Bekanntmachen" der Variable, "Definition"
bezeichnet die initiale Zuweisung eines Werts zu der Variablen.

Variablen haben eine Lebensdauer, je nachdem, in welchem Scope (siehe [semantische Analyse](semantische-analyse.md))
sie deklariert werden.
Variablen werden zerstört, wenn der Scope, in dem sie deklariert wurden, zerstört wird.
Variablen im globalen Scope "leben" dementsprechend für die gesamte Dauer der Laufzeit des DungeonDSL Programms (TODO: genau
dokumentieren, was das bedeutet).

Es gibt unterschiedliche Arten, Variablen zu deklarieren und zu definieren:
```
// kombinierte Deklaration und Definition
variablen_name = 42;
```

```
// Deklaration mit angabe des Datentyps
variablen_name : datentyp_name;
```

```
// NICHT zulässig
variablen_name;
```

```
variablen_name = complex_type {
    property1: expression1,
    property2: expression2
}
```

### Datentypen

Wie zuvor beschrieben, bestimmt der Datentyp einer Variablen, welche Werte der Variablen zugewiesen werden
können. Die DungeonDSL unterscheidet im Wesentlichen zwei Arten von Datentypen: primitive und komplexe Datentypen.

Die primitiven Datentypen sind:
- `int`: ganzzahlige Werte
- `float`: dezimale Werte
- `string`: Zeichenketten
- `bool`: Wahrheitswert, entweder `true` oder `false` (Note: das ist noch nicht implementiert)

Primitive Datentypen können zu komplexen Datentypen zusammengesetzt werden. Ein komplexer Datentyp hat
"Member", das sind Variablen, die nur im Kontext eines Datentypen gültig sind. Ein Beispiel für einen
komplexen Datentyp ist der `quest_config` Datentyp.

TODO: `entity_type`, `entity`, arrays hinzufügen

Für alle Datentypen (primitive und komplexe) können Listen erstellt werden, welche mehrere
Elemente eines Datentypen speichern können. Listen mit statischer Länge sind nicht vorgesehen,
Listen wachsen dynamisch.

Es existiert aktuell kein Mechanismus für DSL-Nutzende, abseits von
[Entitätsdefinitionen](#entitätstyp-definition), per DSL-Eingabe komplexe Datentypen zu erstellen.
Alle komplexen Datentypen werden in der Implementierung der DSL und des `DSLInterpreters` definiert, diese
Implementierung ist DSL-Nutzenden nicht zugänglich.

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

**inline Objektdefinition**

TODO: kommt mit [PR #272](https://github.com/Programmiermethoden/Dungeon/pull/272)

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

Als "Ausdruck" wird alles bezeichnet, was der `DSLInterperter` zu einem Wert evaluieren kann.
Folgende Ausdrücke werden unterstützt:

**Ganzzahliger Wert**

Ganzzahlige Werte haben in der DSL den `int` Datentyp.
```
property_name: 42
```

**Dezimalwert**

Dezimalwerte haben in der DSL den `float` Datentyp.
```
property_name: 3.14
```

**Zeichenketten (Strings)**

Zeichenketten (auch Strings genannt) mit beliebiger Länge haben in der DSL den `string` Datentyp.
```
property_name: "Hello, World!"
```

**Variablenname**

Wird der Name einer Variablen als Ausdruck verwendet, setzt der Interpreter an die Stelle des verwendeten
Variablennamens den Wert der Variablen ein.
```
property_name: id
```

**Funktionsaufrufe**

Ein Funktionsaufruf kann optional einen Wert zurückgeben (siehe [Funktionsdefinition](#funktionsdefinition)).
```
property_name: function(param1, param2)
```

Der `DSLInterpreter` evaluiert die Ausdrücke, die als Argumente (hier `param1` und `param2`) an die Funktion übergeben werden, führt die
Funktion aus und setzt den Rückgabewert der Funktion an der Stelle des Funktionsaufrufs ein.

**Funktionsreferenz**

Einige [Komponenten des ECS](../ecs/components/readme.md) bieten die Möglichkeit, Event-Handler Funktionen zu
registrieren, über die das Verhalten der Komponenten per DSL beeinflusst werden kann. Diese Event-Handler
Funktionen werden für die jeweilige Komponente als Reaktion auf das Eintreten eines bestimmten Falls aufgerufen.
```
// Funktion als Wert
property_name: function
```
Damit eine DSL Funktion als Event-Handler Funktion für ein bestimmtes Event verwendet werden kann, muss die
Funktionssignatur (also Datentypen und Anzahl der Parameter sowie Rückgabetyp) der DSL Funktion zu der
Signatur passen, die von der Komponente für diese Event-Handler Funktion erwartet wird.

**Listen-Zugriff**

Auf die Elemente einer Liste kann per `[]`-Operator und Index zugegriffen werden, dies könnte wie folgt
aussehen:

```
string_array_variable = ["a", "b", "c"];
string_variable = string_array_variable[0]
```

### Quest-Config

Die `quest_config`-Definition ist die zentrale Objektdefinition für ein DungeonDSL-Program. über die
Eigenschaften des `quest_config`-Objekts werden dem Dungeon-Framework alle nötigen Informationen übergeben,
die benötigt werden, um ein Level im Dungeon zu erstellen.

### Entitätstyp-Definition

Über eine Objektdefinition mit dem Datentypnamen `entity_type` können [Entitäten](../ecs/create_own_content.md)
definiert werden.

```
entity_type object_name {
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

### Aufgabentypbezogene Definitionen

**Ersetzung**

Sortierte Menge:

```
replacement_task t {
  elements: (
    n1: ["elem1", "elem2"]
  )
}
```

Note: die `{` und `}` direkt hinter `elements:` kennzeichnen ebenfalls eine unsortierte Menge, d.h. Mengen
können andere Mengen enthalten. Die Elemente einer Menge können benannt sein.
Problem: Wie das syntaktisch von den `entity_type`-Definitionen abgrenzen?
Yet another Problem: Wie Mengen von Mengen von Mengen im Typsystem abbilden? Eine Menge ist ja irgendwie
eine eigene Kategorie `IType`, die einen weiteren, "zugrundeliegenden" `IType` hat.

Idee:
- `[x, y, z]` könnte eine sortierte Menge sein (halt eine Liste, mit optional benannten Elementen)
- `(x, y, z)` könnte eine unsortierte Menge sein (ein Set), optional mit benannten Elementen

**Frage:** Was macht hier wirklich den Unterschied aus? Eigentlich muss nur gespeichert im Datentyp
gespeichert werden, ob die Reihenfolge relevant ist, oder nicht.

Benannte sortierte Menge:

```
replacement_task t {
  elements: (
    n1: ["elem1", "elem2"]
  )
}
```

Unsortierte Menge:

```
replacement_task t {
  elements: (
    ("elem1", "elem3")
  )
}
```

Benannte unsortierte Menge:

```
replacement_task t {
  elements: (
    n2: ("elem1", "elem3")
  )
}
```

"Regel":

```
rules: {
  r1: n1 -> n3,
}
```

**Kombination**

```
mapping_task t {
  mapping: {
  // Definition Zuordnung - (<term>, <definition>)
  ["a", "b"],

  // Hinzufügen von zusätzlichem Term
  ["c", _],

  // Hinzufügen von zusätzlicher Definition
  [_, "w"]
}
```

**Lücken**

### Taskdefinition

### Funktionsdefinition

TODO:
- Überladung erlauben?

Funktionen bieten die Möglichkeit, Anweisungen zu kapseln und in Abhängigkeit von Parametern
bestimmtes Verhalten zu implementieren (beispielsweise native Dungeon-Funktionen aufzurufen).

Beispiel für eine simple Funktion ohne Parameter und ohne Rückgabewert:
```
fn my_function() {
    print("Hello, World!");
}
```

Das Keyword, um eine Funktionsdefinition zu kennzeichnen, ist `fn`. Anschließend folgt der Name der
Funktion (`my_function`), die Parameterlist in runden Klammern (hier leer), ein optionaler Rückgabewert und
die Anweisungsliste innerhalb von geschweiften Klammern. Die Anweisungen müssen mit einem `;` beendet werden.

**Parameter**

Beispiel für eine Funktion mit Parametern:
```
fn function_with_parameters(int number, string text) {
    print(text);
    print(number);
}
```

Parameter werden als Kombination aus Datentyp (z.B. `int`) und Namen (z.B.`number`) definiert und können
einer Funktion beim Aufruf übergeben werden.

```
// ...

// Funktionsaufruf mit Argumenten
function_with_parameters(42, "Hello, World!");

// ...
```

Die Ausgabe (TODO: beschreiben, wo die landet) dieses Funktionsaufrufs sieht wie folgt aus:

```
> "Hello, World!"
> 42
```

**Rückgabewert**

Beispiel für eine Funktion mit Rückgabewert:
```
fn function_with_return_value() -> string {
    "Dieser String wird der Rückgabewert"
}
```

Der Datentyp des Rückgabewerts muss hinter einem `->` angegeben werden (hier `string`).
Der letzte Ausdruck innerhalb der Anweisungsliste der Funktion wird implizit als Rückgabewert behandelt
(hier `"Dieser String wird der Rückgabewert"`).

Der Rückgabewert wird bei einem Funktionsaufruf von der Funktion zurückgegeben und kann als Ausdruck
weiterverwendet werden, wie in folgendem Beispiel zu sehen:

```
// ...
my_variable = function_with_return_value();
print(my_variable);
// ...
```

Die Ausgabe für diese Anweisungen ist:

```
> "Dieser String wird der Rückgabewert"
```

### Import von anderen DSL-Files

```
#import <relative_file_path.dsl/name_of_thing_to_import>

Bspw.
#import <my_entity_types.dsl/question_monster>
#import <my_function_definitions.dsl/evaluation_thingy>
```

Gedanken dazu:
- brauchen wir namespaces?
- soll einfach alles aus einer Datei in die aktuelle Datei inkludiert
  werden?
  - Das würde bedeuten, dass ich mir das auch verketten kann, also
    indem ich in Datei1 Datei2 inkludiere, und in Datei3 Datei2 inkludiere,
    automatisch Datei1 in Datei3 inkludiert habe
- wie mit überladenen Funktionen umgehen? einfach alle mit dem gleichen
  Namen laden?
