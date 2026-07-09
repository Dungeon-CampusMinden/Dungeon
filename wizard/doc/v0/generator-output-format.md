# Generator Output Format V0.2

Status: Foundation-Contract
Scope: Ausgabe des Java-Generators

## Artefakt

Der Java-Generator erzeugt genau ein **generiertes Modul-ZIP**. Es ist ein
Quellmodul für den Dungeon-Checkout, kein UI-Export und noch kein
plattformunabhängiger Ein-Klick-Installer.

```text
<room-id>-module.zip
  <module-id>/
    build.gradle
    src/
      starter/
        GeneratedRoom.java
        GeneratedRoomServer.java
        GeneratedRoomClient.java
      level/
        GeneratedRoomLevel.java
      generated/
        GeneratedRoomDefinition.java
    assets/
      levels/
        <level-name>/
          <level-name>_1.level
      custom/
        ...
    generated/
      room-manifest.json
      generator-report.json
      facilitator-guide.json
      source/
        deer.json
      settings-entry.txt
```

Archiv-, Modul- und Levelname werden portabel aus der stabilen Room-ID
abgeleitet. Java-Packages und Klassennamen bleiben innerhalb des isolierten
Moduls fest (`starter.GeneratedRoom*`, `level.GeneratedRoomLevel` und
`generated.GeneratedRoomDefinition`). Das generierte `build.gradle` folgt den
vorhandenen Modulkonventionen und hängt von `:escapeRoom` ab.
`settings-entry.txt` enthält die nötige Root-`settings.gradle`-Eintragung für
die technische Integration.

Alle DEER-IDs sind auf 64 Zeichen begrenzt. Generierte Datei- und
Verzeichnisnamen erhalten ein `wizard-`/`wizard_`-Präfix, werden auf
Windows-reservierte Namen und Pfadlängen geprüft und kollidieren nicht nach
Case- oder Unicode-Normalisierung.

`level-name` verwendet Kleinbuchstaben, Ziffern und Bindestriche, aber keine
Unterstriche. Der vorhandene `DungeonLoader` teilt den gesamten Dateinamen an
jedem Unterstrich und akzeptiert genau zwei Teile: Levelname und Variante.
Deshalb wird beispielsweise aus `wizard_foundation_v0_2` der Dateiname
`wizard-foundation-v0-2_1.level`.

Die technische Betreuung entpackt bzw. integriert das Modul und baut es mit
dem realen Root-Gradle-Setup. `buildGeneratedRoomJar` erzeugt bereits ein
developer-facing runnable Fat JAR; ein lehrenden-/spielerfertiger Launcher,
Installer oder gebündeltes Java-Runtime-Paket bleibt ein späterer
Produkt-Slice.

Der generierte Build stellt mindestens diese Tasks bereit:

```powershell
gradlew.bat :<module-id>:compileJava
gradlew.bat :<module-id>:runGeneratedRoom
gradlew.bat :<module-id>:runGeneratedRoomServer
gradlew.bat :<module-id>:runGeneratedRoomClient
gradlew.bat :<module-id>:buildGeneratedRoomJar
```

## Manifest

`room-manifest.json` enthält mindestens:

```json
{
  "packageFormatVersion": "0.2-draft",
  "roomId": "wizard_foundation_v0_2",
  "moduleId": "wizard_foundation_v0_2",
  "deerFormatVersion": "0.2-draft",
  "generatorVersion": "0.1.0",
  "inputSha256": "<sha256>",
  "seed": "123456789",
  "layoutProfile": "foundation_single_room_v1",
  "mainClass": "starter.GeneratedRoom",
  "serverMainClass": "starter.GeneratedRoomServer",
  "clientMainClass": "starter.GeneratedRoomClient",
  "levelName": "wizard-foundation-v0-2",
  "playerCount": {
    "min": 1,
    "max": 4
  },
  "assetMappings": [
    {
      "authoringPath": "assets/custom/3b50ea522803-foundation-note.png",
      "runtimePath": "custom/3b50ea522803-foundation-note.png",
      "sha256": "3b50ea522803ed6e067c75c00df584271a6e8fd62896b63eab4d64d618f0d1a9"
    }
  ],
  "requiredModules": [
    "dungeon",
    "escapeRoom"
  ]
}
```

`assetMappings` ist vollständig und Pflicht. Der Generator kopiert
`assets/custom/<file>` in den Modul-Source-Root `assets/custom/<file>`; der
LibGDX-interne Pfad entfernt den Source-Root-Präfix und lautet
`custom/<file>`. Zusätzlich darf das Manifest aufgelöste Platzierungen
enthalten. Es enthält keine UI-Draft-Daten.

## Determinismus

