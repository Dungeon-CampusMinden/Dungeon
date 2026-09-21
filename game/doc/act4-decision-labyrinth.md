# Akt IV: Das Labyrinth der Entscheidungen

Nach dem vollständig ausgeführten Werkstattprogramm öffnet sich der Ausgang aus Akt III.
Nox geht durch die Tür zum Start des Labyrinths. Das Runenbuch am Start öffnet die
Steuerung. Ein Spieler wählt LINKS oder RECHTS, weitere Spieler können zusehen.
Beim Schließen oder Verlassen übernimmt ein bereits anwesender Beobachter die Steuerung.
Ohne Beobachter bleibt sie für den nächsten Spieler frei. Ein laufender Weg wird beendet.

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

Start ist die Fußposition 34/54. Die sechs Kreuzungen liegen bei 34/(60 + 10 × Index).
Die beiden Türspuren liegen bei x=26 und x=42; die Rückgänge bei x=18 und x=50.
Am unteren Rand verbinden sich beide Rückgänge mit START. Nox erreicht das Herzfeuer bei 34/120. Der Schrein steht daneben bei 41/122,
damit seine Flamme neben Nox sichtbar bleibt.
Die Wege berücksichtigen Nox' fünf Tiles breite und zweieinhalb Tiles tiefe Grundfläche.
Schmale Nischen enthalten die jeweiligen Quellen, Werkzeuge und Runen.

Jede Auswahl führt zunächst durch die gewählte Tür. Der richtige Weg schließt oberhalb
an die nächste Kreuzung an; der falsche biegt in den Rückgang ein. Nox wird weder bei
einer Fehlentscheidung noch bei einer Unterbrechung versetzt. Die vorhandene
kollisionsgeprüfte Bewegung arbeitet die Wegpunkte mit fünf Tiles pro Sekunde ab.

Die Kamera folgt Nox. Während der Bewegung verschwindet der Codebereich; Werte und
Ereignistext bleiben sichtbar. Im Stillstand zeigt das Runenbuch den vollständigen Code
mit Einrückung. Das Herzfeuer zeigt die vollständige letzte Rune als Java-Code,
einschließlich `&&` und `||`, sowie die Übersetzung der Schlüsselwörter.

## Zuständigkeit

`DecisionMaze` enthält Programme, Auswertung und feste Zustandsänderungen.
`ProgrammingDecisionWorld` erzeugt identische Tiles und Türmarker für Server und Client.
`ProgrammingDecisionRuntime` besitzt Fortschritt, Werte, Wegpunkte und Steuerungsrecht.
`ProgrammingDecisions` überträgt typisierte Dialogdaten und öffentliche Snapshots.
Jede Auswahl enthält die erwartete Revision; nur der berechtigte steuernde Spieler
kann eine Entscheidung auslösen. Veraltete Auswahlen und Auswahlen während der Bewegung
werden verworfen. Der Client übermittelt keine Werte oder Ergebnisse.

Akt III behält seine bisherigen Erfolge. Erst das Herzfeuer schließt die letzte Phase ab.
