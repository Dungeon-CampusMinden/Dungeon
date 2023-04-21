---
title: "Level Basics"
---

WIP, Issue #530

-   Wie sind Level aufgebaut?
-   Wie funktionieren die Generatoren?
-   Wie genau funktioniert die LevelAPI?
-   Wie funktioniert das Pathfinding?

Die LevelAPI ist dafür Zuständig neue Level zu erzeugen und diese zu laden. Bevor ein neues Level mit der angegebenen
oder zufälligen Größe und einem zufälligen Design geladen werden kann, muss dieses erstmal erzeugt werden. Mit
`runPreGeneration()` wird ein Level aus `SKIP`-Level-Elementen vorgeneriert, die später durch die Dungeonelemente wie
(`FLOOR`(Bodenplatten des Dungeons), `WALL`(Wände des Dungeons), `HOLE`(fehlende Bodenplatten im Dungeon, die den Weg
blockueren)) überschrieben werden. Wände werden am äußeren Rand eines Levels erzeugt. Liegen die `SKIP`-Level-Elemente
neben einem `FLOOR`und es gibt keinen Platz für eine `WALL`, werden die `SKIP`-LevelElemente durch eine `HOLE` ersetzt.
Es entsteht ein 2D Spielefeld, bei dem jedes Tile mit seinem unmittelbaren Nachbarn verbunden wird. Funktion onLevelLoad
sorg dafür, dass alle vorhandenen Levelelemente beim Laden des Levels vorhanden sind. Zudem folgt die Erstellung eines
Helden und eines wandelnden Monsters.

``` java
public void onLevelLoad() {
        currentLevel = levelAPI.getCurrentLevel();

        entities.clear();
        entities.add(hero);
        heroPositionComponent.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());

        // TODO: when calling this before currentLevel is set, the default ctor of PositionComponent
        // triggers NullPointerException
        setupDSLInput();
    }
```

Damit wird der Held auf seine Startposition zu beginn des Spils gesetzt.

``` java
heroPositionComponent.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
```

Die Level werden als ein 2D Tile-Array gespeichert, wobei jedes Tile eine feste Coordinate (Index der Position des Tiles
im Array) im Array besitzt. Ein Tile ist ein einzelnes Feld innerhalb des Levels (`FLOOR`, etc), welches zur Laufzeit
zur vollständigen Ebene zusammengesetzt werden kann (Speilfeld). Es muss ausßerdem zwischen einer `Coordinate` und einem
`Point` unterschieden werden. Die `Coordinate` beinhalten zwei integer Werte x und y, durch welche die Tile Positionen
im Dungeon bestimmt werden. Der `Point` beinhalten zwei float Werte x und y und wird verwendet, um die Position einer
Entität (`Hero`, `Chest`, etc.) im Spiel anzugeben. Die Umwandlung des `Point` zu einer `Coordinate` erfolgt durch
Parsen von float zu int, dies dient dazu, damit sich die Entitäten (z.B Monster) zwischen den einzelnen Tiles bewegen
und miteinander interagieren können.
