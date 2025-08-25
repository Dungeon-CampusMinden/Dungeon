---
title: "Recherche zu virtuellen Escape-Rooms in der Lehre - Erzählung"
---

> Video games is the most difficult medium to write for. [..] There is very little overlap between those who are good writers at a baseline, and those who are able to piece together a coherent story as the development process inevitably brutalizes their work.

[via reddit](https://www.reddit.com/r/truegaming/comments/16fuqu5/comment/k04b4ri/)

Die Erzählung in Videospielen erfüllt die Kriterien eines [komplexen Problems](https://de.wikipedia.org/wiki/Komplexes_Problem).

Viele Resourcen zu Erzähltechniken in Videospielen sind auf große Produktionen ausgerichtet. Die prinzipiellen Probleme ("Die Geschichte muss sich den Anforderungen an das Spiel beugen") gelten aber auch im Kleinen.

# <a name="video_game_means"></a>Mittel zur Erzählung in Spielen

- explizit
  - innerhalb der Simulation
    - Sounds/Musik
    - (bewegte) Bilder
    - Interaktivität
    - konsumierbare Gegenstände (Bücher/Poster/Aushänge)
    - Artefakte
      - bspw. Tassen in "Horizon: Zero Dawn": "wurden bestimmt für religiöse Rituale verwendet"
    - Dialog
      - häufig gesprochen
      - nicht notwendigerweise an den Spieler gerichtet
    - unzuverlässiges Erzählen
      - bspw. Psychosen in "Hellblade: Senua's Sacrifice"
      - bspw. Batman unter dem Einfluss von Scarecrows Gift
  - außerhalb der Simulation
    - "Star Wars Intro"
    - Cutscenes
    - Booklet
    - Erzählung vom Gamemaster
    - Website
  - "4th-Wall-Breaking"
    - "Absturz" des Spiels (Pony Island, Arkham Asylum)
- implizit
  - Ereignisse/Begebenheiten in der Simulation
    - ein leeres, von Federn umgebenes Vogelnest
    - Propaganda auf einem öffentlichen Bildschirm
    - beflügelnde/beklemmende/intuitive/einfache/umfangreiche Spielmechanik
      - Sprung in Super Mario
    - Interaktion mit Charakteren
      - vlt. mit dem Ziel einer Verbindung (Horizon: Zero Dawn, Animal Crossing, Potionomics)
    - Kennenlernen/Erforschen von Characteren/Spielwelt
     - Kameraführung in Metal Gear Solid
  - Entscheidungen des Spielers/Spielweise
    - Auswahl einer Charakterklasse/Antwort in einem Dialog
    - Verwendung von Cheats/Hacks/Mods
    - Beeinflussung des Spielverlaufs/der Simulation durch Spielmechanik (z.B. Entführung eines Charakters)
    - alle Kisten leeren ("nimmt alles mit")
    - nur mit Dolch ("schleicht unentdeckt heran")

# Andere Veröffentlichungen

## Alternativen in der Erzählung

Die starken Parallelen einer [Veröffentlichung zu "Narrative Graph Models"](https://maetl.net/notes/storyboard/narrative-graph-models) und [Abhängigkeitsgraphen](./requirements_tooling.md) offenbaren die Verwandtschaft von Geschichte und der Verknüpfung der Puzzle.

In ["The Shapes in your Story"](https://gdcvault.com/play/1023095/The-Shapes-in-Your-Story) heißen die Graphen "Story Maps". Die Folien sind aus einer Veranstaltung an der Northeastern University - College of Professional Studies. Es wird das Konzept von "Narrative Mapping Framework" vorgestellt. Das beispielhafte Template heißt "Lock and Key" (40f) und es dient als Vehikel zur Präsentation von Puzzeln, um die herum eine Geschichte geschrieben wird.

In [All Choice No Consequence: Efficiently Branching Narrative ](https://gdcvault.com/play/1023072/All-Choice-No-Consequence-Efficiently) werden sehr konkrete Anweisungen zum Umgang mit Story und Branching gegeben. Die Perspektive ist "Dating-Spiele auf dem Handy". Eine Auswahl nach Einordnung und Nutzen: "choices that FEEL impactful vs. those that ARE (too expensive)", "options with equal weight" (avoid clear right or wrong), "unavoidable Consequences - bury it in choices", "avoid false, misleading, or vague choices", "don't add branches until you have a great story", "write immediate actions", "never negate player agency". Hier wäre hervorzuheben, dass wahrscheinlich nichts davon Anwendung finden sollte, wenn es um Entscheidungen geht, die Lerninhalte betreffen.

Emily Short stellt in [Beyond Branching: Quality-Based, Salience-Based, and Waypoint Narrative Structures](https://emshort.blog/2016/04/12/beyond-branching-quality-based-and-salience-based-narrative-structures/) drei Paradigmen gegenüber. 'Quality-based' bezieht sich auf das Erlangen einer "Qualität" (Messlatte) auf verschiedenen unabhängigen Wegen (um ).

## [The Art of Game Design: A Book of Lenses](https://doi.org/10.1201/b22101)

Jesse Schell motiviert seine Leser, "Interest Curves" über den Verlauf der Geschichte zu malen, ähnlich bspw. einem Intensität-Graphen, den ein Musiker verwenden würde. [Hier](https://game-studies.fandom.com/wiki/Interest_Curve) gibt es ein Bild aus dem Buch dazu.

# Aufstellung/Vergleich

Für die Beantwortung dieser Fragen:

> Welche Techniken werden für die Erzählung eingesetzt (Literatur, Software, analoges Testspiel)?
> Welche Strukturen (Puzzle-Graph) kommen finden sich in den betrachteten Spielen (Literatur, Software, analoges Testspiel)?

| Category | Conventional Literature (e.g. novella) | Software/Video Games | Analog Games|
| - | :-: | :-: | :-: |
| key attributes | <ul><li>linear</li><li>passive</li></ul> | <ul><li>often dynamically structured to enhance player agency</li><li>narration optional (e.g. Minesweeper, Fifa, Pong, Bejeweled)</li><li>active through simulation</li></ul> | <ul><li>structure can be anything from linear to completely open</li><li>provide rules for player to make the game 'active'</li><li>narration optional (or completely in the hands of the players e.g. Dixit)</li><li>can have no story at all (e.g. Poker)</li><li>or story-through-mechanics (e.g. time progression in Secret Hitler/Werewolfs, induction of anxiety in losing players during the late stages of Monopoly)</li><ul> |
| principal means for structuring | <ul><li>condense concept</li><li>create list of scenes</li><li>gradually fill in the parts</li><li>top-down</li></ul> | <ul><li>either 'story-first' (e.g. Call of Duty) or 'story-as-a-means' (e.g. XCOM)</li><li>story flow chart, storyboard, and/or puzzle dependency graph (distinct from the story flow chard)</li></ul> | <ul><li>often: strong focus on mechanics, story follows</li><li>game board (e.g. path)</li><li>story could be up for a gamemaster to create</li><ul> |
| means for storytelling | <ul><li>stylistic devices (from metaphors to dramaturgy)</li><li>illustrations</li><ul> | <ul><li>anything a text can have and much more</li><li>see [corresponding heading](#video_game_means)</li></ul> | <ul><li>most of the time: story in booklet, setting the story once at the beginning</li><li>quasi opposite: meaning derived by player from images or abstract symbolism</li><li>visual communication from objects in the game</li><li>story elements discovered during play</li></ul> |

Key points of Game narration:

- **narrative design** and **script writing** are usually distinct in video game writing (temporally and personally)
  - roles in video games typically fade in and out over the course of development
  - writers seldomly needed in late stages (except maybe for games like Disco Elysium)
  - sound or graphic designers also take over roles for storytelling
  - narration might even be organised at runtime
    - simplistic variant: dynamic content generation with little impact on story
    - more involved approach: handing out elements from pre-defined story definition
  - 'writing' focuses more on the world, the dialogue, character development, and making an enticing story
  - 'design' focuses on the player, the branching, consequences, and making the stork work in the game
- narration can be an 'intrusion' for the player, no matter how good it is
  - skipable cutscenes and dialog
  - gameplay and narration need to work together
  - 'implicit storytelling' is difficult but highly-awarded
  - alternative approaches
    - turn the story itself into an element of the game
      - Stories: Path of Destinies (re-starting the story is a part of the game and information carries over)
      - Baby is You (moving symbols, verbs, and items around on a tile floor to form sentences that change the simulation)
      - The Longing (the objective of the game is to wait 400 days)
      - or commentary: How Fish is Made, Arctic Eggs
- key moments of storytelling and play time often separated in games
  - e.g. cutscenes / narration by game master in DnD
  - no "mucking about" during dramatic climax
  - easier to control narration with superfluous parts of the simulation disabled
- critical commonalities with traditional writing
  - story needs to be enticing from the beginning (or the mechanics)
  - e.g. through unanswered questions

For our analog evaluation: [see other file](./evaluation_analog.md)
