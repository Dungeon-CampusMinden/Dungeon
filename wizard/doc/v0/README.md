# Wizard V0 Documentation

Status: V0-Konzept und UI-first Authoring-Contract.

## Zweck

Diese Dateien beschreiben den öffentlichen V0-Stand des Dungeon Wizards. V0 ist
eine Web-App für nicht-technische Lehrende. Sie erzeugt eine validierte
`deer.json` und packt sie mit referenzierten Custom Assets als
DEER-Authoring-Bundle `deer.zip`.

## Aktive Dateien

Diese V0-Dokumente liegen unter `./wizard/doc/v0`. Der Root `./wizard` bleibt
frei für Web-App-Code, Bundle-Output und spätere Generator-Anbindung.

- `concept.md`: aktuelle Projektdefinition und Scope-Grenze für V0.
- `frontend-handoff-overview-v0.md`: kompakter Einstieg für die
  Frontend-/UI-Person.
- `wizard-ui-flow-v0.md`: Wizard-Schritte, sichtbare Eingaben,
  Validierungsstellen und Export.
- `teacher-workflow-v0.md`: funktionaler UI-Contract für den Lehrenden-
  Workflow.
- `deer-json-spec.md`: menschenlesbare Spezifikation der internen
  `deer.json`, die der Generator konsumiert.
- `deer.schema.json`: maschinenlesbares JSON Schema für `deer.json`.
- `examples/deer.example.json`: valides Beispiel mit den aktuell verfügbaren
  V0-Bausteinen.
- `parameter-table-v0.md`: Pflichtparameter der V0-Bausteine.
- `the-last-hour-interaction-catalog.md`: Mapping vorhandener The-Last-Hour-
  Interaktionen auf wiederverwendbare Wizard-Bausteine.
- `room-package-format.md`: Format des DEER-Authoring-Bundles `deer.zip`
  und Generator-Handoff.
- `implementation-handoff-v0.md`: kompakte Übergabe für UI- und
  Generator-Start.

## V0-Kernvertrag

- `deer.json` ist der Contract zwischen Wizard-UI und Java-Generator.
- Die sichtbare Endaktion ist `deer.zip herunterladen`.
- `deer.zip` ist ein DEER-Authoring-Bundle: validierte `deer.json` plus alle
  referenzierten Custom Assets.
- `deer.zip` ist kein spielbares Room-Paket und kein Generator-Output.
- Lehrende bearbeiten keine JSON-Datei direkt.
- Der Java-Generator wird in V0 manuell mit dem Paket oder Projektordner
  gestartet und erzeugt daraus das Room-Paket.
- The Last Hour liefert verfügbare Bausteine und Assets, aber keine
  vorausgewählte Vorlage.
- Bundle-Erstellung ist nur aktiv, wenn Client-Preflight, Schema,
  Asset-Referenzen, Ablauf und verwendete Bausteine gültig sind.
- Nicht generierbare Bausteine oder Optionen erscheinen deaktiviert mit
  sichtbarem Grund.
- Blockierend sind Fehler, die Pflichtdaten, Assets, Progression oder
  Spielbarkeit beschädigen. Warnungen blockieren nicht.
