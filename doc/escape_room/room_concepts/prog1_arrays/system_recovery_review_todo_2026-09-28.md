# System Recovery: Review und umsetzbare Aufgaben (28.09.2026)

Dieses Dokument ist eine **Aufgabenliste**, keine bereits implementierte Korrektur. Scope:
System Recovery und die dafuer noetigen, rueckwaertskompatiblen Konfigurationspunkte im
gemeinsamen Hauptmenue. `tracking-viewer/` und andere Raeume bleiben unangetastet.
Jede Aufgabe bekommt zuerst einen fehlschlagenden Test oder eine reproduzierbare
Spielpruefung, dann den Fix, dann den erneuten Test. Beim Weitergeben an kleinere Modelle
jeweils **eine Aufgabe** bearbeiten lassen und Ergebnis, Tests und Abweichungen notieren.

## P0: Spielbarkeit und Fortschritt

### 1. Level-Editor: versteckte Dialoge und Eingabesperre entkoppeln

**Befund:** Der Editor-Start ruft in `engine/game/MainMenu.java` `activateOnStart()` auf;
`LevelEditorSystem` wird aber erst waehrend seiner `execute()`-Phase aktiv. Das Level
entscheidet beim ersten Tick und vor jedem Intro allein anhand von
`LevelEditorSystem.active()` (`SystemRecoveryLevel.java:226,634`), ob Terminal und Intro
freigegeben werden. Der Story-Dispatcher nutzt dieselbe Bedingung
(`SystemRecoveryStoryDialogs.java:130`). `LevelEditorSystem.active(true)` unterdrueckt
Dialoge nur **visuell** (`LevelEditorSystem.java:203-211`); ein bereits angelegtes UI kann
laut `LevelEditorSystem.java:410-413` weiterhin die Editor-Eingabe sperren. Ob genau
das den gemeldeten Fehler ausloest, ist noch im Spiel zu reproduzieren.

**Umsetzung:** Einen expliziten Startmodus `LEVEL_EDITOR` fuer den lokalen Spielstart
vor dem ersten Level-Tick setzen. Im System-Recovery-Level in diesem Modus weder Intro,
Steuerungsdialog, Story-Dialog noch Telefonanruf/Triggerdialog erzeugen; Terminal sofort
freigeben. Nicht `debugMode` als Synonym verwenden: normales Debug-Spiel soll die Story
weiter zeigen. Beim Umschalten des Editors per F4 pruefen, ob bereits offene/queued
System-Recovery-Dialoge den Editor blockieren, und diese gezielt schliessen oder
pausieren. Keine globale Abschaltung aller Engine-Dialoge einbauen. Die vorhandene
Multiplayer-Behandlung pro Spieler beibehalten.

**Tests/Abnahme:** Editor ueber `:game:runSystemRecoveryLevelEditor` und ueber den
versteckten Hauptmenue-Eintrag starten. Direkt Tiles, Custom Points und Kamera bewegen;
Dialoge sind nicht sichtbar und `hasOpenUI` blockiert nicht. F4 aus/an testen.
Normaler Debug-Start und normales neues Spiel muessen Intro und Telefon weiter zeigen.
Einen fokussierten Modus-/Intro-Test ohne LibGDX-Renderkontext schreiben; bei
UI-Blockierung zusaetzlich einen manuellen Repro mit offenem Intro dokumentieren.

### 2. Batterie bleibt nach Einsetzen sichtbar, auch nach Load

**Befund:** `EnergyEntityFactory.batteryBox()` schaltet die Box-Textur um, entfernt dann
aber ihre `InventoryComponent` (`EnergyEntityFactory.java:72-79`). Die Batterie ist
damit weder im Inventar noch als eigenes Weltobjekt sichtbar. Beim Restore setzt
`EnergyRiddle.restoreCompletedState()` nur `batteryInserted = true`, ohne die
gespawnte Box auf den On-Zustand zu setzen (`EnergyRiddle.java:156-166`).

