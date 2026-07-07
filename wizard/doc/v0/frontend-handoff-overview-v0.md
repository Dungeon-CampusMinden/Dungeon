# Frontend Handoff V0

## Zweck

Dieses Dokument ist der kurze Einstieg für die UI-Umsetzung. Es ersetzt nicht
Schema, Parameter-Tabelle oder Workflow-Dokumente.

Der Wizard ist eine Authoring-Web-App für Lehrende. Lehrende erfassen einen
Escape-Room-Entwurf über fachliche Eingaben; die UI erzeugt daraus intern eine
valide `deer.json` und verwaltet referenzierte Assets im Projektordner.

```text
Wizard-Web-App
-> fachliche Eingaben
-> interne deer.json
-> Projektordner mit assets/
-> manueller Java-Generator
-> Room-Paket
```

Sichtbare V0-Abschlussaktion:

```text
Entwurf finalisieren
```

## Relevante Dateien

Für den ersten UI-Slice reicht diese Lesereihenfolge:

1. `implementation-handoff-v0.md`
2. `wizard-ui-flow-v0.md`
3. `teacher-workflow-v0.md`
4. `deer.schema.json`
5. `examples/deer.example.json`
6. `parameter-table-v0.md`

Kontextdateien:

- `concept.md`
- `deer-json-spec.md`
- `generator-input-format.md`
- `the-last-hour-interaction-catalog.md`

## UI-Verantwortung

Die UI ist kein JSON-Editor. Sie verantwortet:

- sichtbare Wizard-Schritte und Schrittstatus,
- Erfassung fachlicher Inhalte,
- Ableitung stabiler IDs, Oberflächen und kontrollierter `successEffect`-
  Werte,
- Asset-Upload und relative Asset-Referenzen,
- Client-Validierung vor der Finalisierung,
- deaktivierte Zustände für nicht generierbare Bausteine,
- Ausgabe einer validierten `deer.json`.

Lehrende bearbeiten keine Tokens, Petri-Netze, Slot-IDs oder JSON-Dateien
direkt. Die UI erzeugt kein spielbares Room-Paket und kein Generator-ZIP.

## Erster UI-Slice

Der erste Slice soll den End-to-End-Pfad klein, aber echt abbilden:

```text
Rahmen
-> Szenario
-> Fund
-> Keypad
-> Tür öffnen
-> deer.json finalisieren
```

Benötigte Bausteine:

- `collection.single` als UI-Label für `riddle.type=collection` mit
  `parameters.rewardMode=find_resource`
- `input.numeric` als UI-Label für `riddle.type=input` mit
  `parameters.inputMode=numeric`
- kontrollierter `successEffect`, z. B. Tür öffnen
- einfache Textressourcen
- optionale Bildassets
- optionale Hinweise ohne komplexe Freischaltbedingungen

Komplexere Bausteine wie Computer, E-Mail, USB, Control Panel und Assembly
werden erst aktiviert, wenn der Generator sie im jeweiligen Slice unterstützt.

## Validierung

Der Button `Entwurf finalisieren` ist nur aktiv, wenn keine blockierenden
Fehler existieren.

Blockierend:

- Pflichtfeld fehlt,
- required Asset fehlt,
- Baustein ist im aktuellen Generator-Slice nicht verfügbar,
- Rätsel ist nicht erreichbar,
- Progression kann nicht abgeschlossen werden,
- Progressionsrätsel kann übersprungen werden,
- Softlock oder zyklische Abhängigkeit,
- Baustein passt nicht zur abgeleiteten Oberfläche.

Warnungen blockieren nicht, bleiben aber sichtbar:

- sehr lange Texte,
- schwierige Rätsel ohne Hinweise,
- ungenutzte Assets,
- schwache Story-Einbettung,
- geschätzte Dauer passt schlecht zum Zeitlimit.

## Gestaltungsspielraum

Das visuelle Design ist frei. Liste, Timeline, Board oder Canvas sind möglich,
solange daraus ein eindeutiger Ablauf und eine valide `deer.json` entstehen.

Die UI soll deaktivierte Optionen begründen, z. B. "im aktuellen Generator noch
nicht verfügbar".
