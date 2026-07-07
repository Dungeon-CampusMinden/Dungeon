# DEER Parameter Table V0

Stand: 02.07.2026
Status: V0-Detailtabelle für typ-spezifische Parameter

## Ziel

Diese Tabelle beschreibt, welche Parameter der Wizard für die erste
The-Last-Hour-abgeleitete Bausteinpalette erfassen sollte. Sie ergänzt
[`deer-json-spec.md`](deer-json-spec.md) um die typ-spezifischen Details. Das
JSON Schema prüft noch nicht jedes Detail hart; UI und Generator müssen diese
Tabelle für die V0-Validierung berücksichtigen.

Grundregel:

- Der Wizard fragt fachliche Inhalte und notwendige Entscheidungen ab.
- Oberflächen entstehen aus den gewählten Bausteinen und werden in der UI
  fachlich benannt.
- Der manuell gestartete Generator wählt konkrete Positionen, Slot-Instanzen und
  technische Runtime-Details.
- Der Client validiert vor der Bundle-Erstellung; der Java-Generator validiert
  erneut als Sicherheitsnetz.

## Gemeinsame Riddle-Felder

Jedes Rätsel braucht außerhalb von `parameters`:

| Feld | Pflicht | Bedeutung |
|---|---:|---|
| `id` | ja | Stabile technische ID. |
| `type` | ja | Einer der V0-Typen. |
| `title` | ja | Interner und optional sichtbarer Titel. |
| `designRole` | ja | `progression`, `clue`, `story`, `support`. |
| `difficulty` | ja | `easy`, `medium`, `hard`. |
| `estimatedMinutes` | ja | Zeitannahme für Balance und Warnungen. |
| `playerFacingTask` | ja | Aufgabenformulierung für Wizard/UI. |
| `requiresTokens` | ja | Tokens, die vorher verfügbar sein müssen. |
| `producesTokens` | ja | Tokens, die dieses Rätsel nach Erfolg erzeugt. |
| `assetIds` | ja | Direkt benötigte Asset-Referenzen, sonst `[]`. |
| `resources` | ja | Normale Hinweise/Informationen im Raum, sonst `[]`. |
| `hints` | ja | Optionale Hilfen, sonst `[]`. |
| `parameters` | ja | Typ-spezifische Parameter. |

## Gemeinsame Parameter

Diese Felder können für alle Typen sinnvoll sein:

| Feld | Pflicht | Bedeutung |
|---|---:|---|
| `surfaceId` | ja, wenn eine Oberfläche genutzt wird | Referenz auf `surfaces[].id`, z. B. `s_main_computer`. |
| `slotType` | ja | Gewünschter Slot-Typ, z. B. `computer_slot`, `keypad_slot`. |
| `successEffect` | ja | Kontrollierter Effekt nach Erfolg, z. B. `open_surface` auf eine Tür-Surface. |
| `successFeedback` | nein | Kurzer sichtbarer Erfolgstext. |
| `wrongFeedback` | nein | Kurzer Fehlertext bei falscher Eingabe/Auswahl. |
| `retryPolicy` | nein | Standard `infinite_retry`; V0 sollte keine Progression dauerhaft blockieren. |

Der Wizard leitet benötigte Oberflächen und Slot-Typen aus den gewählten
Bausteinen ab. Lehrende sollen die Oberflächen fachlich sehen und benennen
können, aber nicht zuerst eine technische Slot-Liste bauen müssen.

Der Generator darf `slotType` weiter verfeinern, aber der Wizard sollte
sichtbar machen, wenn ein Rätsel keinen kompatiblen Slot im geplanten Raum
findet.

`surfaceId` ist besonders für wiederverwendbare Ziele wichtig. Damit kann der
Wizard z. B. mehrere Computer, Keypads oder Weltobjekte unterscheiden und
Rätsel an die richtige Oberfläche binden. Die zugehörigen Oberflächen stehen
im top-level `surfaces`-Array und werden von der UI aus den Bausteinen
abgeleitet.

`successEffect` sollte nicht als beliebige Nutzereingabe verstanden werden. Der
Wizard sollte nur Effekte anbieten, die für den gewählten Baustein, die
Surface und die vorhandenen Slots valide sind.

