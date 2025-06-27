# Blockly Dungeon Workshop

## Zielgruppe und Kurzbeschreibung des Inhalts

Der Workshop „Blockly Dungeon“ richtet sich an Schüler\:innen ab der 6. Klasse, die noch keine Informatikkenntnisse mitbringen. Der Fokus liegt auf einem spielerischen und visuell unterstützten Einstieg in das Programmieren mit Blockly.
Ziel ist es, grundlegende Programmierkonzepte, algorithmisches Denken und das Erarbeiten komplexerer Lösungen durch *Divide-and-Conquer*-Techniken kennenzulernen.
Im Zentrum steht dabei das Lösen von Programmier-Rätseln innerhalb einer Dungeon-Spielumgebung.

**Lernform:** Es wird auf exploratives, selbstbestimmtes Arbeiten gesetzt. Die Teilnehmenden erkunden den Dungeon, lösen Rätsel und wählen individuelle Wege zur Lösung. Code-Techniken werden „on demand“ erklärt.

## Benötigte Unterlagen und Software

Damit der Workshop reibungslos funktioniert, sind folgende Software-Komponenten und Vorbereitungen notwendig:

### Software-Voraussetzungen

* **Java:** Die aktuelle vom [Dungeon-Projekt](https://github.com/Dungeon-CampusMinden/Dungeon) benötigte Java-Installation ist erforderlich, um das Dungeon-Spiel starten und Java-Code ausführen zu können.

### Blockly als ausführbare Datei (JAR oder EXE)

* Die Blockly-Umgebung kann entweder als ausführbare `.exe` (für Windows) oder als `.jar` gestartet werden. In beiden Fällen wird automatisch das Blockly-Web-UI geöffnet, sofern ein Browser installiert ist.
* **Start per `.exe`:** Doppelklick genügt, das UI öffnet sich automatisch.
* **Start per `.jar`:** Kann direkt aus VS Code oder über die Kommandozeile gestartet werden. In diesem Fall wird kein Browser benötigt – nur VS Code.

  * In diesem Fall muss das Frontend auch manuell gestartet werden (siehe unten).
* Die Anleitung zur Vorbereitung der ausführbaren Datei ist unter folgendem Link dokumentiert:
  [https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/doc/installation.md](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/doc/installation.md)

### Materialien für Teilnehmende

* **Blockly-Crossword:** Ein Kreuzworträtsel mit IT-Begriffen; dient als Karte für Level 21. [Leere-Version](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/blockly_crossword_empty.pdf);  [Lösung](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/blocky_crossword_solved.pdf).
* **Blockly-Circuit-Diagram:** Zeigt, wie die Schalter in den Leveln verdrahtet sind. [PDF-Version](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/blockly_circuit_diagram.pdf).

### Hinweise
* Level können über das Webinterface freigeschaltet werden
  * `Rechtsklick` im Blockly Browserfenster -> `Untersuchen`
  * Reiter `Application` auswählen
  * Unter `Localstorage` die Anwendung Blockly (z.B. `localhost:5173`) auswählen
  * Dem Key `levelProgress` den gewünschten Value (Levelindex) zuweisen (z.B. 5 um Level 6 freizuschalten).
  * Browserfenster neu laden.
* Endlosschleifen können zum Absturz führen => Neustart notwendig.

## Tag 1

### Begrüßung und Einstieg

Zum Einstieg in den Workshop werden die Teilnehmenden begrüßt und auf ihre Teilnahme an einem laufenden Forschungsprojekt hingewiesen.
In einer Vorstellungsrunde nennen alle ihren Namen, ihr Alter, ihre Klassenstufe, ihre Programmiererfahrung und ihr Lieblingsspiel. Auch das Workshopleitungsteam stellt sich vor.

### Vorerfahrungsumfrage

Eine anonyme Umfrage (per QR-Code) wird durchgeführt, um ein realistisches Bild des Kenntnisstands vor dem Aufbau von Gruppendynamik zu erhalten.

**Achtung:** Nicht jede\:r Teilnehmende hat ein Smartphone. Alternative Möglichkeiten zur Teilnahme sollten bedacht und vorbereitet werden (z. B. via Webbrowser auf den Arbeitsgeräten; Link vorbereiten).

### Einführung

Bevor die SuS an Blockly geschickt werden, sollte eine erste Einführung analog gemacht werden.
Dafür kann ein Grid auf dem Boden gezeichnet oder geklebt werden, und das Roboterverhalten (vgl. [dieses Video](https://www.youtube.com/watch?v=nwMeINjRl6Y)) geübt werden.
Kurz gesagt: Ein\:e Schüler\:in ist der Roboter, und die anderen geben mit ausgewählten Befehlen Anweisungen, um das Ziel zu erreichen – quasi das Dungeon-Spiel „in echt“.
Dadurch wird das Denken in Einzelschritten geübt.

### Blockly-Dungeon

Den Teilnehmenden wird via Beamer das erste Level gezeigt, das Ziel des Spiels und die Bedienoberfläche erklärt.
Gemeinsam wird das erste Level gelöst.

Je nach Gruppendynamik kann der Dungeon nun entweder frei oder geleitet (z. B. durch regelmäßige Abfragen an der Tafel) erkundet werden.
Zielsetzung für Tag 1 ist es, Level 11 zu schaffen – das ist der erste Zwischenboss.
In Level 3 sollte die `for`-Schleife erklärt werden, um Bewegungen effizienter zu gestalten.
Für Level 7, 8 und 11 sind boolesche Ausdrücke nötig (`and`, `or`, `xor`, `not`) sowie deren Notation (vgl. Circuit-Diagramm).

Blockly schaltet automatisch im UI immer neue Blöcke frei. Die SuS sollen daher eigenständig erkunden und üben.
Es gilt: Kaputtmachen geht nicht.

## Tag 2

### Einstieg

Begrüßung und Vorstellung des Tagesprogramms.
Zur wiederholung kann das Kreuzworträtsel gemacht werden. 

### Blockly-Dungeon

Fortsetzung im Java-Dungeon wie an Tag 1.

Folgende Programmier-Skills werden benötigt:

* **ab Level 12:** `if`, `else`
* **ab Level 13:** `while`
* **ab Level 15:** Variablen und Zähler
* **Level 16 bis inkl. Level 19:** Backtracking-Level – hier kann mit Brotkrumen und Kleeblättern einfaches Pathfinding implementiert werden; ggf. überspringen, wenn zu komplex
  * Zum Skippen im Local-Storage (Im Browser Rechtsklick -> Untersuchen -> Reiter Applikation -> Local Storage) den Wert ´levelProgress` auf `20` setzen.
* **Level 20:** Boss-Level 1 *Red Light, Green Light* – Der Boss darf einen nicht sehen; mit `wait`-Befehl zu lösen
* **Level 21:** Hier wird das Kreuzworträtsel als Karte benötigt – das Rätsel lässt sich auf das Level legen; jedes Feld im Kreuzworträtsel kann im Level betreten werden; die Schalter sind die „Lösungsbuchstaben“
* **Level 22:** Finale – Der Boss kopiert die Bewegungen des Helden; Ziel: den Boss in ein Loch locken, um zu gewinnen

### Abschluss

Es folgt eine abschließende Feedback-Umfrage sowie die Vergabe von Zertifikaten zur Bestätigung der erfolgreichen Teilnahme.
