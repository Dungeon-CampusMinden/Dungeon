# System Recovery: Vereinfachung von Petri-Netz, Hinweisen und Questlog

Stand: Umsetzungskonzept und verbindliche Anforderungen. Dieses Dokument beschreibt noch keine
fertige Implementierung.

## 1. Ausgangsproblem

Das aktuelle Petri-Netz bildet nicht nur die eigentlichen Programmier- und Denkaufgaben ab. Es
enthält zusätzlich Places und Übergänge für technische Weltzustände wie:

- Batterie einsetzen,
- Display untersuchen,
- Türcode eingeben,
- Scannerhebel betätigen,
- Scan oder Animation abwarten,
- Pakete vollständig einsammeln,
- Chip in ein Gerät einsetzen,
- Gegenstand durch einen Roboter ausliefern lassen.

Diese Zustände sind für die Weltlogik wichtig, aber als Quelle für Hinweise ungeeignet. Ein Spieler
kann eine bekannte Anzeige überspringen oder eine Animation anders erleben als erwartet. Dann
bleibt das Token an einem Place hängen, obwohl die eigentliche Lernaufgabe längst erledigt ist. Das
Telefon liest diesen veralteten Place und liefert Hinweise für die falsche Aufgabe.

Zusätzlich existieren aktuell zwei Wahrheiten über den Fortschritt:

1. die Tokens in den echten `PlaceComponent`s,
2. das manuell gepflegte Feld `SystemRecoveryProgressNet.activeState`.

`activateAfterTransition(...)` versucht Abweichungen nachträglich zu reparieren. Diese Reparatur
verdeckt Fehler, statt eine eindeutige Zustandsquelle sicherzustellen.

## 2. Verbindliche Zieldefinition

Das Petri-Netz modelliert ausschließlich den gemeinsamen **Lernfortschritt**.

Ein Lernschritt ist eine konkrete Aufgabe, bei der die Spieler selbst eine fachliche Entscheidung
treffen oder eine Lösung eingeben. In System Recovery ist das in der Regel ein serverseitig
validierter Code-Submit. Die manuelle Sortieraufgabe ist ebenfalls ein Lernschritt, weil die Spieler
mehrfach selbst über Tauschen oder Nicht-Tauschen entscheiden.

Das Petri-Netz modelliert ausdrücklich nicht:

- rein technische Türzustände ohne eigenen nächsten Spielerweg,
- Displays oder das Lesen eines Displays,
- Lever,
- beliebige Inventartransfers,
- das bloße Erzeugen oder Ausgeben von Items,
- Animationen und Wartezeiten,
- Scannerstarts oder Scanabschlüsse,
- Paket- und Roboterbewegungen,
- Raumtrigger und Storydialoge,
- das Erreichen des Endpunkts.

Eine verpflichtende, bewusste Spielerhandlung darf dagegen ein eigener Step-Place sein, wenn der
Spieler dafür sinnvoll nach einem Hinweis fragen kann. Das Einsetzen der Batterie am Ende von
Rätsel 1 ist ein solcher Step. Ein automatisch beendeter Scan oder ein intern gezähltes Paket ist
kein Step.

Ein geöffneter Zugang darf ebenfalls als Step-Place modelliert werden, wenn er die nächste konkrete
Spielerhandlung eindeutig festlegt. Nach dem Transportlager ist `DATA_STORAGE_DOOR_OPEN` deshalb
der Step „Warte auf die Freigabe des Datenspeichers“. Das erfolgreiche Öffnen der Tür schließt den
Place ab und aktiviert das nächste Rätsel. Ein Raumtrigger ist dafür nicht erforderlich.

Die technische Weltreaktion bleibt in der jeweiligen Riddle-Klasse. Das Petri-Netz kennt nur die
erwartete Spielerhandlung. Eine erfolgreiche Aktion erzeugt das zweite Token direkt im aktiven
Aufgaben-Place.

## 3. Neues lineares Lernschritt-Netz

### 3.1 Places

Das neue Enum soll `SystemRecoveryLearningStep` heißen. Jeder Eintrag steht für genau eine
Aufgabe, für die es vier spezifische Hinweise gibt.

