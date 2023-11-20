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

Ein Entitätstyp beschreibt eine definierte Zusammenstellung aus Komponenten, die eine
bestimmte Rolle in einer Spielmechanik einnehmen.

Ein Beispiel für einen Entitätstyp wäre “Container”. Eine “Container”-Entität könnte
folgende Komponenten enthalten:

- `AnimationComponent` (zum Anzeigen einer Textur und zum Abspielen von Animationen, bspw.
  beim Öffnen des Containers)
- `InventoryComponent` (zum Speichern von Items im Container)
- `InteractionComponent` (um dem Spielcharacter die Möglichkeit der Interaktion mit dem
  Container zu geben)

Ein Entitätstyp definiert nicht die konkreten Werte in den enthaltenen Komponenten. Die
konkreten Werte werden erst durch konkrete Ausprägungen der Entitätstypen definiert. Die
Ausprägung eines Entitätstypen umfasst alle Ausprägungen der im Entitätstypen enthaltenen
Komponenten.

Einige Beispiele für Ausprägungen des “Container”-Entitätstyps sind:

- Briefkasten
  - `AnimationComponent` speichert Briefkastentexturen
  - `InteractionComponent` ruft ein Verhalten auf, welches nur das Transferieren eines Items
    aus dem Spielcharakter-Inventar in das `InventoryComponent` des Briefkastens zulässt
- Truhe
  - `AnimationComponent` speichert Truhentextur
  - `InteractionComponent` überprüft eine externe Bedingung (z.B. ob die korrekte Antwort
    auf eine Frage gegeben wurde) und transferiert bei Erfüllung der Bedingung alle Items
    aus dem `InventoryComponent` in das Inventar des Spielcharakters

**Note:** Das beschriebene Verhalten der Ausprägungen dient nur der Veranschaulichung und
kann von dem tatsächlichen Verhalten der (noch nicht implementierten) Ausprägungen
abweichen.

Entitätstypen schließen sich nicht automatisch gegenseitig aus. Eine Entität kann
gleichzeitig mehrere Entitätstypen haben, bspw. “Questgeber” (zur Aktivierung einer Aufgabe)
und “Container” (um einen Questgegenstand anzunehmen). In solchen Fällen muss die
Interaktion der einzelnen Komponenten durch die Taskbuilder-Methoden passend konfiguriert
werden, um den Anforderungen durch den zu realisierenden Aufgabentyp gerecht zu werden.

### Spielmechanik

