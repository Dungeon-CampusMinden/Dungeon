---
title: "Documentation_Basics"
---

##Installation
- Java SE Development Kit 17.x.x oder höher benötigt
- in IDE einbinden
- libGDX wird verwendet, ist aber schon eingebunden und muss nicht extra installiert werden

#### kurzer Einschub: was ist ein ECS?
- siehe [readme.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/readme.md)

##Struktur des Repos

Relevante Klassen zum Einstieg:
Package "starter"
Game.java (Haupt-Steuerung des Dungeon)
- beinhaltet main
- verwaltet die anderen Controller und Systeme
- alle Systeme befinden sich im „package ecs→ systems“
	VelocitySystem -> aktualisiert Position von Entitäten 
	DrawSystem -> zum zeichnen von Objekten
	PlayerSystem -> zur Steurung der Figur
	AISystem  -> AI steurung
	CollisionSystem  -> Kollisions Prüfung auf zwischen zwei Entitäten
	HealthSystem -> Schaden, den Entität erleidet

wichtige Methoden:
setup(): 
- Aufruf beim Anwendungsstart
- initialisirung und Konfigurierung Objekte, die vor dem Spielstart existieren müssen (z.B Systeme,  Controller, Spielfigur)
- levelAPI Erstellung und laden der neuen Level

frame() 
- Wird zu Beginn eines jeden Frame aufgerufen

render()
- Game-Loop des Spiels. Zeichnet den Dungeon neu und ruft die eigene Implementierung auf.

onLevelLoad()
- Aufruf, wenn das Laden eines neuen Levels erfolgt

Class DesktopLauncher:
- erstellt das Hauptfenster des Spiels mit bestimmten Maßen (maxWidth" und "maxHeight Beeinflussen die Größe) inklusive Logo  und Spieltitel

Class LibgedxSetup:
 create()
- mithilfe der SpriteBatch werden alle Sprites (individuelle Zeichnungen) in einzigen Zeichenaufruf gebündelt

--------------------------------------------------------------
Package "level"
class LevelAPI 
- Level Erstellung und Verwaltung
- lädt neues Level mit loadLevel()
- update() zeichnet neues Level

--------------------------------------------------------------
Package "graphic"
Class Animation:
- Erstellung und Verwaltung von Animationen

getNextAnimationTexturePath()
- aktualisieren des aktuellen Frames (nächstes Bild) und Rückgabe der Textur des nächsten Animationsschrittes

getAnimationFrames()
- Abruf der Liste mit Sprites und Rückgabe der Pfade der einzelnen Sprites 

Class DungeonCamera:
- ermöglicht Blick in den Dungeon

--------------------------------------------------------------
Package "ecs→components"

Class AnimationComponent:
- speichert die möglichen Animationen und aktuelle Animation einer Entität
- Grundaktivitäten zum setzen und ausführen der Animation 

Class HitboxComponent:
- definiert Hitbox und legt dessen Größe Fest
onEnter()
- ermittelt Aktivitäten bei der Kollision
onLeave()
- ermittelt Aktivitäten die mit Entitäten nach ende der Kollision passieren

Class  InteractionComponent:
- Radius für Interaktionen zwischen Entitäten
triggerInteraction()
- Auslösung Interaktion zwischen Held(Hero) und der Entität der Komponente aus

Class PositionComponent:
- Speichert Position einer Entität in einem 2D Punkt (x, y koordinaten)

Class VelocityComponent:
- Bewegungsrichtung und Bewegungsgeschwindigkeit über die Achsen x und y

--------------------------------------------------------------

Package "ecs→entities"
Class Hero
erhält PositionComponent (startposition des Helden in Spielwelt), PlayableComponent (markiert Entität als Spieler)und HitboxComponent
- Erstellung Helden mit bestimmten Skills
setupAnimationComponent() 
- erstellt Animation aus einzelnen Bildern des Sprite sheets
heroCollision()
- Kollisionsbeschreibung
 
Package "ecs→systems"
Class CollisionSystem:
- Prüft Kollision zwischen Entitäten und die Richtung der Kollision 
checkDirectionOfCollision()

Class DrawSystem:
- zum zeichnen von Entitäten
draw()
- Darstellung Entitaten an ihrer Position

Class ECS_System:
- markiert ob Systeme benutzt werden
 isRunning() und toggleRun()

Class PlayerSystem:
- Spieler Steuerung
checkKeystroke(KSData ksd)
-Überprüfung welche Tasten gedrückt wurden (bewegung und einsatz der Skills)

Class  VelocitySystem:
- Update der Position und Animation
updatePosition()
- berechnet neue Position und verlegt die Animation auf den neuen Punkt

-----------------------------------------
Package ecs→ tools.interaction

Class ControlPointReachable:
checkReachable()
- prüft ob ein Bewegungspfad blockiert ist

----------------------------------------------
Package graphic→ hud
- Enthält sämtliche Widgets (z.B ScreenButton PauseMenu) und Builder z.B(FontBuilder,
LabelStyleBuilder)

##Erster Start
- Vorgabe ist luffähig und kann über die run-Funktion der IDE gestartet werden
- Alternativ per Konsole in das Dungeon-Verzeichnes wechseln und `./gradlew run` ausführen

##Eigenen Helden erstellen
- Held ist eine Entität, hat aber eine eigene Klasse unter `ecs/components/entities`
	- vergleiche Abschnitt `Eigene Klasse für Hero` unter [structure_design_desicions.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/structure_design_decisions.md)

##Held steuern
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

##Erstellen weiteren Contents

- siehe [create_own_content.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/create_own_content.md)
