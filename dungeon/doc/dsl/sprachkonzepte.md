---
title: "Sprachkonzepte der DungeonDSL"
---

## Sprachkonzepte

### Kommentare

Kommentare können an jeder Stelle in einer DungeonDSL Datei eingefügt werden,
um das DSL Programm mit erklärenden Informationen zu versehen, die vom `DSLInterpreter`
ignoriert werden.

Kommentare können auf zwei unterschiedliche Weisen ausgeführt werden.

Einzeiliger Kommentar:
```
// einzeiliger Kommentar
```

Mehrzeilige Kommentare:
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

Variablen haben eine Lebensdauer, je nachdem, in welchem Scope
sie deklariert werden.
Variablen werden zerstört, wenn der Scope, in dem sie deklariert wurden, zerstört wird.

Es gibt unterschiedliche Arten, Variablen zu deklarieren und zu definieren:
```
// kombinierte Deklaration und Definition
variablen_name = 42;

// Deklaration mit angabe des Datentyps
variablen_name : datentyp_name;

// NICHT zulässig
variablen_name;

variablen_name = complex_type {
  property1: expression1,
  property2: expression2
}
```

### Datentypen

Der Datentyp einer Variablen bestimmt, welche Werte der Variablen zugewiesen werden
können. Die DungeonDSL unterscheidet im Wesentlichen zwei Arten von Datentypen:
primitive und komplexe Datentypen.

Die primitiven Datentypen sind:

- `int`: ganzzahlige Werte
- `float`: dezimale Werte
- `string`: Zeichenketten
- `bool`: Wahrheitswert, entweder `true` oder `false`

Primitive Datentypen können zu komplexen Datentypen zusammengesetzt werden. Ein komplexer Datentyp hat
"Member", das sind Variablen, die nur im Kontext eines Datentypen gültig sind. Ein Beispiel für einen
komplexen Datentyp ist sind die `level_config`, `entity_type` und `entity` Datentypen.

#### Mengen

Für alle Datentypen (primitive und komplexe) können sortierte Mengen (Listen) und
unsortierte Mengen erstellt werden, welche mehrere Elemente eines Datentypen speichern können.
Listen mit statischer Länge sind nicht vorgesehen, Listen wachsen dynamisch.
Dass es sich bei einem Objekt oder einer Variable um eine Menge handelt, wird ebenfalls im Datentyp
abgebildet.

**Sortierte Menge (Liste):**

```
list = [ "Hello", "World", "!" ];
```

**Unsortierte Menge:**

```
set = < "elem1", "elem2" >;
```

**Mengen in Mengen:**

Mengen können andere Mengen enthalten:

```
set = <
  ["elem1", "elem2"],
  ["elem3", "elem4"]
>;
```

Es existiert aktuell kein Mechanismus für DSL-Nutzende, abseits von
Entitätstypen, per DSL-Eingabe selbst komplexe Datentypen zu erstellen.
Alle komplexen Datentypen werden in der Implementierung der DSL und des `DSLInterpreters` definiert, diese
Implementierung ist DSL-Nutzenden nicht zugänglich.

#### Objektdefinition

Die DungeonDSL erlaubt die Definition von Objekten. Zuerst muss der Name des Objektdatentyps, anschließend
der Name des erstellten Objekts und abschließend eine Liste von Eigenschaftsdefinitionen angegeben werden.
Die Liste der Eigenschaftsdefinitionen muss von zwei geschweiften Klammern (`{` und `}`) umgeben sein und kann
leer sein.

Beispiel:
```
type_id object_id {}
```

##### Eigenschaftsdefinition (Propertydefinition)

Welche Eigenschaften ein Objekt hat, ist abhängig vom verwendeten Objektdatentyp.

Eigenschaften, welche nicht explizit per DSL definiert werden, werden auf den
Defaultwert ihres Datentyps gesetzt.

Mehrere Eigenschaftsdefinitionen müssen mit einem `,` getrennt werden.

```
type id {
    property1: value,
    property2: other_value
}
```

Eine Eigenschaftsdefinition besteht aus dem Namen der Eigenschaft, welche zugewiesen werden soll auf der
linken Seite (im Folgenden `property_name`) und dem Wert, der der Eigenschaft zugewiesen werden soll auf
der rechten Seite. Die linke und rechte Seite werden durch `:` getrennt.

