# Quickstart

Dieses Dokument liefert einen Einstieg in das PM-Dungeon. Es erläutert die Installation des Frameworks und die ersten Schritte, um eigene Inhalte zum Dungeon hinzuzufügen. Es dient als Grundlage für alle weiteren Praktika. Lesen Sie das Dokument daher aufmerksam durch und versuchen Sie sich zusätzlich selbst mit dem Aufbau vertraut zu machen.
Das Framework ist in `core` und `desktop` aufgeteilt, wobei `core` das Framework und `desktop` ein Basis-Starter ist. 

*Hinweis: Achten Sie darauf, Daten nur dann in öffentliche Git-Repos zu laden, wenn Sie die nötigen Rechte an diesen Daten haben. Dies gilt insbesondere auch für Artefakte wie Bilder, Bitmaps, Musik oder Soundeffekte.*

## Installation

Um das PM-Dungeon-Framework zu nutzen haben Sie zwei Möglichkeiten.

1. Erstellen Sie sich ein Fork des [`desktop`-Repository](https://github.com/PM-Dungeon/desktop) und ziehen Sie sich einen lokalen Klon auf Ihr Gerät. (Empfohlen)
2. Erstellen Sie eigenständig ein neues Projekt und binden Sie [`core`](https://repo1.maven.org/maven2/io/github/pm-dungeon/core/) in Ihr Projekt ein. Beachten Sie dabei, dass Sie damit nur das "Backend" des Frameworks implementieren. Um das "Frontend" nutzen zu können, benötigen Sie einen libGDX-Launcher und die entsprechenden Abhängigkeiten; ein einfaches Basisbeispiel finden Sie im [`desktop`-Repository](https://github.com/PM-Dungeon/desktop). 

## Arbeiten mit dem Framework

Zu Beginn einige grundlegende Prinzipien, die Sie verstanden haben sollten, bevor Sie mit dem Dungeon arbeiten.

Das PM-Dungeon benutzt aktuell das Cross-Plattform Java-Framework [`libGDX`](https://libgdx.com) als Backend. Dieses ist im `core`- und `desktop`-Projekt bereits als Abhängigkeit in die Gradle-Konfiguration integriert, Sie müssen dieses nicht extra installieren. Die Ihnen zur Verfügung gestellten Vorgaben sind so umgesetzt, dass Sie kein tieferes Verständnis für das Framework oder `libGDX` benötigen, um die Aufgaben zu lösen. Sollten Sie allerdings einmal auf Probleme stoßen, kann es unter Umständen helfen, einen Blick in die Dokumentation von `libGDX` zu werfen.

Das Framework ist in ein Frontend ([`desktop`]((https://github.com/PM-Dungeon/desktop))) und ein Backend ([`core`]((https://github.com/PM-Dungeon/core))) aufgeteilt. 
Das Frontend setzt die Parameter, erzeugt ein Fenster und startet die Anwendung.
Das Backend liefert die Schnittstellen, mit denen Sie arbeiten, und integriert die `libGDX`.

Sie selbst schreiben die Logik des Spiels und implementieren die Helden/Monster/Gegenstände. Sie können Ihren Code am einfachsten in einem Fork des `desktop`-Projekts entwickeln. 

Bis auf seltene (dokumentierte) Ausnahmen werden Sie nicht gezwungen sein, an den Vorgaben Änderungen durchzuführen. 

Sie werden im Laufe der Praktika verschiedene Assets benötigen. Diese liegen per Default im `asset`-Verzeichnis. Sie können das Standardverzeichnis in der `build.gradle` anpassen.
  - Standardpfad für Texturen: `assets/`
  - Standardpfad für Level: `assets/level` 
  - Standardpfad für Level-Texturen: `assets/textures/level` 

## Strukturen 

Bevor wir mit der eigentlichen Implementierung des Spiels anfangen, eine kurze Erklärung über den Aufbau des Frameworks.

- Das Framework verwendet sogenannte `Controller` um die einzelnen Aspekte des Spiels zu managen und Ihnen das Leben einfacher zu machen. 
    - `EntityController`: Dieser verwaltet alle "aktiven" Elemente wie Helden, Monster, Items etc. 
    - `LevelAPI`: Kümmert sich darum, dass neue Level erzeugt und geladen werden. 
    - `HUDController`: Verwaltet alle Bildschirmanzeigen die Sie implementieren.  
    - `MainController` Verwaltet die anderen `Controller` und beinhaltet die Game-Loop. Ihre Implementierung wird Teil des `MainController` 
- Game-Loop: Die Game-Loop ist die wichtigste Komponente des Spieles. Sie ist eine Endlosschleife, welche einmal pro [Frame](https://de.wikipedia.org/wiki/Bildfrequenz) aufgerufen wird. Das Spiel läuft in 30-FPS (also 30 *frames per seconds*, zu Deutsch 30 Bildern pro Sekunde), die Game-Loop wird also 30mal in der Sekunde aufgerufen. Alle Aktionen, die wiederholt ausgeführt werden müssen, wie zum Beispiel das Bewegen und Zeichnen von Figuren, müssen innerhalb der Game-Loop stattfinden. Das Framework ermöglicht es Ihnen, eigene Aktionen in die Game-Loop zu integrieren. Wie genau das geht, erfahren Sie im Laufe dieser Anleitung. *Hinweis: Die Game-Loop wird automatisch ausgeführt, Sie müssen sie nicht aktiv aufrufen.*
- Zusätzlich existieren noch eine Vielzahl an weiteren Helferklassen mit dem sie mal mehr oder mal weniger Kontakt haben werden. 
- `Painter`: Kümmert sich darum, dass die Inhalte grafisch dargestellt werden. 
- `DungeonCamera`: Ihr Auge in das Dungeon. 
- Unterschiedliche Interfaces, welche Sie im Verlauf dieses Dokumentes kennen lernen werden. 

## Erster Start

In diesen Abschnitt werden alle Schritte erläutert, die zum ersten Start der Anwendung führen.

- Legen Sie sich eine neue Klasse an. Der Einfachheit halber wird diese Klasse im weiteren Verlauf `MyGame` genannt. Sie können die Klasse aber nennen wie Sie wollen.
- `MyGame` muss von `MainController` erben.
- Implementieren Sie alle notwendigen Methoden. *Hinweis: Weitere Informationen zu diesen Methoden erfolgen im Laufe der Dokumentation.*
    - `setup()`
    - `beginFrame()`
    - `enFrame()`
    - `onLevelLoad()`  
- Rufen Sie zum Ende der `setup()`-Methode `levelAPI.loadLevel()` auf um das erste Level zu laden:

  ```java
  @Override
  protected void setup() {
    levelAPI.loadLevel();
  }
  ```
  
- Fügen Sie die `main`-Methode hinzu

  ```java
  public static void main(String[] args) {
      DesktopLauncher.run(new MyGame());
  }
  ```

- Passen Sie innerhalb der `code/build.gradle` den Programmeinstiegspunkt (`project.ext.mainClassName = "desktop.DesktopLauncher"`) an und legen Sie ihn auf Ihre Klasse (`MyGame`) fest. Vergessen Sie dabei nicht, auch das Package anzugeben.

Das Spiel sollte nun starten und Sie sollten einen Ausschnitt des Levels sehen können. 

Bevor wir nun unseren Helden implementieren sollten wir verstehen, was genau der `MainController` eigentlich ist. Wie der Name schon vermuten lässt, ist dies die Haupt-Steuerung des Spiels. Er bereitet alles für den Start des Spieles vor, verwaltet die anderen Controller und enthält die Game-Loop. Wir nutzen `MyGame` um selbst in die Game-Loop einzugreifen und unsere eigenen Objekte wie Helden und Monster zu verwalten. Der `MainController` ist der Punkt, an dem alle Fäden des Dungeons zusammenlaufen. Im Folgenden wird Ihnen erklärt, wie Sie erfolgreich mit dem `MainController` arbeiten.

## Eigener Held

Jetzt, wo Sie sichergestellt haben, dass das Dungeon ausgeführt werden kann, geht es darum, das Spiel mit Ihren Inhalten zu erweitern. Im Folgenden wird ein rudimentärer Held implementiert, um Ihnen die verschiedenen Aspekte des Dungeon zu erläutern.

Fangen wir damit an eine neue Klasse für den Helden anzulegen. Da unser Held eine Animation haben soll, implementieren wir das Interface `IAnimatable`. Dies erlaubt es uns, unseren Helden zu animieren. Für Objekte, die keine Animation haben, sondern nur eine statische Textur, würden wir das Interface `IEntity` implementieren.

```java
import graphic.Animation;
import interfaces.IAnimatable;

public class Hero implements IAnimatable {
  //Die SpriteBatch ist "das Papier" auf dem unser Held dargestellt werden soll.   
  private SpriteBatch batch; 
  //Der Painter zeichnet den Helden.
  private Painter painter;   
  public Hero(SpriteBatch batch, Painter painter){
      this.batch=batch;
      this.painter=painter;
  }
    
  @Override
  public Animation getActiveAnimation() {
    return null;
  }
    @Override
    public Animation getActiveAnimation() {
        return null;
    }

    @Override
    public void update() {}

    @Override
    public boolean removable() {
        return false;
    }

    @Override
    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public Point getPosition() {
        return null;
    }

    @Override
    public Painter getPainter() {
        return painter;
    }
}      
```

### Der bewegte (animierte) Held

Fangen wir damit an, die Animation für unseren Helden zu erstellen. Eine Animation ist eine Liste mit verschieden Texturen, die nacheinander abgespielt werden.

```java
// Anlegen einer Animation
private Animation idleAnimation;
private SpriteBatch batch; 
private Painter painter;   
public Hero(SpriteBatch batch, Painter painter) {
    this.batch=batch;
    this.painter=painter;
    
    // Erstellen einer ArrayList
    List<String> idle = new ArrayList<>();
    // Laden der Texturen für die Animation (Pfad angeben)
    idle.add(ASSETS_PATH_TO_TEXTURE_1.png);
    idle.add(ASSETS_PATH_TO_TEXTURE_2.png);
    // Erstellen einer Animation, als Parameter wird die Liste mit den Texturen
    // und die Wartezeit (in Frames) zwischen den Wechsel der Texturen angegeben
    idleAnimation = new Animation(idle, 8);
}

// Da unser Held aktuell nur eine Animation hat,
// geben wir diese als aktuell aktive Animation zurück
@Override
public Animation getActiveAnimation() {
    return this.idleAnimation;
}
```

Super, jetzt hat unser Held eine Animation. Nun muss diese noch im Spiel gezeichnet werden.

Da das Dungeon framebasiert ist, muss unser Held in jedem Frame (also 30-mal in der Sekunde) neu gezeichnet werden. Dazu verwenden wir den `EntityController`. 

Der `EntityController` verwendet das *Observer-Pattern* (vergleiche Vorlesung) um Instanzen vom Typen `IEntity` und `IAnimatable` zu verwalten. Er sorgt dafür, dass die vom Interface bereitgestellte `update`-Methode jeder Entität in der Game-Loop aufgerufen wird. Dazu hält er eine Liste mit allen ihm übergebenen Entitäten. Weiter unten sehen Sie, wie Sie den `EntityController` verwenden können, um unseren Helden managen zu lassen.

*Hinweis: Der `MainController` verfügt bereits über einen `EntityController`, welchen Sie innerhalb von `MyGame` mit `entityController` ansprechen können.*

Dafür implementieren wie nun die Methode `update`. 

```java
@Override
public void update() {
    // zeichnet den Helden.
    // Wird als default Methode vom IAnimatable Interface mitgeliefert
    this.draw();
}
```

### Wo bin ich?

Jetzt ist unsere erste Version vom Helden fast fertig, wir benötigen lediglich noch ein paar kleinere Informationen. Zuerst muss unser Held wissen, wo er überhaupt im Dungeon steht, dafür benötigt er eine Position. Zusätzlich wäre es hilfreich, wenn unser Held das Level (die Dungeon-Ebene) kennen würde, da wir so vermeiden können, dass sich unser Held durch Wände bewegt oder sich außerhalb des eigentlichen Spielbereiches aufhält.

```java
//Positionen werden als float x und float y in der Klasse Point gespeichert
private Point position;
//Das Level
private Level level;

//So können wir später dem Helden das aktuelle Level übergeben
public void setLevel(Level level) {
    this.level = level;
    this.position = level.getStartTile().getGlobalPosition().toPoint();
}
```

Jetzt wo unser Held erst einmal fertiggestellt ist, müssen wir ihn noch im Dungeon einfügen. Dies tun wir in `MyGame`

Die Methode `setup` ermöglicht es uns, einmalig zu Beginn der Anwendung Vorbereitungen zu treffen. Wir nutzen dies nun, um unseren Helden anzulegen und ihn dem `EntityController` zu übergeben.

```java
//Unser Held
private Hero hero;
@Override
public void setup() {
    //Erstellung unseres Helden
    hero = new Hero(batch, painter);
    //Ab jetzt kümmert sich der EntityController um das aufrufen von Held.update
    entityController.add(hero);
    //unsere Kamera soll sich immer auf den Helden zentrieren.
    camera.follow(hero);

    levelAPI.loadLevel();
}
```

Die Kamera `camera` ist unser "Auge" im Dungeon. Mit `camera.follow` können wir ihr ein Objekt übergeben, welches von nun an immer im Mittelpunkt des Bildes sein soll.

### Sie werden platziert

Jetzt müssen wir unseren Helden nur noch im Level platzieren. Dafür bietet Sich die Methode `onLevelLoad` an, diese wird immer dann automatisch aufgerufen, wenn von der `LevelAPI` ein neues Level geladen wird. Die `LevelAPI` ist dafür zuständig, die Struktur des Dungeons zu laden und zu zeichnen. Sie hält eine Referenz auf das eigentliche `Level`-Objekt.

*Hinweis: Der `MainController` verfügt bereits über eine `LevelAPI` , welchen Sie innerhalb von `MyGame` mit `levelAPI` ansprechen können*

```java
@Override
public void onLevelLoad() {
    hero.setLevel(levelAPI.getCurrentLevel());
}
```

Jetzt wird der Held bei jedem neuen Level auf den Startpunkt platziert..

Zu guter Letzt sollten wir noch prüfen, ob unser Held auf der Leiter zum nächsten Level steht und wenn ja, das nächste Level laden (also die Leiter herab steigen in die nächste Ebene des Dungeons).

Die Methoden `beginFrame` und `endFrame` ermöglichen es uns, unsere eigenen Aktionen in die Game-Loop zu integrieren. Alles, was wir in der Methode `beginFrame` implementieren, wird am Anfang jedes Frames ausgeführt, analog alles in `endFrame` am Ende eines Frames.

Zum Überprüfen, ob ein neues Level geladen werden soll, verwenden wir diesmal die `endFrame`-Methode.

```java
@Override
public void endFrame() {
    if (hero.getPosition().toCoordinate.equals(levelAPI.getCurrentLevel().getEndTile().getGlobalPosition()))
        levelAPI.loadLevel();
}
```

### WASD oder die Steuerung des Helden über die Tastatur

Damit wir unser Spiel auch richtig testen können, sollten wir unserem Helden noch die Möglichkeit zum Bewegen geben. Dafür fügen wir Steuerungsoptionen in der `Held.update`-Methode hinzu:

```java
// Temporären Point um den Held nur zu bewegen, wenn es keine Kollision gab
Point newPosition = new Point(this.position);
// Unser Held soll sich pro Schritt um 0.1 Felder bewegen.
float movementSpeed = 0.1f;
// Wenn die Taste W gedrückt ist, bewege dich nach oben
if (Gdx.input.isKeyPressed(Input.Keys.W)) newPosition.y += movementSpeed;
// Wenn die Taste S gedrückt ist, bewege dich nach unten
if (Gdx.input.isKeyPressed(Input.Keys.S)) newPosition.y -= movementSpeed;
// Wenn die Taste D gedrückt ist, bewege dich nach rechts
if (Gdx.input.isKeyPressed(Input.Keys.D)) newPosition.x += movementSpeed;
// Wenn die Taste A gedrückt ist, bewege dich nach links
if (Gdx.input.isKeyPressed(Input.Keys.A)) newPosition.x -= movementSpeed;
// Wenn der übergebene Punkt betretbar ist, ist das nun die aktuelle Position
if(level.getTileAt(newPosition.toCoordinate()).isAccessible())
    this.position = newPosition;
```

## Head-up-Display (HUD)

Dieser Abschnitt soll Ihnen die Werkzeuge nahebringen, welche Sie für die Darstellung eines HUD benötigen.

Um eine Grafik auf dem HUD anzeigen zu können, erstellen wir zuerst eine neue Klasse, welche das Interface `IHUDElement` implementiert.

```java
public class MyIcon implements IHUDElement {
    private String texture;
    private Painter painter;
    private Point position;

    public MyIcon(Painter painter, Point position, String texture){
        this.painter=painter;
        this.position=position;
        this.texture=texture;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public String getTexture() {
        return texture;
    }

    @Override
    public Painter getPainter() {
        return painter;
    }
}
```

Die Methode `getTexture` gibt den Pfad zu der gewünschten Textur zurück, dies funktioniert identisch zur bereits bekannten Helden-Implementierung. Die Methode `getPosition` gibt die Position der Grafik auf dem HUD zurück. Es wird vorkommen, dass Sie Grafiken in Abhängigkeit zu anderen Grafiken positionieren möchten, überlegen Sie sich daher bereits jetzt eine gute Struktur, um Ihre HUD-Elemente abzuspeichern.

Jetzt müssen wir unsere Grafik nur noch anzeigen lassen. Ähnlich zu den bereits bekannten Controllern gibt es auch für das HUD eine Steuerungsklasse, welche im `MainController` mit `hud` angesprochen werden kann.

```
public class YourClass extends MainController {
     @Override
    protected void setup() {
        ...
        // hinzufügen eines Elementes zum HUD
        hud.add(new MyIcon(painter,new Point(0,0),"TEXTURE"));
        //so entfernt man ein Element
        //hud.remove(OBJECT);
    }
}
```

## Abschlussworte

Sie haben nun die ersten Schritte im Dungeon gemacht. Von nun an müssen Sie selbst entscheiden, wie Sie die Aufgaben im Praktikum umsetzten möchten. Ihnen ist mit Sicherheit aufgefallen, dass einige Interface-Methoden in diesem Dokument noch nicht erläutert wurden. Machen Sie sich daher mit der Javadoc des Frameworks vertraut.

## Zusätzliche Funktionen

### Sound

Möchten Sie Soundeffekte oder Hintergrundmusik zu Ihrem Dungeon hinzufügen, bietet `libGDX` eine einfache Möglichkeit dafür.

Es werden die Formate `.mp3`, `.wav` und `.ogg` unterstützt. Das Vorgehen unterscheidet sich zwischen den Formaten nicht.

```Java
//Datei als Sound-Objekt einladen
Sound bumSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound/bum.mp3"));
bumSound.play();
//Sound leise abspielen
bumSound.play(0.1f);
//Sound mit maximal Lautstärke abspielen
bumSound.play(1f);
//Soud endlos abspielen
bumSound.loop();
```

Sie können noch weitere Parameter und Methoden verwenden, um den Sound Ihren Wünschen anzupassen. Schauen Sie dafür in die [`libGDX`-Dokumentation](https://libgdx.badlogicgames.com/ci/nightlies/docs/api/com/badlogic/gdx/audio/Sound.html)

### Text

Verwenden Sie die Methode `HUDController#drawText`, um einen String auf Ihren Bildschirm zu zeichnen. Sie haben dabei eine umfangreiche Auswahl an Parametern, um Ihre Einstellungen anzupassen.`HUDController#drawText` gibt Ihnen ein `Label`-Objekt zurück, dieses können Sie verwenden, um den Text später anzupassen oder ihn vom Bildschirm zu entfernen. Um den Text anzupassen, können Sie `label.setText("new String")` verwenden und um das Label zu löschen, können Sie `label.remove()` verwenden.

Im unteren Beispiel wird ein Text implementiert, welcher das aktuelle Level ausgibt.

```
public class MyGame extends MainController {
    .....
    Label levelLabel;
    int levelCounter=0;
	
    public void onLevelLoad() {
        levelCounter++;
        if (levelCounter==1){
            levelLabel=hud.drawText("Level"+x,"PATH/TO/FONT.ttf",Color.RED,30,50,50,30,30);
        }
        else{
            levelLabel.setText("Level"+x);
        }
    }
    //remove label
    //levelLabel.remove();
}
```

Genauere Informationen zu den Parametern entnehmen Sie bitte der JavaDoc.

### Levelgeneratoren wechseln

Das Framework verfügt über verschiedene Levelgeneratoren zwischen denen Sie wechseln können.

1. `LevelG` : Wird per default verwendet und generiert beim Aufruf von `levelAPI.loadLevel()` "zufällig" ein neues Level. 
2. `LevelLoader`: Lädt Level aus der `level.json` ein. Diese Level wurden von `LevelG` generiert und abgespeichert. Diese Variante ist deutlich Rechenzeiteffektiver. 
3. `DummyGenerator`: Lädt ein statisch gecodedetes Level. Kann für Experimente genutzt werden. 

Um den Levelgenerator zu ändern, rufen Sie einfach `levelAPI.setGenerator(new {GENERATOR_TYPE})` auf. Ersetzten Sie `{GENERATOR_TYPE}` mit dem gewünschten Generatortypen. Unter umständen müssen Sie noch Parameter übergeben. Weitere Informationen finden Sie in der jeweiligen javadoc. 

### Level 

- tbd

