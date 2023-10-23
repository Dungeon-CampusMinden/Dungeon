# Erweitern der Applikation

## Inhaltsverzeichnis

- [Erweitern der Applikation](#erweitern-der-applikation)
  - [Inhaltsverzeichnis](#inhaltsverzeichnis)
  - [Architektur der Applikation](#architektur-der-applikation)
    - [1. Blockly-Bibliothek](#1-blockly-bibliothek)
    - [2. TypeScript](#2-typescript)
    - [3. Vite.js](#3-vitejs)
      - [Entwicklungsserver](#entwicklungsserver)
      - [Build-System](#build-system)
    - [4. Anwendungslogik](#4-anwendungslogik)
    - [5. Benutzeroberfläche](#5-benutzeroberfläche)
    - [6. Kommunikation mit dem Server](#6-kommunikation-mit-dem-server)
    - [7. Dungeon-Server](#7-dungeon-server)
  - [Hinzufügen neuer Blöcke](#hinzufügen-neuer-blöcke)
    - [Schritt 1: Toolbox erweitern (toolbox.ts)](#schritt-1-toolbox-erweitern-toolboxts)
    - [Schritt 2: Block beschreiben (./blocks/dungeon.ts)](#schritt-2-block-beschreiben-blocksdungeonts)
    - [Schritt 3: Funktion für den Java Codegenerator erstellen (./generators/java/...)](#schritt-3-funktion-für-den-java-codegenerator-erstellen-generatorsjava)
    - [Schritt 4: Funktionalität im Dungeon-Server implementieren](#schritt-4-funktionalität-im-dungeon-server-implementieren)

## Architektur der Applikation

Übersicht über die Schlüsselelemente von Blockly-Dungeon

### 1. Blockly-Bibliothek

Blockly bildet das Fundament der Anwendung. Es ist eine Bibliothek, die es ermöglicht, visuelle Programmierblöcke zu erstellen und diese in Code zu übersetzen. Blockly lässt sich einfach in eine Webanwendung integrieren und bietet eine Vielzahl an Konfigurationsmöglichkeiten. Der Code ist unter der [Apache 2.0 Lizenz](https://github.com/google/blockly/blob/develop/LICENSE) lizenziert und kann somit frei verwendet und modifiziert werden.

### 2. TypeScript

TypeScript ist die Programmiersprache, in der die Anwendungslogik geschrieben ist. Sie bringt statische Typisierung und moderne Sprachfeatures mit sich. TypeScript wird zu JavaScript kompiliert und läuft somit problemlos im Browser. Die Sprache ist ebenfalls unter der [Apache 2.0 Lizenz](https://github.com/microsoft/TypeScript/blob/main/LICENSE.txt) lizenziert.

### 3. Vite.js

Vite.js fungiert als Build-Tool und Entwicklungsserver für die Anwendung. Vite bietet eine schnelle Entwicklungsumgebung und ein effizientes Build-System. Außerdem wird TypeScript unterstützt, was die Integration von TypeScript in das Projekt nahtlos ermöglicht. Vite ist unter der [MIT Lizenz](https://github.com/vitejs/vite/blob/main/LICENSE) lizenziert.

**Die Architektur von Vite.js besteht aus:**

#### Entwicklungsserver

Der Entwicklungsserver ist ein HTTP-Server, der die Anwendung im Browser bereitstellt. Er bietet Hot-Module-Reloading, was bedeutet, dass Änderungen am Code sofort im Browser sichtbar sind. Der Entwicklungsserver ist in der Lage, TypeScript-Code zu kompilieren und bietet eine Vielzahl an Konfigurationsmöglichkeiten.

#### Build-System

Das Build-System ist ein Tool, das die Anwendung für die Produktion baut. Es kompiliert den TypeScript-Code und optimiert die Anwendung für die Produktion. Die Anwendung wird in eine statische HTML-Datei und mehrere JavaScript-Dateien kompiliert. Diese Dateien können dann auf einem Webserver bereitgestellt werden.

### 4. Anwendungslogik

Die Anwendungslogik ist in TypeScript geschrieben und befindet sich im Ordner `./src`. Die Anwendungslogik ist in mehrere Dateien aufgeteilt, die jeweils einen bestimmten Teil der Anwendung implementieren. Sie ist in folgende Teile aufgeteilt:

- `./src/index.ts`: Die index.ts Datei ist der Einstiegspunkt der Anwendung. Sie wird als Erstes in die `index.html` Datei eingebunden. Die Datei lädt die Blockly-Bibliothek und initialisiert die Anwendung.

- `./src/serialization.ts`: Die serialization.ts Datei implementiert die Serialisierung und Deserialisierung des Workspace. Die Serialisierung und Deserialisierung wird von der Blockly-Bibliothek verwendet, um den Workspace zu speichern und zu laden. Dies wird benötigt, damit der Workspace nach einem Neuladen der Seite wiederhergestellt werden kann.

- `./src/toolbox.ts`: Die toolbox.ts Datei beschreibt die Toolbox, die in der Anwendung angezeigt wird. Die Toolbox enthält alle Blöcke, die in der Anwendung verwendet werden können.

- `./src/blocks/dungeon.ts`: Die genaue Beschreibung der Blöcke wird in der dungeon.ts Datei implementiert. Alle Blöcke, welche in der toolbox.ts Datei angelegt werden, müssen in der dungeon.ts Datei beschrieben werden.

- `./src/generators/...`: Der generierte Code wird im Ordner `./src/generators/...` implementiert. In der java.ts wird eine Klasse JavaGenerator erstellt, welche von der Klasse Blockly.Generator erbt. Die erzeugte Klasse importiert anschließend alle Kategoerien aus den `./src/generators/java/...` Dateien. In den `./src/generators/java/...` Dateien werden die Funktionen für die Codeerstellung implementiert. Die Funktionen werden von der `JavaGenerator` Klasse aufgerufen, um aus dem jeweiligen Block den gewünschten Code zu erzeugen.

- `./src/character.ts`: Die character.ts Datei enthält die Klasse Character, welche den Charakter der Anwendung repräsentiert. Sie speichert die Position des Charakters.

- `./src/config.ts`: In der config.ts Datei werden die Konfigurationen der Anwendung definiert.

```ts
export const config: Config = {
  API_URL: "http://localhost:8080/", // <- Hier muss die URL vom Dungeon-Server eingetragen werden
  CHARACTER_MAX_MOVEMENT: 20, // <- Hier muss die maximale Anzahl an Schritten eingetragen werden, die ein Charakter mit den Bewegungsblöcken ausführen kann.
  VARIABLE_MAX_VALUE: 20, // <- Hier muss der maximale Wert eingetragen werden, den eine Variable annehmen kann.
  REPEAT_MAX_VALUE: 10, // <- Hier muss die maximale Anzahl an Wiederholungen eingetragen werden, die eine Schleife ausführen kann.
  HIDE_GENERATED_CODE: false, // <- Hier muss eingestellt werden, ob der generierte Code in der Anwendung angezeigt werden soll oder nicht.
  HIDE_RESPONSE_INFO: true, // <- Hier muss eingestellt werden, ob die Serverantwort in der Anwendung angezeigt werden soll oder nicht.
};
```

### 5. Benutzeroberfläche

Die Benutzeroberfläche ist in HTML und CSS erstellt. Sie umfasst die `index.html` Datei und die `./src/style.css` Datei. Die `index.html` Datei enthält die Grundstruktur der Anwendung. Die `style.css` Datei enthält das Styling der Anwendung.

### 6. Kommunikation mit dem Server

Die Kommunikation mit dem Server wird in der `./src/api.ts` Datei implementiert. Die `api.ts` Datei enthält eine Klasse `Api`, welche die Kommunikation mit dem Server bereitstellt. Sie verwendet die `fetch-API`, um mit dem Server zu kommunizieren. Die `fetch-API` ist eine moderne Alternative zu `XMLHTTPRequest`, die es ermöglicht, HTTP-Anfragen im Browser zu senden. Die Klasse bietet eine Methode `post`, welche eine HTTP-POST-Anfrage an den Server sendet. Die Methode `post` erwartet als Parameter den Endpoint der URL, an die die Anfrage gesendet werden soll, und alternativ die Daten, die mit der Anfrage gesendet werden sollen. Die Methode gibt ein Promise zurück, welches die Antwort des Servers enthält.

### 7. Dungeon-Server

Für die Auswertung des generierten Codes und dem Ausführen der Befehle wird der Code per HTTP an den Dungeon-Server gesendet. Der Server ist in Java geschrieben und im Verzeichnis `Dungeon/blockly-dungeon/src/server/...` zu finden.

## Hinzufügen neuer Blöcke

Hier ist eine Schritt-für-Schritt-Anleitung, wie neue Blöcke hinzugefügt werden können.

### Schritt 1: Toolbox erweitern (toolbox.ts)

Um einen neuen Block hinzuzufügen, muss dieser zuerst in die Toolbox (`toolbox.ts`) eingetragen werden. Dabei kann entweder eine neue Kategorie erstellt werden oder der Block einer bestehenden Kategorie hinzugefügt werden.

Beispiel move_up Block:

```ts
export const toolbox = {
  kind: "categoryToolbox", // Gibt an das es sich um eine Toolbox handelt
  contents: [
    {
      kind: "category", // Gibt an das es sich um eine Kategorie handelt
      name: "Bewegung", // Hier muss der Name der Kategorie eingetragen werden
      colour: "290", // Hier kann die Farbe der Kategorie eingetragen werden
      contents: [
        {
          kind: "block", // Gibt an das es sich um einen Block handelt
          type: "move_up", // Hier muss der Name des Blocks eingetragen werden
        },
      ],
    },
  ],
};
```

### Schritt 2: Block beschreiben (./blocks/dungeon.ts)

Bevor der Block in der Toolbox angezeigt werden kann, muss dieser in der `dungeon.ts` Datei definiert werden.
Eine gute Übersicht, welche Eigenschaften ein Block haben kann, findet sich in der [Blockly Dokumentation](https://developers.google.com/blockly/guides/create-custom-blocks/define-blocks) oder man erstellt einen Block mithilfe des [Blockly Developer Tools](https://blockly-demo.appspot.com/static/demos/blockfactory/index.html). Hierbei handelt es sich um eine Webanwendung, die es ermöglicht, Blöcke zu erstellen und die JSON-Definition des Blocks zu exportieren.

Beispiel move_up Block Definition:

```ts
export const blocks = Blockly.common.createBlockDefinitionsFromJsonArray([
  {
    type: "move_up", // Hier muss der Name des Blocks eingetragen werden. Der Name muss mit dem Namen in der toolbox.ts übereinstimmen.
    message0: "Oben %1", // Hier wird der Text angegeben, der im Block angezeigt wird. %1 wird durch den ersten Wert in args0 ersetzt.
    // previousStatement und nextStatement geben an, welche Blöcke miteinander verbunden werden können. Wird null angegeben, können alle Blöcke verbunden werden. Diese Eigenschaft ist nützlich, wenn der Block nur mit bestimmten Blöcken verbunden werden sollen.
    previousStatement: null,
    nextStatement: null,
    colour: 290, // Hier kann die Farbe des Blocks eingetragen werden
    tooltip: "Nach oben gehen", // Hier kann ein Tooltip für den Block eingetragen werden.
    // Hier werden die Argumente des Blocks definiert. In diesem Fall wird ein Feld für die Anzahl der Schritte angezeigt.
    args0: [
      {
        type: "field_number", // Gibt an das es sich um ein Feld handelt, in dem eine Zahl eingegeben werden kann.
        name: "amount", // Hier muss der Name des Feldes eingetragen werden. Der Name wird verwendet, um auf den Wert des Feldes zuzugreifen.
        value: 1, // Hier kann ein Standardwert für das Feld eingetragen werden.
        min: 1, // Hier kann der minimale Wert für das Feld eingetragen werden.
        max: config.CHARACTER_MAX_MOVEMENT, // Hier kann der maximale Wert für das Feld eingetragen werden.
      },
    ],
  },
]);
```

### Schritt 3: Funktion für den Java Codegenerator erstellen (./generators/java/...)

Nachdem der Block zur Toolbox hinzugefügt und entsprechend definiert wurde, muss noch die Funktion für den Java Codegenerator erstellt werden. Hierfür muss eine Funktion angelegt werden, welche den Namen des Blocks entspricht. Die Funktion erhält als Parameter den Block und den Generator.
Der **block** Parameter enthält alle Informationen über den Block. Der **generator** Parameter enthält alle Funktionen, die benötigt werden, um den Code zu generieren. Bei simplen Blöcken, die keine Werte enthalten, kann die Funktion einfach einen String zurückgeben. Bei komplexeren Blöcken, die Werte enthalten, muss die Funktion den Code für den Block generieren und diesen zurückgeben.

Beispiel move_up Block Codegenerator:

```ts
export function move_up(block: Blockly.Block, _generator: Blockly.Generator) {
  const amount: number = block.getFieldValue("amount");
  let code = "";

  for (let i = 0; i < amount; i++) {
    if (i === amount - 1) {
      code += "oben();";
      break;
    }
    code += "oben();\n";
  }

  return code;
}
```

### Schritt 4: Funktionalität im Dungeon-Server implementieren

Wenn der zuvor erzeugte Block den gewünschten Code generiert, muss die Funktionalität im Dungeon-Server implementiert werden.
Nachdem der Start-Button in der Anwendung gedrückt wird, wird der generierte Code an den Dungeon-Server gesendet. Der Dungeon-Server führt den Code Schritt für Schritt aus, in dieser Auswertung muss die Funktionalität des neuen Blocks implementiert werden. Alle Funktionen, welche den Dungeon-Server betreffen, sind unter `Dungeon/blockly-dungeon/src/server/...` zu finden.
