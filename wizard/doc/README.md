# Wizard documentation

Der Wizard führt nicht-technische Lehrende von einem privaten Entwurf zu einer
verteilbaren Spielerdatei:

```text
privater Wizard-Entwurf
-> temporäres DEER-Projekt
-> heruntergeladene WizardRoom.jar
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
alle Uploadbytes ausschließlich in einer neuen IndexedDB und bittet den Browser
beim Start um dauerhafte Speicherung. Alte Browser- oder AppData-Entwürfe
werden nicht migriert. Genau ein Tab darf gleichzeitig bearbeiten; ein weiterer
Tab erhält eine klare Warnung und bleibt in der Übersicht.

Der Java-Host bindet fest an `127.0.0.1:27777`. Er ist zustandslos bezüglich
Entwürfen und Uploads und übernimmt nur:

- Auslieferung der UI;
- Produktionsvalidierung;
- temporäres Packaging;
- Auslieferung der fertigen `WizardRoom.jar` als Browserdownload.

Der Host bietet keine Draft- oder Upload-Persistenz und speichert keinen
Ready-Status. Ist Port `27777` belegt, scheitert der Start mit einer klaren
Meldung.

Sobald der Entwurf und seine eigenen Dateien lokal vollständig und lesbar sind,
erzeugt die UI unmittelbar vor der ersten möglichen Produktionsprüfung einmal
einen zufälligen 53-Bit-Seed im Bereich `0..9007199254740991`. Sie speichert ihn
im Entwurf. Danach bleibt er stabil. Java erzeugt oder ersetzt keinen Seed.

Im nativen Host startet die Produktionsprüfung nach ungefähr zwei Sekunden ohne
inhaltliche Änderung. Sie ist nicht an die Seite `Spiel erstellen` gebunden.
Lokale Fehler verhindern diese Hintergrundprüfung, aber nicht den Seitenwechsel.
Die reine Browserentwicklung führt keine Java-Produktionsprüfung aus. `Spiel
erstellen und herunterladen` liest den aktuellen gespeicherten Stand, validiert
ihn erneut und paketiert ihn nur mit einem aktuellen gültigen Ergebnis.
Warnungen blockieren das Packaging nicht. Nur ein erfolgreicher Download der
aktuellen UI-Sitzung zeigt das Spiel als bereit. Ein Reload stellt diesen
Zustand nicht wieder her.

## Projekt und Runtime

Eigene Bilder liegen innerhalb der Spieler-JAR inhaltsadressiert unter
`assets/custom/`. Bereits mit dem Spiel ausgelieferte Bilder werden ohne Kopie
über ihren internen Pfad referenziert.

Mit Java 25 öffnet `java -jar WizardRoom.jar` das Host-/Join-Menü. Der
Foundation-Vertrag bleibt DEER `0.4` mit mandatory AND-DAG. Runner-Identität,
Multiplayer-Vertrag und `PROTOCOL_VERSION` ändern sich durch den
Authoring-Refactor nicht.

`wizard/start_wizard_dev.cmd` ist ein Entwicklungslauncher. Aktuell benötigen
Wizard und Spieler-JAR eine vorhandene Java-25-Runtime. Eine Zielgruppen-EXE,
beispielsweise über `jpackage`, und ein Installer mit gebündelter Runtime
sind nicht Teil des hier beschriebenen Auslieferungswegs. Der lokale
Authoring-Host richtet sich an Entwickler und technische Betreuung, nicht direkt
an Lehrende. Der Entwickler- und CI-Pfad bleibt:

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
4. [`v0/runner-project-format.md`](v0/runner-project-format.md) definiert das
   eingebettete Projekt, Assets, Packaging und Validierungsreports.
5. [`v0/runner-runtime-contract.md`](v0/runner-runtime-contract.md) definiert
   Packaging, Host-/Join-Grenze und Spielruntime.
6. [`../examples/foundation-v0.4/`](../examples/foundation-v0.4/) und
   [`../examples/the-last-hour-v0.4/`](../examples/the-last-hour-v0.4/) sind
   ausführbare Beispiele.

[`v0/the-last-hour-interaction-catalog.md`](v0/the-last-hour-interaction-catalog.md)
und [`research/`](research/) sind nicht normativ.
