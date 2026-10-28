# Wizard Runner Runtime and Bootstrap Contract V0.5

Status: kanonischer implementierter Runner-/Runtime-Contract

Scope: deterministische In-Memory-Ableitung und Multiplayer-Ausführung des
generischen Runners

## Produktgrenze

Das DEER-Projekt besteht logisch aus dieser Packaging-Eingabe:

```text
<project>/
  deer.json
  assets/custom/...  # nur bei eigenen Bildern
```

Im browserbasierten Authoring-Flow materialisiert der Host diese Eingabe nur
temporär. Nach erfolgreichem Packaging lädt der Browser die Ausgabe getrennt
davon herunter:

```text
<bereinigter Spieltitel>-WizardRoom.jar
```

Der Runner validiert dieses Projekt und leitet daraus vor dem Serverstart einen
vollständigen Foundation-Raum im Speicher ab. Er erzeugt keinen Java-Code, kein
projektspezifisches Modul, keine `.level`-Datei, kein Buildskript und kein
Room-ZIP.

Der gemeinsame Java-Packager erzeugt aus der generischen
`WizardRoomTemplate.jar` eine projektspezifische `WizardRoom.jar`, die
die nach erfolgreicher Validierung erneut gelesenen und durch `rawDeerSha256`
abgesicherten exakten `deer.json`-Bytes, die referenzierten und verifizierten
Custom-Dateien unter `assets/custom/`, den zugehörigen `files.list`-Index,
Runner, Engine und benötigte Runtime-Ressourcen enthält. Zusätzliche
unreferenzierte Dateien unmittelbar unter `assets/custom/` werden nicht
paketiert und erzeugen Warnungen, solange die unterstützte Höchstzahl an
Verzeichniseinträgen nicht überschritten wird. Eine Überschreitung ist ein
blockierender Kapazitätsfehler. Referenzierte Spielbibliothek-Bilder sind bereits
als Basisassets in derselben JAR vorhanden und werden nicht kopiert. Dieselbe
vollständige JAR wird an Host und alle weiteren Spielenden verteilt.

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

Die Authoring-Integration bindet `ProjectValidationService`,
`ProjectValidationReport` und `RoomDeriver` direkt als Java-Bibliothek an.
Prüfungen laufen in einem temporären Projekt und sind read-only. Für das
Packaging liest die Authoring-Integration den aktuellen gespeicherten
Kandidaten, validiert ihn erneut und verlangt ein aktuelles gültiges Ergebnis.
Warnungen blockieren nicht. Anschließend paketiert sie ihn mit derselben
`WizardRoomPackager`-Implementierung wie der Gradle-Entwickler-/CI-Pfad und
liefert die JAR als Browserdownload aus. Auf dem Hostsystem werden dafür weder
Gradle noch Node benötigt. Bei einem Packaging-Fehler wiederholt die UI den
Package-Aufruf mit dem aktuellen Draft.

Der Authoring-Host bindet fest an `127.0.0.1:27777`. Er bewahrt keine Drafts,
Uploads, JARs oder Ready-Zustände dauerhaft auf. Seine API beschränkt
sich auf Status, Validierung und Packaging. Nur ein erfolgreicher JAR-Download
in der aktuellen UI-Sitzung markiert das Spiel als bereit; ein Reload stellt
diesen Zustand nicht wieder her.

`wizard/start_wizard_dev.cmd` unter Windows und `wizard/start_wizard_dev.sh`
unter Linux bauen und starten den Host nur für die Entwicklung. Der aktuelle
Entwickler- und Spieler-JAR-Fluss benötigt Java 25; zum Bauen der eingebetteten
UI benötigen die Entwicklungslauncher zusätzlich Node.js 22. Eine
Zielgruppen-`.exe`, beispielsweise über `jpackage`, mit gebündelter Java-Runtime
sowie ein Installer gehören nicht zu diesem Runtime-Vertrag.

Auch `max=1` verwendet im Spielerfluss einen echten Hostprozess und einen
getrennten Join-Client.

## Deterministische Ableitung