**Umsetzung:** Den eingesetzten Zustand als sichtbare Batterie in/auf der Box darstellen
(z.B. eindeutige On-Textur/Overlay; Asset zuerst ansehen). Entnahme weiterhin verhindern,
aber den Zustand nicht allein aus einer entfernten Inventarkomponente ableiten. Eine
idempotente `markBatteryInserted`-Operation fuer Live-Spiel und Restore benutzen und
den Draw-Zustand serverseitig synchronisieren. Keine zweite Batterie spawnen. Den
bereits geloesten Petri-Schritt beim Restore nicht erneut schalten.

**Tests/Abnahme:** Batterie aufnehmen und einsetzen: Spielerinventar leer, Batterie
sichtbar in der Box, Tuer offen. Save/Load: dieselbe Optik und kein dupliziertes Item.
Zweiten Client vor und nach Einsetzen verbinden: beide sehen den gleichen Box-Zustand.
Fokussierter Test fuer die idempotente Zustandsprojektion.

### 3. Finale Sortierdaten konsistent auf sechs Werte umstellen

**Befund:** R10 hat derzeit `{42, 17, 8, 31, 23}` und genau fuenf `b`-Kristalle
(`SystemCoreRiddle.java:28,196-203`). Der Level enthaelt nur `b0` bis `b4`
(`game/assets/levels/systemRecovery/systemrecovery_1.level`, Custom-Point-Zeile);
die Pflichtpunktliste erwartet fuenf (`SystemRecoveryPointRegistry.java:161-167`).
Meta-Draft und Parser erwarten ebenfalls exakt fuenf Werte
(`SystemCoreMetaDraft.java:15`, `SystemCoreMetaInput.java:19`). Beide Sprachen zeigen
die alten fuenf Werte (`language/systemRecovery/{de,en}.json`, `display-sort`).

**Umsetzung:** Den **Zahlensatz** `4, 8, 15, 16, 23, 42` verwenden, aber die Startreihe
bewusst **unsortiert** anordnen; sonst prueft Bubble Sort nichts. Einen passenden,
begehbaren Punkt `b5` im Level-Editor setzen und in der Registry verlangen. Alternativ
die Position aus `b0`/`b4` ableiten, falls das Raumlayout keinen sechsten Punkt erlaubt;
die Entscheidung dokumentieren und einen Layout-Test ergaenzen. Sortieranzeige,
Meta-Eingabe mit sechs Feldern, Parser, DE/EN-Texte, Musterloesung und Tests gemeinsam
aendern. `map00`/`map24` und die fuenf Modul-Punkte sind **nicht** Teil dieser Aenderung.

**Tests/Abnahme:** Level laedt ohne fehlende Punkte; sechs Kristalle sind sichtbar und
sortierbar. Finales Display zeigt `4 8 15 16 23 42`. Meta-Maske akzeptiert genau sechs
korrekte Werte und weist fuenf, sieben, vertauschte sowie doppelte Werte ab. Mit
`SystemCoreMetaInputTest`, `SystemRecoveryPointRegistryTest` und Layout-Test absichern;
anschliessend R10 bis zum Ausgang durchspielen.

### 4. Achievement „Nicht nach Plan“ bei direkter Sortierung, ohne R10 zu loesen

**Befund:** R10-Schritt 1 akzeptiert nur die Bubble-Sort-Schleife
(`TerminalInterpreterSetup.java:554-571`); Ablehnung laeuft ueber den allgemeinen
Fehler-Callback (`InterpretationCallbacks.java:201-226`). Der Achievement-Tracker
bekommt dort den autoritativen Versuch, kennt aber noch keinen Sonderfall
(`SystemRecoveryAchievementTracker.java:225-245`). Wird das Achievement als
zusaetzliche gueltige Terminal-Loesung implementiert, schreitet Petri faelschlich fort.

