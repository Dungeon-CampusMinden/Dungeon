# System Recovery: Refactoring- und Stabilitätsaufgaben

Dieses Dokument beschreibt die technischen Arbeiten so konkret, dass sie einzeln an eine andere
Person oder einen Coding-Agenten übergeben werden können.

## Umsetzungsstand

Die Implementierungen aus SR-01 bis SR-19 sind im aktuellen Arbeitsstand vorhanden. Die
Verifikation ist noch nicht vollständig: Die automatisierten Tests decken zentrale Abläufe und
einige gezielte Randfälle ab. Host/Join, individuelle Clientsprachen und Rendererzustände müssen
zusätzlich manuell geprüft werden; ein erfolgreicher Build belegt diese Fälle nicht.

```text
./gradlew :game:test --tests 'rooms.systemRecovery.*'
./gradlew :game:checkstyleMain :game:checkstyleTest
```

Die abschließenden Host-/Join- und Level-Editor-Durchläufe bleiben manuelle Prüfungen, weil sie
echte Clients, unterschiedliche Sprachen und den grafischen Renderer benötigen.

| Bereich | Ergebnis |
| --- | --- |
| SR-01 bis SR-06 | Fortschritt, Computeraktionen, Übersetzung und Hinweise strukturell abgesichert; gezielte Randfalltests ergänzt |
| SR-07 bis SR-10 | Terminal-Schritte, Chip-Phasen und Snapshot-Synchronisation benannt und getrennt |
| SR-11 bis SR-14 | Factories, Riddle-Registry, Level-Setup und Custom-Point-Prüfung strukturiert |
| SR-15 bis SR-17 | Debug-Konfiguration, Tracking-Grenze und unbenutzter Chat bereinigt |
| SR-18 bis SR-19 | Checkstyle bereinigt und README auf den aktuellen Aufbau gebracht |

## Ausgangslage

- Die Tests unter `game/test/rooms/systemRecovery` laufen aktuell erfolgreich.
- `./gradlew :game:checkstyleMain :game:checkstyleTest` läuft ohne Warnungen durch.
- Die Regressionstests decken Terminal-Szenarien, den Petri-Netz-Ablauf, Chipphasen,
  wiederholte Chipcallbacks und ausgewählte synchronisierte Visualzustände ab. Nicht jede
  Callback-UI und nicht jeder vollständige Late-Join-Pfad ist automatisiert durchlaufen.
- Serverentscheidungen, Tracking, Hinweise und Clientdarstellung sind über benannte Grenzen
  getrennt.
- Die verbliebenen Risiken liegen im manuellen Multiplayer- und Renderer-Test.

## Empfohlene Reihenfolge

1. Petri-Netz und finalen Spielfluss korrigieren.
2. Chip- und Computeraktionen serverseitig absichern.
3. Übersetzung und Hinweise multiplayerfähig machen.
4. Fehlende Regressionstests ergänzen.
5. Danach Klassen und Zuständigkeiten aufräumen.
6. Zum Schluss Javadocs, Checkstyle und Dokumentation bereinigen.

# P0: Funktionale Fehler und Multiplayer-Risiken

## SR-01: Den letzten Abschnitt des Petri-Netzes linear modellieren

### Problem

In `SystemRecoveryProgressNet.connectStates()` erzeugt `R9_SCAN_COMPLETED` aktuell gleichzeitig einen Token in `R10_SORT` und `SYSTEM_ACCESS`.

Damit existieren zwei aktive, mit Hinweisen versehene Fortschritts-Places gleichzeitig. Das widerspricht der Annahme der Klasse, dass genau ein Place die aktuelle Spieleraufgabe repräsentiert. Zusätzlich wird `activeState` als zweiter, manueller Zustand neben den echten Petri-Netz-Tokens geführt. Dieser Wert kann deshalb einen anderen Fortschritt anzeigen als die tatsächlichen Places.

Der Event-Typ `ESCAPE_COMPLETED` existiert, wird aber nirgends ausgelöst. Der Place `ESCAPE_COMPLETE` kann dadurch nicht zuverlässig den tatsächlichen Abschluss am Custom Point `end` darstellen.

### Gewünschter Ablauf

Nach dem Suchroboter soll der Fortschritt genau so laufen:

```text
R9_SCAN
  -> SYSTEM_ACCESS
  -> R10_SORT
  -> R10_COUNT
  -> R10_SEARCH
  -> R10_META
  -> R10_FINAL
  -> ESCAPE_COMPLETE
```

`SYSTEM_ACCESS` bedeutet: Der Spieler besitzt das Systemkern-Zugriffsmodul und muss es an einem Computer ausführen.

`R10_FINAL` bedeutet: Der Systemkern ist gelöst, der Aufzug ist offen und der Spieler muss den Ausgang erreichen.

`ESCAPE_COMPLETE` bedeutet: Ein Spieler hat den Punkt `end` erreicht und die Endsequenz wurde gestartet.

### Umsetzung

Betroffene Dateien:

- `game/src/rooms/systemRecovery/petrinet/SystemRecoveryProgressNet.java`
- `game/src/rooms/systemRecovery/level/SystemRecoveryLevel.java`
- `game/test/rooms/systemRecovery/petrinet/SystemRecoveryProgressNetTest.java`

Konkrete Schritte:

1. In `connectStates()` die Verzweigung mit `connectWithAdditionalOutput(...)` entfernen.
2. Stattdessen `R9_SCAN` mit `R9_SCAN_COMPLETED` direkt nach `SYSTEM_ACCESS` verbinden.
3. `SYSTEM_ACCESS` mit `SYSTEM_ACCESS_GRANTED` direkt nach `R10_SORT` verbinden.
4. `R10_META` weiterhin mit `R10_META_ACCEPTED` nach `R10_FINAL` verbinden.
5. `R10_FINAL` mit `ESCAPE_COMPLETED` nach `ESCAPE_COMPLETE` verbinden.
6. Das interne Feld `accessGranted` entfernen.
7. Die nun überflüssigen Hilfsmethoden `connectWithAdditionalOutput`, `connectToInternal` und `connectPlaces` entfernen.
8. Eine öffentliche Methode `SystemRecoveryProgressNet.escapeCompleted()` ergänzen. Sie darf nur den Event `ESCAPE_COMPLETED` für den erwarteten Place `R10_FINAL` auslösen.
9. Im Collider des Endpunkts in `SystemRecoveryLevel.setupEndTrigger()` diese Methode ausführen, bevor die Endsequenz gestartet wird.
10. Nach jedem Event müssen alle Fortschritts-Places zusammen genau einen Token besitzen.

