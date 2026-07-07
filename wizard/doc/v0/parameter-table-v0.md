# DEER Parameter Table V0

Stand: 07.07.2026
Status: Detailtabelle für den V0-Foundation-Slice

## Ziel

Diese Tabelle beschreibt die typ-spezifischen Parameter, die der Wizard im
ersten lauffähigen Slice erzeugt. Sie ergänzt
[`deer-json-spec.md`](deer-json-spec.md). Das JSON Schema prüft die Struktur;
UI und Generator prüfen zusätzlich fachliche Referenzen und Spielbarkeit.
Die aktiven V0-Parameterobjekte sind typ-spezifisch geschlossen: Ein
`input.numeric` darf keine Fundparameter tragen und ein `collection.single`
keine Eingabeparameter.

Grundregel:

- Der Wizard fragt fachliche Inhalte und notwendige Entscheidungen ab.
- Oberflächen entstehen aus den gewählten Bausteinen und werden in der UI
  fachlich benannt.
- Der Rätselgraph ist die Quelle für Progression; `deer.json` enthält keine
  öffentlichen Tokens.
- Die Graph-Topologie ist die Abhängigkeitsquelle. Kanten tragen keine zweite
  Bedingungsliste.
- Der manuell gestartete Generator wählt konkrete Positionen, Slot-Instanzen,
  Runtime-States und technische Details.
- Der Client validiert vor der Finalisierung; der Java-Generator validiert
  erneut als Sicherheitsnetz.

## Gemeinsame Riddle-Felder

Jedes Rätsel braucht außerhalb von `parameters`:

| Feld | Pflicht | Bedeutung |
|---|---:|---|
| `id` | ja | Stabile technische ID. |
| `type` | ja | Einer der aktiven V0-Typen: `collection` oder `input`. |
| `title` | ja | Interner und optional sichtbarer Titel. |
| `playerFacingTask` | ja | Aufgabenformulierung für Wizard/UI. |
| `assetIds` | ja | Direkt benötigte Asset-Referenzen, sonst `[]`. |
| `resources` | ja | Normale Hinweise/Informationen im Raum, sonst `[]`. |
| `hints` | ja | Optionale Hilfen, sonst `[]`. |
| `parameters` | ja | Typ-spezifische Parameter. |
| `difficulty` | nein | Redaktionsmetadatum für Warnungen. |
| `estimatedMinutes` | nein | Zeitannahme für Balance und Warnungen. |

Progression liegt ausschließlich in `riddleGraph.edges`.

## Wiederkehrende Parameter

Diese Felder können je nach aktivem Typ sinnvoll sein:

| Feld | Pflicht | Bedeutung |
|---|---:|---|
| `surfaceId` | ja | Referenz auf `surfaces[].id`, z. B. `s_storage_keypad`. |
| `slotType` | ja | Gewünschter Slot-Typ, z. B. `container_slot`, `keypad_slot`. |
| `successEffect` | typabhängig | Kontrollierter Effekt nach Erfolg, z. B. `open_surface` auf eine Tür-Surface. |
| `successFeedback` | nein | Kurzer sichtbarer Erfolgstext. |
| `wrongFeedback` | nein | Kurzer Fehlertext bei falscher Eingabe. |
| `retryPolicy` | nein | Standard `infinite_retry`; V0 sollte Progression nicht dauerhaft blockieren. |

`successEffect` ist keine freie Nutzereingabe. Lehrende wählen fachlich, z. B.
"Tür öffnen"; die UI schreibt intern ein kontrolliertes Objekt.
Fund-Rätsel nutzen keinen `successEffect`, um gefundene Ressourcen zu vergeben;
bei `collection.single` ist `resourceIds` selbst das Ergebnis des Fundes.

V0-Effektmodell:

| Kategorie | Bedeutung | Beispiel |
|---|---|---|
| `set_state` | Welt- oder Runtime-Zustand setzen | `{ "type": "set_state", "stateId": "power_on" }` |
| `unlock_surface` | Interaktionsoberfläche freischalten | `{ "type": "unlock_surface", "surfaceId": "s_keypad" }` |
| `open_surface` | Tür oder Bereich öffnen | `{ "type": "open_surface", "surfaceId": "s_exit_door" }` |

