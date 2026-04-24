# How to Produs Guide

> **[English Version](./readme.md)**

Willkommen zum **Produs-Guide**!

Produs ist eine Plattform für spielerisches Programmierenlernen. In interaktiven Dungeon-Leveln löst man Rätsel, steuert einen Helden und verbessert dabei Schritt für Schritt die eigenen Programmierkenntnisse. Es gibt drei verschiedene Bausteine, die sich an unterschiedliche Zielgruppen richten:

- **Blockly Dungeon (Web-Version):** Einstieg in die Programmierung mit einer blockbasierten Oberfläche im Browser. Ideal für Anfänger:innen ohne Vorkenntnisse (ab Klasse 6).
- **Java Dungeon (Desktop-Version):** Die gleichen Dungeon-Level, aber die Lösung wird als Java-Code in Visual Studio Code geschrieben. Geeignet für Schüler:innen ab Klasse 8 mit ersten Programmierkenntnissen.
- **Advanced Dungeon:** Ein fortgeschrittenes Dungeon-Projekt, bei dem echte Java-Klassen in einer IDE (z. B. IntelliJ) bearbeitet werden. Geeignet für Schüler:innen ab Klasse 10 mit Java-Vorkenntnissen.

Für ein besseres Verständnis des Workshop-Konzepts gibt es ausführliche Beschreibungen:
- [Blockly Dungeon Workshop](./materials/blockly_workshop.md)
- [Java Dungeon Workshop](./materials/java_workshop.md)
- [Advanced Dungeon Workshop](./materials/advanced_workshop.md)

---

## Inhaltsverzeichnis

