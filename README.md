
<!--  pandoc -s -f markdown -t markdown+smart+four_space_rule-grid_tables-multiline_tables-simple_tables --columns=94 --reference-links=true  README.md  -o xxx.md  -->

<h1 align="center">Dungeon</h1>
<p align="center"><img src="https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/img/banner.png?raw=true" alt="Banner"></p>

The Dungeon is a multifaceted project for the gamification of educational content.

You can find an [interesting report](https://www.hsbi.de/presse/pressemitteilungen/informatik-studierende-am-campus-minden-entwickeln-2d-rollenspiel-zum-lehren-und-lernen) on our project in the news section of Bielefeld University of Applied Sciences (04 April 2024, in German), as well as [another press release](https://www.hsbi.de/presse/pressemitteilungen/hsbi-informatiker-entwickeln-ein-tool-mit-dem-die-erstellung-von-escape-rooms-fuer-die-lehre-auch-ohne-programmier-kenntnisse-gelingt) (19 December 2025, in German).
Additionally, a local school has published an [article](https://www.herder-gymnasium-minden.de/allgemein/projekt-produs/) about the project (March 2026, in German).

For more information about the [PRODUS project], please visit our [project page] on the
Bielefeld University of Applied Sciences website.

![][1]

## Requirements

[Java SE Development Kit 25 LTS] installed.

## Known Limitations

Currently the path to the project files cannot contain any spaces, special characters or
umlauts.

This project is intended as supplementary teaching material for German-language university
courses and is therefore aimed at German-speaking students. If you have any questions,
problems or suggestions, please feel free to contact us in English or German.

## Funding

It is acknowledged that parts of the materials contained in this repository have been
developed as part of various publicly funded projects.

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

## Credits

The assets in [`dungeon/assets/`] are a mix from free and self
modified resources:

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

## Licenses

Unless otherwise noted, this [work] by [contributors] is licensed under [MIT].

All files in [`doc/publication/`] are licensed under [CC BY-SA 4.0].

<p align="right"><img src="https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/img/logo/cat_logo_64x64.png?raw=true" alt="Banner"></p>

  [PRODUS project]: #programming-dungeon-adventures-at-school-produs
  [project page]: https://www.hsbi.de/minden/produs/home
  [1]: dungeon/doc/img/monster.gif
  [Java SE Development Kit 25 LTS]: https://jdk.java.net/25/
  [Freiraum 2025]: https://stiftung-hochschullehre.de/foerderung/freiraum/
  [Stiftung Innovation in der Hochschullehre]: https://stiftung-hochschullehre.de/
  [Pakt für Informatik 2.0]: https://www.efre.nrw/einfach-machen/foerderung-finden/pakt-fuer-informatik-20
  [EFRE/JTF NRW 2021--27]: https://www.efre.nrw/
  [2]: doc/press_kit/blockly/blockly_gif.gif
  [Fellowships für Innovationen in der digitalen Hochschullehre (digi-Fellows)]: https://www.dh.nrw/kooperationen/Digi-Fellows-2
  [`dungeon/assets/`]: dungeon/assets/
  [\@Flamtky]: https://github.com/Flamtky
  [Health Potion]: dungeon/assets/items/potion/health_potion.png
  [`dungeon/assets/items/potion/`]: dungeon/assets/items/potion/
  [\@dkirshner]: https://github.com/dkirshner
  [`dungeon/assets/dungeon/*/floor`]: dungeon/assets/dungeon/
  [`dungeon/assets/dungeon/fire/floor/floor_1.png`]: dungeon/assets/dungeon/fire/floor/floor_1.png
  [work]: https://github.com/Dungeon-CampusMinden/Dungeon
  [contributors]: https://github.com/Dungeon-CampusMinden/Dungeon/graphs/contributors
  [MIT]: LICENSE.md
  [`doc/publication/`]: doc/publication/
  [CC BY-SA 4.0]: LICENSE-PAPER.md
