# deer.json Specification V0.2

Status: `0.2-draft` Foundation-Contract
Stand: 09.07.2026

## 1. Rolle und Lebenszyklus

`deer.json` ist die vollständige Authoring-Konfiguration zwischen Wizard-UI und
Java-Generator. Sie ist **nicht** das Speicherformat eines unvollständigen
Wizard-Entwurfs.

```text
unvollständiger UI-Draft
-> DEER-Projektion
-> Schema- und Fachvalidierung
-> deer.json + assets/custom/
-> Java-Generator
-> generiertes Modul-ZIP
```

Die UI schreibt `deer.json` erst bei erfolgreicher Finalisierung. Der Generator
liest die Datei unverändert, validiert erneut und leitet daraus Geometrie,
Java-Konfiguration, Runtime-Zustände und das Modul-ZIP ab.

Nicht Teil von `deer.json`:

- UI-Layout, geöffnete Panels oder Draft-Status;
- konkrete Raumkoordinaten und Slot-Instanzen;
- Petri-Netze, Netzwerktokens oder Runtime-Snapshots;
- Generatorversion, Layoutprofil oder Seed;
- ZIP- und Build-Metadaten.

## 2. Top-Level

```json
{
  "formatVersion": "0.2-draft",
  "metadata": {},
  "learningDesign": {},
  "session": {},
  "scenario": {},
  "surfaces": [],
  "riddleGraph": {},
  "riddles": [],
  "assets": []
}
```

| Feld | Zweck |
|---|---|
| `formatVersion` | Unveränderliche Capability- und Contract-Version. |
| `metadata` | ID, Titel, Inhaltssprache und redaktionelle Angaben. |
| `learningDesign` | Lernziele und optionale Reflexionsfragen. |
| `session` | Zielgruppe, Vorwissen, Spielerzahl und Zeit. |
| `scenario` | Standard-Theme und Storytexte. |
| `surfaces` | Von der UI abgeleitete Orte und Geräte. |
| `riddleGraph` | Abgeleitete Progression und gemeinsames Erfolgsziel. |
| `riddles` | Foundation-Rätsel, Inhalte und Hilfen. |
| `assets` | Referenzierte PNG-/JPEG-Dateien. |

## 3. IDs und Referenzen

IDs verwenden:

```text
lower_snake_case
```

Alle Entity-IDs sind dokumentweit global eindeutig:

- Lernziele;
- Surfaces;
- Graphknoten und -kanten;
- Rätsel;
- Resources;
- Hilfen;
- Assets.

Die UI erzeugt eine ID einmal und verändert sie bei Umbenennung nicht.
Referenzen sind immer vorwärts gerichtet; es gibt keine redundante
`asset.linkedTo`-Rückreferenz.

`collection.parameters.resourceIds` verweist ausschließlich auf Resources
desselben Rätselobjekts. Alle anderen ID-Referenzen werden dokumentweit
aufgelöst.

## 4. metadata

```json
{
  "id": "wizard_foundation_v0_2",
  "title": "Foundation Beispielraum V0.2",
  "locale": "de-DE",
  "description": "Kleiner Foundation-Slice.",
  "author": "Beispiel Lehrkraft"
}
```

Pflicht:

- `id`;
- `title`;
- `locale`, in V0.2 fest `de-DE`.

`description` und `author` sind optional. Strings mit ausschließlich
Whitespace sind ungültig.

`metadata.locale` beschreibt die Sprache des erzeugten Inhalts, nicht die
Sprache der Wizard-Oberfläche.

## 5. learningDesign

```json
{
  "objectives": [
    {
      "id": "lo_extract_numeric_clue",
      "description": "Die Lernenden können eine relevante Zahleninformation aus einem Hinweis entnehmen und anwenden."
    }
  ],
  "debriefPrompts": [
    "Welche Information war für die Lösung relevant?"
  ]
}
```

Regeln:

