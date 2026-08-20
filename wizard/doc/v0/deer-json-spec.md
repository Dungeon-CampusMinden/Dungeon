# deer.json Specification V0.4

Status: kanonischer implementierter Contract für `formatVersion=0.4`

## 1. Rolle und Lebenszyklus

`deer.json` ist die vollständige finalisierte Authoring-Konfiguration für den
generischen Wizard Runner. Ein unvollständiger UI-Entwurf bleibt ein privates
Format des Autorenwerkzeugs und ist keine teilweise gültige `deer.json`.

Die Authoring-App schreibt genau ein DEER-Projekt:

```text
wizard-project/
  deer.json
  assets/custom/...  # nur bei eigenen Bildern
  WizardRoom.jar     # nach erfolgreichem Packaging
```

Sie erzeugt weder Java-Code noch ein Raummodul, Buildskripte oder ein Room-ZIP.
Der Runner liest als Eingaben nur `deer.json` und referenzierte Custom-Assets
und leitet daraus deterministisch einen Foundation-Raum im Speicher ab. Die UI
bettet das finalisierte Projekt anschließend mit dem gemeinsamen Java-Packager
und einer generischen `WizardRoomTemplate.jar` in eine projektspezifische
ausführbare `WizardRoom.jar` ein. Diese JAR ist Ausgabe, keine Runner-
Projekteingabe.

Dieselbe JAR wird an Host und alle weiteren Spielenden verteilt. Damit besitzen
alle JAR-Empfänger lokal auch `deer.json`, Seed, Lösungen und unveröffentlichte
Inhalte. Sie leiten daraus denselben vollständigen Foundation-Raum ab.

## 2. Top-Level und Seed

```json
{
  "formatVersion": "0.4",
  "seed": 123456789,
  "metadata": {},
  "learningDesign": {},
  "session": {},
  "scenario": {},
  "surfaces": [],
  "riddleGraph": {},
  "riddles": [],
  "assets": []
}
```

| Feld | Zweck |
|---|---|
| `formatVersion` | Exakt `0.4`. |
| `seed` | Stabiler Layout-Seed als Ganzzahl von `0` bis `9007199254740991`. |
| `metadata` | Stabile Projekt-ID, Titel, Inhaltssprache und optionale redaktionelle Angaben. |
| `learningDesign` | Lernziele und Fragen zur Nachbesprechung. |
| `session` | Zielgruppe, Vorwissen, Spielergrenzen und Zeitlimit. |
| `scenario` | Theme und Storytexte. |
| `surfaces` | Stabile Identitäten der Orte und Interaktionsflächen. |
| `riddleGraph` | Progression und gemeinsames Erfolgsziel. |
| `riddles` | Ausführbare Rätsel, Inhalte und optionale Hinweise. |
| `assets` | Referenzierte PNG-/JPEG-Dateien. |

Der Authoring-Refactor ändert weder `formatVersion` noch die Semantik dieses
Dokuments. Ein unvollständiger Draft besitzt keinen echten Seed. Beim ersten
Betreten der Abschlussseite erzeugt die UI genau einmal einen Wert im Bereich
`0..9007199254740991` und speichert ihn vor dem
Hostaufruf im Browserdraft. Danach bleibt er stabil. Der Java-Host erzeugt und
ersetzt keinen Seed. Der Bereich ist der lückenlos exakt darstellbare
nicht-negative Safe-Integer-Bereich der RFC-8785-Zahlendarstellung. Weitere Schreibregeln
stehen in
[`runner-project-format.md`](runner-project-format.md).

## 3. IDs und Referenzen

IDs beginnen mit einem Kleinbuchstaben und enthalten danach nur lowercase
ASCII-Buchstaben, Ziffern, `_` und `-`. Sie sind höchstens 64 Zeichen lang.

Die UI hält Lernziel-, Surface-, Graphknoten-, Rätsel-,
Informationsquellen-, Input-, Resource-, Hinweis- und Asset-IDs im
finalisierten Authoring-Dokument stabil und dokumentweit eindeutig. Sie erzeugt
eine ID einmal und verändert sie bei Umbenennung nicht. IDs dürfen keine
Lösungen oder noch nicht freigegebenen Inhalte codieren.

