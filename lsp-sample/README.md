# LSP Dungeon
Ziel dieser Extension ist die Umsetzung einer Erweiterung für VS-Code. Die Erweiterung soll bei der Entwicklung der Dungeon Programmiersprache helfen. Hierfür wurden Code-Highlighting, Fehlermeldungen und Auto-Completions umgesetzt, die für eine angenehmere Implementierung sorgen. Im folgenden wird erläutert, welche Schritte durchlaufen wurden um die drei geforderten Komponenten zu implementieren. 
## Grundlagen
## LSP Server

### Umsetzung LSP Server

### Auto Completions

## Fehleranalyse
Für die Fehleranalyse wird ANTLR benötigt um Textdokumente zu parsen und mit der definierten Grammatik zu vergleichen. Im Gegensatz zum LSP-Server, welcher als Node-Projekt umgesetzt wurde, wurde das Projekt für den Parser/Interpreter als Java-Projekt aufgesetzt. Dementsprechend wird eine Schnittstelle zwischen dem Node- und dem Java-Projekt benötigt, da die Projekte unabhängig voneinander implementiert sind. Da der Parser/Interpreter lediglich die Aufgabe hat alle Fehlermeldungen aus einem Textdokument zurückzugeben wurde sich dafür entschieden das Java-Projekt als ausführbare JAR in das Node-Projekt einzubinden. Im Node-Projekt liegt dementsprechend nur die fertige JAR und der Nutzer hat keinen Einblick in die Funktionalität der JAR, da diese aus einem seperaten Unterprojekt gebaut wurde. Dieses Unterprojekt trägt den Namen Dungeon-Diagnostics.

## Dungeon-Diagnostics
Das Unterprojekt Dungeon-Diagnostics ist ein Java-Projekt, welches als Argument einen Dokumentpfad entgegennimmt und dieses Dokument parsed und mit einem Visitor weitere Analysen durchführt und gefundene Probleme ausgibt. Das Projekt ist wie folgt aufgebaut:
```
Dungeon-Diagnostics/
│
├── out/                 				# Outputdateien
│   ├── artifacts/			
|       └── Dungeon_Diagnostics_jar
│           └── Dungeon-Diagnostics.jar 
│
├── src/                 				# Quellcode 
│   ├── Main.class          			
│   ├── ErrorInfo.class     			
│   ├── CustomErrorListener.class     	
│   └── DiagnosticsVisitor.class
│
└── DungeonDiagnostics.g4   			# Grammatik
```
In der Ordnerstruktur wurden einige Dateien ausgelassen, welche zusätzlich zu den aufgeführten Dateien vorhanden sind. Dabei handelt es sich um die von ANTLR generierten Dateien wie den Lexer und den Parser, welche nicht händisch angepasst werden. Der Grundstein des Projekts ist die Grammatik **DungeonDiagnostics.g4**. Anhand dieser Grammatik werden alle relevanten Dokumente generiert, die für die weitere Analyse relevant sind (Lexer, Parser, Visitor, etc.). Der generierte Parser und Lexer werden wie bereits erwähnt nur generiert und nicht angepasst. Der Visitor hingegen bietet die Möglichkeit mit dem gewünschten Verhalten implementiert zu werden. Hierdurch bietet sich die Möglichkeit weitere Semantische Analysen durchzuführen.
### Code Highlighting

# Installationen
## JAR Updaten
Falls Änderungen am Verhalten der Diagnostics durchgeführt werden und eine neue JAR erzeugt wurde, muss diese JAR in den Ordner *server\jars* eingefügt und die alte JAR gelöscht werden.
## Erstellen der .vsix-Datei:

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



