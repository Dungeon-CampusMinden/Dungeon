# Wizard UI Flow V0

Stand: 07.07.2026
Status: UI-Contract für Schritte, Eingaben, Validierung und Abschluss

## Ziel

Dieses Dokument beschreibt den sichtbaren Authoring-Flow für Lehrende. Es legt
Schritte, Eingaben, Validierungszeitpunkte, deaktivierte Zustände und die
Abschlussaktion fest. Es ist keine Layout-Vorgabe.

```text
Rahmen festlegen
-> Szenario beschreiben
-> Foundation-Bausteine wählen
-> Rätselablauf konfigurieren
-> Rätsel, Inhalte, Assets und Hinweise ergänzen
-> Prüfen und Entwurf finalisieren
```

## UI-Grundsätze

- Der Wizard ist eine separate Browser-/Standalone-Oberfläche.
- Lehrende bearbeiten keine JSON-Datei direkt.
- Technische Begriffe wie Token, Petri-Netz oder Generator-Action erscheinen
  nicht als zentrale UI-Begriffe.
- Jeder Schritt hat einen Status: `leer`, `unvollständig`, `gültig`,
  `Warnung` oder `Fehler`.
- Die Finalisierung ist deaktiviert, bis der Client-Preflight gültig ist.
- Fehler erscheinen am betroffenen Schritt, Rätsel oder Feld.
- Warnungen bleiben sichtbar und blockieren nicht.
- V0 startet ohne vorausgewählte Raumstruktur.
- The Last Hour liefert mögliche Bausteine und Assets, aber keine Vorlage.
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
8. **Prüfen & Entwurf Finalisieren**

## 1. Übersicht

Zweck: Projektstatus sichtbar machen und die nächsten offenen Aufgaben zeigen.

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
- `Entwurf finalisieren`, wenn der Preflight gültig ist

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

Warnungen:

- Text sehr lang
- Mission unklar
- Intro beschreibt Fachinhalt ohne Spielsituation

## 4. Raum & Oberflächen

Zweck: Sichtbar machen, welche Interaktionsorte aus den gewählten Bausteinen
entstehen.

V0 startet nicht mit vorausgewählten Oberflächen. Oberflächen werden aus den
gewählten Rätselbausteinen abgeleitet.

Aktive Foundation-Oberflächen:

| Oberfläche | Sichtbarer Name | Zweck |
|---|---|---|
| `world` | Raum | allgemeiner Kontext |
| `container` | Fundort | Hinweis oder Ressource finden |
| `keypad` | Keypad | Zahlencode eingeben |
| `door` | Tür | Ziel öffnen |

Weitere Oberflächen bleiben deaktiviert, bis der Generator sie unterstützt.

Pflichtangaben entstehen aus den gewählten Rätseln:

| UI-Feld | Bedeutung | Validierung |
|---|---|---|
| mindestens ein Fundort | für `collection.single` | vorhanden, wenn Fund-Rätsel genutzt werden |
| mindestens ein Keypad | für `input.numeric` | vorhanden, wenn Zahlencode genutzt wird |
| mindestens eine Tür oder Zieloberfläche | für `open_surface` | vorhanden, wenn ein Rätsel eine Tür öffnet |

Intern schreibt die UI die abgeleiteten Oberflächen in `surfaces`. Lehrende
sehen fachliche Namen, nicht technische IDs.

## 5. Rätselablauf

Zweck: Festlegen, welche Rätsel in welcher Reihenfolge gelöst werden müssen.

Fachliches Modell:

- einfache Ablauf-Liste mit optionalen Parallelgruppen
- jede Rätselinstanz als einzelnes bearbeitbares Element
- Abhängigkeiten als "nach Rätsel X verfügbar"
- intern reine Graphkanten statt zusätzlicher Kantenbedingungen
- keine sichtbaren Token-Namen für Lehrende
- freie Darstellung, solange ein eindeutiger Graph ableitbar bleibt