- V0.2 nutzt `foundation_single_room_v1` als festes Layoutprofil.
- `inputSha256` ist SHA-256 über ein RFC-8785-kanonisiertes JSON-Objekt mit
  der vollständigen DEER-Konfiguration und einer nach Pfad sortierten Liste
  `{path, sha256}` aller referenzierten Assets.
- Der Standard-Seed entsteht aus SHA-256 über dieses ebenfalls RFC-8785-
  kanonisierte Objekt:

  ```json
  {
    "generatorVersion": "0.1.0",
    "inputSha256": "<sha256>",
    "layoutProfile": "foundation_single_room_v1"
  }
  ```

  Die ersten 63 Bit werden als positive Dezimalzeichenfolge im Manifest
  gespeichert.
- Ein technischer `--seed`-Override ist zulässig und wird im Manifest
  festgehalten; er gehört nicht in `deer.json`.
- Gleicher Input, gleiche Assets, Generatorversion, Profil und Seed erzeugen
  dieselbe logische Geometrie, Platzierung und Runtime-Konfiguration.
- Für byte-identische ZIPs werden Eintragsreihenfolge, Pfadseparatoren,
  Kompressionseinstellungen und Zeitstempel normalisiert.

## Generierter Runtime-Inhalt

Das Modul enthält:

- eine `DungeonLevel`-Unterklasse und einen Starter;
- genau eine `.level`-Variante mit Layout, Startpositionen, benannten Punkten,
  Dekorationen und Tile-Matrix;
- Foundation-Definitionen für Rätsel, Ressourcen, Hilfen und Ausgang;
- `facilitator-guide.json` mit Lernzielen, Reflexionsfragen, Lösungen und
  Hilfereihenfolge;
- kopierte Assets unter runtime-internen, kollisionsfreien Pfaden;
- Konfiguration des gemeinsamen Foundation-Progressionslayers;
- Build- und Run-Tasks nach bestehenden Projektmustern.

Minimaler Guide-Contract:

```json
{
  "formatVersion": "0.2-draft",
  "roomId": "wizard_foundation_v0_2",
  "title": "Foundation Beispielraum V0.2",
  "learningObjectives": [
    {
      "id": "lo_extract_numeric_clue",
      "description": "..."
    }
  ],
  "debriefPrompts": [
    "..."
  ],
  "solutions": [
    {
      "riddleId": "r_enter_code",
      "title": "Zahlencode eingeben",
      "answerSummary": "3758",
      "hints": [
        "Der Code besteht aus vier Ziffern."
      ]
    }
  ]
}
```

## Engine-Bindung

`GeneratedRoomLevel` besitzt den vom vorhandenen `LevelFormatParser`
erwarteten öffentlichen Konstruktor:

```java
public GeneratedRoomLevel(
    LevelElement[][] layout,
    DesignLabel designLabel,
    Map<String, Point> namedPoints)
```

Alle Starter registrieren `levelName` zusammen mit
`GeneratedRoomLevel.class` über `DungeonLoader.addLevel(...)`. Die generierte
`.level`-Datei enthält eine DoorTile und dahinter eine ExitTile. Da eine
DoorTile aus der Leveldatei offen entsteht, schließt
`GeneratedRoomLevel.onFirstTick()` sie auf der autoritativen lokalen bzw.
Server-Seite vor Interaktionsbeginn. Im Multiplayer wird der geschlossene
Zustand vor dem ersten veröffentlichten Snapshot gesetzt; Clients können erst
nach diesem Snapshot interagieren.

Ein nur auf der autoritativen lokalen bzw. Server-Seite laufendes
Foundation-Completion-System wertet die ExitTile aus. Die generierten Starter
ersetzen vor Interaktionsbeginn auf **jeder** Authority den vorhandenen
`LevelSystem.onEndTile`-Callback durch eine No-op. Damit kann weder Server noch
Client über den standardmäßigen
`DungeonLoader::loadNextLevel`-Pfad konkurrierend abschließen. Ausschließlich
das Foundation-Completion-System setzt den idempotenten Session-Ausgang;
Multiplayer-Clients zeigen ihn nur aus dem autoritativen Snapshot.

Der Server wird mit `--players N` gestartet, wobei
`playerCount.min <= N <= playerCount.max` gilt. Die Roster-Liste schließt vor
der Ready-Barriere; weitere Verbindungen werden abgelehnt, bestehende
Roster-Slots dürfen reconnecten. Ein serverseitiger Spawn-Allocator ordnet die
`ILevel.startTiles()` deterministisch nach Roster-Slot zu. Roster, Slot,
Spawnpunkt und Ready-Zustand sind Teil des Foundation-Snapshots.

