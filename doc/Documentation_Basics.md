---
title: "Documentation_Basics"
---

## Installation



- Java SE Development Kit 17.x.x oder höher benötigt
- in IDE einbinden
- libGDX wird verwendet, ist aber schon eingebunden und muss nicht extra installiert werden

- bei Problemen: [FAQ](https://github.com/Programmiermethoden/Dungeon/wiki/FAQ)

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
- was ist das
- wie benutz ich das
- Unterschied `Point` und `Coordinate` erklären
- `onLevelLoad` erklären
- Wie krieg ich denn so ein Bodenteile um mein Monster drauf zu setzten? 
- link detailiertes Level gedöns

### HUD
- Wie erstelle ich Elemente für das HUD
- Text, Bild, Button

##Erstellen weiteren Contents

- siehe [create_own_content.md](https://github.com/Programmiermethoden/Dungeon/blob/master/doc/ecs/create_own_content.md)
