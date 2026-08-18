# Frontend Handoff V0.4

Status: M2-Authoring-Architektur, Runner und Packaging umgesetzt; Zielgruppen-Distribution noch offen

## Auftrag

Die Frontend-Umsetzung baut eine lokale Standalone-App mit Web-Oberfläche für
nicht-technische Lehrende. Sie führt einen unvollständigen privaten Entwurf,
prüft ihn und finalisiert daraus genau diesen öffentlichen Projektordner:

```text
<project>/
  deer.json
  assets/custom/...  # nur bei eigenen Bildern
  WizardRoom.jar
```

Die UI startet keine Spielruntime. Sie erzeugt weder Java-Code noch ein
Raummodul, Buildskripte oder ein ZIP. Der generische Java-Runner bleibt die
Autorität für Projektvalidierung und Runtimefähigkeit. Aus dem finalisierten
Projekt erzeugt die UI mit dem gemeinsamen Java-Packager eine
projektspezifische ausführbare `WizardRoom.jar` im Projektordner.

Der implementierte Foundation-Slice bildet **Informationsquellen**,
**Zahlencode**, verpflichtendes Entdecken und den gemeinsamen Ausgang ab.
Schema, Runner und UI setzen diesen Rätselvertrag um. Spielende starten Host
oder Join danach über das Main-Menü derselben JAR; ein separates Serverartefakt
ist nicht erforderlich. M2 ist noch keine releasefertige
Lehrer-Distribution: Eine Zielgruppen-`.exe` mit gebündelter Runtime bleibt
ein späterer Meilenstein.

## Rollen

- **Lehrende Person:** erstellt und überarbeitet den privaten Entwurf ohne JSON
  oder technische IDs, wählt den Zielordner, prüft, finalisiert und erzeugt die
  `WizardRoom.jar` direkt in der lokalen Wizard-Anwendung.
- **Technische Betreuung/Distribution:** stellt bis zur späteren `.exe` den
  Java-25-Entwicklungsstart bereit und verteilt dieselbe fertige JAR an alle
  Spielenden.
- **Spielende:** starten mit Java 25 das Main-Menü derselben JAR und wählen
  Host oder Join. Sie besitzen nicht den privaten UI-Entwurf, können aber die
  eingebettete `deer.json` einschließlich Seed und Lösungen auslesen.

## Verbindliche Lesereihenfolge

1. Dieses Dokument definiert Auftrag, Schichtgrenzen und Abnahme.
2. [`wizard-ui-flow-v0.md`](wizard-ui-flow-v0.md) definiert sichtbare Schritte,
   Zustände und Texte.
3. [`deer.schema.json`](deer.schema.json) ist der maschinenlesbare
   Ausgabevertrag; [`deer-json-spec.md`](deer-json-spec.md) erklärt die
   Semantik.
4. [`runner-project-format.md`](runner-project-format.md) definiert
   Finalisierung, Assets und das Validierungsinterface.
5. [`../../examples/foundation-v0.4/`](../../examples/foundation-v0.4/) ist
   das kanonische ausführbare Beispiel.

Multiplayer und Spielruntime sind kein Frontend-Auftrag. Ihre Grenze steht in
[`runner-runtime-contract.md`](runner-runtime-contract.md).

## Verantwortungsgrenze

M1 und M2 bezeichnen Implementierungsmeilensteine, keine Dateiformate. DEER
bleibt `0.4`, das private Draftformat `1`. Der produktive Pfad verwendet den
loopback-only Java-Host und dessen same-origin React-UI. LocalStorage für
Draft-Snapshots und IndexedDB für Uploadbytes bleiben ausschließlich der
Fallback beim direkten Vite-Entwicklungsstart.

