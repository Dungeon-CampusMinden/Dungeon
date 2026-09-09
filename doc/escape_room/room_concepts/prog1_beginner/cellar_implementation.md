# Akt 2: Maschinenkeller

Nox ist Valerius' Arbeitsgolem. Der Spieler setzt seine Seelenbindung wieder zusammen,
um den blockierten zweiten Ausgang der Schmiede zu erreichen. Hinter der Trennwand
liegen Steuerplatz und Runenarchiv. Nox fährt durch die Schleuse in den Keller.
Heißer Dampf aus beschädigten Leitungen hält Menschen oben.

Die fünf Programme führen Nox zu Arbeitspositionen an Förderbahn, Pumpenzugang,
Kettenzug, Kühlkanal und Torwinde. Am korrekt erreichten Ziel führt er einen festen
Räumauftrag aus. Dieser folgt auf das Programm; er verändert dessen Schleifenlogik
nicht. Fehlversuche räumen nichts frei und führen zur letzten Arbeitsposition zurück.

## Schleifen und eindeutige Lösungen

Alle 24 Runen dürfen an jeder Station ausgeführt werden. Der Executor entscheidet anhand
des vollständig ausgeführten Programms, seiner Endposition und Blickrichtung, nicht anhand
einer freigeschalteten Lösungs-ID. Das bloße Überqueren einer Zielmarke schließt keinen Auftrag ab.

| Station | Lösung | Lernziel |
| --- | --- | --- |
| Förderbahn | `forge-press-for` | Drei Wiederholungen; die Zielmarke liegt vor dem Gangende. |
| Pumpenzugang | `bellows-while` | Vor jeder Wiederholung Boden prüfen; vier Felder bis zur Wand. |
| Kettenzug | `chain-lift-while` | Angriff vor dem Schritt; die Reihenfolge im Schleifenrumpf zählt. |
| Kühlkanal | `cooling-channel-do-while` | Mindestens ein Durchlauf trotz zunächst falscher Boden-Abfrage vor der Grube. |
| Torwinde | `heart-gate-for` | Ein Schleifendurchlauf enthält mehrere Schritte und Drehungen. |

Keine dieser fünf Runen löst einen zweiten Auftrag. Ablenker verändern Zählgrenze,
Bedingung, Befehlsreihenfolge oder Drehung. Die früher doppelte Rune und die leere
Wiederholung wurden durch unterscheidbare Programme ersetzt. Die Archivproben testen
einzelne Bewegungen, Drehfolgen oder eine endlose Wiederholung.

`bodenVoraus()` meldet Boden, auch wenn dort ein Gegner steht. `amWegzeichen()` prüft
die aktuelle Zielzelle; der Zielpfeil wird getrennt bei Programmende geprüft. Eine Rune
wird weder beim Erfolg noch beim Fehler verbraucht. Solange Nox läuft, zurückkehrt oder
räumt, nimmt der Server kein neues Programm an. Fehler setzen Position, Blickrichtung
und gegebenenfalls den Gegner zurück. Die Grenze von 24 Schleifendurchläufen verhindert
Endlosschleifen. Fehlermeldungen unterscheiden Hindernisse, falsche Endposition und
falsche Blickrichtung und bleiben nach der Rückkehr lesbar.

Zwischen Programmende und dem separaten Räumauftrag liegt eine kurze Ruhephase mit
eigener Statusmeldung. Die Rune selbst enthält keine versteckten Räumbefehle.

Beim letzten erfolgreichen Auftrag entfernt Nox den Schutt vor der Torwinde.
Die rostige Halterung bricht, das Gegengewicht fällt und eine Wandplatte im Archiv
wird hochgezogen. Der Kameraschnitt zeigt erst den Unfall unten, dann dessen Wirkung
oben. Die Öffnung ist bis dahin verdeckt und unpassierbar. Kein Erzähler erklärt den
Unfall, und kein UI nennt Aktnummern oder kündigt die Entdeckung an.

## Noch provisorische Darstellung

- Torwinde: bestehende Säulen-, Kisten-, Ketten- und Steinsprites bilden Rahmen,
  Halterung und Gegengewicht. Eine eigene Winden-Grafik mit Seiltrommel fehlt noch.
- Arbeitsstellen: Schutthaufen und kleine Messingplatten markieren Räumpositionen.
  Eigene Grafiken für Förderanlage, Pumpe und Kettenzug fehlen; Lagergut und Leitungen
  geben den bestehenden Gängen ihren Kellerkontext.
- Leitungen: Ausschnitte aus `FG_Cellar.png` dienen als Rohre. Dampf wird im vorhandenen
  Lichtshader prozedural gezeichnet. Eigene Rohrstücke mit sichtbarer Bruchstelle fehlen.

Der Raum spielt vorerst keine eigenen Sounds ab. Räumarbeiten und Unfall werden nur
visuell dargestellt.

Akt 3 und 4 bleiben inhaltlich offen. Hinter der Wand ist nur die bestehende Reservefläche.
