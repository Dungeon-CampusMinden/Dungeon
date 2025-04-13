---
title: "Escape-Room-Konzept"
---

# Eigenschaften

Welche Eigenschaften von Escape-Room sind für dieses Projekt wichtig?
Die Anforderungen sind in Aspekte aufgeteilt.

## Grundlegende Eigenschaften

- viele Gemeinsamkeiten mit Rätsel-Spielen/Adventures
- Kombination von Rätseln zur Erreichung eines vorgegeben Ziels
- ein oder mehrere Orte geben Raum zur Erkundung und schaffen Atmosphäre
  - Schiff
  - Stadt
  - Pyramide
- Mittel und Wege müssen die Teilnehmer identifizieren
  - Erkundung, um Informationen, Objekte und Orte zu finden
  - Interaktionen (z.B. mit Tieren, anderen Teilnehmern, Objekten, Orten)
- Vorwissen und Fähigkeiten der Teilnehmer nicht unbedingt bekannt
  - in der Lehre eventuell eingrenzbar
  - auch bei vorhandendem Wissen ist ein Feststecken möglich
  - etablierte Lösungsansätze:
    - parallele Lösungswege um ungewisse Pfade
    - Fehlschläge/Erfolge können die Möglichkeiten ändern
      - z.B. Computer geht kaputt, wenn Raum unter Wasser gesetzt wird, aber schwimmend kann man einen Lüftungsschacht erreichen
    - ein Gamemaster kann kontaktiert werden, um Hilfe zu erhalten
- keine Failure-States (es gibt immer einen Weg zum Ziel / keine Sackgassen)

## Motivation

- zeitliche Begrenzung
  - Komfortzone verlassen
  - zur Erkundung drängen
- Problemlösung im Team
  - möglicherweise kompetitiv (Teil derselben Simulation?)
  - Zusammenspiel entweder lokal oder über das Netzwerk
  - Kombination unterschiedlichen Wissen und unterschiedlicher Fähigkeiten der Teilnehmer
    - leichter Widerspruch mit konventioneller Didaktiv (gleiches Wissen für alle Teilnehmer)
- Spielästhetik (weiter gefasste Interpretation)
  - Neugier, Ausprobieren, Erforschung (das fürs Lernen Wichtige)
  - Ausdruck eigener Ideen, Schlüpfen in eine Rolle
    - Hund verscheuchen vs. Hund mit Wurst bestechen
  - Erzählung, Musik, Aussehen, etc.
  - Versuch einer umfassenden Liste ist z.B. das MDA-Framework ([Paper](https://users.cs.northwestern.edu/~hunicke/pubs/MDA.pdf)/[Wikipedia](https://en.wikipedia.org/wiki/MDA_framework))

## Abhängigkeitsgraph

- direktional
- ein Startknoten
- ein Zielknoten
- Knoten sind (Teil-) Ziele und Ereignisse
  - "Katze mit Fisch anlocken"
  - "Hund mit Pfeife verscheuchen"
- Ereignisse können die Möglichkeiten verändern
  - bspw. ein durch einen Timer ausgelöstes Ereignis, wenn zu Zeit X ein Ziel noch nicht erreicht wurd
- Ziele können weitere Bedingungen zur Laufzeit haben
  - "zwei Personen müssen anwesend sein"
  - "geht nur draußen"
  - "Brechstange notwendig"
  - "Radio muss auf bestimmte Frequenz eingestellt sein"
  - "Kiste muss geöffnet sein"
  - noch nicht "Katze mit Fisch anlocken" erreicht
  - Interaktion für mehrere Gegenstände
    - "zwei Hebel gleichzeitig halten"
  - möglicherweise fehlschlagende Interaktionen
    - "Ast bricht bei Versuch"

## Lehre


1. Vermittlung von Inhalten
  - Vorteil "eigenes Lerntempo" evtl. durch Timer im Escape-Room eingeschränkt
2. Übung / Anwendung von Wissen und Fähigkeiten
3. Überprüfung von Wissen und Fähigkeiten

# Erwartete Schwierigkeiten

- viele Lernspiele (Gamification im Allgmeinen) kranken daran, dass das Ergebnis keinen Spaß mehr macht
- die grundsätzliche Überzeugung ist trotzdem, dass es möglich sein sollte, Spaß und Lernen zu verbinden
- Mathe-Nerds sind vielleicht weniger an immersiven Elementen interessiert
- mögliche Konsequenz: Sollte an der Erschaffung eines didaktischen Escape-Rooms auch ein Spieldesigner mitwirken?
- zeitlich begrenzte Förderung
  - mögliches Ziel: Prototyp, mit dem man schon irgendwie arbeiten kann?

