# Frontend handoff V0.4

Status: schlanker V0-Authoring-Vertrag für DEER `0.4`

## Auftrag

Die lokale Web-Oberfläche führt nicht-technische Lehrende von einem
unvollständigen privaten Entwurf zu diesem Projektordner:

```text
<project>/
  deer.json
  assets/custom/...  # nur bei eigenen Bildern
  WizardRoom.jar     # nach erfolgreichem Packaging
```

Die UI zeigt weder JSON noch Java-, Gradle-, Petri-Netz- oder technische
ID-Begriffe. Sie startet keine Spielruntime und erzeugt weder Java-Code noch
ein Raummodul, Buildskripte oder ein ZIP.

## Verantwortungsgrenze

| Schicht | Verantwortung |
|---|---|
| Browser-UI | sichtbarer Flow, `WizardDraft` v1, Uploadbytes, Autosave, lokale Feldhinweise, Vorschau, Seed und Ready-Zustand der aktuellen Sitzung |
| Java-Host | UI ausliefern, nativen Ordnerdialog öffnen, Kandidaten produktiv validieren, Projektdateien einzeln atomar ersetzen und direkt als `WizardRoom.jar` paketieren |
| Runner und Runtime | normative Projektvalidierung, deterministische Ableitung und Host-/Join-Runtime |

Der Java-Host bindet fest an `127.0.0.1:27777`. Er ist bezüglich Drafts und
Uploads zustandslos. Ist der Port belegt, bricht der Hoststart mit einer klaren
Meldung ab. Eine dynamische Portwahl gehört nicht zu V0.

## Privater Browserzustand

Die UI speichert Draft v1 und Uploadbytes ausschließlich in einer neuen
IndexedDB. Der Draft darf fachlich unvollständig sein und ist kein zweites
DEER-Format. Er behält alle sichtbaren Eingaben, stabilen Authoring-IDs,
Uploadbytes, Assetmetadaten, den optional gewählten Projektordner und den Seed,
sobald dieser erzeugt wurde.

V0 ist ein Clean Cut. Alte Browser- und AppData-Entwürfe werden weder erkannt
noch migriert. Mehrere Tabs werden nicht koordiniert und sind nicht
unterstützt. Es gibt daher keine Revisionen, Compare-and-set-Logik, Web Locks
oder Konfliktauflösung zwischen Tabs.

Der Host hat keine Draft-/Upload-API. Er speichert auch keine
Finalisierungsidentität, Recovery-Belege, Ownership-Marker, JAR-Pfade oder
Ready-Zustände.

## Seed

Ein neuer unvollständiger Draft besitzt keinen echten Projektseed. Beim ersten
vollständigen `Entwurf prüfen` oder `Spiel erstellen` erzeugt die UI genau
einmal einen zufälligen ganzzahligen Wert im Bereich
`0..9007199254740991`. Sie speichert ihn im Draft, bevor sie den Kandidaten an
den Host sendet. Jeder spätere Kandidat desselben Drafts verwendet diesen Wert
unverändert. Java erzeugt und ersetzt keinen Seed.

## Minimale Host-API

Die same-origin API umfasst nur diese Operationen:

| Operation | Request | Ergebnis |
|---|---|---|
| Status | keiner | Host erreichbar und kompatibel |
| Ordner wählen | keiner | nativer, vom Nutzer bestätigter `projectDirectory` oder Abbruch |
| Validieren | `{ project, customAssets: [{ path, bytesBase64 }] }` | `ProjectValidationReport` |
| Finalisieren | Validierungsrequest plus `projectDirectory` | erfolgreich finalisierter Projektordner |
| Paketieren | `{ projectDirectory, projectId }` | erzeugte `<project>/WizardRoom.jar` |

`project` ist der vollständige DEER-Kandidat. Die Teacher-UI zeigt weder
`projectId` noch andere interne IDs. Ein Abbruch des Ordnerdialogs verändert
den Draft nicht.

Lokale Feldhinweise sind erlaubt, aber nicht normativ. Vor `Spiel erstellen`
verwendet die UI immer die Produktionsvalidierung für exakt den Kandidaten und
die Uploadbytes, die finalisiert werden sollen. Technische Codes, JSON-Pointer
und IDs werden auf verständliche Felder oder Rätsel abgebildet.

## Finalisierung und Packaging

Die Finalisierung akzeptiert:

- einen leeren normalen Ordner;
- ein produktiv gültiges vorhandenes Wizard-Projekt mit derselben
  `metadata.id` wie der Kandidat.

Ein fremder oder anderweitig nicht leerer Ordner sowie ein ungültiges
vorhandenes Projekt werden abgelehnt. Symlinks und vergleichbare Umleitungen
sind kein gültiges Ziel. Der Host ersetzt jede Custom-Datei einzeln atomar und
`deer.json` zuletzt. Diese Reihenfolge ist keine transaktionale Garantie für den
gesamten Projektordner. Der Host löscht keine fremden oder alten, nicht mehr
referenzierten Dateien. V0 verspricht keine Wiederherstellung nach einem
abgebrochenen Schreibvorgang.

Nach erfolgreicher Finalisierung ruft die UI Package mit Projektordner und
Projekt-ID auf. Nur der erfolgreiche Package-Aufruf dieser UI-Sitzung setzt den
sichtbaren Zustand `Das Spiel ist bereit`. Ein Reload stellt ihn nicht wieder
her. Bei einem Packaging-Fehler bietet die UI einen erneuten Versuch an und
wiederholt den gesamten Finalize-und-Package-Ablauf. Es gibt keinen separat
wiederholten Package-Schritt auf Grundlage persistierter Hostmetadaten.

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
Runtime sind ein späterer Meilenstein.

## Lesereihenfolge

1. [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md) für den sichtbaren Flow.
2. [`deer.schema.json`](deer.schema.json) und
   [`deer-json-spec.md`](deer-json-spec.md) für das Ausgabeformat.
3. [`runner-project-format.md`](runner-project-format.md) für Projektordner,
   Assets und Validierung.
4. [`runner-runtime-contract.md`](runner-runtime-contract.md) für Packaging
   und Spielruntime.
