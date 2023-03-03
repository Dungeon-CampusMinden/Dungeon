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

**Steuermechanismen**

Steuermechanismen sind alle Mechanismen des Systems, die den Ablauf der Umsetzung einer Aufgabe in ein
Spielszenario beeinflussen.

### Spielelemente gesammelt

- Räume
- Truhen, "Briefkästen", bzw. allgemein ein Spielobjekt mit Inventar
- Questgeber-NPC
- "Brief", bzw. Item, auf dem etwas drauf steht, was der Spieler lesen kann
- verschiebbare Spielelemente ("Klötze", "Statuen")
- Monster
- Türen (evtl. mit erkennbarer Kennzeichnung)
- Hinweis / Hilfemeldung
- Schalter / Hebel mit booleschen Zuständen oder größerer Zustandsmenge
- "Bodenaktivator" -> bspw. ein "Pentagram" auf dessen Spitzen Kerzen gelegt werden müssen
- UI-Elemente für "einfache / kleine Aufgaben" (also plain Single Choice, Multiple Choice, Freitexteingabe)
- abstrakt: Item-Kombinator (bspw. "Zauberkessel"), der die Möglichkeit bietet, Items zu kombinieren

### Spielmechaniken gesammelt

- Dinge aus einem Inventar herausnehmen und in das Spielerinventar ablegen (bspw. aus einer Truhe)
- Dinge aus dem Spielerinventar in ein anderes Inventar ablegen (bspw. in eine Truhe)
- Dinge aus dem Spielerinventar auf den Boden fallen lassen
- Zustand bzw. Inhalt eines Inventars auf "Korrektheit" (je nach Aufgabe definiert) überprüfen
- Bodenaktivator durch bestimmten Itemtyp aktivieren
- Schalter / Hebel betätigen (wahlweise tastend, schaltend oder bei Zustandsanzahl > 2 den Zustand festlegen)
- NPC ansprechen
- UI-Elemente für kleine Aufgaben anzeigen
- Monster angreifen
- verschiebbares Spielelement schieben oder ziehen (auf Gitter oder frei?)
- Ein Item durch ein anderes (oder mehrere) ersetzen
- Mehrere Items zu einem Item zusammenfassen
- Durch eine (ggfs. gekennzeichnete) Tür gehen und somit einen anderen Raum betreten
- Auf eine richtige "Antwort" reagieren und den Spieler belohnen
- Auf eine falsche "Antwort" reagieren und den Spieler bestrafen

### Aufgabentypen gesammelt

- bestimmte Anzahl Objekte sortieren (Sortieren)
- Objekte einander Zuordnen (Zuordnen)
- Single Choice (Single-Choice)
- Multiple Choice (Multiple-Choice)
- Freitexteingabe (Lückentext)
- Objekte miteinander zu neuen Objekten kombinieren 
- Ein Objekt in weitere Objekte unterteilen
- Objekte in der richtigen Reihenfolge auswählen 
- Eine Matrix füllen

### Steuermechanismen gesammelt

- Unterteilung einer Aufgabe in Teilaufgaben
- Festlegung des Aufgabentyps für eine Aufgabe
- Definition, welche Spielmechaniken in der konkreten Realisierung einer Aufgabe
  im Dungeon-Level genutzt werden sollen (oder, falls nicht angegeben, eine aus
  den für den Aufgabentyp möglichen auswählen)
- Definition von richtigen Lösungen für eine (Teil-)Aufgabe
- Definition von richtigen Lösungsmengen für eine (Teil-)Aufgabe, falls mehrere Lösungen möglich sind
- Definition von falschen Lösungen für eine (Teil-)Aufgabe
- Definition, wie die DSL / der Dungeon falsche Antworten für eine (Teil-)Aufgabe
  generieren soll (ggfs. mit Schranken / Bedingungen)
    - Bspw. Alle Integer-Werte von 0 bis 100 außer 42
- Petri-Netz für die sequentielle Organisation von Aufgaben
- Petri-Netz für die sequentielle Organisation von Teil-Aufgaben (also innerhalb einer Aufgabe)
- Logging von "Antworten" für eine (Teil-)Aufgabe
- Gewichtung von Teilaufgaben (sodass bestimmte Teilaufgaben stärker in die Bewertung einer
  gesamten Aufgabe einfließen)
- beliebig tiefe Verschachtelung von Aufgaben: Zur Lösung einer Aufgabe müssen Teil-Aufgaben x, y,z. Teilaufgaben liefern Ergebnisse/Items die zum Lösen der Hauptaufgabe nötig sind.```
  gelöst werden


## Aufgabentypen

### Single-Choice

### Multiple-Choice

### Lückentext

### Zuordnen

### Sortieren


## Spielmechanik

### GUI-Buttons drücken

### GUI Text eingeben

### Item(s) abgeben

### Item(s) in Kiste tuen

### Item(s) aus Kiste nehmen

### Blöcke Schieben

### Schalter betätigen

### Kämpfen

### Mit Entitäten interagieren 
- Lesen/Sprechen

## Spielszenario 

### NPC stellt Frage

### Monster greift mit Frage an 

### Truhe ist mit Frage verschlossen

### Mehrere Truhen aber nur eine ist richtig

### Blöcke müssen in die richtige Reihenfolge geschoben werden

### Schalter müssen in der richigen Reihenfolge betätigt werden

### Samlung an Schalter müssen in die richtigen Zustände gebracht werden

### Monster müssen in der richtigen Reihenfolge gehauen werden

### Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältniss entsteht

### Items müssen in Truhen abgelegt werden

### Items müssen aus einer Trhue entfernt werden



## Zuordnung Spielszenario und Spielmechanik
- Matrix
  Spalte: Spielszenario
  Zeile: Spielmechanik 
  Wert 1 Wenn das Szenario mit dieser Mechanik abgebildet werden kann 
  Wert 0 Wenn das Szenario mit dieser Mechanik nicht abgebildet werden kann 

## Zuordnung Aufgabentyp und Spielszenario

- Matrix
  Spalte: Szenario
  Zeile: Aufgabentyp 
  Wert 1 Wenn der Typ in diesem Szenario abgebildet werden kann 
  Wert 0 Wenn der Typ in diesem Szenario nicht abgebildet werden kann 


## Konkrete Aufgaben

### Regex-monster

### Codesmells erkennen (PM)

### Sortieralgorithmus (ADS)

### CSP mit Forward Checking lösen (KI)
