# Morph & Co. KG: Die Wörterfabrik

<p align="center"><img src="../../../../doc/img/readme/morph-co.png" alt="Morph &amp; Co. KG: a technician in the word factory in front of crates for the word classes and a conveyor belt"></p>

A morphology escape room for 1 or 2 players. There is no release yet. From the repository:
`./gradlew :game:runMorphCo`

## For players

For centuries, the word factory of Morph & Co. KG has produced every German word. This morning
the machines went haywire: prefixes end up on the wrong words, inflections get mixed up, and
quality control has stopped the production. When you arrive, the alarm is so loud that the shift
supervisor can only shout "ZU LAUT. ALARM!" at you. Silence the alarm, find out what is broken,
repair the affix sorting unit and the inflection line and restart the production before
communication collapses. Play alone or with a partner.

## For educators

| | |
| --- | --- |
| Subject | German linguistics (Germanistische Linguistik), morphology |
| Contents | inflectable word classes, prefixes, suffixes and circumfixes, word formation vs. inflection, free morphemes, morpheme analysis |
| Players | 1 or 2, alone or in multiplayer |
| Duration | Planned for 30 to 45 minutes; not yet confirmed in playtests |

Players switch off the alarm in the control room and learn from the production control system
that two machines are broken. In any order, they sort the words of two sentences into crates for
the inflectable word classes, cut words into morphemes, sort the affixes and tell word formation
from inflection, and finally answer a question about the morphemes of *Überlegungen* to restart
the production. The handout "Morphemtypen und wo sie zu finden sind" is split into two books, one
in each hall, so both halls depend on each other. Optional extras: a circumfix assembly station, a
fuse box, a wastepaper basket, phone numbers to try and a book with 100 hamster jokes.

**Controls and help:** move with W, A, S, D and interact by clicking an object or pressing E; B
opens the quest log, Esc closes dialogs. Every puzzle works with the mouse (drag and drop, or click
a piece and then its target), and the timing puzzles also work with the keyboard. Wrong answers cost
nothing. The shift supervisor at the entrance gives a hint for the current step. The technical
hotline (phone in the control room, extension **42**) helps with the same step in three stages: a
direction, a tip and finally the solution, which it reveals only after asking. The quest log keeps
tasks, clues and every hint heard.

**Evaluation:** with the players' consent, the room records puzzle starts, solutions, attempts
and the hints used.

-   [Walkthrough] with screenshots (German, full spoilers, also as [PDF][Walkthrough PDF])
-   [Concept] (German)
-   [Handout "Morphemtypen und wo sie zu finden sind"][Handout] (German)

## Technical notes

-   **Play from source:** `./gradlew :game:runMorphCo`
-   **Multiplayer:** one player chooses *Schicht beginnen* (start shift), the other
    *Spiel beitreten* (join game). A dedicated server starts with
    `./gradlew :game:runMorphCoServer`.
-   **[DEER-Editor] (level editor):** `./gradlew :game:runMorphCoLevelEditor`
-   **Autoplay:** `./gradlew :game:runMorphCoAutoplay` plays the room once from start to end.
-   **Tests:** `./gradlew :game:test --tests 'rooms.morphco.*'`
-   Texts: `game/assets/language/morphCo/` (German and English)

  [Walkthrough]: ../../../../doc/escape_room/room_concepts/morph_co/Morph%20&%20Co%20Walkthrough/MorphCo.md
  [Walkthrough PDF]: ../../../../doc/escape_room/room_concepts/morph_co/Morph%20&%20Co%20Walkthrough/MorphCo.pdf
  [Concept]: ../../../../doc/escape_room/room_concepts/morph_co/morph_co_concept.md
  [Handout]: ../../../../doc/escape_room/room_concepts/morph_co/Handout_Morphemtypen.pdf
  [DEER-Editor]: ../../../../game/doc/deer_editor.md

## Developer notes (German)

### Aufbau

