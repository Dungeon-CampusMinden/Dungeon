# System Recovery: Save und Load

## Prinzip

System Recovery speichert automatisch den aktiven Petri-Place und den dazu passenden Spielzustand.
Es gibt keinen frei wählbaren Spielstand und kein nachträgliches Replay der Dialoge. Der
autoritative Server schreibt `system-recovery-save.json` ins Arbeitsverzeichnis; das Hauptmenü
bietet `Fortsetzen` an, wenn die Datei gültig ist.

Das aktuelle JSON-Format ist Version 9.

| Bereich | Checkpoints |
| --- | --- |
| 1 Energie | `MODULE_ARRAY` nach dem Einsetzen der Batterie |
| 2 Module | `INVENTORY_COUNT` |
| 3 Inventarscanner | `TRANSPORT_ARRAY` |
| 4 Transportlager | `MANUAL_SORTING` |
| 5 Manuelle Sortierung | `BUBBLE_SORT_CONDITION` und direkt nach erfolgreichem USB-Upload `BUBBLE_SORT_MACHINE` |
| 6 Bubble Sort | `ARCHIVE_ACCESS` nach Abschluss der Maschine |
| 7 Archiv | `ARCHIVE_ACCESS`, nach dem Öffnen der Tür zusätzlich `ARCHIVE_ARRAYS` |
| 8 2D-Speicher | `STORAGE_ARRAY` |
| 9 Suchroboter | `SEARCH_PROGRAM` und direkt nach erfolgreichem Chip-Upload `SEARCH_ROBOT_RUN`; nach der Lieferung des Zugangschips `SYSTEM_CORE_ACCESS` |
| 10 Systemkern | `CORE_SORT`, `CORE_COUNT`, `CORE_SEARCH`, `CORE_SEARCH_ROBOT`, `CORE_META` und `COMPLETE` |

Die erste automatische Speicherung entsteht nach Rätsel 1 beim Übergang zu `MODULE_ARRAY`.
Wird ein programmierter Stick aus dem Inventar in einen Rechner, die Sortiermaschine oder den
Suchroboter eingesetzt, wird er für den Snapshot als `gemountet` erfasst. Ein geladener
Zwischenstand legt ihn ins Inventar zurück, damit der Verarbeitungsschritt erneut gestartet werden
kann. Beim erfolgreichen Abschluss wird er vor dem nächsten Checkpoint aus diesem Zustand
entfernt; nach dem Verbrauch wird er daher nicht erneut vergeben. Das gespeicherte Item ist
run- und spielerbezogen und wird nicht aus einem alten Snapshot in ein leeres Inventar kopiert.

Der Zugangschip zum Systemkern wird schon im Place `SYSTEM_CORE_ACCESS` gesichert, sobald er
aufgehoben wurde. Wird er benutzt, wechselt das Netz zu `CORE_SORT`; ab dort wird der verbrauchte
Chip nicht wiederhergestellt.

## Gespeicherte Daten

- Aktiver Petri-Place und akzeptierte Terminal-Quelltexte samt Interpreterzustand. Chip-Editor und
  finale Eingabemaske sind keine normalen Terminaleingaben; ihre Zustände dürfen beim Laden
  übersprungen werden, erforderliche Terminalschritte jedoch nicht.
- Terminal-History exakt in der Reihenfolge, in der sie im UI angezeigt wird.
- Memory Watch mit Arraynamen, Datentypen und bekannten Zellinhalten.
- Questlog einschließlich Dialogen, Hinweisen und eigenen privaten oder gemeinsamen Notizen.
- Rätselrelevante Inventargegenstände pro Spielername: Sortierstick und Ortungschip mit
  Programmierstatus und Entwurf, Systemkern-Zugangsmodul sowie Archivschlüssel. Kurzzeitig in
  Rechner oder Maschine eingesetzte Sticks werden als gemountete Items aufgenommen.
- Aktive Spielzeit `activeMs`, Run-ID, Spielername, Tracking-Einwilligung, Achievement-Fortschritt und Zustand der
  Systemkern-Ausgangstür.

Achievement-Unlocks liegen zusätzlich in `system-recovery-achievement-unlock.json`.

