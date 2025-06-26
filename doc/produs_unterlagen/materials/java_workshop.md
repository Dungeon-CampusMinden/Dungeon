# Java Dungeon Workshop

## Zielgruppe und Kurzbeschreibung des Inhalts

Der Workshop „Java Dungeon“ richtet sich an Schüler\:innen ab der 8.–10. Klasse, die grundlegende Informatikkenntnisse mitbringen, aber noch keine oder nur wenig Java-Erfahrung haben. Der Fokus liegt auf einem spielerischen und visuell unterstützten Einstieg in das Programmieren mit Java. Die Teilnehmenden nutzen eine spezielle VS-Code-Extension, um mit Java-Code zu interagieren und ihn im Blockly-Dungeon direkt auszuprobieren.

Ziel ist es, grundlegende Programmierkonzepte wie Variablen, Kontrollstrukturen und Methoden zu verstehen und anzuwenden. Im Zentrum steht dabei das Lösen von Programmier-Rätseln innerhalb einer Dungeon-Spielumgebung.

**Lernform:** Es wird auf exploratives, selbstbestimmtes Arbeiten gesetzt. Die Teilnehmenden erkunden den Dungeon, lösen Rätsel und wählen individuelle Wege zur Lösung. Code-Techniken werden "on demand" erklärt.

## Benötigte Unterlagen und Software

Damit der Workshop reibungslos funktioniert, sind folgende Software-Komponenten und Vorbereitungen notwendig:

### Software-Voraussetzungen

