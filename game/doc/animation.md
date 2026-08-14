---
title: "Animation Basics"
---

In diesem Dokument wird erläutert, wie Animationen im Dungeon-Framework aufgebaut und verwendet werden können.

## Basics
Als Animationen beschreiben wir alles, was im Spiel visuell zu sehen ist. Das können Helden und Monster sein, aber auch statische Texturen wie die Wände und Böden im Dungeon. Eine Animation besteht aus einem oder mehreren Texturen (oder auch Frames), die nacheinander im Spiel angezeigt (gezeichnet) werden.

Wir kapseln libGDX Sprites in unserem Framework in der Klasse `Animation`.

## Ordnerstruktur und Pfade

Das `DrawComponent` erstellt bei der Erstellung alle Animationen, die im übergebenen Pfad gespeichert sind. Dafür ist eine spezifische Ordnerstruktur vorgesehen. Gehen wir von dem Pfad `character/hero` aus. In diesem Verzeichnis befinden sich weitere Unterverzeichnisse. Jedes Unterverzeichnis darf maximal eine Animation (bestehend aus einem oder mehreren `png`-Files) enthalten. Jedes Unterverzeichnis wird als Animation eingelesen, indem alle Bilder als Textur für die Animation verwendet werden. Die Animation wird im `DrawComponent` unter den Namen des Unterverzeichnisses gespeichert.

Für bessere Codelesbarkeit wurden Enum-Werte eingeführt. Das Enum `CoreAnimations` hat Werte für die Idle- und Laufanimationen. So kann z.B. mit `CoreAnimations.IDLE_LEFT` auf die Animation im Unterverzeichnis `idle_left` zugegriffen werden. Dies eignet sich besonders gut als Parameter für `DrawComponent#queueAnimation`.

## Animationen

Neben den eigentlichen Texturen kann eine Animation noch konfiguriert werden.
* `frameTime`: Gibt an, wie viele Frames vergehen sollen, bevor auf die nächste Textur in der Animation gezeigt werden soll.
* `loop`: Gibt an, ob eine Animation von vorne beginnen soll, wenn die letzte Textur der Animation gezeichnet worden ist. Wenn ja, springt der Zeiger wieder auf die erste Animation, wenn nein, dann bleibt der Zeiger auf der letzten Textur stehen.
* `prio`: Gibt die Priorität der Animation an; das `DrawSystem` zeichnet immer nur die Animation mit der höchsten Priorität.

## DrawComponent
Anmerkung: Das `DrawComponent` verwendet überwiegend Instanzen von `IPath` als Parameter, welche dann den Namen der Animation enthalten (siehe oben). Im Folgenden wird dennoch von "übergebenen Animationen" o.ä. gesprochen.

Animationen können mit `DrawComponent#queueAnimation` in die Animations queue aufgenommen werden. Dabei kann nur eine Animation oder eine Liste an Animationen übergeben werden. Es können nur Animationen in die Queue aufgenommen werden, die im `DrawComponent` auch gespeichert sind. Wenn mehrere Animationen übergeben werden, dann wird die erste Animation in der Liste in die Queue aufgenommen, die im `DrawComponent` gespeichert ist. Dies kann nützlich sein, wenn man nicht sicher weiß, ob eine bestimmte Animation gespeichert ist, um so eine Ausweichanimation festzulegen.

Mit dem Parameter `forFrames` kann festgelegt werden, wie lange eine Animation in der Queue gespeichert werden soll. Jedem Frame wird dieser Wert um 1 reduziert.

Das `DrawSystem` zeichnet immer die Animation in der Queue mit der höchsten Priorität. Der Frame-timer für jede Animation wird um 1 reduziert.