| Reihenfolge | Place | Spieleraufgabe | Erfolgsquelle |
| ---: | --- | --- | --- |
| 1 | `ENERGY_ARRAY` | Energie-Array deklarieren | `TerminalStep.ENERGY_ARRAY` akzeptiert |
| 2 | `ENERGY_VALUES` | fünf Energiewerte zuweisen | `TerminalStep.ENERGY_VALUES` akzeptiert |
| 3 | `ENERGY_INSERT_BATTERY` | materialisierte Batterie einsetzen | Batteriebox akzeptiert die Batterie und meldet Rätsel 1 vollständig |
| 4 | `MODULE_ARRAY` | Modul-Array deklarieren | `TerminalStep.MODULE_ARRAY` akzeptiert |
| 5 | `MODULE_VALUES` | fünf Module zuweisen | `TerminalStep.MODULE_VALUES` akzeptiert |
| 6 | `MODULE_REMOVE_GPU` | defekten GPU-Eintrag entfernen | `TerminalStep.MODULE_REMOVE_GPU` akzeptiert |
| 7 | `MODULE_LENGTH` | Länge des Modul-Arrays abfragen | `TerminalStep.MODULE_LENGTH` akzeptiert |
| 8 | `ROOM2_DOOR_CODE` | den aus der Modullänge ermittelten Türcode eingeben | Keypad akzeptiert Code 5 |
| 9 | `INVENTORY_COUNT` | belegte Module zählen | `TerminalStep.INVENTORY_COUNT` akzeptiert |
| 10 | `ROOM3_DOOR_CODE` | das Scannerergebnis als Türcode eingeben | Keypad akzeptiert Code 4 |
| 11 | `TRANSPORT_ARRAY` | Paket-Array erstellen | `TerminalStep.TRANSPORT_ARRAY` akzeptiert |
| 12 | `TRANSPORT_COLLECT_LOOP` | Pakete mit einer Schleife übergeben | `TerminalStep.TRANSPORT_COLLECT` akzeptiert |
| 13 | `DATA_STORAGE_DOOR_OPEN` | auf die Freigabe des Datenspeichers warten | `door_datenspeicher` wechselt erfolgreich auf offen |
| 14 | `MANUAL_SORTING` | alle Vergleichsentscheidungen korrekt treffen | `ManualSortingRiddle` meldet ersten Abschluss |
| 15 | `BUBBLE_SORT_CONDITION` | Bubble-Sort-Bedingung auf den Chip schreiben | Sortierchip-Editor akzeptiert die Eingabe |
| 16 | `BUBBLE_SORT_MACHINE` | programmierten Sortierchip in der Maschine ausführen | Maschine beendet die Sortierung und gibt den Archivschlüssel aus |
| 17 | `ARCHIVE_ACCESS` | Archiv mit dem Schlüssel öffnen | `door_datenarchiv` wechselt erfolgreich auf offen |
| 18 | `ARCHIVE_ARRAYS` | drei Archiv-Arrays erstellen | `TerminalStep.ARCHIVE_ARRAYS` akzeptiert |
| 19 | `STORAGE_ARRAY` | zweidimensionales Array erstellen | `TerminalStep.STORAGE_ARRAY` akzeptiert |
| 20 | `STORAGE_VALUES` | markierte Matrixwerte setzen | `TerminalStep.STORAGE_VALUES` akzeptiert |
| 21 | `SEARCH_PROGRAM` | innere Suchschleife auf dem Ortungschip ergänzen | Ortungschip-Editor akzeptiert die Eingabe |
| 22 | `SEARCH_ROBOT_RUN` | programmierten Ortungschip im Suchroboter ausführen | Suchroboter liefert das Systemkern-Zugangsmodul aus |
| 23 | `SYSTEM_CORE_ACCESS` | Zugangsmodul ausführen und Systemkern freigeben | `door_systemcore` wechselt erfolgreich auf offen |
| 24 | `CORE_SORT` | Bubble Sort im Systemkern programmieren | `TerminalStep.CENTRAL_SORT` akzeptiert |
| 25 | `CORE_COUNT` | belegte Module im Systemkern zählen | `TerminalStep.CENTRAL_COUNT` akzeptiert |
| 26 | `CORE_SEARCH` | komplette Matrix im Systemkern durchsuchen | `TerminalStep.CENTRAL_SEARCH` akzeptiert |
| 27 | `CORE_META` | drei Ergebnisse in der Eingabemaske kombinieren | `TerminalStep.SYSTEM_CORE_META` akzeptiert und öffnet den Aufzug |
| 28 | `COMPLETE` | keine weitere Lernaufgabe | Senke ohne `HintComponent` |

`OPENING_CALL_PENDING`, `R2_DISPLAY`, `R2_DOOR`, `R3_LEVER`, `R3_SCAN`,
`R3_DOOR`, `R6_STICK`, `R6_MACHINE`, `R8_CHIP`, `R9_CONTROLLER`, `R9_SCAN`, `SYSTEM_ACCESS`,
`R10_FINAL` und `ESCAPE_COMPLETE` entfallen aus dem Petri-Netz.

Der bisherige Place `R1_BATTERY` wird fachlich eindeutig in `ENERGY_INSERT_BATTERY` umbenannt. Er
beschreibt die noch ausstehende Spielerhandlung und nicht den internen Zustand der Batteriebox.

### 3.2 Bedeutung der Tokenanzahl

Jeder Aufgaben-Place verwendet seine Tokenanzahl als kleinen, eindeutigen Lebenszyklus:

- **0 Token:** Dieser Schritt ist nicht aktiv.
- **1 Token:** Dieser Schritt ist aktiv und muss gerade gelöst werden. Das Telefon liefert dafür
  Hinweise.
- **2 Token:** Die erwartete Spielerhandlung wurde erfolgreich abgeschlossen. Die ausgehende
  Transition darf feuern, verbraucht beide Token und erzeugt genau ein Token im nächsten Place.

Zwei Token sind nur ein sehr kurzer technischer Zwischenzustand innerhalb des atomaren
Fortschrittsaufrufs. Im stabilen Zustand besitzt immer genau ein Aufgaben-Place genau ein Token.

Die Tokenanzahl allein bedeutet nicht automatisch:

- dass der dazugehörige Raum betreten wurde,
- dass irgendeine nicht im Place benannte Tür offen ist,
- dass eine Animation beendet ist,
- dass das nächste Item bereits erreichbar ist.

Hinweise erklären vor allem Programmier- und Denkaufgaben. Eine zwingende Bedienhandlung darf einen
eigenen Hint-Step besitzen, wenn sie die aktuell erwartete Spieleraktivität darstellt. Rein
technische Zwischenzustände kommen weiterhin nur aus Storydialogen, Displays oder der Welt.

### 3.3 Übergänge

Das Netz bleibt streng linear. Zu jedem Place außer `COMPLETE` existiert genau eine Transition zum
nächsten Place. Jede Transition besitzt eine Eingangskante mit Gewicht 2 vom aktuellen Place und
eine Ausgangskante mit Gewicht 1 zum nächsten Place. Die vorhandene Petri-Netz-API unterstützt
dies direkt über `addInputArc(transition, place, 2)`.

Für Rätsel 1 gilt damit:

```text
ENERGY_ARRAY [2]          --consume 2 / produce 1--> ENERGY_VALUES [1]
ENERGY_VALUES [2]         --consume 2 / produce 1--> ENERGY_INSERT_BATTERY [1]
ENERGY_INSERT_BATTERY [2] --consume 2 / produce 1--> MODULE_ARRAY [1]
```

Die Batteriebox ruft nach einer erfolgreich eingesetzten Batterie lediglich
`complete(ENERGY_INSERT_BATTERY)` auf. Die Fortschrittslogik prüft, ob dieser Place aktuell genau
ein Token besitzt, erzeugt dort genau ein weiteres Token und führt das Netz einmal aus. Wie die
Batteriebox ihren Erfolg feststellt, bleibt Implementierungsdetail von `EnergyRiddle`.

Zusätzliche Event-Places wie `STEP_SOLVED`, `ENERGY_ARRAY_ACCEPTED`, `RIDDLE_1_COMPLETED` oder
`MANUAL_SORTING_COMPLETED` werden nicht angelegt. Die Gewicht-2-Kante verhindert, dass ein Place
bereits mit seinem Aktivierungs-Token weiterschaltet.

