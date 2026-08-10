# Wizard Runner Runtime and Bootstrap Contract V0.4

Status: kanonischer implementierter Runner-/Runtime-Contract

Scope: deterministische In-Memory-Ableitung und Multiplayer-Ausführung des
generischen Runners

## Produktgrenze

Der Wizard erstellt genau ein DEER-Projekt:

```text
<project>/
  deer.json
  assets/custom/...  # nur bei eigenen Bildern
```

Der Runner validiert dieses Projekt und leitet daraus vor dem Serverstart einen
vollständigen Foundation-Raum im Speicher ab. Er erzeugt keinen Java-Code, kein
projektspezifisches Modul, keine `.level`-Datei, kein Buildskript und kein
Room-ZIP. Projektordner und Checkout bleiben unverändert.

Der Gradle-Packager erzeugt eine projektspezifische `WizardRoom.jar`, die
`deer.json`, vorhandene Dateien unter `assets/custom/`, Runner, Engine und
benötigte Runtime-Ressourcen enthält. Referenzierte Spielbibliothek-Bilder sind
bereits als Basisassets in derselben JAR vorhanden und werden nicht kopiert.
Dieselbe vollständige JAR wird an Host und alle weiteren Spielenden verteilt.

## Spielerstart und Main-Menü

```text
java -jar WizardRoom.jar
```

`WizardRoomApplication` benötigt Java 25. Ohne Argumente materialisiert und
validiert sie das eingebettete Projekt im disposablen Runtime-Verzeichnis und
öffnet das bestehende Host-/Join-Menü. Beim Prozessende werden die temporären
Projektdateien entfernt.

Der Host-Knopf startet dieselbe JAR intern mit `--server` als verwalteten
headless Childprozess. Nach dessen Bereitschaft verbindet sich der Host-Client
automatisch mit `127.0.0.1` auf Port `7777`. Beim Beenden des Host-Clients wird
auch der verwaltete Server beendet. Es gibt weder ein separates
Serverartefakt noch einen benutzersichtbaren zusätzlichen Serverstarter.

Der Join-Knopf fragt Spielername, Hostadresse und Port ab und verwendet die
Assetreferenzen des eingebetteten Projekts. `--server` ist ausschließlich ein
interner Startmodus der Spieler-JAR. Andere Argumente an deren Main-Class
werden mit einem klaren Startfehler abgelehnt.

## Produktionsvalidierung und Packaging

Die Authoring-Integration bindet `ProjectValidationPipeline` und
`ProjectValidationReport` direkt als Java-Bibliothek an. Es gibt keine
öffentliche Runner-CLI oder Runner-Distribution. Der Gradle-Packager verwendet
einen kleinen internen Prozesseinstieg, der ausschließlich den übergebenen
Projektordner validiert, seine Ableitbarkeit mit `RoomDeriver` prüft und den
kanonischen Validierungsreport ausgibt. Er bietet keine Subcommands und startet
keine Multiplayer-Runtime.

Auch `max=1` verwendet im Spielerfluss einen echten Hostprozess und einen
getrennten Join-Client.

## Deterministische Ableitung

Aus gültigen kanonischen DEER-Daten und den verifizierten Custom-Assetbytes
entstehen:

1. eine vollständige `FoundationRoom`, die jeder Teilnehmer lokal ableitet;
2. die `RoomDefinition` mit `session.playerCount.min` und allen Slots von eins
   bis `session.playerCount.max`;
3. das generische `RoomLevel` mit Geometrie, benannten Stationspunkten und
   einem gemeinsamen Startpunkt;
4. die vollständige Präsentation und die verifizierten Custom-Assets.

Gleiche kanonische DEER-Daten erzeugen unabhängig von JSON-Formatierung und
Property-Reihenfolge denselben `hostInputSha256`. Gleiche vollständige
DEER-Daten und gleiche verifizierte Custom-Assetbytes erzeugen dieselbe
vollständige `FoundationRoom` einschließlich Raumdefinition, Layoutplan,
Präsentation und gemeinsamem Level-Startpunkt.
Netzwerkzustand, Verbindungsreihenfolge und Laufzeit-Timestamps fließen nicht
in diese Projektidentität ein.

Der Host stellt das fertig abgeleitete Level vor dem Öffnen des Servers als
aktuelles Dungeon-Level bereit. Es gibt kein Platzhalterlevel, keine
level-freie Lobby, kein nutzergesteuertes Ready/Unready und keinen
Roster-Freeze.

## Bootstrap und Assets