## Aktive V0-Typen

### `collection.single`

Anwendungsfall: Notiz, Hinweis oder Ressource an einem Fundort finden.

Interner JSON-Contract: `riddle.type=collection`,
`parameters.rewardMode=find_resource`.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Fundort oder Container-Surface. |
| `slotType` | `container_slot` oder `world_object_slot`. |
| `sourceKind` | `container` oder `world_object`. |
| `rewardMode` | `find_resource`. |
| `resourceIds` | Eine oder mehrere Ressourcen, die durch den Fund verfügbar werden. |

Optional:

- `consumeOnCollect`
- `repeatable`

```json
{
  "surfaceId": "s_desk",
  "slotType": "container_slot",
  "sourceKind": "container",
  "rewardMode": "find_resource",
  "resourceIds": ["res_keypad_note"]
}
```

### `input.numeric`

Anwendungsfall: Keypad, Zahlen-Code oder einfache numerische Antwort.

Interner JSON-Contract: `riddle.type=input`, `parameters.inputMode=numeric`.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Eingabeoberfläche, z. B. Keypad. |
| `slotType` | `keypad_slot`. |
| `inputMode` | `numeric`. |
| `answer` | Erwarteter Zahlencode; nur Ziffern. |
| `maxLength` | Maximale Eingabelänge. |
| `successEffect` | Effekt nach korrekter Eingabe. |

Optional:

- `minLength`
- `showDigitCount`
- `acceptedCharacters`, Standard `digits`

```json
{
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
```

## Resources

Resources sind normale Informationen im Raum oder Computer. Sie sind keine
Hints und erzeugen keine Progression.

Pflicht pro Resource:

| Feld | Bedeutung |
|---|---|
| `id` | Resource-ID. |
| `kind` | `inline_text`, `asset`, `world_object`, `computer_file`. |
| `title` | Sichtbarer Name. |
| `availability` | `visible_in_level`, `inside_container`, `after_riddle`, `generated_by_riddle`. |
| `purpose` | `clue`, `context`, `instruction`, `decoy`. |

Zusätzlich:

- Bei `kind=asset`: `assetId`
- Bei `availability=after_riddle`: `requiredRiddleId`
- Bei `kind=inline_text`: `text`

## Hints

Hints bleiben optional, aber das Array existiert immer.

Pflicht pro Hint:

| Feld | Bedeutung |
|---|---|
| `id` | Hint-ID. |
| `title` | Kurztitel. |
| `text` | Hilfetext. |
| `severity` | Eskalationsstufe, beginnend bei `1`. |

Optional pro Hint:

| Feld | Bedeutung |
|---|---|
| `unlock` | Freischaltbedingung. Ohne `unlock` ist der Hint sofort verfügbar. |

`unlock` ist kein flaches Condition-Objekt, sondern ein Envelope:

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

V0-Bedingungen:

| Bedingung | Pflichtfelder | Bedeutung |
|---|---|---|
| `elapsed_time` | `seconds` | nach Zeitablauf freischalten |
| `failed_attempts` | `riddleId`, `count` | nach Fehlversuchen in einem Rätsel |
| `riddle_completed` | `riddleId` | nachdem ein Rätsel gelöst wurde |

Weitere Bausteinideen stehen im
[`the-last-hour-interaction-catalog.md`](the-last-hour-interaction-catalog.md).
Sie werden erst in diese Tabelle aufgenommen, wenn UI, Validierung und
Generator sie im selben Slice unterstützen.

## Entscheidungen

1. `surfaceId` bleibt dort relevant, wo mehrere Oberflächen möglich sind oder
   ein wiederverwendbares Objekt adressiert wird.
2. `successEffect` ist eine kontrollierte Wizard-Auswahl für Bausteine, die
   eine Welt- oder Oberflächenänderung auslösen; Fund-Ressourcen werden über
   `resourceIds` modelliert.
3. V0 bleibt klein: vollständige Spezifikation nur für tatsächlich
   generierbare Foundation-Bausteine.
4. Laufzeit-Tokens, Petri-Netze und konkrete Slots werden aus `deer.json`
   abgeleitet und gehören in den Generator-Scope.
