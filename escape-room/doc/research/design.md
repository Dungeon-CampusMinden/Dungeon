---
title: "Recherche zu virtuellen Escape-Rooms in der Lehre - Erstellung eines Escape-Rooms in der Lehre"
---

# Welche Erwartungen werden an den Prozess gestellt?

Lehrende interagieren voraussichtlich zur Erfüllung der folgenden Zwecke mit dem System:

- Erstellung eines Escape-Rooms anhand des Lerninhalts
- Bereitstellung des Escape-Rooms für die Lernenden
- evtl. Einsicht in Metriken

## Annahmen über die Erstellung des Systems

Lehrende starten vermutlich in erster Linie mit einem Lerninhalt. Die Findung der Geschichte und geeigneter Rätsel ist ein [komplexes Problem](https://de.wikipedia.org/wiki/Komplexes_Problem), das sich nicht allein mit Software sinnvoll lösen lassen wird. Der Benutzer ist dafür verantwortlich, gute Escape-Rooms zu bauen. Die Software kann dabei helfen indem sie den Prozess lenkt, Mängel erkennt und allgemein die Erstellung von effektiven Escape-Rooms begünstigt. Ziele und Vorgehen sollten dem [Vorgehen](#approach) entsprechen und Erkenntnisse über bspw. Belohnungssysteme oder Lernmechanismen berücksichtigen.

Eine einfach umzusetzende Lösung ist die Bereitstellung von vorgefertigten Schablonen zur Adaption an Lerninhalte. Dabei ist zu untersuchen, ob die Geschichte noch "funktioniert" und z.B. die [Motivation](motivation.md) nicht negativ beeinträchtig wird.

# Wie funktioniert das Design bei existierenden Lösungen?

Wir suchen Richtlinien dafür, was vom Dungeon und den Tools erwartet wird, um damit Escape-Rooms zu erstellen.

## <a name="escapeif"></a> [EscapeIF](#escapeif)

Hier gibt es eine eigene Sammlung von Anleitungen, die auf analoge Spiele mit Grundschülern ausgerichtet sind. Das Konzept überträgt sich gut auf digitale Escape-Rooms und ist im Einklang mit Forschung, die höhere Bildung behandelt.

[Die Schritte](https://docs.google.com/document/d/1Xgsuv-6KgIfou8HrvkBkf414iZuuy4qaj1oenZYdaTs/edit#heading=h.81iim33prqjb) ([hier](https://www.becauseplaymatters.com/escapeif) verlinkt) sind:

- Selecting Specific Learning Outcomes
- Developing the Story World and Genre
- Creating the Story Beats
- Expanding the Learning Outcomes
- Brainstorming Connections between the Learning Outcomes and the Story
- Developing the Challenges
- Rounding Out the Narrative
  - Adding Additional Choices
  - Introducing an Ally ('create a role for the teacher' - doesn't apply here)
- Creating the Reflection
- Playtesting
- Optional: Adding Additional Assets to the Game

> For a one-hour game, it is recommended to have 1-3 learning outcomes (EscapeIF)

## <a name="escaped"></a> [EscapED](research/science.md#escaped)

In dem Artikel zu dem Design von Escape-Rooms im Rahmen von EscapED werden unter Angabe der wissenschaftlichen Einflüsse, folgende Stufen des Designs festgelegt:

- "Participants" (Publikum, Zeit, Schwierigkeit)
- "Objectives" (Lernziele)
- "Theme" (Narrativ)
- "Puzzles" (Puzzles, Anweisungen, Hinweise)
- "Equipment" (Einrichtung der Lokalität, benötigte Gegenstände, Akteure)
- "Evaluation" (Testen, Reflektieren, Abgleich mit Lernzielen, Anpassen, Zurücksetzen)

## Guideline to Creating a Virtual Escape Room

Bei GameChangers (gchangers.org) veröffentlicher Artikel "A Guideline to Creating a Virtual Escape Room (VER) in Microsoft Teams and Zoom".
Folgende abstrakte (und übertragbare) Schritte werden angegeben:

> 1. Ideation – Determining what your game is about.
> 2. Research – Researching the subject of your game.
> 3. Conceptualisation – Creating the storyline and characters utilised within your game.
> 4. Game Design – Designing puzzles and educational materials for inclusion within your game.
> 5. Content Creation – Creating the content and forming the platform of your game.
> 6. Quality Assurance – Testing and improving your game.

## Außerhalb der Lehre

### [Interview mit Escape-Room-Designer](https://static1.squarespace.com/static/62bc928cda86cf2dcc7f378d/t/65a85086591a9e1f2a897b2b/1705529482357/CoolJobsEscapeRoom.pdf) am 20. November 2023 in ScienceWorld von Scholastic.

Es geht um das Design kommerzieller Escape-Rooms.

> I start by deciding on the genre (type of game) and setting (where it takes place). I outline the players’ goals and the obstacles they’ll face. These decisions let me create “beats,” or major points in the game’s narrative. Next I start thinking of challenges—math and word puzzles, searching activities, or physical tasks—that could fit within the story. We want a variety of challenges. That way, every player gets a chance to solve something and feel like a hero.
>
> I work with a team. We sketch out ideas on paper or using digital drawing tools and 3-D design programs. Once we have our prototypes, or early versions, of the puzzles, we test and rework them. This process is known as iteration, and it’s the most important part of game development. The next step is to construct the actual room that will make players feel immersed in the game.

### [The Process of Designing an Escape Room](https://reddoorescape.com/blog-the-process-of-designing-an-escape-room/)

Escape-Room-Anbieter schreibt über den Prozess im eigenen Blog.

> 1. CREATE YOUR SETTING AND THEME
> ..
> 2. DETERMINE HOW PLAYERS DISCOVER CLUES
> ..
> 3. DESIGN YOUR PUZZLES AND GAMEFLOW
> ..

### [Escape Room Design Blueprint](https://lockpaperscissors.co/escape-room-design-blueprint)

Die Autoren verkaufen Escape-Room-Boxes für Feiern (und Unterricht, die Anleitung ist aber nicht darauf ausgerichtet). Die Anleitung ist nicht methodisch und nur lose strukturiert, dafür hat sie eine große Spannweite bis hin zu Musik und Essen. Für Ideenfindung und Orientierung lohnt sich ein Blick darein vielleicht trotzdem.

# Zusammestellung des Workflows zur Erstellung eines Escape-Rooms

## Beobachtungen

Der grobe Ablauf ist in etwa immer gleich:

- Narrativ/Setting/Thema und Lernziele werden am Anfang definiert
- Rätsel und Geschichte werden "ausgefüllt"
- Playtesting

Zusätzlich zu den Referenzprozessen, sollen die Erkenntnisse aus der Recherche (bspw. nicholson2015meaningful oder shroff2019immersion) berücksichtigt werden.

## <a name="approach"></a> Abgeleitetes Vorgehen

- Festlegung grober Lernziele
- Auswahl von Umgebung und Leitmotiv
- Festlegung von Spielphasen und Meilensteinen
  - bspw. Einführungsrätsel + drei Kernrätsel mit ansteigender Schwierigkeit
- Konkretisierung der Lernziele im Kontext der Geschichte
  - wie bringt die Meisterung der Herausforderung die Geschichte voran?
- Konkretisierung der Herausfoderungen/Rätsel
- Abrundung der Geschichte
  - "Onboarding"
  - zusätzliche Entscheidungen (alternative Lösungsansätze und/oder erzählerische Elemente) um verschiedene Lerntypen abzuholen
- Gestaltung der Reflektionsphase nach dem Spiel
  - Frage nach Lernerfolgen, Schwierigkeiten, Anwendungen aus der echten Welt
  - möglichst die Lernenden beantworten lassen
    - z.B. "Eine Änderung für ein Projekt auf Github einreichen"
    - "bin zuversichtlich"
    - "bin verunsichert"
    - "wie hilft mir das in meinem Beruf weiter?"
  - ggf. mit Feedback für Lehrende kombinierbar
- iterative Verbesserungen durch Playtesting

Als begleitende Orientierungshilfe werden folgende Aspekte und die [6 Fragen von Nicholson](sources/nicholson2015meaningful.md#questions) hervorgehoben.

- unterschiedliche Lerntypen berücksichtigen
- Erreichung der Lernziele und Spaß müssen vorsichtig gegeneinander abgewogen werden ("what makes or breaks a game")
- "meaningful gaming elements and competitive game mechanics are central to facilitating successful learning outcomes"
- Erhalt der Motivation durch Berücksichtigung der von den Spielenden erwarteten Schwierigkeiten und ihrer Einschätzung der eigenen Fähigkeiten
