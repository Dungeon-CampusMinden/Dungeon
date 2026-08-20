# Wizard documentation

Der Wizard führt nicht-technische Lehrende von einem privaten Entwurf zu einem
ausführbaren DEER-Projekt:

```text
privater Wizard-Entwurf
-> deer.json + optionale Dateien unter assets/custom/
-> projektspezifische WizardRoom.jar
-> Host-/Join-Menü
-> deterministisch abgeleiteter Foundation-Raum im Speicher
-> Dungeon-Multiplayer
```

Der Wizard erzeugt weder Java-Code noch ein Raummodul, Buildskripte oder ein
Room-ZIP. Der Packager bettet das finalisierte Projekt mit Runner, Engine,
Basisassets und Runtime-Abhängigkeiten in eine `WizardRoom.jar` ein. Host und
alle weiteren Spielenden erhalten dieselbe JAR. Seed, Lösungen und noch nicht
freigegebene Inhalte sind deshalb für jeden Empfänger lokal lesbar.

## Authoring in V0

Die React-UI läuft im Browser. Sie speichert den privaten `WizardDraft` v1 und
alle Uploadbytes ausschließlich in einer neuen IndexedDB. Alte Browser- oder
AppData-Entwürfe werden nicht migriert. Mehrere gleichzeitig geöffnete Tabs
sind in V0 nicht unterstützt.

Der Java-Host bindet fest an `127.0.0.1:27777`. Er ist zustandslos bezüglich
Entwürfen und Uploads und übernimmt nur:

- Auslieferung der UI;
- nativen Ordnerdialog;
- Produktionsvalidierung;
- Finalisierung mit dateiweise atomarem Ersetzen;
- direktes Packaging als `<project>/WizardRoom.jar`.

Der Host bietet keine Draft- oder Upload-Persistenz. Es gibt keine Revisionen,
Recovery-Belege, Ownership-Marker oder persistierten Ready-Status. Ist Port
`27777` belegt, scheitert der Start mit einer klaren Meldung.

Beim ersten vollständigen Prüfen oder Erstellen erzeugt die UI einmal einen
zufälligen Seed im Bereich `0..9007199254740991`. Sie speichert ihn vor dem
Hostaufruf im Draft. Danach bleibt er stabil. Vor diesem Zeitpunkt besitzt der
Entwurf keinen echten Seed; Java erzeugt keinen Seed.

`Spiel erstellen` finalisiert zuerst das Projekt und paketiert es danach. Nur
ein erfolgreicher Package-Aufruf der aktuellen UI-Sitzung zeigt das Spiel als
bereit. Ein Reload stellt diesen Zustand nicht wieder her. Nach einem
Packaging-Fehler wiederholt die UI den vollständigen Finalize-und-Package-
Ablauf. Beim Finalisieren ersetzt der Host jede Custom-Datei einzeln atomar und
`deer.json` zuletzt. Der Projektordner als Ganzes ist keine Transaktion.

## Projekt und Runtime

Eigene Bilder liegen inhaltsadressiert unter `assets/custom/`. Bereits mit dem
Spiel ausgelieferte Bilder werden ohne Kopie über ihren internen Pfad
referenziert. Ein Projekt ohne eigene Bilder kann nur aus `deer.json` bestehen.

Mit Java 25 öffnet `java -jar WizardRoom.jar` das Host-/Join-Menü. Der
Foundation-Vertrag bleibt DEER `0.4` mit mandatory AND-DAG. Runner-Identität,
Multiplayer-Vertrag und `PROTOCOL_VERSION` ändern sich durch den
Authoring-Refactor nicht.

`wizard/start_wizard_dev.cmd` ist ein Entwicklungslauncher. Eine
Zielgruppen-EXE oder ein Installer mit gebündelter Runtime bleibt ein späterer
Meilenstein. Der Entwickler- und CI-Pfad bleibt:

```text
gradlew.bat :wizard:buildWizardRoomJar -PwizardProject=<projektordner>
```

## Maßgebliche Dokumente

1. [`v0/frontend-handoff-overview-v0.md`](v0/frontend-handoff-overview-v0.md)
   beschreibt die Frontend-/Host-Grenze und die minimale Host-API.
2. [`v0/wizard-ui-flow-v0.md`](v0/wizard-ui-flow-v0.md) beschreibt den
   sichtbaren Autorenfluss.
3. [`v0/deer.schema.json`](v0/deer.schema.json) ist der maschinenlesbare
   Vertrag; [`v0/deer-json-spec.md`](v0/deer-json-spec.md) erklärt seine
   Semantik.
4. [`v0/runner-project-format.md`](v0/runner-project-format.md) definiert
   Projektordner, Finalisierung, Assets und Validierungsreports.
5. [`v0/runner-runtime-contract.md`](v0/runner-runtime-contract.md) definiert
   Packaging, Host-/Join-Grenze und Spielruntime.
6. [`../examples/foundation-v0.4/`](../examples/foundation-v0.4/) und
   [`../examples/the-last-hour-v0.4/`](../examples/the-last-hour-v0.4/) sind
   ausführbare Beispiele.

[`v0/the-last-hour-interaction-catalog.md`](v0/the-last-hour-interaction-catalog.md)
und [`research/`](research/) sind nicht normativ.
