# deer.json Spezifikation

Status: 0.1-draft V0-Contract
Stand: 07.07.2026
Scope: Foundation-Slice, ein spielbarer Escape-Room-Level, ein Standard-Theme

## 1. Rolle Von deer.json

`deer.json` ist die interne editierbare Quelle des Wizards und der Contract zum
manuell gestarteten Java-Generator. Der Wizard liest und schreibt diese Datei.
Der Generator darf daraus Runtime-Dateien, Petri-Netze, technische Tokens und
das spielbare Room-Paket ableiten, aber `deer.json` bleibt das
Authoring-Modell.

Für Lehrende ist `deer.json` nicht direkt sichtbar. Die sichtbare
Abschlussaktion ist `Entwurf finalisieren`. Danach liegt ein Projektordner mit
`deer.json` und referenzierten Assets vor, den der Java-Generator manuell
verarbeitet.

V0 beschreibt:

```text
Raum-Metadaten
-> Spielsitzung
-> Standard-Szenario
-> Oberflächen
-> Rätselgraph
-> Foundation-Rätsel mit Parametern
-> Asset-Referenzen
```

Die typ-spezifischen Pflichtparameter stehen in
[`parameter-table-v0.md`](parameter-table-v0.md). Der Projektordner für den
Generator steht in [`generator-input-format.md`](generator-input-format.md).

Contract-Grenzen:

- `deer.json` beschreibt den Raum als Authoring-Modell, nicht als Runtime-
  oder Generator-Output.
- Assets werden referenziert, nicht binär in JSON eingebettet.
- Runtime-Tokens, Petri-Netze, Generatorparameter und Paketierung entstehen
  außerhalb von `deer.json`.
- Ein Raum hat genau einen Endzustand.

## 2. Top-Level Struktur

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
| `surfaces` | Vom Wizard abgeleitete Interaktionsorte. |
| `riddleGraph` | Progression und Abhängigkeiten. |
| `riddles` | Konkrete Rätseldefinitionen. |
| `assets` | Referenzen auf Projektordner-Assets. |

## 3. ID-Konventionen

Alle IDs sollen stabil und menschenlesbar sein.

Empfehlung:

```text
lower_snake_case
```

Beispiele:

- `r_find_keypad_note`
- `n_storage_keypad`
- `asset_password_note_1`
- `s_storage_door`

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
  "description": "Beispielkonfiguration für den Foundation-Slice.",
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
- Der Raum ist immer kooperativ. Deshalb gibt es kein `collaborationMode`.
- `time.limitMode=hard`: Nach Ablauf endet der Raum.
- `time.limitMode=soft`: Nach Ablauf läuft der Raum weiter, aber Hinweise oder
  Unterstützung dürfen stärker werden.

## 6. scenario

V0 nutzt genau ein Standard-Theme. Das Feld `themeId` bleibt im JSON, damit
spätere Versionen erweiterbar sind.

```json
{
  "themeId": "default",
  "playerRole": "Untersuchungsteam",
  "premise": "Ein Labor ist verriegelt. Die Gruppe muss Hinweise rekonstruieren und einen Ausgang freischalten.",
  "mission": "Findet den Zugangscode und öffnet die Ausgangstür.",
  "introText": "Der Alarm ist aktiv. Auf dem Wandtimer laufen 30 Minuten herunter.",
  "successText": "Die Ausgangstür öffnet sich.",
  "failureText": "Die Zeit ist abgelaufen."
}
```

V0-Regeln:

- `themeId` ist vorerst immer `default`.
- The Last Hour liefert Bausteine, aber keine vorausgewählte Raumstruktur.
- Custom Assets dürfen Inhalte ergänzen, aber das Theme nicht ersetzen.
- Storytexte sollten kurz bleiben.

## 7. surfaces