V0-Effektmodell:

- `successEffect` ist ein kleines Objekt mit kontrolliertem `type`.
- Lehrende wählen fachlich, z. B. "Storage-Tür öffnen"; die UI schreibt
  intern z. B. `{ "type": "open_surface", "surfaceId": "s_storage_door" }`.
- Neue Effekte dürfen nur ergänzt werden, wenn UI und Generator sie beide
  kennen.
- Referenzen wie `surfaceId`, `resourceId` oder `itemId` müssen existieren.

Erste Effekt-Kategorien:

| Kategorie | Bedeutung | Beispiel |
|---|---|---|
| `set_state` | Welt- oder Runtime-Zustand setzen | `{ "type": "set_state", "stateId": "power_on" }` |
| `grant_resources` | Informationen/Ressourcen verfügbar machen | `{ "type": "grant_resources", "resourceIds": ["res_note"] }` |
| `grant_items` | Inventar-Items verfügbar machen | `{ "type": "grant_items", "itemIds": ["item_usb_blue"] }` |
| `unlock_surface` | Interaktionsoberfläche freischalten | `{ "type": "unlock_surface", "surfaceId": "s_main_computer" }` |
| `open_surface` | Tür oder Bereich öffnen | `{ "type": "open_surface", "surfaceId": "s_storage_door" }` |
| `mount_item` | Item an Ziel verwenden und neue UI freischalten | `{ "type": "mount_item", "itemId": "item_usb_blue" }` |
| `spawn_content` | Inhalte in der Welt erscheinen lassen | `{ "type": "spawn_content", "resourceId": "res_fragments" }` |
| `reveal_resource` | Ergebnisinformation sichtbar machen | `{ "type": "reveal_resource", "resourceId": "res_exit_password" }` |

## `state_change`

Use Case: Stromschalter, Hebel, einfacher Weltzustand.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Referenz auf eine Welt-/Interaktionsfläche; meist eine `world`-Surface. |
| `slotType` | Meist `world_interaction_slot`. |
| `interactionKind` | V0: `confirm`. |
| `target` | Logischer Zielname, z. B. `power_switch`. |
| `successEffect` | Welt-/State-Effekt. |
| `successFeedback` | Rückmeldung nach Erfolg. |

Optional:

- `promptText`
- `cancelText`
- `soundCue`

```json
{
  "surfaceId": "s_world",
  "slotType": "world_interaction_slot",
  "interactionKind": "confirm",
  "target": "power_switch",
  "successEffect": {
    "type": "set_state",
    "stateId": "power_on"
  },
  "successFeedback": "Die Stromversorgung springt an."
}
```

## `collection`

Use Case: Notiz finden, USB finden, Trash-Minispiel, Datei als Ressource
öffnen.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Pflicht, wenn der Fund an eine bestimmte Oberfläche gebunden ist. |
| `slotType` | `container_slot`, `trash_slot`, `world_object_slot`, `computer_file_slot`. |
| `sourceKind` | `container`, `world_object`, `trash_minigame`, `computer_file`. |
| `rewardMode` | `single`, `collect_all`, `find_resource`. |
| `successEffect` | Effekt nach erfolgreichem Fund. |

Zusätzlich Pflicht je nach Modus:

| Bedingung | Pflichtfeld |
|---|---|
| Item-Fund | `rewards` |
| Resource-Fund | `resourceIds` |
| `collect_all` | mindestens zwei Einträge in `rewards` oder `resourceIds` |

Optional:

- `paperCount` bei `trash_minigame`
- `consumeOnCollect`
- `repeatable`

```json
{
  "surfaceId": "s_trash_area",
  "slotType": "trash_slot",
  "sourceKind": "trash_minigame",
  "rewardMode": "single",
  "rewards": ["asset_note_password_2"],
  "paperCount": 30,
  "successEffect": {
    "type": "grant_resources",
    "resourceIds": ["res_login_note"]
  }
}
```

## `input`

Use Case: Keypad, Login, Text-/Codeeingabe, Decoding-Antwort.