**Harte Randbedingung:** Kein Petri-Netz-Übergang darf durch die Position eines Spielers, die Nähe
zu einem Custom Point oder das Betreten eines Raumtriggers ausgelöst werden. Fortschritt entsteht
ausschließlich durch einen autoritativen Erfolgs-Callback des zuständigen Systems, beispielsweise
eine akzeptierte Terminaleingabe, einen richtigen Keypadcode, eine erfolgreiche Itemaktion, einen
abgeschlossenen Rätselcontroller oder den bestätigten Zustandswechsel einer Tür auf offen.

Positionsbasierte Trigger dürfen weiterhin optionale Storydialoge pro Spieler anzeigen. Sie dürfen
weder `complete(...)` aufrufen noch Token, aktiven Hint-Step oder Terminal-State verändern.

Es gibt keine Events mit Namen wie:

- `DISPLAY_INSPECTED`,
- `DOOR_OPENED`,
- `LEVER_PULLED`,
- `SCAN_COMPLETED`,
- `PACKAGES_COLLECTED`,
- `CHIP_DELIVERED`,
- `CONTROLLER_INSERTED`,
- `MACHINE_COMPLETED`.

### 3.4 Einzige Fortschritts-API

Es soll genau einen öffentlichen Einstieg geben:

```java
boolean complete(SystemRecoveryLearningStep expectedCurrentStep)
```

Die Methode erfüllt atomar folgende Regeln:

1. Das Netz muss initialisiert sein.
2. `expectedCurrentStep` muss der aktuell markierte Place sein.
3. Der erwartete Place muss vor dem Aufruf exakt ein Token besitzen.
4. Der erwartete Place erhält genau ein weiteres Token.
5. Das generische `PetriNetSystem` wird einmal ausgeführt.
6. Danach muss genau der direkte Nachfolger genau ein Token besitzen.
7. Bei falscher Reihenfolge, Wiederholung oder unbekanntem Step wird nichts verändert.

`complete(...)` läuft ausschließlich im autoritativen Server-Tick. Zwischen Schritt 4 und 6 darf
weder ein Netzwerk-Snapshot noch eine Hint-Abfrage stattfinden. Feuert die erwartete Transition
wegen eines Konfigurationsfehlers nicht, entfernt die Methode das zusätzlich erzeugte Token wieder,
protokolliert den Fehler und liefert `false`. So bleibt kein Place dauerhaft mit zwei Token hängen.

Die Methoden `successfulInteraction(...)`, `hasInteractionTransition(...)`, `solved(...)`,
`progress(...)`, `escapeCompleted()` und die auf Weltaktionen zugeschnittenen `ProgressEvent`s
werden entfernt.

### 3.5 Nur eine Wahrheit über den aktiven Place

Das Feld `activeState` wird entfernt. `activeStep()` liest ausschließlich die Tokens der echten
`PlaceComponent`s.

Erwartetes Verhalten:

- genau ein Learning-Place besitzt ein Token,
- `COMPLETE` besitzt nach der letzten Lösung das einzige Token,
- null oder mehrere aktive Places sind ein Fehlerzustand,
- der Debug-Modus zeigt diesen Fehler sichtbar an,
- Produktionscode darf den Zustand nicht still durch `activateAfterTransition(...)` reparieren.

Die Reparaturmethoden `activateAfterTransition(...)`, `activate(...)` als Fallback und alle
manuellen Token-Clears während normaler Spieleraktionen werden entfernt. Ein Debug-Sprung darf eine
eigene, deutlich benannte Methode behalten, muss aber anschließend dieselbe Ein-Token-Invariante
prüfen.

### 3.6 Verpflichtende Petri-Netz-Tests

1. **Aktivierung:** Zu Beginn besitzt nur `ENERGY_ARRAY` genau ein Token; keine Transition feuert.
2. **Erfolg:** `complete(ENERGY_ARRAY)` erzeugt kurz das zweite Token, verbraucht beide und legt
   genau ein Token in `ENERGY_VALUES`.
3. **Falsche Reihenfolge:** `complete(MODULE_ARRAY)` verändert während `ENERGY_ARRAY` kein Token.
4. **Duplikat:** Ein zweiter Aufruf `complete(ENERGY_ARRAY)` nach dem Fortschritt liefert `false`
   und verändert `ENERGY_VALUES` nicht.
5. **Fehlende Transition:** Ist die Ausgangskante absichtlich falsch konfiguriert, wird das zweite
   Token zurückgenommen und `ENERGY_ARRAY` bleibt mit genau einem Token aktiv.
6. **Gesamtablauf:** Alle 27 Aufgaben lassen sich exakt in Reihenfolge bis `COMPLETE` abschließen;
   nach jedem einzelnen Aufruf gilt die Ein-Token-Invariante.
7. **Türübergang 4 nach 5:** Der akzeptierte Transportcode schließt `TRANSPORT_COLLECT_LOOP` ab und
   aktiviert `DATA_STORAGE_DOOR_OPEN`. Erst das tatsächliche Öffnen von `door_datenspeicher`
   schließt den Tür-Place ab und aktiviert `MANUAL_SORTING`.
8. **Positionsunabhängigkeit:** Spieler werden nacheinander auf alle Dialog- und Raumtriggerpunkte
   gesetzt. Kein Place und keine Tokenanzahl darf sich dadurch verändern. Erst der jeweils
   zuständige Erfolgs-Callback darf den aktiven Place abschließen.

## 4. Anbindung an Terminal und Spezialrätsel

### 4.1 Normale Terminalschritte

`SystemRecoveryPuzzleEvents.terminalAttempt(...)` trackt zuerst den Versuch. Nur wenn der
Interpreter die Eingabe akzeptiert hat, wird der zu `TerminalStep` gehörende
`SystemRecoveryLearningStep` an `complete(...)` übergeben.

Die Zuordnung darf nicht in mehreren Switches dupliziert werden. `TerminalStep` erhält entweder ein
Feld `learningStep`, oder `SystemRecoveryLearningStep` besitzt ein Feld `terminalStep`. Ein Test
muss garantieren, dass jeder normale Terminalschritt genau einmal zugeordnet ist.

### 4.2 Türcodes und Übergang zum Datenspeicher