`surfaces` ist ein internes Register der Interaktionsorte im Raum. Lehrende
sollen diese Liste nicht technisch pflegen. Die UI leitet sie aus den gewählten
Bausteinen ab und erlaubt fachliche Benennung.

```json
[
  {
    "id": "s_storage_keypad",
    "kind": "keypad",
    "title": "Storage-Keypad"
  },
  {
    "id": "s_storage_door",
    "kind": "door",
    "title": "Storage-Tür"
  }
]
```

Aktive V0-Werte für `kind`:

- `world`
- `container`
- `keypad`
- `door`

Spätere Werte wie `computer`, `control_panel` oder `assembly_area` werden erst
Schema-Teil, wenn der Generator die dazugehörigen Bausteine unterstützt.

## 8. riddleGraph

Der Rätselgraph beschreibt Progression, nicht Raumgeometrie. Er ist die einzige
öffentliche Quelle für Abhängigkeiten. Der Generator kann daraus später
Petri-Net-Strukturen, Trigger oder Runtime-States ableiten.

```json
{
  "startNodeId": "n_start",
  "endNodeId": "n_exit_open",
  "nodes": [
    {
      "id": "n_start",
      "kind": "start",
      "title": "Start"
    },
    {
      "id": "n_storage_keypad",
      "kind": "riddle",
      "title": "Storage-Keypad",
      "riddleId": "r_storage_keypad"
    }
  ],
  "edges": [
    {
      "id": "e_start_to_keypad",
      "from": "n_start",
      "to": "n_storage_keypad"
    }
  ]
}
```

Node-Arten für V0:

- `start`
- `riddle`
- `end`

Edge-Semantik:

- Eine Kante vom Startknoten macht das Ziel zu Beginn verfügbar.
- Eine Kante von einem Rätselknoten macht das Ziel verfügbar, nachdem das
  Quellrätsel abgeschlossen wurde.
- Hat ein Ziel mehrere eingehende Kanten von Rätselknoten, müssen alle diese
  Vorgänger abgeschlossen sein.
- Bedingungen werden nicht zusätzlich auf der Kante kodiert. Die Topologie ist
  die einzige öffentliche Quelle für Reihenfolge und Abhängigkeiten.

Validierung:

- Genau ein `startNodeId`.
- Genau ein `endNodeId`.
- Genau ein Graphknoten mit `kind=end`; er muss `endNodeId` entsprechen.
- Alle Rätsel-Nodes referenzieren ein existierendes `riddleId`.
- Der Endknoten muss vom Start erreichbar sein.
- Standard ist ein azyklischer Graph. Retry-Verhalten gehört in das jeweilige
  Rätsel, nicht als Graphzyklus.
- Jeder Rätselknoten muss erreichbar sein und auf einem durchspielbaren Pfad
  zum Ende liegen.
- Branches dürfen nur Reihenfolge oder Parallelität ausdrücken, aber keine
  optionalen Alternativpfade, die Rätsel auslassen.
- Auch nach V0 bleibt das Authoring-Modell auf genau ein Ende ausgelegt.
  Alternative Pfade dürfen später auf denselben Endzustand zulaufen, aber nicht
  mehrere Enden erzeugen.

## 9. riddles

Ein Rätsel beschreibt Aufgabe, Parameter, optionale Ressourcen, optionale
Hinweise und optionale Assets. Es beschreibt keine Progression; diese liegt im
Graph.

```json
{
  "id": "r_storage_keypad",
  "type": "input",
  "title": "Storage-Keypad",
  "difficulty": "easy",
  "estimatedMinutes": 5,
  "playerFacingTask": "Gebt den gefundenen Code am Keypad ein.",
  "assetIds": [],
  "resources": [],
  "hints": [],
  "parameters": {
    "surfaceId": "s_storage_keypad",
    "slotType": "keypad_slot",
    "inputMode": "numeric",
    "answer": "3758",
    "maxLength": 4,
    "successEffect": {
      "type": "open_surface",
      "surfaceId": "s_storage_door"
    }
  }
}
```

