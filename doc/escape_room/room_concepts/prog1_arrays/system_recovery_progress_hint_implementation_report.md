# System Recovery: Umsetzungs- und Prüfbericht

Stand: 16.09.2026. Alle sieben Arbeitspakete sind umgesetzt. Die vorgeschriebenen Tests,
Checkstyle und beide System-Recovery-JAR-Builds wurden ausgeführt. Ein echter Host/Join-Spieltest
wurde nicht gestartet; die konkreten manuellen Schritte stehen in Abschnitt 9.

## 1. Istzustand vor der Umsetzung

Der Worktree war bereits vor Beginn umfangreich verändert. Vorhandene lokale Änderungen wurden
bewahrt. Sie betrafen unter anderem Rätsel, Entities, Shader, Netzwerkcode, Übersetzungen, Dialoge
und Tests.

Vor der Überarbeitung bestanden diese Probleme:

- `SystemRecoveryProgressPlace` enthielt 34 Zustände statt 27 Lernaufgaben plus `COMPLETE`.
- `SystemRecoveryProgressNet` führte zusätzlich `activeState`, Event-Places und `ProgressEvent`-
  Routen. `activateAfterTransition(...)` konnte Tokens löschen und dadurch Fehler verdecken.
- Hinweise kamen teils aus `hints.generic` oder Storytexten. Die normalen Stufen verwendeten
  dieselbe Tracking-ID; jede Hint-Stufe war dadurch nicht separat identifizierbar.
- Questlogtexte wurden als wachsende Entity-Spawn-/Snapshot-Metadaten transportiert.
- System-Recovery-Inhalte lagen im allgemeinen `escapeRoom`-Namespace; die clientlokale
  Übersetzung servergesendeter Texte war nicht durchgängig abgesichert.
- Das Petri-Netz enthielt technische Zwischenzustände wie Display-, Animations-, Scan- und
  Raumereignisse statt ausschließlich der Spieler-Lernschritte.

## 2. Umgesetzte Arbeitspakete

### Paket 1: Lernschritte und Petri-Netz

`SystemRecoveryLearningStep` enthält exakt 27 geordnete Aufgaben plus `COMPLETE`. Jeder Place ist
eine echte ECS-`PlaceComponent`; initial besitzt nur `ENERGY_ARRAY` ein Token. Alle linearen
Transitionen haben Eingangsgewicht 2 und Ausgangsgewicht 1. `complete(expected)` prüft den aktiven
Token-Place, lehnt falsche Reihenfolge und Duplikate ohne Zustandsänderung ab und rollt einen
fehlgeschlagenen Übergang auf die vorherige Markierung zurück. `activeStep()` liest nur echte
Place-Tokens. Der initiale Start von Rätsel 1 wird beim Setzen der ersten Markierung genau einmal
an die Dungeon-Tracking-API gemeldet.

Dateien: `petrinet/SystemRecoveryLearningStep.java`,
`petrinet/SystemRecoveryProgressNet.java`. Tests: `SystemRecoveryLearningStepTest`,
`SystemRecoveryProgressNetTest`.

### Paket 2: Erfolgs-Callbacks

Die Terminal-Zuordnung liegt genau einmal als `TerminalStep`-Feld in `SystemRecoveryLearningStep`.
Erfolgreiche Terminalauswertung geht zentral über `SystemRecoveryPuzzleEvents.terminalAttempt(...)`
und wird dort auf genau einen typisierten Step abgebildet. Weltaktionen bleiben in ihren
Rätselklassen. Beide Keypads prüfen jetzt vor dem Türwechsel, ob der jeweilige Code-Step aktiv ist;
ein vorher eingegebener korrekter Code öffnet die Tür nicht, entsperrt das Keypad nicht und bleibt
erneut eingebbar.

Dateien: `util/tracking/SystemRecoveryPuzzleEvents.java`, `util/interpreter/TerminalStep.java`,
`util/interpreter/InterpretationCallbacks.java`, `level/SystemRecoveryRiddleRegistry.java`,
Rätsel- und Computerklassen.

### Paket 3: Hinweise und Tracking

`SystemRecoveryHintCatalog` erstellt für alle Lernschritte genau `orientation`, `approach`, `near`
und `solution`; generische und Story-Fallbacks sind entfernt. Das Telefon fragt vor jedem Hinweis
und vor der Lösung gesondert nach Bestätigung. Beim Bestätigen prüft es erneut, dass Step, Place und
Hint unverändert sind. Erst ein von `HintSystem` angenommener Hinweis wird mit
`Tracking.hintUsed(...)` erfasst und in den gemeinsamen Questlog geschrieben. Die Hint-Sequenz ist
teamweit; der Tracking-Teilnehmer stammt aus der anfragenden Player-Entity.

