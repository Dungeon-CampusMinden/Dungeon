# System Recovery

## Aufbau

`SystemRecoveryLevel` besitzt je eine Controller-Instanz pro Rätsel. Aufbau, Zustand und
Interaktionen eines Raums liegen in `riddles`; wiederverwendbare Entity-Konstruktion liegt in
`entities`. Terminalerfolge werden in `SystemRecoveryRiddleRegistry` dem zuständigen Controller
zugeordnet. Neue Rätsellogik gehört nicht in statische Level-Weiterleitungen.

| Nr. | Controller | Kernaufgabe |
| --- | --- | --- |
| 1 | `EnergyRiddle` | Energie-Array, Werte, Batterie |
| 2 | `ModuleStorageRiddle` | Modul-Array, GPU-Ausfall, Länge, Türcode |
| 3 | `InventoryScannerRiddle` | belegte Module zählen, Türcode |
| 4 | `TransportStorageRiddle` | Paketarray und indexbasierte Verarbeitung |
| 5 | `ManualSortingRiddle` | benachbarte Werte manuell ordnen |
| 6 | `BubbleSortRiddle` | Sortierchip ausführen und Archivschlüssel vergeben |
| 7 | `DataArchiveRiddle` | drei Array-Deklarationen rekonstruieren |
| 8 | `TwoDimensionalStorageRiddle` | 3x4-Matrix und markierte Zellen |
| 9 | `SearchRobotRiddle` | Ortungschip und zweidimensionale Suche |
| 10 | `SystemCoreRiddle` | drei Kernprüfungen mit eigenem Suchroboter und Abschlussmaske |

Pflichtpunkte werden beim Levelstart durch `SystemRecoveryPointRegistry` gesammelt geprüft.
Historische Tippfehler werden dort nur als explizite Aliase unterstützt. Rätsel 8 nutzt die festen
`storage_cell_*`-Punkte; Rätsel 9 erzeugt seinen Laufweg aus `roboter_start` und `roboter_end`.
Rätsel 10 setzt einen zweiten, controllerlosen Roboter am `map00`-Eingang ein.

## Fortschritt und Hinweise

`SystemRecoveryProgressNet` ist die einzige Quelle für den aktiven Lernschritt. Auf dem Server
liegt genau ein Token auf einem Place aus `SystemRecoveryLearningStep`. Erfolgreiche
Terminaleingaben werden über `SystemRecoveryPuzzleEvents.terminalAttempt(...)` zugeordnet;
physische Aktionen schließen ihren erwarteten Schritt ausdrücklich ab. Story-Trigger verändern
das Netz nicht.

Jeder aktive Place besitzt vier spezifische Hinweise. `SystemRecoveryHintPhone` zeigt sie nach
Bestätigung, schreibt sie ins Questlog und trackt ihre Verwendung. Versuche, Lösungen und
Rätselstarts laufen ebenfalls zentral über `SystemRecoveryPuzzleEvents` und die Dungeon-Tracking-
API.

Jede serverseitig akzeptierte Code- oder Ergebnis-Eingabe wird zusätzlich als originale
Spielerlösung im passenden Rätsel-Tab des gemeinsamen Questlogs gespeichert. Der PC enthält den
Tab `Memory Watch`; er zeigt die Array-Namen aus diesen akzeptierten Eingaben und aktualisiert sich
nach einer bestätigten Terminaleingabe direkt im geöffneten PC.

Die ausführliche, aktuelle Beschreibung steht in:

- `doc/escape_room/room_concepts/prog1_arrays/petri_net_system_recovery_concept.md`
- `doc/escape_room/room_concepts/prog1_arrays/system_recovery_story_flow.md`
- `doc/escape_room/room_concepts/prog1_arrays/system_recovery_save_load.md`

## Story und Lokalisierung

Nach Lore und Steuerungsdialog werden die Terminals freigegeben. Die erste falsche Eingabe lässt
ECHOs Telefon klingeln. Ist bereits die erste Eingabe korrekt, meldet sich zunächst AXIOM; danach
klingelt ECHO mit einem anderen Einstiegsgespräch. ECHO liefert später Übergangsgespräche und
Hinweise. Das Öffnen des Systemkerns aktiviert Alarm und Wendepunkt.
Nach dem letzten Kernschritt klingelt ECHO erneut; erst nach diesem Gespräch öffnet sich der
Aufzug.

## Save und Load

Der Server speichert automatisch am Anfang jedes Haupträtsels. Beim Laden werden die aktiven
Petri-Place, abgeschlossene Räume, akzeptierte Terminaleingaben, Questlog und
Achievement-Fortschritt wiederhergestellt; Dialoge werden nicht erneut abgespielt. Teilschritte
innerhalb eines Rätsels sind keine Checkpoints. Details und Grenzen stehen in der
[Save/Load-Dokumentation](../../../../doc/escape_room/room_concepts/prog1_arrays/system_recovery_save_load.md).

Alle sichtbaren Texte liegen in `game/assets/language/systemRecovery/de.json` und `en.json`.
Serverseitig erzeugte Dialoge transportieren Schlüssel, damit jeder Client in seiner eigenen
Sprache rendert. `SystemRecoveryStoryDialogs` schreibt angezeigte Storyschritte zugleich ins
gemeinsame Questlog und verzögert sie, solange der betroffene Spieler einen Computer geöffnet hat.

## Multiplayer

Der Server entscheidet über Fortschritt, Items, Türen und zeitgesteuerte Abläufe.
`SystemRecoverySnapshotTranslator` und `SystemRecoveryEntitySpawnStrategy` übertragen den
aktuellen Zustand. Renderingeffekte werden nur auf grafischen Clients erzeugt; der Headless-Server
lädt keine Texturen oder Schriftarten.

## Tests

```sh
./gradlew :game:test --tests 'rooms.systemRecovery.*'
```