### Darauf achten

- Ein verspätetes oder doppeltes Netzwerk-Event darf keinen zweiten Token erzeugen.
- Ein Event für einen zukünftigen Schritt muss ignoriert werden.
- Der Hinweis am Telefon muss immer zum einzigen aktiven Place gehören.
- Debug-Sprünge müssen dieselbe Ein-Token-Regel einhalten.

### Tests

In `SystemRecoveryProgressNetTest` ergänzen:

1. Den kompletten Weg vom Eröffnungsanruf bis `ESCAPE_COMPLETE` ausführen und nach jedem Event den exakten Place prüfen.
2. `SYSTEM_ACCESS_GRANTED` vor `R9_SCAN_COMPLETED` senden. Der Place darf sich nicht ändern.
3. `R10_SORT_ACCEPTED` senden, solange `SYSTEM_ACCESS` aktiv ist. Der Place darf sich nicht ändern.
4. Jeden erfolgreichen Event zweimal senden. Der zweite Aufruf darf keine Änderung und keinen zusätzlichen Token erzeugen.
5. `escapeCompleted()` vor `R10_FINAL` aufrufen. Der Place darf sich nicht ändern.
6. Nach `escapeCompleted()` muss ausschließlich `ESCAPE_COMPLETE` einen Token besitzen.

### Fertig, wenn

- Der Debug-Snapshot zeigt immer genau einen Fortschritts-Token.
- Das Telefon liefert nach dem Suchroboter zuerst den Hinweis zum Zugriffsmodul.
- Nach dem Systemkern liefert es den Hinweis zum Ausgang.
- Am Endpunkt wird `ESCAPE_COMPLETE` aktiv.

## SR-02: Den Terminal-State erst nach dem Freigabeskript auf den Systemkern umstellen

### Problem

`SearchRobotRiddle.finishScan()` setzt den Interpreter direkt auf `CENTRAL_SORT_STATE`.

Zu diesem Zeitpunkt hat der Spieler das Zugriffsmodul zwar erhalten, aber noch nicht in einen Computer eingesetzt und das Freigabeskript ausgeführt. Da alle Computer dasselbe Terminal verwenden, kann der Spieler bereits außerhalb des Systemkerns den Code für Rätsel 10 abschicken. Die Tür zum Systemkern ist dann noch geschlossen, aber die internen Rätselzustände können schon weiterlaufen.

### Gewünschtes Verhalten

Nach dem Suchlauf bleibt der normale Terminal-Interpreter in der Chip-Phase stehen. Erst wenn das Zugriffsmodul erfolgreich ausgeführt wurde, wird `CENTRAL_SORT_STATE` aktiviert.

### Umsetzung

Betroffene Dateien:

- `game/src/rooms/systemRecovery/riddles/SearchRobotRiddle.java`
- `game/src/rooms/systemRecovery/level/SystemRecoveryLevel.java`

Konkrete Schritte:

1. In `SearchRobotRiddle.finishScan()` den Aufruf `TerminalInterpreter.instance().synchronizeState(CENTRAL_SORT_STATE)` entfernen.
2. In `SystemRecoveryLevel.completeSystemCoreAccess(...)` nach erfolgreicher serverseitiger Prüfung und nach dem Öffnen von `door_systemcore` den Interpreter auf `CENTRAL_SORT_STATE` setzen.
3. Der Petri-Netz-Event `SYSTEM_ACCESS_GRANTED` und das Umschalten des Interpreters müssen Teil derselben erfolgreichen Aktion sein.
4. Wird das Freigabeskript abgelehnt, dürfen weder Tür noch Interpreter noch Petri-Netz geändert werden.

### Darauf achten

- Der Code darf nicht nur über die sichtbare UI gesperrt werden. Der Server muss die Phase prüfen.
- Ein Client kann theoretisch einen Callback senden, obwohl der passende Tab nicht sichtbar ist.
- Debug-Modus darf einen eigenen, ausdrücklich erkennbaren Sprung besitzen. Dieser darf nicht unbemerkt im normalen Ablauf wirken.

### Tests

1. Suchroboter abschließen und anschließend Systemkern-Code senden: muss abgelehnt werden.
2. Zugriffsmodul korrekt ausführen: Tür öffnet, Petri-Netz steht auf `R10_SORT`, Interpreter steht auf State 13.
3. Systemkern-Code danach senden: muss akzeptiert werden.
4. Freigabeskript zweimal ausführen: Tür und State dürfen nur einmal geändert werden.

## SR-03: Das Einsetzen von Chips atomar machen

### Problem

`SystemRecoveryComputerFactory.showChipChoice(...)` merkt sich ein Java-Objekt aus dem Inventar, öffnet einen Dialog und entfernt dieses Objekt erst nach der Antwort.

Zwischen Öffnen und Bestätigen kann sich das Inventar ändern. Im Multiplayer oder bei einem doppelten Dialog-Callback kann der Chip bereits entfernt worden sein. Der aktuelle Code meldet trotzdem einen erfolgreichen Tracking- und Petri-Netz-Event und öffnet den Programm-Tab, weil das Ergebnis von `inventory.remove(chip)` nicht geprüft wird.

Beim Speichern gibt `addToInventory(...)` außerdem kein Ergebnis zurück. Falls das Inventar voll ist, kann der Code `programReturned[0] = true` setzen, obwohl der programmierte Chip gar nicht zurückgegeben wurde. Der ursprüngliche Chip ist dann verloren.

### Gewünschtes Verhalten

Eine Chipaktion ist nur erfolgreich, wenn der Server den erwarteten Chip im aktuellen Inventar findet und tatsächlich entfernt. Ein programmierter Chip gilt erst als zurückgegeben, wenn `InventoryComponent.add(...)` erfolgreich war.

### Umsetzung

Betroffene Datei:

- `game/src/rooms/systemRecovery/modules/computer/SystemRecoveryComputerFactory.java`

Konkrete Schritte:

1. Beim Bestätigen den Spieler erneut mit `Game.findEntityById(...)` laden.
2. Das aktuelle `InventoryComponent` erneut laden.
3. Prüfen, ob genau der erwartete Chip noch im Inventar liegt.
4. `inventory.remove(chip)` ausführen und das `Optional` auswerten.
5. Nur bei erfolgreichem Entfernen:
   - den erfolgreichen Versuch tracken,
   - das Petri-Netz schalten,
   - den passenden Computer-Tab öffnen.
