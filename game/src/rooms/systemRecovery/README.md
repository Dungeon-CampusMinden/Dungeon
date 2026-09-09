# System Recovery: Codeübersicht

## Einstieg

`SystemRecoveryLevel` baut das Level in Raumreihenfolge auf. Die eigentliche Rätsellogik
liegt in `riddles`: Jede Klasse hält Aufbau, Zustand, Interaktionen und zeitgesteuerte
Aktionen ihres Rätsels zusammen. Die Instanzen gehören einem Level, nicht einer
statischen, über mehrere Levelstarts hinweg geteilten Sitzung.

## Rätsel und Custom Points

| Raum | Klasse | Aufgaben | Wichtige Custom Points |
| --- | --- | --- | --- |
| 1 – Materialisierungskammer | `EnergyRiddle` | Energiecontainer erzeugen, Füllstände setzen, Batterie einmalig freigeben, Batteriebox | `a0`–`a4`, `array_lever`, `array_item_spawn`, `batteriebox_modul` |
| 2 – Modulspeicher | `ModuleStorageRiddle` | Sockel aktivieren und belegen, GPU entfernen, Länge anzeigen, Chips zum Scanner bewegen | `s0`–`s4`, `display_room2`, `room3_keypad` |
| 3 – Inventarscanner | `InventoryScannerRiddle` | Hebel nach Terminaleingabe freigeben, Scan durchführen, Ergebnis anzeigen | `scanner0`–`scanner4`, `scanner_display`, `scanner_lever`, `scanner_terminal`, `keypad_transportlager` |
| 4 – Transportlager | `TransportStorageRiddle` | Förderband und Pakete, sequenzielles Einsammeln, Tür zum Datenspeicher | `band_start`, `band_ende`, `band0`–`band4`, `lager_terminal`, `door_datenspeicher` |
| 5 – Datenspeicher | `ManualSortingRiddle` | Benachbarte Werte vergleichen, bei Fehler zurücksetzen, leeren USB-Stick vergeben | `sort_data_0`, `sort_data1`–`sort_data4`, `sort_compare_display`, `sort_trigger`, `chip_spawn` |
| 6 – Bubble-Sort-Maschine | `BubbleSortRiddle` | Programmierten Stick annehmen, Pakete auf dem Lagerband sortieren, Archivschlüssel vergeben | `sort_machine`, gemeinsam genutzte `band0`–`band4` |

Die abweichenden Schreibweisen `sort_data_0` und der alte Förderband-Endpunkt `baned_end`
werden weiterhin unterstützt. Der Archiv-Türverschluss bleibt beim Levelaufbau
(`door_datenarchiv`), die Schlüsselbelohnung beim Bubble-Sort-Rätsel.

## Terminal und USB-Stick

- `util.interpreter.TerminalInterpreterSetup`: Pattern und Steps in Spielreihenfolge.
- `util.interpreter.InterpretationCallbacks`: Zuordnung akzeptierter Steps zu den Rätseln,
  sowie Dialog- und Soundfeedback. Noch nicht ausgebaute Räume behalten ihre TODOs hier.
- `modules.interpreter`: Allgemeine Prüfung des Codes; keine Abhängigkeit zur Spiellogik.
- `modules.computer`: Gemeinsamer Computer für alle Räume, inklusive Editor und USB-Dialog.
- `modules.computer.content.SortProgramTab`: Lückencode zum Programmieren des Sortierchips.
- `items.SortProgramStickItem`: Leerer bzw. programmierter Stick, inklusive Netzwerkdaten.

## Entitäten und Multiplayer

`entities.EntityFactory` baut wiederverwendbare Objekte, besitzt aber keinen
Rätselfortschritt. Zustandsänderungen kommen aus den Rätselklassen.

Der Server entscheidet über Fortschritt, Belohnungen, Türen und zeitgesteuerte Abläufe.
`network.SystemRecoverySnapshotTranslator` und `SystemRecoveryEntitySpawnStrategy`
übertragen die Zustände. Vergleichspaare und Paketwerte bleiben Teil des bisherigen
Metadatenformats; Clients stellen daraus Farben und Markierungen dar.
Renderingeffekte benötigen weiterhin einen eigenen Client-Pfad, keine Texturladevorgänge
auf dem Headless-Server.

`SystemRecoveryLevel` behält schlanke statische Weiterleitungen für bestehende
Interpreter-Callbacks und Snapshot-Abfragen. Diese arbeiten mit den Rätselinstanzen
des aktuell aktiven Levels. Neue Spiellogik gehört in die betreffende Rätselklasse,
nicht in diese Weiterleitungen.

## Tests

Die Interpreter- und Szenariotests liegen unter `game/test/rooms/systemRecovery`.
`riddles/ManualSortingRiddleTest` prüft zusätzlich den tatsächlichen Raumcontroller:
Vergleichsfortschritt, Tauschpositionen inklusive Collider, Neustart nach Fehlern,
einmalige Belohnung und voneinander unabhängige Rätselinstanzen.

```sh
./gradlew :game:test --tests 'rooms.systemRecovery.*'
```
