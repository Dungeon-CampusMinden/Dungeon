---
title: "Escape-Room-Konzept"
---

# Einordnung des Begriffs "Escape-Room" in das Forschungsprojekt und den Dungeon

Die Verwendung der Terminologie "digitale Escape-Rooms (dER)" im Antrag des `L<ESC>rod`-Projekts deutet auf eine explizite Anwendung der Ausprägungen physischer Escape-Rooms (Team, begrenzte Zeit etc.) im digitalen Medium. Es geht um eine Ergänzung um "zentrale Elemente aus dem Bereich analoger Escape-Rooms". Ausprägungen von physischen Escape-Rooms, die in der für Videospiele üblichen Interpretation nicht vorkommen, werden im Antrag explizit erwähnt (z.B. Gamifikation, Team/Multi-Team).

Dieses Projekt stützt sich auf dem neueren und außerhalb der Spielwelt verbreiteten Verständnis von Escape-Rooms, die im Team gespielt werden und (neben der Lösung von Rätseln) auf Soft-Skills ausgerichtet sind. Das "digital" im "dER" ist eine Abgrenzung vom analogen Escape-Room in eine andere Richtung als die namensgebenden Point-And-Clicks.

Die Laufzeit des Dungeons umfasst die Echtzeit-Simulation (aus dem Antrag: "dynamisch auf das Spielgeschehen reagieren") einer "tile-basierten" Spielwelt, die von den Studenten selber ausgeführt und modifiziert werden kann.
Daraus soll im Laufe des Projekts ein "fachunabhängiges" Framework entstehen.

Diese Ausprägungen finden Anwendung bei der Eingrenzung der Recherche und der Bewertung der Ergebnisse.

# Eigenschaften

Welche Eigenschaften von Escape-Room sind für dieses Projekt wichtig?
Die Anforderungen sind in Aspekte aufgeteilt.

## Grundlegende Eigenschaften

- viele Gemeinsamkeiten mit Rätsel-Spielen/Adventures
  - Abgrenzungen: (was macht den "Escape-Room" gegenüber dem Adventure aus?)
    - Team-Gedanke eingebaut
    - laterales Denken
      - Betrachtung/Lösungsfindung aus einer anderen Perspektive
      - nicht analytisch und ohne begrenzten Rahmen
      - Öffnung für kreative Lösungen statt Konvergenz innerhalb der Problemdomäne
      - z.B. Bekämpfung eines Waldbrandes mit Kettensäge (Schneise) statt Löschschlauch
      - z.B. Verwendung von Grafikhardware für den Betrieb von neuronalen Netzen
      - z.B. "Möbel teuer wegen Logistik und Fertigung" - Montage durch den Käufer zuhause, Transport der Einzelteile mit PKW möglich
      - in Escape-Room oft auch eher abwegig:
        - Verwendung der Seriennummer im Türflügel als Code für ein digitales Schloss
        - ein Zahnrad wird anstatt in einem Mechanismus als Magnet zum Lösen eines anderen Rätsels verwendet
        - der Schaft eines Schraubendrehers dient als elektrischer Leiter
    - Dringlichkeit
      - Begrenzung der Spielzeit in herkömmlichen Adventures unüblich, bei Escape-Rooms dagegen schon
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
- Gegenstände werden gefunden und zur späteren Verwendung/Kombination mitgenommen

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
    - eigenständige Regulierung kognitiver Last
  - Interaktivität erlaubt Probieren
  - Möglichkeit der Ausrichtung auf verschiedene Lerntypen
  - Motivation bspw. durch:
    - Meisterung der Fähigkeiten
    - spielerische Herausforderung (Wahl der Konfrontation)
2. Übung / Anwendung von Wissen und Fähigkeiten
  - Wiederholung einzelner Puzzle sinnvoll?
3. Überprüfung von Wissen und Fähigkeiten
  - aus dem Antrag: "wird [..] um [..] Elemente [..] wie u.a. [..] Bewertungskonzept [..] ergänzt"
  - Absprache dazu: erstmal kein wichtiges Ziel dieses Projekts

Brauchen wir ein Learner-Model?

# Erwartete Schwierigkeiten

- der Dungeon ist darauf ausgerichtet, dass Studenten eine eigene Instanz des Spiels starten und auch den Code ändern können.
  - in einer Multiplayer-Variante muss es einen allen Teilnehmern gemeine Simulation geben
  - um in diesem Rahmen weiter programmatisch Einfluss auf die Simulation nehmen zu können, müsste z.B. der Code Gegenstand des Spiels werden
- Motivation
  - Spaß
    - viele Lernspiele (Gamification im Allgmeinen) kranken daran, dass das Ergebnis keinen Spaß mehr macht
    - nicht nur Spaß, sondern auch der Ausdruck der eigenen Persönlichkeit im Lernprozess
    - die grundsätzliche Überzeugung ist trotzdem, dass es möglich sein sollte, Spaß und Lernen zu verbinden
    - prinzipieller Widerspruch zwischen zweckgebundenem Lernen und dem Spiel als Selbstzweck
      - Spiele werden häufig um ein spaßiges Element herum aufgebaut
      - in der Lehre kommen die Inhalte zuerst
    - für die Vermittlung neuer Inhalte sollte die Lösung vielleicht der Begrenztheit des 'Constructivist Teaching' bewusst sein oder sogar einen Rahmen vorgeben
  - Immersion
    - mehr Absorption der Gegenwart im Spiel, weniger Ablenkung aus / Hinterfragung der Gegenwart außerhalb
    - individuelle Ansprüche an die Lernerfahrung
      - Mathe-Nerds sind vielleicht weniger an immersiven Elementen interessiert (wann geht es endlich los?)
      - Kinder profitieren mehr vom Setting, halten aber nicht so lange durch (weniger Zeit für Inhalte)
      - unterschiedliche Lerntypen (vlt. abhängig vom Feld)
  - mögliche Konsequenzen:
    - Sollte an der Erschaffung eines didaktischen Escape-Rooms auch ein Spieldesigner mitwirken?
    - Sollen die Spieler den Zeitaufwand für die Immersion selber festsetzen können?
    - Untescheidung von Vorgehen abhängig vom Zielalter, Rahmen, Themenfeld etc.
- zeitlich begrenzte Förderung
  - mögliches Ziel: Prototyp, der sich bereits für den Einsatz eignet
