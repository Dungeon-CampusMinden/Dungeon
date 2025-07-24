---
title: "Recherche zu virtuellen Escape-Rooms in der Lehre - Andere Lösungen"
---

# Anlaufpunkte

Resourcen für Spiele, Anleitungen, Präsentationen, Erfahrungsberichte, etc.

- GDC (gdcvault.com)
- gamasutra/gamedeveloper.com
- (Web-) Suchmaschinen
- itch.io (Spiele)

# <a name="solutions"></a> Fertige Lösungen

Gibt es existierende Lösungen um ausreichend ähnliche Ziele zu erreichen?

## Analog

### <a name="escapeif"></a> [EscapeIF](https://www.becauseplaymatters.com/escapeif)

Auf Kollaboration von Lehrenden ausgelegte Plattform zum Austausch von Spielen, die im Prinzip nur eine Tafel brauchen.
Teil der [mEducation Alliance](https://meducationalliance.org/ed-storytelling-games/), die als Non-Profit das Ziel hat, das Ökosystem der Bildung insgesamt zu verbessern.
Die vorgestellten Spiele sind auf Schulinhalte ausgerichtet, also eventuell nur begrenzt für uns anwendbar oder nach der ersten Projektphase sogar völlig irrelevant.

Dort gibt es auch Design-Anleitungen zur Erstellung von "EscapeIF" (IF - Interactive Fiction). Die widerum ist hoffentlich gut übertragbar.

### ["Classroom" von lockpaperscissors](https://lockpaperscissors.co/printable-worksheet-games)

Für den Einsatz in Schulen ausgelegte Escape-Room-Boxen.

## Digital

### [Apogee](https://web.archive.org/web/20240131152124/https://apogee.online/)

Inaktives Forschungsprojekt zur Verwendung von dynamischen Puzzle-Spielen in der Lehre. Die Forscherin antwortet auf Emails 🤞.
Viel Aufmerksamkeit auf Anpassung an Spielsituation und Schwierigkeitsgrad zur Laufzeit.

### [Badaboom!](https://www.polyu.edu.hk/kteo/knowledge-transfer/innovations-and-technologies/technology-search/4-smart-cities-and-information-technology/4_ama_02_0920/)

Lernplattform mit Ausrichtung auf mathematische Hochschulinhalte (TeX-Eingabe und Handschrifterkennung für Formeln). Das Forschungsprojekt dahinter legt die Verwendung von Spielelementen nahe (shroff2019immersion), im Endprodukt findet sich eher Gamification (Scoreboards etc.) rund um Multiple-Choice und Texteingaben.

### Cluevity

Ein Outdoor-Escape-Spiel mit GPS und Augmented-Reality. Die Zielkunden können mit einer Lizenz vorgefertigte Spiele an ihrem Standort anbieten.
Anpassungen sind oberflächlich (z.B. Bild und Text für Branding). Kein Hinweis darauf, dass die Rätsel selber veränderlich sind.
Keine explizite Ausrichtung auf Lerninhalte.

### Genially

Eine Sammlung von so etwas wie Power-Point-Templates. Bei einer Stichprobe wurden nur Multiple-Choice-Fragen gesichtet (es gab auch Bilder als Optionen).

# Testläufe von Spielen

[Bericht eines Escape-Room-Durchlaufs](doc/research/field_trip_escape_room.md)

# <a name="tooling"></a> Tooling

## Dependency Charts

There's a [guide on dependency charts](https://grumpygamer.com/puzzle_dependency_charts/) from the personal blog of Ron Gilbert (games industry).
[This blog post](https://heterogenoustasks.wordpress.com/2015/01/26/standard-patterns-in-choice-based-games/) attempts to categorize different graph layouts.

### Puzzlon

The [wiki page on the Puzzlon Editor](https://www.ifwiki.org/Puzzlon) of the Interactive Fiction Technology Foundation also details the concept and has an elaborate example from a real game.
This wiki page also shows a snippet of code used to generate the graph:

```
end_of_part_one : goal {
   depends_on      = [ gain_access_to_mansion ]
   end_state       = positive
}

gain_access_to_mansion : goal {
   depends_on = [ smash_lock_on_gate,  calm_dog ]
}

get_beer_mat           : action {
   depends_on= [ talk_to_man, at_tavern ]
}

get_branch             : action {
   depends_on = [ climb_tree ]
}
```

There is also a version of the editor for [running inside the web browser](https://adventuron.io/puzzlon/).

An escape room language that draws from both (DSL and more generic dependency graph) could be the basis for defining escape rooms for use with the dungeon.

### PuzzleGraph

A more interactive tool is [PuzzleGraph](https://hg.sr.ht/~runevision/puzzlegraph) (pre-built binaries linked). It reflects stateful game entities like switches, sensors, and doors. Probably out of scope for us.

## <a name="story_tools"></a> Story/Narrative Mapping

Mit den Puzzle-Dependency-Charts verwandt sind "Story-Graphs", die auch von Autoren passiver Geschichten (z.B. Buch) eingesetzt werden. Sie unterscheiden sich prinzipiell dadurch, dass sie der Strukturierung der Geschichte dienen und nicht der Lösung von Abhängigkeiten zur Erreichung eines Ziels.

[Twine](https://twinery.org/) ist ein auf Narrativ ausgerichtes interaktives Tool (erlaubt Beschreibungen der Knoten).

[Storyspace](https://www.eastgate.com/storyspace/) ist ein ähnliches kommerzielles Werkzeug für macOS, das allerdings aus der Vermeidung von Plotlöchern entstanden zu sein scheint.
