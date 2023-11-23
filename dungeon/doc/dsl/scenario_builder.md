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

## Definition von Szenario-Builder-Funktionen

Als Szenario-Builder-Funktion werden alle Funktionen behandelt, denen als Parameter eine Aufgabendefinition
übergeben werden kann. Aktuell stehen hierfür die Datentypen `single_choice_task`, `multiple_choice_task` und
`assign_task` zur Verfügung (vergleiche hierzu die [Dokumentation zu Aufgabendefinitionen](task_definition.md)).
Der Rückgabetyp für Szenario-Builder-Funktionen muss eine Menge aus Entitätsmengen sein, also `entity<><>`.

Ein Beispiel für die Definition einer Szenario-Builder-Funktion könnte wie folgt aussehen:

```
fn build_scenario(single_choice_task task) -> entity<><> {
    // Code...
}
```

### Rückgabewert

Der Rückgabewert einer Szenario-Builder-Funktion ist eine Menge aus Entitätsmengen.
Jede *Entitätsmenge*, die in der *Gesamtmenge* enthalten ist, wird als ein Raum vom Dungeon-System interpretiert.
Die Entitäten die in den jeweiligen *Entitätsmengen* enthalten sind, werden in den entsprechenden Räumen im Spiellevel
platziert.
Alle Räume, die von einer Szenario-Builder-Funktion für **die gleiche Aufgabendefinition** zurückgegeben
werden, werden anschließend vom Dungeon-System miteinander verbunden.

Hierzu ein Beispiel:

```
fn build_scenario(single_choice_task task) -> entity<><> {
    // Deklaration der leeren Gesamtmenge
    var return_set : entity<><>;

    // Deklaration der leeren Entitätsmengen
    var room_1 : entity<>;
    var room_2 : entity<>;

    /*
     * Entitäten in Entätsmengen hinzufügen (Code abstrahiert)
     *
     * - einen Ritter zu room_1 hinzufügen
     * - eine Truhe zu room_2 hinzufügen
     * - Schriftrollen zu room_2 hinzufügen
     */

    // Hinzufügen der Entitätsmengen zur Gesamtmenge
    return_set.add(room_1);
    return_set.add(room_2);

    // Rückgabe der Gesamtmenge als Rückgabewert der Funktion
    return return_set;
}
```

Die Menge `return_set` ist die *Gesamtmenge*, die Mengen `room_1` und `room_2` sind die *Entitätsmengen*.
Der Kommentarblock in der Mitte symbolisiert das Füllen der *Entitätsmengen* mit Entitäten (eine genaue Erklärung
dazu folgt in [Entitäten erstellen](#entitäten-erstellen)).

Aus jeder der *Entitätsmengen* wird im Anschluss an die Ausführung der Szenario-Builder-Funktion ein Raum erstellt und
die enthaltenen Entitäten werden platziert. Die folgenden Bilder zeigen die so erstellten Räume:

![Abbildung: Raum 1](img/scenario_builder_room1.png)

Oben ist der Raum abgebildet, der für `room_1` erstellt wird und nur den Ritter enthält.

![Abbildung: Raum 2](img/scenario_builder_room2.png)

Im Raum, der für `room_2` erstellt wird, sind eine Truhe und Schriftrollen enthalten.

