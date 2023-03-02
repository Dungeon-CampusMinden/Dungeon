---
title: "Aufgaben"
---

## Begriffsklärung

Folgende Begriffe haben im Kontext der Aufgabenbeschreibung eine besondere Bedeutung und werden
daher nochmal definiert:

**Spielelement**

Ein Spielelement ist ein "Objekt" mit bestimmten Eigenschaften und [Komponenten](../ecs/components/readme.md),
welches im Dungeon platziert ist. Ein Beispiel hierfür wäre eine Truhe, also Objekt, welche eine Truhen-Textur
und eine Inventar-Komponente hat, in der weitere Items (die wiederum auch Spielelemente sind) gespeichert werden
können.

**Spielmechanik**

Eine Spielmechanik definiert, wie verschiedene Spielelemente miteinander interagieren. Beispiele hierfür
wäre eine Mechanik, die es dem Spielercharakter erlaubt, Items aus einer Truhe herauszunehmen und in sein
eigenes Inventar zu transferieren.

**Aufgabentyp**

Ein Aufgabentyp beschreibt in abstrakter Weise, welche Datenstrukturen nötig sind und in welcher Form sie
zur Bewertung einer Aufgabe verarbeitet werden. Ein Beispiel hierfür ist der Aufgabentyp Single Choice,
bei dem aus einer Menge mehrerer Elemente das eine korrekte Element ausgewählt werden muss. Ein weiteres
Beispiel wäre eine Sortieraufgabe, bei der eine festgelegte Anzahl an Elementen in die korrekte Reihenfolge
gebracht werden muss.

**Spielszenario**

Ein Spielszenario ist eine Komposition aus Spielelementen und Spielmechaniken, die genutzt wird um eine
konkrete Aufgabe im Dungeon abzubilden.
