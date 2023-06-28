---
title: "Typsystem"
---

## Überblick

- welche Arten von Datentypen gibt es?

## Wie werden Datentypen gespeichert und resolved?

- `IEnvironment` erklären

## Welche Klassen sind am Typsystem beteiligt?

## Boolesche Ausdrücke

Für konditionale Anweisungen (`if-else`-Verzweigungen) wird eine Bedingung auf einen
Wahrheitswert abgebildet (also entweder "wahr" / `true` oder "falsch" / `false`).
Die Abbildung aller Datentypen auf Wahrheitswerte sieht wie folgt aus:

| **Datentyp**  | **Abbildung**                                                |
|---------------|--------------------------------------------------------------|
| none          | `false`                                                      |
| int           | Value = `0`: `false`; Value != `0`: `true`                   |
| float         | Value = `0.0`: `false`; Value != `0.0`: `true`               |
| string        | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| graph         | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| AggregateType | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| FunctionType  | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
| EntityType    | Value = `Value.NONE`: `false`; Value != `Value.NONE`: `true` |
