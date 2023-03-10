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

### Ersetzen

Aus einer Reihe aus Elementen (kann auch nur eins sein) muss ein Element durch ein oder mehrere andere Elemente ersetzt werden.
Hierdurch wird die Reihe aus Elementen sowie deren Anzahl verändert.

### Kombinieren

Aus einer Reihe aus Elementen müssen mehrere Elemente (oder alle) zu einem anderen Element zusammengefasst werden.
Hierdurch wird die Reihe aus Elementen sowie deren Anzahl verändert.

### Objekte in der richtigen Reihenfolge auswählen

Es muss in der richtigen Reihenfolge ein Element nach dem anderen aus einer Menge ausgewählt werden.
Es müssen nicht alle Elemente von Anfang an bekannt sein.

### Positionieren

Positionierung bezeichnet im Allgemeinen die Aufgabe, Elemente aus einer Menge (z.B. Antwortmöglichkeiten)
zu Positionen (bspw. Array-Index, Reihe-Spalte Kombination einer Matrix) zuzuordnen. Hierbei muss nicht jedes
Element aus der Menge der Antwortmöglichkeiten auch einer Position zugeordnet werden. Allerdings muss jede Postion
mit einem Element aus der Menge der Antwortmöglichkeiten belegt sein.

## Spielmechanik

### Ein GUI-Button drücken

- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und mehrere Buttons mit Antworten (Text oder Bild) angezeigt
- Der Spieler muss auf den Button mit der Antwort klicken, die er abgeben möchte.

### GUI Checkboxen anhaken

- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und mehrere Buttons mit Antworten (Text oder Bild) angezeigt
- Der Spieler muss auf die Buttons mit der Antwort klicken, die er für richtig hält.
- Mit einem Klick aktiviert er eine nicht aktive (nicht ausgewählte) Antwort oder deaktiviert eine aktivierte Antwort.
- Mit einem Bestätigungsbutton muss der Spieler seine Antworten einloggen.

### GUI Text eingeben

- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und einem Feld zur Texteingabe angezeigt.
- Der Spieler muss seine Antwort in das Eingabefeld schreiben.
- Mit einem Bestätigungsbutton muss der Spieler seine Antworten einloggen.


### Item an NPC abgeben

- Eine Menge an Items ist im Dungeon verteilt (dabei ist nicht relevant, wie und wo der Spieler die
  Items findet und erhält)
- Die Items können vom Spieler aufgesammelt werden
- Bei Interaktion mit einem NPC kann der Spieler ein aufgesammeltes Item auswählen und an den NPC abgeben
  - Hierfür öffnet sich ein UI-Element, welches das Spielerinventar anzeigt und eine Auswahl der Item(s) ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Überprüfung der Korrektheit des abgegebenen Items
- Der NPC reagiert in bestimmter Form auf den Fall, dass das falsche Item abgegeben wurde und in anderer Form
  auf den Fall, dass das korrekte Item abgegeben wurde

### Item(s) in Container ablegen

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

### Item(s) aus Container nehmen

- In einem Container ist eine Menge von Items gespeichert
- Durch Interaktion mit dem Container kann der Spieler eine beliebige Anzahl Items aus dem Container herausnehmen
  - Hierzu öffnet sich ein UI-Element, welches das Inventar des Containers anzeigt
     und die Auswahl der herauszunehmenden Items ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Die Überprüfung der Korrektheit der aus dem Container entnommenen Items kann zu unterschiedlichen Zeitpunkten geschehen
  - Direkt nach dem Schließen des UI-Elements zur Auswahl der Items
  - Bei externem Event (bspw. Betätigung eines Schalters)

### Blöcke schieben

- Im Raum sind verschiedene Blöcke platziert
- Jeder Block kann angeschaut und untersucht werden (dann wirt Text angezeigt)
- Blöcke können geschoben und gezogen werden, um dessen Position zu ändern
- Es gibt spezielle Felder zu denen die Blöcke geschoben werden müsse.
- Die platzierung der Blöcke auf den Felder gibt die Reihenfolge der Blöcke an
- Der Spieler muss die Blöcke untersuchen und sie so Anordnene, dass sie die Aufgabenstellung lösen
- Um seine Anordnung abzugeben, muss er einen Schalter betätigen

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

### Kämpfen

- Im Dungeon laufen Monster herum.
- Der Spieler muss die Monster mit Waffen oder Zaubern angreifen und besiegen.
- Besiegte Monster werden aus dem Spiel gelöscht und können Items fallen lassen.

### Crafting

