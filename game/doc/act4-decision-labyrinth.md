# Akt IV: Das Labyrinth der Entscheidungen

Nach dem vollständig ausgeführten Werkstattprogramm öffnet sich der Ausgang aus Akt III.
Nox geht durch die Tür zum Start des Labyrinths und wartet auf einen Reiter.
Mit E an Nox steigt ein Spieler auf. Die Steuerung öffnet sich automatisch und bleibt
bis zum Ende der Fahrt offen. Nur dieser Spieler entscheidet; weitere Spieler bewegen
sich weiterhin frei im Raum. Nach einem Verbindungsabbruch wird der Sitz wieder frei.
Ein anderer Spieler kann dann direkt an Nox aufsteigen und den laufenden Versuch fortsetzen.
Am Herzfeuer endet die Fahrt: Der Reiter steigt automatisch ab und die Steuerung schließt sich.

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

Jeder weitere Versuch beginnt mit eigenen, festen Startwerten; nach dem vierten
wiederholen sie sich:

| Versuch | Kraft / Energie / Temperatur | Fehlerfreier Weg |
| --- | --- | --- |
| 1 | 45 / 70 / 22 | RECHTS, RECHTS, LINKS, LINKS, RECHTS, LINKS |
| 2 | 50 / 90 / 21 | RECHTS, LINKS, RECHTS, RECHTS, LINKS, LINKS |
| 3 | 35 / 70 / 15 | LINKS, LINKS, RECHTS, RECHTS, RECHTS, LINKS |
| 4 | 40 / 75 / 27 | RECHTS, RECHTS, LINKS, LINKS, LINKS, RECHTS |

Die Runen am Weg verändern die Werte wie oben. Bei jedem Satz erfüllt Nox an allen sechs
Kreuzungen die äußerste Bedingung, sodass nie das äußerste SONST entscheidet. Dieselbe
Kreuzung kann deshalb in einem neuen Versuch einen anderen Zweig verlangen, ohne dass
sich die Runen auf einen trivialen Zweig reduzieren.

Eine falsche Tür führt durch den äußeren Rückgang bis zum START. Erst dort erhält Nox
die neuen Startwerte. Mit Weiter beginnt der nächste Versuch. Bei einem tatsächlichen
Hindernis setzt Weiter den unterbrochenen Abschnitt von Nox' aktueller Position fort.

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
dieser Zustand erhalten; bei der Ankunft klappt der Bereich automatisch auf. Die aktuellen
Werte bleiben sichtbar. Die Statuszeile darunter erscheint nur bei Hinweisen. Der Code hat
Einrückungen und übersetzte Schlüsselwörter. Bei langem Code oder kleinen Fenstern
lässt sich der Bereich horizontal und vertikal scrollen.

## Hilfe und Questlog

Neben Weiter stehen Hilfe und Quest-Log. Die Hilfe nutzt dieselbe Petri-Netz-Stufung wie
Akt I bis III. Sie ersetzt das Runenbuch, reicht bis zum oberen Bildrand und blendet LINKS
und RECHTS aus, bis der Spieler zur Rune zurückkehrt. Nur der Reiter kann sie verwenden,
und nur solange Nox an einer Kreuzung wartet, werden Tipps freigegeben; sonst nennt die
Hilfe den Grund. Tipp 1 erklärt das Lesen
von oben nach unten, Tipp 2 UND, ODER sowie `>` und `>=`. Tipp 3 setzt im Runenbuch
Nox' aktuelle Werte in die Bedingungen ein. Danach wählt die bestätigte Lösung nur an der
aktuellen Kreuzung die Tür des ausgeführten Zweigs; die Versuche werden mit Hilfestufe und
automatischer Lösung getrackt.

## Abschluss am Herzfeuer

Nach der sechsten Rune steigt der Spieler automatisch von Nox ab. Ein Hinweis verweist
auf die Schriftrolle vor dem Herzfeuer. Der Button „Opfergabe darbringen“ verbraucht die
beiden dekorativen Kristalle und entzündet das Herzfeuer. Das Schließen des Dialogs
bricht die Auswahl ab.

`ProgrammingEnding` nimmt die Opfergabe nur nach Abschluss des Labyrinths und nur einmal
auf dem Server an. Dabei beendet `Tracking.completed()` die Sitzung erfolgreich, bevor
der Abspann beginnt. Alle verbundenen Spieler sehen den Erfolgstext. Wer ihn bestätigt,
wartet auf die übrigen Spieler; getrennte Spieler halten den Abschluss nicht auf.
Anschließend beendet `Game.complete()` das Spiel über den regulären Shutdown.

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
