# Morph & Co. KG – Die Wörterfabrik

Morphologie-Escape-Room für 1–2 Spieler. Konzept, Lösungsweg und Technik:
[morph_co_concept.md](../../../../doc/escape_room/room_concepts/morph_co/morph_co_concept.md).

## Aufbau

| Paket | Inhalt |
| --- | --- |
| `MorphCo` | Starter, Server-/Client-Setup, Speicherpunkte, Tracking-Einwilligung |
| `level` | `MorphCoLevel` (Server), `MorphCoClientLevel`, ein Controller pro Bereich: `ControlRoom`, `Corridor`, `FlexionLine`, `AffixSorter`, `CircumfixStation`, `ShiftLead`; Zustand und Questlog |
| `puzzles` | reine Rätselmodelle ohne Framework-Abhängigkeiten (Wortarten, Affixe, Zirkumfixe, Zahlenfragen) |
| `petrinet` | `MorphCoMilestone`, `MorphCoProgressNet` |
| `ui` | eigene Dialoge: Wortarten-Kisten, Affix-Zerschneiden, Eingabeschächte, Zirkumfixstation, PC, Bücher |
| `items` | Zirkumfix-Teile im Inventar |
| `save`, `network`, `util` | Speicherstand, Snapshot-Erweiterung, Texte, Übersetzer, Achievements, Tracking |

Neue Rätsellogik gehört in den Controller ihres Bereichs; Meilensteine werden nur über
`MorphCoLevel.complete(...)` abgeschlossen, damit Questlog und Speicherstand mitlaufen.

## Starten

```sh
./gradlew :game:runMorphCo
./gradlew :game:runMorphCoLevelEditor
./gradlew :game:runMorphCoAutoplay
./gradlew :game:test --tests 'rooms.morphco.*'
```
