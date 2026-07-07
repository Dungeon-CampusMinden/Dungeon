# Wizard UI Flow V0

Stand: 06.07.2026
Status: UI-Contract für Schritte, Eingaben, Validierung und Export

## Ziel

Dieses Dokument beschreibt den sichtbaren Authoring-Flow für Lehrende. Es legt
Schritte, Eingaben, Validierungszeitpunkte, deaktivierte Zustände und den
Export von `deer.zip` als DEER-Authoring-Bundle fest. Es ist keine
Layout-Vorgabe.

Der detaillierte Lehrenden-Workflow steht in
[`teacher-workflow-v0.md`](teacher-workflow-v0.md). Dieses Dokument bleibt die
kompakte Schrittübersicht.

```text
Rahmen festlegen
-> Szenario beschreiben
-> Bausteine wählen und Oberflächen ableiten
-> Rätselablauf konfigurieren
-> Rätsel, Inhalte, Assets und Hinweise ergänzen
-> Validieren und deer.zip als Authoring-Bundle herunterladen
```

## UI-Grundsätze

- Der Wizard ist eine separate Browser-/Standalone-Oberfläche.
- Lehrende bearbeiten keine JSON-Datei direkt.
- Technische Begriffe wie Token, Petri-Netz oder Generator-Action erscheinen
  nicht als zentrale UI-Begriffe.
- Jeder Schritt hat einen Status: `leer`, `unvollständig`, `gültig`,
  `Warnung` oder `Fehler`.
- Die Bundle-Erstellung ist deaktiviert, bis der Client-Preflight gültig ist.
- Fehler erscheinen am betroffenen Schritt, Rätsel oder Feld.
- Warnungen bleiben sichtbar und blockieren nicht.
- V0 startet ohne vorausgewählte Raumstruktur.
- The Last Hour liefert verfügbare Bausteine und Assets, aber keine Vorlage.
- Bausteine oder Optionen, die im aktuellen Generator-Slice nicht generierbar
  sind, erscheinen deaktiviert mit sichtbarem Grund.

## Schritte

1. **Übersicht**
2. **Rahmen**
3. **Szenario**
4. **Raum & Oberflächen**
5. **Rätselablauf**
6. **Rätsel bearbeiten**
7. **Inhalte & Assets**
8. **Prüfen & Paket Erstellen**

## 1. Übersicht

Zweck: Projektstatus sichtbar machen und die nächsten offenen Aufgaben zeigen.

Pflichtangaben: keine.

Anzeigen:

- Raumtitel
- Fortschritt der Wizard-Schritte
- Anzahl Rätsel
- offene Pflichtfelder
- blockierende Fehler
- Warnungen
- letzter gültiger Preflight-Status

Aktionen:

- zum nächsten offenen Schritt wechseln
- Entwurf speichern
- `deer.zip` als Authoring-Bundle erstellen und herunterladen, wenn der
  Preflight gültig ist

## 2. Rahmen

Zweck: Allgemeine Sitzungsdaten erfassen.

Pflichtfelder:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Raumtitel | `metadata.title` | nicht leer |
| Sprache | `metadata.locale` | V0-Standard: `de-DE` |
| Zielgruppe | `session.targetAudience` | nicht leer |
| Vorwissen | `session.priorKnowledge` | nicht leer |
| Spielerzahl min/max | `session.playerCount` | `1 <= min <= max` |
| Zeitlimit | `session.time.limitMinutes` | positive Zahl |
| Zeitmodus | `session.time.limitMode` | `hard` oder `soft` |

Fest für V0:

- Theme: Standard-Theme.
- Levelanzahl: ein Level.
- Kooperationsmodus: kooperativ.

Zeitmodus:

- `hard`: Nach Ablauf endet der Raum.
- `soft`: Nach Ablauf bleibt der Raum spielbar; Hinweise oder Unterstützung
  können stärker werden.

## 3. Szenario

Zweck: Story-Rahmen für den Raum erfassen.

