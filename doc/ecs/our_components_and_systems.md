## Unsere Components

Hier ist eine Liste aller aktuellen Components und derren Funktion im Dungeon. 

|Name | Funktion | Anmerkung|
|-------- | -------- | --------|
|`PositionComponent`   | Speichert die aktuelle Position im Dungeon   | |
|`VelocityComponent`   | Speichert die Beschleunigung und die aktuelle Beschleunigung   | |
|`AnimationComponent`   | Speichert die Idle-Animationen und die aktuelle Animation (auch aus anderen Components)   | |
|`AIComponent`   | Lässt eine Entität von der KI Steuern   | Verwendet 3-mal das Strategy-Pattern|
|`HitboxComponent`   | Speichert die Hitbox und das Verhalten bei einer Kollision   | Soll auf Strategy-Pattern umgebaut werden|
|`PlayableComponent`   | Markiert eine Entität als die Spieler-Entität   | Ist nur für den Helden gedacht|
|`SkillComponent`   | Speichert die Skills einer Entität   | Aktuell noch in Entwicklung|


## Unsere Systeme

Hier ist eine Liste aller aktuellen Systeme und derren Funktion im Dungeon. 
Als Key-Component wird das Component bezeichnet, welches ein System als "ich agiere darauf"-Markierung benötigt.
Benötigte Components sind dann weitere Abhängikeiten, die bei nicht Anwesenheit ggf. zu einer `MissingComponentException` führen. 

|Name |     Funktion | Key-Component        | Benötigte Components| Anmerkung|
|-------- | -------- |  ------------------ |  ------------------ | ---------|
|`DrawSystem`   | Zeichnet die Entiäten   | `AnimationComponent`              | `PositionComponent`              ||
|`VelocitySystem`   | Führt die Bewegung der Entiäten durch   | `VelocityComponent`              | `PositionComponent`, `AnimationComponent`              ||
|`KeyboardSystem`   | Übernimmt die Spielersteuerrung und aktionen bei Tastendruck | `PlayableComponent`              | `VelocityComponent`              ||
|`AISystem`   | Steuerrt die NPCs (z.B Monster)   | `AIComponent`              | abhängig von den verwendeten Strategien              ||
|`CollisionSystem`   | Prüft auf eine Kollision und ruft dann die `onCollide`Methode des `HitboxComponent` auf   | `HitboxComponent`              | `PositionComponent`              ||