Dateien: `petrinet/SystemRecoveryHintCatalog.java`, `story/SystemRecoveryHintPhone.java`,
`util/tracking/SystemRecoveryTracking.java`, `util/tracking/SystemRecoveryPuzzleEvents.java`.
Der Test `SystemRecoveryPuzzleEventsTest.acceptedHintIdsAreTrackedPerParticipantAndPuzzle` prüft
vier Stufen und zwei Teilnehmer.

### Paket 4: Petri-Debug-Ansicht

`ProgressDebugSnapshot` ist unveränderlich und enthält primitive Werte sowie stabile Keys: aktiver
Step, Interpreter-State, Hintzähler, Tokenzahlen, Status aller Places, letzte angenommene/abgelehnte
Anfrage, Ablehnungsgrund und Kantengewichte. Der Client übersetzt/rendered die Antwort. Sie wird nur
auf explizite Debug-Anfrage gesendet, nicht im Welt-Snapshot. Der UTF-8-Payload bleibt laut Test
unter 8 KiB.

Datei: `petrinet/ProgressDebugSnapshot.java`. Test: `ProgressDebugSnapshotTest`.

### Paket 5: Multiplayer-Questlog

Das Questlog bleibt serverautoritativ und wird beim Levelstart initialisiert. Der Client sendet
einen zuverlässigen Custom-Request zum Öffnen oder Auswählen eines Tabs. Der Spieler wird nicht aus
dem Payload gelesen: `HeroController` übergibt den authentifizierten
`InputCommandContext`-Spieler an `QuestLogUI`. Die Antwort wird nur an diese Entity adressiert. Ist
bei diesem Spieler bereits ein Dialog offen, wird kein zweites pausierendes UI darübergelegt. Der
Client erhält nur den angefragten Tab samt Einträgen. Ein 50-Einträge-Test bestätigt, dass
Questloginhalt nicht in Spawn- oder Snapshot-Metadaten enthalten ist.

Dateien: `feature/questlog/QuestLogUI.java`, `feature/entities/HeroController.java`,
`rooms/systemRecovery/util/SystemRecoveryQuestLogUtil.java`,
`network/SystemRecoveryComponentSync.java`, `network/SystemRecoverySnapshotTranslator.java`,
`network/SystemRecoveryEntitySpawnStrategy.java`.

### Paket 6: Übersetzungen

System-Recovery- und Questlogtexte liegen in `game/assets/language/systemRecovery/de.json` und
`en.json`; beide Sprachdateien besitzen gleiche Blatt-Key-Strukturen. `SystemRecovery.java`
registriert sie neben den allgemeinen Escape-Room-Dateien, die weiterhin gemeinsame Menü- und
Standarddialogtexte enthalten. `SystemRecoveryTranslator` löst `systemRecovery.*` und
`questlog.*` erst lokal im rendernden Client auf.

Dateien: `SystemRecovery.java`, `util/SystemRecoveryTranslator.java`, `util/SystemRecoveryText.java`,
beide neuen JSON-Dateien, `util/SystemRecoveryTranslatorTest.java`.

### Paket 7: Integration und Regression

`SystemRecoveryProgressFlowTest` nutzt die echten Registrierungen aus
`TerminalInterpreterSetup.setupRoomStates()` und prüft alle 27 Lernschritte, aktive Place,
Ein-Token-Invariante, Interpreter-State und nächsten Hint. Varianten decken falsche Reihenfolge,
zukünftigen Code, Wiederholung, wiederholte `solved`-Callbacks, zwei wechselnde Teilnehmer und den
Debug-Skip über den registrierten Erfolgspfad ab. `SystemRecoveryDialogTriggerProgressTest` läuft
alle Story-Triggerpositionen ab, ohne Petri-Änderung. `SystemRecoveryKeypadProgressTest` führt beide
Keypad-Setupmethoden mit zu frühem und später gültigem Code aus.

Gefundener Gameplay-Bug: ein richtiger Code konnte an beiden Keypads vor Aktivierung des zugehörigen
Steps bereits die Tür öffnen. Die aktive-Step-Prüfung liegt nun vor dem Türwechsel. Der komplette
Testlauf deckte außerdem ein unvollständiges `ModuleStorageRiddleTest`-Fixture ohne Tür-Kachel auf;
der Test stellt diese jetzt bereit.

