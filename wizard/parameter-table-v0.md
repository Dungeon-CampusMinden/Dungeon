# DEER Parameter Table V0 Draft

Stand: 29.06.2026  
Status: Vorschlag zur Diskussion

## Ziel

Diese Tabelle beschreibt, welche Parameter der Wizard fuer die erste
The-Last-Hour-abgeleitete Bausteinpalette erfassen sollte. Sie ist noch kein
finaler Schema-Contract. Nach Review kann daraus `deer.schema.json`
verschaerft werden.

Grundregel:

- Der Wizard fragt fachliche Inhalte und notwendige Entscheidungen ab.
- Oberflaechen entstehen aus den gewaehlten Bausteinen und werden in der UI
  fachlich benannt.
- Der spaetere Generator waehlt konkrete Positionen, Slot-Instanzen und
  technische Runtime-Details.
- Der Client validiert vor dem JSON-Export; der Java-Generator validiert spaeter
  erneut als Sicherheitsnetz.

## Gemeinsame Riddle-Felder

Jedes Raetsel braucht ausserhalb von `parameters`:

| Feld | Pflicht | Bedeutung |
|---|---:|---|
| `id` | ja | Stabile technische ID. |
| `type` | ja | Einer der V0-Typen. |
| `title` | ja | Interner und optional sichtbarer Titel. |
| `designRole` | ja | `progression`, `clue`, `story`, `support`, `decoy`. |
| `difficulty` | ja | `easy`, `medium`, `hard`. |
| `estimatedMinutes` | ja | Zeitannahme fuer Balance und Warnungen. |
| `playerFacingTask` | ja | Aufgabenformulierung fuer Wizard/Preview/UI. |
| `requiresTokens` | ja | Tokens, die vorher verfuegbar sein muessen. |
| `producesTokens` | ja | Tokens, die dieses Raetsel nach Erfolg erzeugt. |
| `assetIds` | ja | Direkt benoetigte Asset-Referenzen, sonst `[]`. |
| `resources` | ja | Normale Hinweise/Informationen im Raum, sonst `[]`. |
| `hints` | ja | Optionale Hilfen, sonst `[]`. |
| `parameters` | ja | Typ-spezifische Parameter. |

## Gemeinsame Parameter

Diese Felder koennen fuer alle Typen sinnvoll sein:

| Feld | Pflicht | Vorschlag |
|---|---:|---|
| `surface` | ja, wenn mehrere Oberflaechen moeglich sind | `world`, `computer`, `keypad`, `control_panel`, `inventory`. |
| `slotType` | ja | Gewuenschter Slot-Typ, z. B. `computer_slot`, `keypad_slot`. |
| `successAction` | ja | Kontrollierter Aktionswert, z. B. `open_door_storage`, `unlock_computer_ui`. |
| `successFeedback` | nein | Kurzer sichtbarer Erfolgstext. |
| `wrongFeedback` | nein | Kurzer Fehlertext bei falscher Eingabe/Auswahl. |
| `retryPolicy` | nein | Default `infinite_retry`; V0 sollte keine Progression dauerhaft blockieren. |

Der Wizard leitet benoetigte Oberflaechen und Slot-Typen aus den gewaehlten
Bausteinen ab. Lehrende sollen die Oberflaechen fachlich sehen und benennen
koennen, aber nicht zuerst eine technische Slot-Liste bauen muessen.

Der spaetere Generator darf `slotType` weiter verfeinern, aber der Wizard sollte
sichtbar machen, wenn ein Raetsel keinen kompatiblen Slot im geplanten Raum
findet.

`surface` ist besonders fuer wiederverwendbare Ziele wichtig. Damit kann der
Wizard z. B. mehrere Computer, Keypads oder Weltobjekte unterscheiden und
Raetsel an die richtige Oberflaeche binden.

`successAction` sollte nicht als beliebiger Nutzereingabe-String verstanden
werden. Der Wizard sollte nur Aktionen anbieten, die fuer den gewaehlten
Baustein, die Surface und die vorhandenen Slots valide sind. Intern kann der
Wert als String gespeichert werden, aber die Auswahl kommt aus einer
kontrollierten Aktionsliste.

## `state_change`

