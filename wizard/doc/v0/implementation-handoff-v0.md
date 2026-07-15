# Implementation Handoff V0.2

## Ziel

Der Foundation-Slice beweist einen echten, reproduzierbaren Pfad:

```text
UI-Entwurf
-> valide deer.json + Bildasset
-> Java-Generator
-> generiertes Modul-ZIP
-> Integration in Dungeon
-> Gradle-Build
-> Fund -> Zahlencode -> Ausgang
```

Die UI, der Java-Generator und die Runtime implementieren denselben
sprachunabhängigen Contract, aber nicht „dieselben Klassen“.

## Contract-Reihenfolge

1. [`deer.schema.json`](deer.schema.json)
2. [`deer-json-spec.md`](deer-json-spec.md)
3. [`parameter-table-v0.md`](parameter-table-v0.md)
4. [`examples/deer.example.json`](examples/deer.example.json)
5. [`generator-input-format.md`](generator-input-format.md)
6. [`generator-output-format.md`](generator-output-format.md)

Der sichtbare Flow steht in
[`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md).

## Foundation-Fähigkeiten

- `collection` mit `rewardMode=find_resource`;
- `input` mit `inputMode=numeric` und 1 bis 8 Ziffern;
- mindestens ein Inline-Text und optional PNG/JPEG als Fundinhalt;
- ab Rätselaktivierung nacheinander anforderbare Hilfen;
- geordnete Abschnitte mit AND-Parallelität;
- ein gemeinsamer Ausgang;
- hartes oder weiches Zeitlimit;
- ein bis vier Spielende, serverautoritativ.

Werte außerhalb dieser Liste gehören nicht in Schema oder Picker der
Formatversion `0.2-draft`.

## Architekturgrenzen

### UI

Die UI:

- hält unvollständige Entwürfe in einem privaten Draft-Modell;
- speichert und öffnet lokale Drafts;
- erzeugt stabile IDs und die DEER-Projektion;
- leitet Surfaces und den V0.2-Graphen aus fachlichen Eingaben ab;
- validiert Schema und Fachregeln;
- schreibt inhaltsadressierte Assets zuerst und ersetzt `deer.json` zuletzt;
- startet weder Generator noch Build und erzeugt kein ZIP.

### Java-Generator

Der Generator:

- behandelt den Projektordner als read-only;
- validiert Schema, Semantik, Pfade und Assetinhalt erneut;
- berechnet deterministische Generierungsparameter;
- erzeugt Layout, benannte Punkte, Startpositionen und Runtime-Adapter;
- schreibt Java-Quellcode, `.level`, Assets, Build-Datei, Manifest und Report;
- erstellt erst nach Erfolg das finale Modul-ZIP;
- mutiert weder Eingabe noch Dungeon-Checkout.

### Runtime

Die Runtime braucht einen wiederverwendbaren Foundation-Layer für:

- stabile Rätsel-IDs und serverautoritative Abschlussereignisse;
- Aktivierung nach AND-Vorgängern;
- teamweiten Fundabschluss;
- Keypadabschluss und gemeinsamen Zustand;
- Öffnen des Ausgangs nach dem letzten Abschnitt;
- autoritativen Timer und Erfolgs-/Fehlschlagzustand;
- Snapshot/Spawn-Synchronisation für Reconnect und Multiplayer.

Generierter Code konfiguriert diesen Layer. Er soll nicht für jede Graphkante
individuelle Callback-Ketten erzeugen.

## Reale Engine-Lücken

| Bereich | Ist-Zustand | Foundation-Aufgabe |
|---|---|---|
| Level | `.level` enthält Geometrie und benannte Punkte, aber keine vollständige Rätsellogik | Layoutprofil und generierte `DungeonLevel`-Unterklasse |
| Fund | normale Itemaufnahme liefert kein generisches Rätselabschlussereignis | Resource-/Fund-Adapter mit teamweitem Abschluss |
| Keypad | Factory kennt Code, Callback und Ziffernanzeige | Adapter meldet Abschluss an die gemeinsame Progression |
| Graph | kein generischer Riddle-Graph-Executor | Foundation-Progressionssystem |
| Ende | Türöffnung und `LevelSystem`-Levelerfolg sind getrennte Mechaniken | Ausgangsbindung, neutralisierter Default-Endcallback und eindeutige Completion-Semantik |
| Timer | vorhandene Ablaufreaktion ist nicht allgemein serverautoritativ | gemeinsamer Session-Timer |
| Netzwerk | Custom-State benötigt aktuell szenariospezifische Übersetzung | generischer Foundation-Snapshot/Spawn-Layer |

Die relevanten Ausgangspunkte liegen unter
`dungeon/src/core/level/loader`,
`dungeon/src/contrib/modules/keypad`,
`escapeRoom/src/petriNet`,
`escapeRoom/src/hint` und
`theLastHourEscapeRoom/src/level/LastHourLevel.java`.

## Validierungsvertrag

TypeScript und Java teilen:

- JSON Schema und Formatversion;
- stabile Rule-Codes;
- Severity- und Phase-Werte;
- gültige und ungültige Conformance-Fixtures;
- erwartete Pfade und Entity-IDs;
- deterministische Issue-Sortierung.

Sie teilen keine sprachspezifischen Validator-Klassen.

Normatives Issue-Objekt:

```json
{
  "severity": "error",
  "phase": "graph",
  "code": "GRAPH_RIDDLE_UNREACHABLE",
  "messageKey": "validation.graph.riddle_unreachable",
  "arguments": {
    "title": "Tür-Keypad"
  },
  "path": "/riddleGraph/nodes/2",
  "entity": {
    "kind": "riddle",
    "id": "r_open_exit"
  },
  "relatedPaths": [
    "/riddleGraph/edges/1"
  ]
}
```

Regeln:

- `severity=error` blockiert; `warning` blockiert nicht.
- Ein zusätzliches `blocking`-Feld existiert nicht.
- `code` und `messageKey` sind stabil; sichtbare Texte werden lokalisiert.
- `path` ist ein JSON Pointer auf die DEER-Projektion.
- `entity` verbindet das Issue mit dem UI-Draft.
- Sortierung: Severity, Phase, Pfad, Code.

Mindestcodes:

| Code | Phase |
|---|---|
| `SCHEMA_INVALID` | schema |
| `ID_DUPLICATE` | references |
| `REFERENCE_UNKNOWN` | references |
| `LEARNING_OBJECTIVE_UNKNOWN` | learning |
| `GRAPH_PROFILE_INVALID` | graph |
| `GRAPH_RIDDLE_UNREACHABLE` | graph |
| `RIDDLE_TYPE_UNSUPPORTED` | capability |
| `SURFACE_INCOMPATIBLE` | capability |
| `SURFACE_CARDINALITY_INVALID` | capability |
| `SURFACE_OWNERSHIP_INVALID` | capability |
| `COLLECTION_REQUIRED_TEXT_MISSING` | capability |
| `RESOURCE_AVAILABILITY_INVALID` | capability |
| `INPUT_RESOURCES_UNSUPPORTED` | capability |
| `HINT_ORDER_INVALID` | capability |
| `ASSET_PATH_UNSAFE` | assets |
| `ASSET_PATH_DUPLICATE` | assets |
| `ASSET_HASH_MISMATCH` | assets |
| `ASSET_MISSING` | assets |
| `ASSET_CONTENT_MISMATCH` | assets |
| `ASSET_EVIDENCE_TEXT_MISSING` | assets |

## Semantische Pflichtprüfungen

- `playerCount.min <= playerCount.max`;
- alle IDs global eindeutig;
- jede Referenz existent und typkorrekt;
- genau ein Graphknoten pro Rätsel und genau ein Rätsel pro Riddle-Knoten;
- Start hat keine Eingänge, Ende keine Ausgänge;
- keine Self-Edges oder doppelten Kanten;
- azyklisch, alle Knoten vom Start erreichbar und zum Ende führend;
- Graph entspricht der geordneten Abschnitts-/AND-Form;
- jedes Rätsel hat mindestens ein existierendes Lernziel;
- jedes Rätsel hat eine positive `estimatedMinutes`-Angabe;
- `collection/container` nutzt eine Container-Surface;
- `collection/world_object` nutzt die World-Surface;
- jedes `collection`-Rätsel enthält mindestens eine Inline-Text-Resource;
- mindestens eine `resourceIds`-Referenz jedes `collection`-Rätsels zeigt auf
  eine eigene Inline-Text-Resource mit `purpose=clue` oder `instruction`;
- Container-Funde nutzen ausschließlich `inside_container`; Weltobjekt-Funde
  ausschließlich `visible_in_level`;
- `input.numeric` nutzt eine Keypad-Surface;
- `input.resources` ist in V0.2 immer leer;
- Endknoten verweist auf genau eine Door-Surface;
- genau eine World- und eine Door-Surface existieren;
- jede Container-/Keypad-Surface gehört genau einem Rätsel und keine bleibt
  ungenutzt;
- Assetpfade sind sicher, vorhanden und inhaltlich vom deklarierten Typ;
- Assetpfade sind nach portabler Normalisierung eindeutig;
- der zwölfstellige Dateipräfix entspricht dem SHA-256-Inhaltshash;
- Asset-`purpose` und `accessibility.decorative` sind konsistent;
- jedes `riddle_evidence`-Bild besitzt im selben Rätsel einen begleitenden
  Inline-Text mit `purpose=clue` oder `instruction`;
- `failureText` ist bei hartem Zeitlimit vorhanden;
- Hint-`severity` entspricht der lückenlosen Listenreihenfolge;
- nur Fähigkeiten der Formatversion `0.2-draft`.

## Umsetzungsreihenfolge

1. Schema, Beispiel und semantische Regeln stabilisieren.
2. Gemeinsame Conformance-Fixtures und Issue-Codes definieren.
3. UI-Draft, Projektion, Autosave und Finalisierung implementieren.
4. Foundation-Progressions- und Multiplayer-Layer in `escapeRoom` ergänzen.
5. Deterministischen Layout-/Modulgenerator implementieren.
6. Generator-ZIP in einem temporären Integrations-Checkout bauen.
7. Foundation-Beispiel lokal und mit mehreren Clients durchspielen.

## Definition of Done

- Beispiel und alle Fixtures bestehen in TypeScript und Java.
- Kein ungültiger oder unvollständiger Entwurf wird finalisiert.
- Generatorausgabe ist bei gleichem Input und gleicher Generatorversion
  reproduzierbar.
- Fehler erzeugen weder Teil-ZIP noch Checkout-Mutationen.
- Generiertes Modul baut mit Java 25 und dem realen Root-Gradle-Setup.
- Fund, Keypad, Timer, Ausgang und Progression sind serverautoritativ.
- Reconnect/Snapshot stellt denselben gemeinsamen Zustand wieder her.
- Ein im lokalen UI-Draft protokollierter Playtest bestätigt die menschliche
  Durchspielbarkeit des Foundation-Beispiels.
