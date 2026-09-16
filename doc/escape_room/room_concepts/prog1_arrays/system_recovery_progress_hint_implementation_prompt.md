# Umsetzungsauftrag: System-Recovery-Fortschritt, Hinweise, Questlog und Übersetzungen

Du arbeitest im Repository `Dungeon` am Raum `game/src/rooms/systemRecovery`. Setze die unten
beschriebene Überarbeitung vollständig, testgetrieben und in kleinen Arbeitspaketen um. Höre nicht
nach Analyse oder Teilimplementierungen auf. Bewahre bestehende, nicht zu dieser Aufgabe gehörende
Änderungen im Worktree und ändere keine fremden Räume ohne zwingenden technischen Grund.

## Verbindliche Quellen

Lies vor der ersten Änderung vollständig:

1. `doc/escape_room/room_concepts/prog1_arrays/system_recovery_progress_hint_rework.md`
2. `game/src/feature/petrinet/` und `game/test/feature/petrinet/`
3. die bestehenden System-Recovery-Klassen in den unten genannten Paketen
4. die Questlog-, Hint-, Tracking- und Localization-APIs der Engine sowie ihre Tests

Das Rework-Dokument ist die fachliche Quelle der Wahrheit. Weicht der aktuelle Code davon ab, wird
der Code angepasst. Findest du einen technisch unmöglichen oder widersprüchlichen Punkt, ändere ihn
nicht stillschweigend: dokumentiere ihn im Abschlussbericht mit Datei, Zeile, Ursache und einer
konkreten Empfehlung.

## Unverhandelbare Regeln

- Das Spiel ist multiplayerfähig. Fortschritt, Hintverbrauch, Questlog und Tracking sind
  serverautoritativ.
- Kein Petri-Netz-Übergang darf von Spielerposition, Nähe zu einem Custom Point oder Raumtrigger
  abhängen. Positionsbasierte Trigger dürfen ausschließlich optionale Storydialoge anzeigen.
- Es gibt keine separaten Event-Places wie `*_ACCEPTED`, `*_COMPLETED` oder `STEP_SOLVED`.
- Jeder Aufgaben-Place hat stabil entweder 0 oder 1 Token. Ein erfolgreicher Abschluss erzeugt
  atomar das zweite Token; eine Eingangskante mit Gewicht 2 verbraucht beide und eine Ausgangskante
  mit Gewicht 1 aktiviert den direkten Nachfolger.
- Nur die Tokens der echten `PlaceComponent`s bestimmen den aktiven Schritt. Kein zweites
  `activeState` und keine stille Reparatur durch Token-Clears oder Fallback-Aktivierungen.
- Alle transportierten Texte bleiben Translation-Keys. Jeder Client übersetzt lokal.
- Questlogtexte dürfen nicht in periodischen UDP-Snapshots transportiert werden.
- Verwende die vorhandene Dungeon-Tracking-API. Führe keine parallele Logdatei oder eigene
  Statistikzähler ein.
- Erhalte bestehende Rätselmechanik, Multiplayer-Synchronisation und Debug-Funktionen, soweit das
  Zielkonzept keine ausdrückliche Änderung verlangt.

## Arbeitspaket 1: Lernschritte und Petri-Netz

Arbeite hauptsächlich in:

- `game/src/rooms/systemRecovery/petrinet/SystemRecoveryProgressNet.java`
- `game/src/rooms/systemRecovery/petrinet/SystemRecoveryProgressPlace.java`
- neue Klasse `SystemRecoveryLearningStep` im selben Paket
- `game/test/rooms/systemRecovery/petrinet/SystemRecoveryProgressNetTest.java`

Implementiere exakt die im Rework-Dokument unter „3.1 Places“ definierte Reihenfolge mit 27
Aufgaben-Places und `COMPLETE`. Verbinde jeden Place linear mit einer Transition, deren Eingang vom
aktuellen Place Gewicht 2 und deren Ausgang zum Nachfolger Gewicht 1 besitzt.

Die einzige öffentliche Fortschrittsmethode lautet sinngemäß:

```java
boolean complete(SystemRecoveryLearningStep expectedCurrentStep)
```