Use Case: Stromschalter, Hebel, einfacher Weltzustand.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surface` | Nur noetig, wenn mehrere Welt-/Interaktionsflaechen unterschieden werden muessen; meist `world`. |
| `slotType` | Meist `world_interaction_slot`. |
| `interactionKind` | V0: `confirm`. |
| `target` | Logischer Zielname, z. B. `power_switch`. |
| `successAction` | Welt-/State-Aktion. |
| `successFeedback` | Rueckmeldung nach Erfolg. |

Optional:

- `promptText`
- `cancelText`
- `soundCue`

```json
{
  "surface": "world",
  "slotType": "world_interaction_slot",
  "interactionKind": "confirm",
  "target": "power_switch",
  "successAction": "set_power_on",
  "successFeedback": "Die Stromversorgung springt an."
}
```

## `collection`

Use Case: Notiz finden, USB finden, Trash-Minispiel, Datei als Ressource
oeffnen.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surface` | Pflicht, wenn der Fund an eine bestimmte Oberflaeche gebunden ist: `world`, `computer` oder `inventory`. |
| `slotType` | `container_slot`, `trash_slot`, `world_object_slot`, `computer_file_slot`. |
| `sourceKind` | `container`, `world_object`, `trash_minigame`, `computer_file`. |
| `rewardMode` | `single`, `collect_all`, `find_resource`. |
| `successAction` | Token-/World-Aktion nach erfolgreichem Fund. |

Zusaetzlich Pflicht je nach Modus:

| Bedingung | Pflichtfeld |
|---|---|
| Item-Fund | `rewards` |
| Resource-Fund | `resourceIds` |
| `collect_all` | mindestens zwei Eintraege in `rewards` oder `resourceIds` |

Optional:

- `paperCount` bei `trash_minigame`
- `consumeOnCollect`
- `repeatable`

```json
{
  "surface": "world",
  "slotType": "trash_slot",
  "sourceKind": "trash_minigame",
  "rewardMode": "single",
  "rewards": ["asset_note_password_2"],
  "paperCount": 30,
  "successAction": "grant_login_note"
}
```

## `input`

Use Case: Keypad, Login, Text-/Codeeingabe, Decoding-Antwort.

Pflicht in `parameters` fuer alle `input`-Raetsel:

| Feld | Bedeutung |
|---|---|
| `surface` | Pflicht, wenn mehrere Eingabeoberflaechen moeglich sind: `computer`, `keypad` oder `control_panel`. |
| `slotType` | Passender Slot, z. B. `computer_login_slot`, `keypad_slot`. |
| `inputMode` | `numeric`, `text`, `credentials`, `decoded_text`. |
| `successAction` | Aktion nach korrekter Eingabe. |

### `inputMode=numeric`

Pflicht:

- `answer`
- `maxLength`

Optional:

- `minLength`
- `showDigitCount`
- `acceptedCharacters`, default `digits`

```json
{
  "surface": "keypad",
  "slotType": "keypad_slot",
  "inputMode": "numeric",
  "answer": "3758",
  "maxLength": 4,
  "successAction": "open_door_storage"
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
| `secret` | nein | Passwortmodus, default `false`. |
| `caseSensitive` | nein | Default `false`. |

```json
{
  "surface": "computer",
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
  "successAction": "unlock_computer_tabs"
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
  "surface": "computer",
  "slotType": "computer_input_slot",
  "inputMode": "decoded_text",
  "encodedValue": "00110110001101010011010000111000",
  "answer": "6548",
  "decoderSteps": ["binary_to_hex", "hex_to_ascii"],
  "decoderResourceIds": ["res_binary_hex_table", "res_hex_ascii_table"],
  "successAction": "unlock_access_code_document"
}
```

## `choice`

Use Case: richtige E-Mail, richtige URL, richtige Option erkennen.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surface` | Fuer The Last Hour meist `computer`. |
| `slotType` | Meist `computer_choice_slot`. |
| `presentation` | V0: `email_list`, `url_list`, `item_list`, `plain_options`. |
| `selectionMode` | V0 meistens `single_correct`. |
| `options` | Auswahloptionen. |
| `correctOptionId` | Bei `single_correct`. |
| `successAction` | Aktion nach korrekter Auswahl. |

Optionale Fehlerpolitik:

- `wrongChoicePolicy`: default `feedback_retry`
- `wrongChoiceFeedback`

V0-Empfehlung: falsche Optionen geben Feedback und erlauben Retry. Keine
dauerhafte Infektion, kein Progressionsverlust.

Fuer The Last Hour sollte `choice.email_list` nach Moeglichkeit in den Computer
integriert werden. Der erste technische Schnitt muss dafuer nicht den gesamten
aktuellen Mail-Client frei konfigurierbar machen; ausreichend waere ein
konfigurierter Computer-Tab, der strukturierte E-Mail-Optionen anzeigt und die
richtige Option prueft.

```json
{
  "surface": "computer",
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
  "successAction": "open_recovery_page"
}
```

## `item_use`