**Umsetzung:** Vor oder im R10-Fehlerpfad den **serverseitig eingereichten Quelltext**
gezielt auf eine Java-gueltige direkte Sortierung des R10-Arrays pruefen. Die
Zielbedingung praezisieren: etwa eine direkte Deklaration/Indexzuweisung mit genau den
sechs Werten in aufsteigender Reihenfolge, **ohne** Bubble-Sort-Schleife. Keine blosse
Teilzeichenkettensuche; bestehende Statement-Zerlegung/Parser-Regeln nutzen, damit
Kommentare, Whitespace und andere Arrays keine False Positives ausloesen. Einen neuen
Achievement-Key samt DE/EN-Name/Beschreibung, Definition, Bild und Lizenz nach
vorhandenem Muster hinzufuegen. Unlock nur einmal pro Run; bestehende
Achievement-Snapshot-Persistenz nutzen. Fuer diese Eingabe normales rotes
Terminal-Feedback oder eigenes, nicht irrefuehrendes Feedback senden. Keinen
`applyTerminalStep`, keine gueltige Loesung im Questlog/Terminalverlauf, keine
Sortier-Tint-/Petri-Aenderung ausloesen. Danach muss der echte Bubble-Sort-Code noch
angenommen werden.

**Tests/Abnahme:** Vollstaendig sortierte direkte Eingabe schaltet genau das neue
Achievement, aber `CORE_SORT` bleibt aktiv und R10-Stage 0. Unsortierte, unvollstaendige,
andere Zahlen und Code nur im Kommentar schalten es nicht. Wiederholtes Senden und
Save/Load erzeugen kein zweites Unlock. Anschliessende korrekte Schleife loest Schritt 1.
Tests in `SystemRecoveryAchievementTrackerTest` plus Interpreter-/Progress-Flow-Test.

### 5. Meta-Eingabefenster auf kleinen Aufloesungen erreichbar halten

**Befund:** Der Computer-Dialog benutzt Fensterbreite/-hoehe mit festem Padding 100
(`SystemRecoveryComputerDialog.java:65,146-150`). Die Meta-Maske setzt alle
Energiefelder in **eine Zeile**, jedes mit fester Breite und Abstand
(`SystemCoreMetaTab.java:54-69`), plus feste Buttonbreiten (`:109-111`). Schon vor dem
sechsten Feld kann der Inhalt bei schmalen Fenstern ausserhalb des sichtbaren Bereichs
liegen. Es gibt hier kein ScrollPane.

**Umsetzung:** Den Dialog an die aktuelle Stage-/Viewport-Groesse binden und beim
Resize neu layouten. Meta-Inhalt in einen vertikalen ScrollPane legen; Energiefelder
bei wenig Breite umbrechen (z.B. 2-3 Spalten statt einer horizontalen Reihe). Buttons
duerfen ebenfalls umbrechen und muessen ohne horizontales Scrollen bedienbar sein.
Bei Feldzahl 6 keine neuen festen Pixelbreiten addieren. Fokus, Tastatur und
Zwischenstand (`SystemCoreMetaDraft`) beim Resize/Reopen erhalten.

**Tests/Abnahme:** 640x480, 800x600, 1024x768, 1280x720 und Full HD mit DE und EN
oeffnen. Ueberschrift, alle Werte, beide Zaehler, Feedback, Uebertragen und Loeschen
sind per Scrollen erreichbar; nichts ueberlappt oder ragt aus dem Fenster. Dialog
schliessen/oeffnen und Resize mit teilweiser Eingabe testen. Falls UI-Tests moeglich:
Actor-Bounds gegen Viewport-Grenzen pruefen; ansonsten Screenshots protokollieren.

## P1: Bedienung und Regressionen

### 6. Hauptmenue nur fuer System Recovery umschaltbar beschriften

