# DEER Parameter Table V0.2

Status: Foundation-Contract für `formatVersion=0.2-draft`

## Grundregeln

- Der Wizard fragt fachliche Entscheidungen ab.
- Die UI leitet IDs, Surfaces und Graphkanten ab.
- Der Generator leitet Positionen, Slots, Petri-Netze, Netzwerkzustand und
  Java-Adapter ab.
- Parameterobjekte sind typ-spezifisch geschlossen.
- Nur Werte mit UI-, Validator-, Generator- und Runtime-Unterstützung gehören
  in diese Formatversion.

## Gemeinsame Rätsel-Felder

| Feld | Pflicht | Regel |
|---|---:|---|
| `id` | ja | global eindeutige, stabile ID |
| `type` | ja | `collection` oder `input` |
| `title` | ja | nicht leer |
| `learningObjectiveIds` | ja | mindestens ein existierendes Lernziel |
| `playerFacingTask` | ja | nicht leer; wird im Spiel gezeigt |
| `resources` | ja | bei Fund mindestens ein Inline-Text, optional Bilder; bei Zahlencode `[]` |
| `hints` | ja | geordnete Hilfen; sonst `[]` |
| `parameters` | ja | exakt eine aktive Typvariante |
| `difficulty` | nein | `easy`, `medium`, `hard` |
| `estimatedMinutes` | ja | positive Ganzzahl |

Progression steht ausschließlich in `riddleGraph`.

## Fund / collection.single

```json
{
  "surfaceId": "s_desk",
  "sourceKind": "container",
  "rewardMode": "find_resource",
  "resourceIds": [
    "res_keypad_code"
  ]
}
```

| Feld | Pflicht | Regel |
|---|---:|---|
| `surfaceId` | ja | Container bei `container`, World bei `world_object` |
| `sourceKind` | ja | `container` oder `world_object` |
| `rewardMode` | ja | fest `find_resource` |
| `resourceIds` | ja | nicht leere Liste eigener Resources |

Abschluss: alle `resourceIds` wurden teamweit als gefunden registriert.

Alle Resources verwenden bei `container` die Availability
`inside_container`, bei `world_object` die Availability
`visible_in_level`.

Jeder Fund enthält mindestens eine Inline-Text-Resource. Mindestens eine
`resourceIds`-Referenz zeigt auf einen eigenen Inline-Text mit `purpose=clue`
oder `purpose=instruction`. Bilder können diesen notwendigen Inhalt ergänzen,
aber nicht ersetzen; die Referenzbindung wird semantisch geprüft.

Nicht in DEER:

- `slotType`;
- `consumeOnCollect`;
- `repeatable`;
- freier `successEffect`;
- konfigurierbares Erfolgsfeedback.

## Zahlencode / input.numeric

```json
{
  "surfaceId": "s_exit_keypad",
  "inputMode": "numeric",
  "answer": "3758",
  "showDigitCount": true
}
```

| Feld | Pflicht | Regel |
|---|---:|---|
| `surfaceId` | ja | Keypad-Surface |
| `inputMode` | ja | fest `numeric` |
| `answer` | ja | 1 bis 8 Ziffern |
| `showDigitCount` | ja | explizites Boolean |

Abschluss: das serverautoritative Keypad bestätigt den exakten Code.
`resources` ist bei diesem Typ immer `[]`.

Nicht in DEER:

- `slotType`;
- `minLength` oder `maxLength`;
- `acceptedCharacters`;
- `retryPolicy`;
- `wrongFeedback` oder `successFeedback`;
- freier `successEffect`.

## Resources

### Inline-Text

Pflicht:

- `id`;
- `kind=inline_text`;
- `title`;
- nicht leerer `text`;
- `availability`;
- `purpose`.

### Bild

Pflicht:

- `id`;
- `kind=asset`;
- `title`;
- existierende `assetId`;
- `availability`;
- `purpose`.

`availability`:

- `visible_in_level`;
- `inside_container`.

`purpose`:

- `clue`;
- `context`;
- `instruction`;
- `decoy`.

## Hilfen

Pflicht pro Hilfe:

| Feld | Regel |
|---|---|
| `id` | global eindeutig |
| `title` | nicht leer |
| `text` | nicht leer |
| `severity` | lückenlos und eindeutig `1..n` pro Rätsel |

Ab Aktivierung des Rätsels kann die erste Hilfe angefordert werden. Jede
weitere Anforderung zeigt die nächste. Die Arrayreihenfolge ist maßgeblich; die
UI leitet daraus `severity=1..n` ab.

## Assets

| Feld | Pflicht | Regel |
|---|---:|---|
| `id` | ja | global eindeutig |
| `path` | ja | sicherer Pfad unter `assets/custom/` |
| `mediaType` | ja | `image/png` oder `image/jpeg` |
| `purpose` | ja | `riddle_evidence` oder `decorative`; muss zu `accessibility.decorative` passen |
| `source` | ja | Herkunft und Lizenz |
| `accessibility` | ja | dekorativ oder nicht-spoilernde Beschreibung |

Ein Asset wird ausschließlich durch eine Asset-Resource referenziert.
Ein als `riddle_evidence` verwendetes Bild braucht im selben Rätsel zusätzlich
einen Inline-Text mit `purpose=clue` oder `instruction`.

V0.2 enthält genau eine World- und eine Door-Surface. Jede Container- oder
Keypad-Surface gehört genau einem Rätsel; nur die World-Surface darf von
mehreren Weltobjekt-Funden geteilt werden.

## Kompatibilitätsmatrix

| Rätsel | Parameter | Surface |
|---|---|---|
| Fund | `sourceKind=container` | `container` |
| Fund | `sourceKind=world_object` | `world` |
| Zahlencode | `inputMode=numeric` | `keypad` |
| Endknoten | `surfaceId` | `door` |

Graphaktivierung ist die autoritative Interaktionsfreigabe. Die Generator-
Runtime entscheidet, wie inaktive Geräte dargestellt werden.

## Bewusst entfernte V0.1-Felder

`slotType`, freie `successEffect`-Objekte, Hint-Unlock-Envelopes,
`computer_file`, `after_riddle`, `generated_by_riddle`, Audio-Medientypen und
`asset.linkedTo` sind in `0.2-draft` nicht erlaubt. Sie waren dem realen
Foundation-Generator und der Runtime voraus und können in einer späteren
Formatversion gezielt zurückkehren.
