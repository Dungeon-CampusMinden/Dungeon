# Überblick Components und Systeme


## Components


| Name                 | Funktion                                                                                | Anmerkung                                 |
|----------------------|-----------------------------------------------------------------------------------------|-------------------------------------------|
| `PositionComponent`  | Speichert die aktuelle Position im Dungeon                                              |                                           |
| `VelocityComponent`  | Speichert die Beschleunigung und die aktuelle Beschleunigung                            |                                           |
| `AnimationComponent` | Speichert die Idle-Animationen und die aktuelle Animation (auch aus anderen Components) |                                           |
| `AIComponent`        | Lässt eine Entität von der KI steuern                                                   | Verwendet 3-mal das Strategy-Pattern      |
| `HitboxComponent`    | Speichert die Hitbox und das Verhalten bei einer Kollision                              |                                           |
| `PlayableComponent`  | Markiert eine Entität als die Spieler-Entität                                           | Ist nur für den Helden gedacht            |
| `SkillComponent`     | Speichert die Skills einer Entität                                                      | Aktuell noch in Entwicklung               |
| `HealthComponent`    | Verwaltet die Lebenspunkte von Entiteän im Dungeon                                      |                                           |


## Systeme

| Name              | Funktion                                                                                | Key-Component        | Required Components                       | Anmerkung |
|-------------------|-----------------------------------------------------------------------------------------|----------------------|-------------------------------------------|-----------|
| `DrawSystem`      | Zeichnet die Entitäten                                                                  | `AnimationComponent` | `PositionComponent`                       |           |
| `VelocitySystem`  | Führt die Bewegung der Entitäten durch                                                  | `VelocityComponent`  | `PositionComponent`, `AnimationComponent` |           |
| `KeyboardSystem`  | Übernimmt die Spielersteuerung und Aktionen bei Tastendruck                             | `PlayableComponent`  | `VelocityComponent`                       |           |
| `AISystem`        | Steuert die NPCs (z. B. Monster)                                                        | `AIComponent`        | abhängig von den verwendeten Strategien   |           |
| `CollisionSystem` | Prüft auf eine Kollision und ruft dann die `onCollide`-Methode des `HitboxComponent` auf| `HitboxComponent`    | `PositionComponent`                       |           |
| `HealthSystem`    | Verrechnet den zuzufügenden Schaden und lässt Entitäten sterben                         | `HealthComponent`    | `AnimationComponent`                      |           |