# Generator Input Format

Status: V0-Contract
Scope: manuelle Übergabe vom Web-Wizard an den Java-Generator

## V0-Entscheidungen

- Der Web-Wizard finalisiert eine validierte `deer.json`.
- Die UI erzeugt kein spielbares Room-Paket und kein Generator-ZIP.
- Der Java-Generator liest in V0 manuell einen Projektordner.
- Runtime-Dateien werden erst vom Generator abgeleitet.
- Custom Assets ergänzen Inhalte, ersetzen aber nicht das Standard-Theme.
- V0 unterstützt nur Medien, die die LibGDX-Runtime direkt verarbeiten kann.

## Projektordner

```text
wizard-project/
  deer.json
  assets/
    custom/
```

Regeln:

- `deer.json` liegt im Wurzelverzeichnis des Projektordners.
- Asset-Pfade in `deer.json` sind relativ zum Projektordner.
- Referenzierte Assets müssen im Projektordner existieren.
- Pfade dürfen nicht aus dem Projektordner herauszeigen.
- Nicht referenzierte Dateien sind für V0 nicht erforderlich.

## File Roles

### `deer.json`

Die einzige editierbare Authoring-Quelle. Der Wizard schreibt diese Datei; der
Generator liest sie und leitet daraus Runtime-Dateien ab.

Sie enthält:

- Raum-Metadaten,
- Session-Parameter,
- Standard-Theme und Storytexte,
- Oberflächen,
- Rätselgraph,
- Rätselparameter,
- Asset-Referenzen.

Die detaillierte Spezifikation steht in
[`deer-json-spec.md`](deer-json-spec.md). Das maschinenlesbare Schema steht in
[`deer.schema.json`](deer.schema.json). Ein gültiges Beispiel steht in
[`examples/deer.example.json`](examples/deer.example.json).

### `assets/custom/`

Custom Assets sind von Lehrenden hochgeladene oder vom Wizard ausgewählte
Inhaltsmedien. In V0 sind das nur Runtime-fähige Dateien wie Bilder, Audio und
einfache Textdateien.

Textinhalte sollen bevorzugt im Wizard eingegeben und in `deer.json`
gespeichert werden. Wenn ein Inhalt als Datei gebraucht wird, muss er als
unterstütztes Asset im Projektordner liegen.

## Validierung Und Handoff

Der Wizard darf den Entwurf nur finalisieren, wenn Schema, Graph,
Pflichtparameter und Asset-Referenzen valide sind. Blockierende Fehler werden
vor der Finalisierung im Client angezeigt.

Der manuell gestartete Java-Generator validiert dieselben harten Regeln erneut,
bevor er Runtime-Dateien ableitet. Diese zweite Prüfung ersetzt nicht die
Client-Validierung; sie schützt vor manuell veränderten Projektdateien und
Implementierungsfehlern.

Für V0 endet die Wizard-Verantwortung mit validierter `deer.json`, gültigen
Asset-Referenzen und einem klaren manuellen Generator-Handoff. Alles, was
danach erzeugt wird, gehört zum Generator- und Runtime-Scope.