## 3. Vollständige Callback-Matrix

Terminalschritte laufen über `InterpretationCallbacks` →
`SystemRecoveryPuzzleEvents.terminalAttempt(...)` → die einmalige Zuordnung in
`SystemRecoveryLearningStep`. Die Tabelle nennt Erfolgsquelle, Abschlusswert und Testnachweis.

| Learning-Step | Erfolgsquelle / Kriterium | `complete(...)` | Testnachweis |
| --- | --- | --- | --- |
| `ENERGY_ARRAY` | Terminal akzeptiert die Array-Deklaration | `ENERGY_ARRAY` | Gesamtflow; `onlyAcceptedTerminalCallbacksCompleteTheirMappedStep` |
| `ENERGY_VALUES` | Terminal akzeptiert alle geforderten Energie-Slots | `ENERGY_VALUES` | Gesamtflow; Terminal-Szenariotests |
| `ENERGY_INSERT_BATTERY` | Batteriebox akzeptiert Batterie; Modulspeichertür wird geöffnet | `ENERGY_INSERT_BATTERY` | Flow prüft Stepfolge; Batteriebox nicht als vollständige Levelinteraktion gestartet |
| `MODULE_ARRAY` | Terminal akzeptiert fünf-slotiges String-Array | `MODULE_ARRAY` | Gesamtflow; Terminal-Szenariotests |
| `MODULE_VALUES` | Terminal akzeptiert die geforderten Modulbelegungen | `MODULE_VALUES` | Gesamtflow; Terminal-Szenariotests |
| `MODULE_REMOVE_GPU` | Terminal akzeptiert `module[2] = null` | `MODULE_REMOVE_GPU` | Gesamtflow; Terminal-Szenariotests |
| `MODULE_LENGTH` | Terminal akzeptiert Längenabfrage | `MODULE_LENGTH` | Gesamtflow; Terminal-Szenariotests |
| `ROOM2_DOOR_CODE` | `ModuleStorageRiddle`: Code 5 nur beim aktiven Step öffnet Scanner-Tür | `ROOM2_DOOR_CODE` | `inventoryScannerDoorCodeCannotBeConsumedBeforeItsStep` |
| `INVENTORY_COUNT` | Terminal akzeptiert Zählschleife | `INVENTORY_COUNT` | Gesamtflow; Terminal-Szenariotests |
| `ROOM3_DOOR_CODE` | `InventoryScannerRiddle`: Code 4 nur beim aktiven Step öffnet Lagertür | `ROOM3_DOOR_CODE` | `transportDoorCodeCannotBeConsumedBeforeItsStep` |
| `TRANSPORT_ARRAY` | Terminal akzeptiert Paket-Array | `TRANSPORT_ARRAY` | Gesamtflow; Terminal-Szenariotests |
| `TRANSPORT_COLLECT_LOOP` | Terminal akzeptiert Schleife mit `roboter.collect(...)` | `TRANSPORT_COLLECT_LOOP` | Gesamtflow; Terminal-Szenariotests |
| `DATA_STORAGE_DOOR_OPEN` | Transports scannt alle Pakete; `door_datenspeicher` ist offen | `DATA_STORAGE_DOOR_OPEN` | Flow prüft Stepfolge; komplette Scannerwelt nicht als Levelintegration gestartet |
| `MANUAL_SORTING` | `ManualSortingRiddle`: alle Vergleichsentscheidungen sind korrekt | `MANUAL_SORTING` | `ManualSortingRiddleTest`; typisierter Callback im Flow |
| `BUBBLE_SORT_CONDITION` | Computer-Editor akzeptiert Bubble-Sort-Bedingung und legt programmierten Stick ins Inventar | `BUBBLE_SORT_CONDITION` | `SystemRecoveryComputerFactoryTest`; Gesamtflow |
| `BUBBLE_SORT_MACHINE` | Sortierlauf fertig und Archivschlüssel ausgegeben | `BUBBLE_SORT_MACHINE` | Bubble-Sort-Tests; typisierter Callback im Flow |
| `ARCHIVE_ACCESS` | Schlüsselinteraktion öffnet `door_datenarchiv` | `ARCHIVE_ACCESS` | Flow prüft Stepfolge; echte Tür-/Inventarinteraktion nicht als Levelintegration gestartet |
| `ARCHIVE_ARRAYS` | Terminal akzeptiert drei Archiv-Arrays | `ARCHIVE_ARRAYS` | Gesamtflow; Terminal-Szenariotests |
| `STORAGE_ARRAY` | Terminal akzeptiert 3×4-Matrixdeklaration | `STORAGE_ARRAY` | Gesamtflow; Terminal-Szenariotests |
| `STORAGE_VALUES` | Terminal akzeptiert markierte Matrixwerte | `STORAGE_VALUES` | Gesamtflow; Terminal-Szenariotests |
| `SEARCH_PROGRAM` | Computer-Editor akzeptiert Suchprogramm und gibt programmierten Chip aus | `SEARCH_PROGRAM` | `SystemRecoveryComputerFactoryTest`; Search-Szenariotest; Gesamtflow |
| `SEARCH_ROBOT_RUN` | Suchroboterscan fertig; Systemkern-Zugangsmodul wurde ausgegeben | `SEARCH_ROBOT_RUN` | typisierter Controller-Callback im Flow; kompletter Scan nicht als Levelintegration gestartet |
| `SYSTEM_CORE_ACCESS` | Zugangsskript öffnet `door_systemcore`, aktiver Step stimmt | `SYSTEM_CORE_ACCESS` | Flow prüft Stepfolge; echter Level-/Türzugriff nicht als Levelintegration gestartet |
| `CORE_SORT` | Terminal akzeptiert Systemkern-Bubble-Sort | `CORE_SORT` | Gesamtflow; Terminal-Szenariotests |
| `CORE_COUNT` | Terminal akzeptiert Modul-Zählschleife | `CORE_COUNT` | Gesamtflow; Terminal-Szenariotests |
| `CORE_SEARCH` | Terminal akzeptiert vollständige Matrixsuche | `CORE_SEARCH` | Gesamtflow; Terminal-Szenariotests |
| `CORE_META` | Eingabemaske akzeptiert Ergebniskombination und startet Aufzugabschluss | `CORE_META` | `SystemCoreMetaInputTest`; finale Place-Invariante im Flow |