- Crafting ermöglich die Transmutation einer Menge von Items in eine andere Menge (wobei die Mengen auch nur
  aus einem einzelnen Element bestehen können)
- Ein Beispiel für die Transmutation einer Item-Menge mit Elementzahl > 1 in eine Item-Menge mit Elementzahl = 1 wäre ein
  "Zauberkessel", in den Items (evtl. unter Berücksichtigung der Reihenfolge) eingeworfen werden können und so zu
  einem neuen Item kombiniert werden können
- Ein Beispiel für die Transmutation einer Item-Menge mit Elementzahl = 1 in eine Item-Menge mit Elementzahl > 1 wäre
  die Anwendung eines "Zauberspruchs" (evtl. auch *genau des richtigen Zauberspruchs*) auf ein Item, um eine Item-Menge
  mit neuen Items zu erzeugen und so die ursprüngliche Item-Menge zu ersetzen
    - Hierfür könnte der Spieler einen "Zaubertisch"/"Zauberbuch" nutzen, welches als Entität im Spiel platziert ist
- Für das Crafting öffnet sich bei der Interaktion mit dem "Zauberkessel" / "Zaubertisch" ein UI-Element, in dem
  Items und "Zaubersprüche" ausgewählt werden können und Buttons zum "Verzaubern" zur Verfügung stehen
  - Der Aufbau dieses UI-Elements und die genaue Funktion der Bedienelemente ist nicht trivial und muss noch genau
    definiert werden

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

### Schalter müssen in der richtigen Reihenfolge betätigt werden

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

### Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältniss entsteht

Der Spieler kommt in einen Raum mit verschiedenen Monstern.
Der Questgeber erklärt den Spieler, die Aufgabe.
Der Spieler muss die Monster so bekämpfen, dass das richtige Verhältniss erreicht ist.

Löst der Spieler die Aufgabe richtig, bekommt er eine Belohnung.
Löst der Spieler die Aufgabe falsch, bekommt er keine Belohnung.

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

### Items müssen in Crafting-Container geworfen werden

- In einem Raum steht ein Crafting-Container und ein NPC
- Im Dungeon (nicht nur im Crafting-Container Raum) sind Items versteckt
- NPC stellt den Aufgabentext: "Finde und werfe Items in den Zauberkessel nach folgender Vorgabe: (abhängig von der
  konkreten Aufgabenstellung)"
- Spieler sucht im Dungeon nach Items und wirft sie in den Crafting-Container
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die Aufgabenstellung erneut
  angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder die abgegebenen Items als
  Antwort geloggt werden können
- Wenn die richtigen Items abgegeben wurden, wird der Spieler belohnt und die abgegebenen Items
  werden zu einem neuen Item kombiniert, die Belohnung könnte sein
    - eine Textnachricht über UI: "Ja, gut gemacht!"
    - ein Item, was vom NPC fallen gelassen wird
- Je nach Aufgabenstellung könnte das neu erzeugte Item wieder eine "Zutat" für eine weitere Kombination sein

## Zuordnung Aufgabentyp und Spielmechaniken (Malte vorbereiten, rest later)

|                                      | **GUI Button drücken** | **GUI Checkboxen anhaken** | **GUI Text eingeben** | **Item an NPC abgeben** | **Item(s) in Container ablegen** | **Item(s) aus Container nehmen** | **Blöcke schieben** | **Schalter betätigen** | **Kämpfen** | **Crafting** |
|--------------------------------------|------------------------|----------------------------|-----------------------|-------------------------|----------------------------------|----------------------------------|---------------------|------------------------|-------------|--------------|
| **Single Choice**                    | X                      |                            |                       | X                       | X                                | X                                |                     | X                      | X           |              |
| **Multiple Choice**                  |                        | X                          |                       |                         | X                                | X                                |                     | X                      | X           |              |
| **Lückentext**                       |                        |                            | X                     |                         |                                  |                                  |                     |                        |             |              |
| **Ersetzen**                         |                        |                            |                       | (X)                     | (X)                              | (X)                              |                     |                        |             | X            |
| **Kombinieren**                      |                        |                            |                       | (X)                     | (X)                              | (X)                              |                     |                        |             | X            |
| **Objekte in Reihenfolge auswählen** |                        |                            |                       |                         |                                  |                                  |                     | X                      | X           |              |
| **Positionieren**                    |                        |                            | (X)                   | X                       | X                                | X                                | X                   |                        |             |              |

