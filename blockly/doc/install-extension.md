# Lokale Installation der Blockly-Code-Runner Extension

Dieses Dokument beschreibt, wie du die "Blockly-Code-Runner" VS Code Extension paketieren und manuell auf anderen Rechnern installieren kannst.

## Voraussetzungen

### Für den Entwickler (zum Erstellen der `.vsix`-Datei):

1.  **Node.js und npm**: Stelle sicher, dass Node.js (welches npm beinhaltet) installiert ist. Du kannst es von [nodejs.org](https://nodejs.org/) herunterladen.
2.  **vsce (Visual Studio Code Extension Manager)**: Dies ist das offizielle Werkzeug zum Paketieren und Veröffentlichen von VS Code Extensions. Installiere es global über npm:
    ```sh
    npm install -g @vscode/vsce
    ```

### Für den Benutzer (zum Installieren der `.vsix`-Datei):

1.  **Visual Studio Code**: Eine installierte Version von VS Code ist erforderlich.
2.  **Die `.vsix`-Datei**: Du benötigst die `.vsix`-Installationsdatei der "Blockly-Code-Runner" Extension (z.B. `blockly-code-runner-0.0.1.vsix`). Diese Datei wird vom Entwickler der Extension bereitgestellt.

## Schritt 1: Erstellen des Extension-Pakets (`.vsix`-Datei) (Für Entwickler)

Als Entwickler der Extension musst du die folgenden Schritte in deinem Projektverzeichnis ausführen:

1.  **Öffne ein Terminal** in deinem Extension-Projektverzeichnis.
2.  **Kompiliere deinen TypeScript-Code** (falls noch nicht geschehen). Dein Projekt hat bereits ein Skript dafür in der `package.json`:
    ```sh
    npm run compile
    ```
3.  **Paketiere die Extension**: Führe den folgenden Befehl aus, um die `.vsix`-Datei zu erstellen:
    ```sh
    vsce package
    ```
    Dieser Befehl erstellt eine Datei mit einem Namen wie `blockly-code-runner-X.Y.Z.vsix` (die Version stammt aus deiner `package.json`). Diese Datei ist das Installationspaket für deine Extension.

    *Hinweis: `vsce` liest die `.vscodeignore`-Datei, um zu bestimmen, welche Dateien nicht in das Paket aufgenommen werden sollen. Das `vscode:prepublish`-Skript (`npm run compile`) in der `package.json` stellt sicher, dass der Code vor dem Paketieren kompiliert wird.*

## Schritt 2: Verteilen der `.vsix`-Datei

Gib die generierte `.vsix`-Datei (z.B. `blockly-code-runner-0.0.1.vsix`) an deine Benutzer weiter. Dies kann per E-Mail, USB-Stick, Netzlaufwerk oder einer anderen Methode deiner Wahl geschehen.

## Schritt 3: Installation der Extension durch den Benutzer

Benutzer können die `.vsix`-Datei auf zwei Arten installieren:

### Methode A: Über die VS Code Benutzeroberfläche

1.  Öffne Visual Studio Code.
2.  Gehe zur Extensions-Ansicht (Klicke auf das Extensions-Icon in der Activity Bar auf der Seite oder drücke `Ctrl+Shift+X`).
3.  Klicke auf die drei Punkte (`...`) oben rechts in der Extensions-Ansicht, um das Menü "Weitere Aktionen..." zu öffnen.
4.  Wähle "Aus VSIX installieren..." (`Install from VSIX...`).
5.  Navigiere zur heruntergeladenen `.vsix`-Datei und wähle sie aus.
6.  VS Code wird die Extension installieren und dich möglicherweise auffordern, das Fenster neu zu laden.

### Methode B: Über die Kommandozeile

1.  Öffne ein Terminal oder eine Eingabeaufforderung.
2.  Führe den folgenden Befehl aus, wobei du `pfad/zur/blockly-code-runner-VERSION.vsix` durch den tatsächlichen Pfad zur `.vsix`-Datei ersetzt:
    ```sh
    code --install-extension pfad/zur/blockly-code-runner-VERSION.vsix
    ```
    Beispiel: Wenn deine Datei `blockly-code-runner-0.0.1.vsix` im Ordner `Downloads` liegt, könnte der Befehl so aussehen:
    ```sh
    code --install-extension Downloads/blockly-code-runner-0.0.1.vsix
    ```
    Wenn der `code`-Befehl nicht gefunden wird, stelle sicher, dass VS Code zum PATH deiner Shell hinzugefügt wurde während der Installation, oder verwende den vollen Pfad zur VS Code-Anwendung.
3.  Starte VS Code neu, falls erforderlich.

## Aktualisieren der Extension

Um die Extension zu aktualisieren, erstelle als Entwickler eine neue `.vsix`-Datei mit der aktualisierten Version (vergiss nicht, die Versionsnummer in deiner `package.json` zu erhöhen). Lasse die Benutzer die neue `.vsix`-Datei einfach wie in "Schritt 3" beschrieben installieren. VS Code wird die vorhandene Installation überschreiben.
