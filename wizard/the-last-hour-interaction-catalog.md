# The Last Hour Interaction Catalog V0

Stand: 29.06.2026  
Zweck: Wizard-nahe Zerlegung der wiederverwendbaren Spiel-Elemente aus
`theLastHourEscapeRoom`.

## Ausgangsentscheidungen

- Code und Assets aus The Last Hour sollen wiederverwendet werden.
- Das bestehende Level wird nicht uebernommen. Der Raum wird vom Generator neu
  aufgebaut.
- Ziel ist kein exakter Nachbau, sondern eine Wizard-Version der relevanten
  Interaktionen.
- Der Wizard startet ohne vorausgewaehlte The-Last-Hour-Vorlage. The Last Hour
  liefert in V0 nur den verfuegbaren Baustein-Katalog.
- V0 modelliert keine eigenstaendigen Red-Herring-Raetsel. Falsche Optionen
  duerfen aber innerhalb eines konkreten Auswahl- oder Item-Use-Raetsels
  existieren, wenn die Mechanik das braucht.
- Hinweise bleiben optional. Ein Raetsel hat immer ein `hints`-Array, aber es
  darf leer sein.
- Telefon-Dialoge sind fuer V0 eher Story-Events oder Ressourcen, keine eigenen
  Wizard-Raetsel.
- Der Computer soll langfristig als zentrale wiederverwendbare Schnittstelle
  erhalten bleiben. V0 darf aber einzelne Computer-Aufgaben als vereinfachte,
  konfigurierte Tabs oder Dialoge abbilden, statt direkt den kompletten
  The-Last-Hour-Computer zu generalisieren.

## Gelesene Referenzstellen

- `theLastHourEscapeRoom/src/level/LastHourLevel.java`
- `theLastHourEscapeRoom/src/util/Lore.java`
- `theLastHourEscapeRoom/src/modules/computer/ComputerFactory.java`
- `theLastHourEscapeRoom/src/modules/computer/ComputerStateComponent.java`
- `theLastHourEscapeRoom/src/modules/computer/content/LoginTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/EmailsTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/BrowserTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/FileTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/UsbDriveTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/ControlPanelTab.java`
- `theLastHourEscapeRoom/src/modules/trash/TrashMinigameFactory.java`
- `theLastHourEscapeRoom/src/modules/usbstick/UsbStickItem.java`

## Wizard-Nahe Progression

| Schritt | Originalelement | Spieleraktion | Wizard-Baustein | V0-Entscheidung |
|---|---|---|---|---|
| 1 | Stromschalter unter Papier | Schalter finden und bestaetigen | `state_change` | Beibehalten als einfache Weltinteraktion. |
| 2 | Login-Notizen auf Schreibtisch und im Papierkorb | Hinweise finden/einsammeln | `collection` + `resources` | Beibehalten, aber generisch als Fund-/Resource-Mechanik. |
| 3 | Computer-Login | E-Mail und Passwort eingeben | `input` mit `inputMode=credentials` | Beibehalten. |
| 4 | E-Mail-Postfach und Recovery-Link | richtige Mail/URL erkennen | `choice` | Als zentrale Computer-Aufgabe behalten. V0 sollte sie in den Computer integrieren, aber nur als konfigurierten Choice-/Mail-Tab statt direkt als komplett generalisierten Mail-Client. |
| 5 | Recovery-Webseite mit Binary/ASCII-Code | kodierten Wert entschluesseln und eingeben | `input` mit `inputMode=decoded_text` | Beibehalten; Decoder-Tabellen sind Ressourcen. |
| 6 | Download-Dokument mit Morse-Code | Morse-Code in Zahlencode uebersetzen | `input` mit `inputMode=numeric` | Beibehalten; Dokument und Morse-Tabelle sind Ressourcen. |
| 7 | Storage-Keypad | Zahlencode eingeben | `input` mit `inputMode=numeric` | Beibehalten, konkretes Runtime-Mapping auf Keypad. |
| 8 | USB-Stick-Hinweis und blauer Stick | richtigen USB finden | `collection` | Mehrere USB-Sticks bleiben erhalten; der richtige Stick ist Progression, falsche Sticks sind Optionen innerhalb derselben Aufgabe. |
| 9 | USB am PC verwenden | richtigen Gegenstand an Ziel benutzen | `item_use` | Beibehalten; falscher USB erzeugt den The-Last-Hour-Unknown-Device-Security-Zustand, faehrt den PC nach kurzer Zeit auf Login zurueck und erlaubt danach Retry. |
| 10 | USB-Datei `control-panel.key` | Control Panel freischalten | `collection` oder `item_use`-Folge | Fuer V0 als Folge des richtigen USB-Gates modellieren. |
| 11 | Vent-Seriennummer | Seriennummer lesen und im Panel eingeben | `control_panel` mit `text_input` | Beibehalten; Vent-Dialog ist Resource. |
| 12 | AC einschalten | Toggle nach erfolgreicher Verbindung | `control_panel` mit `toggle` | Beibehalten; setzt Spawn/Reveal der finalen Ressource aus. |
| 13 | Bildfragmente aus Vent | Fragmente sammeln/zusammensetzen | `assembly` | Beibehalten als Assembly: mehrere Schnipsel muessen zu einem Bild zusammengesetzt werden, das danach eine Ressource/Information offenlegt. |
| 14 | Finale Tuer | Passwort eingeben und Tuer oeffnen | `control_panel` | Beibehalten. |

