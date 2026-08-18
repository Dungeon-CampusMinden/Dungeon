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
Serverprozess und verbindet anschließend den Host-Client. Die
Authoring-Integration verwendet die Java-Validierungsbibliothek direkt.

Der Java-Runner, der Foundation-Runtime-Slice und die M2-Authoring-Architektur
sind umgesetzt. Die produktive Autorenoberfläche läuft als React-UI unter
einem loopback-only Java-Host. Er speichert private Entwürfe und Uploads unter
`%LOCALAPPDATA%\Dungeon Wizard`, validiert und finalisiert Projekte und erzeugt
anschließend `<project>/WizardRoom.jar`. LocalStorage und IndexedDB dienen nur
als Fallback beim direkten Vite-Entwicklungsstart.

M1 und M2 sind Implementierungsmeilensteine, keine Formatversionen. Das
öffentliche DEER-Format bleibt `0.4`, das private Draftformat `1`; für den
privaten Prototyp ist keine Browsermigration vorgesehen.

## Start und Packaging

`wizard/start_wizard_dev.cmd` baut und startet den aktuellen
Authoring-Host ausschließlich für Entwicklung. Die spätere
Zielgruppen-Distribution als `.exe` mit gebündelter Runtime ist noch nicht
umgesetzt; M2 erzeugt weder eine `.exe` noch ein Room-ZIP. Für den aktuellen
Entwicklungs- und Spieler-JAR-Fluss ist Java 25 erforderlich.

Die UI paketiert ein erfolgreich finalisiertes Projekt mit demselben
Java-Packager, der auch dem Gradle-Entwickler-/CI-Pfad zugrunde liegt. Als
Eingabe dient die generische `WizardRoomTemplate.jar`; am Ziel entsteht
`<project>/WizardRoom.jar`. Im nativen Host sind dafür weder Gradle
noch Node erforderlich. Der weiterhin unterstützte Entwickler-/CI-Aufruf ist:

```text
gradlew.bat :wizard:buildWizardRoomJar -PwizardProject=<projektordner>
```

Ein Packaging-Fehler verändert das bereits finalisierte Projekt nicht und kann
in der UI erneut versucht werden.

`scenario.introText`, `scenario.successText` und das bei harten Zeitlimits
verwendete `scenario.failureText` sind geordnete Seitenfolgen. Jeder
Array-Eintrag wird als eigene weiterklickbare Black-Fade-Seite angezeigt.

## Implementierungsstatus

| Bereich | Status |
|---|---|
| DEER-Schema `0.4`, Projektvalidierung und Validierungsreports | umgesetzt |
| Spieler-JAR, Host-/Join-Menü und Foundation-Runtime | umgesetzt |
| Gradle-Entwickler-/CI-Packaging | umgesetzt |
| Lokale Authoring-UI, privater Draft und nativer Storage-Adapter | in M2 umgesetzt |
| Produktionsvalidierung, atomare Finalisierung und UI-Packaging | in M2 umgesetzt |
| Zielgruppen-`.exe` mit gebündelter Java-Runtime | späterer Distributionsmeilenstein |
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
   Packaging-Prüfung, Host-/Join-Grenze, Bootstrap und Spielruntime.
6. [`../examples/foundation-v0.4/`](../examples/foundation-v0.4/) ist das
   kleine kanonische und direkt ausführbare Beispielprojekt.
7. [`../examples/the-last-hour-v0.4/`](../examples/the-last-hour-v0.4/) ist
   das größere Demonstrations- und Regressionsexemplar für einen
   staggered mandatory AND-DAG-Rätselablauf.

[`v0/the-last-hour-interaction-catalog.md`](v0/the-last-hour-interaction-catalog.md)
inventarisiert vorhandene Interaktionen als nicht-normative Grundlage für
spätere Entscheidungen. [`research/`](research/) enthält die wissenschaftlichen
Quellen; diese begründen Leitplanken, sind aber keine zweite Spezifikation.