Pflichtangaben pro Knoten:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Rätselname | `riddle.title` | eindeutig genug |
| Baustein-Typ | `riddle.type` plus Modusparameter | `collection` + `rewardMode=find_resource` oder `input` + `inputMode=numeric` |
| Kurzaufgabe | `playerFacingTask` | nicht leer |
| Vorgänger | `riddleGraph.edges[].from`/`to` | existierendes Rätsel oder Start |
| Ergebnis/Freischaltung | typabhängig, z. B. `parameters.successEffect` | kontrollierte Auswahl, wenn der Baustein einen Effekt braucht |

V0-Regeln:

- Alle Progressionsrätsel liegen auf einem durchspielbaren Pfad.
- Alle Progressionsrätsel bleiben notwendig.
- V0 und spätere Versionen erzeugen genau einen Endzustand.
- Parallelgruppen drücken Parallelität aus, ohne Progressionsrätsel zu
  überspringen.
- Der Editor markiert Zyklen, unerreichbare Knoten und Abhängigkeiten, die erst
  nach dem benötigten Rätsel verfügbar werden.

## 6. Rätsel Bearbeiten

Zweck: Die konkreten Eingaben für jedes Rätsel erfassen. Die UI zeigt nur die
Felder, die zum gewählten Baustein passen.

### 6.1 Fund / `collection.single`

Interner JSON-Contract: `riddle.type=collection`,
`parameters.rewardMode=find_resource`.

Pflichtfelder:

- sichtbarer Name
- Aufgabe für Spielende
- Fundort/Oberfläche
- Fundtyp: Container oder Weltobjekt
- Hinweis oder Ressource

Optional:

- Hinweistext
- Bildasset
- kurzer Erfolgstext

### 6.2 Keypad / `input.numeric`

Interner JSON-Contract: `riddle.type=input`, `parameters.inputMode=numeric`.

Pflichtfelder:

- Keypad-Oberfläche
- erwarteter Zahlencode
- maximale Länge
- Erfolg: Tür öffnen oder Bereich freischalten

Optionale Felder:

- Feedback bei falscher Eingabe
- Ziffernanzahl anzeigen ja/nein
- Hinweis nach Fehlversuchen oder Zeit

## 7. Inhalte & Assets

Zweck: Texte, Bilder und Audio-Dateien an einer Stelle verwalten.

Pflichtbereiche:

| Bereich | Pflicht, wenn... |
|---|---|
| Texte | ein Rätsel Text, Lore oder Ressource nutzt |
| Bilder | eine Ressource oder Story ein Bild nutzt |
| Audio | Audio in Szenario oder Feedback aktiviert ist |
| Hinweise | optional, aber pro Rätsel als leeres Array vorhanden |

Hint-Freischaltung:

- sofort verfügbar
- nach Zeit
- nach Fehlversuchen
- nachdem ein Rätsel gelöst wurde

V0-Eingaben:

- Text direkt im Wizard
- Bilder als Upload
- Audio als Upload

Uploads müssen in V0 direkt runtime-fähige Medien sein.

## 8. Prüfen & Entwurf Finalisieren

Zweck: Lehrende sehen vor dem Abschluss eine klare, nicht-technische
Checkliste.

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
- verwendeter Baustein ist im aktuellen Generator-Slice nicht generierbar

Warnungen:

- sehr lange Texte
- Rätsel ohne Hinweise
- viele Rätsel ohne klare Story-Einbettung
- erwartete Dauer deutlich höher als Zeitlimit

Hauptaktion:

- `Entwurf finalisieren`

Der Button ist deaktiviert, solange blockierende Fehler existieren.

## Bausteinkatalog Aus The Last Hour

Diese Liste beschreibt The-Last-Hour-nahe Bausteine als Ideensammlung. Sie ist
keine vorausgewählte Raumstruktur und kein aktiver V0-Contract.

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

Lehrende bauen den Raum selbst aus aktiv unterstützten Bausteinen zusammen. Die
UI darf spätere Bausteine zeigen, aber nur deaktiviert und mit klarem Grund.
