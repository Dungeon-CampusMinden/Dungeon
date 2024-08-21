# LSP Dungeon

## Installations-Schritte:

Erstelle die .vsix-Datei:

Um eine .vsix-Datei zu erstellen, verwende das vsce-Tool. Installiere es, falls du es noch nicht hast:
npm install -g vsce

Navigiere dann in das Verzeichnis deiner Extension und führe den folgenden Befehl aus:
vsce package

Dies erstellt eine .vsix-Datei im aktuellen Verzeichnis.

## Installation der .vsix-Datei:

Um die .vsix-Datei in VS Code zu installieren:

Öffne VS Code.
Gehe zu den Extensions (Ctrl+Shift+X).
Klicke auf das Drei-Punkte-Menü oben rechts und wähle „Extension aus VSIX installieren...“.
Wähle die erstellte .vsix-Datei aus und installiere sie.
Deine Extension ist nun lokal in VS Code installiert und du kannst sie wie jede andere Extension verwenden.