Pflicht in `parameters` für alle `input`-Rätsel:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Pflicht, wenn mehrere Eingabeoberflächen möglich sind. |
| `slotType` | Passender Slot, z. B. `computer_login_slot`, `keypad_slot`. |
| `inputMode` | `numeric`, `text`, `credentials`, `decoded_text`. |
| `successEffect` | Effekt nach korrekter Eingabe. |

### `inputMode=numeric`

Pflicht:

- `answer`
- `maxLength`

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

### `inputMode=credentials`

Pflicht:

- `fields`

Feldstruktur:

| Feld | Pflicht | Bedeutung |
|---|---:|---|
| `id` | ja | `username`, `password`, etc. |
| `label` | ja | Sichtbarer Feldname. |
| `answer` | ja | Erwarteter Wert. |
| `secret` | nein | Passwortmodus, Standard `false`. |
| `caseSensitive` | nein | Standard `false`. |

```json
{
  "surfaceId": "s_main_computer",
  "slotType": "computer_login_slot",
  "inputMode": "credentials",
  "fields": [
    {
      "id": "username",
      "label": "E-Mail",
      "answer": "dr.mertens@ciphera-labs.com"
    },
    {
      "id": "password",
      "label": "Passwort",
      "answer": "a12b34xy",
      "secret": true
    }
  ],
  "successEffect": {
    "type": "unlock_surface",
    "surfaceId": "s_main_computer"
  }
}
```

### `inputMode=decoded_text`

Pflicht:

- `encodedValue`
- `answer`
- `decoderSteps`

Optional:

- `decoderResourceIds`
- `maxLength`

```json
{
  "surfaceId": "s_main_computer",
  "slotType": "computer_input_slot",
  "inputMode": "decoded_text",
  "encodedValue": "00110110001101010011010000111000",
  "answer": "6548",
  "decoderSteps": ["binary_to_hex", "hex_to_ascii"],
  "decoderResourceIds": ["res_binary_hex_table", "res_hex_ascii_table"],
  "successEffect": {
    "type": "reveal_resource",
    "resourceId": "res_unlock_code_document"
  }
}
```

## `choice`

Use Case: richtige E-Mail, richtige URL, richtige Option erkennen.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Für The Last Hour meist eine Computer-Surface. |
| `slotType` | Meist `computer_choice_slot`. |
| `presentation` | V0: `email_list`, `url_list`, `item_list`, `plain_options`. |
| `selectionMode` | V0 meistens `single_correct`. |
| `options` | Auswahloptionen. |
| `correctOptionId` | Bei `single_correct`. |
| `successEffect` | Effekt nach korrekter Auswahl. |

Optionale Fehlerpolitik:

- `wrongChoicePolicy`: Standard `feedback_retry`
- `wrongChoiceFeedback`

V0-Regel: falsche Optionen geben Feedback und erlauben Retry. Keine
dauerhafte Infektion, kein Progressionsverlust.

Für The Last Hour sollte `choice.email_list` nach Möglichkeit in den Computer
integriert werden. Der erste technische Schnitt muss dafür nicht den gesamten
aktuellen Mail-Client frei konfigurierbar machen; ausreichend wäre ein
konfigurierter Computer-Tab, der strukturierte E-Mail-Optionen anzeigt und die
richtige Option prüft.

```json
{
  "surfaceId": "s_main_computer",
  "slotType": "computer_choice_slot",
  "presentation": "email_list",
  "selectionMode": "single_correct",
  "correctOptionId": "mail_real_support",
  "wrongChoicePolicy": "feedback_retry",
  "options": [
    {
      "id": "mail_real_support",
      "sender": "Andreas Keller",
      "senderMail": "andreas.keller@secugate.com",
      "subject": "Re: SG-4 Access Code",
      "url": "https://support.secugate.com/sg4/recovery-sequence"
    },
    {
      "id": "mail_fake_urgent",
      "sender": "SecuGate Support",
      "senderMail": "support@secugate-reset247.com",
      "subject": "URGENT! Your Access Has Been Disabled",
      "url": "http://secure-sg4-reset-now.com/verify"
    }
  ],
  "successEffect": {
    "type": "set_state",
    "stateId": "recovery_page_available"
  }
}
```

