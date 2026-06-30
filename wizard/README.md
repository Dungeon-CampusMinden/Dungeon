# Wizard Workspace

Status: V0-Konzept und UI-first Authoring-Contract

## Aktive Dateien

- `concept.md`: kurze aktuelle Projektdefinition fuer V0.
- `wizard-ui-flow-v0.md`: erwarteter Wizard-Ablauf und sichtbare Eingaben.
- `deer-json-spec.md`: menschenlesbare Spezifikation der exportierten
  `deer.json`.
- `deer.schema.json`: maschinenlesbares JSON Schema fuer `deer.json`.
- `examples/deer.example.json`: valides Beispiel mit den aktuell verfuegbaren
  V0-Bausteinen.
- `parameter-table-v0.md`: Pflichtparameter der V0-Bausteine.
- `the-last-hour-interaction-catalog.md`: Mapping vorhandener The-Last-Hour-
  Interaktionen auf wiederverwendbare Wizard-Bausteine.
- `room-package-format.md`: nachgelagerte Packaging-/Generator-Notiz. Nicht die
  primaere UI-Aktion fuer den ersten Wizard-Prototyp.

## V0-Entscheidungen

- Die Wizard-UI exportiert zuerst nur eine `deer.json`.
- `deer.zip`, Runtime-Dateien und spielbare Preview sind Generator-Themen fuer
  spaetere Schritte.
- V0 nutzt genau ein Standard-Theme.
- V0 fragt keine Lernziele, Evaluation, Debriefing, Telemetrie oder
  Pre-/Post-Tests ab.
- The Last Hour ist nur Quelle fuer vorhandene Spielbausteine und Assets, keine
  vorausgewaehlte Vorlage.
- Oberflaechen entstehen aus den gewaehlten Bausteinen. Lehrende sollen keine
  technische Slot-Struktur vorab planen muessen.
- Alle aktuell aus The Last Hour ableitbaren Bausteine duerfen im UI-Prototyp
  angeboten werden.
- Blockierend sind nur Fehler, die zu Softlocks, nicht erreichbaren Raetseln,
  unspielbaren Progressionen oder unerlaubten Skips fuehren.
