# deer.json Spezifikation

Status: vierter Diskussionsdraft  
Stand: 29.06.2026  
Scope: V0, ein spielbarer Escape-Room-Level, ein Standard-Theme

## 1. Rolle Von deer.json

`deer.json` ist die editierbare Quelle des Wizards. Der Wizard liest und
schreibt diese Datei. Der Generator darf daraus Runtime-Dateien ableiten, aber
`deer.json` bleibt das Authoring-Modell.

V0 beschreibt:

```text
Raum-Metadaten
-> Spielsitzung
-> Standard-Szenario
-> Raetselgraph
-> Raetsel mit Parametern
-> Assets
-> Generator-Constraints
```

Ein erster Vorschlag fuer die typ-spezifischen Pflichtparameter steht in
[`parameter-table-v0.md`](parameter-table-v0.md).

Der UI-orientierte Wizard-Ablauf und die von Lehrenden auszufuellenden Felder
stehen in [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md).

Explizit nicht V0:

- Lernziele im Format,
- Evaluation, ob Lernziele erreicht wurden,
- Telemetrieprofile,
- Debriefing-Fragen,
- automatisch generierte Pre-/Post-Tests,
- mehrere Themes,
- Custom-Themes, Tilesets, Sprites oder UI-Skins,
- binaere Assets direkt in JSON,
- generierte `.level`-Dateien.

## 2. Top-Level Struktur

V0-Draft:

```json
{
  "formatVersion": "0.4.0-draft",
  "metadata": {},
  "session": {},
  "scenario": {},
  "riddleGraph": {},
  "riddles": [],
  "assets": [],
  "generation": {}
}
```

Pflichtfelder:

| Feld | Zweck |
|---|---|
| `formatVersion` | Version des Authoring-Formats. |
| `metadata` | Titel, ID, Sprache, Autor, Kurzbeschreibung. |
| `session` | Zielgruppe, Spieleranzahl, Dauer, Kollaborationsmodus. |
| `scenario` | Standard-Theme, Story-Rahmen, Intro/Outro, Timer. |
| `riddleGraph` | Progression und Abhaengigkeiten. |
| `riddles` | Konkrete Raetseldefinitionen. |
| `assets` | Referenzen auf Paket-Assets. |
| `generation` | Seed und technische Generator-Grenzen. |

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

- IDs muessen innerhalb ihres Bereichs eindeutig sein.
- Referenzen muessen auf existierende IDs zeigen.
- IDs sollten nicht automatisch aus Titeln neu erzeugt werden, sobald andere
  Elemente darauf verweisen.

## 4. metadata

Beschreibt das Authoring-Artefakt.

```json
{
  "id": "wizard_example_v0",
  "title": "Wizard Beispielraum V0",
  "locale": "de-DE",
  "description": "Beispielkonfiguration mit den aktuell verfuegbaren V0-Bausteinen.",
  "author": {
    "name": "Beispiel Lehrkraft",
    "organization": "FH Beispiel"
  }
}
```

Pflicht in V0:

- `id`
- `title`
- `locale`

## 5. session

Beschreibt Unterrichts- und Spielsitzung, aber ohne Lernzielmodell.

```json
{
  "targetAudience": "Lernende im Bereich IT-Sicherheit",
  "priorKnowledge": "Grundlagen zu E-Mail, Webseiten und einfachen Codierungen.",
  "playerCount": {
    "min": 1,
    "max": 4,
    "recommended": 2
  },
  "durationMinutes": 60,
  "timeLimitMinutes": 60,
  "collaborationMode": "cooperative"
}
```

V0-Werte fuer `collaborationMode`:

- `single_player`
- `cooperative`

## 6. scenario

V0 nutzt genau ein Standard-Theme. Das Feld `themeId` bleibt trotzdem im JSON,
damit spaetere Versionen erweiterbar sind. In V0 sollte der Wizard dieses Feld
nicht als grosse Theme-Auswahl verkaufen.