Aktive V0-Typen:

| Typ | Bedeutung |
|---|---|
| `collection` | Hinweis oder Ressource in Welt oder Container finden. |
| `input` | Eine numerische Eingabe gegen eine definierte Lösung prüfen. |

UI-Bausteinnamen wie `collection.single` und `input.numeric` sind keine Werte
von `riddle.type`. Die Suffixe beschreiben den Modus in `parameters`, z. B.
`rewardMode=find_resource` oder `inputMode=numeric`.

Optionale Redaktionsmetadaten:

- `difficulty`: `easy`, `medium`, `hard`
- `estimatedMinutes`: positive Zahl

Diese Felder dürfen Warnungen unterstützen, sind aber kein harter
Generator-Contract.

### 9.1 Typ-Spezifische Parameter

Die detaillierten Pflicht- und Optionalparameter stehen in
[`parameter-table-v0.md`](parameter-table-v0.md). Diese Spezifikation hält nur
den gemeinsamen Authoring-Contract fest:

- Alle Rätsel enthalten ein `parameters`-Objekt.
- Das `parameters`-Objekt ist pro aktivem Rätseltyp geschlossen. Typfremde
  Felder werden vom Schema abgelehnt.
- Alle Rätsel enthalten ein `resources`-Array. Wenn es keine Ressourcen gibt,
  ist es leer.
- `resources` beschreiben normale Hinweise, Kontext, Anleitungen oder Decoys
  im Raum. Sie erzeugen keine Progression.
- Hints sind optionale Zusatzhilfen und stehen immer in einem `hints`-Array.
- `successEffect` ist ein kontrollierter Effekt nach erfolgreicher Lösung,
  wenn der konkrete Rätseltyp eine Weltänderung braucht. Bei
  `collection.single` ist `parameters.resourceIds` bereits das Ergebnis; dafür
  wird kein zusätzlicher Resource-Grant-Effekt kodiert.

## 10. resources

Resources werden im Raum oder in UI-Oberflächen sichtbar. Sie sind Teil des
Authoring-Modells, aber keine Hints.

V0-Werte für `kind`:

- `inline_text`
- `asset`
- `world_object`
- `computer_file`

V0-Werte für `availability`:

- `visible_in_level`
- `inside_container`
- `after_riddle`
- `generated_by_riddle`

Bei `availability=after_riddle` muss `requiredRiddleId` gesetzt sein.

## 11. hints

Hints bleiben optional. Ohne `unlock` sind sie sofort verfügbar.

Wenn ein Hint nicht sofort verfügbar sein soll, nutzt `unlock` immer diesen
Envelope:

```json
{
  "operator": "any_of",
  "conditions": [
    {
      "type": "elapsed_time",
      "seconds": 300
    }
  ]
}
```

V0-Freischaltungen:

- `elapsed_time`
- `failed_attempts`
- `riddle_completed`

Komplexere Freischaltungen wie besuchte Oberflächen oder gesehene Ressourcen
folgen erst, wenn die Runtime diese Signale zuverlässig liefert.

## 12. assets

Assets werden nicht binär in `deer.json` gespeichert. `deer.json` referenziert
Dateien innerhalb des Projektordners.