Die Authoring-Felder `metadata.description`, `metadata.author`,
`learningDesign`, `session.targetAudience`, `session.priorKnowledge`,
`difficulty`, `estimatedMinutes`, `learningObjectiveIds`, Riddle-`title` und
Asset-`source` werden schema-validiert und vollständig in den Host-Input-Hash
aufgenommen. Riddle-`title` wird als Authoring-Label in `ProjectDefinition`
gemappt, aber nicht in `GamePresentation` oder die Foundation-Runtime
übernommen und nicht angezeigt. Die übrigen genannten Felder mappt oder
interpretiert der aktuelle Foundation-Runner bewusst nicht.

## 4. Projektdaten

### 4.1 metadata

```json
{
  "id": "wizard_foundation_v0_4",
  "title": "Foundation Beispielraum V0.4",
  "locale": "de-DE",
  "description": "Kleiner Foundation-Slice.",
  "author": "Beispiel Lehrkraft"
}
```

`id`, `title` und `locale` sind Pflicht. `description` und `author` sind
optionale Authoring-Metadaten.

### 4.2 learningDesign

`objectives` enthält mindestens ein Lernziel mit stabiler `id` und
`description`. `debriefPrompts` ist ein stets vorhandenes, gegebenenfalls
leeres Array von Reflexionsfragen. Jedes Rätsel trägt mindestens eine
`learningObjectiveIds`-Referenz. Diese Angaben strukturieren das Authoring,
steuern aber weder Progression noch Runtime-Validierung.

### 4.3 session

```json
{
  "targetAudience": "Lernende im Bereich IT-Sicherheit",
  "priorKnowledge": "Grundlagen zu einfachen Codes und Hinweisen.",
  "playerCount": {
    "min": 1,
    "max": 4
  },
  "time": {
    "limitMinutes": 30,
    "limitMode": "hard"
  }
}
```

`targetAudience` und `priorKnowledge` sind Pflichttexte. `playerCount.min` und
`playerCount.max` liegen in `1..4`; zusätzlich gilt
`min <= max`. `max` bestimmt die Hostkapazität sowie die für die Dauer des
Hostprozesses reservierbaren Dungeon-Identitäten und logischen Authority-Slots.
Alle Spieler verwenden den gemeinsamen Startpunkt des Raums. Eine einmal
vergebene Identität wird nicht durch einen neuen Client ersetzt; nach voller
Erstbelegung ist nur der normale Best-Effort-Reconnect derselben Identität
möglich. Sobald `min` technisch spielbereite Clients erreicht sind, sehen alle
aktuell technisch spielbereiten Clients zuerst alle `introText`-Seiten in
Array-Reihenfolge und danach `mission` als hervorgehobene letzte Intro-Seite.
Erst wenn sie diese Sequenz abgeschlossen haben, beginnen initial Authority,
Timer und Gameplay. Später beitretende Clients werden nach ihrer eigenen
Intro-Sequenz spielbereit.

`time.limitMinutes` liegt in `1..240`. Bei `limitMode=hard` beendet das
Zeitlimit die Sitzung erfolglos und `scenario.failureText` ist Pflicht. Bei
`soft` läuft die Sitzung nach Ablauf weiter und `failureText` ist optional.

### 4.4 scenario

```json
{
  "themeId": "default",
  "mission": "Findet den Zugangscode und verlasst gemeinsam den Raum.",
  "introText": [
    "Die Ausgangstür ist verschlossen."
  ],
  "successText": [
    "Die Gruppe hat den Ausgang erreicht."
  ],
  "failureText": [
    "Die Zeit ist abgelaufen."
  ]
}
```

