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
Die Aufgabenstellung besteht aus einem Fragetext und einer Menge an vorgegebenen Antwortmöglickeiten.
Nur eine Antwort ist richtig. 
Die richtige Antwort muss ausgewählt werden. 

### Multiple-Choice
Die Aufgabenstellung besteht aus einem Fragetext und einer Menge an vorgegebenen Antwortmöglickeiten.
Mehrere Antworten sind richtig. 
Es müssen alle richtigen Antworten markiert wereden. Es dürfen keine falschen Antworten markiert werden.

### Lückentext
Die Aufgabenstellung besteht aus einem Fragetext. 
Die Antwort ist ein kurzer Freitext. 
Es muss die richtige Antwort in den Freitext eingegeben werden.
Die richtige Antwort kann ein konkreter Wert oder eine Eingabe passend zu einem regulären Ausdruck sein. 

### Zuordnen
Es gint die Mengen `V` und `A` an Vorgabetexten (Das können Fragen, Behauptungen, Beschreibungen etc. sein).
Für jeden eintrag `Vi` in `V`gibt es mindestens einen Eintrag `Ai` in `A` der `Vi` zugeordnet werden kann.
Die Aufgabe besteht daraus, die richtigen Einträge aus `A` mit den entsprechenden Einträgen in `V` zu verbinden.
Nicht zu jedem `Ai` muss es auch ein `Vi` geben.

### Sortieren
Es wird ein unsortierte Liste `V` and sortierbaren Werten übergeben, die in eine bestimmte Reihenfolge gebracht werden müssen. 
Die richtige Reihenfolge ergibt sich aus der Aufgabenstellung. 

### Ersetzen   (Malte)

### Kombinieren   (Malte)

### Objekte in der richtigen Reihenfolge auswählen     (Malte)

### Matrix füllen   (Malte)

## Spielmechanik

### Ein GUI-Button drücken
- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und mehrere Buttons mit Antworten (Text oder Bild) angezeigt
- Der Spieler muss auf den Button mit der Antwort klicken, die er abgeben möchte.

### GUI Checkboxen anhaken
- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und mehrere Buttons mit Antworten (Text oder Bild) angezeigt
- Der Spieler muss auf die Buttons mit der Antwort klicken, die er für richtig hält.
- Mit einem Klick aktiviert er eine nicht aktive (nicht ausgewählte) Antwort oder deaktivert eine aktivierte Antwort. 
- Mit einen Bestätigungsbutton muss der Spieler seine Antworten einloggen. 

### GUI Text eingeben
- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und einem Feld zur Texteingabe angezeigt.
- Der Spieler muss seine Antwort in das Eingabefeld schreiben. 
- Mit einen Bestätigungsbutton muss der Spieler seine Antworten einloggen. 

### Item an NPC abgeben  (Malte)

### Item(s) in Container tuen (Malte)

### Item(s) aus Container nehmen (Malte)

### Blöcke schieben
- Im Raum sind verschiedene Blöcke platziert
- Jeder Block kann angeschaut und untersucht werden (dann wirt Text angezeigt)
- Blöcke können geschoben und gezogen werden, um dessen Position zu ändern
- Es gibt spezielle Felder zu denen die Blöcke geschoben werden müsse. 
- Die platzierung der Blöcke auf den Felder gibt die Reihenfolge der Blöcke an
- Der Spieler muss die Blöcke untersuchen und sie so Anordnene, dass sie die Aufgabenstellung lösen
- Um seine Anordnung abzugeben, muss er einen Schalter betätigen

### Schalter betätigen (Malter)

### Kämpfen
- Im Dungeon laufen Monster herum.
- Der Spieler muss die Monster mit Waffen oder Zaubern angreifen und besiegen. 
- Besiegte Monster werden aus dem Spiel gelöscht und können Items fallen lassen. 

## Spielszenario 

### Liste an Entitäten (later)

- Name
  - ComponentA
  - ComponentB

### NPC stellt Frage (Andre)
Im Level steht ein Questgeber mit dem der Spieler reden kann.
Im Gespräch stellt der NPC dem Spieler eine Frage die er beantworten muss. 
Beantwortet der Spieler die Frage richtig, bekommt er eine Belohnung.
Beantwortet der Spieler die Frage falsch, bekommt er keine Belohnung. 

### Monster greift mit Frage an
Im Level läuft ein Monster herum.
Wenn das Monster den Spieler sieht, läuft es in den Spieler hinein (bis zur Kollision).
Bei Kollision stellt das Monster dem Spieler eine Frage.
Beantwortet der Spieler die Frage richtig, stirbt das Monster.
Beantwortet der Spieler die Frage falsch, bekommt er viel Schaden und das Monster rennt weg. 
  