Der richtige Code an den beiden Keypads ist jeweils eine eigene, bewusste Spielerhandlung:

- Nach `MODULE_LENGTH` wird `ROOM2_DOOR_CODE` aktiv. Erst Code 5 ruft
  `complete(ROOM2_DOOR_CODE)` auf.
- Nach `INVENTORY_COUNT` wird `ROOM3_DOOR_CODE` aktiv. Erst Code 4 ruft
  `complete(ROOM3_DOOR_CODE)` auf.

Beim Transportlager schließt die akzeptierte Sammelschleife `TRANSPORT_COLLECT_LOOP` ab, aktiviert
`DATA_STORAGE_DOOR_OPEN` und startet die sichtbare Förderband- und Scannersequenz. Sobald diese
Sequenz `door_datenspeicher` erfolgreich öffnet, ruft die Türlogik
`complete(DATA_STORAGE_DOOR_OPEN)` auf. Erst dann wird `MANUAL_SORTING` aktiv.

Raumtrigger verändern das Petri-Netz nicht. Wiederholte Keypad- oder Türcallbacks müssen durch
`complete(...)` idempotent abgewiesen werden.

### 4.3 Manuelles Sortieren

Einzelne richtige Tauschentscheidungen schalten das Netz nicht. Falsche Entscheidungen schalten es
ebenfalls nicht. Erst wenn `ManualSortingRiddle.completed()` erstmals wahr wird, wird
`complete(MANUAL_SORTING)` aufgerufen.

Der Callback muss idempotent sein: Debug-Skip, doppelter Netzwerkcallback oder erneute Interaktion
dürfen nicht zweimal weiterschalten.

### 4.4 Sortierchip

Das Einsetzen des leeren Sticks in einen Computer öffnet den Editor, schaltet aber noch nicht
weiter. Nur das erfolgreiche Speichern einer fachlich korrekten Bubble-Sort-Bedingung ruft
`complete(BUBBLE_SORT_CONDITION)` auf.

Danach ist `BUBBLE_SORT_MACHINE` aktiv. Das Einsetzen des programmierten Chips startet die
Maschinenanimation. Erst wenn die Sortierung erfolgreich beendet und der Archivschlüssel vergeben
wurde, ruft `BubbleSortRiddle` `complete(BUBBLE_SORT_MACHINE)` auf.

Anschließend ist `ARCHIVE_ACCESS` aktiv. Wenn der Schlüssel `door_datenarchiv` erfolgreich von
geschlossen auf offen setzt, ruft die Türlogik `complete(ARCHIVE_ACCESS)` auf und aktiviert
`ARCHIVE_ARRAYS`. Das Betreten eines Raumtriggers beeinflusst diesen Fortschritt nicht.

### 4.5 Ortungschip

Das Ausliefern, Aufnehmen und Einsetzen des Chips sowie der Suchlauf sind Weltlogik. Nur das
erfolgreiche Speichern des vollständigen Suchprogramms ruft `complete(SEARCH_PROGRAM)` auf und
aktiviert `SEARCH_ROBOT_RUN`.

Das Einsetzen des programmierten Ortungschips startet den Suchroboter. Erst wenn der Suchlauf
beendet und das Systemkern-Zugangsmodul tatsächlich ausgegeben wurde, ruft der Rätselcontroller
`complete(SEARCH_ROBOT_RUN)` auf.

Danach ist `SYSTEM_CORE_ACCESS` aktiv. Der Spieler führt das Zugangsmodul am Terminal aus. Sobald
dieser Vorgang `door_systemcore` erfolgreich öffnet, ruft die Türlogik
`complete(SYSTEM_CORE_ACCESS)` auf und aktiviert `CORE_SORT`. Kein Raumtrigger ist beteiligt.

### 4.6 Systemkern

Die drei Codeaufgaben und die Meta-Eingabe bleiben vier eigene Learning-Places, weil jede Aufgabe
einen eigenen fachlichen Inhalt und eigene Hinweise besitzt. Alarm, Zugangschip, Tür und Aufzug
bleiben außerhalb des Netzes.

## 5. Spezifische Hinweise pro Step

### 5.1 Schlüsselstruktur

Der komplette Block `hints.generic` wird aus Deutsch und Englisch gelöscht.

Jeder Learning-Step erhält vier eigene Texte:

```json
"hints": {
  "steps": {
    "energy-array": {
      "orientation": "...",
      "approach": "...",
      "near": "...",
      "solution": "..."
    }
  }
}
```

Die wiederverwendbaren Titel `orientation-title`, `approach-title`, `near-title` und
`solution-title` dürfen bestehen bleiben. Wiederverwendet werden nur die Titel, niemals die
Hint-Texte.

`SystemRecoveryHintCatalog` konstruiert keine Ersatztexte mehr. Fehlt ein Schlüssel, ist das ein
Testfehler. Insbesondere darf es keinen Fallback auf Storydialoge geben, weil Storytexte eine andere
Aufgabe haben und oft nicht zur gerade benötigten Hilfestufe passen.

### 5.2 Inhaltliche Abstufung

Für jeden der 27 Aufgaben-Places gelten dieselben Qualitätsregeln:

1. **Orientierung:** benennt das fachliche Ziel und den relevanten Weltkontext, aber keine konkrete
   Syntax.
2. **Ansatz:** nennt das benötigte Sprachkonzept, die relevante Datenstruktur oder die nötige
   Schleifenidee. Variablennamen dürfen genannt werden, vollständiger Code nicht.
3. **Fast geschafft:** nennt Struktur, wichtige Werte und Randbedingungen so konkret, dass nur noch
   die korrekte Java-Syntax formuliert werden muss.
4. **Lösung:** enthält eine vollständige, direkt akzeptierte Musterlösung. Alternative erlaubte
   Schreibweisen können darunter kurz genannt werden.

### 5.3 Inhaltliche Prüfliste pro Step

