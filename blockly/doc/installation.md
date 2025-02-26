---
title: "Blockly: How to install"
---

# Installation

## Voraussetzungen

Bevor mit der Installation begonnen werden kann, müssen folgende Voraussetzungen erfüllt sein:

* **JavaScript Runtime:**
  1. **Node.js und npm:** Blockly benötigt Node.js und npm, um die Applikation zu installieren und zu starten. Node.js und npm können von der [offiziellen Website](https://nodejs.org/en/) heruntergeladen werden. Die Installation von Node.js beinhaltet auch die Installation von npm.

    **oder**

  2. **Deno:** Deno kann als Alternative zu Node.js und npm verwendet werden. Weitere Informationen finden Sie unter [https://deno.land](https://deno.land). Falls Sie Deno verwenden möchten, ersetzen Sie in den folgenden Schritten `npm` durch `deno`.
  Deno wird benötigt, um eine ausführbare Datei (.exe) zu erstellen.


* **Git:** Blockly benötigt Git, um das Repository zu klonen. Git kann von der [offiziellen Website](https://git-scm.com/downloads) heruntergeladen werden.

## Schritt 1: Repository klonen

Öffnen Sie Ihr bevorzugtes Terminal und navigieren Sie zu dem Verzeichnis, in dem Sie "Blocky-Dungeon" speichern möchten. Führen Sie den folgenden Befehl aus, um das Repository zu klonen:

```bash
git clone https://github.com/Dungeon-CampusMinden/Dungeon.git
```

## Schritt 2: Wechsel in den Projektordner

Navigieren Sie in das Verzeichnis, in dem das Repository geklont wurde:

```bash
cd Dungeon/blockly/frontend
```

## Schritt 3: Installation der Abhängigkeiten

Verwenden Sie npm, um die benötigten Abhängigkeiten für "Blocky" zu installieren:

```bash
npm install
```

## Schritt 4: Starten der Applikation

Um die Applikation zu starten, wechseln Sie in das Hauptverzeichnis des Projekts:
```bash
cd ../..
```
Führen Sie dann den folgenden Befehl aus, um die Java-Anwendung zu starten:
```bash
gradlew runBlockly
```

Starten Sie dann den Vite.js-Entwicklungsserver, um "Blocky" lokal auszuführen:

Dafür müssen Sie in das `frontend`-Verzeichnis wechseln:
```bash
cd blockly/frontend
```
Und führen Sie den folgenden Befehl aus:
```bash
npm run dev
```

Der Entwicklungsserver wird gestartet und die Anwendung sollte nun unter der Adresse `http://localhost:5173` in Ihrem Webbrowser erreichbar sein.

## Schritt 5 (Optional): Applikation bauen

Um die Applikation zu bauen, führen Sie den folgenden Befehl aus:

```bash
npm run build
```

Dieser Schritt kompiliert und optimiert die Anwendung für die Bereitstellung. Die resultierenden Dateien werden im `dist`-Verzeichnis gespeichert.

## Erstellen einer Executable

Für die Erstellung einer ausführbaren Datei ist **[Deno](https://deno.land) erforderlich**.

### Executable erstellen

Bevor die Executable erstellt werden kann, muss Schritt 5 (Optional) durchgeführt werden, um die Applikation zu bauen. Dannach muss in das `webserver`-Verzeichnis im Blockly-Subprojekt gewechselt werden:

```bash
cd webserver
```

Nun kann die Executable erstellt werden. Hier sind zwei Beispiele für verschiedene Plattformen:

1. **Windows (x86_64):**
    ```bash
    deno compile --target x86_64-pc-windows-msvc --allow-net --allow-read --allow-run --no-npm --output blockly_x86_64.exe --icon ./content/favicon.ico webserver.ts
    ```
    > Bitte beachten Sie, dass in der aktuellen Deno-Version das Icon nicht korrekt übernommen wird und daher in der .exe nicht erscheint.

2. **Linux (x86_64):**
    ```bash
    deno compile --target x86_64-unknown-linux-gnu --allow-net --allow-read --allow-run --no-npm --output blockly_x86_64.bin webserver.ts
    ```

Weitere unterstützte Zielplattformen finden Sie in der [Deno Dokumentation](https://docs.deno.com/runtime/reference/cli/compile/#supported-targets).

Nach dem Erstellen der Executable müssen folgende Dateien in den `content`-Ordner kopiert werden:
- `index.html`
- `favicon.ico`
- `cat_logo.png`
    ```bash
    cp ../dist/* ./content
    ```
- `blockly.jar`
    1. Für die Erstellung der `blockly.jar`-Datei muss in das Hauptverzeichnis gewechselt werden:
        ```bash
        cd ../../../
        ```
    2. Dann kann per Gradle die `Blockly.jar`-Datei erstellt werden:
        ```bash
        ./gradlew buildBlocklyJar
        ```
    3. Danach muss die `blockly.jar`-Datei in den `content`-Ordner kopiert werden.
        ```bash
        cp ./blockly/build/libs/Blockly.jar ./blockly/frontend/webserver/content/blockly.jar
        ```

Nun kann die Executable gestartet werden. Die erstellte Executable startet einen Webserver, welcher die Blockly-Oberfläche lädt und auch den Blockly-Dungeon in Java öffnet (dazu muss Java 21 oder höher installiert sein). Die Executable muss sich im selben Verzeichnis wie der `content`-Ordner befinden.
