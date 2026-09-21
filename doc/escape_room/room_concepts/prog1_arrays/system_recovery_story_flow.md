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
   Questlog. Erst anschließend sind die Terminals freigegeben.
2. **Erster Fehler:** Nach der ersten falschen Terminaleingabe klingelt das Telefon. ECHO stellt
   sich vor, erklärt das Energie-Array und bietet weitere Hinweise über das Telefon an.
3. **Energie:** Nach der Array-Deklaration stellt AXIOM sich vor. Der Spieler setzt die angezeigten
   Werte, materialisiert die Batterie und setzt sie ein.
4. **Module:** Der Spieler legt die Modulplätze an, weist die Module zu, entfernt die defekte GPU,
   liest die Arraylänge und öffnet den Scannerzugang mit den ermittelten Kennwerten.
5. **Inventarscanner:** Eine for-each-Schleife zählt die belegten Module. Scannergebnis und
   Arraylänge ergeben den nächsten Türcode.
6. **Transportlager:** Unter AXIOMs Anleitung erstellt der Spieler die Paketliste und verarbeitet
   jedes Paket mit einer indexbasierten Schleife. Nach dem Transportlauf öffnet sich der
   Datenspeicher und ECHO ruft wegen einer auffälligen Störung an.
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
    AXIOM beschwert sich über die blockierte Freigabe; danach
    klingelt ECHO ein letztes Mal und öffnet den Aufzug.
11. **Ende:** Am Ausgang startet die Schlusssequenz. AXIOM ist aufgehalten, aber nicht zerstört;
    der Spieler verlässt die Anlage erstmals aus eigener Entscheidung.

## Auslöseregeln

- Storytexte werden über Übersetzungsschlüssel aus `language/systemRecovery/de.json` und
  `en.json` übertragen.
- Ein Story-Schritt wird pro Spieler höchstens einmal angezeigt und zugleich ins gemeinsame
  Questlog geschrieben.
- Persönliche Terminalfolgen gehen an den handelnden Spieler; gemeinsame Weltfolgen an alle.
- Ein geöffnetes Computerfenster verzögert den Dialog für den betroffenen Spieler.
- Positionsbasierte `dialog_trigger_*` starten nur Raumdialoge. Sie verändern weder Rätselzustand
  noch Petri-Netz.
- ECHOs Übergangs- und Abschlussgespräche erfordern eine Interaktion mit dem klingelnden Telefon.

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