| Step | Orientierung muss erwähnen | Ansatz muss erklären | Fast geschafft muss konkretisieren |
| --- | --- | --- | --- |
| `ENERGY_ARRAY` | Energiepuffer mit fünf Plätzen | `int[]` und feste Größe | Name `energie`, Größe 5 |
| `ENERGY_VALUES` | fünf geforderte Energiewerte | Index beginnt bei 0 | Zuordnung 0..4 mit 40, 10, 80, 30, 60 |
| `ENERGY_INSERT_BATTERY` | Energieversorgung nach korrektem Code aktivieren | Materialisierung und Batterieaufnahme untersuchen | Hebel betätigen und Batterie in die Wandbox einsetzen |
| `MODULE_ARRAY` | fünf Modulplätze | `String[]` | Name `module`, Größe 5 |
| `MODULE_VALUES` | sichtbare Module den Slots zuordnen | Stringwerte und Indizes | CPU, RAM, GPU, SSD, NETWORK an 0..4 |
| `MODULE_REMOVE_GPU` | defekten Eintrag leeren | Referenzwert `null` | GPU liegt an Index 2 |
| `MODULE_LENGTH` | Anzahl der Slots abfragen | Array-Eigenschaft `length` | Ausdruck für das angelegte Modul-Array |
| `ROOM2_DOOR_CODE` | mit dem ermittelten Ergebnis den nächsten Raum öffnen | Anzeige und Keypad zusammenführen | angezeigte Modullänge als Code verwenden |
| `INVENTORY_COUNT` | nur belegte Einträge zählen | Zähler, Schleife, Null-Prüfung | Initialisierung, `!= null`, Inkrement |
| `ROOM3_DOOR_CODE` | mit dem Scannerergebnis das Transportlager öffnen | Scannerdisplay und Keypad zusammenführen | angezeigte Anzahl als Code verwenden |
| `TRANSPORT_ARRAY` | Gewichte gemeinsam speichern | Array-Literal | Name `pakete`, Werte 15, 40, 20, 60, 30 |
| `TRANSPORT_COLLECT_LOOP` | jedes Paket genau einmal übergeben | indexbasierte `for`-Schleife | Länge als Grenze und `roboter.collect(...)` |
| `DATA_STORAGE_DOOR_OPEN` | die laufende Freigabe des Datenspeichers abwarten | Förderband und Scanner müssen ihren Lauf beenden | nach dem Transportlauf öffnet `door_datenspeicher` automatisch |
| `MANUAL_SORTING` | aufsteigend sortieren | benachbarte Werte vergleichen | links größer bedeutet tauschen |
| `BUBBLE_SORT_CONDITION` | Tauschbedingung ergänzen | Vergleich benachbarter Einträge | `array[j]` und `array[j + 1]` mit `>` |
| `BUBBLE_SORT_MACHINE` | programmierten Chip ausführen | Chip aus dem Computer nehmen und Maschine untersuchen | programmierten Sortierchip in die Bubble-Sort-Maschine einsetzen |
| `ARCHIVE_ACCESS` | mit der erhaltenen Belohnung das Archiv öffnen | Archivschlüssel und verschlossene Archivtür zusammenführen | Schlüssel an `door_datenarchiv` verwenden |
| `ARCHIVE_ARRAYS` | drei verschiedene Arraytypen rekonstruieren | Literale für int, String, boolean | alle drei Namen und sichtbaren Werte |
| `STORAGE_ARRAY` | Raster mit Zeilen und Spalten | zweidimensionales `int`-Array | Name `lager`, Größe 3x4 |
| `STORAGE_VALUES` | markierte Zellen übertragen | zwei Indizes: Zeile, Spalte | drei Koordinaten und ihre Werte |
| `SEARCH_PROGRAM` | jede Matrixzelle prüfen | verschachtelte Schleife | zweite Schleife über `map[i].length` |
| `SEARCH_ROBOT_RUN` | programmierten Ortungschip ausführen | Chip und Suchroboter-Controller zusammenführen | Chip einsetzen und den Suchlauf abschließen lassen |
| `SYSTEM_CORE_ACCESS` | das erhaltene Zugangsmodul ausführen | Modul in einem Terminal verwenden | Freigabeskript ausführen, bis `door_systemcore` öffnet |
| `CORE_SORT` | vollständigen Bubble Sort formulieren | zwei Schleifen und Tauschblock | Grenzen und Vergleich benachbarter Werte |
| `CORE_COUNT` | nicht-leere Module zählen | enhanced for, Null-Prüfung | Zähler und vollständige Bedingung |
| `CORE_SEARCH` | komplette Matrix nach Signalen durchsuchen | zwei indexbasierte Schleifen | `map[i][j] == 1` und Sammelaufruf |
| `CORE_META` | drei angezeigte Ergebnisse kombinieren | Werte aus dem Core-Display übernehmen | erwartete Felder und Format der Eingabemaske |

### 5.4 Hint-Fortschritt

Jeder Place besitzt seinen eigenen `HintComponent`. Beim Wechsel zum nächsten Place beginnt dessen
Index bei 0. Bereits verbrauchte Hinweise eines früheren Place bleiben im Questlog, beeinflussen
aber nicht den neuen Place.

Das Telefon liest Place und Hint als ein unveränderliches Angebot. Beim Bestätigen wird geprüft,
dass derselbe Place noch aktiv ist und genau derselbe Hint noch als nächster angeboten wird. Ein
paralleler Rätselabschluss darf keinen Hint des nächsten Steps verbrauchen.

### 5.5 Tracking angenommener Hinweise

Für jeden tatsächlich angenommenen Hinweis wird genau ein serverseitiges `HINT_USED`-Event über die
vorhandene Dungeon-Tracking-API geschrieben. Ziel der Auswertung ist mindestens:

- Wie viele Hinweise hat ein Spieler pro Rätsel angenommen?
- Welche Hintstufen wurden verwendet?
- Bei welchem konkreten Learning-Step wurden sie verwendet?
- Wurde die vollständige Lösung angefordert?

Die aktuelle Anbindung ist dafür noch nicht ausreichend. Sie verwendet für alle drei normalen
Hintstufen eines Steps dieselbe Hint-ID mit dem Suffix `:hint`. `Tracking.hintUsed(...)` dedupliziert
aber nach Spieler, Rätsel und Hint-ID. Dadurch wird höchstens der erste normale Hinweis eines Steps
gezählt; der zweite und dritte Hinweis verschwinden aus der Statistik.

Jede Hintstufe benötigt deshalb eine eindeutige, stabile ID nach diesem Schema:

