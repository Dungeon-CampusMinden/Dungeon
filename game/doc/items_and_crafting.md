---
title: "Items und Crafting"
---

## Items

[Items](https://de.wikipedia.org/wiki/Item_(Computerspielbegriff)) nehmen eine spezielle Rolle im Dungeon-Framework ein, da sie sich (teilweise) anders als klassische Entitäten verhalten.

Die Klasse `Item` des `contrib`-Packages speichert die verschiedenen Informationen, die für den Lebenszyklus eines Items notwendig sind. Eine Instanz der Klasse `Item` ist keine Entität, sondern ein (abstrakter) Datencontainer.

Ein Item kann in zwei Zuständen im Spiel existieren.

1. Als Item im Inventar einer Entität

In diesem Fall liegt die `Item`-Instanz im `InventoryComponent` der jeweiligen Entität. Das `Item` kann dann im Inventar verwendet werden, was die `use`-Methode des `Item` auslöst. Die Default-Implementierung der `use`-Methode löscht das `Item` aus dem Inventar des Spielers. Um eine eigene `use`-Methode zu bestimmen, kann eine eigene Item-Klasse geschrieben werden, welche dann die Methode überschreibt.

Ein Item kann aber auch aus dem Inventar in die Spielwelt gelegt werden. Dabei wird die `drop`-Methode des Items aufgerufen. In der Default-Implementierung wird dann eine Entität erstellt (siehe unten) und in der Spielwelt platziert. Auch diese Methode kann überschrieben werden, wenn ein anderes Verhalten gewünscht ist.

2. Als Objekt in der Welt

Befindet sich das Item in der Welt (z.B. auf dem Boden), ist es eine Entität. Dafür speichert die Item-Entität das `ItemComponent`, welches dann die `Item`-Instanz hält. Um aus einer `Item`-Instanz eine Entität in der Spielwelt zu machen, sollte am besten der `WorldItemBuilder` (`WorldItemBuilder#buildWorldItem`) verwendet werden. Dieser nutzt dann die Informationen in der `Item`-Instanz, um eine Item-Entität zu erzeugen (denk daran, die Item-Entität auch im Game zu registrieren (`Game#add`)). Der `WorldItemBuilder` verpasst der Entität neben dem `ItemComponent`, `PositionComponent` und `DrawComponent` auch ein `InteractionComponent`, welches bei einer Interaktion mit dem Helden das Item im Inventar des Spielers ablegt und die Item-Entität aus dem Spiel entfernt. Das Verhalten bei einer Interaktion wird in `Item#collect` implementiert und kann bei Bedarf überschrieben werden.