| Schicht | Besitzt | Besitzt ausdrücklich nicht |
|---|---|---|
| Web-UI | sichtbaren Flow, privaten Draft, unmittelbare Feldhinweise, Vorschau | Projektdateisystem, Runner-Runtime, Multiplayer |
| Nativer Host-Adapter | Draft-Persistenz unter `%LOCALAPPDATA%\Dungeon Wizard`, Uploadbytes, Ordnerwahl, Produktionsvalidierung, atomare Finalisierung und Packaging | fachliche UI-Navigation, Foundation-Spielzustand |
| Java-Validierung und Runtime | normative Projektvalidierung, Ableitung und Host-/Join-Runtime | privaten Draft, UI-Zustand, Projektbearbeitung |
| Gemeinsamer Java-Packager | validiertes Projekt plus generische `WizardRoomTemplate.jar` als projektspezifische ausführbare JAR | privaten Draft, UI-Navigation, neue DEER-Semantik |

Der Frontend-Code darf lokale Feldhinweise für schnelle Rückmeldung berechnen.
Er darf aber keine zweite normative semantische Validierung etablieren.
`Spiel erstellen` verwendet immer das Ergebnis der produktiven
Runner-Validierung für exakt den Kandidaten, der geschrieben werden soll.

Die UI erfasst dabei den vollständigen Authoring-Vertrag: optionale
Beschreibung/Autor, Zielgruppe, Vorwissen, mindestens ein Lernziel,
Nachbesprechungsfragen, Schwierigkeit und Zeitschätzung je Rätsel, geordnete
Informationsquellen, mindestens eine Eingabe, geordnete optionale Hinweise mit
je einer verständlich benannten Offenlegungsstufe sowie Bildherkunft. Die UI
speichert die Auswahl intern als `severity=orientation`, `approach` oder
`solution`, zeigt aber weder Feldname noch Enum-Wert. Diese Angaben bleiben
verbindlicher Teil von `deer.json` und des
vollständigen Host-Input-Hash, unabhängig davon, welche Teile der aktuelle
Java-Runner in sein Laufzeitmodell übernimmt.

Eine separate Orte- oder Surface-Ansicht existiert nicht. Jede
Informationsquelle besitzt den sichtbaren Namen ihres **Fundorts**, jede
Zahleneingabe den Namen ihres **Geräts** und „Spiel-Ende“ den Namen des
**Ausgangs**. Die UI verwaltet daraus stabile Surfaces der geschlossenen Arten
`world`, `container`, `keypad` und `door` sowie die jeweiligen `surfaceId`-
Bindungen ab. Der technische Begriff „Surface“ und die IDs bleiben verborgen.
Ein Computer ist noch keine Surface-Art des aktiven Profils. Die UI bietet
weder ein Output-/Effektfeld noch OR-Regeln an; Progression und Ausgang gehören
allein dem frei bearbeiteten mandatory AND-Rätselgraphen. Die UI zeigt genau
einen geschützten Start- und Endknoten sowie genau einen Knoten je Rätsel. Eine
Kante bedeutet „wird nach dem Vorgänger verfügbar“; bei mehreren eingehenden
Kanten müssen ausnahmslos alle Vorgänger gelöst sein. Die UI verhindert
doppelte Kanten und Zyklen, bietet aber weder OR-, Bedingungs- noch
Optionalitätsregeln an. Unverbundene oder noch unvollständige Graphen dürfen im
privaten Draft gespeichert werden und werden vor der Finalisierung als lokale
Probleme angezeigt. Knoten und Kanten werden exakt in ihrer gespeicherten
Reihenfolge in den DEER-Kandidaten übernommen, nicht abgeleitet oder repariert.

## Draft- und Storage-Port

Das Draftformat ist privat und darf unvollständig sein. Es ist kein zweites
DEER-Format. Seine konkrete Serialisierung ist eine Frontend-Entscheidung,
muss aber eine eigene Versionskennung besitzen und mindestens erhalten:

- stabile Draft- und Projektidentität;
- eine nichtnegative, sichere Ganzzahl `revision` für optimistisches Speichern;
- alle sichtbaren Eingaben und stabil erzeugten Authoring-IDs;
- bei Uploads die Bildbytes, bei Spielbildern den internen Pfad sowie jeweils
  Anzeigename und Lizenzmetadaten;
