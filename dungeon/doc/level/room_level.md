---
title: "Room-Level: Konzept Designentscheidungen"
---

Dieses Dokument ist eher Sammlung von Gedanken/Problemen/Ideen, die mit der Integration von Graphenebenen zu tun haben.
Das ist der Milestone [Graphenebenen integrieren](https://github.com/Dungeon-CampusMinden/Dungeon/milestone/56).

## 1. Ausgangssituation

Für die definierten Aufgaben werden Level benötigt.
Für die Lehrperson, das DSL und das Petri-Netz ist der genaue Aufbau der Level nicht wichtig.
Im Petri-Netz wird lediglich angegeben, welche Aufgabe nach welcher Aufgabe kommt. Wie die Level aussehen, ist daher egal. Es muss nur sichergestellt werden, dass der Level-Bereich der zweiten Aufgabe erst erreicht werden kann, wenn diese freigeschaltet ist (zum Beispiel, wenn die erste Aufgabe bearbeitet wurde). In einem Level findet nur eine Aufgabe statt, eine Aufgabe kann jedoch in mehreren Leveln verteilt sein (zum Beispiel muss man im ersten Level mit dem Zauberer sprechen, im zweiten Level die Items sammeln und dann wieder zum Zauberer zurückkehren)

Die Level können als Graph gezeichnet werden, wobei jeder Knoten ein Level ist und jede Kante eine Verbindung zwischen den Leveln darstellt. Wir haben die Bezeichnung "Raum" für diese Level eingeführt und dabei sollten wir bleiben. Im Hinterkopf sollte aber klar sein, dass ein `Raum == ILevel` ist.

Die Kanten setzen wir mit Türen um, wobei an jeder Raumseite (oben, unten, links, rechts) maximal eine Tür platziert werden kann. Das bedeutet auch, dass jeder Raum maximal 4 Kanten haben kann. (vielleicht doch mehr, siehe 4.3)

Bei der Generierung der Räume und der Türen ist darauf zu achten, dass hier eine konstante Logik verfolgt wird. Eine Tür, die oben in Raum A liegt, sollte mich unten in Raum B herausbringen, und wenn ich durch die Tür zurückgehe, sollte ich wieder oben in Raum A herauskommen. Eine Tür ist ein `Tile`.

*Anmerkung* Die gesetzten Grenzen hatten wir uns damals beim Prototypen überlegt, da wir die Implementierung so deutlich einfacher gestalten konnten, und die gesetzten Grenzen sind nicht einschränkend für das eigentliche Ziel.


## 2. Generierung der Level

Für die Generierung solcher Level verwenden wir unterschiedliche Generatoren und Schritte.

1. Graphengenerator: Dieser generiert einen Graphen (unter Berücksichtigung der oben aufgeführten Grenzen).

*Anmerkung* Wir betrachten jetzt die Levelgenerierung für eine Aufgabe. Für die Verkettung der Aufgaben siehe weitere Informationen unter Fragen 3.2.
Damit der Generator weiß, wie viele Knoten im Graphen enthalten sein sollen, wird ein `Set<Set<Entity>>` beim Generierungsprozess übergeben. Jedes `Set<Entity>` enthält die Entitäten für einen Raum (damit kann ich dann gezielt angeben, dass bestimmte Entitäten im gleichen Raum sein sollen bzw. in unterschiedlichen Räumen). `Set<Set<Entity>>.size()` gibt daher an, wie viele Knoten der Graph haben muss.
Der Generator legt dann das `Set<Entity>` als Payload in den Knoten des Graphen ab.
Die Kanten können vom Generator nach Belieben gezogen werden, auf die Verbindung der Räume für eine Aufgabe habe ich in der Eingabe also keinen Einfluss.

2. Raumgenerator: Der Raumgenerator erzeugt für jeden Knoten ein `ILevel` und platziert die Entitäten in diesem Level (siehe auch #900).
Größe, Aussehen und Co können zufällig gewählt werden, wobei die Größe die Anzahl der Entitäten mit einbeziehen sollte.
*Anmerkung* Später könnte man die API dann noch um Schnittstellen zur gezielten Auswahl der Größe oder des Designs des Raums erweitern.
*Anmerkung* Teil der Konfiguration könnte auch "filler content" sein, also ob zufälliger "Spielinhalt" im Raum platziert werden soll oder nicht. Auch später möglich.

3. Logisches Verbinden der Türen: Die Türen müssen jetzt noch miteinander verbunden werden, damit sie auch wirklich den Raumwechsel ermöglichen.
Dafür siehe auch 3.2 und 3.3.

## 3. Fragen

Eine Sammlung an Fragen die es noch zu klären gilt.

### 3.1 Wo werden die `Set<Set<Entity>>` initial gespeichert (mit @malte-r geklärt)

Der `TaskBuilder` wird "mir" diese Collections zurückgeben. der TaskBuilder wird für jedes `Task` Objekt aufgerufen => für jede Teilaufgabe.


### 3.2 Werden alle Level für alle Aufgaben direkt generiert oder "on demand"? Wie werden die Level der verschiedenen Aufgabe miteinander verbunden?

Bei der Generierung der Graphen und Level stellt sich die Frage, ob direkt das gesamte Petri-Netz "übersetzt" wird (Vollständige Generierung vor der Spielzeit) oder die Graphen und Level erst generiert werden, wenn sie benötigt werden (Generierung "on demand").

Grundsätzlich wird der Ansatz verfolgt, für jede Aufgabe einen eigenen Sub-Graphen zu generieren der dann mit den Subgraphen der anderen Aufgaben verbunden wird.

#### Vollständige Generierung vor der Spielzeit. (<= Präferiertes Vorgehen, da einfacher in der Implementierung, Wartung und Dokumentation)

Das Gesamte Petri-Netz wird beim Start des Spiels in einen Level-Graph übersetzt.
Alle Kanten werden durch Türen implementiert, auch wenn im Spiel nur eine davon betretbar ist (Verzweigung im Petri-Netz),

Vorteile:
- Es muss nur einmal der Generierungsprozess gestartet werden, es entfällt daher Konzeptioneller Aufwand die Generierung aus verschiedenen Stellen im Code anzuregen.
- Die verschiedenen Pfade durch das Petri-Netz werden auch im Spiel visualisiert, da hier mehrere verschlossene Türen zu sehen sind und sich nur einige Türen davon öffnen, je nachdem welcher Pfad gewählt wird.
- Die Türen können bereits alle logisch miteinander Verbunden werden, da bereits alle Räume existieren.

Nachteile:
- Es werden Level generiert, die vielleicht vom Spieler nie gesehen werden, da dieser a) vorher aufhört/stirbt oder b) durch bedingte Verzweigungen einen anderen Pfad durch das Petr Netz geht

