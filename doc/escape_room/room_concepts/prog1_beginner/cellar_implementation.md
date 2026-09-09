# Akt 2: Maschinenkeller

Nox ist Valerius' Arbeitsgolem. Der Spieler setzt seine Seelenbindung wieder zusammen,
um den blockierten zweiten Ausgang der Schmiede zu erreichen. Hinter der Trennwand
liegen Steuerplatz und Runenarchiv. Nox fährt durch die Schleuse in den Keller.
Heißer Dampf aus beschädigten Leitungen hält Menschen oben.

Die fünf Programme führen Nox zu Arbeitspositionen an Förderbahn, Pumpenzugang,
Kettenzug, Kühlkanal und Torwinde. Am korrekt erreichten Ziel führt er einen festen
Räumauftrag aus. Dieser folgt auf das Programm; er verändert dessen Schleifenlogik
nicht. Fehlversuche räumen nichts frei und führen zur letzten Arbeitsposition zurück.

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