## Laden

1. `SystemRecoveryLoad` prüft Formatversion, Checkpoint, Zahl und Reihenfolge akzeptierter
   Terminaleingaben. Fehlende normale Code-Schritte machen den Save ungültig; nicht-terminale
   Eingabeflächen dürfen übersprungen werden. Ein Upload-Checkpoint ist außerdem nur gültig,
   wenn der dazugehörige programmierte Stick im Save enthalten ist.
2. Der Server stellt Interpreterkontext, Petri-Markierung, Questlog, History, Memory Watch und
   gespeicherte Puzzle-Items wieder her.
3. Die Checkpoint-Projektion rekonstruiert Türen, abgeschlossene Räume, Displays, Roboter und
   Items ohne Rätsel-Callbacks oder frühere Dialoge erneut auszuführen.
4. Ein beim Laden noch nicht verbundener Item-Besitzer wird anhand des gespeicherten Spielernamens
   nachträglich gesucht. Ein wiederhergestellter Gegenstand wird nicht zusätzlich in der Welt
   gespawnt.
5. Ein gespeicherter laufender Suchroboter beginnt seinen Scan kontrolliert von vorn. Der
   jeweilige programmierte Chip bleibt dafür im Inventar.
6. Der Countdown läuft mit der gespeicherten Spielzeit `activeMs` weiter. Zeit außerhalb des
   laufenden Spiels wird nicht abgezogen.

Ungesendeter Terminalcode und unvollständige Werte in der R10-Maske sind client-lokale Entwürfe.
Sie bleiben beim bloßen Schließen und erneuten Öffnen des jeweiligen Fensters erhalten, werden
aber nicht ins Savegame geschrieben und beim Laden eines Levels (auch bei `Fortsetzen`) verworfen.
So gelangen unfertige, nicht serverbestätigte Eingaben weder in den Spielstand noch auf einen
anderen Client.

Bei alten Save-Formatversionen werden History und Memory Watch, soweit möglich, aus den
akzeptierten Terminaleingaben rekonstruiert. Neue Saves speichern beide Ansichten direkt.

## Restzeit

Wie der Countdown läuft und was bei seinem Ablauf passiert, beschreibt der
[Storyablauf](system_recovery_story_flow.md#zeitlimit-und-hilfe). Für Save und Load gilt:

- Die Restzeit ist die Stunde abzüglich der gespeicherten Spielzeit `activeMs`; ein eigenes Feld
  gibt es nicht. Der Countdown selbst schreibt keine Datei. `Fortsetzen` beginnt daher mit der
  Restzeit des letzten Saves.
- Der Zeitablauf verändert den Save nicht. `Fortsetzen` startet danach erneut am letzten
  Checkpoint mit der dort gespeicherten Spielzeit.
- Die Wartezeit für automatische Hilfen startet beim Laden neu.

## Grenzen

Der Hinweiszähler des `HintSystem` wird beim Levelstart zurückgesetzt; bereits gelesene Hinweise
bleiben im Questlog erhalten. Ein erneutes Laden des aktiven Rätsels kann daher dessen Hinweise
wieder von vorn anbieten. Multiplayer-Inventare werden über den gespeicherten Spielernamen
zugeordnet; bei mehreren gleichzeitig verbundenen Spielern mit identischem Namen ist die
Zuordnung nicht eindeutig.

## Zuständigkeiten

- `save/SystemRecoverySave.java`: Snapshot, Formatversion 9 und atomisches JSON-Schreiben.
- `save/SystemRecoveryLoad.java`: Validierung und stille Runtime-Wiederherstellung.
- `level/SystemRecoveryLevel.java`: Autosave-Auslöser, Item-Recovery und Weltenprojektion.
- `level/SystemRecoveryCheckpointProjection.java`: rekonstruiert den Raum für den Checkpoint.
- `petrinet/SystemRecoveryProgressNet.java`: aktiver Token; siehe
  [Petri-Netz](petri_net_system_recovery_concept.md).
- `time/SystemRecoveryTimeLimit.java`: Restzeit und Hilfefrist auf der gemeinsamen Spieluhr.
