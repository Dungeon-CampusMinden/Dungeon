---
title: "Konzept der Graphenbasierte Level"
---

Dieses Dokument erklärt das Grundkonzept der graphenbasierten Level und erläutert den Aufbau und Zusammenhang der verschiedenen Generatoren.

*Anmerkung*: Einige der Designentscheidungen sind [hier](room_level.md) erläutert.

## Grundkonzept

Zuerst wird ein Levelgraph generiert. Ein Levelgraph besteht aus mehreren Knoten.
Jeder Knoten im Graphen wird später zu einem Raum in einem Level (Erinnerung: Ein Raum ist eine eigene `ILevel`-Instanz).
Jeder Knoten speichert eine `Set<Entity>` als Payload; diese Entitäten werden später im Raum platziert.
Die Kanten im Graphen geben an, dass die Räume miteinander verbunden sind. Im Spiel bedeutet das, dass eine Tür von einem Raum in den anderen führt. Das System ist so implementiert, dass eine Tür, die rechts im Raum A platziert ist, den Spieler links in Raum B eintreten lässt und umgekehrt.
Die Türen werden als `DoorTile` implementiert und logisch miteinander verbunden (die Türen wissen also, welche andere Tür zu ihn gehört/zu ihnen führt/ zu denen sie führen).
Das `LevelSystem` prüft, ob sich der Spieler auf einer Tür befindet, und wenn ja, wird das Level geladen, zu dem die Tür führt, und der Spieler wird bei der zugehörigen Tür im neuen Raum platziert.

Diese Level haben keinen klassischen Ausgang (keine Falltür); dieser kann jedoch über die `ILevel`-API hinzugefügt werden.

Der Generierungsprozess läuft wie folgt ab:
Der `LevelGraphGenerator` generiert einen `LevelGraph`, und für jeden Knoten im Graphen generiert der `RoomGenerator` einen Raum und speichert diesen im Knoten.
Der `RoomBasedLevelGenerator` (der die beiden anderen auch anstößt) führt dann die logische Verbindung der Türen durch und platziert die Entitäten im Raum.

## Anwendung: Für Studierende

Um graphenbasierte Level zu verwenden, reicht es aus, die Klasse `contrib.level.generator.graphBased.RoomBasedLevelGenerator` zu verwenden.
Diese bietet die statische Methode `#ILevel level(Set<Set<Entity>> entities, DesignLabel designLabel)`. Diese Methode startet den gesamten Generierungsprozess und erstellt ein Level, wobei für jedes übergebene `Set<Entity>` ein eigener Raum im ausgewählten Design erstellt wird. Die Entitäten werden in den Leveln hinterlegt und beim ersten Laden des Levels dem Spiel hinzugefügt.

Damit im Spiel auch das generierte graphenbasierte Level verwendet wird, muss es noch als aktives Level festgelegt werden.

Am besten wird dies in der Main-Methode in der Definition von `Game#userOnSetup` gemacht.

Der Code dafür könnte beispielsweise wie folgt aussehen:

```java
ILevel level = RoomBasedLevelGenerator.level(entities, DesignLabel.randomDesign());
Game.currentLevel(level);
```
Fertig.

Die Klasse `contrib.level.generator.graphBased.RoomBasedLevelGenerator` bietet auch die Methode
`#ILevel level(final LevelGraph graph, DesignLabel designLabel)`, welche die Räume für den übergebenen Levelgraphen generiert usw. Dies kann nützlich sein, wenn Sie die Levelstruktur feiner bestimmen möchten, z. B. indem Sie verschiedene Graphen miteinander verbinden (siehe unten).

## Graphengenerator
Der LevelGraphGenerator generiert den LevelGraph.
Der LevelGraph speichert eine Liste mit allen Knoten, mit denen er verbunden ist (das wird klarer, wenn wir uns das Verbinden von Graphen anschauen). Ansonsten speichert er einen Knoten als root. Die Nodes kennen ihre Nachbarn (so sind die Edges des Graphen implementiert).

### LevelGraph generieren
Im ersten Schritt werden dem Generator schrittweise die Set<Entity>s übergeben. In jedem Schritt erstellt der Generator dann einen Knoten und fügt ihn dem Graphen hinzu, indem er ihn mit einem anderen Knoten im Graphen verbindet.
Eine Verbindung kann nur durchgeführt werden, wenn es passende freie Kanten gibt.
Sollte ein Graph voll sein (es gibt keine freien Kanten) und ein neuer Knoten hinzugefügt werden, wird ein Adapterknoten erstellt (siehe weiter unten).

Aus dem so erzeugten Baum wird dann mithilfe der Methode LevelGraph#addRandomEdges ein Graph erstellt, indem zufällig einige Kanten im Graphen hinzugefügt werden.

### Graphen verbinden
Verschiedene Graphen können miteinander verbunden werden. Das ist vor allem für die Task praktisch, da so für jeden Task ein eigener Graph generiert werden kann, und die Türen zwischen den Räumen der beiden Graphen mithilfe des Petri Nets gesteuert werden können (öffnen und schließen).

Das Verbinden von Graphen funktioniert grundsätzlich wie das Hinzufügen eines Knotens zu einem Graphen.
Beide Graphen werden nach Möglichkeiten zum Verbinden untersucht, also ob es Knoten gibt, die freie Kantenplätze haben, die miteinander verbunden werden können.
Wenn ja, wird die Verbindung hergestellt, wenn nicht, wird ein Adapterknoten hinzugefügt und erneut gesucht.

Graphen werden immer nur an Knoten verbunden, deren Ursprung in den zu verbindenden Graphen liegt. Haben Sie also einen Graphen G1 und G2 und G3, und G1 ist bereits mit G2 verbunden, werden beim Verbinden von G1 und G3 nur die Knoten betrachtet, die ursprünglich aus G1 und G3 stammen (kurz gesagt: G3 und G2 werden nicht direkt, sondern nur indirekt miteinander verbunden).

Wenn die beiden Graphen verbunden werden, wird die Knotenliste der Graphen um die Knoten des anderen Graphen ergänzt (dabei werden auch Knoten betrachtet, die aus einem anderen verbundenen Graphen stammen; G2 hält dann also auch die Knoten aus G3).
Die beiden Grapheninstanzen sind nach dem Verbinden strukturell identisch.

### Adapterknoten

Adapterknoten werden dann eingesetzt, wenn keine Verbindung zwischen einem Graphen und einem Knoten (oder anderen Graphen) hergestellt werden kann.

Das kann vorkommen, wenn:

- Der Graph keine freien Kanten mehr hat.
- Die freien Kanten im Graphen nicht mit den freien Kanten im Knoten verbunden werden können (zum Beispiel, wenn im Graphen nur im Süden freie Kanten vorhanden sind, der Knoten jedoch nur im Osten Platz hat).

In solchen Fällen wird an einem zufälligen Knoten im Graphen ein Adapterknoten hinzugefügt.
Dafür wird eine zufällige Kante des Knotens genommen und zu diesem neuen Knoten umgeleitet.
Falls an der Kante, die umgeleitet wird, ein anderer Knoten hängt, wird dieser ebenfalls an den Adapterknoten angehängt.



