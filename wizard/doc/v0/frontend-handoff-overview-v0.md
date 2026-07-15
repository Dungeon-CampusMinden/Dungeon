# Frontend Handoff V0.2

## Auftrag

Die Frontend-Umsetzung baut eine Web-Oberfläche in einem lokalen
Standalone-Host für nicht-technische Lehrende. Sie verwaltet einen
unvollständigen Entwurf, projiziert ihn bei erfolgreicher Prüfung auf
`deer.json` und schreibt `deer.json` plus referenzierte Assets über einen
nativen Storage-Adapter in einen ausgewählten Projektordner.

Die UI startet keinen Java-Generator und erzeugt kein ZIP.

## Lesereihenfolge

1. [`concept.md`](concept.md)
2. [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md)
3. [`deer.schema.json`](deer.schema.json)
4. [`examples/deer.example.json`](examples/deer.example.json)
5. [`implementation-handoff-v0.md`](implementation-handoff-v0.md)

[`teacher-workflow-v0.md`](teacher-workflow-v0.md) enthält ergänzende
Akzeptanzszenarien. Feldsemantik steht in
[`deer-json-spec.md`](deer-json-spec.md) und
[`parameter-table-v0.md`](parameter-table-v0.md).

## Sichtbarer Flow

```text
Starten oder fortsetzen
-> Eckdaten & Lernziel
-> Geschichte
-> Spielablauf
-> Rätsel, Inhalte & Hilfen
-> Prüfen, Vorschau & finalisieren
```

Die Übersicht bleibt als Navigation und Statusfläche sichtbar. „Orte und
Geräte“ werden innerhalb eines Rätsels benannt; es gibt keinen separaten
technischen Surface-Schritt.

## Daten- und Speichergrenze

- Der **Wizard-Entwurf** darf unvollständig sein und bleibt UI-intern.
- Der Entwurf wird automatisch lokal gespeichert und nach einem Neustart
  wieder angeboten.
- IDs entstehen einmal und bleiben bei Umbenennung stabil.
- `deer.json` wird erst aus einem vollständigen, fehlerfreien Entwurf erzeugt.
- Finalisierung ist wiederholbar und sperrt den Entwurf nicht.
- Änderungen nach einer Finalisierung markieren die letzte Ausgabe als
  veraltet.
- Assets bleiben mit dem Entwurf verknüpft und werden bei Finalisierung in
  `assets/custom/` geschrieben.
- Ein optionales Playtest-Protokoll mit Datum, Testgruppe, Ergebnis und Notizen
  bleibt im lokalen Draft und wird nicht in `deer.json` exportiert.

Für V0.2 ist der Standalone-Host verbindlich. Der Frontend-Code kapselt
Draft-Speicher und Projektordnerzugriff hinter einem Storage-Port, damit später
ein Browser-Adapter ergänzt werden kann. Ein beliebiger Browser ohne
persistente Dateiablage ist kein unterstützter Foundation-Host.

## Foundation-Slice

Aktiv:

- ein oder mehrere Lernziele;
- **Fund** mit mindestens einem notwendigen Hinweis-/Aufgabentext und
  optionalem PNG-/JPEG-Bild;
- **Zahlencode** mit 1 bis 8 Ziffern;
- geordnete Abschnitte mit optional parallelen Pflichträtseln;
- ab Rätselaktivierung nacheinander anforderbare Hilfen;
- gemeinsamer Ausgang als Erfolgsziel;
- Textvorschau, Ablaufzusammenfassung und Entwurfsprüfung.

Nicht im aktiven Picker:

- Computer, E-Mail, USB, Assembly, Control Panel;
- freie Zustandsaktionen;
- Audio und Themes;
- zeit- oder versuchsabhängige Hint-Regeln;
- freie Graphkanten.

Geplante Typen werden in einer kurzen Vorschau „Weitere Rätselarten sind
geplant“ erklärt, nicht als große Menge deaktivierter Hauptaktionen angezeigt.