„Aktiv“ bedeutet Mitglied dieses geschlossenen Rosters. Bei Disconnect pausiert
die Session einschließlich Timer und Endauswertung, bis der Slot reconnectet
oder die technische Betreuung die Session abbricht. Dadurch kann ein
Disconnect den Ausgang weder freigeben noch dauerhaft aus der Erfolgsbedingung
entfernen.

Der serverautoritative Timer startet nach einer Ready-Barriere: alle für die
Session aktiven Spielenden sind gespawnt und haben das Intro bestätigt.
Spätere Verbindungen sind nur als Reconnect zulässig. Der Server hält eine
einzige Deadline und verarbeitet Abschluss oder Timeout in seiner
Ereignisreihenfolge genau einmal. Bei `hard` ist ein vor der Deadline
verarbeiteter Abschluss erfolgreich, andernfalls gewinnt der Timeout; `soft`
markiert nur Überzeit.

`deer.json` wird unter `generated/source/` nur zur Reproduzierbarkeit
mitgeführt. Die Runtime darf sie nicht als veränderlichen Live-Zustand
behandeln.

Bildbeschreibungen werden in der generierten Definition und im
Betreuungsmaterial erhalten. Die Foundation-Runtime bietet sie als
Textalternative an, wo der konkrete Renderer dies unterstützt. Das ist keine
pauschale WCAG-Zusage für die bestehende LibGDX-Runtime.

## Report und Fehlerverhalten

`generator-report.json` verwendet den Issue-Contract aus
[`implementation-handoff-v0.md`](implementation-handoff-v0.md) und enthält
zusätzlich Generator- und Manifestdaten.

Report-Envelope:

```json
{
  "reportFormatVersion": "0.2-draft",
  "valid": true,
  "generatorVersion": "0.1.0",
  "rawDeerSha256": "<sha256>",
  "inputSha256": "<sha256>",
  "issues": []
}
```

- Validierungs- oder Generierungsfehler erzeugen kein finales ZIP.
- Jeder Lauf schreibt zusätzlich
  `<room-id>-generator-report.json` als Sidecar in das Ausgabeziel und dasselbe
  JSON nach stdout. Bei nicht parsebarer `deer.json` ist `rawDeerSha256`
  gesetzt und `inputSha256=null`; bei nicht lesbarer Datei sind beide `null`.
  Ist die Room-ID nicht lesbar, lautet der Sidecar `generator-report.json`.
- Fehler verwenden einen Exit-Code ungleich null. Der Sidecar wird selbst über
  eine temporäre Datei sicher ersetzt.
- Der Generator schreibt zuerst in ein neues temporäres Ziel und benennt es
  erst nach erfolgreicher Prüfung um.
- Vorhandene Ausgaben werden ohne explizites `--force` nicht überschrieben.
- Ein Fehler mutiert weder Projektordner noch Dungeon-Checkout.
- Archivpfade werden gegen Zip-Slip und doppelte normalisierte Namen geprüft.

## Kandidatenprüfung

Vor dem ZIP-Schritt liegt das Modul in einem neuen temporären Staging-Ordner.
Die Prüfung läuft in einer temporären Worktree-/Quellkopie mit eigenem
Gradle-User-Home. Damit schreiben Asset-Generierung und Kompilierung
ausschließlich unter den temporären Root. Der Arbeitscheckout bleibt
unverändert.

Pro Artefakt sind mindestens erforderlich:

- `compileJava` mit Java 25;
- `buildGeneratedRoomJar`;
- Generierungsreport ohne Fehler;
- Manifest-, Level-, Asset- und Main-Class-Konsistenz;
- erfolgreicher, zeitlich begrenzter headless Smoke-Start. Der generierte
  Server unterstützt dafür `--smoke-test`, meldet „ready“ maschinenlesbar und
  beendet sich anschließend selbst.

Erst danach wird aus einer expliziten Source-Allowlist dieses geprüften
Staging-Inhalts das finale ZIP gebaut. `build/`, Gradle-Caches, Logs und
temporäre Dateien werden nie paketiert.

## Multiplayer-Abnahme des Foundation-Generators

Vor Freigabe der Generatorversion prüft der kanonische Foundation-Beispielraum:

- Fund, Keypad, Graph, Timer und Ende laufen serverautoritativ;
- alle Clients sehen denselben Rätsel- und Türzustand;
- ein reconnectender Client erhält den aktuellen Snapshot;
- `--players N` begrenzt und schließt das Roster;
- jeder Roster-Slot besitzt einen eigenen gültigen Spawnpunkt;
- ein hartes Zeitlimit beendet die Sitzung einmalig und gemeinsam;
- Ausgangserfolg tritt ein, wenn alle aktiven Spielenden den offenen Ausgang
  erreicht haben;
- kein Client kann Progression durch lokale Zustandsänderung freischalten.

Eine Generatorversion ist erst nach diesen Checks für V0.2 freigegeben.
