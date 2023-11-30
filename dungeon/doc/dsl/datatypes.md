---
title: "Datentypen der DungeonDSL"
---

## Datentypen der DungeonDSL

Die DungeonDSL verwendet ein statisches Typsystem. Das bedeutet, dass für jeden Wert und
jede Variable bereits vor der Ausführung feststeht, welchen Datentyp sie hat.
Der Datentyp einer Variable oder eines Werts bestimmt, welche Operationen auf ihn angewandt werden
können.

## Basisdatentypen

Basisdatentypen sind die Datentypen, die nur zur Abbildung eines einzelnen Werts genutzt werden.

### bool

`bool`-Werte stellen Wahrheitswerte (`true` oder `false`) dar:

```
var my_bool : bool;
my_bool = true;
```

Default-Wert von `bool`-Variablen ist `false`.

### int

`int`-Werte stellen ganzzahlige Wert dar:

```
var my_int : int;
my_int = 42;
```

Default-Wert von `int`-Variablen ist `0`.

### float

`float`-Werte stellen Dezimalwerte dar:

```
var my_float : float;
my_float = 3.14;
```

Default-Wert von `float`-Variablen ist `0.0`.

### string

`string`-Werte stellen Zeichenketten dar:

```
var my_string : sring;
my_string = "Hello, World!";
```

Default-Wert von `string`-Variablen ist `""`, also eine leere Zeichenkette (mit keinen Zeichen).

### graph

`graph`-Werte stellen Aufgabenabhängikeiten dar. Obwohl die Definition von `graph`-Objekten nur
im globalen Scope möglich und komplexer als z. B. die Definition einer `string`-Variablen ist,
werden `graph`-Variablen als atomar, wie ein einzelner Wert betrachtet.

```
graph morning_graph {
    task1 -> task2 [type=seq];
    task2 -> task3 [type=c_f];
}
```

Default-Wert von `graph`-Variablen ist ein leerer Graph, der keine Knoten und keine Kanten enthält.

## Mengen-Datentypen

Mengen-Datentypen bilden Mengen von Werten ab, welche wiederum einen eigenen Datentypen haben
Die Mengen-Datentypen der DungeonDSL sind: Listen (sortierte Mengen), Sets (unsortierte Mengen)
und Maps (Abbildungen).

### Listen

Die Definition eines Listen-Datentypen sieht wie folgt aus:

```
var string_list : string[];
```

Dies ist so zu lesen, dass `string_list` eine Liste von `string`-Elementen darstellt.

Werte mit einem Listen-Datentyp erlauben folgende Operationen:

**add**

Mit der `add`-Operation können Elemente zu der Liste hinzugefügt werden:

```
string_list.add("hello");
string_list.add("world");
string_list.add("!");
```

Die Elemente werden der Reihe nach hinzugefügt, d. h. nach dem obigen Code-Snippet befinden
sich die Elemente `"hello"`, `"world"` und `"!"` in genau dieser Reihenfolge in der Liste `string_list`.

**get**

Mit der `get`-Operation kann ein Element aus der Liste per Index (`0`-basiert) abgefragt werden.

```
var my_string: string;
my_string = string_list.get(0)
```

`my_string` hat im Anschluss an das Snippet den Wert `"hello"`.

**size**

Mit der `size`-Operation kann die Menge der in einer Liste enthaltenen Elemente abgefragt werden:

```
var string_list : string[];

string_list.add("hello");
string_list.add("world");
string_list.add("!");

var list_size : int;
list_size = string_list.size();
```

`list_size` hat im Anschluss an das Snippet den Wert `3`.

**iteration**

