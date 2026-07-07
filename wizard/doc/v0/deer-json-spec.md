# deer.json Spezifikation

Status: 0.1-draft V0-Contract
Stand: 02.07.2026
Scope: V0, ein spielbarer Escape-Room-Level, ein Standard-Theme

## 1. Rolle Von deer.json

`deer.json` ist die interne editierbare Quelle des Wizards und der Contract zum
manuell gestarteten Java-Generator. Der Wizard liest und schreibt diese Datei.
Der Generator darf daraus Runtime-Dateien ableiten, aber `deer.json` bleibt das
Authoring-Modell.

Für Lehrende ist `deer.json` in V0 nicht das sichtbare Endprodukt. Die
sichtbare Abschlussaktion ist der `deer.zip`-Export. Dieses ZIP ist ein
DEER-Authoring-Bundle: Es enthält `deer.json` und referenzierte Custom Assets
in einer Datei. Der Java-Generator wird in V0 noch manuell mit diesem Bundle
oder dem entpackten Projektordner gestartet und erzeugt erst daraus das
spielbare Room-Paket.

V0 beschreibt:

```text
Raum-Metadaten
-> Spielsitzung
-> Standard-Szenario
-> Oberflächen
-> Rätselgraph
-> Rätsel mit Parametern
-> Assets
```

Die typ-spezifischen Pflichtparameter stehen in
[`parameter-table-v0.md`](parameter-table-v0.md).

Der UI-orientierte Wizard-Ablauf und die von Lehrenden auszufüllenden Felder
stehen in [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md).

Explizit nicht V0:

- Lernziele im Format,
- mehrere Themes,
- Custom-Themes, Tilesets, Sprites oder UI-Skins,
- binäre Assets direkt in JSON,
- automatisch gestarteter Generator,
- Einlesen bestehender `deer.zip`-Pakete in den Wizard,
- generierte `.level`-Dateien.

## 2. Top-Level Struktur

Top-Level-Struktur:

```json
{
  "formatVersion": "0.1-draft",
  "metadata": {},
  "session": {},
  "scenario": {},
  "surfaces": [],
  "riddleGraph": {},
  "riddles": [],
  "assets": []
}
```

Pflichtfelder:

| Feld | Zweck |
|---|---|
| `formatVersion` | Version des Authoring-Formats. |
| `metadata` | Titel, ID, Sprache, Autor, Kurzbeschreibung. |
| `session` | Zielgruppe, Spieleranzahl und Zeitlimit. |
| `scenario` | Standard-Theme, Story-Rahmen und Intro/Outro. |
| `surfaces` | Vom Wizard abgeleitete Interaktionsorte wie Computer, Keypad oder Tür. |
| `riddleGraph` | Progression und Abhängigkeiten. |
| `riddles` | Konkrete Rätseldefinitionen. |
| `assets` | Referenzen auf Paket-Assets. |

## 3. ID-Konventionen

Alle IDs sollen stabil und menschenlesbar sein.

Empfehlung:

```text
lower_snake_case
```

Beispiele:

- `r_pc_login`
- `n_storage_keypad`
- `asset_password_note_1`
- `token_pc_logged_in`
- `item_usb_blue`

Validierung:

- IDs müssen innerhalb ihres Bereichs eindeutig sein.
- Referenzen müssen auf existierende IDs zeigen.
- IDs sollten nicht automatisch aus Titeln neu erzeugt werden, sobald andere
  Elemente darauf verweisen.

## 4. metadata

Beschreibt das Authoring-Artefakt.

```json
{
  "id": "wizard_example_v0",
  "title": "Wizard Beispielraum V0",
  "locale": "de-DE",
  "description": "Beispielkonfiguration mit den aktuell verfügbaren V0-Bausteinen.",
  "author": "Beispiel Lehrkraft"
}
```

Pflicht in V0:

- `id`
- `title`
- `locale`

Optional in V0:

- `description`
- `author` als einfacher String

V0-Regel:

- `locale` ist standardmäßig `de-DE`. Mehrsprachigkeit kann später folgen,
  ohne das Feld neu einzuführen.

## 5. session

Beschreibt Unterrichts- und Spielsitzung, aber ohne Lernzielmodell.

