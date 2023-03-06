---
title: "Startpage: Components"
---

| Name                 | Funktion                                                                                | Anmerkung                            |
|----------------------|-----------------------------------------------------------------------------------------|--------------------------------------|
| `PositionComponent`  | Speichert die aktuelle Position im Dungeon                                              |                                      |
| `VelocityComponent`  | Speichert die Beschleunigung und die aktuelle Beschleunigung                            |                                      |
| `AnimationComponent` | Speichert die Idle-Animationen und die aktuelle Animation (auch aus anderen Components) |                                      |
| `AIComponent`        | Lässt eine Entität von der KI steuern                                                   | Verwendet 3-mal das Strategy-Pattern |
| `HitboxComponent`    | Speichert die Hitbox und das Verhalten bei einer Kollision                              |                                      |
| `PlayableComponent`  | Markiert eine Entität als die Spieler-Entität                                           | Ist nur für den Helden gedacht       |
| `SkillComponent`     | Speichert die Skills einer Entität                                                      | Aktuell noch in Entwicklung          |
| `HealthComponent`    | Verwaltet die Lebenspunkte von Entitäten im Dungeon                                     |                                      |
| `XPComponent`        | Verwalten von Erfahrungspunkten einer Entität                                           |                                      |
| `InventoryComponent` | Speichert das Inventar einer Entität                                                    |                                      |
