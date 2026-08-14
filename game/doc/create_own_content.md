---
title: "Eigene Inhalte erstellen"
---

## Entitäten erstellen

Entitäten werden nicht durch das Ableiten der Klasse `Entity` erzeugt, sondern durch das Instanziieren von `Entity` und durch das Hinzufügen von Komponenten zum Objekt. Eine Entitätsinstanz kann einen Komponententypen nur einmal speichern. Um eine eigene Entität zu erstellen, muss man:

1. Eine neue Instanz vom Typ `Entity` anlegen
2. Die gewünschten Komponenten erstellen und hinzufügen
3. Die Entität im Spiel registrieren

Beispiel:
```java
Entity monster = new Entity("My Monster");
monster.add(new PositionComponent());
monster.add(new DrawComponent());
monster.add(new VelocityComponent());
Game.add(monster);
```

*Anmerkung*: Die Komponenten wurden im Beispiel alle mit den jeweiligen Standardwerten initialisiert. Die Verwendung der anderen Konstruktoren (mit eigenen Werten) geht natürlich auch.

*Hinweis: Um Entitäten aus dem Spiel zu entfernen, nutzen Sie die Methode `Game#remove`.*

## Komponente erstellen

Um eigene Komponenten zu implementieren, muss eine Implementierung des Interface  `Component` erstellt werden.

```java
class MyComponent implements Component {
    // meine Inhalte
}
```

## System erstellen

Um eigene Systeme zu implementieren, muss eine Spezialisierung von `System` erstellt werden. Im Konstruktor wird mithilfe des `super`-Konstruktors festgelegt, welche Komponenten das System benötigt.

```java
class MySystem extends System {
    public MySystem() {
        super(MyComponent.class, MySecondComponent.class);
    }
}
```

Die Funktionalität des Systems wird in der `execute`-Methode implementiert. Diese wird einmal pro Frame aufgerufen. In der `execute`-Methode kann mit `entityStream()` der Stream aus `Game` geholt werden, der alle Entitäten enthält, die von dem System verwaltet werden.

```java
@Override
public void execute() {
    entityStream().forEach(this::myLogic);
}

private void myLogic(Entity e) {
    System.out.println(e);
}
```

Das System muss jetzt noch in `Game` registriert werden.

```java
Game.add(new MySystem());
```

Systeme werden in der Reihenfolge ausgeführt, in der sie im Spiel registriert sind.
