# Wizard Runner Project Format V0.4

Status: V0.4-Runner- und Packaging-Contract

Scope: validierte Übergabe von der Wizard-UI an Packaging und Room-first-Host

## Projektgrenze

Das öffentliche Projektformat bleibt DEER `0.4`. Die Browser-UI speichert
private Drafts v1 und Uploadbytes ausschließlich in einer neuen IndexedDB. Der
Java-Host bindet an `127.0.0.1:27777`, bleibt bezüglich dieser Daten zustandslos
und führt Produktionsvalidierung sowie temporäres Packaging aus. Der
generische Wizard Runner liest ein vollständiges Projekt unverändert und leitet
den Raum ausschließlich im Speicher ab. Es entsteht weder Java-Code noch ein
Room-ZIP.

```text
wizard-project/
  deer.json
```

Ein Projekt mit eigenen Bildern ergänzt optional:

```text
wizard-project/
  deer.json
  assets/
    custom/
      foundation-note-3b50ea522803.png
```

Das Repository enthält unter
[`../../examples/foundation-v0.4/`](../../examples/foundation-v0.4/) ein
direkt ausführbares Projekt. Es ist die einzige kanonische Quelle für die
Beispiel-`deer.json` einschließlich Seed und das Custom-Asset.

`deer.json` liegt im Wurzelverzeichnis. Alle Assetpfade sind relative portable
Pfade mit Forward-Slashes und ohne führenden Slash. Eigene Bilder beginnen mit
`assets/custom/`; bereits in der Spiel-JAR enthaltene Bilder werden direkt über
interne Pfade wie `items/puzzle-piece.png` referenziert.

Der Runner liest als Projekteingaben ausschließlich `deer.json` und die darin
referenzierten Dateien unter `assets/custom/`. Im Authoring-Flow existiert
dieser Ordner nur temporär; Beispiele und der Gradle-/CI-Pfad verwenden ihn als
explizite Eingabe. `WizardRoom.jar` ist Ausgabe und keine erneute
Projekteingabe.

Die Java-Validierungsbibliothek liest diesen vollständigen Projektordner
read-only. Die Authoring-Integration bindet sie direkt an. Der Spielerfluss
verwendet die projektspezifische JAR. Der Room-first-Lebenszyklus steht im
[`runner-runtime-contract.md`](runner-runtime-contract.md).

## Projektspezifische Spieler-JAR

Die UI erzeugt mit dem gemeinsamen `WizardRoomPackager` und einer generischen
`WizardRoomTemplate.jar` genau ein verteilbares Spielerartefakt. Der Host
materialisiert den Browserkandidaten temporär, validiert ihn über dieselbe
`ProjectValidationService`- und `RoomDeriver`-Kette und liefert die fertige
`WizardRoom.jar` binär an den Browser. Nach einem Packaging-Fehler wiederholt
die UI diesen Ablauf mit dem aktuellen Draft. Zur Hostlaufzeit sind weder
Gradle noch Node erforderlich.

Der äquivalente Entwickler-/CI-Pfad lautet:

```text
gradlew.bat :wizard:buildWizardRoomJar -PwizardProject=<projektordner>
```

Die Ausgabe liegt unter `wizard/build/libs/WizardRoom.jar`. Die JAR enthält
Runner, Foundation- und Dungeon-Runtime, deren Abhängigkeiten und
Desktop-Natives, Basisassets, DEER-Schema, Runner-Version und Lizenzhinweise
sowie das vollständige Projekt in dieser Struktur:

```text
wizard/embedded-project/
  deer.json
  files.list
  assets/custom/...  # nur projektspezifische Custom-Dateien
```

`files.list` führt die eingebettete `deer.json` und die Dateien unter
`assets/custom/` auf. Gebündelte Assetpfade werden dort nicht als
projektspezifische Dateien aufgeführt; die Bilder sind bereits über die
Dungeon-/EscapeRoom-Runtimeabhängigkeiten in der JAR enthalten. Vor dem
Packaging durchläuft das Projekt dieselbe Produktionsvalidierung und
Room-Ableitung wie die Runtime; ungültige oder nicht ableitbare Projekte werden
nicht verpackt.

