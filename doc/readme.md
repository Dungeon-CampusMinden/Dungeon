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

In diesem Abschnitt werden Ihnen die wichtigsten Klassen im Dungeon vorgestellt.


![Struktur ECS](img/ecs.png)

- `entity` Die Java-Implementierung der Entitäten eines ECS
- `component` Abstrakte Klasse, jedes Component im ECS leitet hiervon ab
- `ECS-Systems` Abstrakte Klasse, jedes System um ECS eitet hiervon ab
- LevelAPI: Kümmert sich darum, dass neue Level erzeugt und geladen werden.
- DungeonCamera: Ihr Auge in das Dungeon.
- libGDXSetup: Bereitet die Anwendung vor, für die Verwendung des Dungeons ist die genau Funktionalität nicht notwendig
- Game erstellt die Entitäten, Components und Systeme des ECS und beinhaltet die Game-Loop. Game ist Ihr Einstiegspunkt in das Dungeon
- Game-Loop: Die Game-Loop ist die wichtigste Komponente des Spieles. Sie ist eine Endlosschleife, welche einmal pro Frame aufgerufen wird. Das Spiel läuft in 30 FPS (also 30 frames per seconds), die Game-Loop wird also 30-mal in der Sekunde aufgerufen. Alle Aktionen, die wiederholt ausgeführt werden müssen, wie zum Beispiel das Bewegen und Zeichnen von Figuren, müssen innerhalb der Game-Loop stattfinden. Das Framework ermöglicht es Ihnen, eigene Aktionen in die Game-Loop zu integrieren. Wie genau das geht, erfahren Sie im Laufe dieser Anleitung.
*Hinweis: Die Game-Loop wird automatisch ausgeführt, Sie müssen sie nicht aktiv aufrufen.*

Zusätzlich existieren noch eine Vielzahl an weiteren Hilfsklassen, mit denen Sie mal mehr oder mal weniger Kontakt haben werden.

### Game

Game implementiert einige wichtige Methoden:

- `setup` wird zu Beginn der Anwendung aufgerufen. In dieser Methode werden die Objekte (wie die Systeme) initialisiert und konfiguriert, welche bereits vor dem Spielstart existieren müssen. In der Vorgabe wird hier bereits das erste Level geladen, die Systeme angelegt und der Herd initialisert.
- `render` Ruft die Logiken der Systeme auf.
- `onLevelLoad` wird immer dann aufgerufen, wenn ein Level geladen wird. Hier werden später Entitäten erstellt, die initial im Level verteilt werden.
- `frame` wird in jedem Frame einmal aufgerufen.
- `main` startet das Spiel.

- `entities`
-entitiestoadd
-entitiestoremove

### Component

- Components speichern immer die Entität zu der sie gehören
- alle bereits implementierten Components finden Sie [hier](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/components/readme.md)
- um eigene Components zu schreiben, leiten Sie von der eigentlichen Component Klasse ab
- siehe [create_own_content.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/create_own_content.md)

### Entity

- Entitäten sind leere Container und speichern Components
- um ein Component einer Entität zu erhalten, benutzen Sie die Methode `entity#getComponent(component.class klass)`
    - Anmerkung: Sie erhalten ein `Optional zurück`
- siehe [create_own_content.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/create_own_content.md)

### System

- um eigene Components zu schreiben, leiten Sie von der ECS_Systems Klasse ab
- in der `ECS_System#update()` iterieren die Systeme über alle Entitäten mit bestimmten Components und agieren darauf
- alle bereits implementierten Systeme finden Sie [hier](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/systems/readme.md)
- siehe [create_own_content.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/create_own_content.md)

### LevelAPI

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




## Übung: Eigenen Helden erstellen

Zwar gibt es in den Vorlagen bereits einen Helden (den schauen wir uns am Ende dieses Kapitels genauer an), trotzdem wird Ihnen hier erklärtm wie Sie Ihre erste eigene Entität in das Spiel implementieren.
*Hinweis: @cagix @AMatutat der bestehende Hero lässt aufgrund besteheder Abhängigkeiten nicht einfach löschen, daher ist nachprogrammieren in diesem Abschnitt schwierig*

### Held Entität erstellen

- Warum muss das eigentlich ne Entität sein? 
- - Held ist eine Entität, hat aber eine eigene Klasse unter `ecs/components/entities`
	- vergleiche Abschnitt `Eigene Klasse für Hero` unter [structure_design_desicions.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/structure_design_decisions.md)

- Entität `myHero` in `Game#onLevelLoad` anlegen
    - Warum in onLevelLoad?
- erklären wie der in der Game-Loop ist (entitiesToAdd)


### Held zeichnen

- positinComponent erstellen
    - Wofür ist das da
    - welche werte sind sinnvoll
- AnimationCompoent erstellen
    - Wofür ist das da
    - wie wird eine Animation erstellt

