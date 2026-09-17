# System Recovery: Codeübersicht

## Einstieg

`SystemRecoveryLevel` baut das Level in Raumreihenfolge auf. Die eigentliche Rätsellogik
liegt in `riddles`: Jede Klasse hält Aufbau, Zustand, Interaktionen und zeitgesteuerte
Aktionen ihres Rätsels zusammen. Die Instanzen gehören einem Level, nicht einer
statischen, über mehrere Levelstarts hinweg geteilten Sitzung.

## Rätsel und Custom Points

| Raum | Klasse | Aufgaben | Wichtige Custom Points |
| --- | --- | --- | --- |
| 1 – Materialisierungskammer | `EnergyRiddle` | Energiecontainer erzeugen, Füllstände setzen, Batterie einmalig freigeben, Batteriebox | `a0`–`a4`, `array_lever`, `array_item_spawn`, `batteriebox_modul`, `display_energie` |
| 2 – Modulspeicher | `ModuleStorageRiddle` | Sockel aktivieren und belegen, GPU entfernen, Länge anzeigen, Chips zum Scanner bewegen | `s0`–`s4`, `display_room2`, `room3_keypad` |
| 3 – Inventarscanner | `InventoryScannerRiddle` | Hebel nach Terminaleingabe freigeben, Scan durchführen, Ergebnis anzeigen | `scanner0`–`scanner4`, `scanner_display`, `scanner_lever`, `scanner_terminal`, `keypad_transportlager` |
| 4 – Transportlager | `TransportStorageRiddle` | Förderband und Pakete, sequenzielles Einsammeln, Tür zum Datenspeicher | `band_start`, `band_ende`, `band0`–`band4`, `lager_terminal`, `display_storage`, `door_datenspeicher` |
| 5 – Datenspeicher | `ManualSortingRiddle` | Benachbarte Werte vergleichen, bei Fehler zurücksetzen, leeren USB-Stick vergeben | `sort_data_0`, `sort_data1`–`sort_data4`, `sort_compare_display`, `sort_trigger`, `dialog_trigger_manual_sorting`, `chip_spawn` |
| 6 – Bubble-Sort-Maschine | `BubbleSortRiddle` | Programmierten Stick annehmen, Pakete auf dem Lagerband sortieren, Archivschlüssel vergeben | `sort_machine`, gemeinsam genutzte `band0`–`band4` |
| 7 – Datenarchiv | `DataArchiveRiddle` | Array-Hinweise aus Regalen lesen, Datenknoten untersuchen, Archivzugang öffnen | `archive_shelf_energie`, `archive_shelf_module`, `archive_shelf_aktiv`, `archive_node0`–`archive_node2`, `door_speicher` |
| 8 – Zweidimensionaler Speicher | `TwoDimensionalStorageRiddle` | Eine 3×4-Matrix anlegen, drei Koordinaten befüllen, Datenobjekte materialisieren, Ortungschip freigeben | `storage_cell_0_0`–`storage_cell_2_3`, `display_2d`, `chip` |
| 9 – Suchroboter | `SearchRobotRiddle` | Ortungschip programmieren, Matrix-Endpunkt mit dem Roboter erreichen, Zugriffsmodul erhalten | `suchroboter`, `suchroboter_controller` bzw. `suchroboter_controlls`, `roboter_start`, `roboter_end`, `roboter_item_destination` bzw. `roboter_item_destionation` |
| 10 – Rechenzentrum | `SystemCoreRiddle` + `TerminalInterpreterSetup` | Drei Bereiche im zentralen Terminal prüfen und anschließend ihre Ergebnisse als gemeinsamen Systemzustand eingeben: Bubble Sort, belegte Module zählen und ein 3×5-Raster durchsuchen | `core_terminal`, `core_display`, `b0`–`b4`, `mod0`–`mod4`, `map00`, `map24`, `door_elevator` |

Die frei positionierbaren Story-Trigger heißen `dialog_trigger_*` und liegen aktuell an den
jeweiligen Türen oder Rätselräumen. Der Sortierdialog verwendet
`dialog_trigger_manual_sorting`; `sort_trigger` bleibt für den Sortieraufbau reserviert. Die
Zuordnung zu den Dialogschritten ist in
`story.SystemRecoveryDialogTriggers` gebündelt. Die Punkte können im Level Editor unabhängig
von den Türen verschoben werden. Jeder Trigger wird pro Spieler nur einmal ausgelöst.

