---
title: "EntitySystemMapper"
---

## Basics

Die Klasse `EntitySystemMapper` wurde zur Optimierung der Laufzeit erstellt und speichert alle Entitäten, die alle Komponenten der zuvor definierten Filterregel (`Set<Class<? extends Component>>`) erfüllen. Ebenso speichert ein `EntitySystemMapper` die Systeme, welche auf Entitäten, die die Filterregeln erfüllen, einwirken.
Somit ist es nicht mehr notwendig, jeden Frame über alle Entitäten im Spiel zu iterieren und nach Komponenten zu filtern.

## Verwendung 

Die Klasse `ECSManagement` speichert (für das `Game`) eine Collection an `EntitySystemMapper`. Wenn ein neues System im Spiel registriert wird, wird überprüft, ob es bereits einen `EntitySystemMapper` mit denselben Filterregeln wie das System gibt. Wenn ja, wird das System zum jeweiligen `EntitySystemMapper` hinzugefügt. Wenn nicht, wird ein neuer `EntitySystemMapper` erstellt.
Wird ein neuer `EntitySystemMapper` erstellt, wird für jede Entität im Spiel überprüft, ob diese die Filterregeln erfüllt, und wenn ja, wird diese dem `EntitySystemMapper` hinzugefügt. 

*Hinweis*: Es gibt einen `EntitySystemMapper` mit leeren Filterregeln, dieser speichert daher **alle** Entitäten im Spiel. 

Wenn eine neue Entität hinzugefügt wird oder sich die Komponenten einer bestehenden Entität verändern, wird für jeden existierenden `EntitySystemMapper` überprüft, ob die Entität die Filterregeln (noch) erfüllt, und die Entität entsprechend dem `EntitySystemMapper` hinzugefügt bzw. entfernt. 

Wenn ein System den Entity-Stream aus dem Game abfragt, werden die Entitäten zurückgegeben, die zum `EntitySystemMapper` gehören, zu dem auch das System gehört.

##  Map<ILevel, Set<EntitySystemMapper>>

Durch die Implementierung des raumbasierten Levelsystems und die daraus folgende Möglichkeit, in einen Raum (also ein Level) wieder zurückzukehren, musste die Entitäten-Verwaltung so erweitert werden, dass sie die verschiedenen Level berücksichtigt.

Daher speichert das `ECSManagement` eine `Map<ILevel, Set<EntitySystemMapper>>`, sodass jedem Level eine eigene Collection an `EntitySystemMapper` zugeordnet werden kann. Die API-Methoden von `Game` verwenden immer das `LevelSystem#currentLevel` als Key. Es ist nicht notwendig, eigenständig Buch über die Level zu halten. Systeme werden automatisch auch in neuen Leveln hinzugefügt, Entitäten nicht.
