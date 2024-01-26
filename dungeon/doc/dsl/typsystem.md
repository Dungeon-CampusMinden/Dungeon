---
title: "Typsystem"
---

## Überblick

**TODO**

- welche Arten von Datentypen gibt es?

## Wie werden Datentypen gespeichert und resolved?

- `IEnvironment` erklären

## Welche Klassen sind am Typsystem beteiligt?

## Boolesche Ausdrücke

Für konditionale Anweisungen (`if-else`-Verzweigungen) wird eine Bedingung auf einen
Wahrheitswert abgebildet (also entweder “wahr” / `true` oder “falsch” / `false`). Die
Abbildung aller Datentypen auf Wahrheitswerte sieht wie folgt aus:

| **Datentyp**  | **Abbildung**                                                |
|---------------|--------------------------------------------------------------|
| none          | `false`                                                      |
| bool          | Value = `false`: `false`; Value = `true`: `true`             |
| int           | Value = `0`: `false`; Value != `0`: `true`                   |
| float         | Value = `0.0`: `false`; Value != `0.0`: `true`               |
| string        | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| graph         | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| AggregateType | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| FunctionType  | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| EntityType    | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |

## Übersetzung von Java-Objekt in DSL-Typsystem

Für DSL Event-Handler Funktionen ist es nötig, dass die Funktionsparameter, welche vom
Dungeon an die DSL-Funktion übergeben werden, in das DSL-Typsystem und somit als
[DSL-Value](interpretation_laufzeit.md#value-und-imemoryspace) übersetzt werden.

Hierfür ist die `RuntimeObjectTranslator`-Klasse zuständig. In der
`translateRuntimeObject()` Methode wird der DSL-Typ für die Klasse des zu übersetzenden
Objekts ermittelt, was voraussetzt, dass der DSL-Typ per [Typebuilding](typebuilding.md) in
das DSL-Typsystem integriert wurde. Abhängig von der Art des DSL-Typs passieren
unterschiedliche Dinge:

### Basistyp

Für Basistypen (z.B. `bool`, `int`, `float`) wird das zu übersetzende Objekt direkt in eine
`Value`-Instanz verpackt. Der Wert wird kopiert, der in der `Value`-Instanz gespeicherte
Wert ist also unabhängig vom übergebenen Parameter.

### AggregateType (komplexer Datentyp)

Komplexe Datentypen werden genutzt, um komplexe Objekte (also Klassen mit Membern oder
Records) zu repräsentieren. Ein Beispiel hierfür sind die `Component`-Klassen aus dem
Dungeon. Über die DSL soll der Zustand dieser Objekte verändert werden können, daher ist es
nötig, das zu übersetzende Objekt in ein `EncapsulatedObject` zu kapseln, welches als
`IMemorySpace` in einem `AggregateValue` verwendet wird (siehe Doku zu [Value und
IMemorySpace](interpretation_laufzeit.md#value-und-imemoryspace)) Die Member des
entsprechenden `AggregateType`s für das Objekt können somit im Kontext des tatsächlichen
Java-Objekts geschehen. Dies ist möglich, da im `AggregateType` die Verknüpfung zwischen
Membernamen des DSL-Typs und dem `Field` in der Java-Klasse gespeichert wird.

### Adaptierte Datentypen

Ist der entsprechende DSL-Datentyp für die Klasse des Objekts
[adaptiert](typebuilding.md#typadaptierung), handelt es sich beim Java-Objekt mit hoher
Wahrscheinlichkeit um ein komplexes Objekt. Es existiert aktuell noch keine Möglichkeit, auf
die Member eines adaptierten Datentyps zuzugreifen, was mit [Issue
#781](https://github.com/Dungeon-CampusMinden/Dungeon/issues/781) erweitert wird (**TODO**).
Daher wird aktuell für diesen Fall auch eine einfache `Value`-Instanz erstellt, welche eine
Referenz auf das zu übersetzende Objekt enthält.
