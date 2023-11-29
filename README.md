<h1 align="center">Dungeon</h1>
<p align="center"><img src="doc/img/banner.png" alt="Banner"></p>

Das Dungeon ist ein mehrdimensionales Projekt zur Gamifizierung von Studieninhalten. 

Mit dem Dungeon-Framework kann Java erlernt werden, indem ein eigenes RPG programmiert wird. 

Im Dungeon-Projekt "Learning by Questing" können klassische Übungsaufgaben in das Spiel geladen und spielerisch gelöst werden. 

Das Blockly-Dungeon richtet sich an komplette Neueinsteiger und dient zur Visualisierung einfacher Algorithmen.

## Dungeon-Framework

Dieses Java-Framework, das auf libGDX basiert, ermöglicht die einfache Entwicklung eines 2D-Rollenspiels im Stil von Rogue. Es eignet sich besonders gut für Programmieranfänger, da es komplexe Aufgaben wie die Generierung von Leveln und das Zeichnen sowie Animieren von Figuren abstrahiert. Dadurch kann sich der Anwender auf die Java-Programmierung konzentrieren.

Das Framework befindet sich im Projekt [`game`](./game).

Die [Dokumentation](./game/doc/) und der [Quickstart](./game/doc/quickstart.md) sollten einen schnellen Einstieg ermöglichen.

![](./game/doc/img/monster.gif)


## Learning by Questing

Das Framework bietet die Möglichkeit, durch die Integration von Quests zu lernen. Quests sind dabei typische Übungsaufgaben aus dem Studienkontext. Diese Aufgaben werden in einer einfachen, eigenen Sprache definiert, ohne dass die Lehrenden selbst Spielmechaniken programmieren müssen. Das Framework wandelt die definierten Aufgaben automatisch in verschiedene Spielszenarien um und ermöglicht so das Erstellen von individuellen Lernpfaden.

Das Dungeon befindet sich im Projekt [`dungeon`](./dungeon).

Die [Dokumentation](./dungeon/doc) und der [Quickstart](./dungeon/doc/readme.md) sollten einen schnellen Einstieg ermöglichen.

![](./dungeon/doc/dsl/img/quickstart_select_config_level.png)
![](./dungeon/doc/dsl/img/quickstart_answer_menu.png)

## Low Code: Blockly-Dungeon
Dieser Teil des Projekts nutzt Googles Blockly, um eine grafische Low-Code-Benutzeroberfläche zu generieren. Über eine Webschnittstelle (lokal) kann die Spielfigur im Dungeon gesteuert werden, wodurch auch Anwender ohne tiefgreifende Programmierkenntnisse am Erlebnis teilhaben können.

Das Blockly-Dungeon befindet sich im Projekt [`blockly`](./blockly).
Die [Dokumentation](./blockly/doc) sollte einen schnellen Einstieg ermöglichen.

![](./blockly/doc/img/examples/komplexes_beispiel.png)

## Requirements

-   Java SE Development Kit 17.0.x LTS

## Contributing

Questions, bug reports, feature requests and pull requests are very welcome.
Please be sure to read the [contributor guidelines](CONTRIBUTING.md) before
opening a new issue.

## Language

This project is intended as teaching material for German-language university
courses and is therefore aimed at German-speaking students. Please keep in
mind that the English documentation may therefore not be available or may be
slightly out of date. If you have any questions, problems or suggestions, please
feel free to contact us in English or German.


## Credits

This project is funded by [Stiftung für Innovation in der Hochschullehre](https://stiftung-hochschullehre.de)
(["Freiraum 2022"](https://stiftung-hochschullehre.de/foerderung/freiraum2022/)).

The used [Assets](doc/wiki/assets-im-dungeon.md) are blended from free resources.

---

## License

This [work](https://github.com/Programmiermethoden/Dungeon) by
[André Matutat](https://github.com/AMatutat),
[Malte Reinsch](https://github.com/malt-r),
[Carsten Gips](https://github.com/cagix), and
[contributors](https://github.com/Programmiermethoden/Dungeon/graphs/contributors)
is licensed under [MIT](LICENSE.md).

All files in [`doc/publication/`](doc/publication/) are licensed under [CC BY-SA 4.0](LICENSE-PAPER.md).

All files in [`game/assets/`](game/assets/) are licensed unter [CC-0](LICENSE-ASSETS.md).

<p align="right"><img src="doc/img/logo/cat_logo_64x64.png" alt="Banner"></p>