`COMPLETE` ist die Senke ohne HintComponent und ohne ausgehende Transition. Displays, Storytrigger,
Lever, Items, Animationen, Zwischenframes und Raumpositionen schalten das Netz nicht.

## 4. Entfernte Altlogik und Suchnachweis

Entfernt wurden `SystemRecoveryProgressPlace.java`, die 34er-Place-Liste, `activeState`,
`ProgressEvent`-/Event-Place-Routen, `activateAfterTransition(...)`, generische Hinttexte und
Story-Fallbacks, wiederverwendete Hint-Tracking-IDs sowie Questlogtexte in Spawn-/Snapshot-Metadaten.
Andere entity-spezifische Synchronisation bleibt bestehen.

Die Suche in System-Recovery-Source und den relevanten Tests nach alten Place-/Event-Namen,
`activeState`, Reparatur-/Interaction-Methoden, generischen Hint-Fallbacks und Questlogmetadaten
ergab keine verbliebenen Produktionsreferenzen. Absichtliche Treffer: der Übersetzungstest prüft
die Abwesenheit von `hints.generic`; die allgemeine `QuestLogUtil`-Javadoc beschreibt weiterhin
Last-Hour-Snapshotverhalten. `SystemRecovery.java` registriert `language/escapeRoom/de.json` und
`en.json` weiterhin für gemeinsame Menü-/Standarddialogtexte. System-Recovery- und Questlog-Inhalte
liegen vollständig im neuen Namespace.

## 5. Petri-Invarianten

- Initial: genau ein Token in `ENERGY_ARRAY`, alle anderen einschließlich `COMPLETE` bei null.
- Stabil: genau ein Place hat genau ein Token.
- Erfolg: aktiver Step mit einem Token wird auf zwei erhöht; Gewicht-2-Eingang verbraucht beide,
  Gewicht-1-Ausgang setzt ein Token im direkten Nachfolger.
- Ablehnung bei falscher Reihenfolge, Duplikat, `null`, `COMPLETE`, fehlerhafter Transition oder
  ungültiger Markierung ändert den stabilen Fortschritt nicht. Bei Übergangsfehler wird die
  vorherige Markierung wiederhergestellt.
- Aktiver Step wird ausschließlich aus echten Place-Tokens abgeleitet; keine zweite Variable und
  keine Produktionsreparatur per Token-Clear.
- Raumposition und Dialogtrigger können niemals Fortschritt schalten.
- Debug-Skip existiert nur für Debugmodus und verwendet denselben registrierten Erfolgsweg mit
  derselben Ein-Token-Invariante.

## 6. Hint- und Tracking-Matrix

