# Blockly-Code-Runner VS Code Extension

[![Extension Icon](blockly/vs-code-extension/images/logo.png)](https://github.com/Dungeon-CampusMinden/Dungeon)

Die "Blockly-Code-Runner" Extension ermöglicht es, aus Blockly generierten Code (derzeit Java-Dateien) an einen externen Server (Dungeon) zu senden und dessen Ausführung zu steuern.

## Features

*   **Code ausführen**: Senden Sie den Inhalt der aktuell geöffneten Java-Datei an einen konfigurierten Blockly-Server zur Ausführung.
    *   Kommando: `Blockly: Run Blockly-Code`
    *   Icon: $(play) (erscheint im Editor-Titelbereich für `.java`-Dateien)
*   **Ausführung stoppen**: Senden Sie einen Befehl, um die aktuelle Ausführung auf dem Blockly-Server zu stoppen.
    *   Kommando: `Blockly: Stop Blockly-Code`
    *   Icon: $(debug-stop) (erscheint im Editor-Titelbereich für `.java`-Dateien)

## Voraussetzungen

*   Ein laufender Blockly-Server, der die entsprechenden Endpunkte zum Empfangen und Stoppen von Code bereitstellt.
*   Die Extension ist standardmäßig so konfiguriert, dass sie mit einem Server unter `http://localhost:8080` kommuniziert.

## Konfiguration

Sie können die URL des Blockly-Servers in den VS Code Einstellungen anpassen:

*   `blocklyServer.url`: Die URL des Servers, an den der Blockly-Code gesendet wird (Standard: `http://localhost:8080`).

Öffnen Sie dazu die Einstellungen (`Ctrl+,` oder `Cmd+,`), suchen Sie nach "Blockly-Code-Runner" und passen Sie die Server-URL an.

## Installation

Diese Extension ist nicht im offiziellen VS Code Marketplace verfügbar. Für Anweisungen zur lokalen Installation, siehe [Lokale Installation der Extension](blockly/doc/install-extension.md).

## Entwicklung

Informationen zum Erstellen und Paketieren der Extension finden Sie ebenfalls in der [Installationsanleitung](blockly/doc/install-extension.md) im Abschnitt für Entwickler.

---

Bei Problemen oder Anregungen erstelle bitte ein Issue im [GitHub Repository](https://github.com/Dungeon-CampusMinden/Dungeon).