6. Bei fehlendem Chip:
   - Fehler tracken,
   - lokalisierte Meldung anzeigen,
   - keinen Tab öffnen,
   - keinen Fortschritt schalten.
7. `addToInventory(...)` in eine Methode mit Boolean-Rückgabe ändern.
8. `programReturned[0]` erst setzen, wenn das programmierte Item erfolgreich hinzugefügt wurde.
9. Bei vollem Inventar eine lokalisierte Meldung anzeigen und den eingesetzten Chip weiterhin als „im Computer“ behandeln, damit er beim Schließen erneut zurückgegeben werden kann.
10. Jeder Save-Callback muss prüfen, ob der aktuelle `ProgramKind` wirklich zu ihm gehört.

### Darauf achten

- `SORT_PROGRAM_SAVE` darf nur mit `ProgramKind.SORT` arbeiten.
- `SEARCH_PROGRAM_SAVE` darf nur mit `ProgramKind.SEARCH` arbeiten.
- `SYSTEM_CORE_SCRIPT_RUN` darf nur mit `ProgramKind.ACCESS` arbeiten.
- Mehrfaches Klicken auf Speichern darf kein zweites Item erzeugen.
- Das Schließen des Computers nach erfolgreichem Speichern darf den leeren Chip nicht zusätzlich zurückgeben.

### Tests

`SystemRecoveryComputerFactoryTest` prüft die freigegebenen Phasen und den einmaligen Chiptransfer.
Ein grafischer End-to-End-Test der Dialogcallbacks bleibt Teil des manuellen Host/Join-Tests:

1. Chip liegt beim Bestätigen noch im Inventar: wird einmal entfernt und Tab öffnet.
2. Chip wurde vor dem Bestätigen entfernt: kein Erfolg, kein Tab, kein Fortschritt.
3. Bestätigungs-Callback kommt zweimal: Chip wird nur einmal entfernt.
4. Sort-Save wird ohne Sort-Tab gesendet: wird ignoriert oder als ungültig abgelehnt.
5. Search-Save wird aus dem Sort-Tab gesendet: darf keinen Ortungschip erzeugen.
6. Inventar ist beim Speichern voll: kein Itemverlust und kein falsches Erfolgsflag.
7. Nach erfolgreichem Speichern und anschließendem Schließen liegt genau ein programmierter Chip im Inventar.

## SR-04: Computeraktionen an den tatsächlichen Spielfortschritt binden

### Problem

Der Besitz eines Items und der im Dialog gespeicherte `ProgramKind` reichen aktuell teilweise aus, um einen Callback auszuführen. Ein Client kann Netzwerk-Callbacks grundsätzlich auch dann senden, wenn die entsprechende Schaltfläche nicht sichtbar ist.

Beispiel: Ein früh erzeugtes Systemkern-Zugriffsmodul kann das Freigabeskript auslösen, obwohl der Suchroboter noch nicht abgeschlossen wurde. Das Petri-Netz ignoriert eventuell den zu frühen Event, aber `completeSystemCoreAccess()` öffnet trotzdem die Tür. Damit unterscheiden sich Weltzustand, Petri-Netz und Hinweise.

### Gewünschtes Verhalten

Jede serverseitige Aktion prüft zusätzlich den fachlichen Zustand:

- Sortierchip programmieren: manueller Sortiervorgang abgeschlossen und leerer Sortierchip eingesetzt.
- Ortungschip programmieren: 2D-Speicher abgeschlossen und leerer Ortungschip eingesetzt.
- Systemkern-Modul ausführen: Suchroboter abgeschlossen und Place `SYSTEM_ACCESS` aktiv.
- Meta-Eingabe absenden: Systemkern steht exakt auf `R10_META` und alle drei Teilaufgaben sind abgeschlossen.

### Umsetzung

1. In den zuständigen Rätselklassen lesbare Abfragen bereitstellen, beispielsweise `canProgramSortChip()`, `canProgramSearchChip()` und `canGrantSystemCoreAccess()`.
2. Alternativ eine zentrale Methode im Fortschrittsmodell bereitstellen, die prüft, ob ein bestimmter Place aktiv ist.
3. Diese Bedingungen direkt in den serverseitigen Computer-Callbacks prüfen.
4. Bei falscher Phase eine lokalisierte Fehlermeldung anzeigen und einen fehlgeschlagenen Tracking-Versuch schreiben.
5. Weltzustand, Interpreter und Petri-Netz nur nach einer gemeinsamen erfolgreichen Prüfung verändern.

### Tests

Für jede Aktion drei Fälle prüfen:

1. Item fehlt.
2. Item ist vorhanden, aber Spielfortschritt ist zu früh.
3. Item und Spielfortschritt sind korrekt.

Nur Fall 3 darf Weltzustand, Itembestand und Petri-Netz verändern.

## SR-05: Alle synchronisierten Texte auf dem jeweiligen Client übersetzen

### Problem

Viele Rätselklassen rufen auf dem Server `SystemRecoveryText.text(...)` auf. Dadurch wird der deutsche oder englische Text bereits in der Sprache des Serverprozesses erzeugt. Anschließend wird dieser fertige Text über Snapshots an alle Clients geschickt.

Bei zwei Clients mit unterschiedlichen Spracheinstellungen sehen dadurch beide dieselbe Sprache. Das betrifft derzeit unter anderem Energie-, Modul-, Scanner-, Transport-, Sortier-, 2D- und Systemkern-Displays.

Auch `SystemRecoveryQuestLogUtil` und `SystemRecoveryHintCatalog` lösen Texte serverseitig auf. Das gemeinsame Questlog enthält dadurch fertige Texte statt übersetzbarer Schlüssel.

### Betroffene Stellen

- `EnergyRiddle.energyDisplayText`
- `ModuleStorageRiddle.moduleDisplayText`
- `InventoryScannerRiddle` Displaytexte
- `TransportStorageRiddle.transportDisplayText`
- `ManualSortingRiddle.sortDisplayText()`
- `TwoDimensionalStorageRiddle.storageDisplayText`
- `SystemCoreRiddle.displayText()`
- `SystemRecoveryLevel.setupRoomLabel()`
- `SystemRecoveryQuestLogUtil`
- `SystemRecoveryHintCatalog`

