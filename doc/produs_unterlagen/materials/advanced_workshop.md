# Advanced Workshop

## Zielgruppe und Kurzbeschreibung des Inhalts

Der Workshop *Produs: Advanced* richtet sich an Schüler:innen ab der 10. Klasse mit Vorerfahrung in Java. Als Mindestvoraussetzung gelten grundlegende Kenntnisse in Kontrollstrukturen (z. B. `if`, `while`, `for`) und Datentypen. Objektorientierung und Methodenkenntnis sind hilfreich, aber nicht zwingend notwendig – die Teilnehmenden dieses Durchgangs konnten jedoch durchgehend mit Funktionen und OOP umgehen.

Ziel des Workshops ist es, den Teilnehmenden in einem begleiteten, aber selbstbestimmten Rahmen das Programmieren zu ermöglichen. Im Fokus steht das entdeckende Lernen: Die Teilnehmenden sollen eigene Ideen entwickeln, umsetzen und dabei gezielt an die Grenzen ihres bisherigen Wissens stoßen. Sobald mit dem aktuellen Skillset nicht mehr weitergekommen wird, werden gezielt neue Inhalte vermittelt – praxisnah und mit direktem Bezug zum Projekt. So ergibt sich eine motivierende Lernkurve, bei der der Wissenserwerb unmittelbar aus der Problemstellung heraus erfolgt.

Inhaltlich basiert der Workshop auf dem *Advanced Dungeon*-Projekt. Am ersten Tag stehen spielerische Programmierrätsel im Vordergrund, die im Spielkontext gelöst werden. Am zweiten Tag wird ein Pathfinding Visualizer genutzt, um eigene Pathfinding-KIs zu entwickeln. Dabei kommen klassische Algorithmen wie Tiefensuche und Breitensuche zur Anwendung.

## Benötigte Unterlagen und Software

### Software-Voraussetzungen

* **Java**
  Eine funktionierende Java-Installation ist zwingend notwendig. Die genaue Version sollte mit dem Stand des Dungeon-Repositories kompatibel sein.

* **IDE**
  Eine Java-fähige Entwicklungsumgebung wie IntelliJ IDEA, Eclipse oder VS Code wird benötigt. Aus didaktischer Sicht empfiehlt sich IntelliJ, da es die Projektstruktur gut visualisiert.

* **Gradle**
  Das Dungeon-Projekt muss zwingend über Gradle gestartet werden, damit das Hot-Reloading des Schülercodes korrekt funktioniert. Startbefehl:

  ```bash
  ./gradlew runAdvancedDungeon
  ```
### Tag 1: Advanced Dungeon

