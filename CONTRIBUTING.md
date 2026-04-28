---
title: "Contributing"
---

Zur besseren Zusammenarbeit im Team gelten folgende Konventionen für Issues und Pull Requests. Bitte beachte diese bei der täglichen Arbeit mit dem Repository.

## Issues
* **Sprache**: Titel können deutsch oder englisch sein; Beschreibungen sind auf deutsch.
* **Beschreibungen** sollen präzise und so ausführlich wie nötig sein. Ziel ist eine eindeutige Nachvollziehbarkeit des Problems oder Vorschlags.
* **Screenshots oder andere Anhänge** sind bei Bedarf ergänzend hinzuzufügen.
* Verwende im **Titel das Subprojekt als Präfix**, z. B. `Blockly: wuppi`.
* **Labels und Typen** sind passend zu wählen:
  * `Type: Bug` – für Fehler
  * `Type: Feature` – für neue Features
  * `Type: Task` – für sonstige Aufgaben

## Pull Requests

### Branches
* Feature-Branches im Repository sollten mit dem GitHub-Nutzernamen (z. B. `flamtky/foobarichzerstoerediewelt` für @Flamtky) als Präfix benannt werden.

### Assignee & Titel
* **Sprache**: Titel sind englisch; Beschreibungen sind auf deutsch.
* Ersteller:in **setzt sich selbst als Assignee**.
* Der **PR-Titel** beschreibt präzise den Inhalt des PRs.
* Verwende im Titel das **Subprojekt als Präfix**, z. B. `Blockly: add wuppi fluppi`.
* **Labels oder Projekte** müssen *nicht* manuell gesetzt werden.

### Drafts & Reviews

* Solange ein PR **nicht final** ist, **muss er als Draft markiert werden**.
* Auch **nach einem Review** kann ein PR wieder als Draft gesetzt werden, falls weitere Änderungen nötig sind.
* Ist der PR **bereit zum Mergen** (alle Änderungen abgeschlossen, Tests erfolgreich), dann:
  * Status auf **„Ready for Review“** setzen
  * gezielt **Reviewer anfragen**

## Sonstiges
* Die [Lizenzregeln](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/license_rules.md) für Assets sind einzuhalten.

### IntelliJ: Star-Imports vermeiden

Checkstyle verbietet Star-Imports wie `import java.util.*;`. IntelliJ kann Imports automatisch
so organisieren, dass einzelne Klassen importiert werden.

Empfohlene Einstellungen:

1. Öffne `Settings > Editor > Code Style > Java > Imports`.
2. Setze `Class count to use import with '*'` auf einen hohen Wert, z. B. `999`.
3. Setze `Names count to use static import with '*'` ebenfalls auf einen hohen Wert, z. B. `999`.
4. Aktiviere `Use single class import`, falls die Option nicht aktiv ist.
5. Führe danach `Code > Optimize Imports` aus, um bestehende Imports in einer Datei aufzuräumen.