Mögliche Subform:
Man könnte auch nur den vollständigen Graphen generieren und den konkreten Raum erst dann, wenn der Spieler den Raum betritt. Das Würde die Laufzeitbelastung etwas besser aufteilen.

Ergänzung von @malt-r : Man könnte auch die Sub-Graphen erst generieren, wenn der "Levelabschnitt" des Subgraphen zum ersten mal betreten wird. Solange könnte ein Place-Holder Raum für den gesamten Task als Placeholder agieren, welcher dann durch das sub-graphen Konstrukt ausgetauscht wird.  Der initiale Graph wäre dann quasi eine Übersetzung des Petri-Netz auf höchster ebene, also ohne Teilaufgaben.

#### Generierung "on demand"

Das Petri-Netz wird schrittweise in einen Level-Graph übersetzt.

Vorteil:
- Keine unnötige (vor) Generierung
- Bei Verzweigungen im Petri-Netz muss nur eine Tür implementiert werden, dessen logische Verbindung dann on-demand gesetzt wird. Das könnte weniger verwirrend sein, als eine Tür die für mich niemals aufgeht.

Nachteil:
- Konzeptioneller Aufwand: Wann und wie wird aus dem Code heraus der Generierungsprozess erneut gestartet
- Konzeptioneller Aufwand: Wie funktioniert das logische Verbinden der Türen zur Laufzeit?
- Keine Visualisierung der verschiedenen Pfade durch das Petri-Netz im Level.

#### Kombination beider vorgehen

Denkbar wäre es auch, einen Graphen nur solange "vor" zu generieren bis es im Petri-Netz eine Verzweigung gibt.
Mein Bauchgefühl sagt mir aber, dass ich mir dann nur die Nachteile beider Konzepte in die Tasche packe....


### 3.3 `Tiles#onEnter`? #504

Die Tür-Tiles müssen so implementiert werden, dass sie beim betreten den Spieler in den nächsten Raum setzen (und den Raum laden).
Im Prototypen haben wir das über eine `Tile#onEnter` Methode umgesetzt. Es wurde dann überprüft, auf welchen Tile der Held  steht und für das Tiel die Methode gertiggerd, im Falle eine Tür, das laden des nächsten Raums.

Für mich riecht das eigentlich stark nach Hitbox und Kollision. Und dann kommt das Thema #504 wieder auf.


### 3.4 Wie werden die so erzeugten Level in das aktuelle System integriert?

Für die Variante "Vollständige Generierung vor der Spielzeit" ist die Frage recht einfach zu beantworten.
Nach der Generierung wird das `currentLevel` auf das erste Level im Graphen gesetzt und danach kann der Generator des Dungeons deaktiviert werden.
Für die "on demand" Lösung stellt sich die Frage, ob das `LevelSystem` die Generierung anstößt (das macht mir Kopfweh) oder das Petri-Netz das irgendwie macht (macht mir auch Kopfweh);

## 4. Begründungen für Designentscheidungen

### 4.1 Warum ist jeder Raum ein Level, warum nicht eine Menge an Räumen?

Um diese Frage zu beantworten, müssen wir Interpretation und Implementierung voneinander trennen.

Aus Sicht des Spielers werde ich ein Level definitiv als eine Sammlung verschiedener Räume betrachten. In Spielen wie Zelda sehe ich auch einen Dungeon als ein Level und nicht nur als einen einzelnen Raum im Dungeon.

