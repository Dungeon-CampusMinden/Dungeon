# Das Erbe der Seelenweber (Soulweaver)

<p align="center"><img src="../../../../doc/img/readme/soulweaver.png" alt="Das Erbe der Seelenweber: the hero next to the stone golem Nox in a forge"></p>

<p align="center"><a href="https://github.com/Dungeon-CampusMinden/Dungeon/releases?q=Seelenweber"><img src="https://img.shields.io/badge/Download-Das_Erbe_der_Seelenweber-2ea44f?style=for-the-badge&logo=github" alt="Download Das Erbe der Seelenweber"></a></p>

A fantasy escape room for first steps in programming, for 1–2 players. From the repository:
`./gradlew :game:runSoulweaver`

## For players

Step into the abandoned forge of Aethelgard. Master Valerius set out for the Heartfire and never
came back. Rubble jams the winch in the cellar, hot steam hisses from the pipes, and the only
helper left is Nox, a stone golem whose soul binding has gone out. Wake him up, steer him
through the cellar he can enter and you cannot, teach him new tricks in Valerius' workshop and
lead him through the labyrinth of decisions. Nox does exactly what you tell him, so choose your
words carefully. Play alone or with a partner.

## For educators

| | |
| --- | --- |
| Subject | Computer science, first semester, introduction to programming |
| Use | After the first lecture, as self-study or for deepening |
| Players | 1–2, alone or in multiplayer |
| Duration | Planned for 60 to 65 minutes; not yet confirmed in playtests |

The room covers four basics in a fixed order. Each act starts with a problem in the game world;
the Java notation follows only once the idea behind it has worked.

| Act | Place | First the experience | Then the term |
| --- | --- | --- | --- |
| I | The forge | Values only fit into certain vessels | `int`, `double`, `String`, `char`, `boolean` as declarations |
| II | Archive and remote-controlled cellar | Nox repeats movements until something happens or a count is reached | `while`, `do-while`, `for` in rune code |
| III | Valerius' workshop | The long program does not fit into the controller | Methods, parameters, return values |
| IV | Labyrinth of decisions | Values decide the way | IF / ELSE, AND, OR as rune pseudocode |

The level of abstraction rises from act to act: in act I players match values, in act II they
choose finished programs, in act III they rework programs, and in act IV they evaluate programs
in their heads.

**Learning from mistakes:** wrong inputs cost nothing but time, and every task can be repeated.
Programs really run: a wrong program makes Nox fail visibly instead of the game just saying
"wrong". For every task the group can unlock graded hints, from an explanation of the task to
the key idea to marking the solution, followed by an optional automatic solution. The quest log
keeps every attempt per act.

**Evaluation:** with the players' consent, the room records start, solution, attempts with the
reasons for failure, and the hints used for each puzzle.

-   [Concept] with acts I to IV (German)

## Technical notes

-   **Play a release:** download `Soulweaver.jar` (or the Windows or Linux package) from the
    [releases] and start it with `java -jar Soulweaver.jar`. It needs the
    [Java SE Development Kit 25 LTS].
-   **Play from source:** `./gradlew :game:runSoulweaver`
-   **Multiplayer:** one player chooses *Spiel hosten* (host game), the other
    *Spiel beitreten* (join game).
-   **Level editor:** `./gradlew :game:runSoulweaverLevelEditor`
-   **Tests:** `./gradlew :game:test --tests 'rooms.soulweaver.*'`
-   Texts: `game/assets/language/soulweaver/` (German and English)

  [Concept]: ../../../../doc/escape_room/room_concepts/soulweaver/soulweaver_concept.md
  [releases]: https://github.com/Dungeon-CampusMinden/Dungeon/releases?q=Seelenweber
  [Java SE Development Kit 25 LTS]: https://jdk.java.net/25/