```json
{
  "targetAudience": "Lernende im Bereich IT-Sicherheit",
  "priorKnowledge": "Grundlagen zu E-Mail, Webseiten und einfachen Codierungen.",
  "playerCount": {
    "min": 1,
    "max": 4
  },
  "time": {
    "limitMinutes": 60,
    "limitMode": "hard"
  }
}
```

V0-Regeln:

- `playerCount` beschreibt nur den erlaubten Bereich.
- Es gibt kein `recommended`, weil diese Empfehlung im Wizard abgeleitet werden
  kann und keine harte Eigenschaft des Raums ist.
- Der Raum ist immer kooperativ. Deshalb gibt es kein `collaborationMode`.
- `time.limitMode=hard`: Nach Ablauf endet der Raum.
- `time.limitMode=soft`: Nach Ablauf läuft der Raum weiter, aber Hinweise oder
  Unterstützung dürfen stärker werden.

## 6. scenario

V0 nutzt genau ein Standard-Theme. Das Feld `themeId` bleibt trotzdem im JSON,
damit spätere Versionen erweiterbar sind. In V0 sollte der Wizard dieses Feld
nicht als große Theme-Auswahl verkaufen.

```json
{
  "themeId": "default",
  "playerRole": "Untersuchungsteam",
  "premise": "Ein Labor ist verriegelt. Die Gruppe muss Hinweise rekonstruieren und einen Ausgang freischalten.",
  "mission": "Findet den finalen Zugangscode und öffnet die Ausgangstür.",
  "introText": "Der Alarm ist aktiv. Auf dem Wandtimer laufen 60 Minuten herunter.",
  "successText": "Die Ausgangstür öffnet sich.",
  "failureText": "Die Zeit ist abgelaufen."
}
```

V0-Regeln:

- `themeId` ist vorerst immer `default`.
- Es gibt kein Vorlagenfeld in V0. The Last Hour liefert Bausteine, aber keine
  vorausgewählte Raumstruktur.
- Custom Assets dürfen Inhalte ergänzen, aber das Theme nicht ersetzen.
- Storytexte sollten kurz bleiben.

## 7. surfaces

`surfaces` ist ein internes Register der Interaktionsorte im Raum. Lehrende
sollen diese Liste nicht technisch pflegen. Die UI leitet sie aus den gewählten
Bausteinen ab und erlaubt nur fachliche Benennung, z. B. "Labor-PC",
"Storage-Keypad" oder "Ausgangstür".

```json
[
  {
    "id": "s_main_computer",
    "kind": "computer",
    "title": "Labor-PC"
  },
  {
    "id": "s_storage_keypad",
    "kind": "keypad",
    "title": "Storage-Keypad"
  }
]
```

V0-Werte für `kind`:

- `world`
- `world_object`
- `computer`
- `keypad`
- `door`
- `container`
- `inventory`
- `control_panel`
- `assembly_area`

Warum ein eigenes Register:

- Rätselparameter können mit `surfaceId` auf eine konkrete Oberfläche zeigen.
- Hints mit `surface_visited` können gegen existierende Oberflächen validiert
  werden.
- Mehrere Computer, Keypads oder Türen bleiben später möglich, ohne den
  Contract neu zu bauen.

V0-Regeln:

- Mindestens eine Surface existiert, z. B. `s_world`.
- `surfaceId`-Referenzen müssen auf existierende Einträge in `surfaces`
  zeigen.
- Lehrende sehen fachliche Namen, nicht technische Slot-IDs.
- Für den ersten Generator-Slice darf die Runtime diese Oberflächen stark
  vereinfachen, z. B. auf eine Level-/World-Surface reduzieren oder pro
  PC-nahem Baustein einen einfachen Computer erzeugen.

## 8. riddleGraph

Der Rätselgraph beschreibt Progression, nicht Raumgeometrie. Er ist die
Authoring-Sicht auf Abhängigkeiten. Der Generator kann daraus später
Petri-Net-Strukturen, Trigger oder Runtime-States ableiten.

```json
{
  "startNodeId": "n_start",
  "endNodeIds": ["n_exit_open"],
  "nodes": [
    {
      "id": "n_start",
      "kind": "start",
      "title": "Start"
    },
    {
      "id": "n_pc_login",
      "kind": "riddle",
      "title": "PC Login",
      "riddleId": "r_pc_login"
    }
  ],
  "edges": [
    {
      "id": "e_start_to_pc_login",
      "from": "n_start",
      "to": "n_pc_login",
      "condition": {
        "type": "all_of_tokens",
        "tokens": ["token_power_on", "token_login_credentials_found"]
      }
    }
  ]
}
```