Die JAR ist projektspezifisch. Exakt dieselbe vollständige `WizardRoom.jar`
wird an Host und alle weiteren Spielenden verteilt; ein separater
Assets-only-Ordner wird nicht manuell verteilt. `java -jar WizardRoom.jar`
benötigt Java 25 und öffnet das Host-/Join-Menü.

## Draft und Packaging

- Ein UI-Entwurf darf unvollständig sein und bleibt außerhalb dieses Formats.
- Draft v1 und Uploadbytes liegen ausschließlich in einer neuen IndexedDB. Alte
  Browser- und AppData-Entwürfe werden nicht migriert. Mehrere Tabs sind in V0
  nicht unterstützt.
- Beim ersten Betreten der Abschlussseite erzeugt die UI einmal einen sicheren
  53-Bit-Seed und speichert ihn vor dem Hostaufruf im Draft. Java erzeugt oder
  ersetzt keinen Seed.
- Die UI projiziert den Draft auf Formatversion `0.4`.
- Sie bewahrt jede authorierte direkte Pflichtabhängigkeit und erfindet oder
  vervollständigt keine Progressionskanten.
- Der Host prüft Schema, Fachregeln, Spielergrenzen und Assetpfade vor dem
  Packaging. Bei einem ungültigen Report entsteht keine JAR.
- Kandidat, Custom-Assets und die serverseitige Ausgabe liegen nur in einem
  temporären Verzeichnis und werden nach der Antwort entfernt.
- Bei jedem späteren Packaging verwendet die UI den im Draft vorhandenen
  Seedwert unverändert; der Runner verändert ihn nie.
- Bei einem Spielbibliothek-Asset schreibt die UI ausschließlich dessen
  internen Pfad und Metadaten in den DEER-Eintrag; es wird keine Bilddatei
  kopiert.
- Bei einem eigenen Upload erzeugt die UI den inhaltsadressierten Pfad. Der Host
  prüft Pfad und Bytes beim Validieren und Packaging erneut und bettet das Bild
  unter `assets/custom/` in die Spieler-JAR ein.
- Der Host speichert weder Draft-/Uploaddaten noch Ready-Metadaten.

Ein browser-only Export, beliebiger `deer.json`-Import und Room-ZIP sind nicht
Teil des Produktflusses. `wizard/start_wizard_dev.cmd` ist nur ein
Entwicklungslauncher. Die aktuelle Spieler-JAR benötigt Java 25. Eine
Zielgruppen-`.exe`, beispielsweise über `jpackage`, und ein Installer mit
gebündelter Runtime bleiben spätere Distributionsmeilensteine.

## Seed in deer.json

`seed` ist ein verpflichtender ganzzahliger Top-Level-Wert in `deer.json`. Der
Wert liegt im Bereich `0..9007199254740991`. Strings, Fließkommazahlen,
negative Werte und größere Ganzzahlen sind unzulässig. Der Seed ist damit Teil
derselben schema-validierten Konfiguration wie alle anderen Authoring-Daten.

Der Bereich ist der nicht-negative IEEE-754-Safe-Integer-Bereich der von RFC
8785 verwendeten Zahlendarstellung. Dadurch bleibt jeder zulässige Seed in der
kanonischen Projektidentität eindeutig.

## Assetregeln

- Das Schema verlangt einen nicht leeren `path` und `mediaType` mit
  `image/png` oder `image/jpeg`.
- Beginnt der Pfad mit `assets/custom/`, behandelt der Runner ihn als
  Custom-Asset. Jeder andere Pfad ist eine gebündelte Referenz.
- `assets/bundled/` besitzt keine Sonderbedeutung und wird nur akzeptiert, wenn
  genau dieser Pfad in der internen Assetliste vorkommt.

Für Custom-Assets gilt:

