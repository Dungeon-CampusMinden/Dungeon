# VS Code Befehls-Cheat Sheet für Java Dungeon

## Befehle für den Helden (hero)

| Befehl                                             | Beschreibung                                                                                                       |
|----------------------------------------------------| ------------------------------------------------------------------------------------------------------------------ |
| `hero.move();`                                     | Bewegt den Helden einen Schritt vorwärts.                                                                          |
| `hero.rotate(direction);`                          | Dreht den Helden in die angegebene Richtung.                                                                       |
| `hero.interact(direction);`                        | Interagiert mit einem Objekt in der angegebenen Richtung des Helden.                                               |
| `hero.pull();`                                     | Zieht ein ziehbares Objekt vor dem Helden ein Feld zurück; bewegt auch den Helden ein Feld zurück.                 |
| `hero.push();`                                     | Schiebt ein schiebbares Objekt vor dem Helden ein Feld vor; bewegt auch den Helden ein Feld vor.                   |
| `hero.pickup();`                                   | Nimmt ein Item vom Boden auf.                                                                                      |
| `hero.shootFireball();`                            | Schießt einen Feuerball ab. Benötigt Munition (Schriftrolle im Spiel sammeln).                                     |
| `hero.active(direction);`                          | Prüft, ob das Objekt in der gegebenen Richtung aktiv ist (z. B. Tür ist offen, Schalter ist aktiviert).            |
| `hero.isNearTile(tileType, direction);`            | Prüft, ob ein bestimmtes Levelelement-Objekt in der angegebenen Richtung in der Nähe (ein Feld vor dem Helden) ist.|
| `hero.isNearComponent(componentClass, direction);` | Prüft, ob ein bestimmtes Komponenten-Objekt in der angegebenen Richtung in der Nähe (ein Feld vor dem Helden) ist. |
| `hero.dropItem("Brotkrumen");`                     | Lässt ein Item („Brotkrumen“) fallen.                                                                              |
| `hero.dropItem("Kleeblatt");`                      | Lässt ein Item („Kleeblatt“) fallen.                                                                               |
| `hero.rest();`                                     | Macht einen kurzen moment nichts.                                                                                  |
| `hero.checkBossViewDirection(direction)`           | **Nur für Level 20:** Prüfe in welche Richtung der Boss guckt.                                                     |
| `hero.moveToExit();`                               | **Nur als Cheat-Block:** Bewegt den Helden direkt zum Ausgang. Nicht für Lösungen verwenden.                       |

<div style="page-break-after: always;"></div>

## Mögliche Richtungen (direction)

```java
Direction.DOWN
Direction.HERE
Direction.LEFT
Direction.RIGHT
Direction.UP
```

## Mögliche Level-Elemente (LevelElement)

```java
LevelElement.FLOOR
LevelElement.EXIT
LevelElement.WALL
LevelElement.PIT
LevelElement.SKIP
LevelElement.HOLE
```

## Mögliche Komponenten-Klassen (Component.class)

```java
AIComponent.class
BreadcrumbComponent.class
CloverComponent.class
PushableComponent.class
LeverComponent.class
```
