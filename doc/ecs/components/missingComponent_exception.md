---
title: "MissingComponentException"
---

## Wofür

Die `MissingComponentException` wird geworfen, wenn ein `Component` nicht gefunden wurde, es jedoch für eine bestimmte
Operation benötigt wird.

## Erzeugen

Um ein `MissingComponentException` zu erzeugen kann der Konstruktor `MissingComponentException(String message)` verwendet
werden. Die Nachricht sollte eine Beschreibung enthalten, wo das gesuchte `Component` fehlt.

## Beispiel

```java
public class Example {

    public void needsPositionComponent() {
        PositionComponent positionComponent =
                entity.getComponent(PositionComponent.class)
                        .map(PositionComponent.class::cast)
                        .orElseThrow(
                                () ->
                                        createMissingComponentException(
                                                PositionComponent.class.getName(), entity));
    }

    private static MissingComponentException createMissingComponentException(String Component, Entity e) {
        return new MissingComponentException(
                Component
                        + " missing in "
                        + DropItemsInteraction.class.getName()
                        + " in Entity "
                        + e.getClass().getName());
    }
}
```

In der Methode `needsPositionComponent` wird ein `PositionComponent` benötigt, um die Position einer Entität zu erhalten.
Wenn das `PositionComponent` nicht existiert, wird die statische Methode `createMissingComponentException` aufgerufen, welche
eine `MissingComponentException` erzeugt. Diese wird dann durch die Methode `orElseThrow` von `Optional` geworfen.
