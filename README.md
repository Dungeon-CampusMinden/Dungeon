
<!--  pandoc -s -f markdown -t markdown+smart+four_space_rule-grid_tables-multiline_tables-simple_tables --columns=94 --reference-links=true  README.md  -o xxx.md  -->

<h1 align="center">Dungeon</h1>
<p align="center"><img src="https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/img/banner.png?raw=true" alt="Banner"></p>

The Dungeon is a multifaceted project for the gamification of educational content.

You can find an [interesting report] on our project in the news section of Bielefeld
University of Applied Sciences (04 April 2024, in German).

For more information about the [PRODUS project], please visit our [project page] on the
Bielefeld University of Applied Sciences website.

![][1]

## Requirements

[Java SE Development Kit 21 LTS] installed.

## Known Limitations

Currently the path to the project files cannot contain any spaces, special characters or
umlauts.

This project is intended as supplementary teaching material for German-language university
courses and is therefore aimed at German-speaking students. If you have any questions,
problems or suggestions, please feel free to contact us in English or German.

## Funding

It is acknowledged that parts of the materials contained in this repository have been
developed as part of various publicly funded projects.

### Lehr-Escape-Rooms mit LowCode im Dungeon (L<ESC>ROD)

04/2025 - 03/2027, 3003-1271, [Freiraum 2025], [Stiftung Innovation in der Hochschullehre]

This project builds upon the goals and insights of the earlier *"Dungeon: Learning by
Questing"* initiative. It aims to combine **playful engagement with meaningful educational
experiences** by using the *Dungeon* framework to design and implement **didactic escape
rooms**.

In these digital escape rooms, students must **collaborate to solve subject-related puzzles**
in order to progress through the game. The approach fosters teamwork, critical thinking, and
applied understanding of academic content in an interactive game environment. To support this,
the *Dungeon* framework is being extended with **remote multiplayer capabilities**, allowing
multiple students to participate in a shared escape room experience from different locations.
Additionally, a **low-code approach** is being developed to enable educators - even those
without programming experience - to easily design and create their own escape rooms. This
makes the concept accessible and adaptable for a wide range of teaching contexts.

### Programming Dungeon Adventures at School (PRODUS)

12/2024 - 08/2026, EFRE-20300105, [Pakt für Informatik 2.0], [EFRE/JTF NRW 2021--27]

The project aims to promote interest in STEM subjects - especially computer science - among
school students in the surrounding region. The primary goal is to inspire young learners to
consider a future path in computer science by engaging them in hands-on, game-based learning
experiences.

To this end, a series of programming workshops were designed, all based on the *Dungeon*
framework and grounded in the principles of **game-based learning**. Each workshop is tailored
to different experience levels:

-   **Workshop 1: Blockly-Dungeon**

    This no-code workshop is designed for younger pupils with little to no prior programming
    experience. Using Google's Blockly language, pupils guide a hero through various levels of
    a dungeon. Along the way, they solve puzzles that gradually increase in complexity,
    introducing programming concepts such as loops and boolean expressions in an intuitive,
    visual format.

-   **Workshop 2: Java-Dungeon**

    This low-code workshop targets pupils who are already familiar with visual programming
    (like Blockly) but have not yet written actual code. Using a custom-built Visual Studio
    Code plugin, pupils control the dungeon hero by writing simple Java code. They combine
    basic programming constructs (like loops and variables) with game-specific commands (such
    as `move`, `use`, etc.), gaining their first experience with real syntax in a motivating,
    game-oriented environment.

-   **Workshop 3: Advanced Dungeon**

    This workshop is intended for pupils who already have some programming experience. Here,
    they solve complex in-game coding challenges and are introduced to selected topics in
    artificial intelligence. The workshop fosters analytical thinking and problem-solving at a
    more advanced level.

Each workshop is complemented by **career-oriented elements**: professional software
developers share insights into their daily work, and Bielefeld University of Applied Sciences
(HSBI) provides information about studying computer science and career paths in tech.

The project is a **collaborative effort** between local schools and companies in the OWL
(Ostwestfalen-Lippe) region, with HSBI providing both academic leadership and operational
coordination.

![][2]

### Spiele-Framework für Digital Game Based Learning

09/2022 - 11/2023, FRFMM-623/2022, [Freiraum 2022][Freiraum 2025], [Stiftung Innovation in der
Hochschullehre]

As part of the project, the principles of game-based learning were further explored and
applied to the existing Dungeon framework. In contrast to the previous PM-Dungeon project,
where students programmed their own games, the focus here was not on game development, but on
learning through gameplay.

Students were provided with a fully developed game in which they had to solve subject-specific
challenges directly within the game world. Real academic problems were seamlessly integrated
with game mechanics - such as crafting or puzzle-solving - to create an engaging and
meaningful learning experience. To evaluate student performance, the game included an
automated analysis system that detected whether solutions were correct or whether players
encountered difficulties during the task. The in-game challenges and puzzles were defined
using a custom domain-specific language (DSL). This DSL enables even non-programmers to create
and integrate educational tasks and puzzles into the game. It supports a variety of input
types, such as single-choice questions, matching tasks, and element combination exercises.
Depending on the level of detail provided, the DSL can either generate a specific gameplay
scenario or automatically create a diverse game environment in which players must solve tasks
to progress. Additionally, a Petri net-based system was implemented to define alternative
learning paths. This allows the game to dynamically adapt to the learner's needs - for
example, by revisiting topic areas where students previously struggled.

