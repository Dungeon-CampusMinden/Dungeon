---
title: "Documentation_Basics"
---

## Installation

Für das Dungeon wird das Java Development Kit 17.x.x oder höher benötigt.

Wie Sie das Projekt in Ihre IDE zu laden, können Sie in dem [Wiki: Import-Project](https://github.com/Programmiermethoden/Dungeon/wiki/Import-Project) nachlesen.

*Falls Sie Probleme beim Installieren haben, schauen Sie in die [Kompatibilitätsliste](https://github.com/Programmiermethoden/Dungeon/wiki/JDK-Kompatibilität) und die [FAQ](https://github.com/Programmiermethoden/Dungeon/wiki/FAQ). Melden Sie sich frühzeitig falls Ihr Problem damit nicht behoben werden konnte.*


## Überblick
- Kurze Erklärung: was ist ein ECS? 
  - E: Entity: Entitites sind die Objekte des Spiels
  - C: Component: Components speichern die Datensätze der Entität. Eine Component-Instanz gehört zu genau einer Enitity.
  - S: System: Beinhalten die eigentliche logik und agieren auf die Components
  - siehe auch [readme.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/readme.md)
- TODO: UML Diagramm mit `Game` (im Center), `Component`, `ECS_System`, `Entity`, `libGDX basics`

## Die Klasse Game
### Erster Start
- Vorgabe ist lauffähig und kann über die run-Funktion der IDE gestartet werden
- Alternativ per Konsole in das Dungeon-Verzeichnes wechseln und `./gradlew run` ausführen

### Gameloop
- Was ist die GameLoop
- Was sind die wichtigen Methode (Frame, Setup) was machen die

## Held bauen

### Entity
- Held ist eine Entität, hat aber eine eigene Klasse unter `ecs/components/entities`
	- vergleiche Abschnitt `Eigene Klasse für Hero` unter [structure_design_desicions.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/structure_design_decisions.md)

- Warum muss das eigentlich ne Entität sein?
- Wie kommt das jetzt in das ECS/Spiel/Game-Loop rein?  


### Held zeichnen
- Was brauch ich da?
- Warum wird der denn jetzt gezeichnet wenn der diese Components hat? (`ECS_Systeme` erklären) 

### Held bewegen
- Was brauch ich da?
- Key Konfiguration erklären

- Um mit WASD den Helden zu steuern, erst die Tastaturbelegung in `configuaration/KeyboardConfig.java` festlegen
- Beispiel:

```java
public static final ConfigKey<Integer> MOVEMENT_UP = new ConfigKey<>(new String[] {"movement", "up"}, new ConfigIntValue(Input.Keys.W));
```

- Dann innerhalb der Methode `checkKeystroke` in `ecs/systems/PlayerSystem.java` die Steuerung des Helden anlegen
- Beispiel:

```java
if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_UP.get()))
            ksd.vc.setCurrentYVelocity(1 * ksd.vc.getYVelocity());
else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_DOWN.get()))
            ksd.vc.setCurrentYVelocity(-1 * ksd.vc.getYVelocity());
```
*Hinweis: quer laufen geht nicht, da immer nur eine Taste abgefragt wird*

- In `starter/Game.java` innerhalb der Methode `setup` ein PlayerSystem erstellen

### Existierenden Helden analysieren
- Was hat der denn noch so? 
- Was ist interessant davon? 

## Erweitert

### Level API
Die LevelAPI ist dafür Zuständig neue Level zu erzeugen und diese zu laden. 
Bevor ein neues Level mit der angegebenen oder zufälligen Größe und einem zufälligen Design geladen werden kann,
muss dieses erstmal erzeugt werden.
Mit `runPreGeneration()` wird ein Level aus `SKIP`-Level-Elementen vorgeneriert, die später durch
die Dungeonelemente wie (`FLOOR`(Bodenplatten des Dungeons), `WALL`(Wände des Dungeons), `HOLE`(fehlende Bodenplatten im Dungeon, die den Weg blockueren)) überschrieben werden. Wände werden am äußeren Rand eines Levels erzeugt.
Liegen die `SKIP`-Level-Elemente neben einem `FLOOR`und es gibt keinen Platz für eine `WALL`, werden die `SKIP`-LevelElemente durch
eine `HOLE` ersetzt.
Es entsteht ein 2D Spielefeld, bei dem jedes Tile mit seinem unmittelbaren Nachbarn verbunden wird.
Funktion onLevelLoad sorg dafür, dass alle vorhandenen Levelelemente beim Laden des Levels vorhanden sind.
Zudem folgt die Erstellung eines Helden und eines wandelnden Monsters.
```java
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
```java
heroPositionComponent.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
```
Die Level werden als ein 2D Tile-Array gespeichert, wobei jedes Tile eine feste Coordinate (Index der Position des Tiles im Array) im Array besitzt.
Ein Tile ist ein einzelnes Feld innerhalb des Levels (`FLOOR`, etc), welches zur Laufzeit zur vollständigen Ebene zusammengesetzt werden kann (Speilfeld). 
Es muss ausßerdem zwischen einer `Coordinate` und einem `Point` unterschieden werden. 
Die `Coordinate` beinhalten zwei integer Werte x und y, durch welche die Tile Positionen im Dungeon bestimmt werden.
Der `Point` beinhalten zwei float Werte x und y und wird verwendet, um die Position einer Entität (`Hero`, `Chest`, etc.) im Spiel anzugeben. 
Die Umwandlung des `Point` zu einer `Coordinate` erfolgt durch Parsen von float zu int, dies dient dazu, damit sich die Entitäten (z.B Monster) 
zwischen den einzelnen Tiles bewegen und miteinander interagieren können.


### HUD
- Wie erstelle ich Elemente für das HUD
- Text, Bild, Button

##Erstellen weiteren Contents

- siehe [create_own_content.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/create_own_content.md)
