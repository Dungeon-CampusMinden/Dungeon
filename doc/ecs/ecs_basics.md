## Was ist ein ECS (Kurzform)

**Entität**
Entitäten sind die Objekte im Spiel. Im Code sind sie nur leere Container, dessen Eigenschaften über die zugewiesenen Components bestimmt werden. Entitäten haben neben den Components keine eigenen Attribute oder Funktionen.

**Component**
Components sind die Datensätze der Entitäten und beschreiben dadurch die Eigenschaften der Entitäten. Eine Component-Instanz gehört zu genau einer Entität.

Components speichern im Regelfall nur die Daten/den Zustand einer Entität.
In Ausnahmefällen kann es erforderlich sein, zusätzliche Logik in Components zu implementieren. Der Umfang sollte in diesen Fällen möglichst klein gehalten werden, da die Logik normalerweise in den Systemen realisiert wird.
Siehe auch [Strategy Pattern im ECS](ecs_and_strategy_pattern.md)

**System**
Systeme agieren auf Components und ändern die Werte in diesen. Sie beschreiben also das Verhalten der Entitäten. Ein System kann auf ein oder mehreren Components agieren.
In Systemen wird die eigentliche Logik implementiert.

Der Zustand einer Entität wird also über ihre Components bestimmt, und ihr Verhalten über die Systeme, die mit der jeweiligen Component-Kombination arbeiten. 

## Basisstruktur

![Struktur ECS](img/ecs.png)

Neu erzeugte Entitäten speichern sich automatisch im HashSet `entities` der `ECS`-Klasse ab.
`ECS_System`e speichern sich automatisch im `SystemController` `systems` der `ECS`-Klasse ab.

Die Systeme iterieren über die in `ECS` gespeicherten Entitäten und greifen über die Methode `Entite#getComponent` auf die für die jeweilige Funktionalität benötigten Components zu.

*Anmerkung*: Gelb hinterlegte Klassen stammen aus dem PM-Dungeon-Framework.

*Anmerkung*: Das UML-Diagramm ist auf die wesentlichen Bestandteile gekürzt.

## Integration des ECS in das PM-Dungeon-Framework

Die Klasse `ECS` ist die Start-Klasse und erbt von `Game` des PM-Dungeon-Frameworks.

Um die Systeme in die GameLoop des Frameworks zu integrieren, wird ein Objekt vom Typ `SystemController` genutzt. Dieser Controller funktioniert analog zu den anderen Controllern des Frameworks: Er hält die Menge aller vorhandenen Systeme und ruft einmal pro Frame für jedes System die `update`-Methode. Die Registrierung der Systeme beim Controller wird über den Konstruktor der Klasse `ECS_System` erledigt - dadurch müssen abgeleitete Systeme dies nicht selbst machen.