- gewählten Projektordner, sofern vorhanden;
- den Seed eines bereits finalisierten Projekts;
- Zeitpunkt und Zustand der letzten Speicherung und Finalisierung.

Ein unbekanntes Draftformat wird verständlich abgelehnt und nicht teilweise
geladen. Draft v1 ist ein Clean Cut; eine Migration privater Browser-
Prototypdaten ist nicht vorgesehen.

Jeder vollständige Draft-Snapshot wird per Compare-and-set mit seiner
`revision` gespeichert und erhält bei Erfolg die nächste Revision. Auch wenn
während eines laufenden Saves weitere lokale Änderungen entstehen, übernimmt
die UI die bestätigte Revision in diese neuere lokale Fassung und sendet den
nächsten Snapshot erst danach. Ein Konflikt bleibt sichtbar; die geöffnete
Fassung wird weder überschrieben noch verworfen. Der Browser-Adapter bildet
denselben Vertrag innerhalb seines versionierten LocalStorage-Envelopes nach.
Sein Lesen, Vergleichen und Schreiben läuft als eine exklusive, tabübergreifende
Web-Lock-Operation. Fehlt die Web-Lock-API, lehnt der Entwicklungsadapter das
Speichern verständlich ab, statt einen unsicheren Fallback auszuführen.
Auch erfolgreiche Hostmutationen bei Finalisierung und Packaging erhöhen die
Revision. Die UI übernimmt diese bestätigte Revision samt Hostmetadaten direkt,
ohne dafür einen zweiten, inhaltlich unveränderten Autosave auszulösen. Nur
anschließend ergänzte private UI-Metadaten werden regulär per CAS gespeichert.

Der Storage-/Host-Port stellt der Web-UI diese logischen Operationen bereit;
konkrete Methodennamen dürfen dem verwendeten Stack folgen:

| Operation | Erfolg | Fehlergarantie |
|---|---|---|
| Drafts auflisten, anlegen, laden und speichern | vollständiger privater Snapshot | vorhandener Snapshot bleibt unverändert |
| Projektordner wählen | explizit vom Nutzer bestätigter nativer Ordner | Abbruch verändert keinen Draft |
| Spielbibliothek-Asset auswählen oder eigenes PNG/JPEG hochladen | interne Referenz oder geprüfte Uploadbytes, jeweils mit Anzeigename und Lizenzmetadaten | keine Teilübernahme |
| vollständigen Kandidaten prüfen | [`ProjectValidationReport`](project-validation-report.schema.json) der Produktionsvalidierung | Zielprojekt bleibt unverändert |
| Kandidaten finalisieren | neue Custom-Dateien zuerst, `deer.json` zuletzt atomar ersetzt | letzte gültige Finalisierung bleibt verwendbar |
| finalisiertes Projekt paketieren | `<project>/WizardRoom.jar` aus generischer Template-JAR | Finalisierung bleibt gültig; Packaging ist wiederholbar |

Der native Java-Host stellt alle Operationen hinter diesem Port bereit. Die UI
lädt die asynchrone Entwurfsliste als Startansicht und speichert Änderungen
automatisch. Ein beliebiger `deer.json`-Import oder Browser-ZIP-Export gehört
nicht zum Produktfluss.

Vor Produktionsprüfung und Finalisierung leert die UI ihre serielle Save-Queue
und verwendet ausschließlich den dadurch zurückgegebenen gespeicherten
Snapshot. `revision`, Kandidat, Uploadbindungen und der UI-interne
Kandidatenhash stammen damit aus derselben Fassung; Render-State wird für den
Request nicht erneut zusammengesetzt.