Node-Arten für V0:

- `start`
- `riddle`
- `event`
- `end`

Edge-Conditions für V0:

- `always`
- `all_of_tokens`
- `any_of_tokens`

Tokens sind für V0 bewusst Teil des Formats. Sie sind für Lehrende im UI
versteckbar, aber für Generator, Validierung und Petri-Net-Ableitung nützlich.

Validierung:

- Genau ein `startNodeId`.
- Mindestens ein `endNodeId`.
- Alle Rätsel-Nodes referenzieren ein existierendes `riddleId`.
- Jeder Endknoten muss vom Start erreichbar sein.
- Tokens in Edge-Conditions müssen von vorher erreichbaren Rätseln erzeugt
  werden können.
- Standard ist ein azyklischer Graph. Retry-Verhalten gehört in das jeweilige
  Rätsel, nicht als Graphzyklus.
- V0 erzeugt keine optionalen Rätsel: Jeder Rätselknoten muss erreichbar sein
  und auf einem durchspielbaren Pfad zum Ende liegen.
- Branches dürfen nur Reihenfolge oder Parallelität ausdrücken, aber keine
  optionalen Alternativpfade, die Rätsel auslassen.
- Vor dem Ende müssen alle als `progression` modellierten Rätsel lösbar
  geworden sein.
- Token-Abhängigkeiten dürfen keine Softlocks erzeugen, z. B. indem ein
  benötigter Token nur hinter dem eigenen Rätsel oder hinter einem
  unerreichbaren Pfad liegt.

## 9. riddles

Ein Rätsel beschreibt Aufgabe, Parameter, benötigte Tokens, produzierte Tokens,
optionale Ressourcen, optionale Hinweise und optionale Assets.

```json
{
  "id": "r_pc_login",
  "type": "input",
  "title": "PC Login",
  "designRole": "progression",
  "difficulty": "easy",
  "estimatedMinutes": 5,
  "playerFacingTask": "Loggt euch am Labor-PC ein.",
  "requiresTokens": ["token_power_on", "token_login_credentials_found"],
  "producesTokens": ["token_pc_logged_in"],
  "assetIds": [],
  "resources": [],
  "hints": [],
  "parameters": {
    "surfaceId": "s_main_computer",
    "slotType": "computer_login_slot",
    "inputMode": "credentials"
  }
}
```

V0-Werte für `designRole`:

- `progression`: schaltet Spielfortschritt frei.
- `clue`: liefert einen Hinweis, Code, Gegenstand oder Kontext für andere
  Rätsel.
- `story`: liefert Story-Kontext.
- `support`: hilft beim Verstehen oder Navigieren.

V0 erzeugt keine eigenständigen Decoy-Rätsel. Irreführende Inhalte bleiben
als `resource.purpose=decoy`, falsche Auswahloption oder falsches Item erlaubt.
Das vermeidet optionale Progressionspfade und hält die Softlock-Prüfung
einfacher.

V0-Werte für `difficulty`:

- `easy`
- `medium`
- `hard`

## 10. Allgemeine V0-Rätseltypen

Die erste Iteration nutzt möglichst allgemeine Kategorien. Fachliche Bedeutung
liegt in Titel, Text, Assets und Parametern, nicht im Typnamen.

| Typ | Bedeutung | The-Last-Hour-Beispiel |
|---|---|---|
| `collection` | Hinweis, Item oder Reward in Welt, Container oder Minigame finden. | Schreibtisch-Notiz, Papierkorb-Hinweis, USB-Fund. |
| `input` | Eine oder mehrere Eingaben gegen definierte Lösungen prüfen. Deckt Codes, Passwörter, Login und Decoding ab. | PC-Login, Keypad, Binary/ASCII-Code, Morse-Code. |
| `choice` | Eine oder mehrere Optionen auswählen, bewerten oder zuordnen. | Vertrauenswürdige E-Mail/URL erkennen. |
| `item_use` | Bestimmtes Inventar-Item an einem Ziel verwenden. | Richtigen USB-Stick in PC stecken. |
| `assembly` | Fragmente, Schritte oder Teile zusammensetzen oder ordnen. | Final-Code-Bildfragmente zusammensetzen. |
| `state_change` | Einfache Weltinteraktion ohne Lösungseingabe, die einen Zustand ändert. | Versteckten Stromschalter aktivieren. |
| `control_panel` | Wiederverwendbare UI mit mehreren Controls, z. B. Buttons, Toggles, Textfeldern. | Licht, Türen, AC, Vent-Verbindung. |