### Truhe ist mit Frage verschlossen
Im Level steht eine Schatzkiste herum.
Wenn der Spieler die Schatzkiste öffnen will, stellt diese ihm eine Frage.
Beantwortet der Spieler die Frage richtig, geht die Schatzkiste auf.
Beantwortet der Spieler die Frage falsch, bleibt die Schatzkiste verschlossen. 

### Mehrere Truhen aber nicht alle sind richtig
Der Spieler kommt in einen Raum.
Der Questgeber stellt ihn eine Frage.
Die Antwortmöglichkeiten sind in formen von Schatzkisten im Raum verteilt. 
Wenn der Spieler mit den Schatzkisten interagiert, sagen diese ihm, welche Antwort sie sind. 
Der Spieler muss die Schatzkiste/n mit der/den richtigen Antwort/en öffnen.

Beantwortet der Spieler die Frage richtig, bekommt er eine Belohnung.
Beantwortet der Spieler die Frage falsch, bekommt er keine Belohnung.

### Blöcke müssen in die richtige Reihenfolge geschoben werden (Malte)

### Schalter müssen in der richigen Reihenfolge betätigt werden (Malte)

### Samlung an Schalter müssen in die richtigen Zustände gebracht werden (Malte)

### Monster müssen in der richtigen Reihenfolge gehauen werden (Malte)

### Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältniss entsteht
Der Spieler kommt in einen Raum mit verschiedenen Monstern. 
Der Questgeber erklärt den Spieler, die Aufgabe.
Der Spieler muss die Monster so bekämpfen, dass das richtige Verhältniss erreicht ist. 

Löst der Spieler die Aufgabe richtig, bekommt er eine Belohnung.
Löst der Spieler die Aufgabe falsch, bekommt er keine Belohnung.

### Items müssen in Truhen abgelegt werden (Malte)

### Items müssen aus einer Trhue entfernt werden (Malte)

### Items müssen in einen Briefkasten gepackt werden (Malte)

## Zuordnung Aufgabentyp und Spielmechniken (Malte vorbereiten, rest later)




                      GUI-Buttons drücken    Mehre Gui Buttosn drücken       Item(s) abgeben        Schalter betätigen          Blöcke Schieben
Single Choice                 x                                                    x                        x
 
- Matrix
  Spalte: Szenario
  Zeile: Aufgabentyp 
  Wert 1 Wenn der Typ in diesem Szenario abgebildet werden kann 
  Wert 0 Wenn der Typ in diesem Szenario nicht abgebildet werden kann 


## Zuordnung  Spielmechanik und Spielszenario (Malte vorbereiten, rest later)

                      Monster greift mit Lückentext-Frage an   NPC stellt Lückentextfrage 
 GUI Text eingeben:       x                                                 x
Mecnanik XYZ              x

- Matrix
  Spalte: Spielszenario
  Zeile: Spielmechanik 
  Wert 1 Wenn das Szenario mit dieser Mechanik abgebildet werden kann 
  Wert 0 Wenn das Szenario mit dieser Mechanik nicht abgebildet werden kann 


## Konkrete Aufgaben

### Regex-monster (Andre)
Monster greift einen an und man muss die Frage "XYZ" beantworten, indem man den passenden regulären Ausdruck eingibt. 

Aufgabentyp: Lückentext
Spielmechanik: Gui Text eingeben
Spielszenario: Monster greift mit Lückentext-Frage an 

### Codesmells erkennen (PM) (Andre)

### Sortieralgorithmus (ADS) (Andre)

### CSP mit Forward Checking lösen (KI) (Andre)



### Notizen (delete later)

                                    MechanikSammlung A        Szenario-MechanikSammlung-B-1
(Aufgabe (A4) ->) Aufgabe-Typen ->  MechanikSammlung B  ->    Szenario-MechanikSammlung-B-2       ---> Implementierungsdetails
                                    MechanikSammlung C        Szenario-MechanikSammlung-B-2


                  Knopf im UI drücken                          -> Szeario A: NPC stellt dir die Frage, Szenario B: Monster greifkt dich an und fragt dich, Szeario C: Kiste ist verschlossen und fragt dich
Single Choice ->  Hebel Umlegen der die richtige Antwort ist  -> Kommst in ein Raum, da steht wer und sag dir was sache ist (Hebel A ist "Antwort A" Hebel B "Antwort B) etc. leg den richtigen um
                  Monter hauen das das richtige ist           -> Kommst in Rau mit Monstenr, jedes Monster gibt ne Antwort, in der Mitte steht nen Text mit der Frage, hau das richtige Monster
                  Richtiges Item abgeben                      -> Jedes Item ist ne Antwort, dem NPC musst du das richtige Item geben  (der stellt dir die Frage) Items sind im level verteilt