`themeId` bestimmt neben dem visuellen Theme den geordneten Pool spielbarer
`CharacterClass`-Skins. Im aktuellen Vertrag ist die ID fest `default`; ihr Pool
besteht in dieser Reihenfolge aus `THE_LAST_HOUR_ROGUE` und
`THE_LAST_HOUR_CHAR03`. Offizielle Wizard-Clients wählen keinen Skin aus. Der
Server weist die Pool-Einträge zyklisch zu, sodass die ersten beiden Spieler
verschiedene Skins erhalten und ein Skin erst nach Erschöpfung des Pools erneut
verwendet wird. Die ID bleibt als Erweiterungspunkt für zukünftige Themes
erhalten. `mission`, `introText` und `successText` sind Pflicht. Die drei
Text-Arrays müssen mindestens eine nicht-leere Seite enthalten; jeder Eintrag
wird in Array-Reihenfolge als eigene weiterklickbare Black-Fade-Seite angezeigt.
`failureText` ist nur bei hartem Zeitlimit verpflichtend.

## 5. surfaces

`surfaces` macht die gemeinsame Identität eines fachlichen Ortes oder einer
Interaktionsfläche explizit. Jede Surface enthält `id`, `kind` und
spielergerichteten `title`. Der aktive Vertrag kennt ausschließlich:

- `world`: der gemeinsame Raum, genau einmal;
- `container`: Fund in einem Behälter;
- `keypad`: numerische Eingabe;
- `door`: gemeinsamer Ausgang, genau einmal.

Jede `container`-Surface gehört im Foundation-Profil genau einer
Informationsquelle und jede `keypad`-Surface genau einem Input. Die
`world`-Surface beschreibt den gemeinsamen Raum und ist keine Fundstation. Der
Endknoten referenziert die `door`-Surface. Diese Surface-IDs bleiben vom
DEER-Projekt über Layoutplatzierung bis zur Foundation-Runtime identisch.

Diese technischen Datensätze sind in der Authoring-UI verborgen. Lehrende
benennen den Fundort direkt an der Informationsquelle, das Gerät direkt an der
Zahleneingabe und den Ausgang unter „Spiel-Ende“. Die UI verwaltet genau eine
World- und Door-Surface sowie je eine private Container-Surface pro Quelle und
Keypad-Surface pro Numeric-Input. Collection-Inputs verwenden die Surface der
gewählten Quelle und besitzen keine eigene. Erzeugen, Löschen und Typwechsel
ändern Surfacebestand und Besitzer atomar; Umbenennen oder Umordnen ändert
keine stabile ID. Eine separate Orte-/Surface-Ansicht existiert nicht.

Ein Computer- oder allgemeiner Device-Typ ist nicht Teil dieses Vertrags. Eine
spätere Computer-Surface oder mehrere Bindungen an dieselbe Surface benötigen
eine ausdrückliche Erweiterung des Profils, Schemas und der Runtime.

Ein Rätsel besitzt keine `roomId`. Mehrere Räume und ein Raumgraph sind noch
nicht Teil des Foundation-Slices. Eine spätere Mehrraum-Ableitung ordnet die
bereits stabilen Surfaces Räumen zu; dadurch kann eine Informationsquelle in
einem früheren Raum liegen als der zugehörige Input, ohne das Rätselmodell zu
ändern.

## 6. riddleGraph

Der Graph beschreibt die strukturelle Progression als beliebigen endlichen
azyklischen Graphen verpflichtender AND-Abhängigkeiten. Jede direkte Kante
`A -> B` bedeutet, dass `A` eine notwendige Voraussetzung von `B` ist. Bei
mehreren eingehenden Kanten müssen alle direkten Vorgänger erfüllt sein.
Jeder Knoten muss den einzigen Endknoten erreichen; deshalb bleiben sämtliche
Rätsel verpflichtend. Vollständige geordnete Abschnittsgraphen sind ein
gültiger einfacher Teilfall. OR-Verzweigungen, optionale oder bedingte Pfade,
mehrere Ausgänge und mehrere Räume sind nicht darstellbar.

Der Graph ist die einzige Progressionsquelle. Jedes Rätsel durchläuft
serverautoritativ und monoton `LOCKED -> ACTIVE -> SOLVED`. Vor dem
Sessionstart sind alle Rätsel gesperrt. Beim Start werden ausschließlich die
direkten Nachfolger des Startknotens aktiv. Ein späteres Rätsel wird erst
aktiv, wenn alle seine Vorgängerrätsel gelöst sind. Der implizite Abschluss
`SOLVED(riddleId)` speist den Graphen; ein separates Output- oder Effektfeld
existiert nicht.