Die abweichenden Schreibweisen `sort_data_0`, `baned_end`, `storage_2_2`,
`suchroboter_controlls` und `roboter_item_destionation` werden weiterhin unterstützt. Rätsel 8 und Rätsel 9 verwenden
bewusst getrennte Matrizen: Die Speicherzellen von Rätsel 8 bilden ein 3x4-Raster. Die Matrix
von Rätsel 9 wird zur Laufzeit aus `roboter_start` und `roboter_end` als inklusiver Laufweg erzeugt.
Beim Levelstart prüft `level.SystemRecoveryPointRegistry` alle Pflichtpunkte einmal und meldet
fehlende Punkte gesammelt mit Rätsel und Namen. Die drei historischen Tippfehler werden dabei
noch als Übergangs-Aliase aufgelöst.
Der Archiv-Türverschluss bleibt beim Levelaufbau
(`door_datenarchiv`), die Schlüsselbelohnung beim Bubble-Sort-Rätsel.

## Terminal und USB-Stick

- `util.interpreter.TerminalInterpreterSetup`: Pattern und Steps in Spielreihenfolge.
- `util.interpreter.InterpretationCallbacks`: einheitliches Feedback für akzeptierte und
  abgelehnte Eingaben.
- `level.SystemRecoveryRiddleRegistry`: Zuordnung eines benannten Terminal-Schritts zur
  zuständigen Rätselinstanz und zur direkt folgenden Storyaktion.
- `modules.interpreter`: Allgemeine Prüfung des Codes; keine Abhängigkeit zur Spiellogik.
- `modules.computer`: Gemeinsamer Computer für alle Räume, inklusive Editor und USB-Dialog.
- `modules.computer.content.SortProgramTab`: Lückencode zum Programmieren des Sortierchips.
- `items.SortProgramStickItem`: Leerer bzw. programmierter Stick, inklusive Netzwerkdaten.

Das finale Rechenzentrum wird von `riddles.SystemCoreRiddle` aufgebaut. Die Klasse spawnt den
zentralen Rechner, ein gemeinsames Display und die drei visuellen Arbeitsbereiche. Nach deren
Abschluss zeigt das Display die drei Ergebnisse und aktiviert die vierte, finale
Eingabemaske im Terminal. Der Spieler überträgt daraus `sortedEnergy`, `activeModules` und
die Zahl der erfassten Rastermodule; erst dann öffnet sich der Aufzug und der Alarm wird deaktiviert. Die
Interpreter-Callbacks ändern nur die aktuelle Präsentationsstufe; die Codeprüfung bleibt in
`TerminalInterpreterSetup`.

## Story-Dialoge

`story.SystemRecoveryStoryDialogs` enthält die Anweisungen des Remote Users. Die erste
Nachricht erscheint erst, wenn ein Spieler den beschädigten Energie-Riegel betätigt. Danach
wird immer nur die nächste konkrete Aktion erklärt: Ein erfolgreicher Terminal-Schritt oder
eine abgeschlossene Weltsequenz löst den unmittelbar folgenden Dialog aus. Spätere Schritte
eines Rätsels werden dabei nicht vorweggenommen. Bei gemeinsam gelösten Weltsequenzen erhalten
alle verbundenen Spieler die Nachricht; bei einer Terminal-Eingabe wird sie an den einreichenden
Spieler adressiert. Der Level-Editor löst keine Story-Dialoge aus.

`util.SystemRecoveryQuestLogUtil` initialisiert das serverseitige Questlog beim ersten Level-Tick.
Jeder Story-Dialog legt genau einen Eintrag in seinem eigenen Tab an: von „Rätsel 1:
Energieversorgung“ bis „Rätsel 10: Rechenzentrum“. Das Questlog wird zusammen mit dem übrigen
System-Recovery-Zustand an die Clients synchronisiert.

## Entitäten und Multiplayer

Die spezialisierten Factories unter `entities` bauen wiederverwendbare Objekte, besitzen aber
keinen Rätselfortschritt. `EnergyEntityFactory`, `ModuleEntityFactory`, `TransportEntityFactory`,
`SortingEntityFactory`, `StorageEntityFactory`, `ScannerEntityFactory`, `SystemCoreEntityFactory`,
`ArchiveEntityFactory` und `SystemRecoveryDisplayFactory` halten jeweils nur die Konstruktion
ihres Bereichs. Zustandsänderungen kommen aus den Rätselklassen.