Arbeitsbereich: [`blockly/src/produsAdvanced`](https://github.com/Dungeon-CampusMinden/Dungeon/tree/master/blockly/src/produsAdvanced)

* **Programmierbereich:**
  Die Schüler:innen schreiben ihre Lösungen im Package `riddle`.

* **Hilfsmethoden:**
  Optional können unterstützende Methoden aus dem Package `abstraction` verwendet werden.

* **Lernform:**
  Es wird auf exploratives, selbstbestimmtes Arbeiten gesetzt. Die Teilnehmenden erkunden den Dungeon, lösen Rätsel und wählen individuelle Wege zur Lösung.

### Tag 2: Pathfinding-Projekt

Pfad: [`blockly/src/utils/pathfinding`](https://github.com/Dungeon-CampusMinden/Dungeon/tree/master/blockly/src/utils/pathfinding)

* **Eigener Code:**
  Umsetzung erfolgt in [`SusPathFinding.java`](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/src/utils/pathfinding/SusPathFinding.java)

* **Referenzimplementierung:**
  [`PathfindingLogic.java`](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/src/utils/pathfinding/PathfindingLogic.java) dient als Vorlage für verschiedene Pfadsuchalgorithmen.

* **Visualisierung:**
  Die grafische Darstellung erfolgt automatisch durch das Dungeon-System.

* **Gradle-Targets:**

  * `runPathfinder`: Startet interaktive Ansicht mit Tastenauswahl (`J`, `K`, `L`, Start mit `B`)
  * `runPathFindingComparison`: Zeigt visuellen Vergleich zwischen Tiefen- und Breitensuche

### Materialien für Teilnehmende

* **Java-Cheat-Sheet:**
  [Java Cheat Sheet](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/java-cheat-sheet.md)

## Tag 1

### Begrüßung und Einstieg

Zum Einstieg in den Workshop werden die Teilnehmenden begrüßt und auf ihre Teilnahme an einem laufenden Forschungsprojekt hingewiesen.
In einer Vorstellungsrunde nennen alle Name, Alter, Klassenstufe, Programmiererfahrung und ihr Lieblingsspiel. Auch das Workshopleitungsteam stellt sich vor.

### Vorerfahrungsumfrage

Eine anonyme Umfrage (per QR-Code) wird durchgeführt, um ein realistisches Bild des Kenntnisstands vor dem Aufbau von Gruppendynamik zu erhalten.

### Status-Check

Mit Hilfe des [Java Cheat Sheets](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/java-cheat-sheet.md) wird gemeinsam ermittelt, welche Konzepte bekannt sind. Im vorliegenden Durchlauf waren Kontrollstrukturen, Datentypen, Methoden und OOP bereits vertraut.

### Einstieg ins Projekt

* Projektstart mit:

  ```bash
  ./gradlew runAdvancedDungeon
  ```

* Hot-Reloading ist aktiviert – kein Neustart bei Codeänderung erforderlich.

* Schüler:innen arbeiten im Package `riddle`, optional unterstützt durch Methoden aus `abstraction`.

* Das Lösen der Aufgaben erfolgt individuell und selbstgesteuert, begleitet durch situative Unterstützung.

* Eigenständiges Erkunden außerhalb der Hauptaufgaben ist möglich und erwünscht.

### Bedarfsorientierte Wissensvermittlung

Neue Konzepte werden bei Bedarf eingeführt, etwa zu Kontrollstrukturen, Sortierlogik oder methodischem Vorgehen – stets eingebettet in den aktuellen Kontext.

### Exkurs: Studium / Unternehmensbeitrag

In der Pause wird ein kurzer Überblick über Studienmöglichkeiten im Bereich Informatik gegeben, verbunden mit Praxisbeispielen.

Abschließend folgt ein unterhaltsamer Input:
[**Bubble-sort mit ungarischem Volkstanz**](https://www.youtube.com/watch?v=Iv3vgjM8Pv4)

## Tag 2

### Einstieg

Begrüßung und Vorstellung des Tagesprogramms.

### Status-Check

Einstiegsfragen zur Wissensabfrage:

* Was ist ein **Graph**, **Knoten**, **Kante**?
* Bekanntheit von **ArrayLists**?
* Vorwissen zu **Pfadsuchen**?

*Falls wenig Vorwissen vorhanden ist, erfolgt eine Einführung.*

### Theorie: Tiefensuche & Breitensuche

Anhand eines kurzen Spiels (z. B. Tic-Tac-Toe) wird die Denkweise beim Suchen im Spielverlauf als Tiefensuche dargestellt.
Begriffe wie **gerichteter Graph**, **Stack**, **Queue** und **ArrayList** werden aus dem Kontext abgeleitet und erklärt.

### Unternehmensbeitrag

Ein Unternehmensvertreter gibt Einblicke in Berufsfelder oder Studiengänge, um die Praxisrelevanz des Themas zu verdeutlichen.

### Pathfinding-Implementierung

Die Teilnehmenden setzen eigene Algorithmen in der `SusPathFinding.java` um, begleitet durch Hilfestellung und Austausch zu Lösungsstrategien.

### GPT-Wizzard

Abschließend erhalten die Teilnehmenden Zugriff auf den [GPT-Wizzard](https://gandalf.lakera.ai/do-not-tell), um eigene Lösungen zu vergleichen, Varianten zu testen und kreativ zu experimentieren.

### Abschluss

Es folgt eine abschließende Feedback-Umfrage sowie die Vergabe von Zertifikaten zur Bestätigung der erfolgreichen Teilnahme.

