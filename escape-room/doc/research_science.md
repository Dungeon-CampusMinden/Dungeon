---
title: "Recherche zu virtuellen Escape-Rooms in der Lehre - Wissenschaftliche Veröffentlichungen"
---

# Ziele

- neuere Meta-Studien zur Verwendung von Spielen in der Lehre
- erste Ideen für Zuordnung von Rätsel-Typen zu bestimmten didaktischen Inhalten?
- gibt es schon Lernspiel-Frameworks?
- wie können Tools aussehen?

Ergebnisse liegen in sources/.

# Recherche-Parameter

## Datenbanken

- DigiBib der HSBI
- WebOfScience
- Google Scholar

## Schlüsselworte für Suche

- escape room / puzzle game / adventure game
  - escape rooms können als erweiterung von puzzle games verstanden werden
- education / teaching / learning

# Forschungsfragen

## Was sagen neuere Meta-Studien zur Verwendung von Escape Rooms in der Lehre?

> (review | meta) (escape room) (teaching | learning | education)

Nach welchen Kriterien werden Implementierungen bewertet?
Worauf sollten wir achten, damit unser Ergebnis brauchbar ist?

Meta-Studien explizit zu Escape-Rooms in der Lehre wurden nicht gefunden. Allgemeinere Studien zu GBL/GBA sind zhu2023review und su2024review.

## Gibt es schon Escape-Room-Frameworks?

Ja. Allerdings passt keins davon zu den ausgewählten Ausprägungen.

### EscaPP

- nicht Echtzeit
- keine Lösung im Team

Web-basiertes Tool zur Verwaltung von Studenten und Aktivitäten. Die Präsentation der Puzzles ist von Haus aus eine Website mit einer zu beantwortende Frage/einem zu Lösenden Rätsel. Es gibt REST- und WebSocket-Schnittstellen, die an den Dungeon angebunden werden könnten. Die Simulation eines Escape-Rooms im Sinne eines räumlichen Spiels ist damit allerdings nicht abgedeckt.
Diese Lösung müsste für die Studenten gehostet werden. Damit ist der Escape-Room für die Studenten nicht eigenständig durchführbar.
Fraglich ist auch, ob die Lehrenden die Studenten und die Escape-Room-Durchläufe verwalten wollen.
Ein möglicher Pluspunkt wäre, dass man wahrscheinlich relativ einfach einen Broker für die Verbindungen der Spielclients einbauen könnte.
Es gibt kein gemeinsames Spiel, lediglich die Ergebnisse werden ausgetauscht.

### EscapED

- nicht digital

Ein Konzept zur Verwendung physischer Escape-Rooms in der Lehre. Das Projekt dahinter hat bisher nur Erfahrung mit Escape-Room-spezifischen Lernzielen.

> The educational objective of the pilot game was for players to develop soft skills such as communication, leadership and teamwork throughout their experience

..

> could see the educational value of escapED, especially if the puzzles and theme of the experience were worked into their taught subject matter

Hervorzuheben ist hier auch der etwas andere Ansatz zum Vermitteln von Inhalten (von der dazugehörigen Website):

> The emphasis of the educational escape room is not on the learning within the allotted time of the game itself, but rather in the reflective process within the debrief period. The game itself merely provided the mechanism for which to create a rich reflective process.

Das gemeinschaftliche Lernen findet in der Nachbesprechung statt - wie der Escape-Room genau aussieht steht nicht unbedingt im Mittelpunkt.

### Ergebnisse außerhalb der Wissenschaft

Sammlung in [separatem Dokument](./research_other.md#solutions)

## Anders herum: Wie können Tools aussehen?

Keine guten Treffer in der Wissenschaft gefunden.
Sammlung in [separatem Dokument](./research_other.md#tooling). Decken nur Teile der Erstellung ab.

## Finden wir erste Ideen für Zuordnung von Rätsel-Typen zu bestimmten didaktischen Inhalten?

Nicht wirklich. antonova2019puzzles stellt eine Taxonomie auf, allerdings bezieht sich die Kategorisierung nicht auf sachbezogene Lerninhalte.

## Welche Rätsel-Typen werden für Lerninhalte in der höherqualifizierenden Lehre verwendet?

Bisher noch kein wirklich überzeugendes Material gesichtet.

## Wie wichtig ist Immersion für uns?

Viele Escape-Room-Design-Guides fangen mit der Story an. In der Lehre kommen die Lerninhalte zuerst.
Warum sollen wir uns für Immersion interessieren?
Bei Studenten im selbst-gewählten Studium gibt es hoffentlich ein bisschen mehr intrinsische Motivation als bei Kindern in einem obligatorischen Unterricht.

Große Spannweite an Papern. Von Augentrackern an Mathestudenten bis Screen-Capture bei Grundschülern.
Suche noch ein gutes Paper, das etwas über den Zusammenhang von Spaß/Immersion und Lernerfolg in höherer Bildung sagt. Große Effektstärke + statistische Signifikanz hilft uns wenig, wenn es um 9-bis-12-Jährige geht.
