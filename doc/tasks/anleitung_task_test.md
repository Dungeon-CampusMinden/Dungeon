---
title: "Anleitung für die Definition von Aufgaben"
---

## Aufgaben per DSL definieren

Aktuell werden die Aufgaben-Definitionen aus `dungeon.assets.scripts.task_test.dng` eingelesen und
in ein Spielszenario übersetzt.

Es sind zwei unterschiedliche Aufgabentypen verfügbar

### Single Choice Frage

Eine Single Choice Frage benötigt eine textuelle Beschreibung (`description`),
eine Liste an Antwortmöglichkeiten (`answers`) und den 0-basierten
Index der korrekten Antwortmöglichkeit in `answers` (`correct_answer_index`).

`my_sc_task` ist im Folgenden der Name der Aufgabendefinition:

```
single_choice_task my_sc_task {
    description: "Hier kommt die Aufgabenbeschreibung hin",
    answers: ["antwort1", "antwort2", "antwort3"],
    correct_answer_index: 2
}
```

### Multiple Choice Frage

Eine Multiple Choice Frage benötigt eine textuelle Beschreibung (`description`),
eine Liste an Antwortmöglichkeiten (`answers`) und eine Liste von 0-basierten
Indizes der korrekten Antwortmöglichkeiten in `answers` (`correct_answer_indices`).

`my_mc_task` ist im Folgenden der Name der Aufgabendefinition:
```
multiple_choice_task my_mc_task {
    description: "Hier kommt die Aufgabenbeschreibung hin",
    answers: ["antwort1", "antwort2", "antwort3"],
    correct_answer_indices: [1,2]
}
```

### Einbindung der Aufgaben in den Dungeon

Über die `quest_config`-Definition können die definierten Aufgaben in das Dungeon integriert werden:

```
quest_config c {
    tasks: [my_sc_task, my_mc_task]
}
```

## Starten des Dungeons

Die Version des Dungeons, welche die Aufgaben enthält, kann mit dem gradle-Target `TaskGenerationTest`
gestartet werden, also: `./gradlew  taskGenerationTest --args "dungeon/assets/scripts/task_test.dng" `.


## So wird gespielt

Du spielst den Helden ![](../../game/assets/character/knight/idle/idle_knight_1.png), welcher mit den Tasten W![](../../game/assets/character/knight/u/idle_knight_1.png), A, S, D durch das Level gesteuert werden kann.

Um gegen die Monster ![](../../game/assets/character/monster/chort/idle_left/chort_idle_anim_mirrored_f0.png) ![](../../game/assets/character/monster/imp/idle_left/imp_idle_anim_mirrored_f1.png) zu kämpfen kannst du sie mit Feuerbällen ![](../../game/assets/skills/fireball/run_down/fireball_down_2.png) bewerfen. Ziele dafür mit deinem Mauszeiger auf das Monster und drücke die Taste "Q". Ich hoffe du hast dein Zielwasser getrunken.

Einige Monster ![](../../game/assets/character/monster/chort/idle_left/chort_idle_anim_mirrored_f0.png) ![](../../game/assets/character/monster/imp/idle_left/imp_idle_anim_mirrored_f1.png) hinterlassen Items (z.B. ![](../../game/assets/items/ring/beholder_ring.png),![](../../game/assets/items/potion/antidote_potion.png),![](../../game/assets/items/key/gold_key.png)) die du aufsammeln kannst, drücke dafür die Taste "E" wenn du in der nähe eines Items bist. In der Testversion haben diese Items keinen Nutzen, aber einstecken kann man sie immer.
Es gibt auch Schatzkisten ![](../../game/assets/objects/treasurechest/idle_left/chest_full_open_anim_f0.png) die Items enthalten können. Um eine Schatzkiste zu öffnen, nähere dich der Schatzkiste und drücke "E".

Im Level sind verschiedene NPCs (Non-Playable Characters) ![](../../game/assets/character/wizard/idle/idle_wizard_1.png) ![](../../game/assets/character/blue_knight/idle_left/knight_m_idle_anim_mirrored_f0.png) verteilt.
Wenn du dich ihnen näherst, kannst du mit der Taste "E" mit ihnen reden. Jeder von ihnen wird dir eine Frage stellen, welche du über die grafische Benutzeroberfläche beantworten sollst.

Aber Achtung, jeder NPC stellt dir die Frage nur einmal, antworte also mit Bedacht.

Um in das nächste Level zu gelangen, musst du einen der Ausgänge ![](../../game/assets/dungeon/dark/floor/floor_ladder.png) ![](../../game/assets/dungeon/default/floor/floor_ladder.png)
![](../../game/assets/dungeon/fire/floor/floor_ladder.png)
 ![](../../game/assets/dungeon/forest/floor/floor_ladder.png) ![](../../game/assets/dungeon/dark/ice/floor_ladder.png)![](../../game/assets/dungeon/rainbow/floor/floor_ladder.png) ![](../../game/assets/dungeon/temple/floor/floor_ladder.png) finden und betreten.

Im neuen Level erwarten dich wieder die NPCs ![](../../game/assets/character/wizard/idle/idle_wizard_1.png) ![](../../game/assets/character/blue_knight/idle_left/knight_m_idle_anim_mirrored_f0.png) und wollen dir ihre Frage erneut stellen.

Viel Glück.