| Paket | Inhalt |
| --- | --- |
| `MorphCo` | Starter, Server-/Client-Setup, Speicherpunkte, Tracking-Einwilligung |
| `level` | `MorphCoLevel` (Server), `MorphCoClientLevel`, ein Controller pro Bereich: `ControlRoom`, `Corridor`, `FlexionLine`, `AffixSorter`, `CircumfixStation`, `ShiftLead`; die Technik-Hotline `HintHotline` am Telefon (gestufte Hinweise zum Schritt der Schichtleitung); Zustand und Questlog |
| `puzzles` | reine Rätselmodelle (Hebel von A-12 mit dem Tempo-Modell `Pace` aus `feature.interaction.cardreader`, Wortarten, Affixe, Zirkumfixe, Zahlenfragen) |
| `petrinet` | `MorphCoMilestone`, `MorphCoProgressNet` |
| `ui` | eigene Dialoge: Aussehen des Engine-Kartenlesers (`feature.interaction.cardreader`, für dessen Dialogtyp `CARD_READER` registriert), Sicherungsbox (Aussehen des Engine-Kabeldialogs `feature.interaction.cables`, die Kabel legt `ControlRoom` fest), Aussehen des Engine-Telefons (`feature.interaction.phone`, für dessen Dialogtyp `PHONE` registriert; Ansagen auf einem Bildschirm unter dem Apparat), Papierkorb, Nahansicht der Affix-Sortieranlage, beide mit dem Ziffernblock `KeypadNode` (Zifferneingabe `DigitEntry` der Engine; Aussehen `KEYPAD` mit C und OK für das Schloss, `PHONE` mit grünem und rotem Hörer fürs Telefon), Wortarten-Kisten, Affix-Zerschneiden, Eingabeschächte, Zirkumfixstation, PC, Bücher und Hamsterbuch auf dem gemeinsamen Papierblatt `PaperUI`; alle auf dem Canvas-Framework (`feature.canvas`) mit `MorphCoConsoleUI` als Rahmen und `MorphCoUI` als gemeinsamem Stil; Wortarten-Kisten, Affix-Fächer, Eingabeschächte und Zirkumfixstation sortieren auf dem Zuordnungs-Board `SortBoard` aus `feature.interaction.sorting` im Aussehen `MorphCoSortBoard` |
| `items` | Zirkumfix-Teile im Inventar |
| `save`, `network`, `util` | Speicherstand, Snapshot-Erweiterung, Texte, Übersetzer, Achievements, Tracking, Sounds der Dialoge |

Kartenleser, Zuordnungs-Board, Kabel-Stecken und Telefon sind Engine-Features mit Prefabs im
DEER-Editor (`feature.interaction.cardreader`, `feature.interaction.sorting`,
`feature.interaction.cables`, `feature.interaction.phone`). Messung, Zuordnen, Stecken und Wählen
kommen aus der Engine. Morph & Co. liefert nur das Aussehen (`CardReaderUI`, `MorphCoSortBoard`,
`FuseBoxUI`, `PhoneUI`) und die Rätseldaten: Wörter, Fächer und Prüfungen in `puzzles`, die Kabel
der Sicherungsbox und die Leitungen des Telefons in `ControlRoom`.

Neue Rätsellogik gehört in den Controller ihres Bereichs; Meilensteine werden nur über
`MorphCoLevel.complete(...)` abgeschlossen, damit Questlog und Speicherstand mitlaufen.

Gefühle und Reaktionen zeigt der Raum als Emote über der Figur statt im Text:
`MorphCoProps.popupReaction(...)` setzt das Emote über den Spieler, sobald das Popup geschlossen ist
(Dialoge verdecken die Figuren). Die Schichtleitung zeigt nach einem Gespräch, wie es ihr gerade
geht; während der Reparaturen wartet sie nur.

### Objekte platzieren

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

### Geräusche und Musik

Der Server entscheidet, was zu hören ist, spielt aber selbst nichts ab: Er schickt die Geräusche an
die Clients, die sie abspielen. Wie die Sirene bekommt jeder Spieler bei der Begrüßung eigene
Schleifen (`targets`), denn ein Geräusch von vor dem Verbindungsaufbau erreicht keinen Client.

| Auslöser | Datei (`game/assets/sounds/`) | Quelle des Geräuschs | Lautstärke, Reichweite |
| --- | --- | --- | --- |
| Alarm aus | `morphco_printer_dying` (einmal: Drucker im Lüftungsschacht) | global | 0.3 |
| F-04 kaputt / repariert | `morphco_f04_squeak` / `morphco_f04_hum` | `flexion-machine` | 0.35 / 0.3, 10 Kacheln |
| Band in F-04 kaputt / repariert | `morphco_conveyor_jerky` / `morphco_conveyor_run` | Mitte von `conveyor` | 0.2 / 0.2, 8 Kacheln |
| A-12 kaputt / repariert | `morphco_a12_clatter` / `morphco_a12_plop` | `affix-sorter` | 0.25 / 0.4, 8 Kacheln |
| Band in A-12 kaputt / repariert | `morphco_conveyor_jerky` / `morphco_conveyor_run` | Mitte von `a12-conveyor` | 0.15 / 0.2, 6 Kacheln |

`MorphCoAmbience` startet die Maschinenschleifen und tauscht sie, sobald `MorphCoLevel.complete(...)`
eine Reparatur meldet. Jeder Client verrechnet die Lautstärken noch mit Gesamt- und
Effektlautstärke aus den Einstellungen. Die Engine dämpft ein Geräusch mit Reichweite nicht mit dem
Abstand, sondern schaltet es außerhalb der Reichweite stumm; die Reichweiten sind so gewählt, dass
man vor allem die Maschinen der eigenen Halle hört.

Die Hintergrundmusik (`game/assets/rooms/morphCo/factory_music.ogg`) spielt jeder Client selbst in
Schleife (`MorphCo.playMusic`). Sie folgt der Musik- und Gesamtlautstärke und liegt bei den
Standardeinstellungen unter den Maschinengeräuschen.
