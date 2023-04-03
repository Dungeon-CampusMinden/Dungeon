---
title: "documentation basics"
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
Dieses ist im `Dungeon`-Projekt bereits als Abhängigkeit in die Gradle-Konfiguration integriert, Sie müssen dieses nicht extra installieren. Die Ihnen zur Verfügung gestellten Vorgaben sind so umgesetzt, dass Sie kein tieferes Verständnis für `libGDX` benötigen, um die Aufgaben zu lösen. Sollten Sie allerdings einmal auf Probleme stoßen, kann es unter Umständen helfen, einen Blick in die [Dokumentation von `libGDX`](https://libgdx.com/wiki/) zu werfen.

Das `Dungeon`-Projekt fungiert, ganz vereinfacht gesagt, als eine Facade zwischen `libGDX` und Ihrer eigenen Implementierung. Es implementiert ein Entity-Component-System (ECS):
- Entity: Entitites sind die Elemente (Helden, Monster, Schatzkisten, etc.) des Spiels
- Component: Components speichern die Datensätze der Entitäten (z.B. die Lebenspunkte)
- System: Systeme beinhalten die eigentliche Logik und agieren auf die Components

*Weiteres zum ECS im Dungeon erfahren Sie [hier](ecs/ecs_basics.md)*.

Sie selbst nutzen und erweitern die Components und Systeme der Vorgaben. Sie werden ebenfalls neue Entities, Components und Systeme konzeptionieren und implementieren. So erschaffen Sie z.B. Ihre eigenen Monster und fallengespickte Level.

Sie werden im Laufe der Praktika verschiedene Assets benötigen. Diese liegen per Default im `assets`-Verzeichnis. Sie können das Standardverzeichnis in der `build.gradle` anpassen.

- Standardpfad für Texturen: `assets/`
- Standardpfad für Charaktere: `assets/character/`
- Standardpfad für Level-Texturen: `assets/textures/dungeon/`

## Strukturen

In diesem Abschnitt werden Ihnen die wichtigsten Klassen im Dungeon vorgestellt.

![Struktur ECS](ecs/img/ecs.png)

Game ist die Basisklasse von der alles ausgeht. Die Methode `Game#render` ist die Game-Loop. Die Klassen `Entity`, `Component` und `ECS_System` sind die Implementierungen des ECS.

Die LevelAPI generiert, zeichnet und speichert das aktuelle Level. Mehr zum Thema Level erfahren Sie [hier](../level/readme.md).

*Anmerkung: Das UML ist für bessere Lesbarkeit gekürzt.*

*Anmerkung: Die in Gelb hinterlegten Klassen des UML-Diagramms sind für ein Basisverständnis des Dungeons nicht nötig.*

- `entity` Die Java-Implementierung der Entitäten eines ECS
- `component` Abstrakte Klasse, jedes Component im ECS leitet hiervon ab
- `ECS-Systems` Abstrakte Klasse, jedes System im ECS leitet hiervon ab
- LevelAPI: Kümmert sich darum, dass neue Level erzeugt und geladen werden
- DungeonCamera: Ihr Auge in das Dungeon
- libGDXSetup: Bereitet die Anwendung vor, für die Verwendung des Dungeons ist Verständnis für die genaue Funktionalität nicht notwendig
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
- alle bereits implementierten Components finden Sie [hier](/ecs/components/readme.md)
- um eigene Components zu schreiben, leiten Sie von der eigentlichen Component Klasse ab
- siehe [create_own_content.md](ecs/create_own_content.md)

### Entity

- Entitäten sind leere Container und speichern Components
- um ein Component einer Entität zu erhalten, benutzen Sie die Methode `entity#getComponent(component.class klass)`
    - Anmerkung: Sie erhalten ein `Optional zurück`
- siehe [create_own_content.md](doc/ecs/create_own_content.md)

### System

- um eigene Components zu schreiben, leiten Sie von der ECS_Systems Klasse ab
- in der `ECS_System#update()` iterieren die Systeme über alle Entitäten mit bestimmten Components und agieren darauf
- alle bereits implementierten Systeme finden Sie [hier](ecs/systems/readme.md)
- siehe [create_own_content.md](ecs/create_own_content.md)


## Übung: Eigenen Helden erstellen

Zwar gibt es in den Vorlagen bereits einen Helden (den schauen wir uns am Ende dieses Kapitels genauer an), trotzdem wird Ihnen hier erklärtm wie Sie Ihre erste eigene Entität in das Spiel implementieren.
*Hinweis: @cagix @AMatutat der bestehende Hero lässt aufgrund besteheder Abhängigkeiten nicht einfach löschen, daher ist nachprogrammieren in diesem Abschnitt schwierig*

### Held Entität erstellen

- Warum muss das eigentlich ne Entität sein? 
- - Held ist eine Entität, hat aber eine eigene Klasse unter `ecs/components/entities`
	- vergleiche Abschnitt `Eigene Klasse für Hero` unter [structure_design_desicions.md](ecs/structure_design_decisions.md)

- Entität `myHero` in `Game#onLevelLoad` anlegen
    - Warum in onLevelLoad?
- erklären wie der in der Game-Loop ist (entitiesToAdd)


### Held zeichnen

![Animation](figs/animation.gif)

- positinComponent erstellen
    - Wofür ist das da
    - welche werte sind sinnvoll
- AnimationCompoent erstellen
    - Wofür ist das da
    - wie wird eine Animation erstellt

- DrawSystem [Link zum Code](https://github.com/Programmiermethoden/Dungeon/blob/master/game/src/ecs/systems/DrawSystem.java)

### Held bewegen

![Bewegen](figs/move.gif)

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

### Nächstes Level laden

![Next-Level](figs/next-level.gif)

- onLevelLoad erklären

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
           

## Linksammlung

Hier finden Sie noch einige Links um Ihr Verständnis des Dungeons zu vertiefen:

- [ECS Basics](ecs/ecs_basics.md)
- [Eigener Inhalt](ecs/create_own_content.md)
- [Übersicht der Systeme](ecs/systems/readme.md)
- [Übersicht der Components](ecs/components/readme.md)
- [Übersicht der Spielelemente](ecs/gameelements/)
- [Level Basics](level/readme.md)
- [HUD Basics](hud/readme.md)
- [Config](configuration/readme.md)
- [Dungeon-Wiki](https://github.com/Programmiermethoden/Dungeon/wiki)
- [LibGDX Dokumentation]((https://libgdx.com/wiki/))