# Wizard Documentation

Das Zielbild des Wizards führt nicht-technische Autorinnen und Autoren von
einem privaten, unvollständigen Entwurf zu einem ausführbaren DEER-Projekt:

```text
Wizard-Entwurf
-> deer.json + optionale Dateien unter assets/custom/
-> projektspezifische WizardRoom.jar
-> Main-Menü für Host oder Join
-> deterministisch abgeleiteter Foundation-Raum im Speicher
-> gewöhnlicher Dungeon-Multiplayer
```

Der Wizard erzeugt weder Java-Code noch ein Raummodul, Buildskripte oder ein
Room-ZIP. Der Packager bettet das finalisierte Projekt zusammen mit Runner,
Engine, Basisassets und Runtime-Abhängigkeiten in eine ausführbare
`WizardRoom.jar` ein. Dieselbe JAR wird an Host und alle weiteren Spielenden
verteilt. Sie enthält daher auch `deer.json`, Seed, Lösungen und noch nicht
freigegebene Inhalte in auslesbarer Form. Jeder Teilnehmer leitet daraus
denselben vollständigen Foundation-Raum ab; das Netzwerk prüft nur den
Hash der vollständigen kanonisierten `deer.json`.

Eigene Bilder liegen inhaltsadressiert unter `assets/custom/`. Bereits mit dem
Spiel ausgelieferte Bilder werden dagegen ohne Kopie über ihren internen Pfad,
zum Beispiel `items/puzzle-piece.png`, referenziert. Assetpfade besitzen keinen
führenden Slash; ein Projekt ohne eigenes Bild kann nur aus `deer.json`
bestehen.

Mit Java 25 öffnet `java -jar WizardRoom.jar` das bestehende Host-/Join-Menü.
Der Host-Knopf startet dieselbe JAR intern als verwalteten headless
Serverprozess und verbindet anschließend den Host-Client. Ein separates
Serverartefakt oder ein benutzersichtbarer CLI-Serverstarter ist nicht nötig.
Die Befehle `validate`, `host` und `join` bleiben als sekundäre
Entwicklungs- und Authoring-Schnittstelle bestehen.

Der Java-Runner und der Foundation-Runtime-Slice sind umgesetzt. Die lokale
Standalone-Autorenoberfläche ist in den folgenden Dokumenten verbindlich
spezifiziert, aber noch nicht Bestandteil dieser Implementierung.

`scenario.introText`, `scenario.successText` und das bei harten Zeitlimits
verwendete `scenario.failureText` sind geordnete Seitenfolgen. Jeder
Array-Eintrag wird als eigene weiterklickbare Black-Fade-Seite angezeigt.

## Implementierungsstatus

| Bereich | Status |
|---|---|
| DEER-Schema `0.3`, Projektvalidierung und Runner-Reports | umgesetzt |
| Spieler-JAR, Main-Menü, `validate`, `host`, `join` und Foundation-Runtime | umgesetzt |
| Lokale Authoring-UI, privater Draft und nativer Storage-Adapter | Soll-Contract, noch nicht umgesetzt |
| Aufruf des JAR-Packagers aus der Authoring-UI | spätere dünne Integration |
| Inhalte unter `research/` | nicht-normative Begründung, kein zweiter Vertrag |

## Maßgebliche Dokumente

1. [`v0/frontend-handoff-overview-v0.md`](v0/frontend-handoff-overview-v0.md)
   ist der kurze Einstieg für die Frontend-Umsetzung und trennt UI,
   Storage-Adapter und Java-Runner.
2. [`v0/wizard-ui-flow-v0.md`](v0/wizard-ui-flow-v0.md) beschreibt den
   sichtbaren Autorenfluss und den privaten Entwurf.
3. [`v0/deer.schema.json`](v0/deer.schema.json) ist der maschinenlesbare
   Vertrag; [`v0/deer-json-spec.md`](v0/deer-json-spec.md) erklärt seine
   Semantik.
4. [`v0/runner-project-format.md`](v0/runner-project-format.md) definiert
   Projektordner, Finalisierung, Assets, deterministische Identität und das
   maschinenlesbare Validierungsergebnis.
5. [`v0/runner-runtime-contract.md`](v0/runner-runtime-contract.md) definiert
   CLI, Host-/Join-Grenze, Bootstrap und Spielruntime.
6. [`../examples/foundation-v0.3/`](../examples/foundation-v0.3/) ist das
   kleine kanonische und direkt ausführbare Beispielprojekt.
7. [`../examples/the-last-hour-v0.3/`](../examples/the-last-hour-v0.3/) ist
   das größere Demonstrations- und Regressionsexemplar für einen
   umfangreicheren Rätselablauf.

[`v0/the-last-hour-interaction-catalog.md`](v0/the-last-hour-interaction-catalog.md)
inventarisiert vorhandene Interaktionen als nicht-normative Grundlage für
spätere Entscheidungen. [`research/`](research/) enthält die wissenschaftlichen
Quellen; diese begründen Leitplanken, sind aber keine zweite Spezifikation.
