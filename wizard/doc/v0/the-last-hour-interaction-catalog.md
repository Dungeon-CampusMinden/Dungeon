# The Last Hour Interaction Catalog

Status: nicht-normatives Inventar; beschriebener Foundation-Zielslice festgelegt

Dieses Dokument beschreibt relevante Elemente aus `theLastHourEscapeRoom`. Es
erweitert weder den DEER-Vertrag noch die vom Runner ausführbaren Mechaniken.
Maßgeblich bleiben [`deer.schema.json`](deer.schema.json) und
[`deer-json-spec.md`](deer-json-spec.md).

## Grenze

- Code und Assets aus The Last Hour können wiederverwendet werden.
- Das bestehende Level wird nicht übernommen; der Runner baut seinen Raum
  deterministisch im Speicher.
- Ziel ist kein exakter Nachbau.
- The Last Hour ist keine vorausgewählte Wizard-Vorlage.
- Telefon-Dialoge sind Story- oder Inhaltsereignisse, keine eigenen Rätsel.

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

## Foundation-Zielslice

Nur diese Zuordnungen gehören zum festgelegten Vertrag in Schema und Runner:

| Originalelement | Spieleraktion | DEER-Abbildung |
|---|---|---|
| Login-Notiz oder Code-Hinweis | Inhalt finden | Informationsquelle mit Container-`surfaceId` und Text-/Bild-Resources; nur bei verpflichtendem Entdecken zusätzlich ein Collection-Input |
| Storage-Keypad oder Tür-Keypad | Zahlencode eingeben | Numeric-Input mit Keypad-`surfaceId`, `answer` und `showDigitCount` |
| Ausgang | nach den direkten Vorgängerrätseln gemeinsam verlassen | Endknoten mit Door-`surfaceId`; dieselbe ID bleibt Door-/Exit-Identität |

Der Foundation-Zielslice deckt damit die Kette von UI-Eingabe über
`deer.json`, Assetreferenzen und Graphvalidierung bis zur deterministischen
In-Memory-Ableitung ab. Die vollständige Parametersemantik steht
ausschließlich in der DEER-Spezifikation. Der Soll-Slice erhält eine geteilte
Surface-Identität über Authoring, Placement und Runtime und kennt nur `world`,
`container`, `keypad` und `door`. Rätsel selbst besitzen mindestens einen
Input und erzeugen ausschließlich ihren impliziten Abschluss für den Graphen.

Der vorhandene The-Last-Hour-Computer wird nicht als `device` oder als
zusätzlicher V0-Typ erfunden. Ein späterer Computer als eigene Surface-Art oder
mehrere Rätselbindungen an dieselbe Surface wären eine explizite Erweiterung
von Schema, Profil, Validierung und Runtime.

## Inventar weiterer vorhandener Interaktionen

Die folgende Tabelle ist Bestandsaufnahme, kein Datenmodell und keine
Umsetzungszusage:

| Originalelement | Beobachtbare Spieleraktion |
|---|---|
| Stromschalter unter Papier | versteckten Schalter finden und aktivieren |
| Papierkorb-Minispiel | Ablenkungen verschieben und Ziel freilegen |
| Computer-Login | Benutzername und Passwort eingeben |
| E-Mail-Postfach und Recovery-Link | Nachricht lesen und Link auswählen |
| Recovery-Webseite | Textcode herleiten und eingeben |
| USB am PC | Gegenstand finden und in ein Gerät einsetzen |
| Vent-Seriennummer | Nummer ablesen und eingeben |
| Klimaanlage | sichtbaren Zustand umschalten |
| Bildfragmente | mehrere Teile zu einem lesbaren Bild zusammensetzen |
| Finale Tür | ermittelten Code eingeben |

## Einordnung vorhandener Elemente

| Element | Beobachtete Rolle |
|---|---|
| Intro und Outro | Szenariotext |
| Timer | Sitzungszustand |
| Telefonanrufe | Story- oder Inhaltsereignis |
| Virus- und Falschaktionssystem | Runtime-Feedback |
| Licht, Heizung und Kamera | Zustände im Control Panel |
| Decoy-Vents, leere Container und Fake-Dateien | Ablenkungsinhalte |

Neue Mechaniken werden erst dann Teil des Wizard-Vertrags, wenn Schema,
Spezifikation, Validierung, deterministische Ableitung und Runtime sie gemeinsam
vollständig unterstützen.
