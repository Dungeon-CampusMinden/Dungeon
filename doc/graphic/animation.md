---
title: "Animation"
---

## Was ist das?
Darstellung von einer Abfolge bestimmter Grafiken. Sowohl in einer Schleife wiederholend sowie eine nicht endlos wiederholende Animation.
Wählt automatisch beim Aufruf von `getNextAnimationTexturePath()` die passende Grafik aus. Hier wird auch sichergestellt das die Anzeigedauer eingehalten wird.
  


## Was sind die Parameter?
- animationFrames: eine Sammlung von Grafiken die in einer bestimmten Reihenfolge angezeigt werden sollen.
- frameTime: die Anzeigedauer jeder Grafik
- Looping: soll die Animation endlos lange laufen oder nicht


## Wie erstelle ich das?
- Aufruf des Konstruktors in 2 Geschmacksarten:
  - einfacher erstellt immer wiederholende Animationen
    - benötigt die Sammlung der Grafiken
    - benötigt die Anzeigedauer
  - komplexerer ermöglicht die Auswahl, ob wiederholend oder nicht
    - benötigt die Sammlung der Grafiken
    - benötigt die Anzeigedauer
    - benötigt den Boolean für das Wiederholen


## Mögliche Fehler
- Grafiken werden zu schnell gewechselt
  - zusätzliche aufrufe von `getNextAnimationTexturePath()`
 