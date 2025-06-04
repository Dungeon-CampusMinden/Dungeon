# VS Code Befehls-Cheat Sheet für Java Dungeon

## Befehle für den Helden (hero)

| Befehl                                             | Beschreibung                                                                                                       |
| -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| `hero.move();`                                     | Bewegt den Helden einen Schritt vorwärts.                                                                          |
| `hero.rotate(direction);`                          | Dreht den Helden in die angegebene Richtung.                                                                       |
| `hero.shootFireball();`                            | Schießt einen Feuerball ab. Benötigt Munition (Schriftrolle im Spiel sammeln).                                     |
| `hero.dropItem("Brotkrumen");`                     | Lässt ein Item („Brotkrumen“) fallen.                                                                              |
| `hero.dropItem("Kleeblatt");`                      | Lässt ein Item („Kleeblatt“) fallen.                                                                               |
| `hero.pickup();`                                   | Nimmt ein Item vom Boden auf.                                                                                      |
| `hero.isNearTile(tileType, direction);`            | Prüft, ob ein bestimmtes Levelelement-Objekt in der angegebenen Richtung in der Nähe (ein Feld vor dem Helden) ist. |
| `hero.active(direction);`                          | Prüft, ob das Objekt in der gegebenen Richtung aktiv ist (z. B. Tür ist offen, Schalter ist aktiviert).            |
| `hero.interact();`                                 | Interagiert mit einem Objekt vor dem Helden.                                                                       |
| `hero.isNearComponent(componentClass, direction);` | Prüft, ob ein bestimmtes Komponenten-Objekt in der angegebenen Richtung in der Nähe (ein Feld vor dem Helden) ist. |
| `hero.pull();`                                     | Zieht ein ziehbares Objekt vor dem Helden ein Feld zurück; bewegt auch den Helden ein Feld zurück.                 |
| `hero.push();`                                     | Schiebt ein schiebbares Objekt vor dem Helden ein Feld vor; bewegt auch den Helden ein Feld vor.                   |
| `hero.moveToExit();`                               | **Nur als Cheat-Block:** Bewegt den Helden direkt zum Ausgang. Nicht für Lösungen verwenden.                       |

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
```