Knotenvarianten:

- `start`: `id` und `kind=start`;
- `riddle`: `id`, `kind=riddle` und `riddleId`;
- `end`: `id`, `kind=end` und `surfaceId` auf die Door-Surface.

Das Knotenarray enthält genau einen Start- und genau einen Endknoten. Der Runner
findet beide über `kind`. Eine Kante enthält ausschließlich `from` und `to`;
dieses Paar beschreibt sie eindeutig.

Semantische Pflichtregeln:

- Start hat keine eingehende, Ende keine ausgehende Kante;
- keine Self-Edges oder doppelten `from/to`-Paare;
- jeder Knoten ist vom Start erreichbar und kann das Ende erreichen;
- der Graph ist azyklisch;
- `riddles[]` und Riddle-Knoten bilden eine Bijektion;
- die Zahl der Kanten überschreitet die Runner-Kapazität von 4096 nicht;
- die `surfaceId` des Endknotens verweist auf die Door-Surface.

Der Endknoten besitzt allein die durch `surfaceId` referenzierte Ausgangstür.
Sobald alle direkten Vorgänger des Endknotens gelöst sind, wird das Ende
erreicht und diese Tür serverautoritativ geöffnet. Erfolg tritt ein, wenn alle
aktuell technisch spielbereiten Spieler den offenen Ausgang erreicht haben.

## 7. riddles

Gemeinsame Pflichtfelder:

| Feld | Bedeutung |
|---|---|
| `id` | stabile Rätsel-ID und Anker der deterministischen Runtime-Ableitung |
| `title` | Authoring-Label; bleibt im DEER-Projekt, wird aktuell nicht angezeigt |
| `learningObjectiveIds` | mindestens eine Authoring-Referenz auf ein Lernziel |
| `estimatedMinutes` | verpflichtende redaktionelle Zeitschätzung als Ganzzahl von `1` bis `9007199254740991` |
| `difficulty` | optionale redaktionelle Schwierigkeit |
| `informationSources` | null oder mehr Informationsquellen |
| `inputs` | mindestens ein Input; alle Inputs sind mit AND verknüpft |
| `hints` | geordnete optionale Hinweise; sonst `[]` |

Ein Rätsel hat genau einen universellen, impliziten Output:
`SOLVED(riddleId)`. Er wird beim ersten erfolgreichen Abschluss aller Inputs
atomar erzeugt und deshalb nicht zusätzlich in `deer.json` gespeichert.
Generische Effekte, frei formulierbare Bedingungen, OR-Verknüpfungen und eine
Trigger-DSL sind nicht Teil dieses Vertrags.

### 7.1 Informationsquellen

Eine Informationsquelle enthält:

- eine stabile `id`;
- `surfaceId` auf eine Container-Surface;
- `resources` als geordnete, nicht leere Folge aus Inline-Texten oder
  Asset-Resources.

Informationsquellen enthalten Informationen oder Aufgabeninhalte, sind selbst
aber keine Abschlussbedingung. Sie dürfen unabhängig vom Rätselstatus
erreichbar und lesbar sein. Damit kann eine Quelle später auch in einem
früheren Raum liegen als der zugehörige Input. Soll eine Interaktion mit ihr
verpflichtend sein, referenziert ein Collection-Input die Quelle.

### 7.2 Inputs

Jeder Input besitzt eine stabile `id` und eine geschlossene `type`-Variante.
Ein Inputschritt wird ausschließlich für ein `ACTIVE`-Rätsel akzeptiert.
Versuche bei `LOCKED` oder `SOLVED` verändern weder Teilfortschritt noch
anderen gemeinsamen Zustand.

Der Zahlencode ist die Variante `type=numeric`:

```json
{
  "id": "input_exit_code",
  "type": "numeric",
  "surfaceId": "s_exit_keypad",
  "answer": "3758",
  "showDigitCount": true
}
```