Jeder Teilnehmer leitet aus seinem vollständig lokal validierten Projekt, also
der vollständigen `deer.json` plus den verifizierten Custom-Assets, dieselbe
`FoundationRoom` ab. Der Host überträgt daher kein zweites Raumabbild.
Das einzige Foundation-Bootstrap-Metadatum ist der vollständige
`hostInputSha256`: Im reservierten Metadata-Key `foundation.bootstrap` steht
direkt der kleingeschriebene 64-stellige SHA-256-String. Die
Dungeon-`PROTOCOL_VERSION` trägt die Wire-Kompatibilität. Der Client vergleicht
den Hash vor `InitialWorldReady` mit SHA-256 über seine vollständige
RFC-8785-kanonisierte lokale `deer.json`. Erst nach erfolgreicher Schema-,
Semantik- und Assetvalidierung wird dieser Hash gebildet. Anschließend bindet
der Client ausschließlich die lokal validierten Custom-Assets. Gebündelte
Assetpfade übernimmt die Präsentation unverändert und lädt sie über den
normalen Dungeon-Assetloader.

Der Identitätsmarker wird genau einmal im initialen Entity-Stream übertragen.
Der Client puffert gewöhnliche initiale Spawns bis zur erfolgreichen Prüfung
und Assetbindung und gibt sie danach in ursprünglicher Reihenfolge frei. Bei
einem Reconnect muss dieselbe Projektidentität verwendet werden. Binäre Assets
werden nicht übertragen; V0.4 bietet keinen Assetdownload.

## Multiplayer-Lebenszyklus

`session.playerCount.max` ist die Hostkapazität und die Zahl der deterministisch
vorbereiteten Dungeon-Identitäten und logischen Authority-Slots. Alle Spieler
verwenden den gemeinsamen Startpunkt des Raums. Eine einmal vergebene Identität
bleibt bis zum Ende des Hostprozesses reserviert. Nachdem alle Identitäten
mindestens einmal vergeben wurden, kann nur derselbe Client seine vorhandene
Identität im normalen Dungeon-Best-Effort-Reconnect wieder aufnehmen; ein neuer
Ersatzclient wird abgewiesen.

`scenario.themeId` bestimmt den geordneten Pool spielbarer
`CharacterClass`-Skins. Für `default` lautet er `THE_LAST_HOUR_ROGUE`, danach
`THE_LAST_HOUR_CHAR03`. Offizielle Wizard-Clients fordern keine Klasse an; der
Server weist sie in Pool-Reihenfolge zyklisch zu. Damit erhalten die ersten
beiden Identitäten verschiedene Skins, und erst nach Erschöpfung des Pools wird
ein Skin erneut vergeben. Ein Reconnect verwendet denselben `ClientState` und
dieselbe `CharacterClass`. Auch während einer vorübergehenden Trennung bleibt
der Skin einer reconnect-fähigen reservierten Identität zugeordnet.

`session.playerCount.min` ist ausschließlich die technische Startschwelle.
Sobald mindestens so viele angenommene Clients ihre normale initiale Welt
bestätigt haben, sehen alle aktuell technisch spielbereiten Clients zuerst die
Seiten aus `scenario.introText` in Array-Reihenfolge und danach
`scenario.mission` als hervorgehobene letzte Intro-Seite. Initiale Authority,
Timer und Gameplay beginnen serverautoritativ erst, wenn alle diese Sequenz
abgeschlossen haben. Später beitretende Clients werden erst nach ihrer eigenen
Intro-Sequenz spielbereit. Solange noch nie vergebene Identitäten vorhanden
sind, dürfen weitere Clients bis `max` beitreten. Es gibt keinen separaten
Lobbyzustand und keine benutzergesteuerte Startfreigabe.

Verbindung, Spielererzeugung, Snapshot-Synchronisation und Best-Effort-Reconnect
verwenden die normalen Dungeon-Verträge. Foundation führt keine zweite
Mitgliedschaft, Slotreservierung, Disconnect-Pause oder eigene
Reconnect-Zustandsmaschine ein. Der headless Host ist kein Spieler.

Beim terminalen Ergebnis zeigt jeder technisch spielbereite Client die
authorierten `successText`- beziehungsweise `failureText`-Seiten in
Array-Reihenfolge als Black-Fade-Sequenz; eine noch nicht abgeschlossene
Intro-Sequenz wird dabei nicht fortgesetzt. Sobald der erste Client die
vollständige terminale Sequenz durchgeklickt hat, beendet der Host die Sitzung.
Dafür gibt es weder einen festen Grace-Timer noch ein zusätzliches ACK- oder
Reconnect-Protokoll.

## Serverautoritative Runtime

Die `RoomLevel` materialisiert die Raumgeometrie mit gemeinsamer Tür, Ausgang
und gemeinsamem Startpunkt sowie benannten Punkten für Informationsquellen-,
Zahlencode- und Hinweisinteraktionen. Jede surface-gebundene
Informationsquelle und jeder Numeric-Input verwendet in Placement,
Präsentation und Authority dieselbe authorierte `surfaceId`; ein
Collection-Input verwendet die Surface seiner Quelle. Hinweispositionen werden
intern aus der `riddleId` abgeleitet und sind keine authorierten Surfaces. Die
End-Surface-ID wird authentisch als Door-Identität übernommen. Nach dem
Serverstart erzeugt der Host an diesen Punkten normale Entities mit
`PositionComponent`, `DrawComponent` und serverseitiger
`InteractionComponent`. Clients erhalten deren Darstellung über den normalen
Entity-Stream. Interaktionen verwenden das gewöhnliche Dungeon-`INTERACT`;
Positionsprüfung und Callback-Ausführung erfolgen im Server-ECS. Resource-,
Hinweis-, Keypad- und Ergebnisdialoge werden serverseitig erzeugt und über den
vorhandenen Dungeon-Dialogvertrag angezeigt und beantwortet. Rätselzustand,
Inputfortschritt, Hinweisfreigabe, Timer, Tür und terminales Ergebnis bleiben
serverautoritativ.