- Mindestens ein Lernziel ist Pflicht.
- Jedes Rätsel referenziert mindestens ein existierendes Lernziel.
- `debriefPrompts` ist immer vorhanden, darf aber leer sein.
- Lernziele sind Authoring-Metadaten und keine Behauptung über gemessene
  Kompetenz.
- Die Generatorausgabe darf Lernziele und Reflexionsfragen in
  Betreuungsmaterial übernehmen; sie steuern keine Runtime-Progression.

## 6. session

```json
{
  "targetAudience": "Lernende im Bereich IT-Sicherheit",
  "priorKnowledge": "Grundlagen zu einfachen Codes und Hinweisen.",
  "playerCount": {
    "min": 1,
    "max": 4
  },
  "time": {
    "limitMinutes": 30,
    "limitMode": "hard"
  }
}
```

Regeln:

- `1 <= min <= max <= 4`.
- Der Raum ist kooperativ; es gibt keinen editierbaren Wettbewerbsmodus.
- `hard` beendet die gemeinsame Session nach Ablauf serverautoritativ als
  fehlgeschlagen.
- `soft` lässt die Session weiterlaufen und markiert Überzeit; V0.2 schaltet
  dadurch keine Hilfen automatisch frei.

## 7. scenario

```json
{
  "themeId": "default",
  "playerRole": "Untersuchungsteam",
  "premise": "Ein Lagerraum ist verriegelt.",
  "mission": "Findet den Zugangscode und verlasst gemeinsam den Raum.",
  "introText": "Die Ausgangstür ist verschlossen.",
  "successText": "Die Gruppe hat den Ausgang erreicht.",
  "failureText": "Die Zeit ist abgelaufen."
}
```

V0.2 nutzt ausschließlich `themeId=default` und Storytexte. Lore-Bilder,
Audio und Theme-Auswahl sind nicht Teil dieser Formatversion.

`successText` gehört zum gemeinsamen Ausgangserfolg. `failureText` ist nur
bei `session.time.limitMode=hard` Pflicht und wird beim harten Zeitablauf
verwendet; dieser Fehlschlag ist kein zweiter Graph-Endknoten.

## 8. surfaces

Surfaces sind interne Orte oder Geräte. Die UI erzeugt sie aus fachlichen
Rätseleingaben und zeigt keine technische Registry.

Aktive Arten:

| `kind` | Sichtbare Bedeutung |
|---|---|
| `world` | allgemeiner Raum / sichtbares Weltobjekt |
| `container` | durchsuchbarer Fundort |
| `keypad` | numerisches Eingabegerät |
| `door` | gemeinsamer Ausgang |

```json
[
  {
    "id": "s_desk",
    "kind": "container",
    "title": "Schreibtisch"
  },
  {
    "id": "s_exit_keypad",
    "kind": "keypad",
    "title": "Tür-Keypad"
  },
  {
    "id": "s_exit_door",
    "kind": "door",
    "title": "Ausgangstür"
  }
]
```

Konkrete Koordinaten und Runtime-Slots gehören in den Generator.

V0.2 enthält genau eine World-Surface und genau eine Door-Surface. Jede
Container- oder Keypad-Surface wird von genau einem Rätsel referenziert und
darf nicht ungenutzt bleiben. Die World-Surface darf von mehreren sichtbaren
Weltobjekt-Funden verwendet werden; die Door-Surface gehört ausschließlich zum
Endknoten.

## 9. riddleGraph

Der Graph beschreibt Progression, nicht Raumgeometrie. V0.2 akzeptiert nur das
aus geordneten Abschnitten ableitbare AND-Profil.

Ableitung:

1. Start hat Kanten zu allen Rätseln des ersten Abschnitts.
2. Jedes Rätsel eines Abschnitts hat Kanten zu jedem Rätsel des nächsten
   Abschnitts.