`state_change` bleibt trotz weniger aktueller Beispiele als eigene Kategorie.
Der Grund ist die klare technische Grenze: Ein `input` prüft eine vom Spieler
eingegebene Lösung. Ein `state_change` ist eine direkte Weltaktion, deren
Interaktion selbst das Ereignis ist. Das deckt später Schalter, Hebel,
Druckplatten, Stromkreise, bewegliche Objekte oder einfache Trigger ab, ohne sie
künstlich als Code-/Text-Eingabe zu modellieren.

### 10.1 Typ-Spezifische Parameter

Die detaillierten Pflicht- und Optionalparameter der einzelnen Rätseltypen
stehen in [`parameter-table-v0.md`](parameter-table-v0.md). Diese Spezifikation
hält nur den gemeinsamen Authoring-Contract fest:

- Alle Rätsel enthalten ein `parameters`-Objekt.
- Alle Rätsel enthalten ein `resources`-Array. Wenn es keine Ressourcen gibt,
  ist es leer.
- `resources` beschreiben normale Hinweise, Kontext, Anleitungen oder Decoys
  im Raum. Sie erzeugen keine Graph-Tokens.
- Hints sind optionale Zusatzhilfen und stehen immer in einem `hints`-Array.
- `successEffect` ist ein kontrollierter Effekt nach erfolgreicher Lösung,
  kein Freitext.
- Graph-Tokens entstehen nur über `riddle.producesTokens`.

Diese Trennung hält `deer-json-spec.md` als Strukturvertrag lesbar und nutzt die
Parameter-Tabelle als Detailquelle für UI und Validatoren.

## 11. assets

Assets werden nicht binär in `deer.json` gespeichert. `deer.json` referenziert
Dateien innerhalb des Pakets.

```json
{
  "id": "asset_note_password_1",
  "path": "assets/custom/note-password-1.png",
  "mediaType": "image/png",
  "purpose": "riddle_evidence",
  "linkedTo": ["r_find_login_note"],
  "required": true,
  "source": {
    "type": "educator_upload",
    "license": "own_material"
  }
}
```

V0-Werte für `purpose`:

- `riddle_evidence`
- `lore`
- `feedback`
- `decorative`
- `audio_cue`

V0-Medien:

- `image/png`
- `image/jpeg`
- `audio/wav`
- `audio/mp3`
- `audio/ogg`
- `text/plain`

Validierung:

- `path` muss relativ zum Paket sein.
- Pfade dürfen nicht aus dem Paket herauszeigen.
- Required Assets müssen im Paket existieren.
- PDFs und Office-Dateien sind in V0 nicht direkt runtime-fähig.

## 12. V0-Export Und Generator-Handoff

`deer.json` beschreibt, was der Escape Room sein soll. Die Web-App erzeugt
eine valide Datei und packt sie mit den referenzierten Custom Assets in ein
herunterladbares DEER-Authoring-Bundle `deer.zip`.

Der Java-Generator wird in V0 manuell mit diesem Bundle oder dem entpackten
Projektordner gestartet. Das Einlesen bestehender `deer.zip`-Pakete in den
Wizard ist nicht Teil von V0.

`deer.zip` ist kein Runtime-Paket. Runtime-Dateien und das spielbare
Room-Paket entstehen erst im Generator.

Technische Angaben für einen konkreten Generatorlauf gehören nicht in
`deer.json`, weil sie nicht zum Authoring-Modell des Raums gehören.

## 13. Harte Validierungen

Der Wizard darf kein `deer.zip`-Bundle erstellen, wenn dadurch ein
game-breaking Raum beschrieben würde. Der Generator muss dieselben harten
Regeln beim manuellen Start erneut prüfen. Blockierend sind besonders
Softlocks, unerreichbare Progression, ungewollte Skips und fehlende
Pflichtdaten.

Die Bundle-Erstellung wird blockiert, wenn:

- `formatVersion` unbekannt ist,
- Pflichtfelder fehlen,
- IDs doppelt sind,
- Referenzen ins Leere zeigen,
- `surfaceId` auf keine existierende Surface zeigt,
- ein Rätsel einen unbekannten Typ nutzt,
- ein Rätsel eine nicht erlaubte `designRole` nutzt,
- ein Rätsel kein `resources`-Array hat,
- ein `input` keinen passenden `inputMode` oder keine Lösungsdefinition hat,
- ein `choice` keinen passenden `selectionMode` oder keine Optionen hat,
- ein `control_panel` keine Controls hat,
- ein Graphknoten nicht erreichbar ist,
- ein Rätselknoten nicht auf einem durchspielbaren Pfad zum Ende liegt,
- ein Endknoten nicht erreichbar ist,
- ein Branch ein Progressionsrätsel optional oder überspringbar macht,
- eine Edge ein nie erzeugbares Token verlangt,
- eine Token-Abhängigkeit zyklisch oder in der aktuellen Graphstruktur
  unerfüllbar ist,
- ein `progression`-Rätsel weder Token noch explizite Weltaktion erzeugt,
- ein required Asset fehlt,
- ein Assettyp nicht unterstützt wird,
- eine `resource` ein nicht existierendes Asset referenziert,
- eine Hint-Freischaltung eine nicht existierende Surface, Ressource oder ein
  nicht existierendes Rätsel referenziert,
- eine `resource` Tokens erzeugen will,
- ein `control_panel`-Control Graph-Tokens erzeugen will,
- `scenario.themeId` nicht `default` ist.

Der Wizard sollte diese Fehler schon im Client verhindern. Der Generator muss
sie trotzdem erneut prüfen, weil `deer.json` aus dem Bundle entnommen und
manuell verändert werden kann und weil Client-Fehler nicht zu kaputten Escape
Rooms führen dürfen.

### 13.1 Validierungszeitpunkte

Die normale Nutzererfahrung soll nicht sein, dass Lehrende erst in einem
späteren Generatorlauf von einem Fehler erfahren. Der Client ist deshalb die
primäre
Validierungsoberfläche.

V0-Validierung läuft in drei Stufen:

1. **Step-Validierung:** Jeder Wizard-Schritt verhindert fehlende Pflichtfelder
   und ungültige lokale Eingaben, bevor der nächste Schritt abgeschlossen
   wird.
2. **Live-Graph-Validierung:** Der Rätselgraph prüft laufend Erreichbarkeit,
   Token-Referenzen, optionale Pfade und offensichtliche Softlocks.
3. **Bundle-Preflight:** Der Bundle-erstellen-Button ist nur aktiv, wenn
   Schema, Graph, Pflichtparameter und Asset-Referenzen gültig sind.

Der Java-Generator führt dieselben harten Validierungen erneut aus. Das ist
kein Ersatz für Client-Validierung, sondern ein Sicherheitsnetz für manuell
veränderte `deer.json`-Dateien und für Fehler im Wizard-Client.

## 14. Warnungen

Der Wizard sollte warnen, aber nicht zwingend blockieren, wenn:

- sehr lange Texte als Lore oder Rätseltext genutzt werden,
- ein schwieriges Rätsel keine Hints hat,
- ein Rätsel keine Tokens erzeugt und nicht als `story` oder `support`
  markiert ist,
- ein Rätsel nur dekorative Assets nutzt,
- ein Asset nicht mit einem Rätsel oder Story-Element verknüpft ist,
- die geschätzte Dauer stark vom Zeitlimit abweicht,
- sehr viele Rätsel in einer strikt linearen Kette liegen.

## 15. V0-UI-Bausteinpalette

Der UI-Prototyp darf alle aktuell aus The Last Hour ableitbaren Bausteine
anbieten:

- `state_change`
- `collection`
- `input`
- `choice`
- `item_use`
- `assembly`
- `control_panel`

Diese Liste beschreibt die Authoring-Sicht. Welche Bausteine der Java-Generator
zuerst vollständig spielbar umsetzt, ist eine nachgelagerte technische
Planungsfrage und nicht Teil der UI-Definition.

## 16. Nachgelagerte Technische Fragen

Diese Punkte müssen nicht vor dem UI-Prototyp entschieden werden:

1. Wie stark `control_panel` in der Runtime frei konfigurierbar wird.
2. Welche `inputMode`-Werte der Generator zuerst komplett spielbar macht.
3. Wie `choice`, `item_use`, `assembly` und `state_change` intern in Dungeon-
   Systeme übersetzt werden.