Er enthält `surfaceId` auf eine Keypad-Surface, `answer` mit genau 1 bis 8
Ziffern und den expliziten Boolean `showDigitCount`. Falsche Versuche bleiben
unbegrenzt möglich. Die Lösung liegt bei jedem Teilnehmer in der vollständigen
lokalen Definition; ausgewertet und als Fortschritt übernommen wird sie
ausschließlich hostautoritativ.

Die Variante `type=collection` enthält eine `informationSourceId` und verwendet
dieselbe Surface wie diese Quelle; sie erzeugt keinen eigenen Placementpunkt.
Sie wird genau durch eine serverseitig akzeptierte Interaktion mit der
referenzierten Informationsquelle erfüllt, während das Rätsel `ACTIVE` ist.
Das gilt auch, wenn alle Inhalte der Quelle bereits vorher gelesen oder
freigegeben wurden. Früheres Lesen erzeugt keinen Inputfortschritt und wird bei
der Aktivierung nicht automatisch angerechnet. Ohne diesen Input bleibt
dieselbe Quelle reine Information beziehungsweise Aufgabeninhalt und
beeinflusst den Rätselabschluss nicht. Ein Inventarsystem ist damit nicht
verbunden.

## 8. Inhalte und Assets

### 8.1 resources

Resources gehören genau zu der Informationsquelle, in deren `resources`-Array
sie stehen.
Der aktuelle Vertrag kennt zwei geschlossene Varianten:

```json
{
  "id": "res_keypad_code",
  "kind": "inline_text",
  "title": "Notiz mit Zahlencode",
  "text": "Die markierten Zahlen ergeben 3758."
}
```

```json
{
  "id": "res_note_image",
  "kind": "asset",
  "title": "Papiernotiz",
  "assetId": "asset_foundation_note"
}
```

Beide Varianten enthalten `id`, `kind` und `title`. Ein Inline-Text enthält
zusätzlich `text`, eine Asset-Resource ausschließlich `assetId` auf ein
deklariertes Asset. Inline-Text wird als Textdialog ohne Bild angezeigt;
Asset-Resources zeigen das referenzierte Bild. Das Stationsbild der
Informationsquelle ist davon unabhängig.

### 8.2 hints

Ein optionaler Hinweis enthält `id`, `title`, `text` und `severity`.
`severity` beschreibt nicht die Schwierigkeit als freie Zahl, sondern die Art
der vorweg angekündigten Offenlegung:

| Wert | Spielerfreundliche Bedeutung |
|---|---|
| `orientation` | Was ist die Aufgabe und wo kann ich anfangen? |
| `approach` | Wie kann ich die Aufgabe lösen? |
| `solution` | Was ist die Lösung? |

Die technischen Enum-Werte bleiben in der UI verborgen. Vor jeder
Hinweisfreigabe zeigt die Runtime die spielerfreundliche Bedeutung der nächsten
`severity` und verlangt eine ausdrückliche Bestätigung. Das gilt auch für
`orientation` und `approach`, damit eine Fehlinteraktion keinen Hinweis
freigibt. Abbrechen verändert keinen Zustand und rückt nicht zum nächsten
Hinweis vor.

Ein neuer Hinweis kann nur für ein `ACTIVE`-Rätsel angefordert werden. Nach der
Bestätigung zeigt jede weitere Anforderung den nächsten Hinweis. Die sichtbare
Arrayreihenfolge ist die Freigabereihenfolge und unabhängig von `severity`.
Bereits freigegebene Hinweise bleiben auch nach dem Rätselabschluss lesbar.
Hinweistexte und -titel liegen in der vollständigen lokalen Definition. Ihre
Anzeige und Freigabereihenfolge bleiben hostautoritativ.

### 8.3 assets

Ein Asset enthält:

- eine global eindeutige `id`;
- einen nicht leeren `path`;
- `mediaType` mit `image/png` oder `image/jpeg`;
- `source` mit nicht leerer `license` und optionaler `attribution`.