### Gewünschtes Verhalten

Der Server verschickt einen stabilen Übersetzungsschlüssel samt dynamischen Argumenten. Erst der Client, der den Text rendert, löst diesen Schlüssel in seiner eigenen Sprache auf.

### Umsetzung

1. In serverseitigen Rätselzuständen `SystemRecoveryText.key(...)` statt `SystemRecoveryText.text(...)` speichern.
2. Dynamische Werte ebenfalls über `key(...)` kodieren, zum Beispiel die beiden Sortierwerte oder den aktuellen Systemkernzustand.
3. Einen transportierbaren Namespace für Questlog-Texte ergänzen, beispielsweise `systemRecoveryQuest.*`.
4. Diesen Namespace im `SystemRecoveryTranslator` registrieren und auf die `questlog`-Übersetzungen abbilden.
5. Questlog-Tabs und Einträge als Keys speichern.
6. Hinweis-Titel und Hinweistext als Keys speichern oder beim Versand als transportierbare Keys erzeugen.
7. Prüfen, ob Türlabel-Texte bereits auf dem Server in eine Textur gerendert werden. Falls ja, muss der Client die Labeldarstellung anhand eines synchronisierten Keys selbst erzeugen.
8. Direkte UI-Texte innerhalb rein clientseitiger Tabs dürfen weiterhin `text(...)` verwenden.

### Darauf achten

- Nicht pauschal jedes `text(...)` ersetzen. Clientseitige UI-Klassen sollen direkt übersetzen.
- Keys mit Parametern müssen Zeilenumbrüche und Sonderzeichen sicher übertragen.
- Verschachtelte Keys wie `status-active` müssen weiterhin korrekt aufgelöst werden.
- Das Questlog ist inhaltlich geteilt, aber jeder Client rendert es in seiner eigenen Sprache.

### Tests

1. Einen Test für identische Schlüsselstrukturen in `de.json` und `en.json` ergänzen.
2. `SystemRecoveryTranslatorTest` um Parameter, Zeilenumbrüche und verschachtelte Keys erweitern.
3. Einen Snapshot-Test schreiben: Server erzeugt ein Display mit Key; Client DE erhält deutschen Text, Client EN englischen Text.
4. Dasselbe für Questlog und einen Telefonhinweis prüfen.

## SR-06: Den Telefon-Hinweis gegen parallelen Spielfortschritt absichern

### Problem

`SystemRecoveryHintPhone.request()` liest den aktuellen Hinweis und den aktiven Place. Danach sieht der Spieler zunächst einen Dialog und anschließend eine Bestätigungsfrage.

Während diese Dialoge offen sind, kann ein anderer Spieler das Rätsel lösen. Beim Bestätigen ruft der Code `acceptSharedHint()` auf und liest anschließend den dann aktuellen Place erneut. Dadurch kann Folgendes passieren:

1. Spieler A bekommt die Frage für Rätsel 4.
2. Spieler B löst Rätsel 4.
3. Spieler A bestätigt.
4. Das System akzeptiert einen Hinweis für Rätsel 5 und trägt ihn möglicherweise unter einem falschen Kontext ein.

### Gewünschtes Verhalten

Das Angebot muss unveränderlich festhalten:

- für welchen Place es erstellt wurde,
- welcher Hinweisindex angeboten wurde,
- welcher Hinweis angezeigt werden soll.

Beim Bestätigen darf genau dieses Angebot angenommen werden. Hat sich der Place geändert, wird das alte Angebot verworfen und der Spieler erhält bei der nächsten Interaktion den neuen Hinweis.

### Umsetzung

1. Einen Record wie `HintOffer(place, entityId, hintIndex, hint)` einführen.
2. `peekSharedHint()` so erweitern, dass es dieses Angebot zurückgibt.
3. `acceptSharedHint(offer)` ergänzen.
4. Beim Akzeptieren prüfen:
   - derselbe Place ist noch aktiv,
   - dieselbe Hint-Entität besitzt noch einen Token,
   - derselbe Index ist noch der nächste gemeinsame Index.
5. Nur dann den Index erhöhen.
6. Den Questlog-Tab aus `offer.place()` ableiten, nicht aus einem später neu gelesenen Place.
7. Bei ungültigem Angebot keine Hint-Stufe verbrauchen.

### Tests

1. Hinweis anbieten und sofort bestätigen: wird genau einmal gespeichert.
2. Hinweis anbieten, Place wechseln, dann bestätigen: wird nicht gespeichert und nicht verbraucht.
3. Zwei Spieler bestätigen dasselbe Angebot fast gleichzeitig: nur eine Bestätigung erhöht den gemeinsamen Index.
4. Ablehnen: Index bleibt unverändert.

# P1: Zuständigkeiten und Lesbarkeit

## SR-07: Magische Terminal-State-Zahlen durch benannte Schritte ersetzen

### Problem

Die Zahlen 0 bis 16 werden an mehreren Stellen unabhängig interpretiert:

- `TerminalInterpreterSetup`
- `SystemRecoveryPuzzle.fromTerminalState(...)`
- `SystemRecoveryProgressNet.terminalSuccess(...)`
- `SystemRecoveryProgressNet.debugTargetAfter(...)`
- `SystemRecoveryLevel.interpretTerminalInput(...)`

Wird ein Schritt eingefügt oder verschoben, müssen alle Switches manuell angepasst werden. Ein vergessener Switch führt dazu, dass Terminal, Tracking, Petri-Netz und Welt unterschiedliche Aufgaben für denselben Integer annehmen.

### Gewünschte Struktur

Einen Typ `TerminalStep` erstellen. Jeder Eintrag enthält mindestens:

- stabile numerische ID für Netzwerk und bestehenden Interpreter,
- zugehöriges `SystemRecoveryPuzzle`,
- kurze Beschreibung des erwarteten Spielerschritts,
- Information, ob der normale Editor oder eine besondere Eingabemaske zuständig ist.

Beispiel:

```java
ENERGY_ARRAY(0, SystemRecoveryPuzzle.ENERGY, InputMode.TERMINAL),
ENERGY_VALUES(1, SystemRecoveryPuzzle.ENERGY, InputMode.TERMINAL),
SEARCH_PROGRAM(12, SystemRecoveryPuzzle.SEARCH_ROBOT, InputMode.CHIP_EDITOR),
SYSTEM_CORE_META(16, SystemRecoveryPuzzle.SYSTEM_CORE, InputMode.FORM);
```