Pflichtfelder:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Rolle der Spielenden | `scenario.playerRole` | nicht leer |
| Ausgangslage | `scenario.premise` | kurzer Fließtext |
| Mission | `scenario.mission` | klares Spielziel |
| Intro-Text | `scenario.introText` | nicht leer |
| Erfolgstext | `scenario.successText` | nicht leer |
| Fehlschlagtext | `scenario.failureText` | nicht leer |

Optionale Felder:

- ein bis drei Lore-Texte
- Lore-Bild
- Intro-/Ambient-Audio

Client-Warnungen:

- Text sehr lang
- Mission unklar
- Intro beschreibt Fachinhalt ohne Spielsituation

## 4. Raum & Oberflächen

Zweck: Sichtbar machen, welche Interaktionsorte aus den gewählten Bausteinen
entstehen.

V0 startet nicht mit vorausgewählten Oberflächen. Oberflächen werden aus den
gewählten Rätselbausteinen abgeleitet. Wenn Lehrende z. B. ein
Computer-Login-Rätsel anlegen, erzeugt die UI daraus einen benötigten Computer
oder bietet einen vorhandenen kompatiblen Computer zur Auswahl an.

Mögliche abgeleitete Oberflächen:

| Oberfläche | Sichtbarer Name | Zweck |
|---|---|---|
| `computer_main` | Labor-PC | Login, E-Mails, Browser, Dateien, USB, Control Panel |
| `keypad_storage` | Storage-Keypad | Zahlencode für Storage |
| `door_storage` | Storage-Tür | durch Keypad oder Control Panel öffnen |
| `door_exit` | Ausgangstür | final öffnen |
| `vent_main` | Lüftung | Seriennummer und Papierfragmente |
| `trash_slots` | Papierkörbe | Funde und Trash-Minispiel |
| `container_slots` | Container/Schreibtische/Regale | Hinweise und Items |
| `assembly_area` | Fragmentbereich | Bildfragmente zusammensetzen |

Pflichtangaben entstehen aus den gewählten Rätseln:

| UI-Feld | Bedeutung | Validierung |
|---|---|---|
| mindestens ein Computer | für computernahe Rätsel | vorhanden, wenn Computer-Rätsel genutzt werden |
| mindestens ein Keypad-Slot | für Keypad-Rätsel | vorhanden, wenn `input.numeric` als Keypad genutzt wird |
| mindestens ein Container/Fundort | für `collection` | vorhanden, wenn Fund-Rätsel genutzt werden |
| mindestens ein Assembly-Bereich | für Fragmente | vorhanden, wenn `assembly.image_fragments` genutzt wird |

Lehrende wählen primär Bausteine wie "Computer-Login", "Keypad" oder
"Control Panel". Die UI leitet daraus die benötigten Oberflächen ab und zeigt
sie zur Kontrolle an. Werte wie Passwort oder Code können vorgeschlagen oder
manuell festgelegt werden.

Intern schreibt die UI die abgeleiteten Oberflächen in `surfaces`. Die
fachliche Auswahl "Labor-PC" wird im JSON z. B. zu einer `surfaceId` wie
`s_main_computer`. Lehrende sehen diese IDs nur, wenn die UI dafür einen
technischen Diagnosebereich anbietet.

## 5. Rätselablauf

Zweck: Festlegen, welche Rätsel in welcher Reihenfolge gelöst werden müssen.

Fachliches Modell:

- strukturierte Ablauf-Liste mit optionalen Parallelgruppen
- jede Rätselinstanz als einzelnes bearbeitbares Element
- Abhängigkeiten als fachliche Freischaltungen
- keine sichtbaren Token-Namen für Lehrende
- freie Darstellung, solange eine eindeutige Reihenfolge mit Parallelgruppen
  ableitbar bleibt

Pflichtangaben pro Knoten:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Rätselname | `riddle.title` | eindeutig genug |
| Baustein-Typ | `riddle.type` | V0-Typ |
| Kurzaufgabe | `playerFacingTask` | nicht leer |
| Vorgänger | `requiresTokens` indirekt | kein Zyklus |
| Ergebnis/Freischaltung | `producesTokens` indirekt | passt zu späterem Schritt |

V0-Regeln:

- Alle Progressionsrätsel liegen auf einem durchspielbaren Pfad.
- V0 erzeugt keine optionalen Rätselpfade.
- Parallelgruppen drücken Parallelität aus, ohne Progressionsrätsel zu
  überspringen.
- Der Editor markiert Zyklen, unerreichbare Knoten und Abhängigkeiten, die erst
  nach dem benötigten Rätsel verfügbar werden.

## 6. Rätsel Bearbeiten

Zweck: Die konkreten Eingaben für jedes Rätsel erfassen. Die UI zeigt nur die
Felder, die zum gewählten Baustein passen.

### 6.1 Stromschalter / `state_change.confirm`

Pflichtfelder:

- sichtbarer Name
- Aufgabe für Spielende
- Oberfläche/Fundort, meist `world`
- Zielobjekt, z. B. Schalter
- Bestätigungsfrage
- Erfolgstext
- Freischaltung aus kontrollierter Auswahl, z. B. "Computer einschalten"

Optionale Felder:

- Abbrechen-Text
- Sound

### 6.2 Fund / `collection`

Pflichtfelder:

- sichtbarer Name
- Aufgabe für Spielende
- Fundtyp: Container, Weltobjekt, Papierkorb-Minispiel, Computer-Datei
- Fundort/Oberfläche
- Reward oder Ressource
- Freischaltung aus kontrollierter Auswahl

Zusätzlich bei Papierkorb-Minispiel:

- Anzahl Papierobjekte
- Asset für gefundenes Objekt

### 6.3 Computer-Login / `input.credentials`

Pflichtfelder:

- Computer-Oberfläche
- Feldliste
- je Feld: Label, erwarteter Wert, geheim ja/nein
- Erfolg: Computer-Tabs freischalten

The-Last-Hour-nahe Vorgabe:

- Feld 1: E-Mail
- Feld 2: Passwort

### 6.4 E-Mail-Auswahl / `choice.email_list`

Pflichtfelder:

- Computer-Oberfläche
- Liste von E-Mails
- mindestens zwei Optionen
- genau eine korrekte Option
- pro E-Mail: Absender, Absenderadresse, Betreff, Inhalt
- bei Link-Aufgaben: Linktext und URL
- Erfolg: Recovery-/Browser-Seite freischalten

V0-Entscheidung:

- bevorzugt als Computer-Tab
- einfacher Dialog als technischer Fallback

### 6.5 Decoding-Eingabe / `input.decoded_text`

Pflichtfelder:

- Oberfläche, meist Computer
- kodierter Wert
- erwartete Antwort
- Decoding-Schritte, z. B. Binary -> Hex -> ASCII
- Ressourcen, die die Decoding-Schritte erklären
- Erfolg: Dokument oder nächstes Rätsel freischalten

### 6.6 Keypad / `input.numeric`

Pflichtfelder:

- Keypad-Oberfläche
- erwarteter Zahlencode
- maximale Länge
- Erfolg: Tür öffnen oder Bereich freischalten

Optionale Felder:

- Feedback bei falscher Eingabe
- Ziffernanzahl anzeigen ja/nein

### 6.7 USB Verwenden / `item_use`

Pflichtfelder:

- Computer-Oberfläche
- Zielobjekt, meist PC
- Liste verfügbarer USB-Sticks
- korrekter Stick
- Verhalten bei falschem Stick
- Erfolg: USB-Laufwerk oder Control-Panel-Zugang freischalten

The-Last-Hour-nahe Vorgabe:

- mehrere farbige USB-Sticks
- blauer USB ist korrekt
- falscher USB erzeugt `Unknown Device`
- nach kurzer Zeit Reset auf eingeschalteten, ausgeloggten PC
- erneuter Versuch bleibt möglich

### 6.8 Control Panel / `control_panel`

Pflichtfelder:

- Computer-/Panel-Oberfläche
- Liste der Controls
- Abschlusszustand
- Freischaltung bei Abschluss

Pflicht pro Control:

- Label
- Typ: Button, Toggle, Textfeld, Passwortfeld
- erwarteter Wert, falls Eingabefeld
- gesetzter interner Zustand
- benötigte vorherige Panel-Zustände, falls vorhanden

