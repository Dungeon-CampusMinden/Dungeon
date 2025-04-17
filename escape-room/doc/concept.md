---
title: "Escape-Room-Konzept"
---

# Eigenschaften

Welche Eigenschaften von Escape-Room sind für dieses Projekt wichtig?
Die Anforderungen sind in Aspekte aufgeteilt.

## Grundlegende Eigenschaften

- viele Gemeinsamkeiten mit Rätsel-Spielen/Adventures
  - Abgrenzungen: (was macht den "Escape-Room" gegenüber dem Adventure aus?)
    - Team-Gedanke eingebaut
    - laterales Denken
    - Dringlichkeit
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

NOTE im antrag steht "wird [..] um [..] Elemente [..] wie u.a. [..] Bewertungskonzept [..] ergänzt.
rückmeldung dazu: überprüfung erstmal sein lassen


1. Vermittlung von Inhalten
  - Vorteil "eigenes Lerntempo" evtl. durch Timer im Escape-Room eingeschränkt
2. Übung / Anwendung von Wissen und Fähigkeiten
3. Überprüfung von Wissen und Fähigkeiten

Brauchen wir ein Learner-Model?

# Was erwarten Nutzer für die Erstellung eines Escape-Rooms? (allgemein)

Wir suchen Richtlinien dafür, was vom Dungeon und den Tools erwarten wird, um damit Escape-Rooms zu erstellen.

## [Interview mit Escape-Rooms](https://static1.squarespace.com/static/62bc928cda86cf2dcc7f378d/t/65a85086591a9e1f2a897b2b/1705529482357/CoolJobsEscapeRoom.pdf) am 20. November 2023 in ScienceWorld von Scholastic.

Es geht um das Design kommerzieller Escape-Rooms. Der Designer ist zufälligerweise unser alter Freund Scott Nicholson.

> I start by deciding on the genre (type of game) and setting (where it takes place). I outline the players’ goals and the obstacles they’ll face. These decisions let me create “beats,” or major points in the game’s narrative. Next I start thinking of challenges—math and word puzzles, searching activities, or physical tasks—that could fit within the story. We want a variety of challenges. That way, every player gets a chance to solve something and feel like a hero.
>
> I work with a team. We sketch out ideas on paper or using digital drawing tools and 3-D design programs. Once we have our prototypes, or early versions, of the puzzles, we test and rework them. This process is known as iteration, and it’s the most important part of game development. The next step is to construct the actual room that will make players feel immersed in the game.

## [The Process of Designing an Escape Room](https://reddoorescape.com/blog-the-process-of-designing-an-escape-room/)

Escape-Room-Anbieter schreibt über den Prozess im eigenen Blog.

> 1. CREATE YOUR SETTING AND THEME
> ..
> 2. DETERMINE HOW PLAYERS DISCOVER CLUES
> ..
> 3. DESIGN YOUR PUZZLES AND GAMEFLOW
> ..

## [Escape Room Design Blueprint](https://lockpaperscissors.co/escape-room-design-blueprint)

Die Autoren verkaufen Escape-Room-Boxes für Feiern. Langes Dokument. Von Musik bis Essen alles dabei.


# Erwartete Schwierigkeiten

- der Dungeon ist darauf ausgerichtet, dass Studenten eine eigene Instanz des Spiels starten und auch den Code ändern können.
  - in einer Multiplayer-Variante muss es einen allen Teilnehmern gemeine Simulation geben
  - um in diesem Rahmen weiter programmatisch Einfluss auf die Simulation nehmen zu können, müsste z.B. der Code Gegenstand des Spiels werden
- Spaß
  - viele Lernspiele (Gamification im Allgmeinen) kranken daran, dass das Ergebnis keinen Spaß mehr macht
  - nicht nur Spaß, sondern auch der Ausdruck der eigenen Persönlichkeit im Lernprozess
  - die grundsätzliche Überzeugung ist trotzdem, dass es möglich sein sollte, Spaß und Lernen zu verbinden
  - prinzipieller Widerspruch zwischen zweckgebundenem Lernen und dem Spiel als Selbstzweck
    - Spiele werden häufig um ein spaßiges Element herum aufgebaut
    - in der Lehre kommen die Inhalte zuerst
  - für die Vermittlung neuer Inhalte sollte die Lösung vielleicht der Begrenztheit des 'Constructivist Teaching' bewusst sein oder sogar einen Rahmen vorgeben
- Immersion
  - individuelle Ansprüche an die Lernerfahrung
    - Mathe-Nerds sind vielleicht weniger an immersiven Elementen interessiert (wann geht es endlich los?)
    - Kinder profitieren mehr vom Setting, halten aber nicht so lange durch (weniger Zeit für Inhalte)
  - mögliche Konsequenzen:
    - Sollte an der Erschaffung eines didaktischen Escape-Rooms auch ein Spieldesigner mitwirken?
    - Sollen die Spieler den Zeitaufwand für die Immersion selber festsetzen können?
    - Untescheidung von Vorgehen abhängig vom Zielalter, Rahmen, Themenfeld etc.
- zeitlich begrenzte Förderung
  - mögliches Ziel: Prototyp, der sich bereits für den Einsatz eignet
