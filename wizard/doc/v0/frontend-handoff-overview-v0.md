# Frontend handoff V0.4

Status: schlanker V0-Authoring-Vertrag für DEER `0.4`

## Auftrag

Die lokale Web-Oberfläche führt nicht-technische Lehrende von einem
unvollständigen privaten Entwurf zu genau einem verteilbaren Artefakt:

```text
WizardRoom.jar
```

Die UI zeigt weder JSON noch Java-, Gradle-, Petri-Netz- oder technische
ID-Begriffe. Sie startet keine Spielruntime und erzeugt weder Java-Code noch
ein Raummodul, Buildskripte oder ein ZIP.

## Verantwortungsgrenze

| Schicht | Verantwortung |
|---|---|
| Browser-UI | sichtbarer Flow, `WizardDraft` v1, Uploadbytes, Autosave, lokale Feldhinweise, Vorschau, Seed und Ready-Zustand der aktuellen Sitzung |
| Java-Host | UI ausliefern, Kandidaten produktiv validieren, temporär als `WizardRoom.jar` paketieren und die JAR binär ausliefern |
| Runner und Runtime | normative Projektvalidierung, deterministische Ableitung und Host-/Join-Runtime |

Der Java-Host bindet fest an `127.0.0.1:27777`. Er ist bezüglich Drafts und
Uploads zustandslos. Ist der Port belegt, bricht der Hoststart mit einer klaren
Meldung ab. Eine dynamische Portwahl gehört nicht zu V0.

## Privater Browserzustand

Die UI speichert Draft v1 und Uploadbytes ausschließlich in einer neuen
IndexedDB. Der Draft darf fachlich unvollständig sein und ist kein zweites
DEER-Format. Er behält alle sichtbaren Eingaben, stabilen Authoring-IDs,
Uploadbytes, Assetmetadaten und den Seed, sobald dieser erzeugt wurde.

V0 ist ein Clean Cut. Alte Browser- und AppData-Entwürfe werden weder erkannt
noch migriert. Beim Start bittet die UI den Browser um dauerhafte Speicherung
der IndexedDB. Genau ein Tab gleichzeitig darf bearbeiten: Die UI hält eine
Web-Locks-Sitzung, solange ein Entwurf geöffnet ist; ein weiterer Tab erhält
eine klare Warnung und kann keinen Entwurf öffnen oder anlegen. Es gibt keine
Revisionen, Compare-and-set-Logik oder Konfliktauflösung zwischen Tabs.

Der Host hat keine Draft-/Upload-API und speichert weder Authoringdaten noch
JAR oder Ready-Zustand.

## Seed

Ein neuer unvollständiger Draft besitzt keinen echten Projektseed. Sobald Draft
und Assets lokal vollständig und lesbar sind, erzeugt die UI unmittelbar vor
der ersten möglichen Hintergrund-Produktionsprüfung genau einmal einen
zufälligen ganzzahligen 53-Bit-Wert im Bereich `0..9007199254740991`. Sie
speichert ihn im Draft, bevor sie den Kandidaten an den Host sendet. Jeder
spätere Kandidat desselben Drafts verwendet diesen Wert unverändert. Java
erzeugt und ersetzt keinen Seed.

## Minimale Host-API

Die same-origin API umfasst nur diese Operationen:

| Operation | Request | Ergebnis |
|---|---|---|
| Status | keiner | Host erreichbar und kompatibel |
| Validieren | `{ project, customAssets: [{ path, bytesBase64 }] }` | `ProjectValidationReport` |
| Paketieren | derselbe Request wie Validieren | bei Fehlern `ProjectValidationReport`, bei Erfolg binäre `WizardRoom.jar` |

`project` ist der vollständige DEER-Kandidat. Die Teacher-UI zeigt weder
`projectId` noch andere interne IDs.

Lokale Feldhinweise prüfen und markieren den aktuellen Stand. Fehler und
Warnungen blockieren weder die Vorwärts- noch die Rückwärtsnavigation. Die
Meldungen fordern die Lehrkraft nicht auf, auf der aktuellen Seite zu bleiben.
Lokale Fehler verhindern jedoch die Hintergrund-Produktionsprüfung und das
Packaging.

Im nativen Host startet die Produktionsprüfung nach ungefähr zwei Sekunden ohne
inhaltliche Änderung, sobald der gesamte Draft und seine Assets lokal vollständig
und lesbar sind. Sie ist nicht an das Betreten von `Spiel erstellen` gebunden und
sendet den exakt gespeicherten Kandidaten mit seinen Uploadbytes. Die reine
Browserentwicklung führt keine Java-Produktionsprüfung aus. `Spiel erstellen und
herunterladen` liest den aktuellen gespeicherten Stand erneut. Der Host validiert
ihn beim Packaging nochmals; dafür ist ein aktuelles gültiges Ergebnis nötig.
Warnungen blockieren nicht. Technische Codes, JSON-Pointer und IDs werden auf
verständliche Felder oder Rätsel abgebildet.

Lokale und produktive Probleme erscheinen in einer gemeinsamen Fehlerübersicht.
Ändert sich der Draft oder ein Upload, verwirft die UI den alten
Produktionsreport. Parallele oder veraltete Antworten dürfen den aktuellen
Zustand nicht ersetzen.

## Packaging und Download

Der Host materialisiert Kandidat und referenzierte Custom-Assets ausschließlich
in einem temporären Projekt. Er validiert dieses geschlossene Projekt über die
Produktionspipeline und erzeugt nur bei einem gültigen Report eine
`WizardRoom.jar`. Temporäre Projektdateien und die serverseitige JAR werden
nach der Antwort entfernt.

Bei Erfolg liefert der Host die JAR als `application/java-archive`; der Browser
lädt sie als `<bereinigter Spieltitel>-WizardRoom.jar` herunter. Bei einem
ungültigen Kandidaten bleibt die Antwort ein lokalisierbarer
`ProjectValidationReport`. Nur ein erfolgreicher Download dieser UI-Sitzung
setzt den sichtbaren Zustand `Das Spiel ist bereit`.
Ein Reload stellt ihn nicht wieder her. Ein erneuter Versuch paketiert erneut
aus dem aktuellen gespeicherten Draft; es gibt keinen serverseitigen
Zwischenstand.

Die fertige JAR enthält das vollständige Projekt. Host und alle weiteren
Spielenden erhalten exakt dieselbe Datei. Auslesbare Seed-, Lösungs- und
Inhaltsdaten sind eine bewusste V0-Grenze.

## Unveränderter Fachvertrag

DEER bleibt bei `formatVersion=0.4`. Der Spielablauf bleibt ein frei
bearbeiteter mandatory AND-DAG mit genau einem geschützten Start- und
Endknoten und einem Knoten je Rätsel. Mehrere eingehende Kanten bedeuten immer
AND. Die UI bietet keine OR-, Bedingungs- oder Optionalitätsregeln an.

Der Runner-/Multiplayer-Vertrag, `hostInputSha256` und `PROTOCOL_VERSION`
bleiben unverändert. Eine Zielgruppen-EXE und ein Installer mit gebündelter
Runtime sind nicht Teil dieses Frontend-Handoffs.

## Lesereihenfolge

1. [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md) für den sichtbaren Flow.
2. [`deer.schema.json`](deer.schema.json) und
   [`deer-json-spec.md`](deer-json-spec.md) für das Ausgabeformat.
3. [`runner-project-format.md`](runner-project-format.md) für eingebettetes
   Projekt, Assets und Validierung.
4. [`runner-runtime-contract.md`](runner-runtime-contract.md) für Packaging
   und Spielruntime.
