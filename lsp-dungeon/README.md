# LSP Dungeon
Ziel dieser Ausarbeitung ist die Umsetzung einer Erweiterung für VS-Code. Die Erweiterung soll bei der Entwicklung der Dungeon Programmiersprache helfen. Hierfür werden Code-Highlighting, Fehlermeldungen und Auto-Completions umgesetzt, die für eine angenehmere Implementierung sorgen. Im folgenden wird erläutert, welche Schritte durchlaufen wurden um die drei geforderten Komponenten zu implementieren. 
## Grundlagen
Als Grundgerüst für die Umsetzung des LSP Servers wurde das Projekt [lsp-sample von Microsoft](https://github.com/microsoft/vscode-extension-samples/tree/main/lsp-sample) verwendet und an die Anforderungen für die Dungeon-Programmiersprache angepasst. Dieses Projekt ist eine beispielhafte Umsetzung, welche die Grundfunktionen der Bibliothek [vscode-languageserver-node](https://github.com/microsoft/vscode-languageserver-node) implementiert hat. Wie der Aufbau eines LSP-Servers aussieht wird im Kapitel [LSP Server](#lsp-server) erläutert. Im Gegensatz zur Fehleranalyse und den Auto-Completions wird für das Code-Highlighting kein LSP Server benötigt, sondern wird mithilfe einer TextMate-Grammatik definiert. TextMate Grammatiken werden in der Regel in JSON-Dokumenten beschrieben und definieren Regeln und Muster zum Einfärben bestimmter Strukturen im gegebenen Quellcode. Hierfür wurde sich am [Syntax Highlight Guide](https://code.visualstudio.com/api/language-extensions/syntax-highlight-guide) orientiert. Das Code-Highlighting ist in einem eigenen kleinem Projekt (syntax-highlighting) umgesetzt und muss einzeln als weitere Extension hinzugefügt werden. Somit ergeben sich in der Gesamtheit 3 Unterprojekte:
1. **syntax-highlighting** Beinhaltet die Umsetzung zum hervorheben bestimmter Code-Bestandteile für bessere Lesbarkeit.
2. **lsp-dungeon** Implementiert die LSP-Server Extension unter Verwendung der Dungeon-Diagnostics.jar.
3. **dungeon-diagnostics** Java-Projekt, das mithilfe von ANTLR genauere Syntax und Semantik-Analysen auf dem Dungeon-Code durchführen kann. Liefert als Output die vom **lsp-dungeon** Projekt verwendete Dungeon-Diagnostics.jar.
## LSP Server
Das LSP Protokoll ist ein vielverbreiteter und beliebter Ansatz für die Entwicklung von Spracherweiterungen von Programmiersprachen. Dazu gehören abgesehen von den hier umgesetzten Erweiterungen (Diagnostics, Completions) auch Dinge wie Zusatzinformationen beim Hovern über bestimmte Code-Elemente, Formattierung oder die Möglichkeit zu der Definition von z.B. Funktionen oder Objekten zu springen.
![LSP-Vorteil](images\lsp_advantage.png)
Die Abbildung stellt dar, aus welchem Grund LSP ein beliebter Ansatz ist Erweiterung für Sprachen zu erstellen. LSP vereinheitlicht die Entwicklung von Erweiterung für verschiedene Sprachen und kann von verschiedenen IDEs verwendet werden. Ohne den einheitlichen Standard müsste jede IDE für jede Sprache eine eigene Extension bereitstellen. Dementsprechend kann ein einzelner Editor eine gute Unterstützung für viele verschiedene Sprachen bereitstellen. Des weiteren verfolgt LSP einen konsistenten Ablauf, was die Kommunikation zwischen dem Server und dem Client (IDE) angeht.
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

Um eine .vsix-Datei zu erstellen, wird das vsce-Tool verwendet. Falls dies noch nicht installiert wurde kann es mit dem folgenden Befehl installiert werden:
`npm install -g vsce`

Danach in das Verzeichnis der Extension navigieren und den folgenden Befehl ausführen:
`vsce package`

Dies erstellt eine .vsix-Datei im aktuellen Verzeichnis.

## Installation der .vsix-Datei:

Um die .vsix-Datei in VS Code zu installieren:

VS Code öffnen.
Zu Extensions gehen (Ctrl+Shift+X).
Auf das Drei-Punkte-Menü oben rechts klicken und „Extension aus VSIX installieren...“ wählen.
Wähle die erstellte .vsix-Datei aus und installiere sie.
Extension ist nun lokal in VS Code installiert und kann wie jede andere Extension verwendet werden.


# Quellen

