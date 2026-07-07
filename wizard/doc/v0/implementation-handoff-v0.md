# Implementation Handoff V0

## Ziel

V0 ist eine schlichte Wizard-Web-App für nicht-technische Lehrende. Die App
erfasst einen einfachen Escape-Room-Entwurf, erzeugt daraus eine validierte
`deer.json` und verwaltet die referenzierten Assets in einem Projektordner.

`deer.json` ist der Contract zwischen UI und Generator. Das spielbare
Room-Paket entsteht erst im Java-Generator.

## Glossar

- **DEER**: das Authoring-Format für einen erzeugbaren Escape-Room-Entwurf.
- **`deer.json`**: validierte Authoring-Datei und einziges UI-Contract-
  Artefakt.
- **Projektordner**: `deer.json` plus referenzierte Assets; manuelle Eingabe
  für den Generator.
- **Room-Paket**: vom Java-Generator erzeugtes spielbares Runtime-Artefakt.

## V0-Produktfluss

```text
Lehrender öffnet Wizard-Web-App
-> erfasst Rahmen, Szenario, Rätsel, Inhalte, Assets und Hinweise
-> Wizard erzeugt `deer.json`
-> Wizard validiert Schema, Referenzen und blockierende Fachregeln
-> Wizard finalisiert den Entwurf im Projektordner
-> Java-Generator liest Projektordner, validiert erneut und erzeugt Room-Paket
```

## Contract-Dateien

Dieses Handoff wiederholt keine Feldlisten. Maßgeblich sind:

- `wizard/doc/v0/deer.schema.json`
- `wizard/doc/v0/deer-json-spec.md`
- `wizard/doc/v0/parameter-table-v0.md`
- `wizard/doc/v0/examples/deer.example.json`
- `wizard/doc/v0/generator-input-format.md`

Ergänzende Arbeitsreferenzen:

- `wizard/doc/v0/teacher-workflow-v0.md`
- `wizard/doc/v0/wizard-ui-flow-v0.md`
- `wizard/doc/v0/the-last-hour-interaction-catalog.md`

The Last Hour liefert verfügbare Bausteine und Assets. Es ist keine Vorlage für
den V0-Wizard-Flow.

## Erster Foundation-Slice

Der erste Slice verbindet UI und Generator end-to-end mit dem kleinsten
nützlichen Raum:

```text
Rahmen
-> Szenario
-> Fund
-> Keypad
-> Tür öffnen
-> deer.json finalisieren
```

Unterstützte Bausteine:

- sichtbarer Baustein **Fund**, intern `collection.single` und
  `riddle.type=collection` mit
  `parameters.rewardMode=find_resource`
- sichtbarer Baustein **Zahlencode**, intern `input.numeric` und
  `riddle.type=input` mit
  `parameters.inputMode=numeric`
- kontrollierter `successEffect` für Welt- oder Oberflächenänderungen, z. B.
  Tür öffnen
- einfache Text- oder Bildassets
- optionale Hinweise ohne komplexe Unlock-Bedingungen

Der Slice prüft den vollständigen Pfad von Eingabe über Assets, Rätselgraph,
Validierung und manuellen Generatorlauf.

## UI-Aufgaben

Die UI verantwortet:

- Workflow aus `teacher-workflow-v0.md` und `wizard-ui-flow-v0.md` abbilden,
- fachliche Eingaben erfassen,
- stabile IDs erzeugen,
- technische Strukturen wie `surfaces`, Referenzen und kontrollierte Effekte
  ableiten,
- `deer.json` nach Contract erzeugen,
- Assets annehmen und relativ referenzieren,
- Schema- und Fachvalidierung vor der Finalisierung ausführen,
- blockierende Fehler sichtbar machen und Finalisierung verhindern,
- Warnungen sichtbar machen, ohne Finalisierung zu blockieren,
- den manuellen nächsten Schritt für den Generator anzeigen.

Nicht generatorfähige Bausteine bleiben deaktiviert oder klar als noch nicht
verfügbar markiert.

## Generator-Aufgaben

Der Generator verantwortet:

- Projektordner mit `deer.json` und `assets/` lesen,
- `deer.json` gegen Schema und Fachregeln validieren,
- Asset-Existenz und Assettypen prüfen,
- ID-, `surfaceId`-, Rätsel-, Hint- und Ressourcenreferenzen prüfen,
- Baustein-Parameter und Oberflächen-Kompatibilität prüfen,
- Graph-Erreichbarkeit, Endzustand und Softlock-Risiken prüfen,
- genau einen Endzustand erzwingen,
- runtime-interne Tokens, Petri-Netze, Trigger oder States ableiten,
- den Foundation-Slice spielbar erzeugen,
- ein Room-Paket auf Disk schreiben,
- einen strukturierten Validierungsbericht ausgeben.

Generator-Laufparameter wie Seed oder Layout-Profil gehören nicht in
`deer.json`. Sie bleiben Code-/Konfigurations- oder Aufrufparameter.

## Validierungsmodell

UI und Generator nutzen dieselben Issue-Klassen. Die UI blockiert die
Finalisierung bei `error`; der Generator bleibt die autoritative zweite
Validierungsstufe.

Gemeinsames Issue-Format:

```json
{
  "severity": "error",
  "phase": "graph",
  "code": "GRAPH_UNREACHABLE_RIDDLE",
  "message": "Das Rätsel ist vom Start aus nicht erreichbar.",
  "path": "/riddleGraph/nodes/4",
  "entity": {
    "kind": "riddle",
    "id": "r_storage_keypad"
  },
  "relatedPaths": ["/riddleGraph/edges/3"],
  "blocking": true
}
```

Regeln:

- `path` ist ein JSON Pointer.
- `code` ist stabil und maschinenlesbar.
- `message` ist nutzerlesbar.
- `entity` erleichtert UI-Markierung.
- Warnungen haben `severity=warning` und `blocking=false`.

Blockierend sind insbesondere fehlende Pflichtfelder, unbekannte Referenzen,
fehlende oder ungültige Assets, nicht unterstützte Bausteine, inkompatible
Oberflächen, unerreichbare Rätsel, überspringbare Progression, nicht erreichbare
Endzustände, unlösbare Abhängigkeiten und ungültige Hint-Unlocks.

Warnungen betreffen Qualität und Redaktionsrisiken, z. B. lange Texte, fehlende
Hinweise, ungenutzte Assets oder schwache Story-Einbettung.

## Contract-Grenzen

- Die UI finalisiert einen Projektordner mit `deer.json` und referenzierten
  Assets; spielbare Runtime-Artefakte erzeugt der Java-Generator.
- `deer.json` bleibt Authoring-Modell. Runtime-Tokens, Petri-Netze,
  Generatorparameter und Paketierung gehören nicht in dieses Format.
- Ein Raum hat genau einen Endzustand.
- Weitere Bausteine werden erst contract-relevant, wenn UI, Validierung und
  Generator sie im selben Slice unterstützen.

## Implementierungsstart

UI und Generator starten mit demselben Schema, demselben Issue-Format und dem
Foundation-Slice. Weitere Bausteine folgen erst nach expliziter
Generator-Unterstützung.
