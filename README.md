
<!-- pandoc -s -f markdown -t markdown --columns=94 --reference-links=true README.md -->

<h1 align="center">Dungeon</h1>
<p align="center"><img src="https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/img/banner.png?raw=true" alt="Banner"></p>

The Dungeon is a multifaceted project for the gamification of educational content. It
comprises three parts: "Game", "Dungeon" and "Blockly":

1.  ["Game"] constitutes a basic gaming platform that can be used in class to learn and deepen
    Java skills and allows students to develop their own role-playing game.
2.  ["Dungeon"] extends "Game" with numerous game elements and a Domain Specific Language
    (*DSL*) that can be used to "code" classic exercises and automatically convert them into a
    ready-made game. Players solve the exercises by playing the quests.
3.  ["Blockly"] adds a block-based programming language to the project. It is primarily aimed
    at programming beginners and can be used to visualise simple algorithms.

You can find an [interesting report] on our project in the news section of Bielefeld
University of Applied Sciences (04 April 2024, in German).

## Game: Dungeon Platform

The sub-project [`game`] is the foundation of the entire framework. It provides a programming
platform based on [libGDX], which is intended to support easy development of [rogue-like 2D
role-playing games] in the [Java] programming language. It is particularly suitable for
programming beginners, as it already provides solutions based on the [ECS architecture
pattern] for complex tasks such as generating levels and drawing and animating characters.
This allows the user to focus on Java programming.

The [Quickstart] (German) and the [Documentation] (German) should help you get started
quickly.

![][1]

## Dungeon: Learning by Questing

The sub-project [`dungeon`] extends "Game" and provides a wide range of game elements that can
be used directly to create a rogue-like 2D role-playing game.

Teachers can use the DSL provided by the project to conveniently devise typical exercises from
the study context. The framework automatically translates these formally described exercises
into various game scenarios and generates ready-to-play games as a result. There are also
various control mechanisms that make it possible to devise customised learning paths. The
exercises are presented as quests in the generated game. Teachers do not have to code the game
mechanics themselves. (Well, of course you always can add your own mechanics using Java code.)

The [Quickstart][2] (German) and the [Documentation][3] (German) should help you get started.

The [Dungeon: StarterKit] provides you with everything you need to get started immediately
without coding and/or compiling.

![][4] ![][5]

## Blockly: Low Code Dungeon

The sub-project [`blockly`] extends "Dungeon" and uses [Google's Blockly] to provide a
graphical low-code user interface. The character in the dungeon can be controlled via a web
interface (locally), allowing users without in-depth programming knowledge to take part in the
experience.

The [Documentation][6] (German) should help you get started.

![][7]

## Requirements

[Java SE Development Kit 21 LTS] installed.

## Known Limitations

Currently the path to the project files cannot contain any spaces, special characters or
umlauts.

This project is intended as supplementary teaching material for German-language university
courses and is therefore aimed at German-speaking students. If you have any questions,
problems or suggestions, please feel free to contact us in English or German.

## Credits

This project was funded by [Stiftung für Innovation in der Hochschullehre] (["Freiraum
2022"]).

The assets in [`game/assets/`](game/assets/), [`dungeon/assets/`](dungeon/assets/) and [`devDungeon/assets/`](devDungeon/assets/) are a mix from free and self created resources:

-   Textures and animations:
    -   https://0x72.itch.io/16x16-dungeon-tileset (CC0 1.0)
    -   https://0x72.itch.io/dungeontileset-ii (CC0 1.0)
-   Music and sound effects:
    -   https://alkakrab.itch.io/free-12-tracks-pixel-rpg-game-music-pack (CC0 1.0)
    -   https://opengameart.org/content/50-rpg-sound-effects (CC0 1.0)
    -   https://opengameart.org/content/hurt-death-sound-effect-for-character (CC0 1.0)
    -   https://opengameart.org/content/80-cc0-creture-sfx-2 (CC0 1.0)
    -   https://freesound.org/s/578488/ (CC0 1.0)
-   Adapted and modified by @Flamtky:
    -   Files (except Health Potion) in [`dungeon/assets/items/potion/`](dungeon/assets/items/potion/) (originating from @dkirshner)
    -   Files in [`game/assets/dungeon/*/floor`](game/assets/dungeon/) each `floor_damaged.png` (originating from @dkirshner)
    -   `floor_1.png` in [`game/assets/dungeon/fire/floor/`](`game/assets/dungeon/fire/floor/floor_1.png`) (originating from @dkirshner)
-    Self created by @Flamtky:  
    -   Files in [`devDungeon/assets/objects/spawner/`](devDungeon/assets/objects/spawner/)

## Licenses

This [work] by [André Matutat], [Malte Reinsch], and [contributors] is licensed under [MIT].

All files in [`doc/publication/`] are licensed under [CC BY-SA 4.0].

All files in [`game/assets/`](game/assets/), [`dungeon/assets/`](dungeon/assets/) and [`devDungeon/assets/`](devDungeon/assets/) are licensed under [CC0 1.0].

<p align="right"><img src="https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/img/logo/cat_logo_64x64.png?raw=true" alt="Banner"></p>

  ["Game"]: #game-dungeon-platform
  ["Dungeon"]: #dungeon-learning-by-questing
  ["Blockly"]: #blockly-low-code-dungeon
  [interesting report]: https://www.hsbi.de/presse/pressemitteilungen/informatik-studierende-am-campus-minden-entwickeln-2d-rollenspiel-zum-lehren-und-lernen
  [`game`]: game
  [libGDX]: https://github.com/libgdx/libgdx
  [rogue-like 2D role-playing games]: https://en.wikipedia.org/wiki/Roguelike
  [Java]: https://jdk.java.net/
  [ECS architecture pattern]: https://en.wikipedia.org/wiki/Entity_component_system
  [Quickstart]: game/doc/quickstart.md
  [Documentation]: game/doc/
  [1]: game/doc/img/monster.gif
  [`dungeon`]: dungeon
  [2]: dungeon/doc/quickstart.md
  [3]: dungeon/doc/
  [Dungeon: StarterKit]: https://github.com/Dungeon-CampusMinden/Dungeon-StarterKit
  [4]: dungeon/doc/dsl/img/quickstart_select_config_level.png
  [5]: dungeon/doc/dsl/img/quickstart_answer_menu.png
  [`blockly`]: blockly
  [Google's Blockly]: https://github.com/google/blockly
  [6]: blockly/doc/
  [7]: blockly/doc/img/examples/komplexes_beispiel.png
  [Java SE Development Kit 21 LTS]: https://jdk.java.net/21/
  [Stiftung für Innovation in der Hochschullehre]: https://stiftung-hochschullehre.de
  ["Freiraum 2022"]: https://stiftung-hochschullehre.de/foerderung/freiraum2022/
  [`game/assets/`]: game/assets/
  [`dungeon/assets/`]: dungeon/assets/
  [work]: https://github.com/Dungeon-CampusMinden/Dungeon
  [André Matutat]: https://github.com/AMatutat
  [Malte Reinsch]: https://github.com/malt-r
  [contributors]: https://github.com/Dungeon-CampusMinden/Dungeon/graphs/contributors
  [MIT]: LICENSE.md
  [`doc/publication/`]: doc/publication/
  [CC BY-SA 4.0]: LICENSE-PAPER.md
  [CC0 1.0]: LICENSE-ASSETS.md