`source` enthält ausschließlich diese Authoring-Metadaten. Zusätzliche Felder
sind ungültig. Eine fehlende, leere oder ausschließlich aus Leerraumzeichen
bestehende Attribution bedeutet fachlich, dass keine Attribution vorliegt. Der
Runner interpretiert Lizenz und Attribution nicht semantisch; als strukturell
unterschiedliche Teile der vollständigen `deer.json` beeinflussen sie dennoch
`hostInputSha256`.

Beide Assetvarianten stehen im selben `assets`-Array:

```json
{
  "assets": [
    {
      "id": "asset_foundation_note",
      "path": "assets/custom/foundation-note-3b50ea522803.png",
      "mediaType": "image/png",
      "source": {
        "license": "Dungeon project asset",
        "attribution": "Dungeon contributors"
      }
    },
    {
      "id": "asset_puzzle_piece",
      "path": "items/puzzle-piece.png",
      "mediaType": "image/png",
      "source": {
        "license": "CC0 1.0",
        "attribution": "Dungeon contributors"
      }
    }
  ]
}
```

Der Pfad bestimmt die Variante allein durch sein Präfix.
`assets/custom/<name>-<hashsuffix>.<ext>` bezeichnet eine Datei im
DEER-Projekt. Jeder andere Pfad, zum Beispiel `emotes/emote_cloud.png`, wird als
Referenz auf ein bereits in der Spiel-JAR enthaltenes Asset behandelt. Dafür
wird weder eine Projektdatei noch ein künstliches `assets/bundled/`-Verzeichnis
angelegt.

Eine Asset-Resource referenziert das Asset über `assetId`. Nicht referenzierte
Assets sind gültig, erzeugen aber eine Warnung.

Für Custom-Assets gelten die Datei- und Sicherheitsregeln:

- keine absoluten, Drive-, UNC- oder URL-Pfade;
- keine Backslashes, leeren, `.`- oder `..`-Segmente;
- nur Forward-Slashes und kein führender Slash;
- nur PNG oder JPEG und eine zur Dateiendung passende `mediaType`;
- sie liegen als genau eine flache Datei unter `assets/custom/`, sind keine
  Symlinks und enden im Dateistamm mit `-` und den ersten zwölf lowercase
  Hex-Zeichen ihres SHA-256-Inhaltshashes.

Für einen gebündelten Pfad gilt stattdessen nur, dass er exakt und
case-sensitive in der gemergten `internal_assets.txt` der Spiel-JAR vorkommen
muss. Der Runner leitet den `mediaType` nicht aus dem Pfad ab. Das Schema prüft
für beide Varianten nur, dass `path` nicht leer ist; die Prefix-Klassifikation
und die jeweils passende Assetprüfung übernimmt der Runner.

Im Spielerfluss werden nur Custom-Assets aus dem eingebetteten Projekt
materialisiert, vollständig geprüft und vor `InitialWorldReady` gebunden.
Gebündelte Assets werden über ihren unveränderten internen Pfad vom normalen
Dungeon-Assetloader geladen. Assetbytes werden nicht über das Netzwerk
übertragen.

## 9. Deterministische Ableitung und Verteilung

Der Runner verwendet Rätsel- und Surface-IDs als gemeinsame Anker für
Reihenfolge, Platzierungen und Runtime-Interaktionen. Er erzeugt generische
Informationsquellen-, Collection- und Code-Interaktionen und übernimmt die
End-Surface-Identität authentisch in Tür und Ausgang.

Für die Präsentations- und Single-Room-Platzierungsreihenfolge sortiert der
Runner Rätsel zuerst nach der längsten Vorgängerdistanz vom Start, dann nach
der stabilen authorierten Rätsel-ID und zuletzt nach der Knoten-ID. Diese
abgeleitete Ordnung ist reine Layoutmetadaten: Sie ergänzt, entfernt oder
verkürzt keine Progressionskante. Gleiche validierte Topologie und gleicher
Seed ergeben unabhängig von Node-, Edge- oder Riddle-Array-Reihenfolge dasselbe
Layout.

Host und Clients verwenden die vollständige DEER-Konfiguration mit Seed,
Lösungen, Texten und Assetreferenzen sowie die verifizierten Custom-Assets als
lokale Ableitungseingabe. V0.4 verspricht daher keine lokale
Antwortgeheimhaltung.