Jeder Hinweisschlüssel ist
`systemRecovery.hints.steps.<step-key>.<stage>`. Für jeden Step existieren exakt die vier Stufen
`orientation`, `approach`, `near`, `solution`. Die Tracking-Hint-ID lautet `<step-key>:<stage>`;
an `Tracking.hintUsed(...)` gehen zusätzlich die Puzzle-ID aus der Tabelle und die vom Dungeon
aufgelöste Teilnehmer-ID.

| Step-Key | Puzzle-ID | Vier Tracking-Hint-IDs (entsprechen den vier Hint-Keys) |
| --- | --- | --- |
| `energy-array` | `energy-array` | `energy-array:orientation`, `energy-array:approach`, `energy-array:near`, `energy-array:solution` |
| `energy-values` | `energy-array` | `energy-values:orientation`, `energy-values:approach`, `energy-values:near`, `energy-values:solution` |
| `energy-insert-battery` | `energy-array` | `energy-insert-battery:orientation`, `energy-insert-battery:approach`, `energy-insert-battery:near`, `energy-insert-battery:solution` |
| `module-array` | `module-storage` | `module-array:orientation`, `module-array:approach`, `module-array:near`, `module-array:solution` |
| `module-values` | `module-storage` | `module-values:orientation`, `module-values:approach`, `module-values:near`, `module-values:solution` |
| `module-remove-gpu` | `module-storage` | `module-remove-gpu:orientation`, `module-remove-gpu:approach`, `module-remove-gpu:near`, `module-remove-gpu:solution` |
| `module-length` | `module-storage` | `module-length:orientation`, `module-length:approach`, `module-length:near`, `module-length:solution` |
| `room2-door-code` | `module-storage` | `room2-door-code:orientation`, `room2-door-code:approach`, `room2-door-code:near`, `room2-door-code:solution` |
| `inventory-count` | `inventory-scanner` | `inventory-count:orientation`, `inventory-count:approach`, `inventory-count:near`, `inventory-count:solution` |
| `room3-door-code` | `inventory-scanner` | `room3-door-code:orientation`, `room3-door-code:approach`, `room3-door-code:near`, `room3-door-code:solution` |
| `transport-array` | `transport-storage` | `transport-array:orientation`, `transport-array:approach`, `transport-array:near`, `transport-array:solution` |
| `transport-collect-loop` | `transport-storage` | `transport-collect-loop:orientation`, `transport-collect-loop:approach`, `transport-collect-loop:near`, `transport-collect-loop:solution` |
| `data-storage-door-open` | `transport-storage` | `data-storage-door-open:orientation`, `data-storage-door-open:approach`, `data-storage-door-open:near`, `data-storage-door-open:solution` |
| `manual-sorting` | `manual-sorting` | `manual-sorting:orientation`, `manual-sorting:approach`, `manual-sorting:near`, `manual-sorting:solution` |
| `bubble-sort-condition` | `bubble-sort` | `bubble-sort-condition:orientation`, `bubble-sort-condition:approach`, `bubble-sort-condition:near`, `bubble-sort-condition:solution` |
| `bubble-sort-machine` | `bubble-sort` | `bubble-sort-machine:orientation`, `bubble-sort-machine:approach`, `bubble-sort-machine:near`, `bubble-sort-machine:solution` |
| `archive-access` | `data-archive` | `archive-access:orientation`, `archive-access:approach`, `archive-access:near`, `archive-access:solution` |
| `archive-arrays` | `data-archive` | `archive-arrays:orientation`, `archive-arrays:approach`, `archive-arrays:near`, `archive-arrays:solution` |
| `storage-array` | `two-dimensional-storage` | `storage-array:orientation`, `storage-array:approach`, `storage-array:near`, `storage-array:solution` |
| `storage-values` | `two-dimensional-storage` | `storage-values:orientation`, `storage-values:approach`, `storage-values:near`, `storage-values:solution` |
| `search-program` | `search-robot` | `search-program:orientation`, `search-program:approach`, `search-program:near`, `search-program:solution` |
| `search-robot-run` | `search-robot` | `search-robot-run:orientation`, `search-robot-run:approach`, `search-robot-run:near`, `search-robot-run:solution` |
| `system-core-access` | `system-core` | `system-core-access:orientation`, `system-core-access:approach`, `system-core-access:near`, `system-core-access:solution` |
| `core-sort` | `system-core` | `core-sort:orientation`, `core-sort:approach`, `core-sort:near`, `core-sort:solution` |
| `core-count` | `system-core` | `core-count:orientation`, `core-count:approach`, `core-count:near`, `core-count:solution` |
| `core-search` | `system-core` | `core-search:orientation`, `core-search:approach`, `core-search:near`, `core-search:solution` |
| `core-meta` | `system-core` | `core-meta:orientation`, `core-meta:approach`, `core-meta:near`, `core-meta:solution` |