```text
<learning-step>:orientation
<learning-step>:approach
<learning-step>:near
<learning-step>:solution
```

Beispiele:

```text
energy-values:orientation
energy-values:approach
energy-values:near
energy-values:solution
```

Das Tracking-Event verwendet weiterhin die stabile `SystemRecoveryPuzzle.id()` als `puzzleId`.
Damit kann das Backend die Anzahl direkt gruppieren:

```text
participantId + puzzleId -> Anzahl unterschiedlicher HINT_USED-Events
```

Die Hint-ID liefert zusätzlich Step und Stufe. Eine separate selbst gepflegte Zählervariable im
Raum ist nicht erforderlich und darf nicht eingeführt werden. Die append-only Tracking-Events sind
die Datenquelle für die spätere Statistik.

Verbindlicher Zeitpunkt des Trackings:

1. Der Spieler bestätigt die Frage nach dem nächsten Hinweis beziehungsweise der Lösung.
2. Das `HintSystem` akzeptiert exakt das zuvor angebotene Hint-Objekt für den weiterhin aktiven
   Learning-Step.
3. Erst danach wird `SystemRecoveryPuzzleEvents.hintUsed(...)` aufgerufen.
4. Anschließend werden Questlog-Eintrag und Auslieferungsdialog erzeugt.

Nicht getrackt werden:

- das bloße Öffnen des Telefons,
- das Anzeigen der Bestätigungsfrage,
- eine abgelehnte Bestätigung,
- ein inzwischen ungültiges Angebot nach einem Step-Wechsel,
- ein doppelter Netzwerkcallback mit derselben Hint-ID,
- der Dialog `Für die aktuelle Aufgabe gibt es keine weiteren Hinweise`.

Im Multiplayer wird als `participantId` der Spieler erfasst, der den Hinweis bestätigt hat. Da die
Hinweise und das Questlog geteilt sind, kann zusätzlich raumweit pro Rätsel aggregiert werden. Ein
Hinweis wird aber nicht künstlich für alle verbundenen Spieler verbucht.

Tests:

1. Orientierung, Ansatz, Fast-geschafft-Hinweis und Lösung eines Steps erzeugen vier verschiedene
   `HINT_USED`-Events.
2. Vier Hinweise aus zwei Steps desselben Rätsels lassen sich gemeinsam unter derselben
   `puzzleId` zählen, behalten aber unterschiedliche `hintId`s.
3. Dieselbe Callback-Nachricht zweimal erzeugt höchstens ein Event für diese Hint-ID.
4. Ablehnen erzeugt kein Event.
5. Step-Wechsel zwischen Angebot und Bestätigung erzeugt kein Event.
6. Zwei verschiedene Spieler, die jeweils einen gültigen Hinweis bestätigen, werden ihren eigenen
   anonymen `participantId`s zugeordnet.
7. Ein Test wertet die erzeugten Events gruppiert nach `participantId` und `puzzleId` aus und prüft
   die erwartete Hinweisanzahl.

## 6. Bessere Petri-Netz-Visualisierung im Debug-Modus

### 6.1 Problem der aktuellen Ansicht

Die aktuelle Debug-Ansicht ist ein langer Textblock mit allen State- und Event-Places. Sie zeigt
nicht auf einen Blick:

- welcher Lernschritt aktiv ist,
- welche Schritte erledigt oder noch gesperrt sind,
- wie viele Hinweise des aktiven Steps verbraucht wurden,
- ob Interpreter und Petri-Netz denselben Schritt erwarten,
- ob die Ein-Token-Invariante verletzt ist.

### 6.2 Zielansicht

Im Debug-Modus erhält der Computer einen Petri-Netz-Dialog mit drei Bereichen:

**Statuskopf**

- aktiver Learning-Step,
- aktiver Terminal-Step beziehungsweise Spezialeditor,
- Tokenanzahl über alle Learning-Places,
- nächster Hint `0/4` bis `4/4`,
- deutliches grünes `KONSISTENT` oder rotes `FEHLER`.

**Lineare Fortschrittsliste**

Jeder Step ist eine feste Zeile:

```text
[✓] 11 TRANSPORT_ARRAY
[●] 12 TRANSPORT_COLLECT_LOOP       Token 1, Hinweise 1/4
[ ] 13 DATA_STORAGE_DOOR_OPEN
[ ] 14 MANUAL_SORTING
```

- Grün mit Haken: liegt vor dem aktiven Step und ist abgeschlossen.
- Cyan mit Token: ist aktiv.
- Grau: liegt nach dem aktiven Step.
- Rot: besitzt unerwartet ein Token oder widerspricht dem Interpreterzustand.

**Diagnoseblock**

- erwartete Erfolgsquelle des aktiven Steps,
- zuletzt akzeptierter Step,
- zuletzt abgelehnter Fortschrittsversuch mit Grund,
- Petri-Tokenanzahl je fehlerhaftem Place,
- optionaler Button `Aktualisieren`.

Das Zielmodell besitzt keine internen Event-Places. Ein ausklappbarer Bereich
`Technische Details` darf die Kantengewichte, die aktuelle Tokenanzahl und den letzten
`complete(...)`-Aufruf für Engine-Debugging anzeigen.

### 6.3 Technische Anforderungen

1. Der Server erstellt ein kleines unveränderliches `ProgressDebugSnapshot` mit primitiven Werten
   und stabilen Keys; keine bereits übersetzten Texte.
2. Der Client rendert daraus die Ansicht in seiner Sprache.
3. Der Snapshot wird nur auf explizite Debug-Anfrage zuverlässig übertragen, nicht in jedem
   Welt-Snapshot.
4. Die Ansicht ist schreibgeschützt. Bestehende Skip-Funktionen bleiben getrennte, deutlich
   markierte Debug-Aktionen.
5. `ProgressDebugSnapshot` enthält mindestens `activeStep`, `tokenCounts`, `interpreterStep`,
   `hintIndex`, `hintCount`, `lastAcceptedStep`, `lastRejectedStep` und `rejectionReason`.
6. Die gesamte Nachricht muss deutlich unter dem TCP-Limit bleiben; Zielwert unter 8 KiB.

## 7. Multiplayer-Questlog: Fehlerdiagnose und Anforderungen