**Befund:** `MainMenuScreen.java:316-334` baut Host und Join immer ein; `GameStarter`
hat nur allgemeine Spiel-/Continue-Konfiguration (`GameStarter.java:28-41`). Die
Host-Aktion startet weiterhin einen lokalen Server und verbindet den Client
(`MainMenuScreen.java:665-718`). Ein global geaenderter Text wuerde Last Hour und
andere Spiele unbeabsichtigt mit aendern.

**Umsetzung:** In `GameStarter` eine optionale Menue-Konfiguration mit
Rueckwaertskompatibilitaet einfuehren: Host-Button-Label (lokalisierbar) und
`showJoinButton`, Standard: bisheriges Host-Label und `true`. In `SystemRecovery.java`
ein klar benanntes Flag (z.B. `SHOW_JOIN_IN_MENU`) und System-Recovery-spezifische
DE/EN-Labels setzen. Bei deaktiviertem Flag nur den **Menue-Eintrag** verstecken;
Netzwerk-/Serverfunktion und dedizierten Join-Pfad nicht stillschweigend entfernen.
Host soll „Spiel starten“ heissen, aber Nameingabe, Save-Ueberschreiben,
Datenschutzabfrage und Continue weiterhin korrekt funktionieren. Bei Flag `true`
erscheint „Spiel beitreten“ wieder. Keine globale `escapeRoom`-Uebersetzung aendern.

**Tests/Abnahme:** Konfigurations-Unit-Test fuer Default und Override; UI-Sichtpruefung
System Recovery mit Flag false/true und Last Hour unveraendert, jeweils DE/EN.
„Spiel starten“ muss dasselbe Spiel wie bisher „Spiel hosten“ starten.

### 7. Terminal-Feedback gegen schnelle Mehrfachsendungen absichern

**Befund:** `TerminalTab.java:259-266` erlaubt waehrend einer ausstehenden Antwort
erneutes Senden. Es speichert nur einen `lastSubmittedFingerprint`; bei Erfolg
schreibt `applyServerFeedback()` den **aktuellen Editorinhalt**, nicht die tatsaechlich
eingereichte Quelle, in die lokale Historie (`:274-283`). Bei schneller Aenderung/
zweitem Senden kann die Antwort ignoriert oder dem falschen Quelltext zugeordnet
werden. Die serverseitige Historie ist autoritativ, die sichtbare lokale Ansicht
darueber aber potenziell inkonsistent.

**Umsetzung:** Pro offener Terminal-UI nur einen in-flight Send-Vorgang zulassen
(Button bis Antwort sperren) oder eine Request-ID/Queue mit eingefrorener Quelle
verwenden. Auf Erfolg genau die bestaetigte Quelle anzeigen; nach Ablehnung Editor
unveraendert lassen. Serverantworten fuer bereits geschlossenes/fremdes Dialogfenster
weiterhin ignorieren. Dasselbe Muster fuer `SystemCoreMetaTab` pruefen.

**Tests/Abnahme:** Zwei schnelle Klicks, Senden + Tippen vor Antwort und Antworten in
umgekehrter Reihenfolge simulieren. Kein doppelter Petri-Fortschritt; lokale Historie,
serverseitige Historie, Questlog und Memory Watch enthalten dieselbe bestaetigte
Quelle. Abgelehnte Quelle bleibt editierbar.

### 8. Terminal-Entwurf an Run und Client statt an statische JVM binden

**Befund:** `TerminalTab.savedCode` ist ein statisches Feld
(`TerminalTab.java:36,100-112`), das nur durch Loeschen oder erfolgreiche Antwort
geleert wird. Es gibt keinen Reset bei neuem Run; im lokalen Editor/neuen Spiel oder
bei Clientwechsel in derselben JVM kann alter, nicht abgesendeter Code wieder
auftauchen. Dieser Entwurf ist nicht Teil des Savegames; das kann gewollt sein, muss
aber eindeutig sein.