Die Authority setzt den `riddleGraph` unmittelbar als mandatory AND-DAG und
Interaktionsfreigabe um. Vor dem Sessionstart sind alle Rätsel `LOCKED`; danach
werden nur direkte Startnachfolger `ACTIVE`. Ein späteres Rätsel wird aktiv,
wenn alle direkten Vorgängerrätsel `SOLVED` sind. Mehrere gleichzeitig
berechtigte Rätsel werden stabil nach ihrer authorierten Rätsel-ID behandelt.
Der Runner rekonstruiert keine Progressionsabschnitte und ergänzt keine
Abhängigkeiten. Nur Inputs aktiver Rätsel werden
angenommen. Vorzeitige oder wiederholte Aktionen erzeugen keinen
Teilfortschritt. Sind alle Inputs eines Rätsels erfüllt, wechselt es atomar und
genau einmal zu `SOLVED`; dieser implizite Abschluss aktiviert gegebenenfalls
Graphnachfolger.

Informationsquellen mit Informationen oder Aufgabeninhalten bleiben unabhängig
davon lesbar und dürfen deshalb schon vor der Rätselaktivierung erreichbar
sein. Neue Hinweise werden nur für aktive Rätsel freigegeben. Vor jeder
Freigabe zeigt die Runtime die spielerfreundliche Bedeutung der `severity` des
nächsten Hinweises und verlangt eine ausdrückliche Bestätigung. Das gilt für
`orientation`, `approach` und `solution`. Ein Abbruch verändert keinen
Authority-Zustand und rückt nicht zum nächsten Hinweis vor. Erst nach der
Bestätigung gibt der Host den nächsten Hinweis frei. Bereits freigegebene
Hinweise bleiben lesbar. Der gemeinsame Zustand ist monoton und fällt weder bei
Rätseln noch bei Inputs, Resources oder Hinweisen in einen früheren Zustand
zurück.

Der Endknoten besitzt allein seine Door-Surface. Sobald alle direkten
Vorgängerrätsel gelöst sind, wird das Ende erreicht und genau diese Tür
serverautoritativ geöffnet. Es gibt weder eine zweite globale
„alle Rätsel gelöst“-Türregel noch frei konfigurierbare Rätseloutputs.

Der aktive Vertrag kennt nur `world`, `container`, `keypad` und `door`. Ein
späterer Computer als neue Surface-Art oder mehrere Rätselbindungen an eine
Surface erfordern eine explizite Profilerweiterung; die Runtime erfindet dafür
keinen generischen Device-Typ.

Ein Client sendet bei einer Informationsquellen-Interaktion nur den normalen
Zielpunkt. Der Server löst dort die Station anhand seiner ECS-Welt auf,
verwendet die authentifizierte Spieleridentität und zeigt die vorhandenen
Inhalte. Eine vollständig gelesene Quelle kann erneut geöffnet werden. Dabei
wertet der Server dieselbe Informationsquellen-Interaktion für einen
Collection-Input aus und erfüllt ihn genau dann, wenn das Rätsel `ACTIVE` ist.
Frühere Interaktionen werden weder angerechnet noch bei Aktivierung automatisch
übernommen. Zahlencodes werden über das vorhandene Dungeon-Keypad eingegeben,
als vollständige Dialogantwort übertragen und ausschließlich serverseitig
geprüft.

## Fehler und Abnahme

Validierungs- und Ableitungsfehler verwenden im Authoring-Adapter und internen
Packaging-Schritt den gemeinsamen `ProjectValidationReport`. Bootstrapfehler
der Spieler-JAR werden als klare Startfehler ausgegeben. Vor erfolgreicher
Validierung startet weder Authority noch Server. Ein Fehler erzeugt keine
Teilausgabe.

Die Abnahme schützt insbesondere:

- wiederholbare Hash-, Raum-, Layout- und gemeinsame Level-Startpunktdaten;
- Ablehnung unterschiedlicher lokaler DEER-Projekte vor `InitialWorldReady`;
- Main-Menü-, Host-/Join-Verantwortlichkeiten, Custom-Asset-Integrität und
  direkte gebündelte Assetreferenzen;
- die `min`-Startschwelle und die `max`-Serverkapazität;
- normales Cleanup der Host- und Join-Runtimes sowie temporärer Projektdateien;
- Input- und Checkout-Immutabilität.