```json
{
  "id": "asset_note_password_1",
  "path": "assets/custom/note-password-1.png",
  "mediaType": "image/png",
  "purpose": "riddle_evidence",
  "linkedTo": ["r_find_keypad_note"],
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

- `path` muss relativ zum Projektordner sein.
- Pfade dürfen nicht aus dem Projektordner herauszeigen.
- Required Assets müssen im Projektordner existieren.
- PDFs und Office-Dateien sind in V0 nicht direkt runtime-fähig.

## 13. V0-Generator-Handoff

`deer.json` beschreibt, was der Escape Room sein soll. Die Web-App erzeugt
eine valide Datei und referenziert Custom Assets im Projektordner.

Der Java-Generator wird in V0 manuell mit diesem Projektordner gestartet. Das
Einlesen bestehender Generator-Pakete in den Wizard ist nicht Teil von V0.

Technische Angaben für einen konkreten Generatorlauf gehören nicht in
`deer.json`, weil sie nicht zum Authoring-Modell des Raums gehören.

## 14. Harte Validierungen

Der Wizard darf den Entwurf nicht finalisieren, wenn dadurch ein
game-breaking Raum beschrieben würde. Der Generator muss dieselben harten
Regeln beim manuellen Start erneut prüfen. Blockierend sind besonders
Softlocks, unerreichbare Progression, ungewollte Skips und fehlende
Pflichtdaten.

Die Finalisierung wird blockiert, wenn:

- `formatVersion` unbekannt ist,
- Pflichtfelder fehlen,
- IDs doppelt sind,
- Referenzen ins Leere zeigen,
- `surfaceId` auf keine existierende Surface zeigt,
- ein Rätsel einen unbekannten Typ nutzt,
- ein Rätsel kein `resources`-Array hat,
- ein `input` nicht `inputMode=numeric` nutzt,
- ein Graphknoten nicht erreichbar ist,
- ein Rätselknoten nicht auf einem durchspielbaren Pfad zum Ende liegt,
- der Endknoten nicht erreichbar ist,
- ein Branch ein Progressionsrätsel optional oder überspringbar macht,
- eine Abhängigkeit zyklisch oder in der aktuellen Graphstruktur unerfüllbar
  ist,
- ein required Asset fehlt,
- ein Assettyp nicht unterstützt wird,
- eine `resource` ein nicht existierendes Asset referenziert,
- eine Hint-Freischaltung ein nicht existierendes Rätsel referenziert,
- `scenario.themeId` nicht `default` ist.

### 14.1 Validierungszeitpunkte

Die normale Nutzererfahrung soll nicht sein, dass Lehrende erst in einem
späteren Generatorlauf von einem Fehler erfahren. Der Client ist deshalb die
primäre Validierungsoberfläche.

V0-Validierung läuft in drei Stufen:

1. **Step-Validierung:** Jeder Wizard-Schritt verhindert fehlende Pflichtfelder
   und ungültige lokale Eingaben.
2. **Live-Graph-Validierung:** Der Rätselgraph prüft laufend Erreichbarkeit,
   Referenzen, optionale Pfade und offensichtliche Softlocks.
3. **Finalisierungs-Preflight:** Der Abschluss ist nur aktiv, wenn Schema,
   Graph, Pflichtparameter und Asset-Referenzen gültig sind.

Der Java-Generator führt dieselben harten Validierungen erneut aus. Das ist
kein Ersatz für Client-Validierung, sondern ein Sicherheitsnetz für manuell
veränderte Projektdateien und Fehler im Wizard-Client.

## 15. Warnungen

Der Wizard sollte warnen, aber nicht zwingend blockieren, wenn:

- sehr lange Texte als Lore oder Rätseltext genutzt werden,
- ein schwieriges Rätsel keine Hints hat,
- ein Rätsel nur dekorative Assets nutzt,
- ein Asset nicht mit einem Rätsel oder Story-Element verknüpft ist,
- die geschätzte Dauer stark vom Zeitlimit abweicht,
- sehr viele Rätsel in einer strikt linearen Kette liegen.

## 16. Nachgelagerte Technische Fragen

Diese Punkte müssen nicht vor dem Foundation-Slice entschieden werden:

1. Wie stark `control_panel` in der Runtime frei konfigurierbar wird.
2. Welche Computer-, Login-, Choice- und Item-Use-Bausteine zuerst spielbar
   werden.
3. Wie `choice`, `item_use`, `assembly` und `state_change` intern in Dungeon-
   Systeme übersetzt werden.
