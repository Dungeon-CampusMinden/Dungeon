## Was ist ein ECS (Kurzform)

**Entität:**
Entitäten sind die Objekte im Spiel. Im Code sind sie nur leere Container dessen Eigenschaften über die zugewiesenen Components bestimmt werden. Entitäten haben neben den Components keine eigenen Attribute oder Funktionen.

**Component**
Components sind die Datensätze der Entitäten. Jede Component-Instanz gehört zu genau einer Entität.
Components beschreiben die Eigenschaften von Entitäten.
Components haben nur `getter` und `setter`Funktionen, sie implementieren kein Verhalten. 

**System** 
Systeme agieren auf Components und ändern die Werte in diesen. Sie beschreiben also das Verhalten der Entitäten. Ein System kann auf ein oder mehrere Components agieren. 
In Systemen wird die eigentliche Logik implementiert.

Eine Entität und dessen Verhalten wird also daher bestimmt, welche Components es hat und wie die Systeme mit dieser Component-Kombination arbeiten. 

## Entitäten erstellen
Entitäten leiten von der Klasse `Entity` ab. 
Entitäten werden im Package `ecs.entitys` abgelegt. 
Auf eine tiefgehende Vererbungshierarchie sollte verzichtet werden.
Die meisten Entitäten werden über die DSL definiert, nur in ausnahmefällen sollten eigenen Entitäten angelegt werden. 
Entitäten tuen eigentlich nichts anderes, als im Konstruktor Components für sich zu erstellen.
Nur in ausnahmefällen sollte Entitäten eigenen (non-hilfs) Funktionen und Attribute haben. 

## Components erstellen
Um eigene Components zu implementieren muss die eigenen Klasse das Interface `Component`implementieren. 
Components werden im package `ecs.components` abgelegt und sollen den Namenshema `$WHAT_IS_THIS_COMPONENT$Component`folgen. 
Jedes Component muss im Konstruktor die zugehörige `Entity` übergeben bekommen und in einer entsprechenden Component-Map in der Klasse `ECS` abgelegt werden. 
Aktuell muss diese Map noch selbst implementiert werden. Maps sind `public static`, der Key ist die Entität, das Value die Component-Instanz. Die Maps werden in `ECS#setupComponentMaps` initialisiert. 
Jede Component-Instanz gehört zu genau einer Entitäs-Instanz. Eine Entitäts-Instanz kann einen Component-Typen nur einmal speichern.  


## Systeme erstellen 
Um eigenen Systeme zu implementieren muss die eigene Klasse von `ECS_System` abgeleitet werden.
Die Funktionalität des Systems wird in der `update`-Methode implementiert. Diese wird einmal pro Frame aufgerufen. 
Für gewöhnlich wird in der `update`-Methode über eine Component-Map (vgl. oben) iteriert. Einige Systeme benötigen mehrere Components um zu funktionieren. Beim verschachteln der Iteration sollte die äußerste Schleife über die spezifischte Component-Map durchgeführt werden (vgl `DrawSystem`), um die Anzahl der "unnötigen" Schleifendurchläufe zu minimieren.
Es ist darauf zu achten, dass einige Entitäten vielleicht nur eines der Components "speichern". Es darf nicht die grundlegende annahme getroffen werden, das zwei Components immer gemeinsam auftreten. Ein entsprechender `null`-Check ist duchzuführen. 

Systeme werden im package `ecs.systems` abgelegt und sollen den Namenshema `$WHAT_IS_THIS_SYSTEM$System`folgen. 
Jedes System muss im Konstruktor `super()` aufrufen, um die Registrierung des Systems im `SystemController`zu gewährleisten. 


## Animationen managen
tbd, siehe [Issue #94](https://github.com/Programmiermethoden/Dungeon/issues/94)

## Wie wurde das ECS in das Dungeon integriert

Die Klasse `ECS` ist die Start-Klasse und erbt von `Game` des PM-Dungeon-Frameworks. 
Um die `ECS_System` in die GameLoop des Frameworks zu integrieren, wird der `SystemController` genutzt. Dieser funktioniert analog zu den bereits bekannten Controllern des Frameworks. Er speichert also ein Set mit den ganzen `ECS_System`en und ruft die `update`Methode jedes Systems einmal pro Frame auf. Im Konstruktor der `ECS_System`-Klasse trägt sich das System in den Controller ein. Da alle Systeme von dieser Klasse abgeleitet sind, ist ein manueller Eintrag in den Controller nicht nötig. 