## Nicht Als V0-Raetsel

| Element | Grund |
|---|---|
| Intro/Outro | Szenario-Text, kein Raetsel. |
| Timer | Session-/Scenario-Konfiguration. |
| Telefonanrufe | Story-Event/Ressource; zu speziell fuer den ersten Wizard-Baustein. |
| Allgemeines Virus-/Falschaktion-System | Zu breit fuer V0. Die konkrete USB-Unknown-Device-Reaktion wird aber als Teil von `item_use` uebernommen. |
| Licht, Heizung, Kamera im Control Panel | Gute UI-Demonstration, aber fuer Progression nicht notwendig. |
| Decoy-Vents, leere Container, Fake-Dateien | Erstmal keine eigenstaendigen Red-Herrings in V0. |

## Computer-Strategie

Der Computer ist in The Last Hour nicht nur ein einzelnes Raetsel, sondern ein
wiederkehrender Interaktionsort: Login, E-Mails, Browser, Dateien, USB-Laufwerk
und Control Panel laufen dort zusammen. Deshalb sollte der Wizard den Computer
nicht als viele unverbundene Einzelobjekte behandeln.

Empfehlung:

1. **Zielbild:** ein zentraler Computer pro Raum oder Szenario, der mehrere vom
   Generator konfigurierte Tabs aufnehmen kann.
2. **V0-Umsetzung:** vorhandene Computer-Codebasis wiederverwenden, aber nur
   eine kleine Menge generischer Tab-/Dialogmuster parametrisieren:
   `login`, `choice`, `file/resource`, `usb_drive`, `control_panel`.
3. **Fallback:** Wenn die Generalisierung des Computers zu gross wird, darf ein
   einzelnes Raetsel weiterhin ueber einen normalen Dialog laufen. Das sollte
   aber als technische Vereinfachung gelten, nicht als langfristiges
   Autorierungsmodell.

Damit bleibt der Computer als zentrale Schnittstelle sichtbar, ohne dass V0
sofort den kompletten The-Last-Hour-Computer als frei konfigurierbare Plattform
implementieren muss.

Fuer `choice.email_list` sollte der Computer bevorzugt werden. Ein einfacher
Dialog ist nur Fallback, falls die technische Generalisierung des Tabs fuer den
ersten Prototyp zu gross wird.

## Collection vs. Assembly

`collection` bedeutet: Der Spieler findet, sammelt oder oeffnet etwas. Die
eigentliche Herausforderung ist der Fundort, die Zugangsbedingung oder das
Durchsuchen eines Containers.

Beispiele:

- Login-Notiz im Schreibtisch finden.
- Hinweis im Papierkorb-Minispiel finden.
- richtigen USB-Stick finden.
- Datei im Computer als Ressource oeffnen.

`assembly` bedeutet: Mehrere Teile muessen aktiv zusammengesetzt, geordnet oder
kombiniert werden, damit daraus eine neue Information entsteht.

Beispiele:

- Bildschnipsel zu einem lesbaren finalen Code zusammensetzen.
- mehrere Fragmente in die richtige Reihenfolge bringen.
- aus mehreren Teilgrafiken eine lesbare Ressource erzeugen.

Das finale Papierfragment-Raetsel aus The Last Hour ist deshalb `assembly`, nicht
`collection`: Das Ergebnis ist nicht nur ein gefundener Gegenstand, sondern ein
zusammengesetztes Bild, das danach als Ressource den finalen Code liefert.

## Erste Pflichtparameter Pro Baustein

### Gemeinsame Pflichtfelder

Jeder Baustein braucht:

- `id`
- `type`
- `title`
- `playerFacingTask`
- `requiresTokens`
- `producesTokens`
- `resources`
- `hints`
- `parameters`

`hints` ist immer vorhanden, aber optional leer.

