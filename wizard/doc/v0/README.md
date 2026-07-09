# Wizard V0.2 Documentation

## Zweck

V0.2 beschreibt einen kleinen, echten End-to-End-Slice:

```text
Wizard-Entwurf
-> deer.json + assets/
-> Java-Generator
-> generiertes Escape-Room-Modul als ZIP
-> Integration und Build
```

Die UI erzeugt kein ZIP und keine Runtime-Dateien. Der Foundation-Slice wird
noch von einer technischen Betreuung generiert und gebaut; die Lehrenden-UI
bleibt davon getrennt.

## Maßgebliche Dateien

| Thema | Datei |
|---|---|
| Produktziel und Scope | [`concept.md`](concept.md) |
| Sichtbarer UI-Flow | [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md) |
| Persona und Akzeptanzszenarien | [`teacher-workflow-v0.md`](teacher-workflow-v0.md) |
| Frontend-Einstieg | [`frontend-handoff-overview-v0.md`](frontend-handoff-overview-v0.md) |
| Gemeinsame Implementierungsübergabe | [`implementation-handoff-v0.md`](implementation-handoff-v0.md) |
| DEER-Semantik | [`deer-json-spec.md`](deer-json-spec.md) |
| Maschinenlesbarer Contract | [`deer.schema.json`](deer.schema.json) |
| Foundation-Beispiel | [`examples/deer.example.json`](examples/deer.example.json) |
| Typ-Parameter | [`parameter-table-v0.md`](parameter-table-v0.md) |
| Generator-Eingabe | [`generator-input-format.md`](generator-input-format.md) |
| Generator-Ausgabe | [`generator-output-format.md`](generator-output-format.md) |
| Spätere Bausteine | [`the-last-hour-interaction-catalog.md`](the-last-hour-interaction-catalog.md) |

## V0.2-Kernentscheidungen

- Unvollständige Entwürfe sind UI-private Daten; nur der Abschluss erzeugt eine
  `deer.json`.
- Mindestens ein Lernziel ist Pflicht; jedes Rätsel wird einem Lernziel
  zugeordnet.
- Der sichtbare Ablauf besteht aus geordneten Abschnitten mit optionaler
  Parallelität. Der Graph wird daraus abgeleitet.
- Alle Rätsel sind Pflichträtsel und führen zu einem gemeinsamen Erfolgsziel.
- V0.2 aktiviert nur **Fund** und **Zahlencode**.
- Technische Orte, IDs und Graphkanten werden von der UI abgeleitet.
- Statische Prüfung belegt Struktur und Generatorfähigkeit, nicht menschliche
  Lösbarkeit oder Lernerfolg.
- Der Java-Generator validiert erneut und erzeugt deterministisch ein
  generiertes Modul-ZIP.
- Runtime-Progression, Timer und Ende sind serverautoritativ und müssen im
  Multiplayer synchronisiert werden.