Aus gültigen kanonischen DEER-Daten und den verifizierten Custom-Assetbytes
entstehen:

1. eine vollständige `FoundationRoom`, die jeder Teilnehmer lokal ableitet;
2. die `RoomDefinition` mit `session.playerCount.min` und allen Slots von eins
   bis `session.playerCount.max`;
3. das generierte `RoomLevel` mit Räumen, Fluren, Türen, Dekoration,
   benannten Stationspunkten und einem gemeinsamen Startpunkt (siehe
   [Levelgenerator](#levelgenerator));
4. die vollständige Präsentation und die verifizierten Custom-Assets.

Gleiche kanonische DEER-Daten erzeugen unabhängig von JSON-Formatierung und
Property-Reihenfolge denselben `hostInputSha256`. Gleiche vollständige
DEER-Daten und gleiche verifizierte Custom-Assetbytes erzeugen mit derselben
Spieler-JAR dieselbe vollständige `FoundationRoom` einschließlich
Raumdefinition, generiertem Level, Präsentation und gemeinsamem
Level-Startpunkt.
Netzwerkzustand, Verbindungsreihenfolge und Laufzeit-Timestamps fließen nicht
in diese Projektidentität ein.

Der Host stellt das fertig abgeleitete Level vor dem Öffnen des Servers als
aktuelles Dungeon-Level bereit und erzeugt dessen Dekorationen als normale
Entities. Es gibt kein Platzhalterlevel, keine
level-freie Lobby, kein nutzergesteuertes Ready/Unready und keinen
Roster-Freeze.

Vor dem Tracking-Start übernimmt der Host die optionale
`metadata.operatorEmail` aus dem eingebetteten Projekt. Fehlt sie, verwendet
die Tracking-Konfiguration den im Quellcode definierten Standardwert.

## Bootstrap und Assets

Jeder Teilnehmer leitet aus seinem vollständig lokal validierten Projekt, also
der vollständigen `deer.json` plus den verifizierten Custom-Assets, dieselbe
`FoundationRoom` ab. Der Host überträgt daher kein zweites Raumabbild.
Das einzige Foundation-spezifische Projekt- und Bootstrap-Metadatum ist der
vollständige `hostInputSha256`. Im reservierten Metadata-Key
`foundation.bootstrap` steht direkt der kleingeschriebene 64-stellige
SHA-256-String. Die Dungeon-`PROTOCOL_VERSION` trägt die Wire-Kompatibilität.
Der generische Dungeon-Handshake enthält daneben Tracking-Daten: `ConnectAck`
liefert die `trackingRoomId` des Hosts, und `InitialWorldReady` meldet mit
`roomPlayedBefore`, ob der Client diesen Raum laut seiner lokalen Historie
bereits gespielt hat. Eine leere `trackingRoomId` bedeutet, dass Tracking für
den Raum deaktiviert ist.

Der Client vergleicht den Hash vor `InitialWorldReady` mit SHA-256 über seine
vollständige RFC-8785-kanonisierte lokale `deer.json`. Erst nach erfolgreicher
Schema-, Semantik- und Assetvalidierung wird dieser Hash gebildet. Anschließend
bindet der Client ausschließlich die lokal validierten Custom-Assets.
Gebündelte Assetpfade übernimmt die Präsentation unverändert und lädt sie über
den normalen Dungeon-Assetloader.

Der Identitätsmarker wird genau einmal im initialen Entity-Stream übertragen.
Der Client puffert gewöhnliche initiale Spawns bis zur erfolgreichen Prüfung
und Assetbindung und gibt sie danach in ursprünglicher Reihenfolge frei. Bei
einem Reconnect muss dieselbe Projektidentität verwendet werden. Der Server
sendet die `trackingRoomId` erneut, und der Client ermittelt
`roomPlayedBefore` erneut aus seiner lokalen Historie. Beide Tracking-Felder
beziehen sich auf einen Raum, nicht auf eine sitzungsübergreifende
Spieleridentität; sie erzeugen oder ersetzen keine solche Identität. Binäre
Assets werden nicht übertragen; V0.5 bietet keinen Assetdownload.

## Levelgenerator

Der Runner baut das Level aus mitgelieferten Vorlagen des Themes
`scenario.themeId`. Die Vorlagen sind `.level`-Dateien im JSON-Levelformat des
Level-Editors und liegen in der Spieler-JAR unter
`wizard/room-templates/<themeId>/`. Der Runner liest sie nur; er schreibt keine
`.level`-Datei. Für `default` gibt es drei Raumgrößen mit je zwei Varianten,
drei Flurstücke und eine Ausgangsnische.

Benannte Punkte einer Vorlage sind Slots. `keypad_N`, `terminal_N`,
`container_N` (Informationsquellen), `chest_N` und `item_N` (Zuordnungen) sowie
`hint_N` nehmen Stationen auf; `N` legt die Füllreihenfolge fest.
`door_<north|east|south|west>_N` markiert eine Außenwandkachel, an der ein
Nachbarstück anschließen kann. Ein Slot liegt auf Boden, auf einem Möbelstück
oder an der Wand. Auch wenn alle Slots belegt sind, müssen jeder Slot, jeder
Türeingang und der Startpunkt der Vorlage über freien Boden verbunden bleiben;
Kollisionsflächen der Dekoration zählen dabei als blockiert. Raumvorlagen
besitzen mindestens einen Hinweis-Slot, Flurstücke mindestens zwei Tür-Slots
und keine Stationen, die Ausgangsnische genau eine Exit-Kachel.

Jedes Rätsel erhält einen Hauptraum. Gewählt wird eine Vorlage, deren Slots alle
Stationen und den Hinweis aufnehmen und die genug Tür-Slots für Eingang und
Graphnachfolger hat; unter passenden Vorlagen bevorzugt der Seed die bisher
seltener genutzten. Passt keine Vorlage, füllt der Runner den Hauptraum zuerst
und verteilt die übrigen Stationen auf Nebenräume. Die Kisten einer Zuordnung
bleiben in einem Raum. Nebenräume hängen am Hauptraum ihres Rätsels.

Die Spielenden starten im Hauptraum eines Rätsels, dessen einzige Vorgänger
der Startknoten ist; bei mehreren wählt der Seed. Jeder weitere Raum grenzt an
genau einen Raum eines direkten Vorgängerrätsels, bei Rätseln ohne
Rätselvorgänger an einen bereits gelegten Startraum. Die Verbindung führt
direkt durch eine gemeinsame Wand oder durch bis zu zwei Flurstücke. Flure
gehören zum vorderen Raum, öffnen mit ihm und können weitere Räume aufnehmen.
Die Tür eines Raums liegt an seinem Eingang und gehört seinem Rätsel. Die
Ausgangsnische mit der Exit-Kachel sitzt hinter einer Außenwandtür im
Hauptraum eines direkten Endvorgängers.

Findet ein Versuch keine kollisionsfreie Anordnung, versucht der Runner
deterministisch aus `seed` abgeleitete Seeds, insgesamt höchstens 32. Danach
meldet die Produktionsvalidierung `LAYOUT_UNPLACEABLE` am Spielablauf.
`FoundationRoom` prüft jedes Ergebnis: Mit den Türen eines Rätsels, seiner
Vorgänger und aller ab Sessionstart aktiven Rätsel erreicht der Startpunkt
jede Station dieses Rätsels, und der Ausgang ist nur durch die Ausgangstür
erreichbar.

## Multiplayer-Lebenszyklus

`session.playerCount.max` ist die Hostkapazität und die Zahl der deterministisch
vorbereiteten Dungeon-Identitäten und logischen Authority-Slots. Alle Spieler
verwenden den gemeinsamen Startpunkt des Levels. Eine einmal vergebene Identität
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

`session.playerCount.min` ist die technische Startschwelle und die Mindestzahl
spielbereiter Teilnehmer für die gemeinsame Spieluhr.
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
Mitgliedschaft, Slotreservierung oder eigene Reconnect-Zustandsmaschine ein.
Der headless Host ist kein Spieler.

Raumtimer und Tracking verwenden dieselbe serverautoritative Spieluhr. Sie
läuft ab der initialen Intro-Freigabe und pausiert, solange weniger als `min`
Teilnehmer spielbereit sind oder alle das Pausenmenü geöffnet haben.
Hard-Timeout und erfolgreicher Ausgang stoppen sie vor den Ergebnis-Seiten; das
Tracking schließt den Lauf dann als `FAILED` beziehungsweise `COMPLETED` ab.

Beim terminalen Ergebnis zeigt jeder technisch spielbereite Client die
authorierten `successText`- beziehungsweise `failureText`-Seiten in
Array-Reihenfolge als Black-Fade-Sequenz; eine noch nicht abgeschlossene
Intro-Sequenz wird dabei nicht fortgesetzt. Sobald der erste Client die
vollständige terminale Sequenz durchgeklickt hat, beendet der Host die Sitzung.
Dafür gibt es weder einen festen Grace-Timer noch ein zusätzliches ACK- oder
Reconnect-Protokoll.

## Serverautoritative Runtime

Die `RoomLevel` materialisiert das generierte Level mit Rätseltüren,
Ausgangstür, Ausgang, Dekoration und gemeinsamem Startpunkt sowie benannten
Punkten für Informationsquellen-, Zahlencode-, Auswahlfrage-, Zuordnungs- und
Hinweisinteraktionen. Jede
surface-gebundene Informationsquelle und jeder Numeric-, Choice- oder
Assignment-Input verwendet in Placement,
Präsentation und Authority dieselbe authorierte `surfaceId`; ein
Collection-Input verwendet die Surface seiner Quelle. Hinweispositionen werden
intern aus der `riddleId` abgeleitet und sind keine authorierten Surfaces. Die
End-Surface-ID wird authentisch als Door-Identität übernommen. Nach dem
Serverstart erzeugt der Host an diesen Punkten normale Entities mit
`PositionComponent`, `DrawComponent` und serverseitiger
`InteractionComponent`. Clients erhalten deren Darstellung über den normalen
Entity-Stream. Interaktionen verwenden das gewöhnliche Dungeon-`INTERACT`;
Positionsprüfung und Callback-Ausführung erfolgen im Server-ECS. Resource-,
Hinweis-, Keypad-, Auswahl- und Ergebnisdialoge werden serverseitig erzeugt und über den
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

Tracking zeichnet `PUZZLE_STARTED` beim Wechsel zu `ACTIVE` auf. Jede
ausgewertete Eingabe wird als Versuch mit `answerKind` und Antwort
aufgezeichnet: `numeric-code` mit den eingegebenen Ziffern, `choice` mit den
gewählten Options-IDs als sortiertem JSON-Array und `assignment` mit der
Kisten-ID jedes einsortierten Items als JSON-Objekt. Eine Zuordnung erzeugt
genau einen richtigen Versuch, sobald der Host sie annimmt; Zwischenstände beim
Sortieren sind keine Versuche.

Informationsquellen liegen im Raum ihres Rätsels und sind lesbar, sobald dessen
Tür offen ist. Informationsquellen, Kisten und Antwort-Items eines gesperrten
Rätsels reagieren nicht auf Interaktionen, auch nicht durch eine Wand hindurch.
Möbel und Kisten sind serverseitig fest. Neue Hinweise werden nur für aktive Rätsel freigegeben. Vor jeder
Freigabe zeigt die Runtime die spielerfreundliche Bedeutung der `severity` des
nächsten Hinweises und verlangt eine ausdrückliche Bestätigung. Das gilt für
`orientation`, `approach` und `solution`. Ein Abbruch verändert keinen
Authority-Zustand und rückt nicht zum nächsten Hinweis vor. Erst nach der
Bestätigung gibt der Host den nächsten Hinweis frei. Bereits freigegebene
Hinweise bleiben lesbar. Der gemeinsame Zustand ist monoton und fällt weder bei
Rätseln noch bei Inputs, Resources oder Hinweisen in einen früheren Zustand
zurück.

Die Tür zu einem Rätselraum und seinen Nebenräumen öffnet serverautoritativ,
sobald das Rätsel `ACTIVE` wird; danach bleibt sie offen. Diese Rätseltüren
sind keine authorierten Surfaces. Der Endknoten besitzt allein seine
Door-Surface, die Ausgangstür. Sobald alle direkten Vorgängerrätsel gelöst
sind, wird das Ende erreicht und genau diese Tür serverautoritativ geöffnet.
Es gibt weder eine zweite globale „alle Rätsel gelöst“-Türregel noch frei
konfigurierbare Rätseloutputs. Clients erhalten den Zustand aller Türen über
den normalen Dungeon-Snapshot.

Der aktive Vertrag kennt nur `world`, `container`, `keypad`, `terminal`,
`assignment` und `door`. Weitere Surface-Arten oder mehrere Rätselbindungen an eine Surface
erfordern eine explizite Profilerweiterung; die Runtime erfindet dafür keinen
generischen Device-Typ.

Ein Client sendet bei einer Informationsquellen-Interaktion nur den normalen
Zielpunkt. Der Server löst dort die Station anhand seiner ECS-Welt auf,
verwendet die authentifizierte Spieleridentität und zeigt die vorhandenen
Inhalte. Eine vollständig gelesene Quelle kann erneut geöffnet werden. Dabei
wertet der Server dieselbe Informationsquellen-Interaktion für einen
Collection-Input aus und erfüllt ihn genau dann, wenn das Rätsel `ACTIVE` ist.
Frühere Interaktionen werden weder angerechnet noch bei Aktivierung automatisch
übernommen. Zahlencodes werden über das vorhandene Dungeon-Keypad eingegeben,
als vollständige Dialogantwort übertragen und ausschließlich serverseitig
geprüft. Auswahlfragen öffnen an ihrer Terminal-Station den vorhandenen
Multiple-Choice-Dialog. Der Client überträgt nur die IDs der bestätigten
Optionen; der Host vergleicht sie mit `correctOptionIds`. Zuordnungen bestehen
aus gewöhnlichen Truhen und aufhebbaren Antwort-Items, die ihre Item-ID in den
Item-Daten tragen. Spieler verschieben sie über den vorhandenen
Inventardialog. Der Host prüft nach jeder Änderung der Kisteninhalte und reicht
eine richtige Zuordnung erneut ein, bis die Authority sie annimmt. Getrackt wird
der Versuch für den Spieler, der zuletzt ein Item in eine der Kisten gelegt oder
daraus genommen hat. Ist er nicht mehr spielbereit, reicht ein anderer
spielbereiter Spieler bei der Authority ein; der Versuch bleibt dem Beweger
zugeordnet.
Verlässt ein Spieler die Sitzung, legt der Server sein Inventar
nach 30 Sekunden ohne Wiederverbindung im Raum ab.

## Fehler und Abnahme

Validierungs- und Ableitungsfehler verwenden im Authoring-Adapter und internen
Packaging-Schritt den gemeinsamen `ProjectValidationReport`. Bootstrapfehler
der Spieler-JAR werden als klare Startfehler ausgegeben. Vor erfolgreicher
Validierung startet weder Authority noch Server. Ein Fehler erzeugt keine
Teilausgabe.

Die Abnahme schützt insbesondere:

- wiederholbare Hash-, Raum-, Layout- und gemeinsame Level-Startpunktdaten;
- erreichbare Stationen und Ausgang in jedem generierten Level;
- Ablehnung unterschiedlicher lokaler DEER-Projekte vor `InitialWorldReady`;
- Main-Menü-, Host-/Join-Verantwortlichkeiten, Custom-Asset-Integrität und
  direkte gebündelte Assetreferenzen;
- die `min`-Startschwelle und die `max`-Serverkapazität;
- normales Cleanup der Host- und Join-Runtimes sowie temporärer Projektdateien;
- Input- und Checkout-Immutabilität.