## `item_use`

Use Case: blauen USB-Stick am PC verwenden.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Meist eine Computer- oder Welt-Surface. |
| `slotType` | `item_target_slot` oder `computer_usb_slot`. |
| `target` | Zielobjekt, z. B. `pc_main`. |
| `requiredItemId` | Item, das Fortschritt erzeugt. |
| `candidateItemIds` | Items, die der Spieler auswählen kann. |
| `wrongItemPolicy` | Für USB V0: `unknown_device_shutdown_retry`. |
| `successEffect` | Effekt nach korrektem Item. |

Optional:

- `consumeItem`
- `wrongItemFeedback`
- `successFeedback`

The-Last-Hour-Referenz: Ein falscher USB-Stick setzt den Computer in den
speziellen `Unknown Device`-Virus-/Security-Zustand. Der Computer zeigt einen
Security-Tab, fährt nach kurzer Zeit herunter, setzt den Zustand auf
eingeschaltet aber nicht eingeloggt zurück und leert lokal die Login-Felder.
Der Fortschritt bleibt erhalten und der Spieler kann danach erneut versuchen.

```json
{
  "surfaceId": "s_main_computer",
  "slotType": "computer_usb_slot",
  "target": "pc_main",
  "requiredItemId": "item_usb_blue",
  "candidateItemIds": [
    "item_usb_red",
    "item_usb_green",
    "item_usb_yellow",
    "item_usb_blue"
  ],
  "wrongItemPolicy": "unknown_device_shutdown_retry",
  "wrongItemFeedback": "Der PC meldet: Unbekanntes USB-Gerät.",
  "shutdownDelayMs": 10000,
  "resetState": "computer_on_logged_out",
  "consumeItem": true,
  "successEffect": {
    "type": "mount_item",
    "itemId": "item_usb_blue",
    "targetSurfaceId": "s_main_computer"
  }
}
```

## `assembly`

Use Case: Papierfragmente zusammensetzen, um finale Information zu erhalten.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Meist Welt-, Computer- oder Assembly-Surface. |
| `slotType` | `fragment_spawn_slot` oder `assembly_slot`. |
| `assemblyMode` | V0: `image_fragments`. |
| `sourceAssetId` | Bild, das zerschnitten wird. |
| `pieceCount` | Anzahl Fragmente. |
| `resultResourceId` | Ressource, die nach Erfolg verfügbar wird. |
| `successEffect` | Effekt nach erfolgreicher Assembly. |

Optional:

- `spawnFromAction`, z. B. `ac_on`
- `snapTolerance`
- `revealedAnswer`

```json
{
  "surfaceId": "s_assembly_area",
  "slotType": "fragment_spawn_slot",
  "assemblyMode": "image_fragments",
  "sourceAssetId": "asset_final_code",
  "pieceCount": 4,
  "spawnFromAction": "turn_ac_on",
  "resultResourceId": "res_final_exit_code",
  "revealedAnswer": "214795541",
  "successEffect": {
    "type": "reveal_resource",
    "resourceId": "res_final_exit_code"
  }
}
```

## `control_panel`