```
// in Objekt-definition:
...
    property_name: zuzuweisender_ausdruck
```

Auf der rechten Seite muss ein Ausdruck stehen, welcher vom `DSLInterpreter` zu einem Wert evaluiert
werden kann (gültige Ausdrücke sind im Kapitel \ref{A2:ausdruecke} dargestellt.)

### Ausdrücke \label{A2:ausdruecke}

Als "Ausdruck" wird alles bezeichnet, was der `DSLInterperter` zu einem Wert evaluieren kann.
Folgende Ausdrücke werden unterstützt:

#### Ganzzahliger Wert

Ganzzahlige Werte haben in der DSL den `int` Datentyp.

```
property_name: 42
```

#### Dezimalwert

Dezimalwerte haben in der DSL den `float` Datentyp.

```
property_name: 3.14
```

#### Zeichenketten (Strings)

Zeichenketten (auch Strings genannt) mit beliebiger Länge haben in der DSL den `string` Datentyp.

```
property_name: "Hello, World!"
```

#### Boolesche Ausdrücke

Boolesche Ausdrücke repräsentieren die Wahrheitswerte `true` oder `false`

```
property_name: true
```

#### Variablenname

Wird der Name einer Variablen als Ausdruck verwendet, setzt der Interpreter an die Stelle des verwendeten
Variablennamens den Wert der Variablen ein.

```
property_name: variable_identifier
```

#### arithmetische Operationen (nicht implementiert)

Arithmetische Operationen (Addition, Subtraktion, Multiplikation und Division)
können zur Berechung von arithmetischen Ausdrücken verwendet werden:

```
var1 = 2 + 2;
var2 = 2 - 2;
var3 = 2 * 2;
var4 = 2 / 2;
```

Es gilt Punkt- vor Strichrechnung.

#### Vergleichsoperationen (nicht implementiert)

Mit Vergleichsoperationen können die Werte zweier Operanden verglichen werden.
Es stehen die Operationen "größer" (`>`), "größer gleich" (`>=`), "kleiner" (`<`),
"kleiner gleich" (`<=`).

```
var1 = 42;
var2 = 3;
var3 = var1 > var2;
```

Nach der Ausführung hat `var3` den Wert `true`.

#### Gleichheit (nicht implementiert)

Es stehen "gleich" (`==`) und "ungleich" (`!=`) zur Verfügung, um die
Gleichheit von zwei Ausdrücken zu überprüfen

```
var1 = 3;
var2 = 3;
var3 = var1 == var2;
```

Nach der Ausführung hat `var3` den Wert `true`.

#### Klammerausdrücke

Mit Klammerausdrücken können Ausdrücke zusammengefasst werden, sodass sie
als ein Ausdruck betrachtet werden.

```
var1 = 2 * (3 + 5);
```

Nach der Ausführung hat `var1` den Wert `16`.

#### Unäre Operationen (nicht implementiert)

Mit den unären Operationen Negation (`!`) und Negativität (`-`) können einzelne Operanden
beeinflusst werden.

Negation:

```
var1 = true;
var2 = !var1;
```

`var2` hat nach der Ausführung den Wert `false`.

Negativität:

```
var1 = 42;
var2 = -var1;
```

`var2` hat nach der Ausführung den Wert `-42`.

#### Logische Operationen (nicht implementiert)

Logische Operationen umfassen `and` und `or` und dienen dazu, boolesche Ausdrücke
miteinander zu verknüpfen:

```
my_var1 = true;
my_var2 = false;
my_var3 = my_var1 and my_var2;
my_var4 = my_var1 or my_var2;
```

Die `and` Operation gibt basierend auf den Operanden folgende Werte zurück:

| linker Operand | rechter Operand | Ergebnis |
|----------------|-----------------|----------|
| false          | false           | false    |
| false          | true            | false    |
| true           | false           | false    |
| true           | true            | true     |

Die `or` Operation gibt basierend auf den Operanden folgende Werte zurück:

| linker Operand | rechter Operand | Ergebnis |
|----------------|-----------------|----------|
| false          | false           | false    |
| false          | true            | true     |
| true           | false           | true     |
| true           | true            | true     |

Logische Operationen können verkettet werden, wobei `and`-Operationen stärker binden
als `or`-Operationen, d.h.:

```
var1 or var2 and var3;
// entspricht
var1 or (var2 and var3);
```

#### Operator-Präzedenz