Sie muss serverseitig atomar arbeiten, falsche Reihenfolge und Duplikate ohne Zustandsänderung
ablehnen und bei einer fehlgeschlagenen Transition das zusätzlich erzeugte Token zurücknehmen.
`activeStep()` wird ausschließlich aus Place-Tokens berechnet. Entferne danach die alte doppelte
Zustandsführung und nicht mehr benötigte `ProgressEvent`-/Event-Place-Logik.

Schreibe zuerst Tests für Initialzustand, jeden einzelnen Übergang, Gesamtablauf, falsche
Reihenfolge, Duplikate, fehlende Transition, Ein-Token-Invariante und Positionsunabhängigkeit.

## Arbeitspaket 2: Erfolgs-Callbacks anbinden

Untersuche insbesondere:

- `game/src/rooms/systemRecovery/util/tracking/SystemRecoveryPuzzleEvents.java`
- `game/src/rooms/systemRecovery/util/interpreter/TerminalStep.java`
- `game/src/rooms/systemRecovery/riddles/`
- `game/src/rooms/systemRecovery/level/SystemRecoveryLevel.java`
- die System-Recovery-Computer-, Keypad-, Tür- und Item-Callbacks

Jeder normale Terminalschritt erhält genau eine typisierte Zuordnung zu seinem Learning-Step.
Dupliziere diese Zuordnung nicht in mehreren Switches. Binde zusätzlich die Erfolgsquellen aus der
Place-Tabelle an: Batteriebox, beide Keypadcodes, Datenspeichertür, manuelles Sortieren,
Sortierchip-Save, Bubble-Sort-Maschine, Archivtür, Ortungschip-Save, Suchroboter,
Systemkernfreigabe und Meta-Rätsel.

Ein Callback darf nur `complete(...)` für den Schritt aufrufen, den er fachlich abschließt. Falsche
Eingaben, Zwischenframes, Displayinteraktionen, Item-Spawns und Storydialoge schalten nicht.
Türschritte werden ausschließlich durch den erfolgreichen serverseitigen Wechsel der konkreten Tür
auf offen abgeschlossen. Raumtrigger schalten niemals das Netz.

Ergänze fokussierte Tests pro Callback-Gruppe und mindestens einen vollständigen Flow-Test über
alle realen Registrierungs- und Callbackpfade.

## Arbeitspaket 3: Spezifische Hinweise und Tracking

Arbeite hauptsächlich in:

- `game/src/rooms/systemRecovery/petrinet/SystemRecoveryHintCatalog.java`
- `game/src/rooms/systemRecovery/story/SystemRecoveryHintPhone.java`
- `game/src/rooms/systemRecovery/util/tracking/SystemRecoveryPuzzleEvents.java`
- `game/src/rooms/systemRecovery/util/tracking/SystemRecoveryTracking.java`
- den System-Recovery-Sprachdateien

Jeder der 27 Aufgaben-Places erhält exakt vier spezifische Hinweise: `orientation`, `approach`,
`near`, `solution`. Entferne `hints.generic` und alle inhaltlichen Fallbacks. Die ersten drei Stufen
führen von grober Orientierung zu fast vollständiger Hilfestellung; nur `solution` enthält eine
direkt akzeptierte Lösung.

Vor einem normalen Hinweis wird bestätigt, ob der nächste Hinweis gewünscht ist. Vor der Lösung
erfolgt eine ausdrückliche Lösungswarnung. Zwischen Angebot und Bestätigung muss erneut geprüft
werden, ob Place und nächster Hint unverändert sind.

Tracke erst einen tatsächlich bestätigten und vom `HintSystem` angenommenen Hinweis. Verwende
`Tracking.hintUsed(...)`, die stabile Puzzle-ID und eindeutige Hint-IDs:

```text
<learning-step>:orientation
<learning-step>:approach
<learning-step>:near
<learning-step>:solution
```

Der bestätigende Spieler wird als Teilnehmer erfasst; Hinweise und Questlog bleiben dennoch
gemeinsam. Tests müssen vier getrennte Events, Deduplizierung, Ablehnen, veraltete Angebote, zwei
Spieler und die Aggregation `participantId + puzzleId` prüfen.

## Arbeitspaket 4: Petri-Debug-Ansicht

Ersetze den unübersichtlichen Textblock durch die im Rework-Dokument definierte Ansicht:

- Statuskopf mit aktivem Step, Interpreter-Step, Tokenanzahl, Hintindex und Konsistenzstatus
- lineare Liste aller Schritte mit erledigt/aktiv/gesperrt/fehlerhaft
- Diagnoseblock mit letztem akzeptierten und abgelehnten Abschlussversuch
- technische Details mit Tokenzahlen und Kantengewichten

Erzeuge serverseitig einen unveränderlichen `ProgressDebugSnapshot` mit stabilen Keys und primitiven
Werten. Übertrage ihn nur auf explizite Debug-Anfrage zuverlässig, niemals in jedem Welt-Snapshot.
Der Client übersetzt und rendert. Halte die Antwort unter 8 KiB und teste Inhalt, Inkonsistenzen und
Nachrichtengröße.

## Arbeitspaket 5: Multiplayer-Questlog reparieren

Untersuche insbesondere:

- `game/src/rooms/systemRecovery/util/SystemRecoveryQuestLogUtil.java`
- `game/src/rooms/systemRecovery/network/SystemRecoveryComponentSync.java`
- `game/src/rooms/systemRecovery/network/SystemRecoverySnapshotTranslator.java`
- `game/src/rooms/systemRecovery/network/SystemRecoveryEntitySpawnStrategy.java`
- die allgemeinen `QuestLogUI`-, `HeroController`- und Netzwerkklassen
- `game/test/rooms/systemRecovery/network/`

Das Questlog bleibt serverautoritativ. Entferne wachsende Questlogtexte aus Entity-Spawns und
periodischen UDP-Snapshots. Der Client fordert das Log beziehungsweise einen Tab an; der Server
ermittelt den Spieler aus dem autorisierten `InputCommandContext` und antwortet zuverlässig nur
diesem Client. Übertrage Translation-Keys, keine serverseitig übersetzten Texte.

Teste Host und Join-Client, zwei gleichzeitige Clients, unterschiedliche Clientsprachen, neue
Einträge ohne Respawn, fehlende Initialisierung, persönliche Sichtbarkeit sowie ein großes Log mit
mindestens 50 Einträgen. Reguläre UDP-Snapshots müssen unter `SAFE_UDP_MTU` bleiben.

## Arbeitspaket 6: Übersetzungen verschieben und vervollständigen

Verschiebe alle System-Recovery-Inhalte aus:

```text
game/assets/language/escapeRoom/de.json
game/assets/language/escapeRoom/en.json
```

nach:

```text
game/assets/language/systemRecovery/de.json
game/assets/language/systemRecovery/en.json
```

Passe `game/src/rooms/systemRecovery/SystemRecovery.java` an. Die Namespaces
`systemRecovery.*` und `questlog.*` bleiben stabil. Lösche unbenutzte Testtexte; lösche die alten
Dateien nur, wenn dort keine allgemeinen Escape-Room-Texte verbleiben.

Erweitere `game/test/rooms/systemRecovery/util/SystemRecoveryTranslatorTest.java` um JSON-Parität,
vollständige Java-Key-Referenzen, vier Hintstufen je Learning-Step, fehlendes `hints.generic` und
lokale Auflösung derselben Keys auf Deutsch und Englisch.

## Arbeitspaket 7: Integration, Bereinigung und Regression

Führe alle Teilsysteme zusammen und prüfe den vollständigen Spielablauf von `ENERGY_ARRAY` bis
`COMPLETE`. Suche projektweit nach alten Progress-Places, Eventnamen, `activeState`,
`activateAfterTransition`, positionsabhängigen Petri-Aufrufen, generischen Hint-Fallbacks,
Questlog-Snapshotmetadaten und alten Localization-Pfaden. Entferne nur nachweislich unbenutzten
Code.

Ergänze einen Flow-Test, der die realen Registrierungsfunktionen verwendet, jeden Schritt über
seine vorgesehene Erfolgsquelle abschließt und nach jedem Übergang aktiven Place, Tokeninvariante,
Terminal-State und nächsten Hint prüft. Ergänze Varianten mit Duplikaten, falscher Reihenfolge und
zwei Teilnehmern. Positionsänderungen an sämtlichen Dialogtriggern dürfen den Progresszustand nicht
verändern.