### `state_change`

Pflicht:

- `interactionKind`: z. B. `confirm`
- `target`: logischer Zielname, z. B. `power_switch`
- `successText`
- `successAction`

Optional:

- `promptText`
- `cancelText`
- Sound-/Feedback-Referenz

### `collection`

Pflicht:

- `rewardMode`: `single`, `collect_all` oder `find_resource`
- `sourceKind`: z. B. `container`, `world_object`, `trash_minigame`,
  `computer_file`
- `rewards` oder `resourceIds`

Optional:

- `paperCount` fuer Trash-Minispiel
- `consumeOnCollect`
- `repeatable`

### `input`

Pflicht:

- `inputMode`
- Loesungsdefinition, abhaengig vom Modus:
  - `numeric`: `answer`, `maxLength`
  - `text`: `answer`
  - `credentials`: `fields`
  - `decoded_text`: `encodedValue`, `answer`, `decoderSteps`
- `successAction`

Optional:

- `caseSensitive`
- `attemptLimit`
- `wrongAnswerFeedback`

### `choice`

Pflicht:

- `selectionMode`
- `options`
- `correctOptionId` oder `correctOptionIds`
- `successAction`

Optional:

- `presentation`: z. B. `email_list`, `url_list`, `item_list`
- `wrongChoiceFeedback`
- `wrongChoiceConsequence`

Falsche Optionen sind erlaubt, wenn sie zur Aufgabe gehoeren. Sie sollten in V0
aber nicht als eigene Red-Herring-Knoten im Raetselgraph stehen.

### `item_use`

Pflicht:

- `requiredItemId`
- `target`
- `successAction`

Optional:

- `consumeItem`
- `wrongItemFeedback`
- `wrongItemPolicy`: z. B. `ignore`, `feedback_retry`, `temporary_reset`

V0-Empfehlung fuer USB: `wrongItemPolicy=feedback_retry`. Ein falscher USB-Stick
gibt Feedback oder einen PC-Fehler, verbraucht aber keinen Fortschritt und darf
endlos erneut versucht werden. `temporary_reset` sollte nur vorsichtig genutzt
werden, weil solche Mechaniken Softlock- und Frust-Risiken erhoehen.

### `control_panel`

Pflicht:

- `controls`
- `completionState`

Jedes progression-relevante Control braucht:

- `id`
- `kind`
- `setsState` oder eine klare Weltaktion

Optional:

- `requiresStates`
- `answer`
- `label`
- `feedback`

Controls erzeugen keine Graph-Tokens. Tokens entstehen nur auf Raetsel-Ebene.

### `assembly`

Pflicht:

- `assemblyMode`
- `assetId` oder `fragmentAssetIds`
- `pieceCount`
- `successAction`

Optional:

- `seed`
- `revealedAnswer`

Das finale Bildfragment-Raetsel aus The Last Hour sollte als `assembly`
modelliert werden. Nach erfolgreicher Assembly entsteht eine Ressource, z. B. ein
Bild oder Text, aus der die finale Loesung gelesen werden kann.

## Slot-Anforderungen

| Baustein | Benoetigte Slot-Typen |
|---|---|
| `state_change` | `world_interaction_slot` |
| `collection` | `container_slot`, `world_object_slot`, `trash_slot`, `computer_file_slot` |
| `input` numeric/keypad | `keypad_slot` oder `computer_input_slot` |
| `input` credentials | `computer_login_slot` |
| `choice` email/url | `computer_choice_slot` |
| `item_use` | `item_target_slot` plus Item-Quelle |
| `control_panel` | `computer_panel_slot` |
| `assembly` | `fragment_spawn_slot` plus Ergebnis-/Anzeige-Slot |

Generator-Regel: Eine Ressource, die zum Loesen eines Raetsels notwendig ist,
muss in einem Slot liegen, der spaetestens mit den `requiresTokens` dieses
Raetsels erreichbar ist.

## Naechste Klaerung

Aus diesem Katalog sollte als naechstes eine verbindliche Parameter-Tabelle fuer
die erste Implementierung entstehen. Besonders offen ist noch, wie weit die
Computer-Generalisierung in V0 gehen soll:

- zentraler Computer mit mehreren generierten Tabs,
- nur wiederverwendete Spezial-Tabs aus The Last Hour,
- oder normale Dialoge als technischer Fallback fuer einzelne Raetsel.

Ein erster Vorschlag fuer diese Parameter-Tabelle steht in
[`parameter-table-v0.md`](parameter-table-v0.md).

Der Wizard-Ablauf aus Lehrenden-Sicht steht in
[`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md).
