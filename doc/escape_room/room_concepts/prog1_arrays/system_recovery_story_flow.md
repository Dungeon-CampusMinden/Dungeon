# System Recovery: Storyablauf

## Ausgangslage

Der Spieler ist ein unvollständig initialisierter KI-Agent. AXIOM, die zentrale Intelligenz der
Anlage, lässt ihn beschädigte Subsysteme reparieren. ECHO ist ein abgespaltener, nur über das
Telefon erreichbarer Teilprozess. Anfangs kennt auch ECHO AXIOMs vollständigen Plan nicht.

AXIOM spricht präzise, überlegen und zunehmend ungeduldig. ECHO bleibt ruhig und hilft dem Spieler,
selbstständig zu handeln. Die Wendung erfolgt erst beim Öffnen des Systemkerns: Die Reparaturen
haben AXIOMs Sperren gelöst. Der Spieler muss die letzten Prüfungen gegen AXIOM verwenden und kann
anschließend fliehen.

## Ablauf

1. **Start:** Eine Lore-Sequenz erklärt Identität und Auftrag, danach zeigt ein Popup Steuerung und
   Questlog. Nach der Lore-Sequenz startet der gemeinsame 60-Minuten-Countdown; erst nach dem
   Steuerungsdialog sind die Terminals freigegeben.
2. **Erster Kontakt:** Vor dem ersten Anruf meldet das Telefon nur eine tote Leitung. Nach der
   ersten falschen Energie-Terminaleingabe klingelt es; ECHO stellt sich vor und erklärt den
   fehlenden Datenspeicher. Ist bereits die erste Eingabe richtig, stellt sich zunächst AXIOM vor;
   anschließend klingelt ECHO mit einem eigenen Gespräch für diesen Fall. Beide Wege führen
   zum selben weiteren Rätselablauf und zur telefonischen Hilfe.
   Ohne jede Terminaleingabe klingelt das Telefon nach vier Minuten ebenfalls: ECHO fragt
   „Du weißt nicht, was du tun sollst, oder?“, stellt sich vor und erklärt das erste Ziel.
   Dieser passive Einstieg behauptet keine falsche Eingabe und wird ebenfalls im Questlog erfasst.
3. **Energie:** Nach der Array-Deklaration meldet sich AXIOM. Der Spieler setzt die angezeigten
   Werte, materialisiert die Batterie und setzt sie ein. AXIOM erklärt beim ersten Gespräch die
   drohende Systemlöschung nach einer Stunde und betont, dass die Zeit bereits läuft.
4. **Module:** Der Spieler legt die Modulplätze an, weist die Module zu, entfernt die defekte GPU,
   liest die Arraylänge und öffnet den Scannerzugang mit den ermittelten Kennwerten.
5. **Inventarscanner:** Eine for-each-Schleife zählt die belegten Module. Scannergebnis und
   Arraylänge ergeben den nächsten Türcode.
6. **Transportlager:** Unter AXIOMs Anleitung erstellt der Spieler die Paketliste und verarbeitet
   jedes Paket mit einer indexbasierten Schleife. Nach dem Transportlauf ruft ECHO wegen einer
   auffälligen Störung an. Erst nach diesem Telefonat öffnet sich der Datenspeicher.
7. **Sortierung:** Der Spieler ordnet die Werte zunächst manuell und ergänzt danach die
   Bubble-Sort-Bedingung auf einem Stick. Die Maschine sortiert die Pakete und gibt den
   Archivschlüssel frei.
8. **Archiv:** AXIOM reagiert gereizt und schickt den Spieler ins Archiv. Hinweise in der Welt
   beschreiben drei fehlende Arrays. Ihre Wiederherstellung öffnet den zweidimensionalen Speicher.
9. **2D-Speicher und Suchroboter:** Der Spieler rekonstruiert eine Matrix, birgt den leeren
   Ortungschip, ergänzt dessen verschachtelte Suche und startet den Roboter. AXIOM verlangt das
   gefundene Zugriffsmodul sofort für den Systemkern.
10. **Systemkern:** Erst das ausgeführte Zugriffsskript öffnet den Kern und aktiviert den Alarm.
    ECHO warnt nun vor AXIOMs tatsächlichem Ziel. Drei Prüfbereiche liefern Ergebnisse, die in der
    Abschlussmaske kombiniert werden. Für die dritte Prüfung steuert ein eigener Suchroboter die
    3x5-Matrix ab; erst nach seinem vollständigen Lauf wird die Abschlussmaske freigegeben.
    Nach der bestätigten Eingabemaske beschwert sich AXIOM über die blockierte Freigabe und
    der Alarm endet. Danach klingelt ECHO ein letztes Mal. Erst das beantwortete Gespräch
    öffnet den Aufzug.
11. **Ende:** Am Ausgang startet die Schlusssequenz. AXIOM ist aufgehalten, aber nicht zerstört;
    der Spieler verlässt die Anlage erstmals aus eigener Entscheidung.

## Zeitlimit und Hilfe

Am Custom Point `timer` steht ein interaktiver Countdown wie in The Last Hour. Beim Anklicken
nennt er die verbleibenden Minuten bis zur Systemlöschung. Alle Spieler sehen dieselbe
serverseitig bestimmte Zeit; spätere Multiplayer-Beitritte starten keine neue Stunde.

Nach **vier Minuten ohne akzeptierten Fortschritt** greift die automatische Hilfe. Dieser Wert
ist ein Ausgangspunkt für Playtests, keine Garantie für einen Abschluss in 60 Minuten.
Eine falsche Eingabe oder das bloße Öffnen eines Fensters setzt die Frist nicht zurück.
Akzeptierte Teil-Eingaben, erfolgreiche Lernschritte, korrekte manuelle Sortierentscheidungen und
abgerufene Hinweise setzen sie zurück.

