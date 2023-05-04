---
title: "Strukturelle Designentscheidungen"
---

Dieses Dokument erläutert verschiedene Design-Entscheidungen, die im Hinblick auf die Struktur des Codes getroffen wurden.

## Verwendung von `public static` in `Game`

Einige Attribute der Klasse `Game`, wie z.B. das aktuelle Level, sind als `public static` deklariert. Der Grund dafür ist, dass diese Attribute an vielen Stellen benötigt werden und so ein einfacher und lesbarer Zugriff möglich ist. Ein Nachteil ist jedoch, dass die Attribute dadurch auch einfacher manipuliert werden können. Als Lösungsansatz können statische Getter implementiert werden, um den Zugriff auf die Attribute zu kontrollieren. Eine Alternative wäre die Übergabe der Attribute per Methodenparameter, jedoch würde dies den Code schwerer lesbar machen und zu redundantem Code führen.

## Verwendung der Collection `Game.entities`

Die Collection `Game.entities` speichert alle Entitäten, die im Spiel existieren. Der Grund dafür ist, dass die Systeme auf die Components zugreifen müssen, um diese zu manipulieren. Dies erleichtert die Handhabung und macht den Code lesbarer. Ein Nachteil ist, dass die Systeme über jede Entität iterieren, auch wenn diese gar nicht das Key-Component besitzt. Dadurch ergibt sich eine Komplexität von mindestens O(n) für jede `ECS_System#update` Methode. Eine Alternative dazu wäre die Verwendung von mehreren Collections für die jeweiligen Components. Dies würde die Komplexität in `ECS_System#update` verringern, da gezielt auf die Components zugegriffen werden kann, aber es würde auch den Code unlesbarer machen, da für jede Component-Klasse eine eigene Collection angelegt werden müsste.

Da die Systeme über die Collection `Game.entities` iterieren, können die Systeme neue Entitäten einfügen oder Entitäten entfernen, da sonst eine `ConcurrentModificationException` geworfen wird. Daher gibt es die Methoden `Game#addEntity` und `Game#removeEntity`, welche benutzt werden können um Entitäten hinzuzufügen oder zu entfernen. Das passiert in `Game#frame` nachdem alle Systeme einmal upgedated wurden. Das bedeutet, eine Entität wird erst im nächsten Frame zum Spiel hinzugefügt oder entfernt.

## Referenzen zwischen Components und Entity

Eine Component speichert eine Referenz auf die zugehörige Entität und die Entität speichert eine Referenz auf alle zugehörigen Components. Der Grund dafür ist, dass die Systeme über die Entitäten iterieren und von dort aus auf die Entitäten zugreifen. Für einige Components, die Logiken per Strategy-Pattern implementieren, ist es wichtig zu wissen, welche Entität die Logik ausführt. Daher speichert das Component auch die Entität. Diese Referenzspeicherung führt zu Redundanz. Eine Alternative dazu wäre, dass die Logiken in den Components über `Game.entities` iterieren und prüfen, ob sie in dieser Entität hinterlegt sind. Dies würde keine redundante Speicherung der Referenzen erfordern, würde jedoch den Code unlesbarer machen und die Komplexität durch die zusätzliche Iteration erhöhen.

## Eigene Klasse für `Hero`

Der Held hat eine eigene Klasse bekommen, anstatt nur eine Instanz von Entity zu sein.
Dies dient überwiegend der Lesbarkeit des Codes, da so die Initalisierung der Components des Helden in eine eigene Klasse ausgelagert sind.
Da `Game#getHero` ein `Optonal<Entity>` zurückliefert, wird der Held im gesamten Code wie eine normale Entität behandelt.
Da Vererbung gegen das ECS-Paradigma verstößt, könnte auch ein Builder benutzt werden, der eine Entität erstellt, welche den Helden als `Entity` erstellt.
So würde zu einem das ECS-Paradigma eingehalten werden, als auch die Lesbarkeit des Codes beibehalten werden, da eine Auslagerung in eine eigene Klasse immer noch möglich ist.
Zusätzlich würde es `instanceOf Hero`-Abfragen verhindern (es zwingt zu besserem Code).