Prüfe abschließend, dass Debug-Skip-Funktionen ausdrücklich als Debugpfad erkennbar bleiben, aber
dieselben Invarianten wie reguläre Abschlüsse herstellen.

## Verbindliche Arbeitsweise

1. Dokumentiere vor Änderungen den Istzustand im Abschlussbericht.
2. Implementiere die Pakete in der angegebenen Reihenfolge.
3. Schreibe oder aktualisiere Tests vor der jeweiligen Produktionsänderung.
4. Führe nach jedem Paket die fokussierten Tests aus und behebe Fehler sofort.
5. Suche nach jedem Paket nach alten Place-Namen, alten Eventmethoden und doppelten Mappings.
6. Führe am Ende die vollständigen Prüfungen aus.
7. Ändere keine unrelated Dateien und führe keine kosmetische Großformatierung durch.
8. Verwende aussagekräftige Javadocs für öffentliche Typen, Invarianten und nicht offensichtliche
   Multiplayergrenzen. Kommentiere keine selbsterklärenden Einzelzeilen.

Mindestens auszuführen:

```bash
./gradlew game:test --tests 'rooms.systemRecovery.petrinet.*'
./gradlew game:test --tests 'rooms.systemRecovery.network.*'
./gradlew game:test --tests 'rooms.systemRecovery.util.SystemRecoveryTranslatorTest'
./gradlew game:test
./gradlew game:checkstyleMain game:checkstyleTest
./gradlew game:buildSystemRecoveryJar game:buildSystemRecoveryServerJar
```

Falls ein Kommando wegen Infrastruktur statt wegen Code scheitert, dokumentiere Kommando,
Fehlermeldung und Abgrenzung. Behaupte niemals, ein Test sei erfolgreich, wenn er nicht lief.

## Abschlussbericht für die Nachkontrolle

Erstelle und pflege während der Umsetzung:

```text
doc/escape_room/room_concepts/prog1_arrays/system_recovery_progress_hint_implementation_report.md
```

Der Bericht muss am Ende enthalten:

1. **Istzustand vor Änderung:** gefundene alte Places, doppelte Zustände, Fortschrittsquellen,
   Questlog-Transport, Hint-ID-Problem und Localization-Pfade.
2. **Umgesetzte Arbeitspakete:** je Paket Ziel, geänderte Dateien und zentrale Designentscheidung.
3. **Vollständige Callback-Matrix:** Learning-Step, auslösende Klasse/Methode, Erfolgskriterium,
   aufgerufener `complete(...)`-Wert und zugehöriger Test.
4. **Entfernte Altlogik:** gelöschte Places, Events, Fallbacks, Metadaten und tote Methoden samt
   Suchnachweis, dass keine Referenzen übrig sind.
5. **Petri-Invarianten:** Initialmarkierung, Kantengewichte, Verhalten von `complete(...)`,
   Duplikat- und Fehlerbehandlung.
6. **Hinweismatrix:** alle 27 Steps mit vier Translation-Keys, Puzzle-ID und vier Tracking-IDs.
7. **Multiplayernachweis:** Autorität, adressierter Empfänger, Host-/Join-Verhalten,
   Nachrichtengrößen und lokale Übersetzung.
8. **Testprotokoll:** jedes ausgeführte Kommando, Ergebnis, Testanzahl und gegebenenfalls Fehler.
9. **Manuelle Restprüfung:** konkrete Schritte für einen Zwei-Client-Durchlauf, die automatisiert
   nicht zuverlässig prüfbar sind.
10. **Abweichungen und Restrisiken:** nichts verschweigen; jeweils Auswirkung und nächste Maßnahme.
11. **Review-Anker:** kurze Liste der wichtigsten Dateien und Methoden, die ein nachfolgendes Modell
    zuerst prüfen soll.

## Fertigstellungskriterium

Die Aufgabe ist erst fertig, wenn alle sieben Arbeitspakete umgesetzt, die Definition of Done des
Rework-Dokuments erfüllt, der Abschlussbericht vollständig und alle möglichen Prüfungen erfolgreich
ausgeführt sind. Gib am Ende eine knappe Zusammenfassung, die Testresultate, den Berichtspfad und
offene Restrisiken nennt.