Note: `(X)` bedeutet, dass eine Umsetzung eines Aufgabentyps mit einer Mechanik möglich wäre, allerdings (vermutlich)
mit viel Aufwand verbunden ist und die Mechanik nur "über Umwege" für den Aufgabentyp eignet.

Note: Aktuell noch keine wirklich geeignete Mechanik fürs **Ersetzen**, **Kombinieren**

## Zuordnung  Spielmechanik und Spielszenario (Malte vorbereiten, rest later)

|                                  | **NPC stellt Frage** | **Monster greift mit Frage an**   | **Truhe ist mit Frage verschlossen** | **Mehrere Truhen aber nicht alle sind richtig** | **Blöcke müssen in die richtige Reihenfolge geschoben werden** | **Schalter müssen in der richtigen Reihenfolge betätigt werden** | **Sammlung an Schaltern müssen in die richtigen Zustände gebracht werden** | **Monster müssen in der richtigen Reihenfolge gehauen werden** | **Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältnis entsteht** | **Items müssen in Truhen abgelegt werden** | **Items müssen aus einer Truhe entfernt werden** | **Items müssen in einen Briefkasten gepackt werden** | **Items müssen in Crafting-Container geworfen werden** |
|----------------------------------|----------------------|-----------------------------------|--------------------------------------|-------------------------------------------------|----------------------------------------------------------------|------------------------------------------------------------------|----------------------------------------------------------------------------|----------------------------------------------------------------|---------------------------------------------------------------------------------------------|--------------------------------------------|--------------------------------------------------|------------------------------------------------------|--------------------------------------------------------|
| **GUI Button drücken**           | X                    | X                                 | X                                    |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **GUI Checkboxen anhaken**       | X                    | X                                 | X                                    |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **GUI Text eingeben**            | X                    | X                                 | X                                    |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **Item an NPC abgeben**          |                      |                                   |                                      |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  | X                                                    |                                                        |
| **Item(s) in Container ablegen** |                      |                                   |                                      | X                                               |                                                                |                                                                  |                                                                            |                                                                |                                                                                             | X                                          |                                                  | X                                                    |                                                        |
| **Item(s) aus Container nehmen** |                      |                                   |                                      | X                                               |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            | X                                                |                                                      |                                                        |
| **Blöcke schieben**              |                      |                                   |                                      |                                                 | X                                                              |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **Schalter betätigen**           |                      |                                   |                                      |                                                 |                                                                | X                                                                | X                                                                          |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **Kämpfen**                      |                      |                                   |                                      |                                                 |                                                                |                                                                  |                                                                            | X                                                              | X                                                                                           |                                            |                                                  |                                                      |                                                        |
| **Crafting**                     |                      |                                   |                                      |                                                 |                                                                |                                                                  |                                                                            | X                                                              | X                                                                                           |                                            |                                                  |                                                      | X                                                      |

## Konkrete Aufgaben

### Regex-monster
Aufgabe: "Geben Sie einen gültigen Regulären ausdruck an, der Telefonnummern mit mindestens 7 aber maximal 10 Ziffern beschreibt."
Aufgabentyp: Lückentext
Spielmechanik: GUI Text eingabe
Spielszenario: Monster greift mit Frage an

### Pattern erkennen (PM)
Aufgabe: "Ordnen Sie die UML-Klassendiagramme den richtigen Pattern zu."
UMLs: *liste an Pfaden mit Bilddateien*.  (UML-A,UML-B,UML-C)
Lösungsvorschläge: "Strategy-Pattern", "Observer-Pattern", "Visitor-Pattern", "Das ist kein Pattern", "Builder-Pattern"
Aufgabentyp: Zuordnen
Spielmechanik: Item(s) in Container tuen
Spielszenario: Items müssen in Truhen abgelegt werden

### Sortieralgorithmus (ADS)
Aufgabe: "Sortieren Sie das Array [12,4,31,-3,3] mit dem Bubblesort Algorithmus. Geben Sie das Array nach dem dritten Schritt an.
Aufgabentyp: Sortieren
Spielmechanik: Blöcke schieben
Spielszenario: Blöcke müssen in die richtige Reihenfolge geschoben werden

### CSP mit Forward Checking lösen (KI)
Aufgabe: "(Bild von Karte mit verschieden Ländern und Färbungen) Geben sie für jede Variable jeden gültigen Zustand an, wenn Sie die Backtracking Search verwenden."
Aufgabentyp: Zuordnen
Spielmechanik: Item(s) aus Container nehmen
Spielszenario: Items müssen aus einer Trhue entfernt werden
Spielmechanik: Gui Text eingeben
Spielszenario: Monster greift mit Lückentext-Frage an