Der Runner-Report und das Netzwerkfeld `hostInputSha256` bleiben der Hash der
vollständigen kanonischen `deer.json` und dienen der DEER-Kompatibilität. Davon
getrennt berechnet der Host den privaten
`finalization.finalizedProjectSha256`: SHA-256 über eine domänenseparierte,
längenpräfigierte Kodierung des kanonischen DEER-Hashs sowie aller verifizierten
Custom-Assets, sortiert nach logischem Pfad und jeweils mit längenpräfigiertem
UTF-8-Pfad und vollständigem Inhalts-SHA-256. Konkret ist die Eingabe
`LP(UTF8("dungeon-wizard-finalized-project-v1")) || LP(DEER_HASH_BYTES) ||
UINT32_BE(ASSET_COUNT) || (LP(UTF8(PATH)) || LP(ASSET_HASH_BYTES))*`, wobei
`LP` die Byteanzahl als vorzeichenlose 32-Bit-Big-Endian-Zahl voranstellt und
die beiden Hashwerte aus ihren 64 Hexzeichen in je 32 Bytes dekodiert werden.
Medientyp und Pfaddeklaration sind
bereits durch die kanonische DEER-Identität gebunden; dieser private Hash bindet
zusätzlich die exakten Assetbytes. Die UI darf `finalizedProjectSha256` beim
Laden und Speichern eines Drafts ausschließlich erhalten: Sie zeigt ihn nicht
an, setzt ihn nicht als Autorität und verwendet den UI-internen Kandidatenhash
nicht als Ersatz. `deerSha256` bezeichnet weiterhin die exakten Bytes der
finalisierten, bereits mit dem stabilen Projektseed versehenen `deer.json`.

Für eine lokale Prüfung vor der ersten Finalisierung darf intern ein flüchtiger
gültiger Prüfseed eingesetzt werden. Dieser Wert ist kein Projektseed und wird
weder im Draft gespeichert noch als endgültige Projektidentität angezeigt. Bei
der ersten Finalisierung erzeugt erst der native Host den echten Seed
flüchtig, baut und validiert exakt diesen Kandidaten, schreibt neue
Custom-Dateien zuerst und `deer.json` zuletzt atomar und gibt den Seed erst nach
vollständig erfolgreichem Abschluss zur Draft-Persistenz zurück. Bei späteren
Finalisierungen verwendet er den bereits im Draft vorhandenen Seed unverändert.
Für ein Spielbild schreibt der Adapter nur den internen Pfad und die
Lizenzmetadaten in `deer.json`; das Bild wird nicht kopiert. Einen Upload hasht
und verifiziert der Host bereits beim Speichern und liefert den privaten
Storage-Key zurück. Der Draft leitet daraus den Pfad
`assets/custom/<normalisierter-stamm>-<12-hashzeichen>.<ext>` ab. Beim
Finalisieren prüft der Host Bindung und Bytes erneut, schreibt das Asset zuerst
und ersetzt danach `deer.json` atomar. Die private Draftrepräsentation darf
beide Varianten als Union unterscheiden. Diese technische Unterscheidung wird nicht als Feld in
`source` exportiert. Eine leere oder ausschließlich aus Leerraumzeichen
bestehende Attribution behandelt der Adapter wie eine fehlende Attribution und
lässt das Feld bei der Finalisierung weg.

Der Adapter bindet die Produktionsvalidierung direkt als Java-Bibliothek an.
Ausschließlich das in
[`project-validation-report.schema.json`](project-validation-report.schema.json)
definierte Ergebnis ist die Brücke zur Web-UI. Temporäre Prüfdateien liegen
außerhalb des Zielprojekts und werden nach der Prüfung entfernt.

Nach erfolgreicher Finalisierung ruft die UI den gemeinsamen Java-Packager mit
der generischen `WizardRoomTemplate.jar` auf und schreibt
`<projektordner>/WizardRoom.jar`. Ein Packaging-Fehler lässt das finalisierte
Projekt gültig und bietet einen erneuten Versuch an. Gradle und Node werden zur
Hostlaufzeit nicht benötigt. Der äquivalente Entwickler-/CI-Pfad bleibt:

```text
gradlew.bat :wizard:buildWizardRoomJar -PwizardProject=<projektordner>
```

Der Packaging-Request identifiziert die gespeicherte Finalisierung vollständig
über die exakte Revision des zuvor geleerten Save-Queue-Snapshots sowie `seed`,
Finalisierungszeitpunkt, Projektordner und `deer.json`-Hash. Der Host akzeptiert
diese Mutation nur per CAS; Erfolg erhöht die Revision genau einmal, veraltete
oder doppelte Requests werden mit HTTP 409 ohne Draftmutation abgelehnt. Die UI
übernimmt Revision und JAR-Pfad nur, wenn alle Identitätsfelder der Antwort
exakt übereinstimmen. Nach einem Reload fragt sie den Hoststatus einmal
neu ab; „Das Spiel ist bereit“ erscheint ausschließlich, wenn der Host sowohl
die aktuelle `deer.json` als auch seine JAR samt Hash bestätigt. Der reine
Browsermodus behauptet keinen solchen Produktionsstatus.

Die Gradle-Ausgabe `wizard/build/libs/WizardRoom.jar` beziehungsweise die UI-
Ausgabe im Projektordner wird an Host und alle weiteren Spielenden verteilt.
Dass die JAR das vollständige Projekt und
damit auslesbare Antworten enthält, ist eine bewusste Distributionsgrenze.
Host und Clients leiten daraus denselben vollständigen Foundation-Raum ab.

## Fehlerdarstellung

- `severity=error` blockiert, `severity=warning` nicht.
- `path`, `entity` und `relatedPaths` werden auf Draftfelder oder Rätsel
  abgebildet.
- `messageKey` und `arguments` werden lokalisiert; technische Codes und JSON
  Pointer werden Lehrenden nicht roh angezeigt.
- Ein unbekannter Issue-Code oder ein ungültiger Report ist ein technischer
  Adapterfehler, kein angeblicher Fehler des Entwurfs.
- Eingaben bleiben bei jedem Prüf-, Adapter- oder Schreibfehler erhalten.

## M2-Abnahme

Die umgesetzte M2-Architektur erfüllt für das kanonische Beispiel folgende
Abnahme:

1. als unvollständiger Draft gespeichert und nach Neustart geöffnet werden
   kann;
2. ohne technische Begriffe und vollständig per Tastatur erstellt werden kann;
3. lokale und produktive Fehler beziehungsweise Warnungen verständlich und
   ohne rohe technische Kennungen zeigt;
4. Story, Ablauf, Parallelität, Inhalte, Hinweise einschließlich ihrer
   Offenlegungsstufen und Bilder ohne Runtime voranzeigt und Lernziele,
   Nachbesprechung sowie die fachlichen Orte/Geräte vollständig erhält;
5. über die Produktionsvalidierung geprüft und mit seinem vorhandenen
   inhaltsadressierten Custom-Asset sowie stabil erzeugtem Seed sicher
   finalisiert wird;
6. nach Änderungen erneut finalisiert werden kann, ohne Seed oder vorhandene
   gültige Ausgabe bei einem Fehler zu verlieren;
7. aus der erfolgreichen Finalisierung im selben UI-Fluss eine
   `WizardRoom.jar` erzeugt und ein Packaging-Fehler unabhängig wiederholt
   werden kann.

Ein separates Abnahmeszenario wählt ein Bild aus der Spielbibliothek und
finalisiert ausschließlich dessen internen Pfad und Lizenzmetadaten. Dabei wird
keine Custom-Kopie erzeugt und das kanonische Foundation-Beispiel bleibt
inhaltlich unverändert.

Framework, Styling-System und konkrete Draftserialisierung sind keine
öffentlichen Verträge. Sie dürfen frei gewählt werden, solange der sichtbare
UI-Contract, der Storage-Port und die DEER-/Runner-Grenze eingehalten werden.
