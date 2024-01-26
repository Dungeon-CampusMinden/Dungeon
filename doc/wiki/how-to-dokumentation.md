---
title: "How to: Dokumentation"
---


Die Dokumentation des Projektes unterteilt sich in `Javadoc` und Dokumentation im `/doc`-Ordner.

## Dokumentation des Source-Codes

Die folgenden Punkte werden als Javadoc im Source-Code beschrieben:

* Dokumentation von `public` Klassen, Methoden und Variablen
  * Wofür sind sie da, was machen sie und wie benutze ich sie? Falls sinnvoll, kann Beispielcode beigefügt werden.
  * Welche Besonderheiten oder Spezialfälle sind bei der Verwendung zu beachten?
* Falls es für nötig erachtet wird, können auch nicht-`public` Klassen, Methoden und Variablen dokumentiert werden.
* Des Weiteren können komplexere Codeabschnitte innerhalb von Methoden durch einfache Kommentare (`//`) detaillierter beschrieben werden.

Als Orientierung für die Javadoc dient der [Oracle Javadoc Guide](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html). Bitte beachten Sie insbesondere den Punkt _Add description beyond the API name_ des Guides. Eine Kurzform findet ihr in der [Online-Präsentation](https://www.hsbi.de/elearning/data/FH-Bielefeld/lm_data/lm_1359639/coding/javadoc.html) des Programmiermethoden-Kurses.

## Dokumentation im `/doc`-Ordner

Die folgenden Punkte werden im `/doc`-Ordner beschrieben:

* Allgemeine Projektstruktur
* Einstieg in das Projekt (Installation, Setup und Quickstart)
* Übersicht über alle Objekte/Klassen, wie z.B. `Component`s oder `Systems`s
  * Wofür ist eine Klasse da und in welchem Verhältnis steht sie zu anderen Klassen?
* Erläuterung von Designentscheidungen

### Aufbau des `/doc`-Ordners

Folgende Punkte sollen beim Dokumentieren im `/doc`-Ordner eingehalten werden:

* Die Dokumentation ist thematisch passend in Unterordner sortiert.
* Jeder Ordner enthält eine `README.md`, in der das Thema des (Unter-)Ordners beschrieben ist.
* Ordner und Dateinamen werden im `snake_case`-Format benannt.
* Abbildungen werden in einem Unterordner `img/` abgelegt, welcher sich im gleichen Ordner befindet wie die Datei, in der die Abbildung referenziert wird.
  * Abbildungen werden sowohl im `.png`-Format als auch im `.uxf`-Format abgelegt.
  * Bitte achten Sie auch darauf, dass die Abbildungen in der Dokumentation eine angemessene Größe haben.
  * Weitere Informationen zu Abbildungen basierend auf UML finden Sie [hier](UML).
* Textdateien werden in Markdown verfasst.
  * Jede Textdatei enthält einen YAML-Header in dem folgenden Format:

```
---
title: "TITLE"
author: "AUTHOR"
lang: "en"
---
```

## Rechtschreib- und Grammatikkorrektur

Die Dokumentation sollte vor einem Commit auf Rechtschreib- und Grammatikfehler überprüft werden. Im Folgenden finden Sie eine Liste mit möglichen Tools für unterschiedliche Anwendungen:

* Browser (Chrome): [LanguageTool](https://chrome.google.com/webstore/detail/grammar-spell-checker%E2%80%94lan/oldceeleldhonbafppcapldpdifcinji) (Nicht getestet)
* Browser (Firefox): [LanguageTool](https://addons.mozilla.org/de/firefox/addon/languagetool/)
* VS Code: [LTeX](https://marketplace.visualstudio.com/items?itemName=valentjn.vscode-ltex)
* IntelliJ IDEA: [Grazie](https://plugins.jetbrains.com/plugin/16136-grazie-professional)
* Eclipse: [LanguageTool Eclipse Plugin](https://github.com/vogellacompany/languagetool-eclipse-plugin) (Nicht getestet)
