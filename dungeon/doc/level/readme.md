---
title: "Level Basics"
---

## Level Grundlagen

Die Level werden als ein 2D Tile-Array gespeichert, wobei jedes Tile eine feste Koordinate (Index der Position des Tiles im Array) im Array besitzt. Ein Tile ist ein einzelnes Feld innerhalb des Levels (`FLOOR`, `WALL`, `EXT`, `DOOR`, etc.), welches zur Laufzeit zur vollständigen Ebene zusammengesetzt werden kann (Spielbrett). Es muss außerdem zwischen einer `Coordinate` und einem `Point` unterschieden werden. Die `Coordinate` beinhalten zwei Integer-Werte x und y, durch welche die Tile-Positionen im Dungeon bestimmt werden. Der `Point` beinhaltet zwei Float-Werte x und y und wird verwendet, um die Position einer Entität (`Hero`, `Chest`, etc.) im Spiel anzugeben. Die Umwandlung des `Point` zu einer `Coordinate` erfolgt durch Parsen von Float zu Int, dies dient dazu, damit sich die Entitäten (z.B Monster) zwischen den einzelnen Tiles bewegen und miteinander interagieren können.

## LevelSystem

Das `LevelSystem` speichert das aktuelle Level, zeichnet dieses und prüft, ob ein neues Level geladen werden muss. Im `LevelSystem` ist ein Levelgenerator hinterlegt, welcher immer wieder neue Level generiert. Befindet sich der Spieler auf einem Ausgang, wird ein neues Level generiert und geladen.

Die [Raumbasierten Level](room_level.md) verwenden keine klassischen Ausgänge, sondern Türen. Daher prüft der Levelgenerator, ob der Spieler sich auf einer Tür befindet, und wenn ja, lädt das Level zu der Tür, die führt. Wenn der [Graphenbasierte Levelgenerator](graphbased.md) verwendet wird, werden die Räume und die Levelstruktur zu Beginn des Spiels generiert. Es findet keine neue Generierung statt, das Level ist also endlos.

In der Standardkonfiguration wird der [Drunkard Walk](https://de.wikipedia.org/wiki/Drunkard%E2%80%99s_Walk) Algorithmus zum Generieren von Leveln verwendet.

Beim Laden eines neuen Levels wird `Game#onLevelLoad` und damit auch `Game#userOnLevelLoad` aufgerufen.

## Pathfinding

Die Level im Dungeon unterstützen das libGDX-A*-Pathfinding. Mit `Game#findPath` kann der Pfad von einem Tile zum anderen Tile berechnet werden. Dabei wird eine `GraphPath<Tile>`-Instanz zurückgeliefert, diese kann als Liste der Tiles, die zu betreten sind, interpretiert werden. Dabei ist das erste Tile das Start-Tile und das letzte Tile das Endtile.

Anmerkung: Die Pathfindung funktioniert nur innerhalb eines Levels oder Raums. Türen können aktuell nicht mitbetrachtet werden.
