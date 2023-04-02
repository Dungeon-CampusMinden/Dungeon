---
title: "Startpage: Systems"
---

| Name              | Funktion                                                                                 | Key-Component        | Required Components                       | Anmerkung |
|-------------------|------------------------------------------------------------------------------------------|----------------------|-------------------------------------------|-----------|
| `DrawSystem`      | Zeichnet die Entitäten                                                                   | `AnimationComponent` | `PositionComponent`                       |           |
| `VelocitySystem`  | Führt die Bewegung der Entitäten durch                                                   | `VelocityComponent`  | `PositionComponent`, `AnimationComponent` |           |
| `PlayerSystem`    | Übernimmt die Spielersteuerung und Aktionen bei Tastendruck                              | `PlayableComponent`  | `VelocityComponent`                       |           |
| `AISystem`        | Steuert die NPCs (z. B. Monster)                                                         | `AIComponent`        | abhängig von den verwendeten Strategien   |           |
| `CollisionSystem` | Prüft auf eine Kollision und ruft dann die `onCollide`-Methode des `HitboxComponent` auf | `HitboxComponent`    | `PositionComponent`                       |           |
| `HealthSystem`    | Verrechnet den zuzufügenden Schaden und lässt Entitäten sterben                          | `HealthComponent`    | `AnimationComponent`                      |           |
| `XPSystem`        | Verwaltung des LevelUp-Events.                                                           | `XPComponent`        |                                           |           |