Use Case: Vent verbinden, AC einschalten, finale Tür öffnen.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surfaceId` | Computer- oder Control-Panel-Surface. |
| `slotType` | `computer_panel_slot`. |
| `controls` | Liste der Controls. |
| `completionState` | Interner Zustand, der das Rätsel abschließt. |
| `successEffect` | Effekt, wenn `completionState` erreicht wird. |

Pflicht pro Control:

| Feld | Bedeutung |
|---|---|
| `id` | Control-ID. |
| `kind` | `button`, `toggle`, `text_input`, `password_input`. |
| `setsState` | Zustand, der nach Erfolg gesetzt wird. |

Optional pro Control:

- `label`
- `answer` für `text_input` und `password_input`
- `requiresStates`
- `disabledUntilStates`
- `feedback`

```json
{
  "surfaceId": "s_main_computer",
  "slotType": "computer_panel_slot",
  "controls": [
    {
      "id": "connect_ac",
      "kind": "text_input",
      "label": "Vent Serial",
      "answer": "49221",
      "setsState": "ac_vent_connected"
    },
    {
      "id": "turn_ac_on",
      "kind": "toggle",
      "label": "Air Conditioning",
      "requiresStates": ["ac_vent_connected"],
      "setsState": "ac_started"
    }
  ],
  "completionState": "ac_started",
  "successEffect": {
    "type": "spawn_content",
    "resourceId": "res_final_code_fragments"
  }
}
```

## `resources`

Resources sind normale Informationen im Raum oder Computer. Sie sind keine
Hints und erzeugen keine Tokens.

Pflicht pro Resource:

| Feld | Bedeutung |
|---|---|
| `id` | Resource-ID. |
| `kind` | `inline_text`, `asset`, `world_object`, `computer_file`. |
| `title` | Sichtbarer Name. |
| `availability` | `visible_in_level`, `inside_container`, `after_token`, `generated_by_riddle`. |
| `purpose` | `clue`, `context`, `instruction`, `decoy`. |

Zusätzlich:

- Bei `kind=asset`: `assetId`
- Bei `availability=after_token`: `requiredToken`
- Bei Inline-Text: `text`

## `hints`

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

`unlock` besteht aus:

| Feld | Bedeutung |
|---|---|
| `operator` | `all_of` oder `any_of`. |
| `conditions` | Eine oder mehrere Bedingungen. |

V0-Bedingungen:

| Bedingung | Pflichtfelder | Bedeutung |
|---|---|---|
| `elapsed_time` | `seconds` | nach Zeitablauf freischalten |
| `failed_attempts` | `riddleId`, `count` | nach Fehlversuchen in einem Rätsel |
| `resource_viewed` | `resourceId` | nachdem eine Information gelesen wurde |
| `surface_visited` | `surfaceId` | nachdem ein Ort/eine Oberfläche besucht wurde |
| `riddle_completed` | `riddleId` | nachdem ein Rätsel gelöst wurde |
| `token_available` | `token` | interne technische Bedingung für Generator/Runtime |

Der Wizard sollte `token_available` nicht als primäre Lehrenden-Option
anzeigen. Für Lehrende sind die fachlichen Varianten wie "nach gelesener
Information" oder "nach Fehlversuchen" besser.

## V0-UI-Bausteinpalette

Für den UI-Prototyp sollen alle aktuell aus The Last Hour ableitbaren
Bausteine auswählbar sein. Die Tabelle beschreibt die Authoring-Oberfläche,
nicht die Reihenfolge der späteren Runtime-Implementierung.

| Baustein | In UI auswählbar? | Begründung |
|---|---:|---|
| `state_change.confirm` | ja | Stromschalter. |
| `collection.single` | ja | Notizen, USB, Ressourcen. |
| `collection.trash_minigame` | ja | Papierkorb-Fund aus The Last Hour. |
| `input.credentials` | ja | Computer-Login. |
| `input.numeric` | ja | Keypad, Morse-Code, finale Codes. |
| `input.decoded_text` | ja | Binary/ASCII-Recovery. |
| `choice.email_list` | ja | E-Mail-/URL-Erkennung im Computer. |
| `item_use.unknown_device_shutdown_retry` | ja | mehrere USB-Sticks mit echter The-Last-Hour-Fehlerreaktion, aber ohne Softlock. |
| `assembly.image_fragments` | ja | finales Papierfragment-Bild. |
| `control_panel` | ja | Vent, AC, finale Tür. |

## Entscheidungen

1. `surfaceId` bleibt dort relevant, wo mehrere Oberflächen möglich sind oder
   ein wiederverwendbares Objekt wie ein Computer adressiert wird.
2. `successEffect` ist eine kontrollierte Wizard-Auswahl und wird in JSON als
   strukturierter Effekt gespeichert.
3. `choice.email_list` soll nach Möglichkeit in den Computer integriert werden.
4. Falsche USB-Sticks nutzen das The-Last-Hour-nahe Verhalten
   `unknown_device_shutdown_retry`, ohne Softlock.
