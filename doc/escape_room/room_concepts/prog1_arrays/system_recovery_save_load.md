# System Recovery: Save und Load

## Prinzip

System Recovery verwendet **einen automatischen Checkpoint am Anfang jedes Haupträtsels**,
keinen frei wählbaren Spielstand und kein Replay aller bisherigen Eingaben. Der autoritative
Server schreibt `system-recovery-save.json` im Arbeitsverzeichnis. Im Hauptmenü erscheint
`Load Game`, wenn ein lesbarer Spielstand vorliegt.

| Rätsel | Wiederhergestellter Start-Place |
| --- | --- |
| 1 Energie | `ENERGY_ARRAY` |
| 2 Module | `MODULE_ARRAY` |
| 3 Scanner | `INVENTORY_COUNT` |
| 4 Transport | `TRANSPORT_ARRAY` |
| 5 Manuelle Sortierung | `MANUAL_SORTING` |
| 6 Bubble Sort | `BUBBLE_SORT_CONDITION` |
| 7 Archivzugang | `ARCHIVE_ACCESS` |
| 8 2D-Speicher | `STORAGE_ARRAY` |
| 9 Suchroboter | `SEARCH_PROGRAM` |
| 10 Systemkern | `SYSTEM_CORE_ACCESS` |

Ein Fortschritt **innerhalb** des aktuellen Rätsels ist kein neuer Checkpoint. Wer beispielsweise
im Systemkern bereits zwei Prüfungen löst und dann beendet, beginnt beim Laden wieder vor dem
Zugangsskript. Erst der nächste Haupträtsel-Place wird zum neuen Checkpoint.

## Was gespeichert wird

- Stabile Kennung des Checkpoints (Petri-Place).
- Bisher akzeptierte Terminaleingaben mit ursprünglichem Quelltext. So bleiben auch frei
  gewählte Arraynamen für spätere Prüfungen erhalten.
- Gemeinsames Questlog einschließlich der Einträge und privaten Notizen von Spielern.
- Laufzeit-Fortschritt der Achievements, etwa Fehlversuche und bereits vergebene Erfolge.

Die dauerhaften Achievement-Unlocks liegen zusätzlich in
`system-recovery-achievement-unlock.json` und werden von der Achievement-Verwaltung
getrennt gespeichert. Der `Memory Watch` wird aus den akzeptierten Eingaben wieder aufgebaut.
Gespeichert wird serverseitig bei einem neuen Haupträtsel-Place sowie bei Änderungen an
Questlog oder Achievement-Fortschritt, **solange** ein solcher Checkpoint aktiv ist.

## Wie geladen wird

1. `SystemRecoveryLoad` prüft Formatversion, Checkpoint und die erwartete Folge akzeptierter
   Terminal-States. Ein fehlender oder ungültiger Spielstand wird nicht als Fortsetzung angeboten.
2. Das Level erzeugt die gewöhnlichen Entitäten. Danach setzt `restoreRuntime` genau einen
   Token auf den Checkpoint-Place, stellt Interpreterkontext, Questlog und Achievement-Fortschritt
   wieder her.
3. `SystemRecoveryLevel.restoreWorldAtCheckpoint` stellt die bereits abgeschlossenen Räume,
   Türen und benötigten Items direkt her. Rätsel-Callbacks und Storydialoge werden dabei
   **nicht erneut abgespielt**.
4. Ab dem Checkpoint spielt man das aktuelle Rätsel wieder von Anfang an. Für Checkpoints nach
   dem Intro sind die Terminals sofort nutzbar.

Die gespeicherten Questlog-Einträge werden direkt geladen; Dialoge werden nicht im Hintergrund
nachgespielt. Das Petri-Netz speichert also **nicht allein** den vollständigen Spielzustand:
Interpreterhistorie, Questlog und Achievement-Fortschritt gehören ebenfalls zum Save.

## Bekannte Grenze

Der gemeinsame Hinweiszähler des `HintSystem` wird beim Levelstart zurückgesetzt und ist nicht
Teil des Save-Formats. Bereits gelesene Hinweise stehen weiter im geladenen Questlog, aber das
Telefon kann beim neu begonnenen aktiven Rätsel wieder mit dessen erstem Hinweis anfangen.
Questlog-/Achievement-Änderungen während eines begonnenen Teilschritts nach dem letzten
Checkpoint werden ebenfalls erst mit einem neuen speicherbaren Zustand dauerhaft.

## Zuständigkeiten

- `save/SystemRecoverySave.java`: Snapshot und atomisches JSON-Schreiben (Formatversion 2).
- `save/SystemRecoveryLoad.java`: Validierung und stille Wiederherstellung des Runtime-Zustands.
- `level/SystemRecoveryLevel.java`: Zeitpunkt des Autosaves und Projektion der Spielwelt.
- `petrinet/SystemRecoveryProgressNet.java`: aktiver Token; siehe
  [Petri-Netz](petri_net_system_recovery_concept.md).
