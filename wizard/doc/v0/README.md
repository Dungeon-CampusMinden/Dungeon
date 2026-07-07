# Wizard V0 Documentation

Status: V0-Konzept und UI-first Authoring-Contract.

## Zweck

Diese Dateien beschreiben den öffentlichen V0-Stand des Dungeon Wizards. V0
ist eine Web-App für nicht-technische Lehrende und erzeugt eine validierte
`deer.json` mit referenzierten Assets. Der Java-Generator liest diese
Projektdateien manuell und erzeugt daraus das spielbare Room-Paket.

## Aktive Dateien

Diese V0-Dokumente liegen unter `./wizard/doc/v0`. Der Root `./wizard` bleibt
frei für Web-App-Code, Entwurfsdaten und spätere Generator-Anbindung.

- `concept.md`: aktuelle Projektdefinition und Scope-Grenze für V0.
- `frontend-handoff-overview-v0.md`: kompakter Einstieg für die
  Frontend-/UI-Person.
- `wizard-ui-flow-v0.md`: Wizard-Schritte, sichtbare Eingaben,
  Validierungsstellen und Abschlussaktion.
- `teacher-workflow-v0.md`: funktionaler UI-Contract für den Lehrenden-
  Workflow.
- `deer-json-spec.md`: menschenlesbare Spezifikation der internen
  `deer.json`, die der Generator konsumiert.
- `deer.schema.json`: maschinenlesbares JSON Schema für `deer.json`.
- `examples/deer.example.json`: valides Beispiel für den Foundation-Slice.
- `parameter-table-v0.md`: Pflichtparameter des V0-Foundation-Slices.
- `the-last-hour-interaction-catalog.md`: Mapping vorhandener The-Last-Hour-
  Interaktionen auf spätere Wizard-Bausteine.
- `generator-input-format.md`: Projektordner- und Generator-Handoff.
- `implementation-handoff-v0.md`: kompakte Übergabe für UI- und
  Generator-Start.

## V0-Kernvertrag

- `deer.json` ist der Contract zwischen Wizard-UI und Java-Generator.
- Die sichtbare Endaktion ist `Entwurf finalisieren`.
- Die UI erzeugt keine spielbaren Runtime-Dateien und kein Generator-Paket.
- Die Generator-Eingabe ist ein Projektordner mit `deer.json` und
  referenzierten Assets.
- Lehrende bearbeiten keine JSON-Datei direkt.
- Der Java-Generator wird in V0 manuell mit dem Projektordner gestartet und
  erzeugt daraus das Room-Paket.
- The Last Hour liefert verfügbare Bausteine und Assets, aber keine
  vorausgewählte Vorlage.
- V0 aktiv sind nur die Bausteine, die der Foundation-Slice wirklich
  spielbar erzeugen kann.
- Der Abschluss ist nur aktiv, wenn Client-Preflight, Schema,
  Asset-Referenzen, Ablauf und verwendete Bausteine gültig sind.
- Nicht generierbare Bausteine oder Optionen erscheinen deaktiviert mit
  sichtbarem Grund.
- Blockierend sind Fehler, die Pflichtdaten, Assets, Progression oder
  Spielbarkeit beschädigen. Warnungen blockieren nicht.