Die Dungeon-API dedupliziert mit `TrackingSession.HintUse` anhand `(participantId, puzzleId,
hintId)`. Dadurch zählt dieselbe Hintstufe für dieselbe Person und dasselbe Rätsel nur einmal;
andere Teilnehmer bleiben getrennt. Es gibt keinen eigenen Raumzähler.

## 7. Multiplayernachweis

Automatisiert belegt:

- Fortschritt wird serverseitig im ECS-Petri-Netz geändert; Storytrigger und Clientdarstellung
  schalten keine Transition.
- Hint-Annahme verwendet `HintSystem` an der gemeinsamen Place-Entity. Die Bestätigung prüft genau
  diese Entity; Tracking ermittelt den Teilnehmer über `Tracking.participantForEntity(...)`.
- Questlog-Requests laufen zuverlässig. Authentifizierte Spieleridentität kommt aus
  `InputCommandContext`, nicht aus dem Payload. Zwei getrennte Requests erhalten im Test jeweils
  nur den Dialog für die eigene Entity.
- Ein schon offenes UI verhindert einen darübergelegten Questlogdialog. Ein 50-Einträge-Test weist
  nach, dass der Log nicht in Entity-Spawns oder Snapshot-Metadaten serialisiert wird.
- Dieselben `systemRecovery.*`- und `questlog.*`-Keys werden lokal auf Deutsch und Englisch
  aufgelöst; JSON-Parität und Hint-Vollständigkeit sind getestet.
- Debugantwort bleibt unter 8 KiB; Questlogtexte werden nicht in periodischen UDP-Snapshots
  übertragen.

Nicht automatisiert: Ein echter Host/Join-Prozesslauf mit zwei verschiedenen Clients,
Netzwerkunterbrechungen oder Packet-Capture. Der Zwei-Client-Test mockt Dialogadressierung, startet
aber keinen Server und zwei vollständige Clients.

## 8. Testprotokoll

Der finale vollständige `game:test`-Lauf nach dem Debug-Host-Fix meldete 840 Tests, 0 Fehler und
0 übersprungene Tests.

