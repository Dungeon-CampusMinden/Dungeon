## Was ist ein ECS (Kurzform)

**Entität:**
Entitäten sind die Objekte im Spiel. Im Code sind sie nur leere Container dessen Eigenschaften über die zugewiesenen Components bestimmt werden. Entitäten haben neben den Components keine eigenen Attribute oder Funktionen.

**Component**
Components sind die Datensätze der Entitäten. Jede Component-Instanz gehört zu genau einer Entität.
Components beschreiben die Eigenschaften von Entitäten.
Components speichern hautptsächlich Daten/den Zustand einer Entität.
Teilweise kann es nötig sein, Logik in Components zu implementieren, dies sollte aber nur in Ausnahmefällen passieren und die implementierte Logik sollte möglichst klein gehalten werden.
Siehe auch [Strategy Pattern im ECS](./ecs_and_strategy_pattern.md)

**System**
Systeme agieren auf Components und ändern die Werte in diesen. Sie beschreiben also das Verhalten der Entitäten. Ein System kann auf ein oder mehrere Components agieren.
In Systemen wird die eigentliche Logik implementiert.

Eine Entität und dessen Verhalten wird also daher bestimmt, welche Components es hat und wie die Systeme mit dieser Component-Kombination arbeiten.

## Basisstruktur

[ECS](./img/ecs.png)

Neu erzeugte Entitäten speichern sich automatisch im HashSet `entities` der `ECS`-Klasse ab.
`ECS_System`e speichern sich automatisch im `SystemController` `systems` der `ECS`-Klasse ab.
Die `ECS_System`e iterieren über das `entities` Set und prüfen mit `Entite#getComponent`, ob die benötigten Componenten in der Entiät abgelegt sind. 

*Anmerkung: In gelb hinterlegte Klassen stammen aus dem PM-Dungeon-Framework.* 
*Anmerkung: UML auf die wesenntlichen Bestandteile gekürzt.

## Wie wurde das ECS in das Dungeon integriert

Die Klasse `ECS` ist die Start-Klasse und erbt von `Game` des PM-Dungeon-Frameworks.
Um die `ECS_System` in die GameLoop des Frameworks zu integrieren, wird der `SystemController` genutzt. Dieser funktioniert analog zu den bereits bekannten Controllern des Frameworks. Er speichert also ein Set mit den ganzen `ECS_System`en und ruft die `update`-Methode jedes Systems einmal pro Frame auf. Im Konstruktor der `ECS_System`-Klasse trägt sich das System in den Controller ein. Da alle Systeme von dieser Klasse abgeleitet sind, ist ein manueller Eintrag in den Controller nicht nötig.
