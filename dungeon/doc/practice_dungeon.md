# Practice Dungeon

## 1 Einleitung

Das folgende Konzept beschreibt die Ideen und Elemente für ein 2D Dungeon-Spiel mit dem
Titel ”Practice Dungeon”. In diesem Spiel erkundet der Spieler verschiedene Level,
löst Rätsel, besiegt Monster und sammelt Schätze. Hier sind die Details für jedes Level.

<div style="text-align: center;">
    <img src="dsl/img/concept_introduction.png" width="600" />
</div>

## 2 Level 1: ”Der Vergessene Wald”

- Umgebung:
    - Ein geheimnisvoller Wald mit dichtem Un-
      terholz und moosbedecktem Boden.
    - Der Wald besteht aus drei Räumen.
- Ziele und Aufgaben:
    - Der Spieler muss den versteckten Schlüssel
      finden, um die Tür zuöffnen. Der Schlüssel
      befindet sich in einer der Truhen.
    - Auf dem Weg begegnet er wilde Imps und
      Goblins.
    - In den Truhen befinden sich zusätzlich
      Heiltränke, damit sich der Spieler heilen
      kann.
- Rätsel im zweiten Raum:
    - Stelle dem Spieler Codeausschnitte oder
      Fehlermeldungen vor.
    - Der Spieler muss den Fehler identifizieren
      und korrigieren, um voranzukommen.
- Bosskampf ”OgreX”:
    - OgreX hat verschiedene Angriffsmuster, die
      aus regulären Ausdrücken bestehen. Der
      Spieler muss daraufhin einen zum regulären
      Ausdruck passenden String eingeben. Hat
      der Spieler die passenden Strings eingegeben,
      ist der Boss erledigt und die nächste Ebene
     öffnet sich.

<div style="text-align: center;">
    <img src="dsl/img/concept_level_structure.png" width="280" />
</div>

## 3 Level 2: ”Tempel der verlorenen Geheimnisse”

- Umgebung:
    - Ein uralter Tempel mit geheimnisvollen In-
      schriften.
    - Der Tempel besteht aus drei Räumen.
- Ziele und Aufgaben:
    - Der Spieler muss den versteckten Saphir
      finden, der von einem bestimmten Monster
      fallengelassen wird. Sobald der Saphir im
      Besitz des Spielers ist, öffnet sich die Tür.
    - Ork-Krieger und Ork-Schamanen wollen es
      verhindern.
    - Zusätzlich befinden sich Truhen im Raum,
      die Ausrüstung beinhalten.
- Rätsel im zweiten Raum:
    - Eine Schriftrolle befindet sich auf dem Bo-
      den, mit der Aufgabe, dass der Spieler
      ein Monster mit verschiedenen Eigenschaften
      (z.B. Gesundheit, Angriffskraft) erstellen
      muss, um weiterzukommen.
- Bosskampf ”Skelettdoktor der Code Refac-
  torer”:
  - Der Skelettdoktor ist verzweifelt, weil der
  Quellcode miserabel, unvollständig und in-
  effizient ist und bittet den Spieler um Hilfe.
  Ziel ist es, den Code zu optimieren, Fehler
  zu beheben und die Lesbarkeit zu verbessern.
  Falls die Lösung zufriedenstellend ist, öffnet
  sich die Tür zur nächsten Ebene.

<div style="text-align: center;">
    <img src="dsl/img/concept_level_structure.png" width="280" />
</div>

## 4 Level 3: ”Kerker des Grauens”

- Umgebung:
    - Ein düsterer, verfallener Kerker mit steiner-
      nen Wänden und alten Türen.
    - Der Kerker besteht aus drei Räumen.
- Ziele und Aufgaben:
    - Der Spieler muss Schriftrollen mit Tieren
      der richtigen Kategorie (Reptil usw.)
      den Truhen zuordnen. Erst wenn alle
      Schriftrollen korrekt zugeordnet sind, öffnet
      sich die nächste Tür.
    - Unterwegs trifft er auf Kürbisköpfe und
      Zombies, die ihn angreifen.