The-Last-Hour-nahe Controls:

- Vent-Seriennummer eingeben
- AC einschalten
- finale Tür mit Passwort entsperren
- finale Tür öffnen

### 6.9 Bildfragmente / `assembly.image_fragments`

Pflichtfelder:

- Ausgangsbild
- Anzahl Fragmente
- Spawn-/Startauslöser, z. B. AC eingeschaltet
- Ergebnis-Ressource, z. B. finales Code-Bild
- Erfolg: finale Information verfügbar machen

V0 nutzt das vorhandene Puzzle-/Item-System.

## 7. Inhalte & Assets

Zweck: Texte, Bilder und Audio-Dateien an einer Stelle verwalten.

Pflichtbereiche:

| Bereich | Pflicht, wenn... |
|---|---|
| Texte | ein Rätsel Text, Lore oder Ressource nutzt |
| Bilder | eine Ressource oder Assembly ein Bild nutzt |
| Audio | Audio in Szenario oder Feedback aktiviert ist |
| Hinweise | optional, aber pro Rätsel als leeres Array vorhanden |

Hint-Freischaltung:

- sofort verfügbar
- nach Zeit
- nach Fehlversuchen
- nachdem eine Information gelesen wurde
- nachdem eine Oberfläche/ein Ort besucht wurde
- nachdem ein Rätsel gelöst wurde

Die UI benennt diese Bedingungen fachlich. Petri-Net-Tokens bleiben interne
Generator-/Runtime-Details.

V0-Eingaben:

- Text direkt im Wizard
- Bilder als Upload
- Audio als Upload

Theme-, Tileset-, Sprite-, UI-Skin- und Office/PDF-Uploads sind keine
Eingaben dieses V0-Flows.

## 8. Prüfen & Paket Erstellen

Zweck: Lehrende sehen vor dem Erstellen des DEER-Authoring-Bundles `deer.zip`
eine klare, nicht-technische Checkliste.

Blockierende Fehler:

- Pflichtfeld fehlt
- Rätsel ohne Fundort/Oberfläche
- benötigte Ressource fehlt
- Asset fehlt
- Rätsel im Ablauf nicht erreichbar
- Progression kann nicht abgeschlossen werden
- Progressionsrätsel kann übersprungen werden
- Softlock oder zyklische Abhängigkeit
- Aktion passt nicht zur gewählten Oberfläche
- Computer-Rätsel ohne Computer
- Keypad-Rätsel ohne Keypad
- verwendeter Baustein ist im aktuellen Generator-Slice nicht generierbar

Warnungen:

- sehr lange Texte
- Rätsel ohne Hinweise
- viele Rätsel ohne klare Story-Einbettung
- erwartete Dauer deutlich höher als Zeitlimit

Hauptaktion:

- `Paket erstellen` / `deer.zip herunterladen`

Der Button ist deaktiviert, solange blockierende Fehler existieren. V0 enthält
in diesem Schritt keine Live-Preview, keinen Neu-Generieren-Button, keinen
Import bestehender `deer.zip`-Pakete und keinen Generator-Start.

## Bausteinkatalog Aus The Last Hour

Diese Liste beschreibt die The-Last-Hour-nahen Bausteine, die in V0 verfügbar
sein können. Sie ist keine vorausgewählte Raumstruktur.

1. Strom einschalten
2. Login-Hinweise finden
3. Computer-Login
4. richtige E-Mail erkennen
5. Recovery-Code decodieren
6. Storage-Code aus Dokument entschlüsseln
7. Storage-Keypad öffnen
8. richtigen USB-Stick finden
9. USB am PC verwenden
10. Vent-Seriennummer im Control Panel eintragen
11. AC einschalten
12. Bildfragmente zusammensetzen
13. finale Tür öffnen

Lehrende bauen den Raum selbst aus diesen Bausteinen zusammen. Die UI kann
Beispiele oder leere Baustein-Instanzen anbieten, ohne eine fertige
Raumstruktur vorauszuwählen.
