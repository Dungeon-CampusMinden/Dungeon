---
title: "Documentation_Basics"
---

# Quickstart


Dieses Dokument liefert einen Einstieg in das Dungeon. Es erläutert die Installation des Spiels und die ersten Schritte, um eigene Inhalte zum Dungeon hinzuzufügen. Es dient als Grundlage für alle weiteren Praktika. Lesen Sie das Dokument daher aufmerksam durch und versuchen Sie sich zusätzlich selbst mit dem Aufbau vertraut zu machen.
Das Spiel befindet sich im [`Dungeon`](https://github.com/Programmiermethoden/Dungeon)-Repo.

Sie benötigen nur dieses Projekt für die Aufgaben, die zusätzlichen Abhängigkeiten werden automatisch über Gradle eingebunden.

*Hinweis: Achten Sie darauf, Daten nur dann in öffentliche Git-Repos zu laden, wenn Sie die nötigen Rechte an diesen Daten haben. Dies gilt insbesondere auch für Artefakte wie Bilder, Bitmaps, Musik oder Soundeffekte.*

## Installation

Für das Dungeon wird das Java Development Kit 17.x.x (JDK 17) oder höher benötigt, stellen Sie sicher, dass Sie es installiert haben.

Laden Sie das Projekt herunter und binden Sie es als Gradle-Projekt in Ihre IDE ein. 
Eine genauere Anleitung finden Sie [hier](https://github.com/Programmiermethoden/Dungeon/wiki/Import-Project)

Sie können über die run-Funktion Ihrer IDE überprüfen, ob die Installation lauffähig ist. Alternativ können Sie per Konsole in das Dungeon-Verzeichnis wechseln und `./gradlew run` ausführen.


*Falls Sie Probleme beim Installieren haben, schauen Sie in die [Kompatibilitätsliste](https://github.com/Programmiermethoden/Dungeon/wiki/JDK-Kompatibilität) und die [FAQ](https://github.com/Programmiermethoden/Dungeon/wiki/FAQ). Melden Sie sich frühzeitig falls Ihr Problem damit nicht behoben werden konnte.*


## Grundlagen

Zu Beginn einige grundlegende Prinzipien, die Sie verstanden haben sollten, bevor Sie mit dem Dungeon arbeiten.

Das Dungeon benutzt das Cross-Plattform-Java-Framework [`libGDX`](https://libgdx.com) als Backend.
Dieses ist im `Dungeon`-Projekt bereits als Abhängigkeit in die Gradle-Konfiguration integriert, Sie müssen dieses nicht extra installieren. Die Ihnen zur Verfügung gestellten sind so umgesetzt, dass Sie kein tieferes Verständnis für `libGDX` benötigen, um die Aufgaben zu lösen. Sollten Sie allerdings einmal auf Probleme stoßen, kann es unter Umständen helfen, einen Blick in die [Dokumentation von `libGDX`](https://libgdx.com/wiki/) zu werfen.


Das `Dungeon`-Projekt fungiert, ganz vereinfacht gesagt, als eine Facade zwischen `libGDX` und Ihrer eigenen Implementierung. Es implementiert ein Entity-Component-System (ECS):
- Entity: Entitites sind die Elemente (Helden, Monster, Schatzkisten, etc.) des Spiels
- Component: Components speichern die Datensätze der Entitäten (z.B. die Lebenspunkte)
- System: Beinhalten die eigentliche Logik und agieren auf die Components

*Weiteres zum ECS im Dungeon erfahren Sie [hier](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/readme.md).*

Sie selbst nutzen und erweitern die Components und Systeme der Vorgaben. Sie werden ebenfalls neue Entities, Components und Systeme konzeptionieren und implementieren. So erschaffen Sie z.B. Ihre eigenen Monster und fallengespickte Level.

Sie werden im Laufe der Praktika verschiedene Assets benötigen. Diese liegen per Default im `assets`-Verzeichnis. Sie können das Standardverzeichnis in der `build.gradle` anpassen.

- Standardpfad für Texturen: `assets/`
- Standardpfad für Charaktere: `assets/character/`
- Standardpfad für Level-Texturen: `assets/textures/dungeon/`

## Strukturen

- TODO: upgedatetes UML-Diagramm, wie [hier](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/img/ecs.png)  


## Die Klasse Game

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