### PM-Dungeon

10/2021 - 11/2022, [Fellowships für Innovationen in der digitalen Hochschullehre
(digi-Fellows)], Ministerium für Kultur und Wissenschaft (MKW) in NRW im Einvernehmen mit der
Digitalen Hochschule NRW (DH.NRW)

The goal of the project was to motivate computer science students to engage deeply with
programming by providing exciting challenges and development environments. A particular focus
was placed on promoting self-directed learning, enabling students to explore programming
concepts at their own pace and based on their individual interests and progress.

As part of the project:

-   Open Educational Resources (OER) were created and published to support teaching
    programming with Java.
-   The Java-based framework Dungeon, built on libGDX, was conceptualized and developed. This
    framework has since been extended, revised, and optimized through additional projects.

The Dungeon framework was used in the "Programming Methods" course at Bielefeld University of
Applied Sciences (HSBI) to teach students advanced programming techniques in Java. Students
used the framework to develop their own 2D rogue-like games, applying key programming concepts
in a creative and practical way. To support this, the framework provides essential
functionalities to simplify complex development tasks. These include a built-in game loop,
graphical rendering of textures, an animation system, and tools for level generation and
loading. In addition, a submission process called "Deploy to Grading", based on GitHub
Actions, was developed. This allows students to submit their code solutions via GitHub pull
requests. The submitted code is automatically evaluated using a CI pipeline that includes
JUnit tests, Checkstyle, and similar tools - providing immediate, formative feedback and
reinforcing the idea of autonomous, feedback-driven learning.

## Credits

The assets in [`dungeon/assets/`] and [`devDungeon/assets/`] are a mix from free and self
created resources:

-   Textures and animations:
    -   https://0x72.itch.io/16x16-dungeon-tileset (CC0 1.0)
    -   https://0x72.itch.io/dungeontileset-ii (CC0 1.0)
-   Music and sound effects:
    -   https://alkakrab.itch.io/free-12-tracks-pixel-rpg-game-music-pack (CC0 1.0)
    -   https://opengameart.org/content/50-rpg-sound-effects (CC0 1.0)
    -   https://opengameart.org/content/hurt-death-sound-effect-for-character (CC0 1.0)
    -   https://opengameart.org/content/80-cc0-creture-sfx-2 (CC0 1.0)
    -   https://freesound.org/s/578488/ (CC0 1.0)
-   Adapted and modified by [\@Flamtky][]:
    -   Files (except [Health Potion]) in [`dungeon/assets/items/potion/`] (originating from
        [\@dkirshner])
    -   Files in [`dungeon/assets/dungeon/*/floor`][]: each `floor_damaged.png` (originating
        from [\@dkirshner])
    -   [`dungeon/assets/dungeon/fire/floor/floor_1.png`] (originating from [\@dkirshner])
-   Self created by [\@Flamtky][]:
    -   Files in [`devDungeon/assets/objects/spawner/`]

## Licenses

Unless otherwise noted, this [work] by [contributors] is licensed under [MIT].

All files in [`doc/publication/`] are licensed under [CC BY-SA 4.0].

<p align="right"><img src="https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/img/logo/cat_logo_64x64.png?raw=true" alt="Banner"></p>

  [interesting report]: https://www.hsbi.de/presse/pressemitteilungen/informatik-studierende-am-campus-minden-entwickeln-2d-rollenspiel-zum-lehren-und-lernen
  [PRODUS project]: #programming-dungeon-adventures-at-school-produs
  [project page]: https://www.hsbi.de/minden/produs/home
  [1]: dungeon/doc/img/monster.gif
  [Java SE Development Kit 21 LTS]: https://jdk.java.net/21/
  [Freiraum 2025]: https://stiftung-hochschullehre.de/foerderung/freiraum/
  [Stiftung Innovation in der Hochschullehre]: https://stiftung-hochschullehre.de/
  [Pakt für Informatik 2.0]: https://www.efre.nrw/einfach-machen/foerderung-finden/pakt-fuer-informatik-20
  [EFRE/JTF NRW 2021--27]: https://www.efre.nrw/
  [2]: blockly/doc/img/examples/blockly_gif.gif
  [Fellowships für Innovationen in der digitalen Hochschullehre (digi-Fellows)]: https://www.dh.nrw/kooperationen/Digi-Fellows-2
  [`dungeon/assets/`]: dungeon/assets/
  [`devDungeon/assets/`]: devDungeon/assets/
  [\@Flamtky]: https://github.com/Flamtky
  [Health Potion]: dungeon/assets/items/potion/health_potion.png
  [`dungeon/assets/items/potion/`]: dungeon/assets/items/potion/
  [\@dkirshner]: https://github.com/dkirshner
  [`dungeon/assets/dungeon/*/floor`]: dungeon/assets/dungeon/
  [`dungeon/assets/dungeon/fire/floor/floor_1.png`]: dungeon/assets/dungeon/fire/floor/floor_1.png
  [`devDungeon/assets/objects/spawner/`]: devDungeon/assets/objects/spawner/
  [work]: https://github.com/Dungeon-CampusMinden/Dungeon
  [contributors]: https://github.com/Dungeon-CampusMinden/Dungeon/graphs/contributors
  [MIT]: LICENSE.md
  [`doc/publication/`]: doc/publication/
  [CC BY-SA 4.0]: LICENSE-PAPER.md
