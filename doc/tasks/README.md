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

### Single-Choice (Andre)
Es wird eine Frage gestellt und eine Menge an Antwortmöglichkeiten gegeben.
Nur eine Antwort ist richtig. Die richtige Antwort muss ausgewählt werden.

### Multiple-Choice  (Andre)

### Lückentext  (Andre)
- Gibt eine Frage
- Spieler gibt eine Antwort in Textform
- Antwort wird ausgewertet

### Zuordnen  (Andre)

### Sortieren  (Andre)

### Ersetzen

Aus einer Reihe aus Elementen (kann auch nur eins sein) muss ein Element durch ein oder mehrere andere Elemente ersetzt werden.
Hierdurch wird die Reihe aus Elementen sowie deren Anzahl verändert.

### Kombinieren

Aus einer Reihe aus Elementen müssen mehrere Elemente (oder alle) zu einem anderen Element zusammengefasst werden.
Hierdurch wird die Reihe aus Elementen sowie deren Anzahl verändert.

### Objekte in der richtigen Reihenfolge auswählen

Es muss in der richtigen Reihenfolge ein Element nach dem anderen aus einer Menge ausgewählt werden.
Anders als beim Aufgabentyp [Sortieren](#sortieren--andre-) müssen hier nicht alle Elemente von Anfang
an bekannt sein.

### Matrix füllen

Die Elemente einer Matrix mit beliebigen Dimensionen müssen befüllt werden.

## Spielmechanik

### Ein GUI-Button drücken (Andre)

### GUI Checkboxen anhaken (Andre)

### GUI Text eingeben (Andre)
- Ein UI geht auf, da steht die Frage drin
- Gibt ein Textfeld um Text einzugeben
- Gibt einen Button "okay" zum bestätigen oder abbrechen
- Wenn okay: -> Antwort wird ausgewertet, Belohnung/Bestrafung wird durchgeführt, UI geht zu
- Wenn abbrechen -> UI schließt sich
- Was brauch ich dafür:
 - Text anzeigen
 -

### Item an NPC abgeben  (Malte)

- Eine Menge an Items ist im Dungeon verteilt (dabei ist nicht relevant, wie und wo der Spieler die
  Items findet und erhält)
- Die Items können vom Spieler aufgesammelt werden
- Bei Interaktion mit einem NPC kann der Spieler ein aufgesammeltes Item auswählen und an den NPC abgeben
  - Hierfür öffnet sich ein UI-Element, welches das Spielerinventar anzeigt und eine Auswahl der Item(s) ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Überprüfung der Korrektheit des abgegebenen Items
- Der NPC reagiert in bestimmter Form auf den Fall, dass das falsche Item abgegeben wurde und in anderer Form
  auf den Fall, dass das korrekte Item abgegeben wurde

### Item(s) in Container ablegen (Malte)

- Eine Menge an Items ist im Dungeon verteilt (dabei ist nicht relevant, wie und wo der Spieler die
  Items findet und erhält)
- Die Items können vom Spieler aufgesammelt werden
- Bei Interaktion mit Container kann der Spieler eine beliebige Anzahl der gesammelten Items in den Container
  legen
  - Hierfür öffnet sich ein UI-Element, welches das Spielerinventar anzeigt und eine Auswahl der Item(s) ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Die Überprüfung der Korrektheit der im Container abgelegten Items kann zu unterschiedlichen Zeitpunkten geschehen
  - Direkt nach dem Schließen des UI-Elements zur Auswahl der Items
  - Bei externem Event (bspw. Betätigung eines Schalters)

### Item(s) aus Container nehmen (Malte)

- In einem Container ist eine Menge von Items gespeichert
- Durch Interaktion mit dem Container kann der Spieler eine beliebige Anzahl Items aus dem Container herausnehmen
  - Hierzu öffnet sich ein UI-Element, welches das Inventar des Containers anzeigt
     und die Auswahl der herauszunehmenden Items ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Die Überprüfung der Korrektheit der aus dem Container entnommenen Items kann zu unterschiedlichen Zeitpunkten geschehen
  - Direkt nach dem Schließen des UI-Elements zur Auswahl der Items
  - Bei externem Event (bspw. Betätigung eines Schalters)

### Blöcke schieben (Andre)

### Schalter betätigen

- Aus einer Menge von Schaltern müssen ein oder mehr korrekte Schalter ausgewählt werden
- Die Schalter können dabei entweder Tastend, Schaltend oder mit einer Zustandszahl > 2 agieren
- Bei Interaktion mit einem Schalter öffnet sich ein UI-Element, welches Informationen über
  den Schalter anzeigt (bspw. Beschreibungstext und aktuellen Zustand)
  - Bei Schaltern mit Zustandszahl > 2 besteht die Möglichkeit, den gewünschten Zustand über das UI-Element
    auszuwählen
- Die Überprüfung der Korrektheit der Schalterbetätigungen kann zu unterschiedlichen Zeitpunkten geschehen
  - Direkt nach Betätigung eines Schalters
  - Bei einem externen Event (bspw. Ansprechen eines NPCs und Bestätigung über UI-Element)

### Kämpfen (Andre)

## Spielszenario

### Liste an Entitäten (later)

- Name
  - ComponentA
  - ComponentB

### NPC stellt Frage (Andre)
Stellt Frage du gibst Antwort, NPC Happy

### Monster greift mit Lückentext-Frage an  (Andre)
Das Monster rennt in den Spieler und bei kollision wird eine Lückentext-Frage gestellt.

### Truhe ist mit Frage verschlossen (Andre)

### Mehrere Truhen aber nur eine ist richtig (Andre)

### Blöcke müssen in die richtige Reihenfolge geschoben werden

- Eine Menge verschiebbarer Blöcke stehen in einem Raum
- Ein NPC stellt den Aufgabentext: "Bringe die Blöcke in die passende Reihenfolge nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Der Spieler interagiert mit den verschiebbaren Blöcken und bringt sie in die geforderte Reihenfolge
  - Hierbei müssen die Blöcke auf speziellen Feldern auf dem Boden platziert werden
- Wenn der Spieler erneut mit dem NPC interagiert, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung
  erneut angezeigt werden kann, oder die Konfiguration der Blöcke auf den speziellen Felder als Antwort geloggt
  wird
- Wenn die Konfiguration der Blöcke auf den speziellen Feldern korrekt ist, wird der Spieler belohnt, die Belohnung könnte sein
  - eine Textnachricht über UI: "Ja, gut gemacht!"
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die Konfiguration der Blöcke falsch ist, wird der Spieler bestraft, die Bestrafung könnte sein
  - eine Textnachricht über UI: "Nein, das war leider falsch!"
  - Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

### Schalter müssen in der richigen Reihenfolge betätigt werden

- In einem Raum sind mehrere tastende Schalter platziert
- NPC stellt den Aufgabentext: "Betätige die Schalter in der Richtigen Reihnenfolge nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Spieler interagiert mit den verschiedenen Tastern
- Reihenfolge der Betätigungen wird geloggt
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die Reihenfolge als Antwort abgegeben werden
  kann
- Wenn die Reihenfolge der Betätigungen korrekt ist, wird der Spieler belohnt, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Wenn die Reihenfolge der Betätigungen falsch ist, wird der Spieler bestraft, die Bestrafung könnte sein
    - eine Textnachricht über UI: "Nein, das war leider falsch!"
    - Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

### Sammlung an Schalter müssen in die richtigen Zustände gebracht werden

- In einem Raum sind mehrere Schalter mit Zustandsanzahl > 2 platziert
- NPC stellt den Aufgabentext: "Bringe die Schalter in die richtigen Zustände nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Spieler interagiert mit den verschiedenen Schaltern
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die Zustände aller Schalter als Antwort abgegeben werden
  kann
- Wenn die Zustände der Schalter korrekt ist, wird der Spieler belohnt, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Wenn die Zustände der Schalter falsch ist, wird der Spieler bestraft, die Bestrafung könnte sein
    - eine Textnachricht über UI: "Nein, das war leider falsch!"
    - Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

### Monster müssen in der richtigen Reihenfolge gehauen werden

- In einem Raum laufen mehrere unterscheidbare Monster umher
- NPC stellt den Aufgabentext: "Besiege die Monster in der richtigen Reihenfolge nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Spieler kämpft gegen die Monster
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die Reihenfolge der besiegten Monster als
  Antwort geloggt werden kann
- Wenn die Reihenfolge der besiegten Monster korrekt ist, wird der Spieler belohnt, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Wenn die Reihenfolge der besiegten Monster falsch ist, wird der Spieler bestraft, die Bestrafung könnte sein
    - eine Textnachricht über UI: "Nein, das war leider falsch!"
    - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

### Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältniss entsteht (Andre)

### Items müssen in Truhen abgelegt werden

- In einem Raum stehen mehrere unterscheidbare Truhen
- Im Dungeon (nicht nur im Truhen-Raum) sind Items versteckt
- NPC stellt den Aufgabentext: "Finde und platziere Items in den richtigen Truhen nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Spieler sucht im Dungeon nach Items und legt sie in Truhen ab
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die abgelegten Items in den Truhen als
  Antwort geloggt werden kann
- Wenn die richtigen Items in den richtigen Truhen platziert wurden, wird der Spieler belohnt, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Wenn die falschen Items in den falschen Truhen platziert wurden, wird der Spieler bestraft, die Bestrafung könnte sein
    - eine Textnachricht über UI: "Nein, das war leider falsch!"
    - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

### Items müssen aus einer Truhe entfernt werden

- In einem Raum steht eine Truhe, die eine Menge von Items speichert
- NPC stellt den Aufgabentext: "Entferne Items in den aus der Truhe nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Spieler entfernt Items aus der Truhe
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die entfernten Items als
  Antwort geloggt werden kann
- Wenn die richtigen Items entfernt wurden, wird der Spieler belohnt, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Wenn die falschen Items entfernt wurden, wird der Spieler bestraft, die Bestrafung könnte sein
    - eine Textnachricht über UI: "Nein, das war leider falsch!"
    - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

### Items müssen in einen Briefkasten gepackt werden

- In einem Raum steht ein Briefkasten und ein NPC
- Im Dungeon (nicht nur im Briefkasten-Raum) sind Items versteckt
- NPC stellt den Aufgabentext: "Finde und werfe Items in den Briefkasten nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)"
- Spieler sucht im Dungeon nach Items und wirft sie in den Briefkasten
  - Nachdem der Spieler die Items eingeworfen hat, können sie nicht mehr herausgenommen werden
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die abgegebenen Items als
  Antwort geloggt werden kann
- Wenn die richtigen Items abgegeben wurden, wird der Spieler belohnt, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Wenn die falschen Items abgegeben wurden, wird der Spieler bestraft, die Bestrafung könnte sein
    - eine Textnachricht über UI: "Nein, das war leider falsch!"
    - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen beeinflusst werden, ob er
  seine Antwort korrigieren kann und wie so eine korrigierte Antwort in die Bewertung einfließt

## Zuordnung Aufgabentyp und Spielmechaniken (Malte vorbereiten, rest later)

### Notizen (delete later)

                      GUI-Buttons drücken    Mehre Gui Buttosn drücken       Item(s) abgeben        Schalter betätigen          Blöcke Schieben
Single Choice                 x                                                    x                        x

- Matrix
  Spalte: Szenario
  Zeile: Aufgabentyp
  Wert 1 Wenn der Typ in diesem Szenario abgebildet werden kann
  Wert 0 Wenn der Typ in diesem Szenario nicht abgebildet werden kann


## Zuordnung  Spielmechanik und Spielszenario (Malte vorbereiten, rest later)

### Notizen (delete later)

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