1. [Blockly Dungeon (Web-Version)](#1-blockly-dungeon-web-version)
2. [Java Dungeon (Desktop-Version)](#2-java-dungeon-desktop-version)
3. [Advanced Dungeon](#3-advanced-dungeon)
4. [Tipps und häufige Stolperfallen](#4-tipps-und-häufige-stolperfallen)

---

## 1. Blockly Dungeon (Web-Version)

### Was ist das?

Das Blockly Dungeon ist die einsteigerfreundliche Variante. Man programmiert den Helden mit visuellen Blöcken (ähnlich Scratch) direkt im Browser. So können erste Programmiererfahrungen gesammelt werden, ohne eine Programmiersprache kennen zu müssen.

### Was muss installiert werden?

Man braucht nur **Java 21** auf dem Computer. Sonst nichts.

**Java 21 installieren:**

- **Windows / Mac / Linux:** Lade [Java 21] (https://www.oracle.com/de/java/technologies/downloads/#java21) herunter. Wähle dort die passende Version für dein Betriebssystem (Windows, macOS oder Linux) und installiere es. Starte dein Gerät neu.

**Tipp:** Um zu prüfen, ob Java korrekt installiert ist, öffne ein Terminal (Windows: `cmd` oder PowerShell; Mac/Linux: Terminal) und tippe:

```bash
java -version
```

Es sollte eine Ausgabe wie `openjdk version "21.x.x"` erscheinen.

### Blockly Dungeon herunterladen und starten

1. Gehe auf die **Releases-Seite**: [https://github.com/Dungeon-CampusMinden/Dungeon/releases](https://github.com/Dungeon-CampusMinden/Dungeon/releases)
2. Lade die Datei **`Blockly-web.jar`** herunter (unter "Assets" beim neuesten Release).
3. Starte das Dungeon per **Doppelklick** auf die JAR-Datei.
   - Alternativ: Öffne ein Terminal, navigiere zum Download-Ordner und führe aus:
     ```bash
     java -jar Blockly-web.jar
     ```
4. Öffne deinen Browser und gehe zu: [http://localhost:8081/](http://localhost:8081/)
5. Die Blockly-Oberfläche erscheint - du kannst sofort loslegen!

---

## 2. Java Dungeon (Desktop-Version)

### Was ist das?

Im Java Dungeon löst man die gleichen Dungeon-Level wie in der Web-Version, schreibt die Lösung aber als echten Java-Code in Visual Studio Code. Eine spezielle VS-Code-Erweiterung schickt den Code an das laufende Dungeon-Spiel.

### Was muss installiert werden?

1. **Java 21** (siehe [oben](#java-21-installieren))
2. **Visual Studio Code** - herunterladen von: [https://code.visualstudio.com/](https://code.visualstudio.com/)
   - Verfügbar für Windows, Mac und Linux.
3. **Die Blockly-Code-Runner-Erweiterung (.vsix-Datei)**

### Schritt für Schritt: Java Dungeon einrichten

**Schritt 1: JAR-Datei herunterladen**

1. Gehe auf die Releases-Seite: [https://github.com/Dungeon-CampusMinden/Dungeon/releases](https://github.com/Dungeon-CampusMinden/Dungeon/releases)
2. Lade die Datei **`Blockly-desktop.jar`** herunter.
3. Starte das Dungeon per **Doppelklick** auf die JAR-Datei.
   - Alternativ im Terminal:
     ```bash
     java -jar Blockly-desktop.jar
     ```

**Schritt 2: VS-Code-Erweiterung installieren**

1. Lade die **`.vsix`-Datei** von der gleichen Releases-Seite herunter.
2. Öffne **Visual Studio Code**.
3. Klicke in der linken Seitenleiste auf das **Extensions-Symbol** (oder drücke `Ctrl+Shift+X` bzw. `Cmd+Shift+X` auf Mac).
4. Klicke oben rechts in der Extensions-Ansicht auf die **drei Punkte (`...`)** → „More Actions...".
5. Wähle **„Install from VSIX..."**.
6. Navigiere zur heruntergeladenen `.vsix`-Datei und wähle sie aus.
7. VS Code installiert die Erweiterung. Eventuell wirst du aufgefordert, das Fenster neu zu laden - tue das.

**Schritt 3: Code schreiben und ausführen**

1. Erstelle in VS Code eine **neue Datei** mit der Endung `.java` (z. B. `MeinLevel.java`).
2. Schreibe deinen Java-Code, um den Helden zu steuern. Nutze dafür die Cheat Sheets:
   - [Dungeon-Befehle (Cheat Sheet)](./materials/commands_cheat_sheet.md)
   - [Einfache Java-Befehle (Cheat Sheet)](./materials/java-cheat-sheet_simple.md)
3. Klicke oben rechts in VS Code auf **„Run Blockly Code"**, um den Code an das laufende Dungeon zu senden.
4. Im Spielfenster siehst du, wie der Held deine Befehle ausführt!

---

## 3. Advanced Dungeon

### Was ist das?

Das Advanced Dungeon ist ein eigenständiges Dungeon-Projekt für Fortgeschrittene. Hier arbeitet man nicht mehr über eine externe Oberfläche, sondern bearbeitet direkt Java-Klassen in einer richtigen Entwicklungsumgebung (IDE). Das Spiel nutzt Hot-Reloading: Änderungen im Code werden automatisch übernommen, **ohne das Spiel neu starten zu müssen**.

### Was muss installiert werden?

1. **Java** - eine mit dem Repository kompatible Java-Version (mindestens Java 21). Download: [https://jdk.java.net/21/](https://jdk.java.net/21/) oder [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21).
2. **Git** - wird benötigt, um das Projekt herunterzuladen. Download: [https://git-scm.com/downloads](https://git-scm.com/downloads)
   - **Windows:** Lade den Installer herunter und folge den Anweisungen. Die Standardeinstellungen sind in der Regel ausreichend.
   - **Mac:** Git ist häufig schon vorinstalliert. Falls nicht: `xcode-select --install` im Terminal ausführen oder von [https://git-scm.com/downloads/mac](https://git-scm.com/downloads/mac) herunterladen.
   - **Linux:** Über den Paketmanager installieren, z. B. `sudo apt install git` (Ubuntu/Debian) oder `sudo dnf install git` (Fedora).
3. **Eine Java-IDE** - wir empfehlen **IntelliJ IDEA** (Community Edition reicht aus). Download: [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/)
   - Alternativ: Eclipse oder VS Code mit Java-Erweiterungen.

### Schritt für Schritt: Advanced Dungeon einrichten

**Schritt 1: Repository klonen**

Öffne ein Terminal und führe aus:

```bash
git clone https://github.com/Dungeon-CampusMinden/Dungeon.git
```

Das lädt das gesamte Projekt herunter.

**Schritt 2: Projekt in der IDE öffnen**

Öffne den heruntergeladenen Ordner `Dungeon` in IntelliJ IDEA (oder deiner bevorzugten IDE). IntelliJ erkennt automatisch, dass es sich um ein Gradle-Projekt handelt, und lädt die Abhängigkeiten herunter. Das kann beim ersten Mal einige Minuten dauern.

**Schritt 3: Advanced Dungeon starten**

Öffne ein Terminal im Projektordner und führe aus:

- **Windows:**
  ```bash
  .\gradlew.bat runPortal
  ```
- **Mac / Linux:**
  ```bash
  ./gradlew runPortal
  ```

Das Spiel startet und zeigt das erste Rätsel.

### Wo wird gearbeitet? - Der Arbeitsbereich im Detail

Die Schüler:innen arbeiten ausschließlich im Package **`riddles`** innerhalb des Projekts. Der Pfad im Repository ist:

```
advancedDungeon/src/portal/riddles/
```

In diesem Ordner befinden sich mehrere Java-Dateien, die jeweils einem Rätsel im Dungeon entsprechen. Jede Datei enthält eine oder mehrere Methoden, die eine `UnsupportedOperationException` werfen - das ist der Platzhalter, den die Schüler:innen durch eigenen Code ersetzen müssen.

### Was soll verändert werden?

**Das Prinzip ist immer gleich:** Jede Methode, die eine `UnsupportedOperationException` wirft, muss mit einer funktionierenden Implementierung gefüllt werden.

Ein Beispiel - so sieht eine Methode **vor** der Bearbeitung aus:

```java
@Override
public void activate(Entity emitter) {
    throw new UnsupportedOperationException("Implementiere diese Methode!");
}
```

Und so könnte sie **nach** der Bearbeitung aussehen (Beispiel `MyBridgeSwitch`):

```java
@Override
public void activate(Entity emitter) {
    LightBridgeFactory.activate(emitter);
}
```

Die Schüler:innen müssen also herausfinden, welche Methode aufgerufen werden muss, welche Parameter nötig sind und wie die Logik des jeweiligen Rätsels funktioniert. Dabei helfen die vorhandenen Hilfsklassen im Package `abstraction` sowie die Javadoc-Kommentare im Code.

### Übersicht der Rätsel-Dateien

Hier eine Übersicht aller Dateien im `riddles`-Package und was dort implementiert werden muss:

| Datei | Aufgabe |
|---|---|
| `MyPlayerController` | Tastatureingaben verarbeiten (W/A/S/D für Bewegung, Q/F für Fähigkeiten, E für Interaktion) |
| `MyCalculations` | Portal-Austrittsposition berechnen, Endpunkte von Lichtwänden/Brücken bestimmen, Traktorstrahl-Kräfte berechnen |
| `MyCube` | Einen Portal-Würfel mit bestimmten Eigenschaften (Masse, Textur, Aufhebbarkeit) erzeugen |
| `MySphere` | Eine Portal-Kugel mit bestimmten Eigenschaften erzeugen |
| `MyPortalConfig` | Portal-Konfiguration festlegen (Abklingzeit, Geschwindigkeit, Reichweite, Zielposition) |
| `MyBridgeSwitch` | Lichtbrücke ein- und ausschalten |
| `MyLightWallSwitch` | Lichtwand ein- und ausschalten |
| `MyLaserGridSwitch` | Lasergitter aktivieren und deaktivieren |
| `MyTractorBeamLever` | Schubrichtung des Traktorstrahls umkehren |
| `MyEnergyPelletCatcherBehavior` | Verhalten beim Abfangen eines Energiegeschosses definieren |

### Beispiel: Ein Advanced-Dungeon-Rätsel lösen

Nehmen wir die Datei **`MyPlayerController`** als Beispiel. Hier soll die Tastatursteuerung für den Helden implementiert werden (W/A/S/D zum Bewegen).

**Schritt 1:** Öffne die Datei `MyPlayerController.java` im `riddles`-Package. Du findest dort die Methode `processKey(String key)`, die eine `UnsupportedOperationException` wirft.

**Schritt 2:** Überlege: Was soll passieren, wenn eine Taste gedrückt wird? Bei "W" soll sich der Held nach oben bewegen, bei "S" nach unten, bei "A" nach links und bei "D" nach rechts. Dazu kannst du die Methoden `hero.setXSpeed()` und `hero.setYSpeed()` nutzen.

**Schritt 3:** Ersetze den Platzhalter-Code:

```java
protected void processKey(String key) {
    if (key.equals("W")) move(0, 5);
    if (key.equals("S")) move(0, -5);
    if (key.equals("A")) move(-5, 0);
    if (key.equals("D")) move(5, 0);
}

private void move(int x, int y) {
    if (x != 0) hero.setXSpeed(x);
    if (y != 0) hero.setYSpeed(y);
}
```

**Schritt 4:** Speichere die Datei. Dank Hot-Reloading wird die Änderung automatisch ins laufende Spiel übernommen. Drücke im Spiel W/A/S/D und prüfe, ob sich der Held bewegt. Teste auch Q, F und E für die weiteren Aktionen.

---

## 4. Tipps und häufige Stolperfallen

### Allgemein

- **Das Spiel reagiert nicht auf Eingaben?** Manchmal muss man erst **aus dem Spielfenster heraus- und wieder hineinklicken**, damit der Fokus korrekt gesetzt wird. Klicke einfach einmal auf das Spielfenster, um den Fokus wiederherzustellen.
- **Das Spiel muss nicht jedes Mal neu gestartet werden!** Wenn du deinen Code änderst und erneut ausführst, wird der aktuelle Zustand aktualisiert. Du musst das Spiel nicht schließen und neu öffnen. Das gilt sowohl für die Web/Desktop-Version (Code einfach erneut mit „Run Blockly Code" senden) als auch für das Advanced Dungeon (Hot-Reloading).

### Blockly / Java Dungeon

- **Level wechseln:** Im Java-Code kannst du das Level direkt laden mit `loadLevel(INDEX)` oder `loadNextLevel()`. Du musst also nicht immer alle vorherigen Level nochmal lösen.
- **Browser-Cache:** Falls die Blockly-Web-Oberfläche seltsam aussieht oder nicht reagiert, versuche einen Hard-Reload im Browser (`Ctrl+Shift+R` bzw. `Cmd+Shift+R`).

### Advanced Dungeon

- **Hot-Reloading nutzen:** Code-Änderungen werden automatisch übernommen. Speichere die Datei und die Änderung greift sofort im laufenden Spiel - kein Neustart nötig.
- **Nur im `riddles`-Package arbeiten:** Die Schüler:innen sollten ausschließlich Dateien im `riddles`-Package verändern. Der restliche Code ist das Framework und sollte nicht angefasst werden.
- **`UnsupportedOperationException` ist der Wegweiser:** Suche in den Dateien nach `throw new UnsupportedOperationException(...)` - genau an diesen Stellen muss eigener Code geschrieben werden.
- **Hilfsklassen nutzen:** Das Package `abstraction` und die Klassen `Tools`, `LightBridgeFactory`, `LightWallFactory`, `TractorBeamFactory` usw. bieten vorgefertigte Methoden, die bei der Lösung helfen. Nutze die Autovervollständigung der IDE, um zu sehen, welche Methoden verfügbar sind.
- **Gradle starten, nicht direkt die Main-Klasse:** Das Spiel muss zwingend über Gradle gestartet werden (`./gradlew runPortal`), damit das Hot-Reloading funktioniert. Ein direkter Start über die IDE-Run-Konfiguration funktioniert nicht korrekt.

---

## Materialien

- [Dungeon-Befehle (Cheat Sheet)](./materials/commands_cheat_sheet.md)
- [Einfache Java-Befehle (Cheat Sheet)](./materials/java-cheat-sheet_simple.md)
- [Ausführliches Java-Cheat-Sheet](./materials/java-cheat-sheet.md)
- [Blockly-Level-Lösungen](./solution/blockly)
- [Advanced-Dungeon-Lösungen](./solution/advancedDungeon)

---

*Produs ist Teil des Projekts „Produs", gefördert durch: EFRE-20300105, [Pakt für Informatik 2.0](https://www.efre.nrw/einfach-machen/foerderung-finden/pakt-fuer-informatik-20), [EFRE/JTF NRW 2021-27](https://www.efre.nrw/)*
