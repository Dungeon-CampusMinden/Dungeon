---
title: "Szenario-Builder-Funktionen"
---

## Was ist das Ziel von Szenario-Buildern?

Szenario-Builder-Funktionen sind dafür zuständig, eine abstrakte [Aufgabendefinition](task_definition.md)
in ein konkretes Spielszenario zu übersetzen. Konkret bedeutet dies, die Spielelemente zu erzeugen, welche
die Aufgabe im Spiel abbilden. Zu diesen Elementen zählen bspw. die Spielfigur, welche die Frage stellt,
und Spielelemente, welche Lösungsmöglichkeiten für eine Aufgabe darstellen.

Für jeden Aufgabentyp ist bereits eine default Szenario-Builder-Funktion definiert, sodass
diese **Funktionen nicht zwingend durch Nutzende definiert werden müssen**.

### Beispiel

Gegeben sei als Beispiel folgende Aufgabendefinition:

```
single_choice_task my_task {
  description: "Bitte suchen Sie die eine richtige Ausage:",
  answers: [
  "Man kann auf den Betriebssystemen Windows, MACOS und Linux in Python programmieren.",
  "Man kann auf externen Datenträgern schnell sortieren.",
  "Schlüsselfelder eignen sich nicht zum Indizieren von Daten."
  ],
  correct_answer_index: 0
}
```

**Eine** mögliche Abbildung dieser Aufgabe in ein Spielszenario wäre die folgende:

- Die Aufgabe wird von einem "Ritter" gestellt
- Die Antwortmöglichkeiten aus `answers` werden als "Schriftrollen" abgebildet, die von
  Spielenden eingesammelt werden müssen
- Die Schriftrolle, welche der korrekten Antwort entspricht, muss in eine "Truhe" gelegt werden

Dies könnte im Spiel wie folgt aussehen:

![Abbildung: Raumansicht für Szenario](img/scenario_builder_room.png)

Die Schriftrollen sind rot umrandet, die Truhe ist grün umrandet und der Ritter ist schwarz umrandet.

Die Schriftrollen können aufgesammelt werden und im Inventar des Spielcharakters inspiziert werden.
Beim Hovern der Maus über eine Schriftrolle wird angezeigt, welche Antwort die Schriftrolle abbildet:

![Abbildung: Schriftrollen im Inventar](img/scenario_builder_item_hover.png)

Anschließend können die Schriftrollen in die Truhe gelegt werden:

![Abbildung: Schriftrollen in Truhe legen](img/scenario_builder_item_in_chest.png)

Abschließend können Spielende mit dem Ritter agieren, um die Aufgabe abzugeben:

![Abbildung: Aufgabe abgeben](img/scenario_builder_finish_task.png)


## Einbettung in das Gesamtsystem

Das Dungeon-System führt zur Übersetzung einer `.dng`-Datei in ein spielbares Level folgende Schritte durch:
1. Suchen nach `dungeon_config`-Definitionen in der übergebenen `.dng`-Datei
2. Laden der Aufgabenabhängigkeiten aus dem `dependency_graph` der `dungeon_config`-Definitionen
3. Für jede im `dependency_graph` referenzierte Aufgabendefinition wird eine **Szenario-Builder-Funktion**
   ausgeführt, die Räume erstellt und Spielelemente darin platziert
4. Die erstellten Räume werden basierend auf den Abhängigkeiten im `dependency_graph` miteinander verbunden,
   sodass ein zusammenhängendes Level entsteht