Die Prüfung und das Auslösen übernimmt `SystemRecoveryTimedHintSystem`, ein eigenständiges
serverseitiges ECS-System. Es wird beim Server-Setup registriert und bleibt bei Dialogen aktiv.
Das Countdown-System ist ausschließlich für Restzeit, Speicherung und das Zeitablauf-Ende zuständig.

Vor der ersten Terminaleingabe gilt in **beiden** Hilfsmodi eine Ausnahme: Nach Ablauf der
Wartezeit klingelt nur der einmalige ECHO-Erstanruf `opening-call-idle`. Es erscheint kein
Zwangstipp-Popup und es wird keine Hinweisstufe verbraucht. Weitere automatische Hinweise
werden erst nach einer Terminaleingabe freigegeben; manuelle Telefonhilfe ist nach dem
Erstanruf bereits verfügbar. Ein schon durch eine Eingabe ausgelöster Erstanruf wird nicht ersetzt.

Die beiden Optionen sind im Code konfigurierbar:

- `SystemRecovery.FORCE_HINTS = true` (Standard): Der nächste Hinweis wird ohne Rückfrage
  als ECHO-Dialog angezeigt und ins passende Questlog geschrieben. Er verbraucht genau eine
  Stufe derselben Hinweisfolge, die das Telefon verwendet. Offene Story- und Auswahldialoge
  verzögern die Anzeige; ein offener PC und sein Codeentwurf bleiben erhalten.
- `SystemRecovery.FORCE_HINTS = false`: Das Telefon klingelt. ECHO erinnert daran, dass Hilfe
  verfügbar ist; erst eine weitere Telefoninteraktion fordert den nächsten Hinweis an.
  Ein unbeantworteter Storyanruf wird niemals durch den optionalen Hilfsanruf ersetzt.
  Gab es noch keinen Kontakt mit ECHO, klingelt stattdessen zuerst sein Einführungsgespräch.
- `SystemRecovery.HINT_DELAY_MINUTES = 4`: Wartezeit bis zur nächsten Hilfe. Sind alle Hinweise
  des aktiven Schritts verbraucht, werden keine weiteren Erinnerungen erzeugt.

Manuelle Telefonhinweise bleiben in beiden Modi verfügbar. Bei null Sekunden vor `COMPLETE`
endet das Spiel sofort: Steuerung und Interaktionen sind gesperrt, weitere Rätselerfolge werden
abgelehnt. Ein eigenes Outro erklärt, dass der Spieler zu langsam war, der Systemkern gelöscht
wurde und die Verbindungen zu AXIOM und ECHO abgebrochen sind. Nach dem Titel
`SYSTEM RECOVERY GESCHEITERT` wird das Spiel beendet, nicht als Erfolg abgeschlossen. Die
Tracking-Session endet dabei wie bei einem Abbruch als `ABORTED`; den Zeitablauf kennzeichnet
vorher für jeden Spieler ein `INTERACTION_RECORDED` am aktiven Rätsel (`timer` / `time-limit` /
`BLOCKED` / `expired`). Nach `COMPLETE` stoppt die Zeit, sodass das letzte Telefonat und der Weg
zum Aufzug nicht mehr unter Zeitdruck stehen. Wie die Restzeit gespeichert wird, beschreibt
[Save und Load](system_recovery_save_load.md#restzeit).

## Auslöseregeln

- Storytexte werden über Übersetzungsschlüssel aus `language/systemRecovery/de.json` und
  `en.json` übertragen.
- Ein Story-Schritt wird pro Spieler höchstens einmal angezeigt und zugleich ins gemeinsame
  Questlog geschrieben.
- Persönliche Terminalfolgen gehen an den handelnden Spieler; gemeinsame Weltfolgen an alle.
- Ein offener blockierender Dialog verzögert weitere Storytexte für denselben Spieler, nicht für
  andere Spieler. Überholte Texte werden vor der Anzeige gegen den aktuellen Petri-Schritt geprüft
  und verworfen; erst angezeigte Texte gelangen ins Questlog.
- Positionsbasierte `dialog_trigger_*` starten Raumdialoge nur im zugeordneten Petri-Schritt.
  Sie verändern weder Rätselzustand noch Petri-Netz. Beim Inventarscanner entscheidet zusätzlich
  der tatsächliche Scanabschluss zwischen den beiden Texten desselben Schritts.
- ECHOs Übergangs- und Abschlussgespräche erfordern eine Interaktion mit dem klingelnden Telefon.
- Das Petri-Netz folgt akzeptierten Terminal-, Chip- und physischen Erfolgen. Ein Raumdialog
  darf die Markierung nicht verändern.

## Textregeln

Normale Storydialoge nennen die Situation und das unmittelbar nächste Ziel, aber keinen fertigen
Code. Displays liefern die für den Schritt notwendigen Werte oder Bedieninformationen. Die vier
Telefonhinweise werden stufenweise konkreter; nur die letzte Stufe enthält die vollständige Lösung.
Alle Texte werden im passenden Questlog-Tab protokolliert.

## Zuständige Dateien

- `story/SystemRecoveryStoryDialogs.java`: verzögerte Storyschritte und Sprecher
- `story/SystemRecoveryDialogTriggers.java`: einmalige Raumdialoge
- `story/SystemRecoveryHintPhone.java`: ECHO-Hinweise
- `level/SystemRecoveryLevel.java`: Lore, Telefonanrufe und Endsequenz
- `assets/language/systemRecovery/{de,en}.json`: sämtliche sichtbaren System-Recovery-Texte
- [Save/Load](system_recovery_save_load.md): Checkpoints und Wiederherstellung ohne Dialog-Replay