```json
{
  "themeId": "default",
  "playerRole": "Untersuchungsteam",
  "premise": "Ein Labor ist verriegelt. Die Gruppe muss Hinweise rekonstruieren und einen Ausgang freischalten.",
  "mission": "Findet den finalen Zugangscode und oeffnet die Ausgangstuer.",
  "introText": "Der Alarm ist aktiv. Auf dem Wandtimer laufen 60 Minuten herunter.",
  "successText": "Die Ausgangstuer oeffnet sich.",
  "failureText": "Die Zeit ist abgelaufen."
}
```

V0-Regeln:

- `themeId` ist vorerst immer `default`.
- Es gibt kein Vorlagenfeld in V0. The Last Hour liefert Bausteine, aber keine
  vorausgewaehlte Raumstruktur.
- Custom Assets duerfen Inhalte ergaenzen, aber das Theme nicht ersetzen.
- Storytexte sollten kurz bleiben.

## 7. riddleGraph

Der Raetselgraph beschreibt Progression, nicht Raumgeometrie. Er ist die
Authoring-Sicht auf Abhaengigkeiten. Der Generator kann daraus spaeter
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

Node-Arten fuer V0:

- `start`
- `riddle`
- `event`
- `end`

Edge-Conditions fuer V0:

- `always`
- `all_of_tokens`
- `any_of_tokens`

Tokens sind fuer V0 bewusst Teil des Formats. Sie sind fuer Lehrende im UI
versteckbar, aber fuer Generator, Validierung und Petri-Net-Ableitung nuetzlich.

Validierung:

- Genau ein `startNodeId`.
- Mindestens ein `endNodeId`.
- Alle Raetsel-Nodes referenzieren ein existierendes `riddleId`.
- Jeder Endknoten muss vom Start erreichbar sein.
- Tokens in Edge-Conditions muessen von vorher erreichbaren Raetseln erzeugt
  werden koennen.
- Standard ist ein azyklischer Graph. Retry-Verhalten gehoert in das jeweilige
  Raetsel, nicht als Graphzyklus.
- V0 erzeugt keine optionalen Raetsel: Jeder Raetselknoten muss erreichbar sein
  und auf einem durchspielbaren Pfad zum Ende liegen.
- Branches duerfen nur Reihenfolge oder Parallelitaet ausdruecken, aber keine
  optionalen Alternativpfade, die Raetsel auslassen.
- Vor dem Ende muessen alle als `progression` modellierten Raetsel loesbar
  geworden sein.
- Token-Abhaengigkeiten duerfen keine Softlocks erzeugen, z. B. indem ein
  benoetigter Token nur hinter dem eigenen Raetsel oder hinter einem
  unerreichbaren Pfad liegt.

## 8. riddles