### Umsetzung

1. `TerminalStep` im Package `util.interpreter` anlegen.
2. Eine sichere Methode `fromStateId(int)` anbieten.
3. `TerminalInterpreterSetup` registriert Requirements über `TerminalStep.stateId()`.
4. Tracking verwendet `TerminalStep.puzzle()`.
5. Petri-Netz verwendet eine zentrale Zuordnung von `TerminalStep` zum Event.
6. Debug-Sprünge verwenden benannte Schritte.
7. Öffentliche Konstanten wie `CENTRAL_SORT_STATE` nur vorübergehend als Alias behalten oder vollständig ersetzen.

### Tests

1. Jede ID ist eindeutig.
2. Jeder registrierte Interpreter-State besitzt einen `TerminalStep`.
3. Jeder normale Terminal-Step besitzt eine Petri-Netz-Zuordnung.
4. Spezialschritte 12 und 16 sind als Chip-Editor beziehungsweise Formular markiert.

## SR-08: Den Ortungschip nicht als normalen Terminal-State registrieren

### Problem

State 12 ist im Interpreter registriert, soll aber nicht über den normalen Terminal-Send-Button gelöst werden. `SystemRecoveryLevel.interpretTerminalInput()` behandelt diesen State deshalb immer als falsch. Gleichzeitig verwendet der Ortungschip-Tab denselben registrierten State über `analyzeState(...)`.

Dadurch existiert ein scheinbar normaler Interpreter-State, den der normale Interpreter absichtlich niemals akzeptiert. Zusätzlich existiert in `InterpretationCallbacks` eine Legacy-Methode mit TODO, die im echten Ablauf keine sinnvolle Aufgabe mehr besitzt.

### Gewünschtes Verhalten

Der Suchcode ist eine eigenständige Codeprüfung des Ortungschip-Tabs. Der globale Terminal-State beschreibt nur, dass der Spielfluss gerade auf diesen externen Chip-Schritt wartet.

### Umsetzung

1. Die Suchcode-Requirement in eine benannte Factory-Methode auslagern, beispielsweise `searchRobotProgramRequirement()`.
2. Der Ortungschip-Tab prüft direkt diese Requirement.
3. State 12 wird nicht mit einem normalen Success-Callback registriert.
4. `onRiddleNineStepOneSearchRobotCompleted()` entfernen.
5. Im normalen Terminal UI während dieses Schritts entweder den Send-Button deaktivieren oder eine klare Meldung anzeigen, dass der Ortungschip programmiert werden muss.
6. Nach erfolgreichem Suchlauf beziehungsweise Freigabeskript wird der globale Interpreter explizit weitergesetzt.

### Tests

1. Derselbe korrekte Suchcode wird im Ortungschip-Tab akzeptiert.
2. Derselbe Code wird über den normalen Terminal-Send-Button nicht als Rätsellösung behandelt.
3. Ein falscher Suchcode erzeugt keinen programmierten Chip.

## SR-09: Versteckte ThreadLocal-Kontexte entfernen

### Problem

Der aktuelle Spieler wird in `SystemRecoveryStoryDialogs.TERMINAL_PLAYER` gespeichert. State und Source werden separat in `InterpretationCallbacks.CURRENT_ATTEMPT` gespeichert. Beide verwenden `ThreadLocal`.

Damit hängt das Verhalten eines Callbacks von unsichtbarem Zustand des aktuellen Ausführungsthreads ab. Beim Lesen einer Methode wie `onRiddleOneStepTwoEnergyValuesSet()` ist nicht erkennbar, woher Spieler, State und Eingabe kommen. Bei asynchronen Callbacks oder einem späteren Wechsel des Ausführungsthreads geht dieser Kontext verloren.

### Gewünschtes Verhalten

Der Interpreter übergibt ein ausdrückliches Objekt:

```java
TerminalAttempt {
  TerminalStep step;
  String source;
  int playerId;
}
```

Success- und Failure-Callbacks erhalten dieses Objekt direkt.

### Umsetzung

1. `TerminalAttempt` als normalen Record im Interpreter-Integrationspackage anlegen.
2. `TerminalCodeRequirement` so erweitern, dass Callbacks einen `TerminalAttempt` erhalten.
3. `TerminalInterpreter.interpret(...)` erhält neben Source auch den Player oder einen vollständigen Attempt-Kontext.
4. `InterpretationCallbacks` verwendet die Felder direkt für Tracking und Storydialoge.
5. `CURRENT_ATTEMPT`, `TERMINAL_PLAYER`, `withTerminalAttempt(...)` und `withTerminalPlayer(...)` entfernen.
6. Reine Analysefunktionen bleiben callbackfrei und brauchen keinen Player.

### Darauf achten

- Der generische Interpreter darf weiterhin nicht von System-Recovery-Rätselklassen abhängen.
- Dafür kann ein generischer Callback-Kontext im Interpreter-Package liegen.
- Preview- und Unit-Test-Requirements müssen weiterhin ohne Spieler funktionieren.

### Tests

1. Spieler A sendet eine richtige Eingabe, direkt danach Spieler B eine falsche.
2. Tracking enthält für beide den richtigen Player, State und Source.
3. Nur Spieler A erhält den Folge-Storydialog.
4. Nach einer Exception bleibt kein Kontext für die nächste Eingabe übrig.

## SR-10: `SystemRecoverySnapshotTranslator` in kleine Synchronisationsbereiche zerlegen

### Problem

`SystemRecoverySnapshotTranslator` hat derzeit ungefähr 839 Zeilen und kümmert sich gleichzeitig um:

- Standard-Snapshots,
- Keypads,
- Displays,
- Questlog,
- Terminal-State,
- Collider,
- manuelles Sortieren,
- Förderband-Sortierung,
- Modulscanner,
- 2D-Speicher,
- Systemkern-Alarm,
- Systemkern-Shader.

Änderungen an einem Rätsel können dadurch leicht visuelle Zustände eines anderen Rätsels beeinflussen. Die Felder `lastBelt...`, `lastSort...` und `lastModuleScanFault` sind außerdem Cache-Zustände des Translators und werden beim Levelwechsel nicht sichtbar zurückgesetzt.

### Gewünschte Struktur

Der eigentliche Translator delegiert an kleine Klassen:

- `SystemRecoveryMetadataWriter`: sammelt Server-Metadaten.
- `SystemRecoveryComponentSync`: Keypad, Display, Questlog, Terminal, Collider.
- `ManualSortVisualSync`: Vergleichsmarkierungen.
- `ConveyorSortVisualSync`: Paketfarben, Scanner und Tauschmarkierungen.
- `ModuleScannerVisualSync`: aktuelles Modul und GPU-Fehler.
- `StorageVisualSync`: 2D-Zellen.
- `SystemCoreVisualSync`: Alarm und Abschlussdarstellung.

### Umsetzung

1. Zuerst bestehende Snapshot-Tests pro Bereich ergänzen.
2. Danach jeweils einen Bereich unverändert in eine eigene Klasse verschieben.
3. Jede Visual-Sync-Klasse erhält `apply(...)` und `reset()`.
4. Beim Laden eines neuen Levels alle Caches zurücksetzen.
5. Shader-IDs als Konstanten in der jeweils zuständigen Klasse definieren.
6. Parser für Integer und Metadatenlisten nicht mehrfach implementieren.

### Tests

1. Jeder Bereich bekommt einen eigenen Test für initialen Spawn und späteres Update.
2. Ein zweiter Levelstart mit denselben Entity-IDs darf keine alten Highlights übernehmen.
3. Fehlende oder ungültige Metadaten dürfen den Client nicht abstürzen lassen.
4. Ein Late Join muss den vollständigen aktuellen Zustand erhalten.

## SR-11: Die große `EntityFactory` nach Rätselbereichen aufteilen

### Problem

`rooms.systemRecovery.entities.EntityFactory` hat ungefähr 662 Zeilen. Sie enthält Entitäten für fast alle Räume. Dadurch liegen Textur, Interaktionsaufbau und Zustandsnamen eines Rätsels weit entfernt von seiner Controllerklasse.

Der generische Name `EntityFactory` erschwert außerdem die Suche, weil das Projekt mehrere Factory-Klassen besitzt.

### Gewünschte Struktur

Mögliche Aufteilung:

- `EnergyEntityFactory`
- `ModuleEntityFactory`
- `TransportEntityFactory`
- `SortingEntityFactory`
- `StorageEntityFactory`
- `ScannerEntityFactory` für Suchroboter und Scanner
- `SystemCoreEntityFactory`
- `SystemRecoveryDisplayFactory` für wirklich gemeinsame Displays

### Umsetzung

1. Methoden nach Verwendung durch die Rätselklassen gruppieren.
2. Pro Schritt nur eine Gruppe verschieben und danach kompilieren.
3. Texturpfade, Entity-Namen und Interaktionsverhalten unverändert übernehmen.
4. Package-private Methoden verwenden, wenn sie nur innerhalb eines Rätselpackages gebraucht werden.
5. Den allgemeinen Namen `EntityFactory` am Ende entfernen oder auf wenige gemeinsame Methoden reduzieren.

### Darauf achten

- Netzwerkcode erkennt mehrere Entitäten anhand ihres Namens. Diese Namen dürfen beim Verschieben nicht verändert werden.
- Texturen und Animation-State-Namen sind ebenfalls Teil des Verhaltens.
- Factory-Klassen sollen keine Rätselzustände besitzen.

### Tests

- Bestehende Entity- und Rätseltests nach jedem verschobenen Bereich ausführen.
- Snapshot-Tests müssen weiterhin dieselben Entity-Namen und Metadaten sehen.

## SR-12: `SystemRecoveryLevel` auf Level-Lebenszyklus reduzieren

### Problem

`SystemRecoveryLevel` hat ungefähr 746 Zeilen. Neben Setup und Tick enthält es viele statische Weiterleitungen wie `spawnEnergyCrates()`, `activateModuleSockets()` und `completeSystemCoreSort()`.

Diese Methoden existieren hauptsächlich, damit statische Interpreter-Callbacks die richtige Rätselinstanz erreichen. Dadurch wird das Level zur globalen Service-Klasse und fachlicher Code verteilt sich zwischen Callback, Level und Rätselklasse.

### Gewünschte Struktur

`SystemRecoveryLevel` besitzt:

- Levelaufbau,
- Intro und Telefon,
- Storytrigger,
- Endtrigger,
- die levelgebundene Sammlung der Rätsel.

Ein eigener `SystemRecoveryActions`- oder `SystemRecoveryRiddleRegistry`-Typ ordnet einen `TerminalStep` der passenden Aktion auf der richtigen Rätselinstanz zu.

### Umsetzung

1. Einen levelgebundenen Registry-Typ anlegen und ihm alle Rätselinstanzen übergeben.
2. Eine Methode `complete(TerminalStep, TerminalAttempt)` einführen.
3. Die Registry ruft die passende Methode des Rätsels auf und löst den nächsten Storydialog aus.
4. `InterpretationCallbacks` delegiert nur noch einmal an diese Registry.
5. Die vielen statischen Einzelmethoden schrittweise entfernen.
6. Nur Abfragen behalten, die der Snapshot-Writer tatsächlich benötigt. Diese möglichst über ein kleines Read-only-State-Objekt bereitstellen.

### Tests

1. Für jeden `TerminalStep` prüfen, dass genau die erwartete Rätselmethode ausgelöst wird.
2. Ein unbekannter oder für die aktuelle Phase falscher Schritt darf keine Aktion auslösen.
3. Eine neue Levelinstanz darf keinen Zustand der vorherigen Instanz übernehmen.

## SR-13: Shaderzustände eindeutig einem Synchronisationsweg zuordnen

### Problem

Einige Effekte werden direkt in Rätselklassen erzeugt, andere über `ShaderComponent`, andere im Snapshot-Translator und weitere in `SystemRecoveryClientLevel`.

Beispiele:

- Energie verwendet serverseitige `ShaderComponent`-Einträge.
- Manuelles Sortieren erzeugt bei einem nicht-headless Host direkt Shader und zusätzlich erzeugt der Snapshot-Translator Clienteffekte.
- Systemkern setzt serverseitig Tint-Farben und clientseitig zusätzlich Outline-Shader.
- Modulscanner-Highlights werden teilweise aus Metadaten und teilweise aus der Scannerposition abgeleitet.

Diese doppelten Wege erklären verzögerte, falsche oder zu dicke Effekte und erschweren Late Joins.

### Gewünschtes Verhalten

Der Server hält nur kleine fachliche Werte:

- Energie-Füllstand pro Container,
- aktuelle Vergleichs-Entity-IDs,
- Paketwerte und aktuelles Paar,
- Scanner läuft / GPU defekt,
- Systemkern-Stage.

Genau ein Clientpfad erzeugt daraus die Shader.

### Umsetzung

1. Für jeden Effekt dokumentieren, welche serverseitige Zustandsinformation die Quelle ist.
2. Doppelte direkte Shader-Erstellung entfernen.
3. Für synchronisierbare Standardshader bevorzugt `ShaderComponent` und `ShaderSyncSystem` verwenden.
4. Rätselspezifische, aus mehreren Entities berechnete Effekte in eine eigene Client-Visual-Sync-Klasse legen.
5. Beim Ende eines Effekts alle zugehörigen Shader-IDs explizit entfernen.
6. Bei Levelwechsel und Late Join den Zustand vollständig aus dem Snapshot rekonstruieren.
7. `clearModuleScanHighlight()` so ändern, dass nur die zuvor markierte Entity bearbeitet wird.

### Tests

1. Host und Client sehen dieselben fünf Energie-Füllstände.
2. Late Join nach gelöstem Energierätsel sieht dieselben Füllstände.
3. Beim manuellen Sortieren ist nur das aktuelle Paar umrandet.
4. Nach einem Tausch ist im nächsten Snapshot nur das neue Paar markiert.
5. Beim Modulscan leuchtet nur das aktuelle Modul; die defekte GPU bleibt anschließend rot markiert.
6. Nach Abschluss des Systemkerns bleiben nur die vorgesehenen grünen Abschlussanzeigen aktiv.

# P2: Wartbarkeit, Tracking und Qualitätsprüfung

## SR-14: Custom Points einmalig und sichtbar validieren

### Problem

Mehrere Stellen fangen allgemein `RuntimeException` ab und ignorieren sie. Das wurde eingebaut, damit zwischenzeitlich fehlende Editorpunkte das Spiel nicht sofort beenden. Dadurch bleiben Tippfehler oder versehentlich gelöschte Pflichtpunkte aber unsichtbar.

Bei Storytriggern passiert die Punktsuche außerdem in jedem Tick erneut.

### Umsetzung

1. Jede Rätselklasse definiert ihre Pflichtpunkte als Konstanten oder Liste.
2. Beim `setup()` werden alle Punkte einmal aufgelöst und in Feldern gespeichert.
3. Fehlt ein Pflichtpunkt, enthält die Exception den Rätselnamen und den exakten Punktnamen.
4. Optionale Legacy-Aliase werden ausdrücklich über `RiddleSupport.point(level, canonical, aliases...)` behandelt.
5. Storytrigger werden beim Levelstart aufgelöst und als `ResolvedDialogTrigger(point, step)` gespeichert.
6. Allgemeine `catch (RuntimeException ignored)` entfernen. Nur die konkrete Exception für einen fehlenden optionalen Punkt behandeln.
7. Tippfehler-Aliase wie `suchroboter_controlls` und `roboter_item_destionation` in der Leveldatei auf die kanonischen Namen migrieren und danach entfernen.

### Tests

`SystemRecoveryRiddleLayoutTest` erweitern:

- alle Türen,
- alle Terminals,
- alle Displays,
- alle Storytrigger,
- `phone`,
- `end`,
- alle Matrix-Eckpunkte,
- alle rätselspezifischen Spawnpunkte.

Der Test soll den Namen jedes fehlenden Punktes in seiner Fehlermeldung nennen.

## SR-15: Debug-Modus über Startkonfiguration steuern

### Problem

`SystemRecovery.DEBUG_MODE` steht fest auf `true`. Dadurch können normale Host-Sitzungen Debug-Systeme und Debug-Schaltflächen erhalten. Ein vergessener Quellcodewechsel reicht aus, um eine interne Testversion mit Überspringen- und Item-Buttons zu verteilen.

### Umsetzung

1. Debug-Modus aus einem Startargument wie `--debug` oder einer bestehenden Dungeon-Konfiguration lesen.
2. Standardwert ist `false`.
3. Level-Editor darf Debug automatisch aktivieren.
4. Server und Client müssen denselben Modus verwenden oder der Server muss die Debugberechtigung autoritativ entscheiden.
5. Jeder Debug-Callback prüft weiterhin serverseitig den Modus.

### Tests

1. Normaler Start: keine Debug-Systeme und keine Debug-Buttons.
2. Start mit Debug: Buttons sichtbar und funktionsfähig.
3. Gefälschter Debug-Callback bei normalem Start: keine Wirkung.

## SR-16: Tracking-Ereignisse an klaren Grenzen erzeugen

### Problem

`SystemRecoveryTracking.attempt(...)` ruft bei jedem Versuch erneut `started(...)` auf. Die Engine dedupliziert Puzzle-Starts, aber fachlich entsteht der Start dadurch erst beim ersten Versuch und nicht unbedingt dann, wenn die Aufgabe verfügbar wird.

Ein Rätsel ohne Fehlversuch kann erst beim `solved(...)` rückwirkend als gestartet erscheinen. Das erschwert spätere Auswertungen der Zeit zwischen Freischaltung, erstem Versuch und Lösung.

### Gewünschtes Verhalten

- `puzzleStarted`: genau beim Aktivieren des ersten Petri-Places eines Rätsels.
- `attempt`: bei jeder bewerteten Benutzeraktion.
- `puzzleSolved`: genau beim fachlichen Abschluss des Rätsels.
- Hinweise werden über die Tracking-API als Hint-Nutzung erfasst.

### Umsetzung

1. Jedem `SystemRecoveryProgressPlace` das zugehörige `SystemRecoveryPuzzle` zuordnen.
2. Beim Wechsel in den ersten Place eines neuen Rätsels zentral `SystemRecoveryPuzzleEvents.started(...)` auslösen.
3. Den impliziten `started()`-Aufruf aus `attempt(...)` entfernen, sobald alle Aktivierungsgrenzen abgedeckt sind.
4. `solved(...)` darf keinen fehlenden Start mehr reparieren müssen.
5. Beim Akzeptieren eines Telefonhinweises `Tracking.hintUsed(...)` über den zentralen System-Recovery-Adapter aufrufen.
6. Rätselklassen verwenden weiterhin nur `RiddleCallbacks` und kennen die Dungeon-Tracking-API nicht.

### Tests

