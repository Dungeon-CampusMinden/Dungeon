---
title: "Aufgaben"
---

## Begriffsklärung

Folgende Begriffe haben im Kontext der Aufgabenbeschreibung eine besondere Bedeutung und
werden daher hier aus Sicht der aufgabendefinierenden Lehrperson definiert:

### Aufgabentyp

Ein Aufgabentyp beschreibt in abstrakter Weise, welche Datenstrukturen nötig sind und in
welcher Form sie zur Bearbeitung und zur Bewertung einer Aufgabe verarbeitet werden.

Ein Beispiel hierfür ist der Aufgabentyp Single Choice, bei dem aus einer Menge mehrerer
Elemente das eine korrekte Element ausgewählt werden muss. Ein weiteres Beispiel wäre eine
Sortieraufgabe, bei der eine festgelegte Anzahl an Elementen in die korrekte Reihenfolge
gebracht werden muss.

### Entitätstyp

Ein Entitätstyp beschreibt eine definierte Zusammenstellung aus Komponenten die eine
bestimmte Rolle in einer Spielmechanik einnehmen. Ein Beispiel für einen Entitätstyp wäre
“Container”. Eine “Container”-Entität könnte folgende Komponenten enthalten:

- AnimationComponent (zum Anzeigen einer Textur und zum Abspielen von Animationen, bspw. beim
  Öffnen des Containers)
- InventoryComponent (zum Speichern von Items im Container)
- InteractionComponent (um dem Spielcharacter die Möglichkeit der Interaktion mit dem
  Container zu geben)

Ein Entitätstyp definiert nicht die konkreten Werte in den enthaltenen Komponenten. Die
konkreten Werte werden erst durch konkrete Ausprägungen der Entitätstypen definiert. Einige
Beispiele für Ausprägungen des “Container”-Entitätstyps sind:

- Briefkasten
  - AnimationComponent speicher Briefkastentexturen
  - InteractionComponent ruft ein Verhalten auf, welches nur das Transferieren eines Items
    aus dem Spielerinventar in das InventoryComponent des Briefkastens zulässt
- Truhe
  - AnimationCompnent speichert Truhentextur
  - InteractionComponent überprüft eine externe Bedingung (z.B., ob die Korrekte
    Antwort auf eine Frage gegeben wurde) und transferiert bei Erfüllung der Bedingung alle
    Items aus dem InventoryComponent in das Inventar des Spielercharakters

**Note:** Das beschriebene Verhalten der Ausprägungen dient nur der Veranschaulichung und
kann von dem tatsächlichen Verhalten der (noch nicht implementierten) Ausprägungen
abweichen.

### Spielmechanik