Eine Spielmechanik beschreibt **eine Interaktionsart** von verschiedenen Entitätstypen im
Spiel. Spielmechaniken beziehen sich ausschließlich auf die Interaktion von Spielelementen,
die aus Game-Design Perspektive relevant sind (siehe [Steuermechanismen](#steuermechanismen)
für alle anderen “Mechaniken”).

Beispiel: Ein Item, welches im Dungeon liegt, kann aufgehoben und in das Inventar des
Spielcharakters transferiert werden.

Aus einer Spielmechanik kann abgeleitet werden, welche Komponenten an der Mechanik beteiligt
sind. Für obiges Beispiel sind folgende Komponenten erforderlich:

- `InventoryComponent` im Spielcharakter (zur Realisierung des Inventars)
- `CollisionComponent` im Spielcharakter und im Item, um ein Event auszulösen, sobald der
  Spielcharakter über das Item läuft
- Alternativ ein `InteractionComponent` im Item, falls das Item per dedizierter
  Eingabeaktion (bspw. Tastendruck) aufgehoben werden soll

### Spielszenario

Ein Spielszenario beschreibt, welche Spielmechaniken, welche Entitätstypen und auf welche
Art diese in einem konkreten Dungeonlevel in Verbindung gebracht werden, um eine abstrakte
Aufgabe zu realisieren. Ein Spielszenario beschreibt nicht, welche visuellen Ausprägungen
der Entitätstypen verwendet werden, bspw. ob der Questgeber einer Aufgabe wie ein “Zauberer”
oder wie eine “Kriegerin” dargestellt wird.

Beispiel: In einem Raum befindet sich ein Questgeber und verschiedene Schalter. Der
Questgeber stellt dem Spielcharakter eine Frage und aktiviert die Aufgabe. Jeder Schalter
kann inspiziert werden, um herauszufinden, für welche Antwort der jeweilige Schalter steht.
Per Interaktion kann der Spielcharakter den Zustand eines Schalters manipulieren. Per
Interaktion mit dem Questgeber können die konfigurierten Zustände der Schalter als Lösung
abgegeben und die Aufgabe beendet werden.

**Beteiligte Entitätstypen:**

- Questgeber
- Schalter

**Beteiligte Spielmechaniken:**

- Text über GUI-Dialog anzeigen
- Seiten im GUI-Dialog wechseln
- Aufgabe per Questgeber aktivieren
- Aufgabe per Questgeber abschließen
- Eine Entität im Dungeon “inspizieren”
- Einen Schalter (im Dungeon) betätigen

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

### Freitext

Die Aufgabenstellung besteht aus einem Fragetext und einem Textfeld, in welches die
Studierenden einen Antworttext frei eingeben können. Es gibt dabei keine Vorgabe, welche
Form die freie Texteingabe haben muss.

### Lücken füllen

Die Aufgabenstellung besteht aus einem Fragetext und mehreren Lücken, die gefüllt werden
müssen. In jede Lücke muss eine Antwort eingesetzt werden, welche von den Studierenden
selbst in Textform eingegeben werden muss, es gibt keine Antwortmöglichkeiten zur Auswahl.
Die Lücken werden durch eine “lückenhafte Struktur” im Dungeon repräsentiert, bspw. mehrere
Container, in die ein Item eingesetzt werden muss.

Note: Dieser Aufgabentyp wurde als Generalisierung von “Lückentext” eingeführt, sodass
dieser Aufgabentyp nicht an die Realisierung in einem GUI-Dialog gebunden ist, sondern frei
gamifiziert werden kann.

### Ersetzen

Aus einer Gesamtmenge aus $l$ Elementen (mit $l > 0$) muss eine Menge mit $m$ Elementen (mit
$m > 0$) durch eine zweite Menge mit $n$ Elementen (mit $n > 0$) ersetzt werden. Hierdurch
wird die Gesamtmenge der Elemente sowie die Anzahl der enthaltenen Elemente verändert.

### Zuordnen

Zuordnen bezeichnet im Allgemeinen die Aufgabe, Elementen aus einer Menge $A$ (z.B.
Antwortmöglichkeiten) die Elemente aus einer anderen Menge $B$ zuzuordnen (bspw.
Array-Index, Reihe-Spalte Kombination einer Matrix). Die Zuordnung muss dabei nach der Form
einer partiellen Funktion $f: A \rightharpoonup B$ erfolgen.

Das heißt:

- Jeder Antwortmöglichkeit $a \in A$ kann maximal ein Element $b \in B$ zugeordnet werden.
- Jedem Element $b \in B$ können $n$ Elemente $a \in A$ zugeordnet werden, wobei
  $0 \leq n \leq |A|$ ist

Die gesamte Mengen $A$ und $B$ müssen den Studierenden bekannt sein, bevor sie den ersten
Zuordnungsschritt machen können. Es kann Aufgaben geben, wo Studierende selbst Teile der
Menge $A$ definieren können.

## Konkrete Entitätstypen

- Questgeber: Entität, über die eine Aufgabe aktiviert wird
- Bild: Interaktive Entität im Dungeon, die bei Interaktion ein Bild anzeigt (bspw. ein
  UML-Diagram, Graph, etc.)
- Container: Entität, deren Inventar vom Spielcharakter manipuliert werden kann
- Monster: Entität, welche den Spielcharakter (nach einem bestimmten Verhalten) verfolgt,
  angreift und besiegt werden kann
- Interaktives Bedienelement: Eine interaktive Entität, deren Zustand vom Spielcharakter
  manipuliert werden kann. Es sind vier unterschiedliche Arten vorgesehen:
  - Tastend: Das Bedienelement kann impulsartig betätigt werden, d.h. bei Interaktion wird
    einmalig eine Aktion ausgeführt
  - Schaltend: Das Bedienelement kann zwischen zwei Zuständen getoggelt werden (an/aus)
  - Zustandsmenge > 2: Das Bedienelement kann zwischen mehr als 2 fest definierten
    Zuständen umgeschaltet werden
  - Kontinuierlich: Das Bedienelement verhält sich wie ein Schieberegler, bei dem ein Wert
    zwischen einer oberen und einer unteren Grenze eingestellt werden kann
- Item: Eine Entität, die entweder eine Position im Dungeon haben kann (also bspw. auf dem
  Boden liegt), oder in einem Inventar gespeichert sein kann und zwischen diesen beiden
  Zuständen wechseln kann
- Schriftrolle: Ein Item, welches einen Text anzeigt, zwei Alternativen:
  - Selbst beschriftbar: Spielende können per UI-Element die Schriftrolle selbst
    beschriften, also den Text definieren
  - Fix beschriftet: Der Text auf der Schriftrolle wird durch die Aufgabendefinition
    vorgegeben und kann nicht geändert werden
- Craftingcontainer: Ein Container, der zusätzlich die Möglichkeit bietet, die Menge der
  enthaltenen Items unter Verwendung von Craftingrezepten zu transmutieren

## Spielmechaniken

### Text über GUI-Dialog anzeigen

Das Spiel kann Text über ein GUI-Dialog anzeigen, um bspw. Hinweise an die Studierenden zu
geben, oder Aufgabentexte zu stellen.

**Begriffsklärung: GUI-Dialog**

Im Kontext dieses Dokuments wird mit dem Begriff “GUI-Dialog” ein UI-Element innerhalb des
Spielfensters beschrieben, kein separates Fenster auf Betriebssystem-Ebene.

### Einen GUI-Button aktivieren

In einem GUI-Dialog können beschriftete Buttons angezeigt werden, die von Studierenden
aktiviert werden können, um bspw. eine Antwort auf eine Frage auszuwählen. Für diese Aktion
kann auch eine Tastatureingabe verwendet werden.

### Seiten im GUI-Dialog wechseln

Per GUI-Button (z.B. “Weiter”, “Zurück”) kann zwischen mehreren Seiten eines GUI-Dialogs
gewechselt werden. Für diese Aktionen können auch Tastatureingaben verwendet werden.

### GUI Checkboxen anhaken

In einem GUI-Dialog können beschriftete Checkboxen angezeigt werden. Die Checkboxen können
durch Anklicken aktiviert (falls inaktiv) und deaktiviert (falls bereits aktiv) werden.

### GUI Text eingeben

In einem GUI-Dialog kann ein Textfeld dargestellt werden, in das Studierende Text eingeben
können.

### Aufgabe per Questgeber aktivieren

Studierende können mit einem Questgeber interagieren, wodurch sie eine Aufgabe aktivieren.

### Aufgabe per Questgeber abschließen

Falls eine Aufgabe aktiv ist, können Studierende per Interaktion mit dem entsprechenden
Questgeber die Aufgabe abschließen. Diese Mechanik kann genutzt werden, falls zur Lösung
einer Aufgabe die Zustände mehrerer Entitäten manipuliert werden müssen und eine finale
dedizierte Aktion nötig ist, um die aktuelle Zustandskonfiguration als Lösung abzugeben.

### Ein Container-Inventar öffnen

Per Interaktion kann das Inventar eines Containers geöffnet werden, sodass der Inhalt des
Inventars einsehbar ist. Hierfür wird ein GUI-Dialog verwendet, in dem der Inhalt des
Inventars grafisch dargestellt wird. Per GUI-Button oder Tastatureingabe kann das Inventar
wieder geschlossen werden.

### Das Spielcharakter-Inventar öffnen

Per Tastendruck oder GUI-Button im HUD kann das Inventar des Spielcharakters geöffnet und
geschlossen werden. Hierfür wird ein GUI-Dialog verwendet, in dem der Inhalt des Inventars
grafisch dargestellt wird. Per GUI-Button oder Tastatureingabe kann das Inventar wieder
geschlossen werden.

### Item aus Container-Inventar in Charakter-Inventar transferieren

Bei geöffnetem Container-Inventar kann ein Item aus dem Container-Inventar per Klickaktion
oder Tastatureingabe ausgewählt werden und in das Inventar des Spielcharakters transferiert
werden. Per GUI-Button oder Tastatureingabe können auch alle Items aus dem
Container-Inventar in das Inventar des Spielcharakters übertragen werden.

### Item aus Charakter-Inventar in Container-Inventar transferieren

Bei geöffnetem Spielcharakter-Inventar kann ein Item aus dem Spielcharakter-Inventar per
Klickaktion oder Tastatureingabe ausgewählt werden und in das Inventar eines Containers
transferiert werden.

### Ein Item aufheben

Ein Item, welches auf dem Boden des Dungeons liegt, kann vom Spielcharakter aufgesammelt
werden, indem der Spielcharakter mit der Hitbox des Items kollidiert und eine
Tastatureingabe erfolgt.

### Informationen über ein Item ansehen

Bei geöffnetem Inventar (unabhängig zu welcher Entität das Inventar gehört), können
Informationen über ein Item innerhalb des Inventars angezeigt werden.

### Ein Item auf den Boden fallen lassen

Bei geöffnetem Inventar (unabhängig zu welcher Entität das Inventar gehört), kann ein
ausgewähltes Item aus dem Inventar entfernt und auf den Boden gefallen lassen werden.

### Eine Waffe aus dem Inventar ausrüsten

Bei geöffnetem Spielcharakter-Inventar kann eine Waffe als aktive Waffe des Spielcharakters
ausgerüstet werden. Eine ausgerüstete Waffe beeinflusst den Schaden und die Art eines
ausgeführten Angriffs.

### Ein Item aus dem Inventar nutzen

Bei geöffnetem Spielcharakter-Inventar kann ein Item genutzt werden (sofern es ein nutzbares
Items ist), sodass ein “Nutzungseffekt” (bspw. Heilung der Gesundheit des Spielcharakters)
eintritt, welcher sich für die Items unterscheiden kann.

### Eine Schriftrolle beschriften

Bei geöffnetem Spielcharakter-Inventar kann eine beschriftbare Schriftrolle (siehe
[Entitätstypen](#konkrete-entitätstypen)) beschriftet werden. Hierfür wird ein UI-Dialog
verwendet, in den der neue Text für die Schriftrolle eingegeben werden kann.

### Den Text einer Schriftrolle lesen

Bei geöffnetem Inventar kann eine Schriftrolle aus dem Inventar ausgewählt und gelesen
werden, wofür ein separater GUI-Dialog geöffnet wird, der den Text der Schriftrolle enthält.

### Eine Entität im Dungeon “inspizieren”

Entitäten im Dungeon können vom Spielcharakter inspiziert werden, wodurch sich ein
GUI-Dialog öffnet, der Informationen über die inspizierte Entität anzeigt.

### Ein Monster angreifen

Der Spielcharakter kann mit einer ausgerüsteten Waffe ein Monster angreifen. Zum Ausführen
eines Angriffs muss eine Tastatureingabe erfolgen. Ein Angriff hat eine Hitbox, kollidiert
diese Hitbox mit einem Monster, wird er Gesundheit des Monsters Schaden zugefügt.

### Schaden von einem Monster zugefügt bekommen

Monster greifen den Spielcharakter nach einem konfigurierbaren Verhalten an. Auch Angriffe
von Monstern haben Hitboxen. Kollidieren diese Hitboxen mit dem Spielcharakter, wird der
Gesundheit des Spielcharakters Schaden zugefügt.

### Monster besiegen

Wenn die Gesundheit eines Monsters auf 0 fällt, wird es besiegt und stirbt, wodurch es ggfs.
Items aus seinem Inventar (falls vorhanden) fallen lässt und der Spielcharakter
Erfahrungspunkte erhält.

### Eine Entität verschieben

Per Interaktion kann der Spielcharakter eine Entität im Dungeon in eine bestimmte Richtung
verschieben.

### Einen Schalter (im Dungeon) betätigen

Per Interaktion kann der Spielcharakter den Zustand eines Schalters im Dungeon ändern.
Welche Zustände der Schalter annehmen kann, hängt von der Art des Schalters ab.

### Einen Crafting-Schritt durchführen

Bei geöffnetem Inventar eines Craftingcontainers können Items aus dem Container-Inventar
ausgewählt werden, auf die ein auswählbares Craftingrezept (also eine Transmutationsregel)
angewandt wird, um die Menge der ausgewählten Items zu verändern. Nach der Auswahl der Items
und des Craftingrezepts muss der Crafting-Schritt per GUI-Button oder Tastatureingabe
bestätigt werden.

## Spielszenarien

**Begriffsdefinition - Belohnung**

Eine Belohnung für eine korrekt bearbeitete Aufgabe kann unterschiedlich aussehen, z.B.:

- eine Textnachricht über UI: “Ja, gut gemacht!”
- ein Item, was vom Questgeber fallen gelassen wird

**Begriffsdefinition - Bestrafung**

Eine Bestrafung für eine falsch bearbeitete Aufgabe kann unterschiedlich aussehen, z.B.:

- eine Textnachricht über UI: “Nein, das war leider falsch!”
- Monster spawnen, die bekämpft werden müssen

### Questgeber stellt Frage

Im Level steht ein Questgeber, mit dem der Spielcharakter reden kann. Im Gespräch stellt der
Questgeber dem Spielcharakter eine Frage, die er beantworten muss. Beantwortet der
Spielcharakter die Frage richtig, bekommt er eine Belohnung. Beantwortet der Spielcharakter
die Frage falsch, bekommt er keine Belohnung.

### Monster greift mit Frage an

Im Level läuft ein Monster herum. Wenn das Monster den Spielcharakter sieht, läuft es auf
den Spielcharakter zu (bis zur Kollision). Bei Kollision stellt das Monster eine Frage. Wird
die Frage richtig beantwortet, stirbt das Monster. Wird die Frage falsch beantwortet, wird
die Gesundheit des Spielcharakters stark beschädigt und das Monster rennt weg.

### Container ist mit Frage verschlossen

Im Level steht ein Container. Versucht der Spielcharakter den Container zu öffnen, wird eine
Frage per GUI-Dialog gestellt. Wird diese Frage korrekt beantwortet, kann der Container
geöffnet werden. Wird die Frage falsch beantwortet, bleibt der Container geschlossen.

### Richtige Container auswählen

Der Spielcharakter kommt in einen Raum. Per Questgeber kann eine Frage aktiviert werden. Die
Antwortmöglichkeiten sind in Form von Containern im Raum verteilt. Der Spielcharakter kann
die Container inspizieren, um herauszufinden, welcher Container welche Antwortmöglichkeit
repräsentiert. Der Spielcharakter muss die Container, die den richtigen Antworten
entsprechen, öffnen und ein Item aus den Container-Inventaren entfernen. Abschließend kann
per Interaktion mit dem Questgeber die Lösung “abgegeben” werden.

Wird die Frage richtig beantwortet, wird eine Belohnung an den Spielcharakter ausgegeben,
falls nicht, wird keine Belohnung an den Spielcharakter ausgegeben.

### Entitäten müssen in die richtige Reihenfolge geschoben werden

- Eine Menge verschiebbarer Entitäten stehen in einem Raum
- Ein Questgeber stellt den Aufgabentext: “Bringe die Entitäten in die passende Reihenfolge
  nach folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Der Spielcharakter interagiert mit den verschiebbaren Entitäten und bringt sie in die
  geforderte Reihenfolge
  - Hierbei müssen die Entitäten auf speziellen Feldern auf dem Boden platziert werden
- Wenn der Spielcharakter erneut mit dem Questgeber interagiert, öffnet sich ein UI-Element,
  in dem entweder die Aufgabenstellung erneut angezeigt werden kann, oder die Konfiguration
  der Blöcke auf den speziellen Feldern als Antwort geloggt wird
- Wenn die Konfiguration der Entitäten auf den speziellen Feldern korrekt ist, wird eine
  Belohnung erteilt
- Wenn die Konfiguration der Entitäten falsch ist, wird eine Bestrafung erteilt

### Schalter müssen in der richtigen Reihenfolge betätigt werden

- In einem Raum sind mehrere tastende Schalter platziert
- Questgeber stellt den Aufgabentext: “Betätige die Schalter in der richtigen Reihenfolge
  nach folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Der Spielcharakter interagiert mit den verschiedenen Tastern
- Reihenfolge der Betätigungen wird geloggt
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die Reihenfolge als Antwort abgegeben werden kann
- Wenn die Reihenfolge der Betätigungen korrekt ist, wird eine Belohnung erteilt
- Wenn die Reihenfolge der Betätigungen falsch ist, wird eine Bestrafung erteilt

### Sammlung an Schalter müssen in die richtigen Zustände gebracht werden

- In einem Raum sind mehrere Schalter mit Zustandsanzahl > 2 platziert
- Questgeber stellt den Aufgabentext: “Bringe die Schalter in die richtigen Zustände nach
  folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Der Spielcharakter interagiert mit den verschiedenen Schaltern
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die Zustände aller Schalter als Antwort abgegeben werden können
- Wenn die Zustände der Schalter korrekt ist, wird eine Belohnung erteilt
- Wenn die Zustände der Schalter falsch ist, wird eine Bestrafung erteilt

### Monster müssen in der richtigen Reihenfolge gehauen werden

- In einem Raum laufen mehrere unterscheidbare Monster umher
- Questgeber stellt den Aufgabentext: “Besiege die Monster in der richtigen Reihenfolge nach
  folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Der Spielcharakter kämpft gegen die Monster
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die Reihenfolge der besiegten Monster als Antwort geloggt werden kann
- Wenn die Reihenfolge der besiegten Monster korrekt ist, wird eine Belohnung erteilt
- Wenn die Reihenfolge der besiegten Monster falsch ist, wird eine Bestrafung erteilt

### Monsterarten in richtiges Verhältnis bringen

In einem Raum bewegen sich visuell verschiedene Monster. Per Questgeber wird eine Aufgabe
aktiviert. Die Monster müssen so bekämpft werden, dass das richtige Verhältnis zwischen den
visuell unterschiedlichen Monsterarten erreicht ist.

Wird die Aufgabe richtig gelöst, wird eine Belohnung erteilt. Wird die Aufgabe falsch
gelöst, wird eine Bestrafung erteilt.

### Items müssen in Container abgelegt werden

- In einem Raum stehen mehrere unterscheidbare Container
- Im Dungeon (nicht nur im Container-Raum) sind Items versteckt
- Questgeber stellt den Aufgabentext: “Finde und platziere Items in den richtigen Containern
  nach folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Spielende suchen im Dungeon nach Items und legen sie in Containern ab
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgelegten Items in den Containern als Antwort geloggt werden können
- Wenn die richtigen Items in den richtigen Containern platziert wurden, wird eine Belohnung
  erteilt
- Wenn die falschen Items in den falschen Containern platziert wurden, wird eine Bestrafung
  erteilt

### Items müssen aus einem Container entfernt werden

- In einem Raum steht ein Container, die eine Menge von Items speichert
- Questgeber stellt den Aufgabentext: “Entferne Items in den aus dem Container nach
  folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Spielende entfernen Items aus dem Container
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die entfernten Items als Antwort geloggt werden können
- Wenn die richtigen Items entfernt wurden, wird eine Belohnung erteilt
- Wenn die falschen Items entfernt wurden, wird eine Bestrafung erteilt

### Schriftrollen in Container ablegen

- In einem Raum steht ein Container und ein Questgeber
- Im Dungeon (nicht nur im Container-Raum) sind Schriftrollen versteckt
- Questgeber stellt den Aufgabentext: “Finde und werfe Schriftrollen in den Container nach
  folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Spielende suchen im Dungeon nach Schriftrollen und legt sie im Container ab
  - Nachdem die Schriftrollen abgelegt sind, können sie nicht mehr herausgenommen werden
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgegebenen Items als Antwort geloggt werden können
- Wenn die richtigen Schriftrollen abgegeben wurden, wird eine Belohnung erteilt
- Wenn die falschen Schriftrollen abgegeben wurden, wird eine Bestrafung erteilt

### Selbst beschriftete Schriftrollen in Container ablegen

- In einem Raum steht ein Container und ein Questgeber
- Im Dungeon sind beschriftbare Schriftrollen verteilt
- Questgeber stellt den Aufgabentext: “Finde, beschrifte und werfe Schriftrollen in den
  Container nach folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Spielende suchen im Dungeon nach Schriftrollen, beschriftet sie und legt sie im Container
  ab
  - Nachdem die Schriftrollen abgelegt sind, können sie nicht mehr herausgenommen werden
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgegebenen Items als Antwort geloggt werden können
- Wenn die Antworten auf den Schriftrollen korrekt sind, wird eine Belohnung erteilt
- Wenn die Antworten auf den Schriftrollen korrekt sind, wird eine Bestrafung erteilt

### Items müssen in Crafting-Container geworfen werden

- In einem Raum steht ein Crafting-Container und ein Questgeber
- Im Dungeon (nicht nur im Crafting-Container Raum) sind Items versteckt
- Questgeber stellt den Aufgabentext: “Finde und werfe Items in den Zauberkessel nach
  folgender Vorgabe: *(abhängig von der konkreten Aufgabenstellung)*”
- Spielende suchen im Dungeon nach Items und legen sie im Crafting-Container ab
- Bei erneutem Interagieren mit Questgeber, öffnet sich ein UI-Element, in dem entweder die
  Aufgabenstellung erneut angezeigt werden kann, das Rätsel zurückgesetzt werden kann, oder
  die abgegebenen Items als Antwort geloggt werden können
- Wenn die richtigen Items abgegeben wurden, wird eine Belohnung erteilt und die abgegebenen
  Items werden zu einem neuen Item kombiniert
- Je nach Aufgabenstellung könnte das neu erzeugte Item wieder eine “Zutat” für eine weitere
  Kombination sein

## Zuordnung Aufgabentyp und Spielszenario

|                     | **Questgeber stellt Frage** | **Monster greift mit Frage an** | **Container ist mit Frage verschlossen** | **Richtige Container auswählen** | **Entitäten müssen in die richtige Reihenfolge geschoben werden** | **Schalter müssen in der richtigen Reihenfolge betätigt werden** | **Sammlung an Schaltern müssen in die richtigen Zustände gebracht werden** | **Monster müssen in der richtigen Reihenfolge gehauen werden** | **Monsterarten ins Verhältnis bringen** | **Items müssen in Containern abgelegt werden** | **Items müssen aus einem Container entfernt werden** | **Schriftrollen in Container ablegen** | **Selbst beschriftete Schriftrollen in Container ablegen** | **Items müssen in Crafting-Container geworfen werden** |
|---------------------|-----------------------------|---------------------------------|------------------------------------------|----------------------------------|-------------------------------------------------------------------|------------------------------------------------------------------|----------------------------------------------------------------------------|----------------------------------------------------------------|-----------------------------------------|------------------------------------------------|------------------------------------------------------|----------------------------------------|------------------------------------------------------------|--------------------------------------------------------|
| **Single Choice**   | X                           | X                               | X                                        | X                                |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              | X                                                    | X                                      |                                                            |                                                        |
| **Multiple Choice** | X                           | X                               | X                                        | X                                |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              | X                                                    | X                                      |                                                            |                                                        |
| **Freitext**        | X                           | X                               | X                                        |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        | X                                                          |                                                        |
| **Lücken füllen**   |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        | X                                                          |                                                        |
| **Ersetzen**        |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            | X                                                      |
| **Zuordnen**        |                             |                                 |                                          | X                                | X                                                                 | X                                                                | X                                                                          | X                                                              | X                                       | X                                              | X                                                    | X                                      |                                                            |                                                        |

## Zuordnung Spielmechanik und Spielszenario

|                                                                     | **Questgeber stellt Frage** | **Monster greift mit Frage an** | **Container ist mit Frage verschlossen** | **Richtige Container auswählen** | **Entitäten müssen in die richtige Reihenfolge geschoben werden** | **Schalter müssen in der richtigen Reihenfolge betätigt werden** | **Sammlung an Schaltern müssen in die richtigen Zustände gebracht werden** | **Monster müssen in der richtigen Reihenfolge gehauen werden** | **Monsterarten ins Verhältnis bringen** | **Items müssen in Containern abgelegt werden** | **Items müssen aus einem Container entfernt werden** | **Schriftrollen in Container ablegen** | **Selbst beschriftete Schriftrollen in Container ablegen** | **Items müssen in Crafting-Container geworfen werden** |
|---------------------------------------------------------------------|-----------------------------|---------------------------------|------------------------------------------|----------------------------------|-------------------------------------------------------------------|------------------------------------------------------------------|----------------------------------------------------------------------------|----------------------------------------------------------------|-----------------------------------------|------------------------------------------------|------------------------------------------------------|----------------------------------------|------------------------------------------------------------|--------------------------------------------------------|
| **Text über GUI-Dialog anzeigen**                                   | X                           | X                               | X                                        | X                                | X                                                                 | X                                                                | X                                                                          | X                                                              | X                                       | X                                              | X                                                    | X                                      | X                                                          | X                                                      |
| **Einen GUI-Button aktivieren**                                     | X                           | X                               | X                                        | X                                |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **Seiten im GUI-Dialog wechseln**                                   | X                           | X                               | X                                        | X                                | X                                                                 | X                                                                | X                                                                          |                                                                |                                         |                                                |                                                      |                                        | X                                                          |                                                        |
| **GUI Checkboxen anhaken**                                          | X                           | X                               | X                                        |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **GUI Text eingeben**                                               | X                           | X                               | X                                        |                                  |                                                                   |                                                                  |                                                                            |                                                                | X                                       |                                                |                                                      |                                        | X                                                          |                                                        |
| **Aufgabe per Questgeber aktivieren**                               | X                           | X                               | X                                        | X                                | X                                                                 | X                                                                | X                                                                          | X                                                              | X                                       | X                                              | X                                                    | X                                      | X                                                          | X                                                      |
| **Aufgabe per Questgeber abschließen**                              | X                           | X                               | X                                        | X                                | X                                                                 | X                                                                | X                                                                          | X                                                              | X                                       | X                                              | X                                                    | X                                      | X                                                          | X                                                      |
| **Ein Container-Inventar öffnen**                                   |                             |                                 | X                                        | X                                |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              | X                                                    | X                                      | X                                                          | X                                                      |
| **Das Spielcharakter-Inventar öffnen**                              |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              | X                                                    | X                                      | X                                                          | X                                                      |
| **Item aus Container-Inventar in Charakter-Inventar transferieren** |                             |                                 | X                                        | X                                |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                | X                                                    |                                        | X                                                          | X                                                      |
| **Item aus Charakter-Inventar in Container-Inventar transferieren** |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              |                                                      | X                                      | X                                                          | X                                                      |
| **Ein Item aufheben**                                               |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              |                                                      | X                                      | X                                                          | X                                                      |
| **Informationen über ein Item ansehen**                             |                             |                                 |                                          | X                                |                                                                   |                                                                  |                                                                            |                                                                |                                         | X                                              | X                                                    | X                                      | X                                                          | X                                                      |
| **Ein Item auf den Boden fallen lassen**                            |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **Eine Waffe aus dem Inventar ausrüsten**                           |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            | X                                                              | X                                       |                                                |                                                      |                                        |                                                            |                                                        |
| **Ein Item aus dem Inventar nutzen**                                |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **Eine Schriftrolle beschriften**                                   |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        | X                                                          |                                                        |
| **Den Text einer Schriftrolle lesen**                               |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      | X                                      | X                                                          |                                                        |
| **Eine Entität im Dungeon “inspizieren”**                           |                             |                                 |                                          |                                  | X                                                                 | X                                                                | X                                                                          |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **Ein Monster angreifen**                                           |                             | X                               |                                          |                                  |                                                                   |                                                                  |                                                                            | X                                                              | X                                       |                                                |                                                      |                                        |                                                            |                                                        |
| **Schaden von einem Monster zugefügt bekommen**                     |                             | X                               |                                          |                                  |                                                                   |                                                                  |                                                                            | X                                                              | X                                       |                                                |                                                      |                                        |                                                            |                                                        |
| **Monster besiegen**                                                |                             | X                               |                                          |                                  |                                                                   |                                                                  |                                                                            | X                                                              | X                                       |                                                |                                                      |                                        |                                                            |                                                        |
| **Eine Entität verschieben**                                        |                             |                                 |                                          |                                  | X                                                                 |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **Einen Schalter (im Dungeon) betätigen**                           |                             |                                 |                                          |                                  |                                                                   | X                                                                | X                                                                          |                                                                |                                         |                                                |                                                      |                                        |                                                            |                                                        |
| **Einen Crafting-Schritt durchführen**                              |                             |                                 |                                          |                                  |                                                                   |                                                                  |                                                                            |                                                                |                                         |                                                |                                                      |                                        |                                                            | X                                                      |

**Note**: Mechaniken, für die aktuell kein Szenario markiert ist, sind prinzipiell im
Spielkontext sinnvoll und können für die Realisierung einer Aufgabe genutzt werden, es ist
nur aktuell noch kein ausformuliertes Szenario für diese Mechaniken vorhanden.

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
das Array nach dem dritten Schritt an. Aufgabentyp: Sortieren Spielmechanik: Entitäten
schieben Spielszenario: Entitäten müssen in die richtige Reihenfolge geschoben werden

### CSP mit Forward Checking lösen (KI)

Aufgabe: “(Bild von Karte mit verschieden Ländern und Färbungen) Geben sie für jede Variable
jeden gültigen Zustand an, wenn Sie die Backtracking Search verwenden.” Aufgabentyp:
Zuordnen Spielmechanik: Item(s) aus Container nehmen Spielszenario: Items müssen aus einer
Trhue entfernt werden Spielmechanik: Gui Text eingeben Spielszenario: Monster greift mit
Lückentext-Frage an

## Steuermechanismen

### Level-Auswahl

Aus einer Reihe von DSL-Dateien in einem definierten Verzeichnis werden die `level_config`
Definitionen ausgelesen, sodass zur Laufzeit des Dungeons eine Aufgabe zur Bearbeitung
ausgewählt werden kann. Auf die Level-Auswahl wird in
[Level-Auswahl](../control_mechanisms/level_loading.md) (TODO) vertiefend eingegangen.

### Petri-Netz zur Aufgabenverschachtelung

Mehrere Aufgaben können mithilfe eines Petri-Netzes in Beziehung zueinander gesetzt werden.
Eine Aufgabe kann folgende Zustände haben:

- **inaktiv**: die Aufgabe wird den Studierenden nicht angezeigt und die mit der Aufgabe
  verknüpften Entitäten zeigen in einer definierten Form an, dass die entsprechende Aufgabe
  noch nicht aktiviert wurde oder sind nicht interagierbar
- **aktiv ohne Bearbeitung**: die Aufgabe wird den Studierenden im Questlog angezeigt, die
  verknüpften Entitäten verhalten sich wie bei **inaktiv**, alle Teilaufgaben der Aufgabe
  werden ebenfalls aktiviert
- **aktiv mit Bearbeitung**: die Aufgabe wird den Studierenden im Questlog angezeigt, mit
  den verknüpften Entitäten kann interagiert werden, um eine Antwort auf die Aufgabe zu
  geben
- **fertig bearbeitet**: Die Studierenden haben eine Antwort für eine Aufgabe abgegeben, hat
  Feedback darüber bekommen und die Aufgabe wird nicht mehr im Questlog angezeigt

Die möglichen Beziehungen zwischen zwei Aufgaben $t_1$ und $t_2$ sind:

- $t_1$ hat die **erforderliche Teilaufgabe** $t_2$: für $t_2$ muss eine Antwort abgegeben
  werden, bevor $t_1$ abgeschlossen werden kann; $t_1$ wird zuerst aktiviert und bleibt
  aktiv, während $t_2$ bearbeitet wird
- $t_1$ hat die **optionale Teilaufgabe** $t_2$: Für $t_2$ muss nicht zwingend eine Antwort
  gegeben werden, bevor $t_1$ abgeschlossen werden kann. Eine gegebene Antwort für $t_2$
  könnte aber bspw. Bonus-Punkte geben
- $t_1$ und $t_2$ bilden eine **Aufgabensequenz**: für $t_1$ muss eine Antwort abgegeben
  werden, bevor $t_2$ aktiv wird; $t_1$ ist vollständig abgeschlossen (und daher inaktiv),
  während $t_2$ aktiv ist; die **gesamte Aufgabensequenz** gilt erst als abgeschlossen, wenn
  die letzte Aufgabe der Sequenz abgeschlossen ist
- $t_1$ hat eine **bedingte Folgeaufgabe** $t_2$: abhängig davon, ob die gegebene Antwort
  für $t_1$ korrekt oder falsch (oder zu einem gewissen Prozentsatz korrekt) ist, muss $t_2$
  bearbeitet werden

### Reporting

Reporting fasst den Prozess der Erfassung, Bewertung und Speicherung einer Antwort auf eine
Aufgabe zusammen. Welche Daten genau als Teil einer Antwort erfasst werden, unterscheidet
sich basierend auf dem Aufgabentyp. Auf das Reporting wird in
[Reporting](control_mechanisms/reporting.md) vertiefend eingegangen.

### Zurücksetzen einer Aufgabe

Den Studierenden sollte die Möglichkeit gegeben sein, eine Aufgabe auf den Anfangszustand
zurückzusetzen, um mit der Bearbeitung erneut zu beginnen (um Fehlschritte nicht alle
manuell rückgängig machen zu müssen). Hierfür werden alle mit der Aufgabe verknüpften
Entitäten wieder an ihre Anfangsposition im Dungeon zurückgesetzt, der Inhalt von Containern
wird auf den Anfangszustand zurückgesetzt, etc.

## Realisierung

Die Tabellen unter [Zuordnung Aufgabentyp und
Spielszenario](#zuordnung-aufgabentyp-und-spielszenario) und [Zuordnung Spielmechanik und
Spielszenario](#zuordnung-spielmechanik-und-spielszenario) werden als Entscheidungsgrundlage
verwendet, um die im Projekt zu realisierenden Aufgabentypen, Mechaniken und Szenarien
festzulegen.

### Aufgabentypen

- [Single Choice](#single-choice)
- [Multiple Choice](#multiple-choice)
- [Lücken füllen](#lücken-füllen)
- [Zuordnen](#zuordnen)
- [Ersetzen](#ersetzen)

**Aufgabentypen, die im Projekt nicht realisiert werden**

- [Freitext](#freitext), die Texteingabe kann beliebig komplex ausfallen, was eine
  Interpretation der Eingabe voraussetzt, welche über die einfache Auswertung per regulärem
  Ausdruck etc. hinausgeht. Eine derartige Interpretation und Überprüfung zu automatisieren
  liegt außerhalb des Projektumfangs

### Mechaniken

- [Text über GUI-Dialog anzeigen](#text-über-gui-dialog-anzeigen)
- [Einen GUI-Button aktivieren](#einen-gui-button-aktivieren)
- [Seiten im GUI-Dialog wechseln](#seiten-im-gui-dialog-wechseln)
- [GUI Checkboxen anhaken](#gui-checkboxen-anhaken)
- [GUI Text eingeben](#gui-text-eingeben)
- [Aufgabe per Questgeber aktivieren](#aufgabe-per-questgeber-aktivieren)
- [Aufgabe per Questgeber abschließen](#aufgabe-per-questgeber-abschließen)
- [Ein Container-Inventar öffnen](#ein-container-inventar-öffnen)
- [Das Spielcharakter-Inventar öffnen](#das-spielcharakter-inventar-öffnen)
- [Item aus Container-Inventar in Charakter-Inventar
  transferieren](#item-aus-container-inventar-in-charakter-inventar-transferieren)
- [Item aus Charakter-Inventar in Container-Inventar
  transferieren](#item-aus-charakter-inventar-in-container-inventar-transferieren)
- [Ein Item aufheben](#ein-item-aufheben)
- [Informationen über ein Item ansehen](#informationen-über-ein-item-ansehen)
- [Ein Item auf den Boden fallen lassen](#ein-item-auf-den-boden-fallen-lassen)
- [Ein Item aus dem Inventar nutzen](#ein-item-aus-dem-inventar-nutzen)
- [Eine Schriftrolle beschriften](#eine-schriftrolle-beschriften)
- [Den Text einer Schriftrolle lesen](#den-text-einer-schriftrolle-lesen)
- [Eine Entität im Dungeon “inspizieren”](#eine-entität-im-dungeon-inspizieren)
- [Einen Crafting-Schritt durchführen](#einen-crafting-schritt-durchführen)

**Mechaniken, die mit Einschränkungen im Projekt realisiert werden**

Alle Kampfmechaniken bleiben als Grundmechanik des Spiels erhalte, werden allerdings nicht
als Möglichkeit, eine Aufgabe zu realisieren, umgesetzt.

Dies betrifft die Mechaniken:

- [Ein Monster angreifen](#ein-monster-angreifen)
- [Monster besiegen](#monster-besiegen)
- [Schaden von einem Monster zugefügt
  bekommen](#schaden-von-einem-monster-zugefügt-bekommen)
- [Eine Waffe aus dem Inventar ausrüsten](#eine-waffe-aus-dem-inventar-ausrüsten)

**Mechaniken, die im Projekt nicht realisiert werden**

- [Einen Schalter (im Dungeon) betätigen](#einen-schalter-im-dungeon-betätigen), da
  wahrscheinlich sehr komplex in der Umsetzung
- [Eine Entität verschieben](#eine-entität-verschieben), da wahrscheinlich sehr komplex in
  der Umsetzung

### Szenarien

- [Questgeber stellt Frage](#questgeber-stellt-frage)
- [Monster greift mit Frage an](#monster-greift-mit-frage-an)
- [Container ist mit Frage verschlossen](#container-ist-mit-frage-verschlossen)
- [Richtige Container auswählen](#richtige-container-auswählen)
- [Crafting: Items in Crafting-Container
  reinwerfen](#items-müssen-in-crafting-container-geworfen-werden)
- [Schriftrollen in Container ablegen](#schriftrollen-in-container-ablegen)

## Offene Punkte

- Zwischen den abstrakten “Antwortmöglichkeiten” einer Aufgabe (per DSL definiert) und den
  konkreten Items, bzw. Interaktionen die diese abstrakten Antworten im Dungeon abbilden
  muss eine Zuordnung bestehen (vgl. z.B. das Szenario [Blöcke müssen in richtige
  Reihenfolge geschoben
  werden](#entitäten-müssen-in-die-richtige-reihenfolge-geschoben-werden); **damit
  Studierende die Aufgabe korrekt lösen können, muss diese Aufgabe-zu-Dungeon Zuordnung klar
  und deutlich angezeigt werden**, wie das genau aussieht und was dafür nötig ist, ist noch
  unklar
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
- “Brief”, bzw. Item, auf dem etwas drauf steht, was Studierende lesen können
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
- Dinge aus einem Inventar herausnehmen und in das Spielcharakter-Inventar ablegen (bspw.
  aus einer Truhe)
- Dinge aus dem Spielcharakter-Inventar in ein anderes Inventar ablegen (bspw. in eine
  Truhe)
- Dinge aus dem Spielcharakter-Inventar auf den Boden fallen lassen
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
- Auf eine richtige “Antwort” reagieren und eine Belohnung erteilen
- Auf eine falsche “Antwort” reagieren und eine Bestrafung erteilen

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
  x, y, z. Teilaufgaben liefern Ergebnisse/Items die zum Lösen der Hauptaufgabe nötig sind,
  gelöst werden