## UX-Richtung

**Visuelle These:** eine ruhige Werkstatt für Unterrichtsentwürfe – klare
Typografie, helle Arbeitsfläche, ein Akzent für Aktionen und eine
blueprintartige Ablaufspur statt Dashboard-Kartenraster.

**Arbeitsfläche:** schmale Navigation mit Schrittstatus, zentrale
Abschnitts-/Rätselliste und ein kontextbezogener Editor. Prüfhinweise erscheinen
am betroffenen Element und zusätzlich in einer kompakten Zusammenfassung.

**Interaktion:**

- Abschnitt oder Rätsel einfügen und verschieben mit kurzer Layout-Transition;
- ein Prüfhinweis fokussiert und hebt das betroffene Feld/Rätsel hervor;
- Änderungen an Ablauf oder Inhalt aktualisieren Vorschau und Status sichtbar,
  aber ohne dauernde Modals.

Drag-and-drop ist optional. Jede Aktion braucht eine Tastatur- und
Button-Alternative. Animation respektiert `prefers-reduced-motion`.

## Sprache der UI

| Intern | Sichtbar |
|---|---|
| Surface | Ort oder Gerät |
| Asset | Bild oder Datei |
| Resource | Inhalt oder Material |
| Hint | Hilfe |
| Preflight | Entwurfsprüfung |
| Riddle graph | Spielablauf |
| End state | gemeinsames Erfolgsziel |
| Generator capability | in dieser Version verfügbar |

„Hinweis“ bezeichnet fachlichen Rätselinhalt; „Hilfe“ bezeichnet optionale
Unterstützung. Technische IDs, JSON Pointer und Issue-Codes bleiben verborgen.

## Prüfung und Fehlerbehebung

`Entwurf prüfen` ist immer verfügbar. `Entwurf finalisieren` ist nur ohne
blockierende Fehler verfügbar; ein Hilfetext erklärt die Übergabe an die
technische Betreuung.

Jedes Problem zeigt:

- was fehlt oder widersprüchlich ist,
- warum es relevant ist,
- eine konkrete Korrekturaktion,
- einen Sprung zum betroffenen Feld oder Rätsel.

Eingaben bleiben bei Fehlern erhalten. Löschen und Verschieben bieten Undo.
Warnungen blockieren nicht. Die UI spricht bei Graphprüfungen von
„strukturell vollständig“, nicht von „garantiert lösbar“.

## Barrierefreiheit

Die Foundation-Authoring-UI zielt auf WCAG 2.2 AA:

- vollständige Tastaturbedienung und sichtbarer Fokus;
- keine ausschließlich farbliche Statuskommunikation;
- programmatisch verknüpfte Labels, Beschreibungen und Fehlermeldungen;
- Fokus auf die Fehlerzusammenfassung nach einer Prüfung;
- Zoom, Reflow und Reduced Motion;
- zugängliche Datei-Auswahl;
- nicht-spoilernde Alternativbeschreibung für informative Bilder.

Diese Zusage gilt nicht automatisch für die LibGDX-Spielruntime. Informative
Bilder dürfen im Foundation-Raum nicht der einzige Träger notwendiger
Information sein; ihre Beschreibung bleibt in der Generatorausgabe erhalten.

## Frontend-Definition-of-Done

Der erste UI-Slice ist fertig, wenn der Beispielraum:

1. als unvollständiger Entwurf automatisch gespeichert und wieder geöffnet
   werden kann,
2. ohne technische Begriffe erstellt und per Tastatur bedient werden kann,
3. verständliche Fehler mit direkten Korrekturwegen zeigt,
4. als nicht-spielbare Zusammenfassung geprüft werden kann,
5. ein Bild inhaltsadressiert schreibt und danach eine schema- und fachlich
   gültige `deer.json` sicher ersetzt,
6. nach Änderungen erneut finalisiert werden kann.
