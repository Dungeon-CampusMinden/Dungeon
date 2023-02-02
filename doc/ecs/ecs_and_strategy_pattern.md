# Strategy Pattern im ECS

Auch wenn es nicht streng im Sinne des ECS Paradigmas ist, hat sich die Verwendung des Strategy-Pattern als besonders hilfreich herausgestellt. 

## Wofür 

Wir verwenden das Strategy-Pattern vor allem in Komponenten, welche kleine eigenen Logiken implementieren. Beispiel: AI-Verhalten. 

In Zukunft sollen die, in der DSL geschriebenen Funktionen, als eine mögliche Strategie implementiert werden. 

## Wie

Die Strategy wird mithilfe eines Funktionalen-Interfaces umgesetzt. 
Das `Component`, speichert dann eine konkrete Implementierung (entweder als Lambda-Ausdruck oder als Instanz einer implementierenden Klasse) als Referenz. 
Über diese Referenz kann dann die konkrete Strategy ausgeführt werden.

Damit die Strategy von außen getriggert werden kann, bekommt das Component eine `void execute()`-Methode. Dies ruft dann über die Referenz die konkrete Strategy auf. 
Die `#execute`-Methode kann dann vom zuständigen System aufgerufen werden. 

## Bereits implementierte Strategy-Pattern

Hier ist eine Auflistung (mit UML) aller bereits implementierten Strategy-Pattern

### AIComponent
[AI-Component](./img/ai.png)

*Anmerkung: UML auf die wesentlichen Bestandteile gekürzt.* 
*Anmerkung: Die in rot dargestellten Klassen sind konkrete Implementierungen des Interfaces.* 