# Installation

## Inhaltsverzeichnis

- [Installation](#installation)
  - [Inhaltsverzeichnis](#inhaltsverzeichnis)
  - [Voraussetzungen](#voraussetzungen)
  - [Schritt 1: Repository klonen](#schritt-1-repository-klonen)
  - [Schritt 2: Wechsel in den Projektordner](#schritt-2-wechsel-in-den-projektordner)
  - [Schritt 3: Installation der Abhängigkeiten](#schritt-3-installation-der-abhängigkeiten)
  - [Schritt 4: Starten der Applikation](#schritt-4-starten-der-applikation)
  - [Schritt 5 (Optional): Applikation bauen](#schritt-5-optional-applikation-bauen)

## Voraussetzungen

Bevor mit der Installation begonnen werden kann, müssen folgende Voraussetzungen erfüllt sein:

1. **Node.js und npm:** Blockly-Dungeon benötigt Node.js und npm, um die Applikation zu installieren und zu starten. Node.js und npm können von der [offiziellen Website](https://nodejs.org/en/) heruntergeladen werden. Die Installation von Node.js beinhaltet auch die Installation von npm.
2. **Git:** Blockly-Dungeon benötigt Git, um das Repository zu klonen. Git kann von der [offiziellen Website](https://git-scm.com/downloads) heruntergeladen werden.

## Schritt 1: Repository klonen

Öffnen Sie Ihr bevorzugtes Terminal und navigieren Sie zu dem Verzeichnis, in dem Sie "Blocky-Dungeon" speichern möchten. Führen Sie den folgenden Befehl aus, um das Repository zu klonen:

```bash
git clone https://github.com/Programmiermethoden/Dungeon.git
```

## Schritt 2: Wechsel in den Projektordner

Navigieren Sie in das Verzeichnis, in dem das Repository geklont wurde:

```bash
cd Dungeon/blockly-dungeon
```

## Schritt 3: Installation der Abhängigkeiten

Verwenden Sie npm, um die benötigten Abhängigkeiten für "Blocky-Dungeon" zu installieren:

```bash
npm install
```

## Schritt 4: Starten der Applikation

Starten Sie den Vite.js-Entwicklungsserver, um "Blocky-Dungeon" lokal auszuführen:

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
