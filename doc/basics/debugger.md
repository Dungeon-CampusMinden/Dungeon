---
title: "Debugger"
---

Die Klasse `tools.Debugger` liefert einige Funktionen, um während des Spielens bestimmte Szenarien hervorzurufen.
Der Debugger wird als ECS_SYSTEM in der Klasse Game angelegt und in die Game-Loop integriert.
Per Default ist der Debugger deaktiviert und muss erst auf Knopfdruck aktiviert werden.
Danach kann er mit demselben Knopf wieder deaktiviert werden.

Der Debugger liefert folgende Möglichkeiten:

Zoom der Kamera einstellen (rein und raus)
*   Spieler teleportieren
   *   zur Cursor-Position (wenn dieser auf einem gültigen Tile liegt)
   *   zum Level-Start
   *   zu einem Tile neben dem Level-Ende
   *   auf das Level-Ende
*   Spawnen eines Monsters auf die Position des Cursors mit:
   *   `PositionComponent`
   *   `VelocityComponent`
   *   `AnimationComponent`
   *   `HitboxComponent`
   *   `HealthComponent`
   *   `AIComponent`
      *   verwendet `RadiusWalk`, `ColideAI` und `SelfDefendTransition`
- Einstellen der Größe des nächsten Levels
  - toggelt zwischen SMALL -> MEDIUM -> LARGE -> SMALL ...