Der Host bleibt alleinige Autorität für Rätselzustände, akzeptierten
Inputfortschritt, Prüfungen, Hinweisfreigabe, Timer, Tür und terminales
Ergebnis.
Diese gemeinsamen Zustände sind monoton: Ein Rätsel fällt nicht in einen
früheren Zustand zurück, ein erfüllter Input wird nicht unerfüllt und eine
freigegebene Resource oder ein Hinweis wird nicht wieder verborgen. Im initialen
Entity-Stream wird lediglich der vollständige `hostInputSha256` übertragen,
damit ein Client ein abweichendes lokales Projekt vor `InitialWorldReady`
ablehnt. Dynamische Snapshots enthalten bei Keypads nur eingegebene Ziffern und
den serverautoritativen Entsperrstatus; statische Keypaddaten stammen aus der
lokalen vollständigen Definition. Die Dungeon-`PROTOCOL_VERSION` trägt die
Wire-Kompatibilität.
Details stehen im
[`runner-runtime-contract.md`](runner-runtime-contract.md).

## 10. Validierung

JSON Schema prüft Form, geschlossene Varianten, Pflichtfelder, Enums und lokale
Werte. Dazu gehören insbesondere:

- exakt ein Start- und ein Endknoten;
- eine nicht leere `failureText`-Seitenfolge bei hartem Zeitlimit;
- mindestens eine Resource in jeder Informationsquelle;
- mindestens einen Input in jedem Rätsel;
- vollständige Lernziel-, Debriefing-, Zielgruppen-, Schätzungs- und
  Provenienzfelder;
- geschlossene Informationsquellen-, Numeric- und Collection-Varianten.

Die semantische Prüfung ist auf dokumentübergreifende Regeln begrenzt:

- global eindeutige aktuell interpretierte Laufzeit-IDs einschließlich
  Informationsquellen und Inputs sowie existierende Referenzen auf Surfaces,
  Graphknoten, Rätsel und Assets;
- `session.playerCount.min <= session.playerCount.max`;
- Graphreichweite, Azyklizität, Rätsel-Bijektion und die mandatory
  AND-Abhängigkeiten;
- unbekannte oder inkompatible Surface-Referenzen;
- genau eine World- und Door-Surface;
- genau eine Informationsquelle je Container-Surface und genau ein Input je
  Keypad-Surface;
- existierende rätseleigene `informationSourceId` jedes Collection-Inputs;
- eine Door-Surface am Endknoten.

Damit kann kein Input vor Aktivierung seines Rätsels wirksamen Teilfortschritt
erzeugen und kein Rätsel einen eigenen Progressionsvorgänger umgehen. Die
statische Prüfung lehnt Zyklen, unerreichbare Rätsel, tote Erfolgswege und
Selbstabhängigkeiten ab. Räumliche Erreichbarkeit wird erst zusammen mit einem
späteren Raumgraphen validiert; sie wird im aktuellen Single-Room-Vertrag nicht
vorweggenommen.

Es gibt ausdrücklich keine semantischen Prüfungen von Lernziel-IDs sowie keine
Learning-Objective-, Debriefing-, Difficulty-, Estimated-Minutes- oder
Provenienzprüfungen. Diese Authoring-Texte und Felder bleiben dennoch
vollständig Bestandteil der kanonischen DEER-Daten und beeinflussen deshalb
`hostInputSha256`.

Die Assetprüfung kontrolliert davon getrennt die portablen Pfade und
Medientypen. Bei Custom-Assets prüft sie zusätzlich Symlinks, Dateiexistenz,
Inhalt und SHA-256-Dateinamensuffix; bei gebündelten Assets die exakte
Mitgliedschaft in der internen Assetliste. Sie läuft zwingend vor der Bildung
von `hostInputSha256`. Die Machbarkeitsprüfung begrenzt Rätsel, Resources und
Hinweise.

Sehr lange spielergerichtete Texte, sehr lange Hinweise und deklarierte, aber
nicht verwendete Assets erzeugen Warnungen. Ein Validierungsversuch verändert
weder Projekt noch Checkout.