Unäre Operationen binden stärker als Multiplikation und Division.
Multiplikation und Division binden stärker als Subtraktion und Addition.
Subtraktion und Addition binden stärker als Vergleichsoperationen.
Vergleichsoperationen binden stärker als Gleichheitsoperationen.
`and` bindet stärker als `or`.

#### Funktionsaufrufe

Ein Funktionsaufruf kann optional einen Wert zurückgeben (siehe [Funktionsdefinition](#funktionsdefinition)).

```
property_name: function(param1, param2)
```

Der `DSLInterpreter` evaluiert die Ausdrücke, die als Argumente (hier `param1` und `param2`) an die Funktion übergeben werden, führt die
Funktion aus und setzt den Rückgabewert der Funktion an der Stelle des Funktionsaufrufs ein.

#### Funktionsreferenz

Einige Komponenten des ECS bieten die Möglichkeit, Event-Handler Funktionen zu
registrieren, über die das Verhalten der Komponenten per DSL beeinflusst werden kann. Diese Event-Handler
Funktionen werden für die jeweilige Komponente als Reaktion auf das Eintreten eines bestimmten Falls aufgerufen.

```
// Funktionsreferenz als Wert
property_name: function_name
```

Damit eine DSL Funktion als Event-Handler Funktion für ein bestimmtes Event verwendet werden kann, muss die
Funktionssignatur (also Datentypen und Anzahl der Parameter sowie Rückgabetyp) der DSL Funktion zu der
Signatur passen, die von der Komponente für diese Event-Handler Funktion erwartet wird.

#### Listen-Zugriff

Auf die Elemente einer Liste kann per `[]`-Operator und Index (0-basiert) zugegriffen
werden, dies könnte wie folgt aussehen:

```
string_array_variable = ["a", "b", "c"];
string_variable = string_array_variable[0]
```

Nach der Ausführung hat `string_variable` den Wert "a".

#### inline Objektdefinition

Es ist möglich, Objekte für komplexe Datentypen auf der rechten Seite einer
Zuweisung zu erzeugen und den Eigenschaften so zuzuweisen:

```
variable_with_complex_type =
    complex_type {
        my_text: "Hello, World!", my_number: 42
    };
```

#### Member-Zugriff

Auf Member (z.B. Properties) eines Objekts kann per `.`-Operator lesend und schreibend zugegriffen werden:

```
variable = complex_type { my_text: "Hello, World!", my_number: 42 };
other_variable = variable.my_text;
```

Der Wert der Variable `other_variable` ist nach der Ausführung "Hello, World!".

#### Member-Funktionen-Aufruf

Einige Datentypen definieren Member-Funktion, welcher ebenfalls per `.`-Operator aufgerufen werden können:

```
variable = type_with_member_func { /*...*/ };
variable.func();
```

### Level-Config

Die `level_config`-Definition ist die zentrale Objektdefinition für ein DungeonDSL-Program. über die
Eigenschaften des `level_config`-Objekts werden dem Dungeon-Framework alle nötigen Informationen übergeben,
die benötigt werden, um ein Level im Dungeon zu erstellen.

### Entitätstyp-Definition

Über eine Objektdefinition mit dem Datentypnamen `entity_type` können Entitätstypen
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

Eine Entitätstypedefinition verhält sich anders als eine Objektdefinition. Aus ihr wird nicht direkt ein Objekt
erzeugt (wie bei Objektdefinitionen).
Ein Entitätstyp stellt zunächst nur eine "Blaupause" für eine
konkrete Entität im Dungeon-Kontext dar, welche explizit instanziiert werden muss.
"Instanziierung" beschreibt das Erstellen einer konkreten Entität aus so einer
Blaupause, welche anschließend im Dungeon-Kontext existiert.

### Instanziierung von Entitätstypen

Entitätstypen werden mit einem Aufruf der nativen `instantiate`-Funktion instanziiert:

```
// ...

my_entity = instantiate(entity_type_name);

// ...
```

### Entität

Eine Entität (aus dem Dungeon-Kontext) wird durch den `entity`-Datentyp repräsentiert.
Im Dungeon-Kontext können einer Entität dynamisch Komponenten hinzugefügt oder
entfernt werden. Die DungeonDSL verwendet ein statisches Typsystem, um möglichst
früh aussagekräftige Fehlermeldungen generieren zu können.
Über die DungeonDSL-Repräsentation einer Entität soll es möglich sein, auf die
Komponenten der Entität zuzugreifen. Um dem Gedanken des statischen Typsystems
gerecht zu werden, verfügt der DSL `entity`-Datentyp über eine Property für jede
mögliche Komponente.
So wird sichergestellt, dass die semantische Analyse (Typechecking)
auch für die Komponenten einer Entität durchgeführt werden können.

Hierzu ein Beispiel:

```
my_entity = instantiate(my_entity_type);
my_property = my_entity.draw_component.invalid_property;
```

Für Zeile 2 kann festgestellt werden, dass die Eigenschaft
`draw_component` von `my_entity` eine Instanz des `draw_component`-Datentyps zurückgibt.
Für diesen Datentyp kann überprüft werden, ob er über eine `invalid_property`-Eigenschaft
verfügt und falls dies nicht der Fall ist, kann hierfür eine Fehlermeldung
erzeugt werden.

Es besteht aktuell **keine** Möglichkeit, vor der Ausführung eines Programms festzustellen,
ob eine beliebige Entität im Dungeon-Kontext (die durch ein `entity`-Objekt in der DSL
repräsentiert wird), auch tatsächlich über ein `DrawComponent` verfügt.


### Taskdefinition

Eine Taskdefinition verwendet die Syntax der Objektdefinition, um eine Aufgabe
zu definieren. Für jeden Aufgabentyp steht
ein Datentyp zur Verfügung (`single_choice_task` für Single Choice Aufgaben,
`multiple_choice_task` für Multiple Choice Aufgaben, usw.). Welche Eigenschaften
benötigt werden, um eine Aufgabe zu definieren, unterscheidet sich abhängig vom
Aufgabentyp.
Alle Aufgabendefinitionen benötigen allerdings eine Beschreibung (welche die
Aufgabenstellung enthält) und eine Bewertungsfunktion.

```
single_choice_task t {
  description: "Dies ist die Aufgabenstellung",
  fn_score: score_single_choice_task, // Funktionsreferenz
}
```

### Aufgabenabhängigkeiten

Abhängigkeiten zwischen Aufgaben können ähnlich der Syntax eines dot-Graphen definiert werden:

```
task_dependency td {
  t1 -> t2 [type=<dependency_type>]
}
```

Eine Abhängigkeit zwischen zwei Aufgabendefinitionen `t1` und `t2` wird als gerichtete Kante `->` definiert, wobei
das `type`-Attribut die Art der Abhängigkeit angibt. Alle verfügbaren Abhängigkeiten sind in der Dokumentation zu
Petri-Netzen aufgelistet.

#### Aufgabenspezifische Definitionen

Es gibt einige aufgabentypspezifische Definitionen

**Ersetzungsregel (für Ersetzungsaufgaben) (nicht implementiert)**

```
rules: (
  // n1 und n2 sind Mengendefinitionen
  r1: n1 -> n2
)
```

**Kombination (für Kombinationsaufgaben)**

```
mapping_task t {
  ...,
  mapping: <
    // Definition Zuordnung - (<term>, <definition>)
    ["a", "b"],

    // Hinzufügen von zusätzlichem Term
    ["c", _],

    // Hinzufügen von zusätzlicher Definition
    [_, "w"]
  >
}
```

**Lücken (für "Lücken füllen" Aufgaben) (nicht implementiert)**

```
gap_task t {
  ...,
  gaps: <
    // Definition der Lücken
    // der erste Eintrag der Menge wird als regulärer Ausdruck betrachtet,
    // der zweite Eintrag wird als Name der Lücke betrachtet
    ["regexp1", "gapname1"],
    ["regexp2", "gapname2"],
    ["regexp3", "gapname3"],
    ["regexp4", "gapname4"]
  >
}
```

### Funktionsdefinition

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

Die native `print()`-Funktion gibt Text über die Standardausgabe aus.

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

Die Ausgabe dieses Funktionsaufrufs sieht wie folgt aus:

```
> "Hello, World!"
> 42
```


**Rückgabewert**

Beispiel für eine Funktion mit Rückgabewert:

```
fn function_with_return_value() -> string {
  return "Dieser String wird der Rückgabewert"
}
```

Der Datentyp des Rückgabewerts muss hinter einem `->` angegeben werden (hier `string`).
Der Ausdruck hinter der `return`-Anweisung wird als Rückgabewert behandelt
(hier "Dieser String wird der Rückgabewert").

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

Die DungeonDSL unterstützt keine Überladung von Funktionsnamen.

### Import aus anderen DSL-Files (nicht implementiert)

Per `#import`-Anweisung können Definition aus anderen DSL-Dateien in die aktuelle Datei integriert werden.
Dabei wird der (relative) Pfad der Datei in `<` und `>` gefasst, gefolgt von einem `:` und dem Namen des Symbols
(also z.B. einer Entitätstyp-Definition oder Funktionsdefinition), welches importiert werden soll.

```
#import <relative_file_path.dsl>:name_of_thing_to_import

// Bspw.
#import <my_entity_types.dsl>:question_monster
#import <my_function_definitions.dsl>:evaluation_function
```

### Taskbuilder-Funktionen

Der Import-Mechanismus muss nicht für Taskbuilder-Funktionen verwendet werden, da diese gesondert vom
DSLInterpreter behandelt werden.
Das Dungeon-System ließt die Taskbuilder-Methoden automatisch aus einem fest definierten Verzeichnis ein
und wählt, sofern nicht anders in der `quest_config`-Definition vorgesehen, eine passende Taskbuilder-Methode
für den jeweiligen Aufgabentypen aus. Nur falls **eine bestimmte** Taskbuilder-Methode für eine
Aufgabe verwendet werden soll, muss die entsprechende Definition per `#import`-Anweisung importiert werden.

### Kontrollfluss-Steuerung

#### Konditionale Ausdrücke

Per `if` und `else`-Keywords können Anweisungen im Funktionsrumpf abhängig von einer Bedingung ausgeführt werden.
Die Bedingung wird auf einen Wahrheitswert (`true` oder `false`) abgebildet. Falls die Bedingung `true` ist, werden
die Anweisungen im `if`-Zweig ausgeführt, andernfalls die Anweisungen im `else`-Zweig (falls vorhanden).

Falls nur eine Anweisung in einem `if`- oder `else`-Zweig ausgeführt werden soll, kann dies so aussehen:

```
if condition
  print("Hello");
else
  print("World");
```

Zum obigen Beispiel: ist `condition` `true`, wird "Hello" ausgegeben, andernfalls wird "World" ausgegeben.

Falls mehr als Anweisung im Zweig ausgeführt werden sollen, müssen sie in geschweifte Klammern zusammengefasst werden:

```
if condition {
  print("Hello");
  print("World");
}
```

Konditionale Anweisungen können kaskadiert werden:

```
if condition1
  print("Hello");
else if condition2
  print("World");
else
  print("!");
```

Im obigen Beispiel wird zuerst `condition1` überprüft, falls sie `true` ist, wird "Hello" ausgegeben.
Nur falls `condition1` `false` ist, wird `condition2` überprüft und falls diese Bedingung `true` ist, wird
"World" ausgegeben. Nur wenn `condition1` und `condition2` `false` sind, wird die letzte `else` Anweisung
ausgeführt und "!" wird ausgegeben.

#### Schleifen

Per Schleifendefinition können Anweisungen abhängig von einer Bedingung repetitiv ausgeführt werden:

```
while condition {
  print("hello");
  print("world");
  // Code, der condition modifiziert
}
```

Eine Sonderform der Schleifen stellt die foreach-Schleife dar, mit der über
alle Einträge einer Liste oder einer Menge iteriert werden können. Hierzu
wird im unteren Snippet die Iterationsvariable `entry` mit dem Datentypen
`entry_type` erstellt, welche die Elemente der Liste enthält:

```
var my_list : int[];
my_list = [1,2,3];
for entry_type entry in my_list {
    print(entry);
}
```

Die Ausgabe des obenstehenden Listings sieht wie folgt aus:

```
> "1"
> "2"
> "3"
```

Optional kann für eine foreach-Schleife auch eine Counter-Variable definiert werden,
welche die aktuelle Iteration mitzählt (beginnend bei `0`):

```
var my_list : string[];
my_list = ["element eins","element zwei","element drei"];
for entry_type entry in my_list count i {
    print(i);
    print(entry);
}
```

Die Ausgabe des obenstehenden Listings sieht wie folgt aus:

```
> "0"
> "element eins"
> "1"
> "element zwei"
> "2"
> "element drei"
```
