---
title: "Blockly: How to install"
---

# Installation

## Voraussetzungen

Bevor mit der Installation begonnen werden kann, müssen folgende Voraussetzungen erfüllt sein:

1. **Node.js und npm:** Blockly benötigt Node.js und npm, um die Applikation zu installieren und zu starten. Node.js und npm können von der [offiziellen Website](https://nodejs.org/en/) heruntergeladen werden. Die Installation von Node.js beinhaltet auch die Installation von npm.
2. **Git:** Blockly benötigt Git, um das Repository zu klonen. Git kann von der [offiziellen Website](https://git-scm.com/downloads) heruntergeladen werden. Alternativ kann auch das Repository als ZIP-Datei [heruntergeladen](https://github.com/Dungeon-CampusMinden/Dungeon/archive/refs/heads/master.zip) werden.

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

Starten Sie zuerst das Blockly-Dungeon mit:

```bash
gradlew runBlockly
```

Starten Sie dann den Vite.js-Entwicklungsserver, um "Blocky" lokal auszuführen:

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

## Nutzung mit Deno und Erstellen einer Executable

Zusätzlich zur Verwendung von Node.js und npm, kann Deno genutzt werden, um eine ausführbare Datei zu erstellen. Damit sind folgende Schritte zu beachten:

### Deno Installation

- Installieren Sie Deno, wenn Sie eine ausführbare Datei für verschiedene Plattformen erstellen wollen. Weitere Informationen finden Sie unter [https://deno.land](https://deno.land).
- Deno kann auch Pakete verwalten, z.B. mit `deno add` oder `deno install`. Dadurch entfällt in vielen Fällen die Nutzung von Node.js und npm.

### Executable erstellen

1. **Windows (x86_64):**
```bash
deno compile --target x86_64-pc-windows-msvc --allow-net --allow-read --allow-run --no-npm --output blockly_x86_64.exe --icon ./content/favicon.ico webserver.ts
```
> Bitte beachten Sie, dass in der aktuellen Deno-Version das Icon nicht korrekt übernommen wird und daher in der Exe nicht erscheint.

2.**Linux (x86_64):**
```bash
deno compile --target x86_64-unknown-linux-gnu --allow-net --allow-read --allow-run --no-npm --output blockly_x86_64.bin webserver.ts
```

Weitere unterstützte Zielplattformen finden Sie in der [Deno Dokumentation](https://docs.deno.com/runtime/reference/cli/compile/#supported-targets).

Die erstellte Executable startet einen Webserver, welcher die Blockly-Oberfläche lädt und auch den Blockly-Dungeon in Java öffnet (dazu muss Java 21 oder höher installiert sein). Die Executable muss sich im selben Verzeichnis wie der `content`-Ordner befinden.