Der Server entscheidet über Fortschritt, Belohnungen, Türen und zeitgesteuerte Abläufe.
`network.SystemRecoverySnapshotTranslator` und `SystemRecoveryEntitySpawnStrategy`
übertragen die Zustände. Vergleichspaare und Paketwerte bleiben Teil des bisherigen
Metadatenformats; Clients stellen daraus Farben und Markierungen dar.
Renderingeffekte benötigen weiterhin einen eigenen Client-Pfad, keine Texturladevorgänge
auf dem Headless-Server.

Türbeschriftungen besitzen eine `DoorLabelComponent`. Ihre Bedingung wird in
`SystemRecoveryLevel.setupRoomLabel` dem tatsächlich abgeschlossenen Rätsel des
beschrifteten Raums zugeordnet: Das Schild `Inventarscanner` wird erst nach dem
vollständigen Scan grün, `Transportlager` erst nach dem Einsammeln aller Pakete und
`Datenspeicher` erst nach der manuellen Sortierung. Ungelöst ist rot, abgeschlossen
ist grün. Das ersetzt keine Schlüssel- oder Keypadprüfung.
Die Metadaten werden sowohl beim Spawn als auch in Snapshots übertragen. Der
`DisplayTextStatusShader` färbt ausschließlich die grün/türkise Schrift der Textur
rot; nach Abschluss bleibt sie im originalen Grün. Gehäuse und Hintergrund bleiben
unverändert. Der Effekt wird ausschließlich auf grafischen Clients angewendet.

Die Bubble-Sort-Maschine fragt vor dem Einsetzen des programmierten USB-Sticks nach.
Erst nach „Einsetzen“ prüft sie erneut Inventar und Maschinenstatus und verbraucht
den Stick. „Abbrechen“ verändert weder Inventar noch Rätselzustand.

`SystemRecoveryLevel` enthält den Level-Lebenszyklus und einige rückwärtskompatible statische
Weiterleitungen für Snapshot-Abfragen und physische Interaktionen. Terminal-Schritte laufen
über `SystemRecoveryRiddleRegistry` und werden nicht mehr einzeln in der Levelklasse verdrahtet.
Neue Spiellogik gehört in die betreffende Rätselklasse, nicht in die Weiterleitungen.

## Fortschritt, Tracking und Petri-Netz

Rätsel-Controller melden ausschließlich semantische Ereignisse über
`riddles.support.RiddleCallbacks`: `success`, `failure` und `solved`. Die Zuordnung zu stabilen
Rätsel-IDs und die Weiterleitung an das serverseitige Tracking liegen zentral in
`util.tracking.SystemRecoveryPuzzleEvents`. Auch Terminal-Eingaben und Computer-Programme
verwenden diesen Einstiegspunkt. Dadurch muss eine neue Tracking- oder Analyse-Integration
nicht in jedem Rätsel angepasst werden.

Das Petri-Netz ist als serverseitiges Fortschrittsmodell aktiviert. Jeder aktive
Fortschritts-Place trägt einen `HintComponent`; das Hinweistelefon liest daraus den nächsten
gemeinsamen Hinweis. `SystemRecoveryProgressNet` hält genau einen aktiven Fortschritts-Token und
akzeptiert Events nur aus dem erwarteten Place. Die Rätsel selbst bleiben die einzige Quelle für
fachliche Lösungen, Items, Türen und Animationen. Tracking und Fortschrittsnetz sind über
`util.tracking.SystemRecoveryPuzzleEvents` an klaren Ereignisgrenzen angebunden.

## Tests

Die Interpreter- und Szenariotests liegen unter `game/test/rooms/systemRecovery`.
`riddles/ManualSortingRiddleTest` prüft zusätzlich den tatsächlichen Raumcontroller:
Vergleichsfortschritt, Tauschpositionen inklusive Collider, Neustart nach Fehlern,
einmalige Belohnung und voneinander unabhängige Rätselinstanzen.

```sh
./gradlew :game:test --tests 'rooms.systemRecovery.*'
```