- Sie liegen als flache Dateien unter `assets/custom/`.
- Dateinamen werden von der UI portabel normalisiert. Ihr Dateistamm endet mit
  `-` und den ersten zwölf lowercase Hex-Zeichen des SHA-256-Inhaltshashes, zum
  Beispiel `foundation-note-3b50ea522803.png`.
- Sie sind relative, normalisierte Pfade ohne Backslashes, Unterverzeichnisse,
  `.`- oder `..`-Segmente.
- Referenzierte Dateien müssen existieren; ein „optionales fehlendes Asset“
  gibt es nicht.
- MIME-Deklaration, Dateiendung und vollständig dekodierter Dateiinhalt müssen
  zusammenpassen.
- Symlinks werden nicht verfolgt. Pfade werden gegen den realen Projektroot
  aufgelöst und müssen nach Normalisierung weiterhin darunter liegen.
- Nicht referenzierte Dateien werden ignoriert und als Warnung gemeldet.
- Der Runner berechnet jeden Datei-Hash erneut und lehnt ein falsches
  Dateinamensuffix ab.

Für gebündelte Assets gilt stattdessen:

- Jeder andere Pfad muss exakt und case-sensitive in der bereits vom
  Gradle-Assetsystem erzeugten, gemergten `internal_assets.txt` vorkommen.
- Der Runner leitet den deklarierten `mediaType` nicht aus dem Pfad ab.
- Der Runner sucht dafür keine Datei im Projekt, liest keine Bildbytes und
  übernimmt das Asset nicht in die Liste verifizierter Custom-Assets.
- Eine im Build fehlende interne Assetliste ist ein technischer interner Fehler
  und kein angeblicher Fehler des Authoring-Projekts.

Inhaltsadressierte Dateien werden nie mit anderem Inhalt überschrieben. Fremde
und alte unreferenzierte Dateien werden weder überschrieben noch automatisch
gelöscht.

Für den Spielerfluss verteilt die technische Betreuung dieselbe
`WizardRoom.jar` an alle Teilnehmenden. Der Runner überträgt weiterhin keine
binären Assetdaten; jeder Client materialisiert die bereits eingebetteten
Custom-Assets für seine Laufzeit. Gebündelte Bilder lädt er direkt über den
normalen Dungeon-Assetloader.

Alle Teilnehmer validieren dasselbe vollständige lokale Projekt. Ein
ausschließlich gebündelte Assets verwendendes Projekt benötigt kein
Verzeichnis `assets/custom/`.

## Validierung und deterministische Identität

Der Runner:

- öffnet Projekt und Assets read-only;
- lehnt unbekannte `formatVersion`-Werte ab;
- validiert mit demselben Schema und denselben Rule-Codes wie die UI;
- validiert das verpflichtende `seed`-Feld über das DEER-Schema;
- verändert oder löscht keine Eingabedatei und schreibt keine abgeleiteten
  Projektartefakte;
- bildet erst nach erfolgreicher Validierung die In-Memory-Definitionen.

`hostInputSha256` ist der kleingeschriebene Hexwert von SHA-256 über die
UTF-8-Bytes der vollständigen RFC-8785-kanonisierten `deer.json`:

```text
lowercaseHex(SHA-256(UTF-8(RFC8785Canonicalize(complete deer.json))))
```

Whitespace, Einrückung und Property-Reihenfolge verändern diesen Hash nicht.
Jede inhaltliche Änderung in `deer.json` verändert ihn dagegen, darunter
Seed, Rätsel, Antworten, Assetpfade, Medientyp, Lizenz und Attribution.
Custom-Inhalte sind über das zuvor vollständig geprüfte zwölfstellige
SHA-256-Suffix ihres Pfads an die JSON gebunden. Gebündelte Inhalte stammen aus
derselben verteilten `WizardRoom.jar`. Die Assetvalidierung bleibt deshalb
zwingend vor der Hashbildung.

