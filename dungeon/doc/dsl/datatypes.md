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

**bool**:

`bool`-Werte stellen Wahrheitswerte (`true` oder `false`) dar:

```
var my_bool : bool;
my_bool = true;
```

Default-Wert von `bool`-Variablen ist `false`.

**int**:

`int`-Werte stellen ganzzahlige Wert dar:

```
var my_int : int;
my_int = 42;
```

Default-Wert von `int`-Variablen ist `0`.

**float**:

`float`-Werte stellen Dezimalwerte dar:

```
var my_float : float;
my_float = 3.14;
```

Default-Wert von `float`-Variablen ist `0.0`.

**string**:

`string`-Werte stellen Zeichenketten dar:

```
var my_string : sring;
my_string = "Hello, World!";
```

Default-Wert von `string`-Variablen ist `""`, also eine leere Zeichenkette (mit keinen Zeichen).

**graph**:

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

### Komplexe Datentypen

Komplexe Datentypen bündeln mehrere Werte anderer Datentypen und haben benannte "Member".

Ein Beispiel für
