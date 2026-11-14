# Morph & Co. KG – Die Wörterfabrik

Morphologie-Escape-Room für 1–2 Spieler. Konzept, Lösungsweg und Technik:
[morph_co_concept.md](../../../../doc/escape_room/room_concepts/morph_co/morph_co_concept.md).

## Aufbau

| Paket | Inhalt |
| --- | --- |
| `MorphCo` | Starter, Server-/Client-Setup, Speicherpunkte, Tracking-Einwilligung |
| `level` | `MorphCoLevel` (Server), `MorphCoClientLevel`, ein Controller pro Bereich: `ControlRoom`, `Corridor`, `FlexionLine`, `AffixSorter`, `CircumfixStation`, `ShiftLead`; die Technik-Hotline `HintHotline` am Telefon (gestufte Hinweise zum Schritt der Schichtleitung); Zustand und Questlog |
| `puzzles` | reine Rätselmodelle ohne Framework-Abhängigkeiten (Kartenleser und Hebel von A-12 mit gemeinsamem Tempo-Modell `Pace`, Sicherungsbox `FuseBox`, Wortarten, Affixe, Zirkumfixe, Zahlenfragen) |
| `petrinet` | `MorphCoMilestone`, `MorphCoProgressNet` |
| `ui` | eigene Dialoge: Kartenleser, Sicherungsbox, Aussehen des Engine-Telefons (`feature.interaction.phone`, für dessen Dialogtyp `PHONE` registriert), Papierkorb, Nahansicht der Affix-Sortieranlage mit wiederverwendbarem Ziffernblock `KeypadNode` (Zifferneingabe `DigitEntry` der Engine), Wortarten-Kisten, Affix-Zerschneiden, Eingabeschächte, Zirkumfixstation, PC, Bücher und Hamsterbuch auf dem gemeinsamen Papierblatt `PaperUI`; alle auf dem Canvas-Framework (`feature.canvas`) mit `MorphCoConsoleUI` als Rahmen und `MorphCoUI` als gemeinsamem Stil |
| `items` | Zirkumfix-Teile im Inventar |
| `save`, `network`, `util` | Speicherstand, Snapshot-Erweiterung, Texte, Übersetzer, Achievements, Tracking, Sounds der Dialoge |

Neue Rätsellogik gehört in den Controller ihres Bereichs; Meilensteine werden nur über
`MorphCoLevel.complete(...)` abgeschlossen, damit Questlog und Speicherstand mitlaufen.

Gefühle und Reaktionen zeigt der Raum als Emote über der Figur statt im Text:
`MorphCoProps.popupReaction(...)` setzt das Emote über den Spieler, sobald das Popup geschlossen ist
(Dialoge verdecken die Figuren). Die Schichtleitung zeigt nach einem Gespräch, wie es ihr gerade
geht; während der Reparaturen wartet sie nur.

## Objekte platzieren

Alle Objekte des Raums sind Prefab-Instanzen vom Typ `morphco-object` in der Level-Datei und werden
im Level-Editor im Prefab-Modus (4) gesetzt und verschoben. Eine Instanz hat Position, Objektart
(`MorphCoObjects.kinds()`, z. B. `console`, `crate`, `picture-summer`, `server`) und eine Zahl
(Kistenindex, Bandlänge, Größe einer Klickfläche in Zehntelkacheln).

Der Instanzname ist der Entitätsname, an den die Controller ihr Verhalten hängen
(`MorphCoObjects.on("console", ...)`); das greift auch, wenn der Editor ein verschobenes Objekt neu
erzeugt. Im Code stehen keine Positionen. Benannte Punkte gibt es nur noch für Türen und
Auslöseflächen. Reine Möbel ohne Verhalten brauchen keinen Code: eine Instanz mit beliebigem Namen
genügt. Wandbilder sind kleine Rahmen (`wall_*.png`); bei Interaktion zeigt der Raum das große Bild
(`*_large.png`).

## Starten

```sh
./gradlew :game:runMorphCo
./gradlew :game:runMorphCoLevelEditor
./gradlew :game:runMorphCoAutoplay
./gradlew :game:test --tests 'rooms.morphco.*'
```