Use Case: blauen USB-Stick am PC verwenden.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surface` | Meist `computer` oder `world`. |
| `slotType` | `item_target_slot` oder `computer_usb_slot`. |
| `target` | Zielobjekt, z. B. `pc_main`. |
| `requiredItemId` | Item, das Fortschritt erzeugt. |
| `candidateItemIds` | Items, die der Spieler auswaehlen kann. |
| `wrongItemPolicy` | Fuer USB V0: `unknown_device_shutdown_retry`. |
| `successAction` | Aktion nach korrektem Item. |

Optional:

- `consumeItem`
- `wrongItemFeedback`
- `successFeedback`

The-Last-Hour-Referenz: Ein falscher USB-Stick setzt den Computer in den
speziellen `Unknown Device`-Virus-/Security-Zustand. Der Computer zeigt einen
Security-Tab, faehrt nach kurzer Zeit herunter, setzt den Zustand auf
eingeschaltet aber nicht eingeloggt zurueck und leert lokal die Login-Felder.
Der Fortschritt bleibt erhalten und der Spieler kann danach erneut versuchen.

```json
{
  "surface": "computer",
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
  "wrongItemFeedback": "Der PC meldet: Unbekanntes USB-Geraet.",
  "shutdownDelayMs": 10000,
  "resetState": "computer_on_logged_out",
  "consumeItem": true,
  "successAction": "mount_usb_drive"
}
```

## `assembly`

Use Case: Papierfragmente zusammensetzen, um finale Information zu erhalten.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surface` | Meist `world` oder `computer`. |
| `slotType` | `fragment_spawn_slot` oder `assembly_slot`. |
| `assemblyMode` | V0: `image_fragments`. |
| `sourceAssetId` | Bild, das zerschnitten wird. |
| `pieceCount` | Anzahl Fragmente. |
| `resultResourceId` | Ressource, die nach Erfolg verfuegbar wird. |
| `successAction` | Aktion nach erfolgreicher Assembly. |

Optional:

- `seed`
- `spawnFromAction`, z. B. `ac_on`
- `snapTolerance`
- `revealedAnswer`

```json
{
  "surface": "world",
  "slotType": "fragment_spawn_slot",
  "assemblyMode": "image_fragments",
  "sourceAssetId": "asset_final_code",
  "pieceCount": 4,
  "seed": 1586791695537379744,
  "spawnFromAction": "turn_ac_on",
  "resultResourceId": "res_final_exit_code",
  "revealedAnswer": "214795541",
  "successAction": "reveal_exit_password"
}
```

## `control_panel`

Use Case: Vent verbinden, AC einschalten, finale Tuer oeffnen.

Pflicht in `parameters`:

| Feld | Bedeutung |
|---|---|
| `surface` | `computer` oder `control_panel`. |
| `slotType` | `computer_panel_slot`. |
| `controls` | Liste der Controls. |
| `completionState` | Interner Zustand, der das Raetsel abschliesst. |
| `successAction` | Aktion, wenn `completionState` erreicht wird. |

Pflicht pro Control:

| Feld | Bedeutung |
|---|---|
| `id` | Control-ID. |
| `kind` | `button`, `toggle`, `text_input`, `password_input`. |
| `setsState` | Zustand, der nach Erfolg gesetzt wird. |

Optional pro Control:

- `label`
- `answer` fuer `text_input` und `password_input`
- `requiresStates`
- `disabledUntilStates`
- `feedback`

```json
{
  "surface": "computer",
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
  "successAction": "spawn_final_fragments"
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

Zusaetzlich:

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
| `level` | Eskalationsstufe, beginnend bei `1`. |
| `trigger` | `manual_request`, `after_failed_attempts`, `after_time`. |

## V0-UI-Bausteinpalette

Fuer den UI-Prototyp sollen alle aktuell aus The Last Hour ableitbaren
Bausteine auswaehlbar sein. Die Tabelle beschreibt die Authoring-Oberflaeche,
nicht die Reihenfolge der spaeteren Runtime-Implementierung.

| Baustein | In UI auswaehlbar? | Begruendung |
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
| `control_panel` | ja | Vent, AC, finale Tuer. |

## Entscheidungen Und Nachgelagerte Fragen

Entschieden:

1. `surface` bleibt dort relevant, wo mehrere Oberflaechen moeglich sind oder
   ein wiederverwendbares Objekt wie ein Computer adressiert wird.
2. `successAction` ist eine kontrollierte Wizard-Auswahl und wird in JSON als
   stabiler Aktionswert gespeichert.
3. `choice.email_list` soll nach Moeglichkeit in den Computer integriert werden.
4. Falsche USB-Sticks nutzen das The-Last-Hour-nahe Verhalten
   `unknown_device_shutdown_retry`, ohne Softlock.

Nachgelagert:

1. Ob `assembly.image_fragments` im ersten spielbaren Generator schon echte
   Drag-/Snap-Interaktion nutzt oder nur das bestehende Puzzle-/Item-System.
2. Wie stark `control_panel` in der Runtime frei konfigurierbar wird.