1. Rätsel wird freigeschaltet: genau ein Start-Event.
2. Drei Fehlversuche: drei Attempt-Events, kein weiterer Start.
3. Lösung: genau ein Solved-Event.
4. Hinweisstufe akzeptiert: genau ein Hint-Event.
5. Abgelehnter Hinweis: kein Hint-Event.

## SR-17: Unbenutzten Chat vollständig entfernen

### Problem

Der Chat-Tab wird fachlich nicht mehr benötigt. `AssistantChatTab.java` und seine Übersetzungen existieren weiterhin. Solcher toter Code erweckt den Eindruck, dass der Tab noch Teil des geplanten Spiels ist.

### Umsetzung

1. Prüfen, dass `SystemRecoveryComputerDialog` den Tab nicht mehr erzeugt.
2. `AssistantChatTab.java` löschen.
3. `computer.chat` entfernen.
4. Alle Keys `computer.assistant-*` aus `de.json` und `en.json` entfernen.
5. Tests oder Dokumentation mit Verweisen auf den Chat anpassen.

### Test

- Computer mit und ohne eingesetzten Chip öffnen und prüfen, dass kein Chat-Tab erscheint.
- Suche nach `AssistantChatTab`, `computer.chat` und `assistant-` liefert keine System-Recovery-Treffer.

## SR-18: Javadocs und Checkstyle vollständig reparieren

### Problem

Die Checkstyle-Aufgaben schlagen fehl. Häufige Fehler sind:

- fehlende `@param`-Tags,
- fehlende `@return`-Tags,
- unvollständige Record-Dokumentation,
- Stern-Imports in Tests,
- Formatierungsabweichungen.

Betroffen sind unter anderem Rätselklassen, `EntityFactory`, `RiddleCallbacks`, `Hint`, neue DialogFactory-Methoden und drei System-Recovery-Testklassen.

### Umsetzung

1. Zuerst `./gradlew :game:checkstyleMain :game:checkstyleTest` ausführen und die HTML- oder XML-Berichte verwenden.
2. Alle Warnungen in heute neu erstellten oder veränderten System-Recovery-Dateien beheben.
3. Öffentliche Methoden mit vollständigen `@param`- und `@return`-Tags dokumentieren.
4. Bei Records jeden Record-Parameter mit `@param` dokumentieren.
5. Stern-Imports in folgenden Tests durch konkrete Imports ersetzen:
   - `DoorLabelComponentTest`
   - `ManualSortingRiddleTest`
   - `BubbleSortConfirmationTest`
6. Google Java Format über die geänderten Java-Dateien laufen lassen.
7. Checkstyle erneut ausführen.

### Darauf achten

- Kommentare sollen erklären, warum eine ungewöhnliche Entscheidung nötig ist.
- Kommentare wie „returns whether running“ ohne Zusatznutzen nicht unnötig verlängern.
- Multiplayer-, Timing-, Zustands- und Parser-Randbedingungen müssen dokumentiert bleiben.

### Fertig, wenn

```text
./gradlew :game:checkstyleMain :game:checkstyleTest
```

ohne Warnungen und mit Exit-Code 0 endet.

## SR-19: README und technische Dokumentation auf den echten Stand bringen

### Problem

Die aktuelle `rooms/systemRecovery/README.md` behauptet noch, das Petri-Netz sei nicht aktiviert. Tatsächlich wird es bereits in `SystemRecoveryLevel.onFirstTick()` initialisiert und vom Hinweistelefon verwendet.

### Umsetzung

1. README nach Abschluss von SR-01 bis SR-16 aktualisieren.
2. Pro Rätsel festhalten:
   - zuständige Controllerklasse,
   - Terminal-Schritte,
   - physische Aktionen,
   - Petri-Places,
   - wichtige synchronisierte Zustände.
3. Den finalen linearen Petri-Ablauf dokumentieren.
4. Beschreiben, dass Texte als Keys übertragen und auf Clients übersetzt werden.
5. Beschreiben, wo Tracking zentral angebunden ist.
6. Veraltete Hinweise auf Legacy-Routen oder nicht implementierte Räume entfernen.

# Verbindliche Abschlussprüfung

Nach Umsetzung aller Aufgaben muss mindestens Folgendes geprüft werden:

## Automatisiert

```text
./gradlew :game:test --tests 'rooms.systemRecovery.*'
./gradlew :game:checkstyleMain :game:checkstyleTest
```

Zusätzlich müssen Tests existieren oder erweitert werden für:

- vollständigen Petri-Netz-Ablauf bis `ESCAPE_COMPLETE` ohne Debugsprünge,
- falsche Reihenfolge und doppelte Events entlang des vollständigen Ablaufs,
- Chipverlust und doppelte Computer-Callbacks direkt an den UI-Callbacks,
- Hinweisbestätigung während eines Zustandswechsels,
- deutsche und englische Clientübersetzung,
- vollständigen Late Join bei Energie, Modulscan, Bubble Sort, 2D-Speicher und Systemkern,
- erneuten Levelstart ohne alte Singleton- oder Shaderzustände, einschließlich des kompletten
  Snapshot-Translators.

## Manueller Test ohne Debug-Modus

1. Host Game starten.
2. Zweiten Client über Join Game verbinden.
3. Auf einem Client Deutsch und auf dem anderen Englisch einstellen.
4. Intro, Steuerung und Telefonat auf beiden Clients prüfen.
5. Den kompletten Raum ohne Debug-Buttons lösen.
6. Bei jedem Rätsel einmal absichtlich eine falsche Eingabe testen.
7. Während eines offenen Hinweisdialogs mit dem zweiten Spieler das Rätsel abschließen.
8. Während eines offenen Chipdialogs mit dem zweiten Spieler den Zustand verändern.
9. Einen Client nach mehreren gelösten Rätseln neu verbinden und Shader, Türen, Displays, Questlog und Terminal-State prüfen.
10. Systemkern-Zugriff, drei Kernaufgaben, Meta-Eingabe, Aufzug und Endpunkt vollständig prüfen.

## Level-Editor-Test

1. `runSystemRecoveryLevelEditor` starten.
2. Prüfen, dass Intro und Storydialoge den Editor nicht blockieren.
3. Prüfen, dass alle Pflichtpunkte erkannt werden.
4. Einen Pflichtpunkt testweise umbenennen und kontrollieren, dass die Fehlermeldung den exakten Namen und das Rätsel nennt.