3. Mehrere eingehende Kanten bedeuten: **alle Vorgänger abgeschlossen**.
4. Alle Rätsel des letzten Abschnitts haben Kanten zum Endknoten.

Damit sind Rätsel innerhalb eines Abschnitts parallel, alle Rätsel bleiben aber
notwendig. OR-Verzweigungen und optionale Pfade sind nicht darstellbar.

Knotenarten:

- `start`: genau einmal, ohne eingehende Kante;
- `riddle`: genau einmal pro Rätsel, mit `riddleId`;
- `end`: genau einmal, ohne ausgehende Kante und mit `surfaceId` auf eine
  Door-Surface.

```json
{
  "startNodeId": "n_start",
  "endNodeId": "n_exit",
  "nodes": [
    {
      "id": "n_start",
      "kind": "start"
    },
    {
      "id": "n_enter_code",
      "kind": "riddle",
      "riddleId": "r_enter_code"
    },
    {
      "id": "n_exit",
      "kind": "end",
      "surfaceId": "s_exit_door"
    }
  ],
  "edges": [
    {
      "id": "e_start_to_code",
      "from": "n_start",
      "to": "n_enter_code"
    },
    {
      "id": "e_code_to_exit",
      "from": "n_enter_code",
      "to": "n_exit"
    }
  ]
}
```

Semantische Pflichtregeln:

- `startNodeId` und `endNodeId` zeigen auf die jeweiligen eindeutigen Knoten.
- Keine Self-Edges oder doppelten `from/to`-Paare.
- Der Graph ist azyklisch.
- Jeder Knoten ist vom Start erreichbar und kann das Ende erreichen.
- `riddles[]` und Riddle-Knoten bilden eine Bijektion.
- Die Kanten entsprechen vollständig dem Abschnitts-/AND-Profil.
- Der Endknoten referenziert eine existierende Door-Surface.

Wenn alle Vorgänger des Endknotens abgeschlossen sind, öffnet der
serverautoritative Runtime-Layer die Ausgangstür. Erfolg tritt ein, wenn alle
aktiven Spielenden den offenen Ausgang gemäß Runtime-Regel erreicht haben.

## 10. riddles

Gemeinsame Pflichtfelder:

| Feld | Bedeutung |
|---|---|
| `id` | stabile Rätsel-ID |
| `type` | `collection` oder `input` |
| `title` | redaktioneller Name |
| `learningObjectiveIds` | mindestens ein Lernziel |
| `playerFacingTask` | im Spiel sichtbare Aufgabe |
| `resources` | bei Fund mindestens ein Inline-Text und optionale Bilder; bei `input` leer |
| `hints` | optionale Hilfen; sonst leer |
| `parameters` | geschlossener typ-spezifischer Contract |

`estimatedMinutes` ist für jedes Rätsel Pflicht; `difficulty` bleibt
optional. Geschätzte Gesamtdauer wird über den kritischen Graphpfad berechnet.

### 10.1 Fund / collection.single

Interner Typ:

```text
riddle.type = collection
parameters.rewardMode = find_resource
```

Parameter:

| Feld | Regel |
|---|---|
| `surfaceId` | Container-Surface bei `sourceKind=container`, World-Surface bei `world_object` |
| `sourceKind` | `container` oder `world_object` |
| `rewardMode` | fest `find_resource` |
| `resourceIds` | nicht leere Liste eigener Resources |

Das Rätsel ist abgeschlossen, sobald der serverautoritative Foundation-Layer
alle referenzierten Resources als gefunden registriert hat. Konkrete
`slotType`-Werte leitet der Generator ab und speichert sie nicht in DEER.

Bei `sourceKind=container` haben alle Resources
`availability=inside_container`. Bei `sourceKind=world_object` haben sie
`availability=visible_in_level`.