### 7.1 Gefundener struktureller Fehler

Der Client-zu-Server-Weg zum Öffnen ist grundsätzlich vorhanden:

```text
Taste -> QuestLogUI.requestQuestLog(...)
      -> InputMessage "core:questlog"
      -> HeroController.handleShowQuestLog(...)
      -> QuestLogUI.showQuestLogForPlayers(playerId)
```

Der problematische Teil ist die Datenübertragung. Der komplette Questlog-Inhalt wird derzeit in
`SystemRecoveryComponentSync.questLogMetadata(...)` serialisiert und mit regulären Entity-Spawns
und jedem Snapshot verschickt. Der Inhalt wächst mit jedem Storyeintrag und jedem Hinweis.

Die Engine verwirft UDP-Nachrichten oberhalb von `NetworkConfig.SAFE_UDP_MTU` (1400 Byte). Die
bereits beobachtete Warnung `Skip UDP send; payload too large` passt genau zu diesem Aufbau. Damit
kann nicht nur das Questlog auf dem Client veralten; der komplette Snapshot, in dem es enthalten
ist, geht verloren.

Der eigentliche Questlog-Dialog wird zwar zuverlässig über TCP gesendet. Seine Erzeugung hängt aber
weiterhin an einer unnötig duplizierten Clientkopie und es gibt keinen End-to-End-Test für den
Multiplayer-Request. Deshalb muss die Reparatur sowohl die Snapshot-Überlastung entfernen als auch
den Öffnungsweg explizit absichern.

### 7.2 Zielarchitektur

Das Questlog bleibt ausschließlich auf dem Server autoritativ. Es wird nicht mehr als wachsender
Textblock in periodischen Snapshots synchronisiert.

Beim Öffnen gilt:

1. Der Client sendet nur `core:questlog`.
2. Der Server bestimmt den anfragenden Spieler aus der Netzwerk-Session.
3. Der Server liest das autoritative `QuestLogComponent`.
4. Der Server sendet zuverlässig eine kompakte Questlog-Ansicht an genau diesen Client.
5. Tabs und Einträge enthalten Translation-Keys, die erst der Client übersetzt.

### 7.3 Konkrete Umsetzung

1. Questlog-Metadaten aus `SystemRecoverySnapshotTranslator.snapshotMetadata(...)` entfernen.
2. Questlog-Metadaten aus `SystemRecoveryEntitySpawnStrategy` entfernen, sofern keine andere
   allgemeine Engine-Funktion zwingend eine lokale Kopie benötigt.
3. `SystemRecoveryComponentSync.questLogMetadata(...)`, Parser und
   `applyQuestLogState(...)` aus dem Raumcode entfernen, sobald keine Nutzung mehr existiert.
4. `QuestLogUI.showQuestLogForPlayers(...)` serverseitig als einzige Datenquelle verwenden.
5. Der erste Dialog überträgt nur Tab-IDs, den ausgewählten Tab und dessen sichtbare Einträge.
   Beim Tabwechsel fordert der Client den gewählten Tab zuverlässig vom Server an. Dadurch bleibt
   jeder Dialog klein, auch wenn viele Hinweise gespeichert wurden.
6. Wenn das Questlog nicht initialisiert ist, erhält der anfragende Spieler eine sichtbare
   Fehlermeldung; ein reines Server-Log reicht nicht.
7. Jede Serverantwort wird an genau die Entity-ID aus dem autorisierten
   `InputCommandContext` adressiert. Niemals darf ein Client eine fremde Ziel-ID mitsenden.
8. Das Öffnen darf während eines laufenden anderen Dialogs entweder sauber abgelehnt oder in eine
   Warteschlange gelegt werden. Es darf keine unsichtbare zweite pausierende UI erzeugen.

### 7.4 Tests

1. Ein Netzwerkclient sendet `core:questlog`; genau dieser Client erhält einen `QUEST_LOG`-Dialog.
2. Zwei Clients öffnen gleichzeitig; jeder erhält einen eigenen Dialog mit demselben gemeinsamen
   Inhalt.
3. Ein Client mit deutscher und ein Client mit englischer Sprache sehen dieselben Keys jeweils
   lokal übersetzt.
4. 50 lange Einträge überschreiten in keinem regulären Snapshot die Safe-MTU, weil Questlogtexte
   dort nicht mehr enthalten sind.
5. Ein einzelner Questlog-Dialog beziehungsweise Tab-Response bleibt unter einem festgelegten
   Größenbudget.
6. Ein während des Spiels hinzugefügter Hinweis ist beim nächsten Öffnen ohne neuen Levelspawn
   sichtbar.
7. Fehlende Questlog-Initialisierung erzeugt eine sichtbare Rückmeldung.
8. Persönliche Einträge bleiben nur für ihren Ersteller sichtbar.

## 8. Übersetzungen in eigenes Verzeichnis verschieben

### 8.1 Ziel

Alle System-Recovery-Texte liegen anschließend in:

```text
game/assets/language/systemRecovery/de.json
game/assets/language/systemRecovery/en.json
```

`game/assets/language/escapeRoom/de.json` und `en.json` enthalten danach keine
System-Recovery-Schlüssel mehr. Sind dort keine tatsächlich allgemeinen Escape-Room-Texte übrig,
werden die Dateien entfernt.

### 8.2 Zu verschiebende Bereiche

- der komplette Root-Knoten `systemRecovery`,
- der komplette System-Recovery-Root-Knoten `questlog`,
- nur tatsächlich verwendete gemeinsame Schlüssel aus `menu` oder `text`.

Die derzeitigen `menu.exit`- und `text.test`-Einträge sind vor dem Verschieben auf echte Nutzung zu
prüfen. Unbenutzte Testtexte werden gelöscht und nicht in die neue Datei übernommen.

### 8.3 Codeänderungen

1. In `SystemRecovery.initLocalization()` die Pfade auf
   `language/systemRecovery/de.json` und `language/systemRecovery/en.json` ändern.
2. Die Namespaces `systemRecovery.*` und `questlog.*` bleiben stabil. Dadurch müssen gespeicherte
   Keys und `SystemRecoveryTranslator` nicht unnötig umbenannt werden.
