# Akt IV: Das Labyrinth der Entscheidungen

Nach dem vollständig ausgeführten Werkstattprogramm öffnet sich der Ausgang aus Akt III.
Nox geht durch die Tür zum Start des Labyrinths und wartet auf einen Reiter.
Mit E an Nox steigt ein Spieler auf. Die Steuerung öffnet sich automatisch und bleibt
bis zum Ende der Fahrt offen. Nur dieser Spieler entscheidet; weitere Spieler bewegen
sich weiterhin frei im Raum. Nach einem Verbindungsabbruch wird der Sitz wieder frei.
Ein anderer Spieler kann dann direkt an Nox aufsteigen und den laufenden Versuch fortsetzen.
Auch am Herzfeuer bleibt der Spieler auf Nox und befindet sich tatsächlich am Ziel.

## Runen und Werte

Die sechs Programme übernehmen die Verschachtelungen und Vergleiche des Entwurfs.
Der Anfangszustand ist Kraft 45, Energie 70, Temperatur 22. Die Beispielzustände des
Entwurfs unterscheiden sich zwischen den Kreuzungen. Hier verändern sichtbare Runen
auf den erfolgreichen Wegen die tatsächlich mitgeführten Werte:

| Nach Kreuzung | Ereignis | Werte vor der nächsten Rune: Kraft / Energie / Temperatur |
| --- | --- | --- |
| 1 | Schwellenrune: Energie -30 | 45 / 40 / 22 |
| 2 | Kristallquelle: Energie +30 | 45 / 70 / 22 |
| 3 | Kraftquelle: Kraft +10; Feuerrune: Temperatur +5 | 55 / 70 / 27 |
| 4 | Schmiederune: Kraft +13; Energie -15 | 68 / 55 / 27 |
| 5 | Herzglut: Energie -13; Temperatur +4 | 68 / 42 / 31 |

Damit lautet der erste fehlerfreie Weg RECHTS, RECHTS, LINKS, LINKS, RECHTS, LINKS.
Die Werte werden bei einer Wiederholung nicht zurückgesetzt. Bereits ausgelöste
Runenänderungen bleiben bestehen. Auf jedem erfolgreichen Durchgang wirkt die jeweilige
Rune erneut. Nach ungeraden Fehlversuchen verändert der Rückweg am START Kraft +15,
Energie -15 und Temperatur +5. Nach geraden Fehlversuchen folgen Energie +10 und
Temperatur -5. Die Ereignisse sind deterministisch. Dieselbe Kreuzung kann anschließend
einen anderen Zweig ausführen.

Eine falsche Tür führt durch den äußeren Rückgang bis zum START. Erst dort ändern sich
die Werte. Mit Weiter beginnt der nächste Versuch. Diese Pause lässt die Veränderung
lesbar. Bei einem tatsächlichen Hindernis setzt Weiter den unterbrochenen Abschnitt
von Nox' aktueller Position fort.

## Raum und Bewegung

Start ist die Fußposition 34/55. Die sechs Kreuzungen liegen bei x=34 und
y=61, 70, 80, 89, 99 und 108. Nox erreicht das Herzfeuer bei 34/118;
der Schrein steht daneben bei 41/120. Die linken Türspuren liegen bei x=28, 27, 28, 26, 28, 27;
die rechten bei x=40, 41, 40, 42, 41, 40. Die unterschiedlich breiten
Kammern teilen Rückgänge bei x=20 und x=48. Am unteren Rand verbinden sich beide Rückgänge
mit START. Der Reiter wird innerhalb von Nox' Grundfläche mitgeführt. Sein Sprite sitzt
über dem Körper, beim Blick nach oben vor dem Kopf, sonst dahinter. Kollisionsfreiheit
während der Fahrt verhindert gegenseitiges Schieben. Beim Verlassen des Levels werden
die normalen Bewegungs- und Kollisionskomponenten wiederhergestellt.

Die Wege berücksichtigen Nox' fünf Tiles breite und zweieinhalb Tiles tiefe Grundfläche.
Werkbänke, Quellen und Runensockel stehen in seitlichen Arbeitsnischen am Fortschrittsweg.
Jede Nische gruppiert die zum Ereignis gehörenden Gefäße, Kristalle oder Werkzeuge.
Fackeln stehen auf Nischenboden und lassen sich wie die übrigen Raumfackeln umschalten.

Jede Auswahl öffnet die gewählte Eingangstür und genau einen ihrer beiden Ausgänge.
Native Torgitter trennen Fortschrittsweg und Rückgänge. Die äußeren Gitter
liegen bei x=25 und x=47, die inneren direkt neben der jeweiligen Türspur.
Eine geschlossene Wandreihe trennt die Arbeitsnischen von den Zweigausgängen.
Vor der Wahl sind beide Ausgänge geschlossen. Bei Erfolg öffnet sich der innere Ausgang,
bei einem Fehler der äußere. Der jeweils andere bleibt physisch gesperrt. Erst wenn Nox
wieder START erreicht, schließen alle Entscheidungstore für den nächsten Versuch.
So sind geänderte Zweige bei neuen Werten sichtbar, ohne die Lösung vorwegzunehmen.
Nox wird weder bei einer Fehlentscheidung noch bei einer Unterbrechung versetzt.
Die vorhandene kollisionsgeprüfte Bewegung arbeitet die Wegpunkte mit fünf Tiles pro Sekunde ab.

Die Kamera folgt Nox ohne Beobachtungseffekt. Der Code steht anfangs aufgeklappt unten
in der Bildschirmmitte; LINKS und RECHTS stehen über den zugehörigen Türen. Ein Klick
auf die Überschrift klappt den Codebereich ein oder aus. Während der Bewegung bleibt
dieser Zustand erhalten; bei der Ankunft klappt der Bereich automatisch auf. Werte und
Ereignistext bleiben sichtbar. Der Code hat Einrückungen und übersetzte Schlüsselwörter.
Bei langem Code oder kleinen Fenstern lässt sich der Bereich horizontal und vertikal
scrollen. Am Herzfeuer bleibt die letzte Rune als Java-Code einschließlich `&&` und `||`
sichtbar.

## Zuständigkeit

`DecisionMaze` enthält Programme, Auswertung und feste Zustandsänderungen.
`ProgrammingDecisionWorld` erzeugt identische Tiles und Türmarker für Server und Client.
`ProgrammingDecisionRuntime` besitzt Fortschritt, Werte, Wegpunkte und Steuerungsrecht.
`ProgrammingRiderSystem` bindet den echten Spieler an Nox und setzt die lokale Sitzpose.
`ProgrammingRiderCamera` folgt dem Reittier nur auf dem Bildschirm seines Reiters.
`ProgrammingDecisions` überträgt typisierte Dialogdaten und öffentliche Snapshots.
Jede Auswahl enthält die erwartete Revision; nur der berechtigte steuernde Spieler
kann eine Entscheidung auslösen. Veraltete Auswahlen und Auswahlen während der Bewegung
werden verworfen. Der Client übermittelt keine Werte oder Ergebnisse.

Akt III behält seine bisherigen Erfolge. Erst das Herzfeuer schließt die letzte Phase ab.