| Kommando | Ergebnis |
| --- | --- |
| `./gradlew game:test --tests 'rooms.systemRecovery.petrinet.SystemRecoveryLearningStepTest' --tests 'rooms.systemRecovery.petrinet.SystemRecoveryProgressNetTest'` | Paket 1: final 10 bestanden; vorherige Rotphasen erwarteten fehlendes Enum und fehlenden Start-Tracking-Event. |
| `./gradlew game:test --tests 'rooms.systemRecovery.petrinet.*' --tests 'rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEventsTest' --tests 'rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactoryTest'` | Paket 2 fokussiert: 22 bestanden vor dem später ergänzten initialen Puzzle-Start-Trackingtest. |
| `./gradlew game:test --tests 'rooms.systemRecovery.petrinet.SystemRecoveryHintCatalogTest' --tests 'feature.hints.HintSystemTest' --tests 'rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEventsTest'` | Paket 3 fokussiert: 17 Tests bestanden; Hintstufen, angenommene/veraltete/duplizierte Bestätigungen, vier Tracking-IDs und zwei Teilnehmer geprüft. |
| `./gradlew game:test --tests 'rooms.systemRecovery.petrinet.*' --tests 'feature.hints.HintSystemTest' --tests 'rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEventsTest'` | Paket 4 fokussiert: Debugsnapshot, Petri-Netz und Hint-System bestanden. |
| `./gradlew game:test --tests 'feature.questlog.QuestLogUITest' --tests 'rooms.systemRecovery.network.SystemRecoverySnapshotTranslatorTest'` | Paket 5 fokussiert: erster Lauf hatte einen Mockito-Varargsfehler im neuen Test; korrigiert, danach 13 bestanden. |
| `./gradlew game:test --tests 'rooms.systemRecovery.util.SystemRecoveryTranslatorTest' --tests 'rooms.systemRecovery.SystemRecoveryLocalizationTest'` | Paket 6 fokussiert: 6 bestanden. |
| `./gradlew game:test --tests 'rooms.systemRecovery.riddles.SystemRecoveryKeypadProgressTest' --tests 'rooms.systemRecovery.util.interpreter.SystemRecoveryProgressFlowTest' --tests 'rooms.systemRecovery.level.SystemRecoveryDialogTriggerProgressTest'` | Paket 7: 5 Tests bestanden; Keypad-Gating, Gesamtflow und Positionsunabhängigkeit geprüft. |
| `./gradlew game:test --tests 'rooms.systemRecovery.riddles.ModuleStorageRiddleTest'` | 1 bestanden; Test-Fixture stellt benötigte Tür-Kachel bereit. |
| `./gradlew game:test --tests 'rooms.systemRecovery.petrinet.*'` | Final erneut ausgeführt: 15 bestanden, 0 Fehler, 0 übersprungen. |
| `./gradlew game:test --tests 'rooms.systemRecovery.network.*'` | 6 bestanden. |
| `./gradlew game:test --tests 'rooms.systemRecovery.util.SystemRecoveryTranslatorTest'` | 5 bestanden. |
| `./gradlew game:test --tests 'rooms.systemRecovery.SystemRecoveryDebugModeTest' --tests 'rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactoryTest'` | 12 bestanden; Host-Debugflag wird nur im Debugstart weitergegeben, im Normalstart nicht. |
| `./gradlew game:test` | Frühere Läufe: 835 und 837 bestanden. Final nach Debug-Host-Fix: 840 bestanden, 0 Fehler, 0 übersprungen. |
| `./gradlew game:checkstyleMain game:checkstyleTest` | Erster Lauf: 38 Javadoc-/Import-Warnungen; gezielt korrigiert. Wiederholung erfolgreich. |
| `./gradlew game:buildSystemRecoveryJar game:buildSystemRecoveryServerJar` | Beide JAR-Builds erfolgreich. |
| `git diff --check` | Final erfolgreich; keine Whitespace- oder Patchfehler. |
| `rg -n -e 'SystemRecoveryProgressPlace|ProgressEvent|activeState|activateAfterTransition|successfulInteraction|hasInteractionTransition|escapeCompleted|hints\\.generic|questLogMetadata|questLogFromMetadata|METADATA_QUESTLOG|questLogSnapshot' game/src/rooms/systemRecovery game/src/feature/questlog game/test/rooms/systemRecovery game/test/feature/questlog` | Keine verbliebenen Produktionsreferenzen; nur negativer Hint-Regressionstest und allgemeine Last-Hour-Javadoc. |

Ein fokussierter Gradle-Aufruf wurde zwischenzeitlich durch Sandbox-Schutz beim Erzeugen/Öffnen
des vorhandenen Wrapper-Locks unter `~/.gradle/...gradle-9.7.1-bin.zip.lck` verhindert
(`Operation not permitted`). Derselbe Test lief mit freigegebenem Cachezugriff erfolgreich. Das war
kein Codefehler. Nach Ergänzung des Hint-Tracking-Adaptertests wurde der Paket-3-Lauf erneut
ausgeführt und war erfolgreich.

Nach Abschluss der sieben Pakete zeigte ein manueller Start, dass alle Terminal-Debugaktionen
(„Next step“, Debug-Items und Petri-Netz) im gehosteten Spiel scheinbar wirkungslos waren. Die UI
lief im Client mit `--debug`, aber `GameStarter` startete den autoritativen Server als separate JVM
nur mit `--server`. Dessen Debug-Guards lehnten die Aktionen daher korrekt ab. `SystemRecovery`
übergibt `--debug` jetzt zusätzlich an den Host-Kindprozess, wenn der aufrufende Prozess im
Debugmodus ist. `SystemRecoveryDebugModeTest` prüft Debugstart, Level-Editorstart und Normalstart.
Danach liefen die fokussierten Computer-/Debugtests (12), Checkstyle und die vollständige Suite
(840 Tests) erfolgreich.

## 9. Manuelle Restprüfung: Zwei Clients

1. Host und Join-Client starten und unterschiedliche Sprachen einstellen. Intro, Telefon, Räume,
   Questlog und Displays müssen dieselben Keys clientlokal unterschiedlich übersetzen.
2. Hint am Telefon anfordern und ablehnen. Der Index darf sich nicht bewegen. Erneut anfordern,
   bestätigen und kontrollieren, dass der Eintrag im passenden gemeinsamen Questlog-Tab auftaucht.
3. Den nächsten Hint vom zweiten Client bestätigen. Sequenz muss gemeinsam weiterlaufen; Tracking
   muss den zweiten Teilnehmer separat führen.
4. Questlog öffnen, während ein anderes UI/Terminal offen ist. Kein zweites pausierendes Fenster
   darf darüberliegen. Nach Schließen erneut öffnen und Tab wechseln.