Jedes Fund-Rätsel enthält mindestens eine `inline_text`-Resource. Mindestens
eine `resourceIds`-Referenz zeigt auf einen eigenen Inline-Text mit
`purpose=clue` oder `purpose=instruction`. PNG-/JPEG-Bilder können diesen Text
ergänzen, ihn im Foundation-Slice aber nicht ersetzen. Diese Referenzbindung
ist eine semantische, schemaübergreifende Regel.

### 10.2 Zahlencode / input.numeric

Interner Typ:

```text
riddle.type = input
parameters.inputMode = numeric
```

Parameter:

| Feld | Regel |
|---|---|
| `surfaceId` | existierende Keypad-Surface |
| `inputMode` | fest `numeric` |
| `answer` | 1 bis 8 Ziffern |
| `showDigitCount` | explizites Boolean |

Die Antwortlänge ist die effektive Eingabelänge. Es gibt kein separates
`minLength` oder `maxLength`. Falsche Versuche bleiben unbegrenzt möglich; V0.2
hat kein konfigurierbares Fehlerfeedback und keinen Attempt-Counter-Contract.

Das Keypad meldet erfolgreichen Abschluss an den gemeinsamen Progressionslayer.
Es öffnet nicht über einen frei konfigurierbaren `successEffect` direkt eine
Tür.

`resources` ist bei `input.numeric` in V0.2 immer `[]`. Zusätzliche
Aufgaben- oder Lösungshinweise gehören in vorherige Fund-Rätsel oder in
`playerFacingTask`.

## 11. resources

Resources sind notwendige oder redaktionelle Inhalte, keine optionalen Hilfen.
Sie sind unter ihrem besitzenden Rätsel gespeichert.

Aktive Varianten:

- `inline_text` mit nicht leerem `text`;
- `asset` mit `assetId`.

Beide Varianten enthalten `id`, `title`, `availability` und `purpose`.
Varianten sind geschlossen: Ein Inline-Text trägt kein `assetId`, eine
Asset-Resource keinen `text`.

Aktive `availability`-Werte:

- `visible_in_level`;
- `inside_container`.

Aktive `purpose`-Werte:

- `clue`;
- `context`;
- `instruction`;
- `decoy`.

`after_riddle`, `generated_by_riddle`, `world_object` als Resource-Typ und
`computer_file` sind nicht Teil von V0.2.

## 12. hints

Hilfen stehen im `hints`-Array des Rätsels. Pflicht pro Hilfe:

- `id`;
- `title`;
- `text`;
- `severity`.

Arrayreihenfolge ist die sichtbare Reihenfolge. Die UI leitet `severity`
lückenlos als `1..n` aus dieser Reihenfolge ab.

Sobald das besitzende Rätsel verfügbar ist, kann die erste Hilfe angefordert
werden. Jede weitere Anforderung zeigt die nächste Hilfe. `elapsed_time`,
`failed_attempts`, `riddle_completed` und kombinierte Unlock-Envelopes sind
nicht Teil dieser Formatversion.

## 13. assets

V0.2 unterstützt:

- `image/png`;
- `image/jpeg`.

Jedes Asset enthält:

- global eindeutige `id`;
- normalisierten Pfad unter `assets/custom/`;
- `mediaType`;
- `purpose`: `riddle_evidence` oder `decorative`;
- `source` mit Herkunft, Lizenz und optionaler Attribution;
- `accessibility`.

`purpose=riddle_evidence` verlangt `decorative=false` und eine nicht leere,
nicht-spoilernde `description`. `purpose=decorative` verlangt
`decorative=true`.

Die Beschreibung bleibt in der Generatorausgabe erhalten. Die WCAG-2.2-AA-
Zusage des Foundation-Konzepts bezieht sich auf die Authoring-UI, nicht
automatisch auf die LibGDX-Runtime. Notwendige Information darf daher nicht
ausschließlich in einem Bild stecken. Ein Asset mit
`purpose=riddle_evidence` benötigt im selben Rätsel mindestens eine
begleitende `inline_text`-Resource mit `purpose=clue` oder
`purpose=instruction`; die Zweckbindung bleibt eine semantische,
schemaübergreifende Regel.

