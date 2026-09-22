# System Recovery: Petri-Netz

## Zweck

Das Petri-Netz bildet den gemeinsamen Lernfortschritt einer laufenden System-Recovery-Partie ab.
Es beantwortet genau eine Frage: **Welcher Schritt ist als Nächstes zu lösen?** Fachliche
Codeprüfungen, Türen, Items und Animationen bleiben in den jeweiligen Rätselklassen.

`SystemRecoveryProgressNet` läuft ausschließlich auf dem autoritativen Server. Deshalb teilen sich
alle Spieler denselben Fortschritt und dieselbe Hinweisfolge.

## Modell

Jeder Eintrag in `SystemRecoveryLearningStep` besitzt einen ECS-Place. In einer stabilen Markierung
liegt genau ein Token auf genau einem Place. Jeder Lern-Place außer `COMPLETE` enthält außerdem
ein `HintComponent` mit vier Hinweisen: Orientierung, Ansatz, beinahe Lösung und Lösung.

Beim erfolgreichen Abschluss eines Schritts erzeugt `complete(expectedStep)` kurz ein zweites
Token im aktiven Place. Die zugehörige Transition verbraucht beide Tokens und erzeugt ein Token im
direkten Nachfolge-Place. Eine falsche Reihenfolge, ein doppelter Callback oder eine ungültige
Markierung verändert das Netz nicht. Schlägt eine Transition fehl, wird die vorherige Markierung
wiederhergestellt.

```text
[aktiver Place: 1 Token]
          + erfolgreicher Callback
                    |
                    v
[aktiver Place: 2 Tokens] -- Transition (Gewicht 2) --> [nächster Place: 1 Token]
```

## Schrittfolge

| Rätsel | Places in Reihenfolge | Erfolgsquelle |
| --- | --- | --- |
| 1 Energie | `ENERGY_ARRAY` -> `ENERGY_VALUES` -> `ENERGY_INSERT_BATTERY` | Terminal, Terminal, eingesetzte Batterie |
| 2 Module | `MODULE_ARRAY` -> `MODULE_VALUES` -> `MODULE_REMOVE_GPU` -> `MODULE_LENGTH` -> `ROOM2_DOOR_CODE` | Terminalschritte, danach korrektes Keypad |
| 3 Scanner | `INVENTORY_COUNT` -> `ROOM3_DOOR_CODE` | Terminal, danach korrektes Keypad |
| 4 Transport | `TRANSPORT_ARRAY` -> `TRANSPORT_COLLECT_LOOP` -> `DATA_STORAGE_DOOR_OPEN` | Terminalschritte, danach ECHOs Anruf beantworten und Tür öffnen |
| 5 Manuell sortieren | `MANUAL_SORTING` | vollständig richtige Vergleichsfolge |
| 6 Bubble Sort | `BUBBLE_SORT_CONDITION` -> `BUBBLE_SORT_MACHINE` | programmierter Stick, danach Maschinenlauf |
| 7 Archiv | `ARCHIVE_ACCESS` -> `ARCHIVE_ARRAYS` | Archivschlüssel, danach Terminal |
| 8 2D-Speicher | `STORAGE_ARRAY` -> `STORAGE_VALUES` | Terminalschritte |
| 9 Suchroboter | `SEARCH_PROGRAM` -> `SEARCH_ROBOT_RUN` | programmierter Chip, danach Suchlauf |
| 10 Systemkern | `SYSTEM_CORE_ACCESS` -> `CORE_SORT` -> `CORE_COUNT` -> `CORE_SEARCH` -> `CORE_SEARCH_ROBOT` -> `CORE_META` -> `COMPLETE` | Zugriffsskript, drei Terminalschritte, danach zweiter Suchroboter und Abschlussmaske |

Terminalerfolge werden in `SystemRecoveryPuzzleEvents.terminalAttempt(...)` anhand von
`TerminalStep` eindeutig einem Place zugeordnet. Physische Aktionen nennen ihren erwarteten Place
direkt. Raumpositionen und Story-Trigger schalten das Petri-Netz niemals.
Das Öffnen des Systemkerns schließt `SYSTEM_CORE_ACCESS` ab und startet Alarm sowie ECHOs
Warnanruf. Nach `CORE_META` liegt der Token bereits auf `COMPLETE`; ECHOs letzter Anruf öffnet
anschließend den Aufzug, ohne einen weiteren Place zu schalten.

## Hinweise und Tracking

Das Telefon liest den aktiven Place und lässt `HintSystem` den nächsten gemeinsamen Hinweis
anbieten. Erst nach Bestätigung wird der Hinweis verbraucht, im passenden Questlog-Tab gespeichert
und über `SystemRecoveryPuzzleEvents.hintUsed(...)` getrackt. Das Lesen eines Hinweises verändert
keinen Progress-Token.

`SystemRecoveryPuzzleEvents` ist die gemeinsame Grenze für Tracking und Fortschritt:

- Versuche werden als richtig oder falsch getrackt.
- Ein erfolgreicher Terminalschritt darf genau einen explizit zugeordneten Place abschließen.
- `solved(...)` markiert ein Rätsel nur fürs Tracking und schaltet nicht automatisch weiter.
- `started(...)` wird beim Wechsel in das nächste fachliche Rätsel erfasst.

## Invarianten und Debugging

Nach jedem Tick müssen diese Bedingungen gelten:

1. Genau ein Place besitzt genau ein Token.
2. Alle anderen Places besitzen null Tokens.
3. `COMPLETE` ist der einzige Endzustand.
4. Wiederholte oder verspätete Callbacks verändern die Markierung nicht.
5. Hinweise gehören zum aktuell markierten Lern-Place; `COMPLETE` hat keine Hinweise.

Die Debug-Anzeige verwendet `SystemRecoveryProgressNet.debugSnapshot()`. Sie zeigt die
Tokenanzahl jedes Places, den aktiven Schritt, Interpreterzustand, Hinweisfortschritt und den
letzten akzeptierten oder abgelehnten Übergang. Ein inkonsistenter Snapshot ist ein Fehler und
darf nicht durch einen zweiten Zustandswert kaschiert werden.

## Checkpoints

Save/Load speichert **nicht** jede Markierung. Gespeichert wird nur der Anfangs-Place eines
Haupträtsels. Beim Laden wird dieser Place als einzig aktiver Token wiederhergestellt; die
bereits abgeschlossenen Räume werden ohne Rätsel-Callbacks in ihren fertigen Zustand gesetzt.
Ein abgebrochener Teilschritt des aktuellen Rätsels wird erneut gespielt. Insbesondere gibt es
keinen eigenen Checkpoint innerhalb des Systemkerns. Details stehen in
[Save/Load](system_recovery_save_load.md).

## Wichtige Dateien

- `petrinet/SystemRecoveryLearningStep.java`: Reihenfolge und Zuordnungen
- `petrinet/SystemRecoveryProgressNet.java`: Places, Transitionen und Invarianten
- `petrinet/SystemRecoveryHintCatalog.java`: vier Hinweise pro Place
- `story/SystemRecoveryHintPhone.java`: Telefon, Bestätigung und Questlog
- `util/tracking/SystemRecoveryPuzzleEvents.java`: Tracking- und Progress-Grenze