5. Einen Tab mit vielen langen Einträgen füllen. Antwort darf nur zum anfragenden Client gehen;
   laufende UDP-Snapshots dürfen keine Questlogtexte enthalten.
6. Room-2-Code 5 und Transport-Code 4 vor dem aktiven Step eingeben. Türen müssen geschlossen
   bleiben, Keypads zurücksetzen. Nach Aktivierung müssen dieselben Codes die Türen öffnen.
7. Alle Storytrigger ablaufen und Petri-Debugstatus davor/danach vergleichen. Keine Tokenänderung.
8. Debug-Skip ausführen und auf beiden Clients denselben aktiven Place und Interpreter-Step prüfen.

## 10. Abweichungen und Restrisiken

- **Kein echter Host/Join-Test:** Netzwerkadressierung, Shared-State, Snapshotgrenzen und lokale
  Übersetzung sind isoliert automatisiert geprüft. Folge: Abschnitt 9 vor dem internen Spieltest.
- **Einige physische Weltaktionen nicht als komplette Levelinteraktion getestet:** Batteriebox,
  Datenspeicherscan, Archivschlüssel-Tür und Systemkernzugang werden im Flow an den autoritativen
  Callbackgrenzen simuliert. Keypads und Storytrigger besitzen gezielte Integrationstests. Folge:
  reale Ausführung beim Zwei-Client-Test mitprüfen.
- **Tracking-Persistenz:** Der Room-Adaptertest prüft Puzzle-ID, Hint-ID und Teilnehmerzuordnung.
  Er startet keine persistente Tracking-Session und liest kein JSONL. Die Deduplizierung
  `(participantId, puzzleId, hintId)` ist Engine-Verantwortung in `TrackingSession.HintUse`.
- **Allgemeine Übersetzungen bleiben in `escapeRoom`:** Diese Dateien enthalten von mehreren Räumen
  genutzte Standard-/Menütexte; alle System-Recovery- und Questlog-Inhalte sind verschoben.
- **Historische Dokumente:** Suchbereinigung galt für ausführbaren Source-Code und relevante
  Tests. Frühere Konzept-/Inventardokumente wurden nicht als Implementierungsquellen umgeschrieben.
- **Fremde Worktree-Änderungen:** Bereits veränderte/neue Rätsel-, Entity-, Shader- und
  Dokumentationsdateien blieben unangetastet, soweit sie nicht zwingend zu diesem Auftrag gehörten.

## 11. Review-Anker

1. `game/src/rooms/systemRecovery/petrinet/SystemRecoveryLearningStep.java`
2. `game/src/rooms/systemRecovery/petrinet/SystemRecoveryProgressNet.java`
3. `game/src/rooms/systemRecovery/petrinet/SystemRecoveryHintCatalog.java`

## 12. Follow-up: Debugaktionen im gehosteten Spiel

Die Debugaktionen waren im Host-Spiel wirkungslos, weil der Menü-/Clientprozess `--debug` kannte,
der separat gestartete autoritative Server jedoch nur `--server` erhielt. Der Server ließ damit
`next step`, Debug-Items und Petri-Netz-Ansicht an seinen Debug-Guards abprallen. Der
System-Recovery-`GameStarter` reicht den Modus nun per `--debug` an den Kindprozess weiter; der
Normalstart bleibt ohne Debugflag. Tests: `SystemRecoveryDebugModeTest` deckt Debug-,
Level-Editor- und Normalstart ab. Fokussierte Tests, `game:checkstyleMain`, `game:checkstyleTest`
und die vollständige `game:test`-Suite mit 840 Tests bestanden. Ein interaktiver Klicktest am
laufenden Multiplayer-Host wurde nach der Korrektur nicht erneut ausgeführt.
4. `game/src/rooms/systemRecovery/story/SystemRecoveryHintPhone.java`
5. `game/src/rooms/systemRecovery/util/tracking/SystemRecoveryPuzzleEvents.java`
6. `game/src/rooms/systemRecovery/level/SystemRecoveryRiddleRegistry.java`
7. `game/src/feature/questlog/QuestLogUI.java` und `game/src/feature/entities/HeroController.java`
8. `game/src/rooms/systemRecovery/network/SystemRecoverySnapshotTranslator.java`
9. `game/src/rooms/systemRecovery/util/SystemRecoveryTranslator.java` und
   `game/assets/language/systemRecovery/{de,en}.json`
10. `SystemRecoveryProgressFlowTest`, `SystemRecoveryKeypadProgressTest` und `QuestLogUITest`