Eine Spielmechanik beschreibt die Interaktion von verschiedenen Entitätstypen im Spiel.
Spielmechaniken beziehen sich ausschließlich auf die Interaktion von Spielelementen, die aus
Game-Design Perspektive relevant sind (siehe [Steuermechanismen](#steuermechanismen) für
alle anderen “Mechaniken”). Beispiel: ein Item, welches im Dungeon liegt, kann aufgehoben
und in das Inventar des Spielcharakters transferiert werden.

Aus einer Spielmechanik kann abgeleitet werden, welche Komponenten an der Mechanik beteiligt
sind. Für obiges Beispiel sind folgende Komponenten erforderlich:

- InventoryComponent im Spielcharakter (zur Realisierung des Spielerinventars)
- CollisionComponent im Spielcharakter und im Item, um ein Event auszulösen, sobald der
  Spielcharakter über das Item läuft
- alternativ ein InteractionComponent im Item, falls das Item per dedizierter
  Spielendenaktion (bspw. Tastendruck) aufgehoben werden soll

TODO: Frage für morgen: nochmal Unterscheidung zwischen optischer Ausprägung und
funktionaler Ausprägung machen? Und wenn ja, wo (ist das dann einfach ein anderer
Entitätstyp, oder eine andere Ausprägung)?

### Spielszenario

Ein Spielszenario beschreibt, welche Spielmechaniken, welche Entitätstypen und auf welche
Art diese in einem konkreten Dungeonlevel in Verbindung gebracht werden, um eine abstrakte
Aufgabe zu realisieren. Ein Spielszenario beschreibt nicht, welche visuellen

Beispiel: In einem Raum befindet sich ein NPC und verschiedene Schalter. Der NPC stellt dem
Spieler eine Frage und sagt ihm, welcher Hebel für welche Antwort steht.

### Steuermechanismen

Steuermechanismen sind alle Mechanismen des Systems, die den Ablauf der Umsetzung einer
Aufgabe in ein Spielszenario beeinflussen.

Steuermechanismen grenzen sich von Spielmechaniken ab, indem sie die übergeordneten
Mechanismen realisieren, die sich nicht ausschließlich auf Game-Design Elemente beziehen.
Hierzu zählen unter anderem das Logging von Antworten, die Organisation der Aufgabenfolge
und Aufgabenverschachtelung.

## Aufgabentypen

### Single-Choice

Die Aufgabenstellung besteht aus einem Fragetext und einer Menge an vorgegebenen
Antwortmöglickeiten. Nur eine Antwort ist richtig. Die richtige Antwort muss ausgewählt
werden.

### Multiple-Choice

Die Aufgabenstellung besteht aus einem Fragetext und einer Menge an vorgegebenen
Antwortmöglickeiten. Mehrere Antworten sind richtig. Es müssen alle richtigen Antworten
markiert werden. Es dürfen keine falschen Antworten markiert werden. Die Reihenfolge spielt
keine Rolle.

### Lückentext

Die Aufgabenstellung besteht aus einem Fragetext. Die Antwort besteht aus einem oder
mehreren Freitexten. Es muss jeweils die richtige Antwort in den Freitext-Stellen eingegeben
werden. Die richtige Antwort kann ein konkreter Wert oder eine Eingabe passend zu einem
regulären Ausdruck sein.

### Ersetzen

Aus einer Gesamtmenge aus $l$ Elementen (mit $0 < l$) muss eine Menge mit $m$ Elementen (mit $0 < n$) durch eine
zweite Menge mit $m$ Elementen (mit $0 < m$) ersetzt werden.
Hierdurch wird die Gesamtmenge der Elemente sowie die Anzahl der enthaltenen Elemente verändert.

### Zuordnen

Zuordnen bezeichnet im Allgemeinen die Aufgabe, Elemente aus einer Menge $A$ (z.B.
Antwortmöglichkeiten) die Elemente aus einer anderen Menge $B$ zuzuordnen (bspw. Array-Index,
Reihe-Spalte Kombination einer Matrix). Hierbei muss nicht jedes Element aus $A$ der
Antwortmöglichkeiten auch einem Element aus $B$ zugeordnet werden. Allerdings muss jedem Element aus $B$ ein
Element aus $A$ zugeordnet werden. Die gesamte Menge $A$ der Antwortmöglichkeiten muss den Studierenden
bekannt sein, bevor sie den ersten Zuordnungsschritt machen müssen.

### Elemente in der richtigen Reihenfolge auswählen

Es muss in der richtigen Reihenfolge ein Element nach dem anderen aus einer Menge $A$ ausgewählt
werden.
Den Studierenden müssen nicht alle Elemente aus $A$ bekannt sein, bevor sie das erste Element
auswählen müssen.

## Spielmechaniken

### Ein GUI-Button drücken

- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und
  mehrere Buttons mit Antworten (Text oder Bild) angezeigt
- Der Spieler muss auf den Button mit der Antwort klicken, die er abgeben möchte.

### GUI Checkboxen anhaken

- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und
  mehrere Buttons mit Antworten (Text oder Bild) angezeigt
- Der Spieler muss auf die Buttons mit der Antwort klicken, die er für richtig hält.
- Mit einem Klick aktiviert er eine nicht aktive (nicht ausgewählte) Antwort oder
  deaktiviert eine aktivierte Antwort.
- Mit einem Bestätigungsbutton muss der Spieler seine Antworten einloggen.

### GUI Text eingeben

- Es wird eine GUI bestehend aus einem Feld für den Aufgabentext (Text oder Bild) und einem
  Feld zur Texteingabe angezeigt.
- Der Spieler muss seine Antwort in das Eingabefeld schreiben.
- Mit einem Bestätigungsbutton muss der Spieler seine Antworten einloggen.

### Item(s) an NPC abgeben

- Eine Menge an Items ist im Dungeon verteilt (dabei ist nicht relevant, wie und wo der
  Spieler die Items findet und erhält)
- Die Items können vom Spieler aufgesammelt werden
- Bei Interaktion mit einem NPC kann der Spieler ein aufgesammeltes Item auswählen und an
  den NPC abgeben
  - Hierfür öffnet sich ein UI-Element, welches das Spielerinventar anzeigt und eine Auswahl
    der Item(s) ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Überprüfung der Korrektheit des abgegebenen Items
- Der NPC reagiert in bestimmter Form auf den Fall, dass das falsche Item abgegeben wurde
  und in anderer Form auf den Fall, dass das korrekte Item abgegeben wurde

### Item(s) in Container ablegen

- Eine Menge an Items ist im Dungeon verteilt (dabei ist nicht relevant, wie und wo der
  Spieler die Items findet und erhält)
- Die Items können vom Spieler aufgesammelt werden
- Bei Interaktion mit Container kann der Spieler eine beliebige Anzahl der gesammelten Items
  in den Container legen
  - Hierfür öffnet sich ein UI-Element, welches das Spielerinventar anzeigt und eine Auswahl
    der Item(s) ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Die Überprüfung der Korrektheit der im Container abgelegten Items kann zu
  unterschiedlichen Zeitpunkten geschehen
  - Direkt nach dem Schließen des UI-Elements zur Auswahl der Items
  - Bei externem Event (bspw. Betätigung eines Schalters)

### Item(s) aus Container nehmen

- In einem Container ist eine Menge von Items gespeichert
- Durch Interaktion mit dem Container kann der Spieler eine beliebige Anzahl Items aus dem
  Container herausnehmen
  - Hierzu öffnet sich ein UI-Element, welches das Inventar des Containers anzeigt und die
    Auswahl der herauszunehmenden Items ermöglicht
  - Hierbei werden Informationen über die Items angezeigt
- Die Überprüfung der Korrektheit der aus dem Container entnommenen Items kann zu
  unterschiedlichen Zeitpunkten geschehen
  - Direkt nach dem Schließen des UI-Elements zur Auswahl der Items
  - Bei externem Event (bspw. Betätigung eines Schalters)

### Blöcke schieben

- Im Raum sind verschiedene Blöcke platziert
- Jeder Block kann angeschaut und untersucht werden (dann wird Text angezeigt)
- Blöcke können geschoben und gezogen werden, um dessen Position zu ändern
- Es gibt spezielle Felder, auf die die Blöcke geschoben werden müssen
- Die Platzierung der Blöcke auf den Feldern gibt die Reihenfolge der Blöcke an
- Der Spieler muss die Blöcke untersuchen und sie so anordnen, dass sie die Aufgabenstellung
  lösen
- Um seine Anordnung abzugeben, muss er einen Schalter betätigen

### Schalter betätigen

- Aus einer Menge von Schaltern müssen ein oder mehr korrekte Schalter ausgewählt werden
- Die Schalter können dabei entweder tastend, schaltend oder mit einer Zustandszahl > 2
  agieren
- Bei Interaktion mit einem Schalter öffnet sich ein UI-Element, welches Informationen über
  den Schalter anzeigt (bspw. Beschreibungstext und aktuellen Zustand)
  - Bei Schaltern mit Zustandszahl > 2 besteht die Möglichkeit, den gewünschten Zustand
    über das UI-Element auszuwählen
- Die Überprüfung der Korrektheit der Schalterbetätigungen kann zu unterschiedlichen
  Zeitpunkten geschehen
  - Direkt nach Betätigung eines Schalters
  - Bei einem externen Event (bspw. Ansprechen eines NPCs und Bestätigung über UI-Element)

### Kämpfen

- Im Dungeon laufen Monster herum.
- Der Spieler muss die Monster mit Waffen oder Zaubern angreifen und besiegen.
- Besiegte Monster werden aus dem Spiel gelöscht und können Items fallen lassen.

### Crafting

- Crafting ermöglich die Transmutation einer Menge von Items in eine andere Menge (wobei die
  Mengen auch nur aus einem einzelnen Element bestehen können).
- Ein Beispiel für die Transmutation einer Item-Menge mit Elementzahl > 1 in eine
  Item-Menge mit Elementzahl = 1 wäre ein “Zauberkessel”, in den Items (evtl. unter
  Berücksichtigung der Reihenfolge) eingeworfen werden können und so zu einem neuen Item
  kombiniert werden können.
- Ein Beispiel für die Transmutation einer Item-Menge mit Elementzahl = 1 in eine Item-Menge
  mit Elementzahl > 1 wäre die Anwendung eines “Zauberspruchs” (evtl. auch *genau des
  richtigen Zauberspruchs*) auf ein Item, um eine Item-Menge mit neuen Items zu erzeugen und
  so die ursprüngliche Item-Menge zu ersetzen.
  - Hierfür könnte der Spieler einen “Zaubertisch”/“Zauberbuch” nutzen, welches als Entität
    im Spiel platziert ist.
- Für das Crafting öffnet sich bei der Interaktion mit dem “Zauberkessel” / “Zaubertisch”
  ein UI-Element, in dem Items und “Zaubersprüche” ausgewählt werden können und Buttons zum
  “Verzaubern” zur Verfügung stehen.
  - Der Aufbau dieses UI-Elements und die genaue Funktion der Bedienelemente ist nicht
    trivial und muss noch genau definiert werden.

## Spielszenarien

### Liste an Entitäten (later)

- Name
  - ComponentA
  - ComponentB

### NPC stellt Frage

Im Level steht ein Questgeber, mit dem der Spieler reden kann. Im Gespräch stellt der NPC
dem Spieler eine Frage, die er beantworten muss. Beantwortet der Spieler die Frage richtig,
bekommt er eine Belohnung. Beantwortet der Spieler die Frage falsch, bekommt er keine
Belohnung.

### Monster greift mit Frage an

Im Level läuft ein Monster herum. Wenn das Monster den Spieler sieht, läuft es in den
Spieler hinein (bis zur Kollision). Bei Kollision stellt das Monster dem Spieler eine Frage.
Beantwortet der Spieler die Frage richtig, stirbt das Monster. Beantwortet der Spieler die
Frage falsch, bekommt er viel Schaden und das Monster rennt weg.

### Truhe ist mit Frage verschlossen

Im Level steht eine Schatzkiste herum. Wenn der Spieler die Schatzkiste öffnen will, stellt
diese ihm eine Frage. Beantwortet der Spieler die Frage richtig, geht die Schatzkiste auf.
Beantwortet der Spieler die Frage falsch, bleibt die Schatzkiste verschlossen.

### Mehrere Truhen aber nicht alle sind richtig

Der Spieler kommt in einen Raum. Der Questgeber stellt ihm eine Frage. Die
Antwortmöglichkeiten sind in Form von Schatzkisten im Raum verteilt. Wenn der Spieler mit
den Schatzkisten interagiert, sagen diese ihm, welche Antwort sie sind. Der Spieler muss die
Schatzkiste/n mit der/den richtigen Antwort/en öffnen.

Beantwortet der Spieler die Frage richtig, bekommt er eine Belohnung. Beantwortet der
Spieler die Frage falsch, bekommt er keine Belohnung.

### Blöcke müssen in die richtige Reihenfolge geschoben werden

- Eine Menge verschiebbarer Blöcke stehen in einem Raum
- Ein NPC stellt den Aufgabentext: “Bringe die Blöcke in die passende Reihenfolge nach
  folgender Vorgabe: (abhängig von der konkreten Aufgabenstellung)”
- Der Spieler interagiert mit den verschiebbaren Blöcken und bringt sie in die geforderte
  Reihenfolge
  - Hierbei müssen die Blöcke auf speziellen Feldern auf dem Boden platziert werden
- Wenn der Spieler erneut mit dem NPC interagiert, öffnet sich ein UI-Element, in dem
  entweder die Aufgabenstellung erneut angezeigt werden kann, oder die Konfiguration der
  Blöcke auf den speziellen Felder als Antwort geloggt wird
- Wenn die Konfiguration der Blöcke auf den speziellen Feldern korrekt ist, wird der Spieler
  belohnt, die Belohnung könnte sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die Konfiguration der Blöcke falsch ist, wird der Spieler bestraft, die Bestrafung
  könnte sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Schalter müssen in der richtigen Reihenfolge betätigt werden

- In einem Raum sind mehrere tastende Schalter platziert
- NPC stellt den Aufgabentext: “Betätige die Schalter in der richtigen Reihenfolge nach
  folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Spieler interagiert mit den verschiedenen Tastern
- Reihenfolge der Betätigungen wird geloggt
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die Reihenfolge als Antwort abgegeben werden kann
- Wenn die Reihenfolge der Betätigungen korrekt ist, wird der Spieler belohnt, die Belohnung
  könnte sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die Reihenfolge der Betätigungen falsch ist, wird der Spieler bestraft, die
  Bestrafung könnte sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Sammlung an Schalter müssen in die richtigen Zustände gebracht werden

- In einem Raum sind mehrere Schalter mit Zustandsanzahl \> 2 platziert
- NPC stellt den Aufgabentext: “Bringe die Schalter in die richtigen Zustände nach folgender
  Vorgabe: (abhängig von der konkreten Aufgabenstellung)”
- Spieler interagiert mit den verschiedenen Schaltern
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die Zustände aller Schalter als Antwort abgegeben werden können
- Wenn die Zustände der Schalter korrekt ist, wird der Spieler belohnt, die Belohnung könnte
  sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die Zustände der Schalter falsch ist, wird der Spieler bestraft, die Bestrafung
  könnte sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Monster müssen in der richtigen Reihenfolge gehauen werden

- In einem Raum laufen mehrere unterscheidbare Monster umher
- NPC stellt den Aufgabentext: “Besiege die Monster in der richtigen Reihenfolge nach
  folgender Vorgabe: (abhängig von der konkreten Aufgabenstellung)”
- Spieler kämpft gegen die Monster
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die Reihenfolge der besiegten Monster als Antwort geloggt werden kann
- Wenn die Reihenfolge der besiegten Monster korrekt ist, wird der Spieler belohnt, die
  Belohnung könnte sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die Reihenfolge der besiegten Monster falsch ist, wird der Spieler bestraft, die
  Bestrafung könnte sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Verschiedene Monsterarten müssen so verprügelt werden, dass das richtige Verhältnis zwischen den Monsterarten entsteht

Der Spieler kommt in einen Raum mit verschiedenen Monstern. Der Questgeber erklärt den
Spieler, die Aufgabe. Der Spieler muss die Monster so bekämpfen, dass das richtige
Verhältnis zwischen den Arten erreicht ist.

Löst der Spieler die Aufgabe richtig, bekommt er eine Belohnung. Löst der Spieler die
Aufgabe falsch, bekommt er keine Belohnung.

### Items müssen in Truhen abgelegt werden

- In einem Raum stehen mehrere unterscheidbare Truhen
- Im Dungeon (nicht nur im Truhen-Raum) sind Items versteckt
- NPC stellt den Aufgabentext: “Finde und platziere Items in den richtigen Truhen nach
  folgender Vorgabe: (abhängig von der konkreten Aufgabenstellung)”
- Spieler sucht im Dungeon nach Items und legt sie in Truhen ab
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgelegten Items in den Truhen als Antwort geloggt werden können
- Wenn die richtigen Items in den richtigen Truhen platziert wurden, wird der Spieler
  belohnt, die Belohnung könnte sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die falschen Items in den falschen Truhen platziert wurden, wird der Spieler
  bestraft, die Bestrafung könnte sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Items müssen aus einer Truhe entfernt werden

- In einem Raum steht eine Truhe, die eine Menge von Items speichert
- NPC stellt den Aufgabentext: “Entferne Items in den aus der Truhe nach folgender Vorgabe:
  (abhängig von der konkreten Aufgabenstellung)”
- Spieler entfernt Items aus der Truhe
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die entfernten Items als Antwort geloggt werden können
- Wenn die richtigen Items entfernt wurden, wird der Spieler belohnt, die Belohnung könnte
  sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die falschen Items entfernt wurden, wird der Spieler bestraft, die Bestrafung könnte
  sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Items müssen in einen Briefkasten gepackt werden

- In einem Raum steht ein Briefkasten und ein NPC
- Im Dungeon (nicht nur im Briefkasten-Raum) sind Items versteckt
- NPC stellt den Aufgabentext: “Finde und werfe Items in den Briefkasten nach folgender
  Vorgabe: (abhängig von der konkreten Aufgabenstellung)”
- Spieler sucht im Dungeon nach Items und wirft sie in den Briefkasten
  - Nachdem der Spieler die Items eingeworfen hat, können sie nicht mehr herausgenommen
    werden
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgegebenen Items als Antwort geloggt werden können
- Wenn die richtigen Items abgegeben wurden, wird der Spieler belohnt, die Belohnung könnte
  sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Wenn die falschen Items abgegeben wurden, wird der Spieler bestraft, die Bestrafung könnte
  sein
  - eine Textnachricht über UI: “Nein, das war leider falsch!”
  - mehr Monster spawnen, sodass der Spieler kämpfen muss
- Falls der Spieler die falsche Antwort gegeben hat, kann durch Steuermechanismen
  beeinflusst werden, ob er seine Antwort korrigieren kann und wie so eine korrigierte
  Antwort in die Bewertung einfließt

### Items müssen in Crafting-Container geworfen werden

- In einem Raum steht ein Crafting-Container und ein NPC
- Im Dungeon (nicht nur im Crafting-Container Raum) sind Items versteckt
- NPC stellt den Aufgabentext: “Finde und werfe Items in den Zauberkessel nach folgender
  Vorgabe: (abhängig von der konkreten Aufgabenstellung)”
- Spieler sucht im Dungeon nach Items und wirft sie in den Crafting-Container
- Bei erneutem Interagieren mit NPC, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgegebenen Items als Antwort geloggt werden können
- Wenn die richtigen Items abgegeben wurden, wird der Spieler belohnt und die abgegebenen
  Items werden zu einem neuen Item kombiniert, die Belohnung könnte sein
  - eine Textnachricht über UI: “Ja, gut gemacht!”
  - ein Item, was vom NPC fallen gelassen wird
- Je nach Aufgabenstellung könnte das neu erzeugte Item wieder eine “Zutat” für eine weitere
  Kombination sein

## Zuordnung Aufgabentyp und Spielmechaniken

|                                      | **GUI Button drücken** | **GUI Checkboxen anhaken** | **GUI Text eingeben** | **Item an NPC abgeben** | **Item(s) in Container ablegen** | **Item(s) aus Container nehmen** | **Blöcke schieben** | **Schalter betätigen** | **Kämpfen** | **Crafting** |
|--------------------------------------|------------------------|----------------------------|-----------------------|-------------------------|----------------------------------|----------------------------------|---------------------|------------------------|-------------|--------------|
| **Single Choice**                    | X                      |                            |                       | X                       | X                                | X                                |                     | X                      | X           |              |
| **Multiple Choice**                  |                        | X                          |                       |                         | X                                | X                                |                     | X                      | X           |              |
| **Lückentext**                       |                        |                            | X                     |                         |                                  |                                  |                     |                        |             |              |
| **Ersetzen**                         |                        |                            |                       | \(X\)                   | \(X\)                            | \(X\)                            |                     |                        |             | X            |
| **Objekte in Reihenfolge auswählen** |                        |                            |                       |                         |                                  |                                  |                     | X                      | X           |              |
| **Zuordnen**                         |                        |                            | \(X\)                 | X                       | X                                | X                                | X                   |                        |             |              |

Note: `(X)` bedeutet, dass eine Umsetzung eines Aufgabentyps mit einer Mechanik möglich
wäre, allerdings (vermutlich) mit viel Aufwand verbunden ist und die Mechanik nur “über
Umwege” für den Aufgabentyp eignet.

## Zuordnung Spielmechanik und Spielszenario

|                                  | **NPC stellt Frage** | **Monster greift mit Frage an** | **Truhe ist mit Frage verschlossen** | **Mehrere Truhen aber nicht alle sind richtig** | **Blöcke müssen in die richtige Reihenfolge geschoben werden** | **Schalter müssen in der richtigen Reihenfolge betätigt werden** | **Sammlung an Schaltern müssen in die richtigen Zustände gebracht werden** | **Monster müssen in der richtigen Reihenfolge gehauen werden** | **Verschiedene Monster müssen so verprügelt werden, dass das richtige Verhältnis entsteht** | **Items müssen in Truhen abgelegt werden** | **Items müssen aus einer Truhe entfernt werden** | **Items müssen in einen Briefkasten gepackt werden** | **Items müssen in Crafting-Container geworfen werden** |
|----------------------------------|----------------------|---------------------------------|--------------------------------------|-------------------------------------------------|----------------------------------------------------------------|------------------------------------------------------------------|----------------------------------------------------------------------------|----------------------------------------------------------------|---------------------------------------------------------------------------------------------|--------------------------------------------|--------------------------------------------------|------------------------------------------------------|--------------------------------------------------------|
| **GUI Button drücken**           | X                    | X                               | X                                    |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **GUI Checkboxen anhaken**       | X                    | X                               | X                                    |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **GUI Text eingeben**            | X                    | X                               | X                                    |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **Item an NPC abgeben**          |                      |                                 |                                      |                                                 |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  | X                                                    |                                                        |
| **Item(s) in Container ablegen** |                      |                                 |                                      | X                                               |                                                                |                                                                  |                                                                            |                                                                |                                                                                             | X                                          |                                                  | X                                                    |                                                        |
| **Item(s) aus Container nehmen** |                      |                                 |                                      | X                                               |                                                                |                                                                  |                                                                            |                                                                |                                                                                             |                                            | X                                                |                                                      |                                                        |
| **Blöcke schieben**              |                      |                                 |                                      |                                                 | X                                                              |                                                                  |                                                                            |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **Schalter betätigen**           |                      |                                 |                                      |                                                 |                                                                | X                                                                | X                                                                          |                                                                |                                                                                             |                                            |                                                  |                                                      |                                                        |
| **Kämpfen**                      |                      |                                 |                                      |                                                 |                                                                |                                                                  |                                                                            | X                                                              | X                                                                                           |                                            |                                                  |                                                      |                                                        |
| **Crafting**                     |                      |                                 |                                      |                                                 |                                                                |                                                                  |                                                                            | X                                                              | X                                                                                           |                                            |                                                  |                                                      | X                                                      |

## Konkrete Aufgaben

### Regex-monster

Aufgabe: “Geben Sie einen gültigen Regulären ausdruck an, der Telefonnummern mit mindestens
7 aber maximal 10 Ziffern beschreibt.” Aufgabentyp: Lückentext Spielmechanik: GUI Text
eingabe Spielszenario: Monster greift mit Frage an

### Pattern erkennen (PM)

Aufgabe: “Ordnen Sie die UML-Klassendiagramme den richtigen Pattern zu.” UMLs: *liste an
Pfaden mit Bilddateien*. (UML-A,UML-B,UML-C) Lösungsvorschläge: “Strategy-Pattern”,
“Observer-Pattern”, “Visitor-Pattern”, “Das ist kein Pattern”, “Builder-Pattern”
Aufgabentyp: Zuordnen Spielmechanik: Item(s) in Container tuen Spielszenario: Items müssen
in Truhen abgelegt werden

### Sortieralgorithmus (ADS)

Aufgabe: “Sortieren Sie das Array [12,4,31,-3,3] mit dem Bubblesort Algorithmus. Geben Sie
das Array nach dem dritten Schritt an. Aufgabentyp: Sortieren Spielmechanik: Blöcke schieben
Spielszenario: Blöcke müssen in die richtige Reihenfolge geschoben werden

### CSP mit Forward Checking lösen (KI)

Aufgabe: “(Bild von Karte mit verschieden Ländern und Färbungen) Geben sie für jede Variable
jeden gültigen Zustand an, wenn Sie die Backtracking Search verwenden.” Aufgabentyp:
Zuordnen Spielmechanik: Item(s) aus Container nehmen Spielszenario: Items müssen aus einer
Trhue entfernt werden Spielmechanik: Gui Text eingeben Spielszenario: Monster greift mit
Lückentext-Frage an

## Steuermechanismen

### Petri-Netz zur Aufgabenverschachtelung

TODO: erklärung "Aufgabe ist aktiv"


Zustände der Aufgaben
- **inaktiv**: die Aufgabe wird den Studierenden nicht angezeigt und die mit der Aufgabe verknüpften
  Entitäten zeigen in einer definierten Form an, dass die entsprechende Aufgabe noch nicht
  aktiviert wurde oder sind nicht interagierbar
- **aktiv ohne Bearbeitung**: die Aufgabe wird den Studierenden im Questlog angezeigt, die verknüpften
  Entitäten verhalten sich wie bei **inaktiv**
- **aktiv mit Bearbeitung**: die Aufgabe wird den Studierenden im Questlog angezeigt, mit den verknüpften
  Entitäten kann interagiert werden, um eine Antwort auf die Aufgabe zu geben
- **fertig bearbeitet**: Die Studierenden haben eine Antwort für eine Aufgabe abgegeben, hat Feedback darüber bekommen
  und die Aufgabe wird nicht mehr im Questlog angezeigt

Mehrere Aufgaben können mithilfe eines Petri-Netzes in Beziehung zueinander
gesetzt werden.

Die möglichen Beziehungen zwischen zwei Aufgaben $t_1$ und $t_2$ sind:
- $t_1$ hat die **erfordleriche Teilaufgabe** $t_2$: für $t_2$ muss eine Antwort abgegeben werden,
  bevor $t_1$ abgeschlossen werden kann; $t_1$ wird zuerst aktiviert und bleibt aktiv,
  während $t_2$ bearbeitet wird
- $t_1$ hat die **optionale Teilaufgabe** $t_2$: Für $t_2$ muss nicht zwingend eine Antwort
  gegeben werden, bevor $t_1$ abgeschlossen werden kann. Eine gegebene Antwort für $t_2$ könnte
  aber bspw. Bonus-Punkte geben
- $t_1$ und $t_2$ bilden eine **Aufgabensequenz**: für $t_1$ muss eine Antwort abgegeben werden,
  bevor $t_2$ aktiv wird; $t_1$ ist vollständig abgeschlossen (und daher inaktiv), während
  $t_2$ aktiv ist; die **gesamte Aufgabensequenz** gilt erst als abgeschlossen, wenn alle
  Blätter (im Abhängigkeitsbaum; TODO) der Sequenz abgeschlossen sind
- $t_1$ hat eine **bedingte Folgeaufgabe** $t_2$: abhängig davon, ob die gegebene Antwort für $t_1$
  korrekt oder falsch (oder zu einem gewissen Prozentsatz korrekt) ist, muss $t_2$ bearbeitet
  werden

### Reporting


## Realisierung

Die Tabellen unter [Zuordnung Aufgabentyp und
Spielmechaniken](#zuordnung-aufgabentyp-und-spielmechaniken) und [Zuordnung Spielmechanik
und Spielszenario](#zuordnung--spielmechanik-und-spielszenario) werden als
Entscheidungsgrundlage verwendet, um die im Projekt zu realisierenden Aufgabentypen,
Mechaniken und Szenarien festzulegen.

### Aufgabentypen

- [Single Choice](#single-choice)
- [Multiple Choice](#multiple-choice)
- [Lückentext](#lückentext)
- [Positionieren](#positionieren)
- [Ersetzen](#ersetzen)
- [Kombinieren](#kombinieren)

**Aufgabentypen, die im Projekt nicht realisiert werden**

- [Objekte in der richtigen Reihenfolge
  auswählen](#objekte-in-der-richtigen-reihenfolge-auswählen), da die anderen Aufgabentypen
  ein breiteres Anwendungsfeld versprechen

### Mechaniken

- [GUI Button](#ein-gui-button-drücken), [GUI Checkbox](#gui-checkboxen-anhaken), [GUI Text
  eingeben](#gui-text-eingeben)
- [Crafting](#crafting)
- [Items in Container ablegen](#items-in-container-ablegen)
- [Items aus Container nehmen](#items--aus-container-nehmen)

**Mechaniken, die im Projekt nicht realisiert werden**

- [Kämpfen](#kämpfen) bleibt als Grundmechanik des Spiels erhalte, wird aber vorerst nicht
  als Aufgabenmechanik umgesetzt
- [Blöcke schieben](#blöcke-schieben), da wahrscheinlich sehr komplex in der Umsetzung
- [Schalter betätigen](#schalter-betätigen), da wahrscheinlich sehr komplex in der Umsetzung

### Szenarien

- [NPC stellt Frage](#npc-stellt-frage)
- [Monster greift mit Frage an](#monster-greift-mit-frage-an)
- [Truhe ist mit Frage verschlossen](#truhe-ist-mit-frage-verschlossen)
- [Mehrere Truhen aber nicht alle sind
  richtig](#mehrere-truhen-aber-nicht-alle-sind-richtig)
- [Crafting: Items in Crafting-Container
  reinwerfen](#items-müssen-in-crafting-container-geworfen-werden)
- [Items müssen in Briefkasten gepackt
  werden](#items-müssen-in-einen-briefkasten-gepackt-werden)

## Offene Punkte

- Zwischen den abstrakten “Antwortmöglichkeiten” einer Aufgabe (per DSL definiert) und den
  konkreten Items, bzw. Interaktionen die diese abstrakten Antworten im Dungeon abbilden
  muss eine Zuordnung bestehen (vgl. z.B. das Szenario [Blöcke müssen in richtige
  Reihenfolge geschoben
  werden](#blöcke-müssen-in-die-richtige-reihenfolge-geschoben-werden); **damit der Spieler
  die Aufgabe korrekt lösen kann, muss diese Aufgabe-zu-Dungeon Zuordnung klar und deutlich
  angezeigt werden**, wie das genau aussieht und was dafür nötig ist, ist noch unklar
- Der Aufgabentyp bestimmt die Art, auf die eine Aufgabe bewertet werden muss, das bestimmt
  auch die Art, in der der Dungeon die Antworten loggt und welche Datenstruktur dafür
  verwendet werden muss
  - **Ob und wie das noch über die Angabe des Aufgabentypen in der DSL hinaus geht und
    welcher Aufgabentyp welche Bewertung genau benötigt, ist noch nicht ausreichend
    definiert**
  - Bei der Konzeptionierung der [Steuermechanismen](#steuermechanismen-gesammelt) sollte
    diese Frage dringend mit bedacht werden

## Initiale Gedankensammlung zu Mechaniken und Steuermechanismen (damit sie nicht verloren geht)

### Spielmechaniken gesammelt

- Räume
- Truhen, “Briefkästen”, bzw. allgemein ein Spielobjekt mit Inventar
- Questgeber-NPC
- “Brief”, bzw. Item, auf dem etwas drauf steht, was der Spieler lesen kann
- verschiebbare Spielelemente (“Klötze”, “Statuen”)
- Monster
- Türen (evtl. mit erkennbarer Kennzeichnung)
- Hinweis / Hilfemeldung
- Schalter / Hebel mit booleschen Zuständen oder größerer Zustandsmenge
- “Bodenaktivator” -> bspw. ein “Pentagram” auf dessen Spitzen Kerzen gelegt werden müssen
- UI-Elemente für “einfache / kleine Aufgaben” (also plain Single Choice, Multiple Choice,
  Freitexteingabe)
- abstrakt: Item-Kombinator (bspw. “Zauberkessel”), der die Möglichkeit bietet, Items zu
  kombinieren
- Dinge aus einem Inventar herausnehmen und in das Spielerinventar ablegen (bspw. aus einer
  Truhe)
- Dinge aus dem Spielerinventar in ein anderes Inventar ablegen (bspw. in eine Truhe)
- Dinge aus dem Spielerinventar auf den Boden fallen lassen
- Zustand bzw. Inhalt eines Inventars auf “Korrektheit” (je nach Aufgabe definiert)
  überprüfen
- Bodenaktivator durch bestimmten Itemtyp aktivieren
- Schalter / Hebel betätigen (wahlweise tastend, schaltend oder bei Zustandsanzahl > 2 den
  Zustand festlegen)
- NPC ansprechen
- UI-Elemente für kleine Aufgaben anzeigen
- Monster angreifen
- verschiebbares Spielelement schieben oder ziehen (auf Gitter oder frei?)
- Ein Item durch ein anderes (oder mehrere) ersetzen
- Mehrere Items zu einem Item zusammenfassen
- Durch eine (ggfs. gekennzeichnete) Tür gehen und somit einen anderen Raum betreten
- Auf eine richtige “Antwort” reagieren und den Spieler belohnen
- Auf eine falsche “Antwort” reagieren und den Spieler bestrafen

### Steuermechanismen gesammelt

- Unterteilung einer Aufgabe in Teilaufgaben
- Festlegung des Aufgabentyps für eine Aufgabe
- Definition, welche Spielmechaniken in der konkreten Realisierung einer Aufgabe im
  Dungeon-Level genutzt werden sollen (oder, falls nicht angegeben, eine aus den für den
  Aufgabentyp möglichen auswählen)
- Definition von richtigen Lösungen für eine (Teil-)Aufgabe
- Definition von richtigen Lösungsmengen für eine (Teil-)Aufgabe, falls mehrere Lösungen
  möglich sind
- Definition von falschen Lösungen für eine (Teil-)Aufgabe
- Definition, wie die DSL / der Dungeon falsche Antworten für eine (Teil-)Aufgabe generieren
  soll (ggfs. mit Schranken / Bedingungen)
  - Bspw. Alle Integer-Werte von 0 bis 100 außer 42
- Petri-Netz für die sequentielle Organisation von Aufgaben
- Petri-Netz für die sequentielle Organisation von Teil-Aufgaben (also innerhalb einer
  Aufgabe)
- Logging von “Antworten” für eine (Teil-)Aufgabe
- Gewichtung von Teilaufgaben (sodass bestimmte Teilaufgaben stärker in die Bewertung einer
  gesamten Aufgabe einfließen)
- beliebig tiefe Verschachtelung von Aufgaben: Zur Lösung einer Aufgabe müssen Teil-Aufgaben
  x, y,z. Teilaufgaben liefern Ergebnisse/Items die zum Lösen der Hauptaufgabe nötig
  sind, gelöst werden