Auf der Implementierungsebene sieht die Situation anders aus. Unsere Level sind als 2D-Matrizen von "Tiles" strukturiert, auf denen verschiedene "Entities" platziert sind. Unsere Systeme sind so gestaltet, dass sie stets mit Entitäten und Tiles im gesamten Level interagieren.

Ein einzelner Raum entspricht dieser "Definition" eines Levels. Er ist ebenfalls eine 2D-Matrix von "Tiles", auf denen verschiedene "Entities" positioniert sind.

Inhaltlich gesehen unterscheiden sich Räume nur an einer bestimmten Stelle von anderen Leveln. In einem typischen Level befindet sich möglicherweise eine Falltür, die den Spieler ins nächste Level bringt, wobei es keinen Rückweg gibt. Das Level wird in dem Moment generiert, in dem der Spieler die Falltür betritt. Im Gegensatz dazu können in Räumen Türen vorhanden sein, die es dem Spieler ermöglichen, zwischen den Räumen hin und her zu wechseln. Aus diesem Grund müssen wir die verschiedenen Räume im Gedächtnis behalten. Diese Unterscheidung liegt jedoch auf der Implementierungsebene und betrifft nicht notwendigerweise ein neues übergeordnetes Konzept auf Level-Ebene.

Daher lassen sich Räume problemlos in das aktuelle Konstrukt integrieren, und es ist nicht erforderlich, auf Systemebene eine Unterscheidung vorzunehmen, ob gerade mit einem raumbasierten Level oder einem "normalen" Level gearbeitet wird.

Die Alternative wäre ein "Level", das eine Struktur aus einem Graphen oder einer Menge von "Room"-Objekten speichert. Das "Level"-Konstrukt müsste Informationen darüber bereithalten, welcher Raum gerade der aktive ist, und diese Informationen über eine API an das "Game" weitergeben. Dort würde dann mit dem Raum genauso wie mit jedem anderen Level gearbeitet werden, was erneut verdeutlicht, dass "Raum" gleichbedeutend mit "Level" ist.

Diese Alternative würde zwar die Implementierung etwas näher an die Interpretation heranbringen. Als Programmierer finde ich jedoch, dass dies sogar noch verwirrender wäre. Nun hätten wir ein "Level", das jedoch nur Räume enthält, die wiederum die eigentlichen Level darstellen. Dies würde zusätzliche Komplexität hinzufügen, ohne klare strukturelle oder implementierungstechnische Vorteile zu bieten. Es würde im Grunde genommen eine unnötige Wrapper-Schicht um ein bereits funktionierendes Konzept darstellen.

Zusätzlich stellt sich die Frage: Was würde mit den aktuellen Levels geschehen, die keine Räume sind? Wären sie dann letztendlich doch Räume, aber eben nur jeweils einer pro Level?

Bisher konnte ich keine Antwort auf die Frage finden, warum es sinnvoll sein könnte, Levels als eine Menge von Räumen zu speichern.

### 4.2 Warum überhaupt Graphen, wenn ich diese in der DSL eh nicht definiere?

Ursprünglich war geplant, dass der Level-Graph über die DSL definiert werden kann, da dies wohl kaum ein Lehrender wirklich tun würde. Die verbleibende Zeit von uns verlangt jedoch, starke Prioritäten zu setzen, weshalb diese Idee gekippt wurde.

Warum verwenden wir dennoch Graphen? Wären zufällige Level nicht einfacher?

Graphen erlauben es uns, strukturell interessante Level zu generieren, im Vergleich zu einfachen zufälligen Levels. Zusätzlich können wir in der DSL definieren, dass verschiedene Elemente einer Quest in verschiedenen Räumen verteilt sind. Das fordert den Spieler heraus zu erkunden, mehr Monster zu bekämpfen und macht das Spiel generell interessanter.

Dafür bräuchten wir aber streng genommen noch keinen konkreten Graphen. Wir können einfach Räume generieren und miteinander verbinden. Das würde zwar indirekt einen Graphen erzeugen, mit dem müssten wir jedoch nicht direkt arbeiten oder ihn direkt speichern.

Der Level-Graph soll letztendlich mit dem Petri-Netz gemappt werden. Dadurch soll das Implementieren der Steuerelemente vereinfacht werden. Wir können eine Transition im Petri-Netz mit einer Kante im Level-Graphen, also einer Tür im Level, mappen. Die Tür wird dann erst geöffnet, wenn die entsprechende Transition geschaltet wurde.


### 4.3 Warum nur vier Kanten pro Knoten?

Edit: Beim Ausarbeiten der Begründung für diese Entscheidung ist mir aufgefallen, dass es dafür eigentlich keine starken Argumente gibt. Tatsächlich sogt das aufhaben dieser Regel für einige Vereinfachungen im Code.

Edit 2: Beim implementieren ist mir aufgefallen, dass es das doch einfacher macht (grade beim verbinden der Türen).

=> Die begrenzte Anzahl an Kanten erleichtert die Implementierung + macht die Level für die Spielenden Einfachher zu handeln (viele Türen auf einer Seite können schnell verwirren)