- Rätsel im zweiten Raum:
    - Ein friedlicher Imp will, dass der Spieler in
      einem vorgegeben Code Fehler findet und ko-
      rrigiert.
    - Dies kann dazu beitragen, das Verständnis
      für häufige Fehler und Debugging-Techniken
      zu vertiefen.
- Bosskampf ”Zauberer von Patternson”:
    - Der ”Zauber von Patternson” verlangt vom
      Spieler, dass er verschiedene Design Patterns
      (z.B. Singleton, Factory, Observer) ins Spiel
      implementieren muss, damit er in die nächste
      Ebene kommt.
    - Ziel: Der Spieler kann diese Muster erkennen
      und anwenden.

<div style="text-align: center;">
    <img src="dsl/img/concept_level_structure.png" width="280" />
</div>

## 5 Level 4: ”Die Vulkanhöhle”

- Umgebung:
    - Eine unterirdische Höhle mit fließender Lava
      und glühenden Steinen.
    - Die Vulkanhöhle besteht aus drei Räumen.
- Ziele und Aufgaben:
    - Der Spieler muss alle Monster erledigen um
      weiterzukommen.
    - Folgende Monster sind anzutreffen: ”Imps”
      und ”kleiner Dämon.”
    - In einer Truhe befindet sich das “Eisschw-
      ert”, mit dem man mehr Schaden anrichten
      kann.
- Rätsel im zweiten Raum:
    - Der Spieler trifft auf einen mysteriösen NPC.
    - Der NPC stellt Fragen zu Lambda-
      Ausdrücken und Funktionsinterfaces.
    - Der Spieler muss die Fragen richtig beant-
      worten, um weiterzukommen.
- Bosskampf ”Der Artefakt Dämon”:
    - Der ”Artefakt Dämon” ist friedlich und wird
      den Spieler nicht angreifen.
    - Der Spieler erhält eine Beschreibung des er-
      warteten Verhaltens und der Schnittstelle,
      die das Artefakt implementieren muss.
    - Der “Artefakt Dämon” erteilt dem Spieler
      die Aufgabe, eine Java-Klasse zu schreiben,
      die dieses Verhalten implementiert und die
      Schnittstelle implementiert.
    - Die Klasse muss auf Code-Ebene in das Spiel
      integriert werden, um die Truhe zuöffnen.
    - Hat der Spieler die Truhe aufgekriegt und
      das Artefakt erhalten, kommt er in die
      nächste Ebene.

<div style="text-align: center;">
    <img src="dsl/img/concept_level_structure.png" width="280" />
</div>

## 6 Level 5: ”Spezial-Welt Bielefeld”

- Umgebung:
    - Die Stadt Bielefeld mit ihren Se-
      henswürdigkeiten.
- Ziele und Aufgaben:
    - Der Spieler muss verhindern, dass der Zus-
      tand der Gebäude nicht unter 50 % fällt.
    - Der Spieler muss alle Monster erledigen um
      weiterzukommen.
- Rätsel ”Java-Quiz”:
    - Der Spieler trifft auf einen weisen alten Za-
      uberer.
    - Der Zauberer stellt ihm Fragen zu Java-
      Konzepten und -Syntax.
    - Der Spieler muss sein Java-Wissen unter Be-
      weis stellen, um den Zauberer zu beein-
      drucken.
- Bosskampf ”Mr. Unbekannt”:
    - Der Boss stellt ihm eine Reihe von Fragen zu
      Java-Programmierung.
    - Der Spieler muss die Fragen richtig beant-
      worten, um den Boss zu besiegen.

## 7 Ziele

Die Studenten sollen spielerisch lernen, indem sie die im Spiel eingebauten fachlichen Auf-
gaben lösen. Dabei werden gezielt Themen aus den Vorlesungen eingesetzt, um den Fortschritt
im Spiel zu ermöglichen. Gleichzeitig ist es wichtig, dass der Spaß nicht zu kurz kommt und
die Level herausfordernd, aber machbar sind.