- DrawSystem [Link zum Code](https://github.com/Programmiermethoden/Dungeon/blob/master/game/src/ecs/systems/DrawSystem.java)

### Held bewegen

- VelocityComponent
    - VelocitySystem arbeitet darauf
- PlayableComponent
    - PlayerSystem arbeitet darauf


- Um mit WASD den Helden zu steuern, erst die Tastaturbelegung in `configuaration/KeyboardConfig.java` festlegen *Muss nicht so exolizit erklärt werden, vielleicht raus*
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
*Hinweis: quer laufen geht nicht, da immer nur eine Taste abgefragt wird. Haben Sie eine Idee wie man das umsetzen könnte?*




### Existierenden Helden analysieren

Jetzt schauen wir und den bereits existierenden 

- Warum hat der eine eigene Klasse?
- Welche anderen Components hat der noch?
    - HitboxComponent
        - Wofür ist das da?
        - `heroCollision(HitboxComponent other, Tile.Direction from)`
        - Verwendete Parameter erklären
    - SkillComponent
        - Was ist das?
        - beiden getter erklären
        - Verwendete Parameter erklären/FeuerballSkill erklären
        - StrategyPattern im ECS verlinken
           

## Erweitert

### Head-Up-Display (HUD)
Der HUD ist Teil einer visuellen Benutzeroberfläche eines Spiels, welcher dazu verwendet werden kann den Spieler
während des Spiels mit wichtigen Informationen zu versorgen.
Die Darstellung erfolgt als Overlay, welcher die Informationen als 2D -Text oder als Symbole (z.B Lebensanzeige in Herzform)
auf dem Bildschirm über der Spieleszene anzeigt.
Bevor man mit der HUD arbeiten kann, ist es erforderlich einen `ScreenController` anzulegen, welcher
zur Darstellung und Verwaltung von UI-Elementen verwendet wird.
Für dessen Erstellung wird eine `Batch` benötigt, welche bereits im Spiel integriert ist und wird verwendet, um Objekte 
auf dem Bildschirm darstellen zu können.
Zur HUD Darstellung und der Event bearbeitung ist es zudem erforderlich den neu erstellten `ScreenController`
den anderen Controllern hinzuzufügen. 
Die Konstanten `Constants.WINDOW_WIDTH` und `Constants.WINDOW_HEIGHT` entsprechen einer festen Displaygröße, bei veränderung
des Displays werden alle Elemente durch den `ScreenController` entsprechend ausgerichtet. 

```java
package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import tools.Constants;

public class ScreenController<T extends Actor> extends AbstractController<T> {

public ScreenController(SpriteBatch batch) {
        super();
        stage =
                new Stage(
                        new ScalingViewport(
                                Scaling.stretch, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT),
                        batch);

        Gdx.input.setInputProcessor(stage);
    }
...
}
````
#### UI Elemente 
##### ScreenImage	
Kann verwendet werden, um ein nicht bewegliches Bild an einer bestimmten Position auf dem Bildschirm auszugeben
und zu konfigurieren. 
Bei der Zeichenfläche kann es sich entweder eine Textur, Texturregion, Ninepatch etc. handeln und kann innerhalb
der Grenzen des Bild-Widgets auf verschiedene Weise skaliert und ausgerichtet werden.

```java
public class ScreenImage extends Image {

    /**
     * Creates an Image for the UI
     *
     * @param texturePath the Path to the Texture
     * @param position the Position where the Image should be drawn
     */
    public ScreenImage(String texturePath, Point position) {
        super(new Texture(texturePath));
        this.setPosition(position.x, position.y);
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }
}
````

##### ScreenText
Wird verwendet um Texte an einer bestimmten Stelle durch `setPosition()` des Bildschirms ausgegeben und ermöglicht zudem eine dynamische 
Konfiguration des Standardstils für den generierten Text.

```java
package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import tools.Point;

public class ScreenText extends Label {
    /** Allows the dynamic configuration of the default style for the generated ScreenTexts */
    public static final LabelStyle DEFAULT_LABEL_STYLE;

    static {
        DEFAULT_LABEL_STYLE =
                new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.BLUE).build();
    }

    /**
     * Creates a Text with the default label style at the given position.
     * @param text the text which should be written
     * @param position the Point where the ScreenText should be written 0,0 bottom left
     * @param scaleXY the scale for the ScreenText
     * @param style the style
     */
    public ScreenText(String text, Point position, float scaleXY, LabelStyle style) {
        super(text, style);
        this.setPosition(position.x, position.y);
        this.setScale(scaleXY);
    }

    /**
     * Creates the ScreenText with the default style.
     * @param text the text which should be written
     * @param position the position for the ScreenText 0,0 bottom left
     * @param scaleXY the scale for the ScreenText
     */
    public ScreenText(String text, Point position, float scaleXY) {
        this(text, position, scaleXY, DEFAULT_LABEL_STYLE);
    }
}

````

##### ScreenButton	
Beim Button handelt es sich um eine leere, erweiterbare Schaltfläche, welche einen checked"-Status besitzt und in Abhängigkeit,
ob der Button angeklickt oder nicht angeklickt wurde, den unteren- oder den oberen Hintergrund anzeigt. 
Der Button kann außerdem durch eine Beschriftung in einer Bitmap-Schriftart und beliebiger Textfarbe dargestellt werden,
wobei sich die Textfarbe des Buttons in Abhängigkeit der Zustände verändern kann.  
Auch das Erweitern des Buttons durch ein Bild-Widget ist ebenfalls möglich, welches sich in Abhängigkeit der Zustände
gecklickt oder nicht geklickt verändern kann. Größe und Ausrichtung des Elementes können durch folgende
Funktionen `setScale()` sowie `setPosition()` verändert werden.

```java
package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import tools.Constants;
import tools.Point;

public class ScreenButton extends TextButton {
    private static final TextButtonStyle DEFAULT_BUTTON_STYLE;

    static {
        DEFAULT_BUTTON_STYLE =
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.BLUE)
                        .setDownFontColor(Color.YELLOW)
                        .build();
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     * @param style the TextButtonStyle to use
     */
    public ScreenButton(
            String text, Point position, TextButtonListener listener, TextButtonStyle style) {
        super(text, style);
        this.setPosition(position.x, position.y);
        this.addListener(listener);
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     * <p>Uses the DEFAULT_BUTTON_STYLE
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     */
    public ScreenButton(String text, Point position, TextButtonListener listener) {
        this(text, position, listener, DEFAULT_BUTTON_STYLE);
    }
}

````