Ein Raetsel beschreibt Aufgabe, Parameter, benoetigte Tokens, produzierte Tokens,
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
    "inputMode": "credentials"
  }
}
```

V0-Werte fuer `designRole`:

- `progression`: schaltet Spielfortschritt frei.
- `clue`: liefert einen Hinweis, Code, Gegenstand oder Kontext fuer andere
  Raetsel.
- `story`: liefert Story-Kontext.
- `decoy`: ist absichtlich irrefuehrend oder optional.
- `support`: hilft beim Verstehen oder Navigieren.

V0-Werte fuer `difficulty`:

- `easy`
- `medium`
- `hard`

## 9. Allgemeine V0-Raetseltypen

Die erste Iteration nutzt moeglichst allgemeine Kategorien. Fachliche Bedeutung
liegt in Titel, Text, Assets und Parametern, nicht im Typnamen.

| Typ | Bedeutung | The-Last-Hour-Beispiel |
|---|---|---|
| `collection` | Hinweis, Item oder Reward in Welt, Container oder Minigame finden. | Schreibtisch-Notiz, Papierkorb-Hinweis, USB-Fund. |
| `input` | Eine oder mehrere Eingaben gegen definierte Loesungen pruefen. Deckt Codes, Passwoerter, Login und Decoding ab. | PC-Login, Keypad, Binary/ASCII-Code, Morse-Code. |
| `choice` | Eine oder mehrere Optionen auswaehlen, bewerten oder zuordnen. | Vertrauenswuerdige E-Mail/URL erkennen. |
| `item_use` | Bestimmtes Inventar-Item an einem Ziel verwenden. | Richtigen USB-Stick in PC stecken. |
| `assembly` | Fragmente, Schritte oder Teile zusammensetzen oder ordnen. | Final-Code-Bildfragmente zusammensetzen. |
| `state_change` | Einfache Weltinteraktion ohne Loesungseingabe, die einen Zustand aendert. | Versteckten Stromschalter aktivieren. |
| `control_panel` | Wiederverwendbare UI mit mehreren Controls, z. B. Buttons, Toggles, Textfeldern. | Licht, Tueren, AC, Vent-Verbindung. |

`state_change` bleibt trotz weniger aktueller Beispiele als eigene Kategorie.
Der Grund ist die klare technische Grenze: Ein `input` prueft eine vom Spieler
eingegebene Loesung. Ein `state_change` ist eine direkte Weltaktion, deren
Interaktion selbst das Ereignis ist. Das deckt spaeter Schalter, Hebel,
Druckplatten, Stromkreise, bewegliche Objekte oder einfache Trigger ab, ohne sie
kuenstlich als Code-/Text-Eingabe zu modellieren.

### 9.1 Warum Diese Zusammenfassung?

Die vorherigen Typen waren zu nah an The Last Hour:

```text
credential_login -> input
decode_input -> input
message_trust -> choice
image_fragment_puzzle -> assembly
search_reward -> collection
inspect_content -> resources
item_gate -> item_use
```

`control_panel` bleibt bewusst separat. Es ist nicht nur eine einzelne Eingabe,
sondern eine zusammengesetzte Interaktionsoberflaeche mit mehreren Controls und
Weltzustandsaenderungen. Das ist wiederverwendbar genug, um einen eigenen Typ zu
rechtfertigen.

`inspect_content` wird nicht mehr als eigener Raetseltyp gefuehrt. Solche
Inhalte sind Ressourcen eines Raetsels, weil sie meistens ein anderes Raetsel
vorbereiten oder stuetzen.

Alle Raetsel enthalten immer ein `resources`-Array. Wenn ein Raetsel keine
Ressourcen hat, ist es ein leeres Array. Dadurch bleibt die Struktur fuer Wizard,
Generator und spaetere Validierung stabil.

### 9.2 input

`input` bleibt breit, muss aber ueber `parameters.inputMode` eingegrenzt werden.
Dadurch bleibt die Kategorie allgemein, ohne dass Keypads, Login-Dialoge und
Decoding-Felder unklar werden.

V0-Werte fuer `inputMode`:

- `numeric`: Zahlenfeld, z. B. Keypad. Erwartet `answer`, `maxLength`.
- `text`: kurzes Textfeld. Erwartet `answer`, optional `maxLength`.
- `credentials`: mehrere benannte Felder. Erwartet `fields`.
- `decoded_text`: Decoding-Aufgabe. Erwartet `encodedValue`, `answer`,
  optional `decoderSteps`.

Beispiel Keypad:

```json
{
  "type": "input",
  "parameters": {
    "inputMode": "numeric",
    "answer": "3758",
    "maxLength": 4,
    "successAction": "open_door_storage"
  }
}
```

### 9.3 choice

`choice` deckt einfache Multiple-Choice-, Mehrfachauswahl- und
Zuordnungsaufgaben ab.

V0-Werte fuer `selectionMode`:

- `single_correct`
- `multiple_correct`
- `classification`

Beispiele:

- eine sichere E-Mail aus mehreren E-Mails auswaehlen,
- mehrere passende Gegenstaende markieren,
- Begriffe Kategorien zuordnen.

### 9.4 resources

`resources` sind Informationen, die im Raum, in Dialogen, in Dateien oder als
Asset bereits Teil des Raetseldesigns sind. Sie sind keine Hilfen im Sinne des
Hint-Systems. Eine Ressource kann ein notwendiger Hinweis, Kontext, Decoy oder
eine Anleitung sein.

```json
{
  "id": "res_morse_manual",
  "kind": "asset",
  "title": "Morse-Tabelle",
  "assetId": "asset_morse_manual",
  "availability": "visible_in_level",
  "purpose": "clue"
}
```

V0-Werte fuer `kind`:

- `inline_text`
- `asset`
- `world_object`
- `computer_file`

V0-Werte fuer `availability`:

- `visible_in_level`
- `inside_container`
- `after_token`
- `generated_by_riddle`

V0-Werte fuer `purpose`:

- `clue`
- `context`
- `instruction`
- `decoy`

Ressourcen erzeugen keine Tokens. Graph-Tokens entstehen nur ueber
`riddle.producesTokens`. Wenn das Lesen oder Finden einer Information
Spielfortschritt erzeugen soll, wird diese Aktion als Raetsel modelliert,
meistens als `collection`, `input`, `choice`, `state_change` oder
`control_panel`.

### 9.5 control_panel

`control_panel` ist ein eigener Raetseltyp fuer zusammengesetzte UI-Interaktion.
Ein Panel kann mehrere Controls enthalten. Diese Controls koennen interne
Panel-Zustaende setzen, erzeugen aber keine Graph-Tokens. Graph-Tokens stehen
weiterhin nur auf Raetsel-Ebene in `producesTokens`.

V0-Control-Arten:

- `button`
- `toggle`
- `text_input`
- `password_input`

Beispiel:

```json
{
  "type": "control_panel",
  "producesTokens": ["token_exit_open"],
  "parameters": {
    "controls": [
      {
        "id": "unlock_exit",
        "kind": "password_input",
        "answer": "214795541",
        "setsState": "exit_unlocked"
      },
      {
        "id": "open_exit",
        "kind": "button",
        "requiresStates": ["exit_unlocked"],
        "setsState": "exit_open"
      }
    ],
    "completionState": "exit_open"
  }
}
```

### 9.6 hints

Hints sind in V0 optional und werden erst als Unterstuetzung angezeigt, z. B.
manuell, nach Fehlversuchen oder nach Zeit. Ein Hint ist nicht die normale
Informationsquelle des Raetsels.

```json
{
  "id": "h_pc_login_1",
  "title": "Zwei Notizen",
  "text": "Die Zugangsdaten stehen nicht an einer einzigen Stelle.",
  "level": 1,
  "trigger": "manual_request"
}
```

V0-Werte fuer `trigger`:

- `manual_request`
- `after_failed_attempts`
- `after_time`

## 10. assets

Assets werden nicht binaer in `deer.json` gespeichert. `deer.json` referenziert
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

V0-Werte fuer `purpose`:

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
- Pfade duerfen nicht aus dem Paket herauszeigen.
- Required Assets muessen im Paket existieren.
- PDFs und Office-Dateien sind in V0 nicht direkt runtime-faehig.

## 11. generation

`generation` beschreibt reproduzierbare Generator-Entscheidungen.

```json
{
  "seed": 7341,
  "levelCount": 1,
  "layoutProfile": "default_lab",
  "targetRuntime": "dungeon_escape_room",
  "constraints": {
    "allowBranches": true,
    "allowOptionalRiddles": false,
    "maxRiddles": 12,
    "usePredefinedThemeOnly": true
  }
}
```

V0:

- `levelCount` muss `1` sein.
- `usePredefinedThemeOnly` muss `true` sein.
- `allowOptionalRiddles` muss `false` sein.
- Der gleiche Seed plus gleiche `deer.json` soll das gleiche Paket erzeugen.
- Ein anderer Seed darf das Layout veraendern, aber nicht Raetsel, Loesungen,
  Graph-Abhaengigkeiten oder Durchspielbarkeit.

Die sichtbare Bedienung im ersten Wizard-Prototyp bleibt bewusst kleiner: Die
UI prueft den Entwurf und exportiert eine valide `deer.json`. Der spaetere
Generator konsumiert diese Datei und erzeugt daraus Runtime-Dateien oder ein
Paket.

Eine spielbare Preview und erneute Generatorlaeufe mit anderem Seed sind
nachgelagerte Generator-/Runtime-Funktionen.

## 12. Harte Validierungen

Der Wizard darf `deer.json` nicht exportieren, wenn dadurch ein game-breaking
Raum entstehen wuerde. Der spaetere Generator muss dieselben harten Regeln
erneut pruefen. Blockierend sind besonders Softlocks, unerreichbare Progression,
ungewollte Skips und fehlende Pflichtdaten.

Der Export wird blockiert, wenn:

- `formatVersion` unbekannt ist,
- Pflichtfelder fehlen,
- IDs doppelt sind,
- Referenzen ins Leere zeigen,
- ein Raetsel einen unbekannten Typ nutzt,
- ein Raetsel kein `resources`-Array hat,
- ein `input` keinen passenden `inputMode` oder keine Loesungsdefinition hat,
- ein `choice` keinen passenden `selectionMode` oder keine Optionen hat,
- ein `control_panel` keine Controls hat,
- ein Graphknoten nicht erreichbar ist,
- ein Raetselknoten nicht auf einem durchspielbaren Pfad zum Ende liegt,
- ein Endknoten nicht erreichbar ist,
- ein Branch ein Raetsel optional macht, obwohl `allowOptionalRiddles` false ist,
- eine Edge ein nie erzeugbares Token verlangt,
- eine Token-Abhaengigkeit zyklisch oder in der aktuellen Graphstruktur
  unerfuellbar ist,
- ein `progression`-Raetsel weder Token noch explizite Weltaktion erzeugt,
- ein required Asset fehlt,
- ein Assettyp nicht unterstuetzt wird,
- eine `resource` ein nicht existierendes Asset referenziert,
- eine `resource` Tokens erzeugen will,
- ein `control_panel`-Control Graph-Tokens erzeugen will,
- `scenario.themeId` nicht `default` ist,
- `generation.constraints.allowOptionalRiddles` nicht `false` ist,
- `generation.levelCount` nicht `1` ist.

Der Wizard sollte diese Fehler schon im Client verhindern. Der Generator muss
sie trotzdem erneut pruefen, weil `deer.json` importiert oder manuell editiert
werden kann und weil Client-Fehler nicht zu kaputten Escape Rooms fuehren
duerfen.

### 12.1 Validierungszeitpunkte

Die normale Nutzererfahrung soll nicht sein, dass Lehrende erst in einem
spaeteren Generatorlauf von einem Fehler erfahren. Der Client ist deshalb die
primaere
Validierungsoberflaeche.

V0-Validierung laeuft in drei Stufen:

1. **Step-Validierung:** Jeder Wizard-Schritt verhindert fehlende Pflichtfelder
   und ungueltige lokale Eingaben, bevor der naechste Schritt abgeschlossen
   wird.
2. **Live-Graph-Validierung:** Der Raetselgraph prueft laufend Erreichbarkeit,
   Token-Referenzen, optionale Pfade und offensichtliche Softlocks.
3. **Export-Preflight:** Der JSON-Export ist nur aktiv, wenn Schema, Graph,
   Pflichtparameter und Asset-Referenzen gueltig sind.

Der Java-Generator fuehrt dieselben harten Validierungen erneut aus. Das ist
kein Ersatz fuer Client-Validierung, sondern ein Sicherheitsnetz fuer importierte
oder manuell veraenderte `deer.json`-Dateien und fuer Fehler im Wizard-Client.

## 13. Warnungen

Der Wizard sollte warnen, aber nicht zwingend blockieren, wenn:

- sehr lange Texte als Lore oder Raetseltext genutzt werden,
- ein schwieriges Raetsel keine Hints hat,
- ein Raetsel keine Tokens erzeugt und nicht als `story`, `support` oder
  `decoy` markiert ist,
- ein Raetsel nur dekorative Assets nutzt,
- ein Asset nicht mit einem Raetsel oder Story-Element verknuepft ist,
- die geschaetzte Dauer stark vom Zeitlimit abweicht,
- sehr viele Raetsel in einer strikt linearen Kette liegen.

## 14. V0-UI-Bausteinpalette

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
zuerst vollstaendig spielbar umsetzt, ist eine nachgelagerte technische
Planungsfrage und nicht Teil der UI-Definition.

## 15. Nachgelagerte Technische Fragen

Diese Punkte muessen nicht vor dem UI-Prototyp entschieden werden:

1. Wie stark `control_panel` in der Runtime frei konfigurierbar wird.
2. Welche `inputMode`-Werte der Generator zuerst komplett spielbar macht.
3. Wie `choice`, `item_use`, `assembly` und `state_change` intern in Dungeon-
   Systeme uebersetzt werden.
4. Wie eine spielbare Preview und ein `deer.zip`-Export technisch angebunden
   werden.
