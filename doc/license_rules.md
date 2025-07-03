(1) Jedes aus fremden Quellen stammende Artefakt (Textur, Logo, Sound, ...) erhält eine paralle Markdowdatei mit dem Namen `<artefaktdateiname>.license.md`. Beispiel: Artefakt `wuppie_die_hard.png` würde eine Lizenz-Datei `wuppie_die_hard.license.md` auf der selben Dateisystem-Ebene zugeordnet.

(2) Aufbau der Datei (vgl [mit.md](../.github/license-templates/mit.md)): 

```markdown
- Quelle: <URL>
- Author: <Name> (wenn bekannt)
- Modified by: <Name> (falls relevant)
- License: <License Name> ("MIT", "CC0", "CC BY-SA 4.0", ...)

<details>

kompletter Text der Original-Lizenz

</details>
```

(3) Für die üblichen Lizenzen gibt es Templates in `.github/license-templates/`: "MIT", "CC0", "CC BY-SA 4.0", für andere Lizenzen bitte [choosealicense.com](https://choosealicense.com) verwenden.

(4) Perspektivisch werden zwei Gradle-Tasks implementiert. Der eine sammelt rekursiv alle `*.license.md` auf und kopiert sie der Reihe nach in die globale Datei `CREDITS.md`. Dabei werden pro Ordner-Ebene automatisch Überschriften mit dem Ordnernamen eingefügt. 

Beispiel: Ordner `assets/foo/bar/fluppie/` würde die Überschriften 

```markdown
# assets
## foo
### bar
#### fluppie
```

einfügen, und darunter dann den Inhalt aller `*.license.md` Dateien unterhalb von `assets/foo/bar/fluppie/`. Anschließend würde ein `#### wuppie` für den Unterordner `assets/foo/bar/wuppie/` gefolgt von den Inhalten der dortigen `*.license.md` Dateien kommen usw.

Der zweite Task ruft den ersten auf und vergleicht alt und neu und liefert ein entsprechendes Ergebnis.

Der erste Task kann lokal in der IDE aufgerufen werden, um die CREDITS.md zu aktualisieren. Der zweite Task wird in die CI als "Test" eingebaut.

(5) Im Top-Level README.md wird unter "License" folgender Text auftauchen: `Unless otherwise noted, this work by [contributors](https://github.com/Dungeon-CampusMinden/Dungeon/graphs/contributors) is licensed under [MIT](LICENSE.md). See the [credits](CREDITS.md) for a detailed list of contributing projects.`

(6) Im Top-Level README.md entfällt der Abschnitt "Credits".
