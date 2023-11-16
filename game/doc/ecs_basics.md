---
title: "ECS Basics"
---


Im Projekt wird das [ECS-Paradigma](https://en.wikipedia.org/wiki/Entity_component_system) angewendet.

## Was ist ein ECS (Kurzform)

**Entität**
Entitäten sind die Objekte im Spiel. Im Code sind sie nur leere Container, dessen Eigenschaften über die zugewiesenen Components bestimmt werden. Entitäten haben neben den Components keine eigenen Attribute oder Funktionen.

**Component**
Components sind die Datensätze der Entitäten und beschreiben dadurch die Eigenschaften der Entitäten.

Components speichern im Regelfall nur die Daten/den Zustand einer Entität.
In Ausnahmefällen kann es erforderlich sein, zusätzliche Logik in Components zu implementieren. Der Umfang sollte in diesen Fällen möglichst klein gehalten werden, da die Logik normalerweise in den Systemen realisiert wird. Wir verwenden hierfür Funktionale-Interfaces wie `Consumer` oder `Function`. 

**System**
Systeme agieren auf den Components und ändern die Werte in diesen. Sie beschreiben also das Verhalten der Entitäten. Ein System kann auf ein oder mehreren Components agieren.
In Systemen wird die eigentliche Logik implementiert.

Der Zustand einer Entität wird also über ihre Components bestimmt, und ihr Verhalten über die Systeme, die mit der jeweiligen Component-Kombination arbeiten.

## Basisstruktur

![Struktur ECS](./img/ecs.png)

*Anmerkung:* Das UML ist für bessere Lesbarkeit auf die wesentlichen Bestandteile gekürzt.

Die in Grün gekennzeichnete Klasse `Game` ist die Basisklasse, von der alles ausgeht.

*Anmerkung*: In der realtität ist die Klasse `Game` nur ein Sammelpunkt und implementiert selbst kaum Logik. Die Klasse dient als zentralen Steuerrungspunkt für das Framework, leitet die Anfragen aber nur an die jeweiligen zuständigek Klassen weiter. Für eine bessere verständlichkeit sprechen wir dennoch von der Klasse `Game` als operative Klasse. 

Die Methode `GameLoop#render` ist die Game-Loop. Das ECS wird durch die in weiß gekennzeichneten Klassen `Entity`, `Component` und `System` implementiert.

Die LevelAPI generiert, zeichnet und speichert das aktuelle [Level](./level/readme.md). Klassen, die rot gekennzeichnet sind, gehören dazu.

Neu erzeugte Entitäten speichern werden im Game registiert und im [`SystemEntityMapper`](./system_entity_mapper.md) hintelegt. 

Die Systeme registirieren sich in `Game` und geben dabei an, auf welche Entitäten sie agieren wollen, heißt: Welche Components eine Entität implementieren muss, um vom System bearbeitet zu werden. Die Systeme iterieren über die in `Game` gespeicherten Entitäten und greifen über die Methode `Entity#fetch` auf die für die jeweilige Funktionalität benötigten Components zu. Die orangefarbenen `System`s und `Controller` sind in dem UML-Diagramm Beispiele für die bereits bestehenden `System`s und `Controller`.
Systemlogiken werden einmal pro Frame ausgeführt.