**Umsetzung:** Entwurf in einen client-lokalen, Run-gebundenen UI-State verschieben;
bei neuer Partie zuruecksetzen, beim blossen Schliessen/Oeffnen desselben Terminals
erhalten. Festlegen, ob ungesendeter Code beim Continue erhalten bleiben soll; wenn
ja, explizit clientseitig und pro Run-ID speichern, nicht serverseitig als Loesung.
Andere Spielraeume nicht beruehren.

**Tests/Abnahme:** Terminal schliessen/oeffnen behaelt Entwurf; neues Spiel zeigt
leeren Editor; getrennte lokale Sessions/Clients uebernehmen nichts voneinander.
Beim Continue gilt die dokumentierte Entscheidung. Test fuer State-Lifecycle.

### 9. Save/Load-Invarianten fuer Items und neue Achievement-/R10-Daten absichern

**Befund/Risiko:** `SystemRecoveryLevel.persistCheckpoint()` ersetzt bei gleichem
Checkpoint ein leeres aktuelles Spielerinventar durch das letzte nichtleere
(`SystemRecoveryLevel.java:339-347`). Das schuetzt vermutlich den kurz montierten
USB-Stick, kann aber auch einen absichtlich abgegebenen Gegenstand im Save wieder
auferstehen lassen. `SystemRecoverySave.capture()` speichert nur vier bestimmte
Itemtypen (`SystemRecoverySave.java:116-147`); der Batterie-Zustand wird bislang nur
aus dem Fortschritt rekonstruiert. R10 wird in mehrere Checkpoints projiziert
(`SystemRecoveryLevel.java:537-561`). Kein Fehler fuer jeden Pfad nachgewiesen;
hier sind gezielte Regressionstests noetig.

**Umsetzung:** Den Itemzustand explizit als `im Inventar`, `im Computer gemountet`,
`in Welt`, `verbraucht` oder `in Batteriebox` behandeln, statt pauschal altes Inventar
zu kopieren. Falls Save im gemounteten Zustand verboten sein soll, das explizit
dokumentieren/erzwingen; Safe-Eject muss immer einen konsistenten Entwurf speichern.
R10-Sortierzahlensatz und neues Achievement beim Laden neuer und alter Save-Versionen
pruefen; eine inkompatible alte Meta-Draft-Eingabe darf nicht unbemerkt als sechs Werte
gelten. Schema nur erhoehen, wenn gespeicherte Struktur wirklich geaendert wird.

**Tests/Abnahme:** Vor/nach Mount, Safe-Eject, erfolgreichem Upload, Maschinen-Einsatz,
Archivschluessel-Einsatz, Batteriestecken und jedem R10-Teilstep speichern/laden.
Genau ein korrektes Item oder keines, nie ein Duplikat; Questlog, History, Memory
Watch, Achievementstatus, Petri-Marking und sichtbare Welt stimmen ueberein.
Vorhandene Tests `SystemRecoverySaveTest`, `SystemRecoveryCheckpointProjectionTest`,
`SystemRecoveryInventoryRestoreTest` um diese Faelle erweitern.

## Gemeinsame Abschlusspruefung

1. Fokus-Tests: `./gradlew :game:test --tests 'rooms.systemRecovery.*'` (bei Bedarf
   zunaechst konkrete Testklassen statt Wildcard). Danach `./gradlew :game:checkstyleMain`
   und `./gradlew :game:test`.
2. Neues Spiel, Editor, Continue und Host/Join-Flag in DE und EN pruefen; zweiten
   Client fuer Batterie-/Achievement-Synchronisierung verbinden.
3. R10 komplett spielen: sechs unsortierte Startwerte, Sonder-Achievement ohne
   Fortschritt, danach echte Sortierung, Count, Roboterscan, sechs Meta-Werte,
   Exit. An jedem Autosave einen Neustart testen.
4. Aenderungen nur in System Recovery und kleinen opt-in-Menue-Schnittstellen.
   `git diff --check` ausfuehren und fremde Worktree-Aenderungen stehen lassen.
