---
title: "Aufgaben"
---

## Begriffsklärung

Folgende Begriffe haben im Kontext der Aufgabenbeschreibung eine besondere Bedeutung und werden
daher nochmal aus Sicht der aufgabendefinierenden Lehrperson definiert:


**Aufgabentyp**

Ein Aufgabentyp beschreibt in abstrakter Weise, welche Datenstrukturen nötig sind und in welcher Form sie
zur Bewertung einer Aufgabe verarbeitet werden. Ein Beispiel hierfür ist der Aufgabentyp Single Choice,
bei dem aus einer Menge mehrerer Elemente das eine korrekte Element ausgewählt werden muss. Ein weiteres
Beispiel wäre eine Sortieraufgabe, bei der eine festgelegte Anzahl an Elementen in die korrekte Reihenfolge
gebracht werden muss.

**Spielmechanik**

Eine Spielmechanik beschreibt eine Sammlung an verschiedenen Aktionen die der Spieler im Spiel tätigen kann.
Eine Spielmechanik bietet die Möglichkeit einen Aufgabentypen zu realsieren.

Beispiele: Der Spieler legt einen Hebel um, um eine Antwort zu geben.

Aus den Spielmechaniken müssen in der Implmentierung konkrete Components und Systeme erstellt werden. 

**Spielszenario**

Ein Spielszenario beschreibt wie die Spielemechanik genutzt wird, um den Spieler die Aufgabe im Dungeon zu präsentieren. 

Beispiel: In einem Raum befindet sich ein NPC und verschiedene Schalter. Der NPC stellt dem Spieler eine Frage und sagt ihm, welcher Hebel für welche Antwort steht. 

**Steuermechanismen**

Steuermechanismen sind alle Mechanismen des Systems, die den Ablauf der Umsetzung einer Aufgabe in ein
Spielszenario beeinflussen.

### Spielmechaniken gesammelt

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
Es wird eine Frage gestellt und eine Menge an Antwortmöglichkeiten gegeben. 
Nur eine Antwort ist richtig. Die richtige Antwort muss ausgewählt werden. 

### Multiple-Choice

### Lückentext
- Gibt eine Frage
- Spieler gibt eine Antwort in Textform 
- Antwort wird ausgewertet

### Zuordnen

### Sortieren

### Ersetzen 

### Kombinieren

### Objekte in der richtigen Reihenfolge auswählen  

### Matrix füllen

## Spielmechanik

### Ein GUI-Button drücken

### GUI Checkboxen anhaken

### GUI Text eingeben
- Ein UI geht auf, da steht die Frage drin
- Gibt ein Textfeld um Text einzugeben
- Gibt einen Button "okay" zum bestätigen oder abbrechen
- Wenn okay: -> Antwort wird ausgewertet, Belohnung/Bestrafung wird durchgeführt, UI geht zu 
- Wenn abbrechen -> UI schließt sich
- Was brauch ich dafür:
 - Text anzeigen
 -  

### Item an NPC abgeben 

### Item(s) in Container tuen

### Item(s) aus Container nehmen

### Blöcke schieben

### Schalter betätigen

### Kämpfen

## Spielszenario 

### Liste an Entitäten

- Name
  - ComponentA
  - ComponentB

### NPC stellt Frage
Stellt Frage du gibst Antwort, NPC Happy

### Monster greift mit Lückentext-Frage an 
Das Monster rennt in den Spieler und bei kollision wird eine Lückentext-Frage gestellt.  

### Truhe ist mit Frage verschlossen

### Mehrere Truhen aber nur eine ist richtig

### Blöcke müssen in die richtige Reihenfolge geschoben werden

### Schalter müssen in der richigen Reihenfolge betätigt werden

### Samlung an Schalter müssen in die richtigen Zustände gebracht werden

### Monster müssen in der richtigen Reihenfolge gehauen werden

### Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältniss entsteht

### Items müssen in Truhen abgelegt werden

### Items müssen aus einer Trhue entfernt werden

### Items müssen in einen Briefkasten gepackt werden

## Zuordnung Aufgabentyp und Spielmechniken




                      GUI-Buttons drücken    Mehre Gui Buttosn drücken       Item(s) abgeben        Schalter betätigen          Blöcke Schieben
Single Choice                 x                                                    x                        x
 
- Matrix
  Spalte: Szenario
  Zeile: Aufgabentyp 
  Wert 1 Wenn der Typ in diesem Szenario abgebildet werden kann 
  Wert 0 Wenn der Typ in diesem Szenario nicht abgebildet werden kann 


## Zuordnung  Spielmechanik und Spielszenario

                      Monster greift mit Lückentext-Frage an   NPC stellt Lückentextfrage 
 GUI Text eingeben:       x                                                 x
Mecnanik XYZ              x

- Matrix
  Spalte: Spielszenario
  Zeile: Spielmechanik 
  Wert 1 Wenn das Szenario mit dieser Mechanik abgebildet werden kann 
  Wert 0 Wenn das Szenario mit dieser Mechanik nicht abgebildet werden kann 


## Konkrete Aufgaben

### Regex-monster
Monster greift einen an und man muss die Frage "XYZ" beantworten, indem man den passenden regulären Ausdruck eingibt. 

Aufgabentyp: Lückentext
Spielmechanik: Gui Text eingeben
Spielszenario: Monster greift mit Lückentext-Frage an 

### Codesmells erkennen (PM)

### Sortieralgorithmus (ADS)

### CSP mit Forward Checking lösen (KI)



### Notizen (delete later)

                                    MechanikSammlung A        Szenario-MechanikSammlung-B-1
(Aufgabe (A4) ->) Aufgabe-Typen ->  MechanikSammlung B  ->    Szenario-MechanikSammlung-B-2       ---> Implementierungsdetails
                                    MechanikSammlung C        Szenario-MechanikSammlung-B-2


                  Knopf im UI drücken                          -> Szeario A: NPC stellt dir die Frage, Szenario B: Monster greifkt dich an und fragt dich, Szeario C: Kiste ist verschlossen und fragt dich
Single Choice ->  Hebel Umlegen der die richtige Antwort ist  -> Kommst in ein Raum, da steht wer und sag dir was sache ist (Hebel A ist "Antwort A" Hebel B "Antwort B) etc. leg den richtigen um
                  Monter hauen das das richtige ist           -> Kommst in Rau mit Monstenr, jedes Monster gibt ne Antwort, in der Mitte steht nen Text mit der Frage, hau das richtige Monster
                  Richtiges Item abgeben                      -> Jedes Item ist ne Antwort, dem NPC musst du das richtige Item geben  (der stellt dir die Frage) Items sind im level verteilt