Mit einer [foreach-Schleife](sprachkonzepte.md#schleifen) kann über die Elemente einer Liste iteriert
werden:

```
for string element in string_list {
    print (element);
}
```

Obiges Snippet erzeugt folgende Ausgabe:

```
> "hello"
> "world"
> "!"
```

### Sets

Die Definition eines Set-Datentypen sieht wie folgt aus:

```
var int_set : int<>;
```

Dies ist so zu lesen, dass `int_set` eine Menge von `int`-Elementen darstellt. Dabei
kann jedes Element nur einmal in der Menge vorkommen.

Werte mit einem Set-Datentyp erlauben folgende Operationen:

**add**

Mit der `add`-Operation können Elemente zu der Liste hinzugefügt werden:

```
int_set.add(1);
int_set.add(2);
int_set.add(2);
```

`int_set` enthält anschließend die beiden Werte `1` und `2`. Der letzte `add`-Aufruf wird
ignoriert, da zu diesem Zeitpunkt bereits der Wert `2` in der Menge enthalten ist.


**contains**

Mit der `contains`-Operation kann abgefragt werden, ob ein Element in dem Set enthalten ist.

```
var contains1 : bool;
var contains3 : bool;
contains1 = int_set.contains(1)
contains3 = int_set.contains(3)
```

`contains1` ist im Anschluss `true`, `contains3` ist false.

**size**

Mit der `size`-Operation kann die Menge der in einem Set enthaltenen Elemente abgefragt werden:

```
var int_set : int<>;

int_set.add(1);
int_set.add(2);
int_set.add(3);
int_set.add(3); // wird ignoriert

var set_size : int;
set_size = int_set.size();
```

`list_size` hat im Anschluss an das Snippet den Wert `3`.


**iteration**

Mit einer [foreach-Schleife](sprachkonzepte.md#schleifen) kann über die Elemente eines Sets iteriert
werden:

```
for int element in int_set {
    print (element);
}
```

Obiges Snippet erzeugt folgende Ausgabe:

```
> "2"
> "3"
> "1"
```

### Maps

Die Definition eines Map-Datentypen sieht wie folgt aus:

```
var int_to_string_map : [int -> string];
```

Dies ist so zu lesen, dass `int_to_string_map` eine Abbildung von `int` Werten auf `string` Werte
darstellt. `int` ist dabei der "Key" oder "Schlüssel"-Datentyp, `string` ist der "Element"-Datentyp.
Jeder Schlüsselwert kann nur einmal in einer Map enthalten sein.

Werte mit einem Set-Datentyp erlauben folgende Operationen:

**add**

Mit der `add`-Operation können Key/Element-Paare zu der Map hinzugefügt werden:

```
int_to_string_map.add(1, "hello");
int_to_string_map.add(5, "world");
int_to_string_map.add(3, "hello");
```

`int_to_string_map` enthält anschließend die drei Paare `1 -> "hello"`, `2 -> "world"` und
`3 -> "hello"`.

**get_keys**

Mit der `get_keys`-Operation kann eine Liste der enthaltenen Key-Werte abgefragt werden.

```
var keys : int[];
keys = int_to_string_map.get_keys();
```

`keys` enthält im Anschluss die Werte `1`, `5` und `3`, wobei die Reihenfolge nicht garantiert ist.

**get_elements**

Mit der `get_elements`-Operation kann eine Liste der enthaltenen Element-Werte abgefragt werden.

```
var elements : int[];
elements = int_to_string_map.get_elements();
```

`elements` enthält im Anschluss die Werte `"hello"`, `"world"` und `"hello`", wobei die Reihenfolge nicht garantiert ist.

## Komplexe Datentypen

Komplexe Datentypen bündeln mehrere Werte anderer Datentypen und haben benannte "Member".
Auch alle Datentypen, die zur Kommunikation mit dem Spielszenario dienen, werden als komplexe
Datentypen bezeichnet.

Ein Beispiel für komplexe Datentypen sind die [Aufgabendefinitionen](task_definition.md),
z. B. Single Choice Aufgaben:

```
single_choice_task my_task {
    description: "Dies ist der Aufgabentext",
    answers: ["Antwort1", "Antwort2", "Antwort3"],
    correct_answer_index: 1,
    scenario_builder: my_scenario_builder
}
```

Auf die Member eines Werts eines komplexen Datentyps kann mit dem `.`-Operator zugegriffen werden.
Hierbei gilt allerdings die Einschränkung, dass so nicht auf **alle** Eigenschaften zugegriffen werden
kann (aufgrund von technischen Einschränkungen).

### Allgemeine komplexe Datentypen

#### dungeon_config

Member per `.`-Operator zugreifbar:

| Member             | Datentyp            |
|--------------------|---------------------|
| `dependency_graph` | [`graph`](#graph)   |
| `name`             | [`string`](#string) |

### Aufgabendefinition Datentypen

#### task_content

Stellt einen Teil einer Lösung einer Aufgabe dar.

Methoden:

| Methoden | Beschreibung                               | Rückgabetyp         | Parameter |
|----------|--------------------------------------------|---------------------|-----------|
| `text`   | Den Inhalt des `task_content` als `string` | [`string`](#string) | -         |

#### element

Stellt einen Teil einer Lösung einer [Zuordnungsaufgabe](task_definition.md#zuordnungsaufgaben) dar.

Methoden:

| Methoden   | Beschreibung                            | Rückgabetyp     | Parameter |
|------------|-----------------------------------------|-----------------|-----------|
| `is_empty` | Ist das Element das "empty" Element `_` | [`bool`](#bool) | -         |

#### single_choice_task

Siehe [Aufgabendefinition: Single Choice](task_definition.md#single-choice-aufgaben).

Member per `.`-Operator zugreifbar:

| Member        | Datentyp            |
|---------------|---------------------|
| `description` | [`string`](#string) |


Methoden:

| Methoden                      | Beschreibung                                                                                    | Rückgabetyp                      | Parameter                                        |
|-------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------|--------------------------------------------------|
| `get_content`                 | Gibt die Antwortoptionen als [`task_content`](#taskcontent)-Liste zurück                        | [`task_content[]`](#taskcontent) | -                                                |
| `set_scenario_text`           | Setzt die [Szenariobeschreibung](scenario_builder.md#szenario-spezifische-aufgabenbeschreibung) | -                                | [`string`](#string)                              |
| `set_grading_function`        | Setzt die [Bewertungsfunktion](task_definition.md#single-choice-aufgaben)                       | -                                | `fn (single_choice_task, task_content) -> float` |
| `set_answer_picking_function` | Setzt die [Antwort-Auswahl-Funktion](scenario_builder.md#antwort-auswahl-funktion)              | -                                | `fn (single_choice_task) -> task_content`        |
| `is_active`                   | Ist diese Aufgabe aktuell aktiv zur Bearbeitung freigeschaltet?                                 | [`bool`](#bool)                  | -                                                |

#### multiple_choice_task

Siehe [Aufgabendefinition: Multiple Choice](task_definition.md#multiple-choice-aufgaben).

Member per `.`-Operator zugreifbar:

| Member        | Datentyp            |
|---------------|---------------------|
| `description` | [`string`](#string) |


Methoden:

| Methoden                      | Beschreibung                                                                                    | Rückgabetyp                      | Parameter                                          |
|-------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------|----------------------------------------------------|
| `get_content`                 | Gibt die Antwortoptionen als [`task_content`](#taskcontent)-Liste zurück                        | [`task_content[]`](#taskcontent) | -                                                  |
| `set_scenario_text`           | Setzt die [Szenariobeschreibung](scenario_builder.md#szenario-spezifische-aufgabenbeschreibung) | -                                | [`string`](#string)                                |
| `set_grading_function`        | Setzt die [Bewertungsfunktion](task_definition.md#multiple-choice-aufgaben)                     | -                                | `fn (multiple_choice_task, task_content) -> float` |
| `set_answer_picking_function` | Setzt die [Antwort-Auswahl-Funktion](scenario_builder.md#antwort-auswahl-funktion)              | -                                | `fn (multiple_choice_task) -> task_content`        |
| `is_active`                   | Ist diese Aufgabe aktuell aktiv zur Bearbeitung freigeschaltet?                                 | [`bool`](#bool)                  | -                                                  |

#### assign_task

Siehe [Aufgabendefinition: Zuordnung](task_definition.md#zuordnungsaufgaben).

Member per `.`-Operator zugreifbar:

| Member | Datentyp |
|--------|----------|
| -      | -        |


Methoden:

| Methoden                      | Beschreibung                                                                                    | Rückgabetyp                        | Parameter                                 |
|-------------------------------|-------------------------------------------------------------------------------------------------|------------------------------------|-------------------------------------------|
| `get_solution`                | Gibt die definierte Lösungsmenge als [`[task_content -> taskcontent]`](#taskcontent)-Map zurück | [`[element -> element]`](#element) | -                                         |
| `set_scenario_text`           | Setzt die [Szenariobeschreibung](scenario_builder.md#szenario-spezifische-aufgabenbeschreibung) | -                                  | [`string`](#string)                       |
| `set_grading_function`        | Setzt die [Bewertungsfunktion](task_definition.md#zuordnungsaufgaben)                           | -                                  | `fn (assign_task, task_content) -> float` |
| `set_answer_picking_function` | Setzt die [Antwort-Auswahl-Funktion](scenario_builder.md#antwort-auswahl-funktion)              | -                                  | `fn (assign_task) -> task_content`        |
| `is_active`                   | Ist diese Aufgabe aktuell aktiv zur Bearbeitung freigeschaltet?                                 | [`bool`](#bool)                    | -                                         |

### Szenario-Spezifische Datentypen

#### entity

Repräsentiert eine Entität im Dungeon.

Member per `.`-Operator zugreifbar:

| Member                   | Datentyp                                          |
|--------------------------|---------------------------------------------------|
| `velocity_component`     | [`velocity_component`](#velocitycomponent)        |
| `position_component`     | [`position_component`](#positioncomponent)        |
| `draw_component`         | [`draw_component`](#drawcomponent)                |
| `task_component`         | [`task_component`](#taskcomponent)                |
| `task_content_component` | [`task_content_component`](#taskcontentcomponent) |
| `inventory_component`    | [`inventory_component`](#inventorycomponent)      |
| `interaction_component`  | [`interaction_component`](#interactioncomponent)  |

Methoden:

| Methode                               | Beschreibung                                                                             | Rückgabetyp | Parameter           |
|---------------------------------------|------------------------------------------------------------------------------------------|-------------|---------------------|
| `mark_as_task_container_with_element` | Markiert eine Entität als Taskcontainer und verknüpft sie mit einem Element              | -           | (`task`, `element`) |
| `mark_as_task_container`              | Markiert eine Entität als Taskcontainer und setzt den Namen auf den übergebenen `string` | -           | (`task`, `string`)  |

#### quest_item

Repräsentiert ein QuestItem im Dungeon.

### Komponenten-Datentypen

#### position_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar: -

Methoden: -

#### velocity_component

Member in `entity_type`-Definition konfigurierbar:

| Member       | Beschreibung                           | Datentyp          |
|--------------|----------------------------------------|-------------------|
| `x_velocity` | Maximale Geschwindigkeit in x-Richtung | [`float`](#float) |
| `y_velocity` | Maximale Geschwindigkeit in y-Richtung | [`float`](#float) |

Member per `.`-Operator zugreifbar:

| Member       | Beschreibung                           | Datentyp          |
|--------------|----------------------------------------|-------------------|
| `x_velocity` | Maximale Geschwindigkeit in x-Richtung | [`float`](#float) |
| `y_velocity` | Maximale Geschwindigkeit in y-Richtung | [`float`](#float) |

Methoden: -

#### health_component

Member in `entity_type`-Definition konfigurierbar:

| Member         | Beschreibung                      | Datentyp     |
|----------------|-----------------------------------|--------------|
| `on_death`     | Event-Handler für Tod der Entität | `fn(entity)` |
| `max_health`   | Maximale Gesundheit der Entität   | `int`        |
| `start_health` | Start-Gesundheit der Entität      | `int`        |

Member per `.`-Operator zugreifbar:

| Member         | Beschreibung                      | Datentyp     |
|----------------|-----------------------------------|--------------|
| `on_death`     | Event-Handler für Tod der Entität | `fn(entity)` |
| `max_health`   | Maximale Gesundheit der Entität   | `int`        |
| `start_health` | Start-Gesundheit der Entität      | `int`        |

Methoden: -

#### ai_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar: -

Methoden: -

#### interaction_component

Member in `entity_type`-Definition konfigurierbar:

| Member           | Beschreibung                                                      | Datentyp          |
|------------------|-------------------------------------------------------------------|-------------------|
| `radius`         | Der Radius um die Entität, in dem mit ihr interagiert werden kann | [`float`](#float) |
| `on_interaction` | Event-Handler Funktion für Interakton                             | [`float`](#float) |

Member per `.`-Operator zugreifbar:

| Member           | Beschreibung                                                      | Datentyp          |
|------------------|-------------------------------------------------------------------|-------------------|
| `radius`         | Der Radius um die Entität, in dem mit ihr interagiert werden kann | [`float`](#float) |
| `on_interaction` | Event-Handler Funktion für Interakton                             | [`float`](#float) |

Methoden:

| Methoden                      | Beschreibung                                                                                    | Rückgabetyp                        | Parameter                                 |
|-------------------------------|-------------------------------------------------------------------------------------------------|------------------------------------|-------------------------------------------|
| `get_solution`                | Gibt die definierte Lösungsmenge als [`[task_content -> taskcontent]`](#taskcontent)-Map zurück | [`[element -> element]`](#element) | -                                         |

#### draw_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar: -

Methoden: -

#### task_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar:

| Member | Beschreibung                                                         | Datentyp                                                            |
|--------|----------------------------------------------------------------------|---------------------------------------------------------------------|
| `task` | Der mit dem `task_component` verküpfte task (die Aufgabendefinition) | `task`(`single_choice_task`, `multiple_choice_task`, `assign_task`) |

Methoden: -

#### task_content_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar:

| Member    | Beschreibung                                                  | Datentyp       |
|-----------|---------------------------------------------------------------|----------------|
| `content` | Der mit dem `task_content_component` verküpfte `task_content` | `task_content` |

Methoden: -

#### inventory_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar: -

Methoden:

| Methoden     | Beschreibung                                      | Rückgabetyp | Parameter                                            |
|--------------|---------------------------------------------------|-------------|------------------------------------------------------|
| `add_item`   | Fügt ein `quest_item` zu dem Inventar hinzu       | -           | `quest_item` (das Item, was hinzugefügt werden soll) |
| `open`       | Öffnet das Inventar als GUI                       | -           | `entity` (die Entity, welche das Inventar öffnet)    |
| `drop_items` | Lässt alle enthaltenen Items auf den Boden fallen | -           | -                                                    |

#### hitbox_component

Member in `entity_type`-Definition konfigurierbar: -

Member per `.`-Operator zugreifbar: -

Methoden: -
