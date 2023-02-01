## Entitäten erstellen

Anders als im klassischen OOP werden Entitäten nicht durch das ableiten der `Entity` Klasse erzeugt und definiert, sondern durch das hinzufügen von Components.

Um eine eigene Entität zu erstellen muss man:

1. Eine neue Instanz vom Typ `Entity` anlegen
2. Die gewünschten Komponenten erstellen
3. Die Komponenten mit `Entity#addComponent` hinzfügen

Beispiel: 
```java
Entity monster = new Entity();
PositionComponent pc= new PositionComponent(monster);
AIComponent ai = new AIComponent(monster);
AnimationComponent ac = new AnimationComponent(monster);
VelocityComponent vc = new AnimationComponent(monster);

monster.addComponent(PositionComponent.name,pc);
monster.addComponent(AIComponent.name,ai);
monster.addComponent(AnimationComponent.name,ac);
monster.addComponent(VelocityComponent.name,vc);
```

*Anmerkung: Die Komponenten wurden im Beispiel alle mit den jeweiligen default-Werten initialisiert. Die Verwendung der anderen Konstruktoren, mit eigenen Werten, geht natürlich auch.* 


## Component erstellen
Um eigene Components zu implementieren muss die eigenen Klasse von der abstrakten Klasse  `Component` abgeleitet werden.

Components werden im package `ecs.components` abgelegt und sollen den Namenshema `$WHAT_IS_THIS_COMPONENT$Component`folgen.
Jedes Component braucht eine statische Variable `String name`, um den Component-Typen zu identifzieren. Dieser Name wird als Key-Value des HashSets für die, in den Entitäten gespeicherten, Components verwendet. 

Jede Component-Instanz gehört zu genau einer Entitäs-Instanz. Eine Entitäts-Instanz kann einen Component-Typen nur einmal speichern.

Um Components für die DSL verfügbar zu machen, siehe TBD



## System erstellen
Um eigenen Systeme zu implementieren muss die eigene Klasse von `ECS_System` abgeleitet werden.

Die Funktionalität des Systems wird in der `update`-Methode implementiert. Diese wird einmal pro Frame aufgerufen.

In der `update`-Methode wird dann über das HashSet `ECS.entities`iteriert.
Dabei sollte zuerst geprüft werden, ob die Entiät das "Key"-Component enthällt. 
Das "Key"-Component ist das Component, welches dem System sagen soll, dass die Entität unter den System fällt, die Logik des Systems also auf die Entiät angewendet werden soll.
Beispiel: Das `VelocitySystem` soll nur auf Entiäten mit dem `VelocityComponent` agieren.

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
Im `VelocitySystem` wird daher vorrausgesetzt, dass eine Entität die das `VelocityComponent` hält, auch ein `PositionComponent` hält, ansonsten wird eine `MissingComponentException` geworfen. 

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


Systeme werden im package `ecs.systems` abgelegt und sollen den Namenshema `$WHAT_IS_THIS_SYSTEM$System`folgen.
Jedes System muss im Konstruktor `super()` aufrufen, um die Registrierung des Systems im `SystemController` zu gewährleisten. 