* **Java:** Die aktuelle vom [Dungeon-Projekt](https://github.com/Dungeon-CampusMinden/Dungeon) benötigte Java-Installation ist erforderlich, um das Dungeon-Spiel starten und Java-Code ausführen zu können.
* **Visual Studio Code (VS Code):** Die Teilnehmenden schreiben ihren Java-Code in [VS Code](https://code.visualstudio.com/). Es wird empfohlen, die Software vor dem Workshop auf allen Geräten zu installieren.
* **VS-Code-Extension:** Zusätzlich muss eine spezielle VS-Code-Erweiterung installiert werden, um den Blockly-Dungeon mit Java-Code zu verbinden. Die Anleitung zur Installation befindet sich unter:
  [https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/doc/install-extension.md](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/doc/install-extension.md)

### Blockly als ausführbare Datei (JAR oder EXE)

* Die Blockly-Umgebung kann entweder als ausführbare `.exe` (für Windows) oder als `.jar` gestartet werden. In beiden Fällen wird automatisch das Blockly-Web-UI geöffnet, sofern ein Browser installiert ist.
* **Start per `.exe`:** Doppelklick genügt, das UI öffnet sich automatisch.
* **Start per `.jar`:** Kann direkt aus VS Code oder über die Kommandozeile gestartet werden. In diesem Fall wird kein Browser benötigt – nur VS Code.
* Die Anleitung zur Vorbereitung der ausführbaren Datei ist unter folgendem Link dokumentiert:
  [https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/doc/installation.md](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/blockly/doc/installation.md)

### Materialien für Teilnehmende

* **Java-Cheat-Sheet (simple):** Dieses Handout enthält alle grundlegenden Java-Konzepte, die im Workshop vermittelt werden. [Markdown Version](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/java-cheat-sheet_simple.md).
* **VS-Code-Befehls-Cheat-Sheet:** Dieses Cheat-Sheet listet alle Blockly-spezifischen VS-Code-Kommandos auf, mit denen die Teilnehmenden ihre Java-Lösungen testen und visualisieren können. [Markdown Version](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/commandscheat_sheet.md).
* **Blockly-Crossword:** Ein Kreuzworträtsel mit IT-Begriffen; dient als Karte für Level 21. [Leere-Version](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/blockly_crossword_empty.pdf);  [Lösung](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/blocky_crossword_solved.pdf).
* **Blockly-Circuit-Diagram:** Zeigt, wie die Schalter in den Leveln verdrahtet sind. [PDF-Version](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/blockly_circuit_diagram.pdf).

### Hinweise
* mit `loadLevel(INDEX)` oder `loadNextLevel()` kann im Code ein bestimmes Level geladen werden.
* Endlosschleifen können zum Absturz führen => Neustart notwendig.

## Tag 1

### Begrüßung und Einstieg

Zum Einstieg in den Workshop werden die Teilnehmenden begrüßt und auf ihre Teilnahme an einem laufenden Forschungsprojekt hingewiesen.
In einer Vorstellungsrunde nennen alle ihren Namen, ihr Alter, ihre Klassenstufe, ihre Programmiererfahrung und ihr Lieblingsspiel. Auch das Workshopleitungsteam stellt sich vor.

### Vorerfahrungsumfrage

Eine anonyme Umfrage (per QR-Code) wird durchgeführt, um ein realistisches Bild des Kenntnisstands vor dem Aufbau von Gruppendynamik zu erhalten.

**Achtung:** Nicht jeder Teilnehmende hat ein Smartphone. Alternative Möglichkeiten zur Teilnahme sollten bedacht und vorbereitet werden (z. B. via Webbrowser auf den Arbeitsgeräten; Link vorbereiten).

### Status-Check

Mithilfe des [Java-Cheat-Sheets](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/doc/produs_unterlagen/materials/java-cheat-sheet.md) wird gemeinsam ermittelt, welche Konzepte bekannt sind.

### Java-Dungeon

Den Teilnehmenden wird via Beamer das erste Level gezeigt, das Ziel des Spiels und die Bedienoberfläche erklärt.
Es wird auf die Autovervollständigung hingewiesen (`hero.`) sowie auf die Cheat-Sheets (jetzt auch verteilen).
Gemeinsam wird das erste Level gelöst.

Je nach Erfahrung (also ob bereits eigenständig einfacher Java-Code geschrieben wurde oder nicht) werden die Grundlagen des Codes erklärt.
**Hinweis:** Viele Kinder kannten grundlegende Tastenkürzel nicht (Strg+C, Strg+V, Strg+A); diese können hier auch Schritt für Schritt vermittelt werden.

Je nach Gruppendynamik kann der Dungeon nun entweder frei oder geleitet (z. B. durch regelmäßiges Abfragen an der Tafel) erkundet werden.
Zielsetzung für Tag 1 ist, Level 11 zu schaffen – das ist der erste Zwischenboss.
In Level 3 sollte die `for`-Schleife erklärt werden, um Bewegungen effizienter zu gestalten.
Für Level 7, 8 und 11 sind boolesche Ausdrücke nötig (`and`, `or`, `xor`, `not`) sowie deren Notation (vgl. Circuit Diagram).

## Tag 2

### Einstieg

Begrüßung und Vorstellung des Tagesprogramms.

### Java-Dungeon

Fortsetzung im Java Dungeon wie an Tag 1.

Folgende Programmier-Skills werden benötigt:

* **ab Level 12:** `if`, `else`
* **ab Level 13:** `while`
* **ab Level 15:** Variablen und Zähler
* **Level 16 bis inkl. Level 19:** Backtracking-Level; hier kann mit Brotkrumen und Kleeblättern einfaches Pathfinding implementiert werden; ggf. überspringen, wenn zu komplex
* **Level 20:** Boss-Level 1: *Red Light, Green Light* – Der Boss darf einen nicht sehen; mit `wait`-Befehl zu lösen
* **Level 21:** Hier wird das Kreuzworträtsel als Karte benötigt – das Rätsel lässt sich auf das Level legen, jedes Feld im Kreuzworträtsel kann im Level betreten werden; die Schalter sind die "Lösungsbuchstaben"
* **Level 22:** Finale – Der Boss kopiert die Bewegungen des Helden; Ziel: den Boss in ein Loch locken, um zu gewinnen

### Abschluss

Es folgt eine abschließende Feedback-Umfrage sowie die Vergabe von Zertifikaten zur Bestätigung der erfolgreichen Teilnahme.