Use-Site-Referenzen sind autoritativ: Eine Asset-Resource trägt `assetId`.
Das Asset selbst enthält keine `linkedTo`-Liste.

Pfadsicherheit:

- ausschließlich Forward-Slashes;
- Präfix `assets/custom/`;
- keine absoluten, Drive-, UNC- oder URL-Pfade;
- keine Backslashes, leeren, `.`- oder `..`-Segmente;
- keine Symlink-Auflösung aus dem Projektroot;
- Java prüft normalisierten Realpfad, Dateiinhalt und deklarierten Medientyp.
- Assets liegen flach unter `assets/custom/`; der Dateiname beginnt mit den
  ersten zwölf lowercase Hex-Zeichen seines SHA-256-Inhaltshashes und `-`.
- Assetpfade sind nach Slash-, Unicode- und Case-Normalisierung eindeutig.

## 14. Generatorgrenze

Generator-Eingabe:

```text
wizard-project/
  deer.json
  assets/custom/...
```

Generator-Ausgabe:

```text
<room-id>-module.zip
  <generated module>/
```

Details stehen in
[`generator-input-format.md`](generator-input-format.md) und
[`generator-output-format.md`](generator-output-format.md). Seed,
Generatorversion und Layoutprofil stehen im Generator-Manifest, nicht in
`deer.json`.

## 15. Validierung

JSON Schema prüft Form, geschlossene Varianten, Pflichtfelder, Enums und lokale
Werte. TypeScript und Java prüfen zusätzlich die semantischen Regeln.

Phasen:

1. **Feldprüfung:** lokale Eingaben und Draft-Vollständigkeit.
2. **DEER-Projektion:** Schema auf dem vollständigen Kandidaten.
3. **Referenzen:** globale IDs, typisierte Ziele und Resource-Besitz.
4. **Graphprofil:** Bijektion, DAG, Reichweite und Abschnittsform.
5. **Capabilities:** Typ-/Surface-Kompatibilität und Formatversion.
6. **Assets:** Pfad, Existenz, Inhalt, Lizenz- und Accessibility-Metadaten.
7. **Generatorprüfung:** dieselben Regeln plus Layout-/Runtime-Machbarkeit.

Das gemeinsame Issue-Format und die Mindestcodes stehen in
[`implementation-handoff-v0.md`](implementation-handoff-v0.md). Beide
Implementierungen werden gegen dieselben sprachunabhängigen
Conformance-Fixtures geprüft.

## 16. Warnungen und Playtesting

Deterministische Warnungen betreffen unter anderem:

- schwieriges Rätsel ohne Hilfe;
- Lernziel ohne Rätselreferenz;
- sehr lange Texte;
- ungenutztes Asset;
- geschätzter kritischer Pfad und Zeitlimit passen schlecht;
- kein Playtest im lokalen Draft protokolliert.

Die statische Prüfung kann nicht feststellen, ob Menschen einen Hinweis
verstehen, die Lösung tatsächlich herleiten oder das Lernziel erreichen. Ein
Playtest bleibt vor dem Einsatz mit Lernenden erforderlich. Fragen nach
Ableitbarkeit, Lernzielbeitrag, Sprache und Schwierigkeit erscheinen als
redaktionelle Selbstprüfung, nicht als automatisch erkannte Issues.

## 17. Spätere Formatversionen

Erst mit passender UI-, Validator-, Generator- und Runtime-Unterstützung:

- Audio, Lore-Medien und Themes;
- zeit-/versuchsabhängige Hilfen;
- Computer, E-Mail, USB, Item-Use, Assembly und Control Panel;
- deklarierte Zustandsaktionen und Zwischentüren;
- OR-Verzweigungen und optionale Pfade;
- Lernziel-Evidenz, Evaluation, Telemetrie und LMS-Anbindung.
