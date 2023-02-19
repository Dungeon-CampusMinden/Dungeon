---
title: "Eigene Inhalte erstellen"
---


## Entitäten erstellen

Entitäten werden nicht durch das Ableiten der Klasse `Entity` definiert, sondern durch das Instanziieren von `Entity` und durch das Hinzufügen von Komponenten zum Objekt.

Um eine eigene Entität zu erstellen, muss man:

1. Eine neue Instanz vom Typ `Entity` anlegen
2. Die gewünschten Komponenten erstellen
3. Die Komponenten dem Objekt mit `Entity#addComponent` hinzufügen

Beispiel: 
```java
Entity monster = new Entity();

PositionComponent pc = new PositionComponent(monster);
AIComponent ai = new AIComponent(monster);
AnimationComponent ac = new AnimationComponent(monster);
VelocityComponent vc = new AnimationComponent(monster);

monster.addComponent(PositionComponent.name, pc);
monster.addComponent(AIComponent.name, ai);
monster.addComponent(AnimationComponent.name, ac);
monster.addComponent(VelocityComponent.name, vc);
```

*Anmerkung*: Die Komponenten wurden im Beispiel alle mit den jeweiligen Default-Werten initialisiert. Die Verwendung der anderen Konstruktoren (mit eigenen Werten) geht natürlich auch.

## Component erstellen

Um eigene Components zu implementieren, muss Spezialisierung der abstrakten Klasse  `Component` erstellt werden.

Components werden im package `ecs.components` abgelegt und sollen den Namensschema `$WHAT_IS_THIS_COMPONENT$Component`folgen.
Jedes Component braucht ein statisches Attribut `String name`, um den Component-Typen zu identifizieren. Dieser Name wird als Key-Value des HashSets für die in den Entitäten gespeicherten Components verwendet. 

Jede Component-Instanz gehört zu genau einer Entitäs-Instanz. Eine Entitäts-Instanz kann einen Component-Typen nur einmal speichern.

Um Components für die DSL verfügbar zu machen, siehe TBD

## System erstellen

Um eigene Systeme zu implementieren, muss eine Spezialisierung von `ECS_System` erstellt werden.

Die Funktionalität des Systems wird in der `update`-Methode implementiert. Diese wird einmal pro Frame aufgerufen.

In der `update`-Methode wird dann über das HashSet `ECS.entities`iteriert.
Dabei muss zunächst geprüft werden, ob die Entität die gewünschte Component(s) enthält. Dazu wird die `getComponent`-Methode mit dem Namens-Attribut der gesuchten Component aufgerufen. Wenn ein System mehrere Components bearbeiten soll, dann muss dieser Aufruf pro Entität für jede Component wiederholt werden.
Beispiel aus dem `VelocitySystem`: 
```java
 for (Entity entity : ECS.entities) {
            entity.getComponent(VelocityComponent.name)
                    .ifPresent(
                            vc -> {
                                //YOUR CODE
                            });
 } 
```

Für den Fall, dass mehrere Components benötigt werden, kann die Abfrage auch verschachtelt werden. Hierbei sollte dennoch zuerst das "Key"-Component überprüft werden.
Je nach Situation kann es sein, dass Components aufeinander aufbauen. 
Das `VelocityComponent` ist nutzlos ohne das `PositionComponent`. 
Im `VelocitySystem` wird daher vorausgesetzt, dass eine Entität, die das `VelocityComponent` hält, auch ein `PositionComponent` hält, ansonsten wird eine `MissingComponentException` geworfen.

Beispiel aus dem `VelocitySystem`:
```java
 for (Entity entity : ECS.entities) {
            entity.getComponent(VelocityComponent.name)
                    .ifPresent(
                            vc -> {
                                final PositionComponent position =
                                        (PositionComponent)
                                                entity.getComponent(PositionComponent.name)
                                                        .orElseThrow(
                                                                () ->
                                                                        new MissingComponentException(
                                                                                "PositionComponent"));
                                                                                    //YOUR CODE
                            });
```


Systeme werden im Package `ecs.systems` abgelegt und sollen den Namensschema `$WHAT_IS_THIS_SYSTEM$System`folgen.
Jedes System muss im Konstruktor `super()` aufrufen, um die Registrierung des Systems im `SystemController` zu gewährleisten. 