3. Kein anderer Raum darf die neuen Dateien registrieren.
4. Last Hour bleibt vollständig bei `language/theLastHour/...`.
5. Dokumentation und Tests dürfen nicht mehr auf `language/escapeRoom/...` verweisen.

### 8.4 Tests

1. Beide neuen JSON-Dateien sind syntaktisch gültig.
2. Deutsch und Englisch besitzen exakt dieselben Blatt-Schlüssel.
3. Jeder im Java-Code verwendete `SystemRecoveryText.key(...)`, `text(...)`, `story(...)`,
   `phoneCall(...)` und `questKey(...)`-Pfad existiert in beiden Sprachen.
4. `hints.generic` existiert in keiner Sprache mehr.
5. Jeder Learning-Step besitzt genau vier spezifische Hint-Texte.
6. Ein deutscher und ein englischer Client lösen denselben transportierten Key unterschiedlich
   auf.

## 9. Empfohlene Umsetzungsreihenfolge

### Paket 1: Neues Lernschritt-Modell

Ziel: Die neue lineare Step-Liste ist typisiert, aber noch nicht mit Gameplay verbunden.

- `SystemRecoveryLearningStep` anlegen.
- Terminal- und Spezialeditor-Zuordnungen definieren.
- Eindeutigkeits- und Vollständigkeitstests schreiben.

### Paket 2: Neues Petri-Netz testgetrieben aufbauen

Ziel: Eine einzige Token-Wahrheit und nur fachliche Spieleraktionen.

- neue Aufgaben-Places anlegen,
- lineare Transitionen mit Eingangsgewicht 2 und Ausgangsgewicht 1 verbinden,
- `complete(...)` und `activeStep()` implementieren,
- vollständigen Ablauf, falsche Reihenfolge, Duplikate und Token-Invariante testen,
- erst danach alte Places und Events entfernen.

### Paket 3: Callbacks umhängen

Ziel: Nur akzeptierte Lernleistungen schalten das Netz.

- normale Terminalschritte anbinden,
- beide korrekten Keypadcodes anbinden,
- das tatsächliche Öffnen der Datenspeichertür anbinden,
- manuellen Sortierabschluss anbinden,
- Sortierchip-Save anbinden,
- Bubble-Sort-Maschinenabschluss und Öffnen der Archivtür anbinden,
- Ortungschip-Save anbinden,
- Suchroboterabschluss und Öffnen der Systemkerntür anbinden,
- alle übrigen technischen Weltaktions-Aufrufe zum Petri-Netz entfernen.

### Paket 4: Spezifische Hinweise schreiben

Ziel: Jeder aktive Step liefert vier wirklich hilfreiche, unterschiedlich konkrete Hinweise.

- neue Key-Struktur in DE und EN anlegen,
- `SystemRecoveryHintCatalog` ohne Fallback umbauen,
- `hints.generic` löschen,
- für jede Hintstufe eine eindeutige Tracking-ID erzeugen,
- bestätigte Hinweise über `Tracking.hintUsed(...)` dem anfragenden Spieler und Rätsel zuordnen,
- Inhalts- und Key-Vollständigkeit testen.

### Paket 5: Debug-Ansicht ersetzen

Ziel: Aktiver Step, Interpreterzustand und Hintfortschritt sind auf einen Blick prüfbar.

- `ProgressDebugSnapshot` erstellen,
- kompakte Serverantwort und clientseitige Ansicht implementieren,
- Inkonsistenzen rot und eindeutig darstellen,
- Nachrichtengröße testen.

### Paket 6: Questlog-Multiplayer reparieren

Ziel: Das Questlog öffnet zuverlässig und vergrößert keine UDP-Snapshots.

- periodische Questlog-Synchronisation entfernen,
- serverseitigen Tab-Request absichern,
- sichtbare Fehlerantwort ergänzen,
- Zwei-Client- und Größenbudgettests ergänzen.

### Paket 7: Übersetzungen verschieben

Ziel: Der Raum besitzt einen klar abgegrenzten Übersetzungsordner.

- JSON-Dateien verschieben und bereinigen,
- Registrierungs-Pfade ändern,
- Paritäts- und Referenztests ausführen,
- alte Dateien nur löschen, wenn keine allgemeinen Inhalte verbleiben.

## 10. Definition of Done

Die Überarbeitung ist abgeschlossen, wenn:

- das Petri-Netz nur die 27 fachlichen Aufgaben inklusive Batteriehandlung, zweier Türcodeeingaben,
  Bubble-Sort-Maschine, Archivzugang, Suchroboterlauf und Systemkernzugang plus `COMPLETE` enthält,
- ausschließlich akzeptierte Codeeingaben, bewusst modellierte Pflichtaktionen wie das Einsetzen
  der Batterie, der vollständige manuelle Sortierlauf und die beiden erfolgreichen Chip-Saves
  weiterschalten,
- passive Weltaktionen, Animationsframes und bloße Darstellungszustände keinen Petri-Netz-Aufruf
  mehr besitzen; ausgenommen sind ausdrücklich modellierte Abschlussaktionen wie das erfolgreiche
  Öffnen der Datenspeicher- und Archivtür,
- außerhalb des atomaren `complete(...)`-Aufrufs jederzeit genau ein Learning-Place genau ein
  Token hält,
- das Telefon immer Hinweise für den aktiven Learning-Step liefert,
- jeder Step vier eigene deutsche und englische Hinweise besitzt,
- `hints.generic` vollständig entfernt ist,
- jeder angenommene Hinweis genau einmal mit Rätsel, Learning-Step, Hintstufe und Teilnehmer
  getrackt wird und die Anzahl pro Spieler und Rätsel auswertbar ist,
- die Debug-Ansicht aktiven Step, Tokenkonsistenz, Interpreter und Hintindex gemeinsam zeigt,
- das Questlog für Host und Join-Client zuverlässig öffnet,
- Questlogtexte keine periodischen UDP-Snapshots mehr vergrößern,
- alle System-Recovery-Texte aus `language/systemRecovery/de.json` und `en.json` geladen werden,
- Unit-, Netzwerk-, Übersetzungs- und Checkstyle-Tests erfolgreich sind,
- ein manueller Durchlauf mit zwei Clients bestätigt, dass beide stets denselben aktiven
  Lernschritt und die jeweils richtige lokale Sprache sehen.