Auch reine Authoring-Angaben wie Lernziele, Nachbesprechung, Beschreibung,
Autor, Zielgruppe, Vorwissen, Schwierigkeit, Zeitschätzung, Asset-Lizenz,
Attribution sowie Riddle-Titel bleiben in diesen vollständigen DEER-Daten.
Riddle-Titel werden als Authoring-Labels in `ProjectDefinition` gemappt, aber
nicht in `GamePresentation` oder die Foundation-Runtime übernommen und nicht
angezeigt. Die übrigen genannten Angaben mappt oder bewertet der aktuelle
Java-Runner nicht semantisch; jede Textänderung verändert dennoch
`hostInputSha256`.

`rawDeerSha256` ist SHA-256 über die exakten Dateibytes der eingelesenen
`deer.json` und dient der Diagnose. `hostInputSha256` ist der oben definierte
Hash der vollständigen kanonischen `deer.json` und wird als reiner
Kompatibilitätsmarker übertragen. Bei reiner Umformatierung kann sich
`rawDeerSha256` ändern, während `hostInputSha256` gleich bleibt. Seed, Lösungen
und weitere Inhalte kommen aus der lokalen `deer.json` innerhalb der
Spieler-JAR und können von jedem JAR-Empfänger ausgelesen werden. Diese bewusste
Aufgabe lokaler Antwortgeheimhaltung ist Teil des Distributionsmodells.
Ausschließlich der Host wertet Antworten aus und entscheidet über korrekten
Spielfortschritt. Das Netzwerkformat steht in
[`runner-runtime-contract.md`](runner-runtime-contract.md).

## Fehler und Validierungsreports

Die Produktionsvalidierung liefert einen deterministischen
`ProjectValidationReport`. Die Authoring-Integration ruft die Java-Bibliothek
direkt auf. Der interne Gradle-Packaging-Schritt gibt denselben Report als
kanonisches JSON aus und schlägt bei ungültigem oder nicht ableitbarem Input
fehl; dann wird kein neues Spielerartefakt erzeugt.

[`project-validation-report.schema.json`](project-validation-report.schema.json)
ist der maschinenlesbare Vertrag des Reports. Jeder Report enthält:

- `valid`: genau dann `true`, wenn kein Issue mit `severity=error` vorliegt;
- `runnerVersion`;
- `rawDeerSha256` und `hostInputSha256`, sobald die jeweilige Phase erfolgreich
  erreicht wurde, sonst `null`;
- `issues` in der normativen Runnerreihenfolge.

Ein Issue enthält `severity`, `phase`, stabilen `code`, `messageKey`,
skalare `arguments`, den RFC-6901-Pointer `path`, optional `entity` sowie
`relatedPaths`. Die Frontend-Integration lokalisiert `messageKey` mit
`arguments` und verwendet Pfade und Entity-Identität für „Zum Feld“ oder „Zum
Rätsel“. Technische Codes und Pointer werden Lehrenden nicht roh angezeigt.
Sie prüft den Reportvertrag strikt und bildet bekannte Issues auf verständliche
Texte sowie die besitzenden Rätsel-, Spiel-Ende- oder Review-Kontexte ab; ein
unbekannter Report bleibt ein technischer Fehler.

Beispiel:

```json
{
  "valid": false,
  "runnerVersion": "0.4",
  "rawDeerSha256": "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
  "hostInputSha256": null,
  "issues": [
    {
      "severity": "error",
      "phase": "schema",
      "code": "SCHEMA_INVALID",
      "messageKey": "validation.schema.invalid",
      "arguments": {},
      "path": "/riddles/0",
      "entity": null,
      "relatedPaths": []
    }
  ]
}
```

Ein fehlender oder schemawidriger Report ist ein technischer Adapterfehler.
Der interne Gradle-Packaging-Schritt verwendet binäre Prozesscodes: `0`
bedeutet Erfolg, `1` Fehler. Diese Prozessgrenze ist keine öffentliche
Authoring-Schnittstelle.

Reports enthalten genau zwei Hashfelder: `rawDeerSha256` für die exakten
Dateibytes und `hostInputSha256` für die vollständige kanonische `deer.json`.
Im Netzwerk wird nur `hostInputSha256` als Kompatibilitätsmarker verwendet.
Validierung und Packaging-Prüfung mutieren weder Projektordner noch
Dungeon-Checkout.
