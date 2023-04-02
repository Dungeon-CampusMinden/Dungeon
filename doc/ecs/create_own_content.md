---
title: "Eigene Inhalte erstellen"
---

## Entitäten erstellen

Entitäten werden nicht durch das Ableiten der Klasse `Entity` erzeugt, sondern durch das Instanziieren von `Entity` und durch das Hinzufügen von Komponenten zum Objekt.

Um eine eigene Entität zu erstellen, muss man:

1. Eine neue Instanz vom Typ `Entity` anlegen
2. Die gewünschten Komponenten erstellen

Beispiel:
```java
Entity monster = new Entity();

PositionComponent pc = new PositionComponent(monster);
AIComponent ai = new AIComponent(monster);
AnimationComponent ac = new AnimationComponent(monster);
VelocityComponent vc = new AnimationComponent(monster);
```

*Anmerkung*: Die Komponenten wurden im Beispiel alle mit den jeweiligen Default-Werten initialisiert. Die Verwendung der anderen Konstruktoren (mit eigenen Werten) geht natürlich auch.

*Hinweis: Um Entitäten aus dem Spiel zu entfernen, nutzen Sie die Methode `Game#removeEntity`.*

## Component erstellen

Um eigene Components zu implementieren, muss Spezialisierung der abstrakten Klasse  `Component` erstellt werden.

Components werden im package `ecs.components` abgelegt und sollen den Namensschema `$WHAT_IS_THIS_COMPONENT$Component`folgen.

Jede Component-Instanz gehört zu genau einer Entitäs-Instanz. Eine Entitäts-Instanz kann einen Component-Typen nur einmal speichern.

Um Components für die DSL verfügbar zu machen, siehe **TBD**

## System erstellen

Um eigene Systeme zu implementieren, muss eine Spezialisierung von `ECS_System` erstellt werden.
Systeme werden im package `ecs.systems` abgelegt und sollen den Namensschema `$WHAT_IS_THIS_$System`folgen.


Die Funktionalität des Systems wird in der `update`-Methode implementiert. Diese wird einmal pro Frame aufgerufen.
In der `update`-Methode wird dann über die Collectiom `Game.entities`iteriert, alle Entitäten mit dem Key-Component gefiltert und dann die eigentliche System-Logik auf die Entiäten angewendet.

Beispiel aus dem `HealthSystem`:
```java

 // private record to hold all data during streaming
private record HSData(Entity e, HealthComponent hc, AnimationComponent ac) {}


 @Override
public void update() {
        Game.getEntities().stream()
                // Consider only entities that have a HealthComponent
                .flatMap(e -> e.getComponent(HealthComponent.class).stream())
                // Form triples (e, hc, ac)
                .map(hc -> buildDataObject((HealthComponent) hc))
                // Apply damage
                .map(this::applyDamage)
                // Filter all dead entities
                .filter(hsd -> hsd.hc.getCurrentHealthpoints() <= 0)
                // Remove all dead entities
                .forEach(this::removeDeadEntities);
    }

private HSData buildDataObject(HealthComponent hc) {
        Entity e = hc.getEntity();

        AnimationComponent ac =
                (AnimationComponent)
                        e.getComponent(AnimationComponent.class)
                                .orElseThrow(HealthSystem::missingAC);
                return new HSData(e, hc, ac);
        }

private HSData applyDamage(HSData hsd) {...}
private void removeDeadEntities(HSData hsd){...}

```
