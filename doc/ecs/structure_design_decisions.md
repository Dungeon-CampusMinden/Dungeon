---
title: Strukturelle Designentscheidungen
 ---

Dieses Dokument erläutert verschiedene Design-Entscheidungen, die im Hinblick auf die Struktur des Codes getroffen wurden.

## Verwendung von `public static` in ECS

Einige Attribute der Klasse `ECS`, wie z.B. die Collection `entities` oder das aktuelle Level, sind als `public static` deklariert. Der Grund dafür ist, dass diese Attribute an vielen Stellen benötigt werden und so ein einfacher und lesbarer Zugriff möglich ist. Ein Nachteil ist jedoch, dass die Attribute dadurch auch einfacher manipuliert werden können. Als Lösungsansatz können statische Getter implementiert werden, um den Zugriff auf die Attribute zu kontrollieren. Eine Alternative wäre die Übergabe der Attribute per Methodenparameter, jedoch würde dies den Code schwerer lesbar machen und zu redundantem Code führen.

## Verwendung der Collection `ECS.entities`

Die Collection `ECS.entities` speichert alle Entitäten, die im Spiel existieren. Der Grund dafür ist, dass die Systeme auf die Components zugreifen müssen, um diese zu manipulieren. Dies erleichtert die Handhabung und macht den Code lesbarer. Ein Nachteil ist, dass die Systeme über jede Entität iterieren, auch wenn diese gar nicht das Key-Component besitzt. Dadurch ergibt sich eine Komplexität von mindestens O(n) für jede `ECS_System#update` Methode. Eine Alternative dazu wäre die Verwendung von mehreren Collections für die jeweiligen Components. Dies würde die Komplexität in `ECS_System#update` verringern, da gezielt auf die Components zugegriffen werden kann, aber es würde auch den Code unlesbarer machen, da für jede Component-Klasse eine eigene Collection angelegt werden müsste.

## Referenzen zwischen Components und Entity

Eine Component speichert eine Referenz auf die zugehörige Entität und die Entität speichert eine Referenz auf alle zugehörigen Components. Der Grund dafür ist, dass die Systeme über die Entitäten iterieren und von dort aus auf die Entitäten zugreifen. Für einige Components, die Logiken per Strategy-Pattern implementieren, ist es wichtig zu wissen, welche Entität die Logik ausführt. Daher speichert das Component auch die Entität. Diese Referenzspeicherung führt zu Redundanz. Eine Alternative dazu wäre, dass die Logiken in den Components über `ECS.entities` iterieren und prüfen, ob sie in dieser Entität hinterlegt sind. Dies würde keine redundante Speicherung der Referenzen erfordern, würde jedoch den Code unlesbarer machen und die Komplexität durch die zusätzliche Iteration erhöhen.

## Eigene Klasse für `Hero`

Der Held hat eine eigene Klasse bekommen, anstatt nur eine Instanz von Entity zu sein. Der Grund dafür ist, dass der Spielercharakter eine spezielle Rolle im Spiel übernimmt und in vielen Logiken abgefragt wird. Dies erleichtert die Handhabung und macht den Code lesbarer. Ein Nachteil ist jedoch, dass dies ungewollte Manipulationen ermöglicht und nicht ganz konsistent mit dem Umgang mit den anderen Entitäten ist. Eine Alternative wäre, den Helden als normale Instanz von Entity zu erzeugen. Dies wäre konsistenter mit dem Umgang mit den anderen Entitäten, würde jedoch den Code