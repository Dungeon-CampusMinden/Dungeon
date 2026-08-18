# Wizard UI Flow V0.4

Status: M2-Authoring-Flow mit nativem Host und Packaging umgesetzt
Stand: 27.07.2026

## Ziel

Dieses Dokument definiert den sichtbaren Authoring-Flow. Feldsemantik und
Runnerregeln stehen in den verlinkten Contract-Dateien; sie werden hier
nicht vollständig wiederholt. Auftrag, Schichtgrenzen, Storage-Port und
Frontend-Abnahme stehen im
[`Frontend-Handoff`](frontend-handoff-overview-v0.md).
Die späteren Fachabschnitte erläutern Semantik und wiederholen nicht die
Navigationsreihenfolge.

```text
Starten oder fortsetzen
-> Eckdaten
-> Geschichte
-> Spieleinstellungen
-> Eigene Bilder & Dateien
-> Rätsel
-> Spielablauf
-> Spiel-Ende
-> Entwurf prüfen und Spiel erstellen
```

## Grundsätze

- Lehrende bearbeiten keine JSON-Datei und keine technischen IDs.
- Der unvollständige Entwurf ist nicht `deer.json`.
- Eine ständig erreichbare Übersicht zeigt Navigation, Abschlussgrad,
  blockierende Probleme und Warnungen.
- Der Spielablauf ist ein frei bearbeiteter mandatory AND-DAG mit genau einem
  Knoten je Rätsel sowie genau einem geschützten Start- und Endknoten.
- Knoten und Kanten werden exakt gespeichert; die UI leitet keine zusätzlichen
  Abhängigkeiten ab und repariert den Graphen nicht bei der Finalisierung.
- `Entwurf prüfen` ist immer aktiv; `Spiel erstellen` erst nach einer
  erfolgreichen Prüfung.
- Warnungen blockieren nicht.
- Eine Prüfung garantiert Struktur und Runnerfähigkeit, nicht menschliche
  Lösbarkeit oder Lernerfolg.

## Entwurfslebenszyklus

1. Ein neuer oder wieder geöffneter UI-Entwurf darf unvollständig sein.
2. Änderungen werden lokal automatisch gespeichert.
   Gleichzeitige Speicherstände werden über eine fortlaufende Draftrevision
   erkannt. Bei einem Konflikt bleiben die geöffneten Änderungen sichtbar und
   werden nicht durch den fremden Stand ersetzt.
   Erfolgreiche Finalisierung und Packaging zählen ebenfalls als bestätigte
   Hoständerungen; ihre neue Revision übernimmt die UI vor dem nächsten
   Autosave.
3. Stabile IDs werden bei der ersten Anlage erzeugt und bei Umbenennung
   beibehalten.
4. Die UI projiziert den Entwurf für jede vollständige Prüfung auf ein
   `deer.json`-Kandidatenobjekt.
5. Nur ein fehlerfreier Kandidat wird finalisiert. Bei der ersten
   Finalisierung erzeugt der native Host den verpflichtenden `seed`
   zunächst nur flüchtig, baut und validiert exakt diesen Kandidaten, schreibt
   neue inhaltsadressierte Dateien zuerst und `deer.json` zuletzt atomar. Erst
   nach vollständig erfolgreichem Abschluss gibt er den Seed zur
   Draft-Persistenz zurück; vorher erzeugt oder speichert die UI keinen echten
   Projektseed.
6. Der Entwurf bleibt danach bearbeitbar. Eine Änderung markiert die letzte
   Finalisierung als veraltet. Weitere Finalisierungen verwenden den danach im
   Draft vorhandenen Seedwert unverändert.

V0.4 unterstützt die Wiederaufnahme eigener lokaler Entwürfe. Der Import
beliebiger Room-ZIPs oder manuell veränderter Projektordner ist nicht Teil des
Foundation-Slices. Der Produktfluss erzeugt selbst keine Room-ZIPs.

Der produktive Pfad läuft über den loopback-only Java-Host und die von ihm
same-origin ausgelieferte React-UI. Er speichert Draft v1 und Uploadbytes unter
`%LOCALAPPDATA%\Dungeon Wizard`. LocalStorage und IndexedDB werden nur beim
direkten Vite-Entwicklungsstart verwendet. Dessen LocalStorage-CAS serialisiert
Lesen, Revisionsvergleich und Schreiben über einen exklusiven Web Lock zwischen
Tabs; Browser ohne diese API können nicht still unsicher speichern. M1 und M2 sind
Implementierungsmeilensteine; DEER bleibt Format `0.4`. Für private
Browser-Prototypdaten gibt es keine Migration.

## Dauerhafte Übersicht

Die Navigation zeigt:

- Projekttitel;
- alle Authoring-Bereiche;
- den höchsten Fehler- oder Warnstatus je bereits besuchtem Bereich;
- den aktuellen Autosave-Status.

Die Review-Ansicht zeigt Zielordner, vollständige und lokale Prüfergebnisse
sowie Zustand und Zeitpunkt der letzten Finalisierung beziehungsweise JAR-
Erzeugung. Nach dem Öffnen bestätigt der native Host diesen Zustand erneut;
lokale Metadaten allein führen nie zur Anzeige „Das Spiel ist bereit“.

## Starten oder fortsetzen

Aktionen:

- neuen Entwurf anlegen;
- letzten lokalen Entwurf fortsetzen;
- einen anderen eigenen lokalen Entwurf öffnen;

Die Startansicht lädt Entwürfe asynchron. Änderungen im geöffneten Entwurf
werden automatisch gespeichert; beim Zurückkehren zur Startansicht wird eine
ausstehende Speicherung zuerst abgeschlossen. Der Zielordner wird im
Prüf-/Erstellen-Schritt gewählt. Beliebiger `deer.json`-Import und Browser-ZIP-
Export sind nicht vorgesehen.

## Eckdaten, Lernziel und Spieleinstellungen

Pflichtangaben:

| Sichtbares Feld | DEER-Ziel |
|---|---|
| Raumtitel | `metadata.title` |
| Inhaltssprache | `metadata.locale` |
| Zielgruppe | `session.targetAudience` |
| Vorwissen | `session.priorKnowledge` |
| Spielerzahl von/bis | `session.playerCount` |
| Zeitlimit und Modus | `session.time` |
| Was sollen Spielende wissen oder können? | `learningDesign.objectives[]` |

Die stabile `metadata.id` wird einmal aus dem Projekt angelegt und bei späteren
Umbenennungen beibehalten. Sie ist kein frei bearbeitetes Textfeld.

Optional sind `metadata.description`, `metadata.author`, weitere Lernziele und
Fragen für `learningDesign.debriefPrompts`. Lernziele und Nachbesprechung
werden nach `deer.json` projiziert und beeinflussen den Host-Input-Hash. Sie
steuern die Runtime nicht und werden vom aktuellen Runner nicht semantisch
bewertet.

V0.4 zeigt `de-DE` als einzige unterstützte Inhaltssprache und nicht als
scheinbar freie Sprachauswahl. Die Sprache der Wizard-Oberfläche ist eine
separate UI-Einstellung.

Blockierend:

- leeres Pflichtfeld;
- kein Lernziel;
- Spielerzahl außerhalb `1..4` oder `min > max`;
- Zeitlimit außerhalb `1..240` Minuten.

„von“ (`min`) ist die technische Startschwelle; „bis“ (`max`) ist die
Hostkapazität und Anzahl der für den Hostprozess reservierbaren
Dungeon-Identitäten und logischen Authority-Slots. Alle Spieler verwenden den
gemeinsamen Startpunkt. Ein neuer Client ersetzt keine bereits vergebene
Identität. Es gibt keinen separaten technischen Spielerzahlwert und keinen
Startknopf.

## Geschichte

Pflichtangaben:

| Sichtbares Feld | DEER-Ziel |
|---|---|
| Mission | `scenario.mission` |
| Intro-Seiten | `scenario.introText` |
| Erfolgsseiten | `scenario.successText` |
| Fehlschlagseiten bei hartem Zeitlimit | `scenario.failureText`, nur bei `hard` |

V0.4 nutzt das feste Theme `default` und reine Storytexte. Es gibt keine
Theme- oder Skin-Auswahl in der UI. Die feste `themeId` bestimmt automatisch den
geordneten Pool der spielbaren Skins: zuerst `THE_LAST_HOUR_ROGUE`, dann
`THE_LAST_HOUR_CHAR03`. Die offiziellen Wizard-Clients wählen daraus keinen
Skin; die Zuweisung erfolgt beim Beitritt durch den Server. Die `themeId` bleibt
als Erweiterungspunkt für zukünftige Themes erhalten. Die drei Seitenfolgen
werden als geordnete, nicht leere Listen bearbeitet; jeder Eintrag entspricht
einer weiterklickbaren Black-Fade-Seite. Nach den Intro-Seiten erscheint die
Mission als hervorgehobene letzte Intro-Seite.

Warnungen:

- sehr langer Text;
- Mission ohne erkennbare Spielsituation;
- Erfolgstext widerspricht dem gemeinsamen Ausgang.

## Spielablauf

Die UI zeigt einen frei bearbeitbaren **mandatory AND-DAG**:

- genau einen geschützten Startknoten;
- genau einen geschützten Endknoten;
- genau einen Knoten je Rätsel;
- frei gesetzte gerichtete Kanten ohne zusätzliche Bedingungen;
- eine Kante bedeutet: Der folgende Knoten wird nach seinem Vorgänger
  verfügbar;
- mehrere eingehende Kanten bedeuten immer, dass alle Vorgänger gelöst sein
  müssen.

Aktionen:

- Verbindungen zwischen Knoten hinzufügen und entfernen;
- Knoten frei verschieben oder automatisch anordnen;
- ein Rätsel über seinen Knoten öffnen und bearbeiten;
- Rätsel hinzufügen oder löschen; dieselbe fachliche Aktion fügt den genau
  einen zugehörigen Knoten hinzu beziehungsweise entfernt ihn samt Kanten.

Die UI verhindert beim Bearbeiten:

- doppelte Kanten;
- Kanten eines Knotens auf sich selbst;
- Zyklen.

Der private Draft darf dennoch unverbunden oder fachlich unvollständig sein.
Solche Zustände bleiben speicherbar, werden aber als lokale Probleme angezeigt
und blockieren eine spätere Finalisierung. Der DEER-Kandidat übernimmt
`riddleGraph.nodes` und `riddleGraph.edges` exakt und in ihrer gespeicherten
Reihenfolge. Er ergänzt, sortiert oder repariert nichts.

Nicht darstellbar in V0.4:

- OR-Verzweigung;
- optionales Pflichträtsel;
- Zyklus;
- manuelle Start-/Endknoten;
- freie Kantenbedingungen.

## Rätsel, Inhalte, Hinweise und eigene Dateien

Gemeinsam pro Rätsel:

- Authoring-Titel zur Orientierung im Wizard;
- mindestens ein zugeordnetes Lernziel;
- geschätzte Dauer in Minuten;
- optional Schwierigkeit;
- null oder mehr Informationsquellen;
- mindestens eine Eingabe;
- null oder mehr geordnete optionale Hinweise mit einer Offenlegungsstufe.

Alle Eingaben eines Rätsels müssen erfüllt sein. Die UI bietet dafür keine
OR-Regel oder frei formulierbare Bedingung an.

### Informationsquelle

Eine Informationsquelle benötigt:

- einen fachlichen Behälter;
- mindestens eine Information, einen Aufgabeninhalt oder ein Bild.

Optional kann eine PNG-/JPEG-Datei ergänzt werden. Ein Bild benötigt eine
eigene stabile Asset-ID. Der Asset Selector bietet dafür die verständlichen
Wege **Aus Spielbibliothek auswählen** und **Eigenes Bild hochladen**. Interne
Pfade, Hashes und Asset-IDs bleiben verborgen. Für ein Spielbild merkt sich der
private Draft die Auswahl und ihre Lizenzmetadaten; beim Finalisieren wird nur
der DEER-Eintrag mit dem internen Pfad geschrieben. Es wird keine Bilddatei
kopiert. Einen eigenen Upload hasht und verifiziert der native Host bereits
beim Speichern und liefert den privaten Storage-Key zurück. Der Draft leitet
daraus den inhaltsadressierten Pfad
`assets/custom/<normalisierter-stamm>-<12-hashzeichen>.<ext>` ab. Bei der
Finalisierung prüft der Host Bindung und Bytes erneut, schreibt das Asset
zuerst und ersetzt `deer.json` zuletzt atomar. Die Auswahlvariante erscheint
nicht als zusätzliches Feld in den öffentlichen `source`-Metadaten.

Die Lehrkraft benennt den **Fundort** direkt an der Informationsquelle. Die UI
legt dazu einmalig eine private Container-Surface an und hält deren technische
ID verborgen. Umbenennen oder Umordnen ändert diese ID nicht. Beim Löschen der
Quelle entfernt die UI auch deren Surface und leert betroffene Collection-
Referenzen. Informationsquellen dürfen unabhängig
vom Status des zugehörigen Rätsels gelesen werden. Soll das Entdecken selbst
verpflichtend sein, fügt die UI dafür eine Collection-Eingabe hinzu. Diese
verwendet dieselbe Surface und wird erst durch eine Interaktion bei verfügbarem
Rätsel erfüllt; früheres Lesen wird nicht angerechnet. Andernfalls liefert die
Quelle nur Information oder Aufgabeninhalt. Die einmalige World-Surface wird
vollständig automatisch verwaltet und ist keine Fundstation. Eine separate
Orte- oder Surface-Ansicht existiert nicht.

### Eingaben

Mindestens eine Eingabe ist Pflicht. V0.4 bietet geschlossen:

- **Zahlencode**;
- **Information entdecken**, wenn eine Informationsquelle zwingend gefunden
  werden muss.

Für einen Zahlencode sind Pflicht:

- der sichtbare Name des Geräts;
- erwarteter Code mit 1 bis 8 Ziffern.

Optional:

- Ziffernanzahl im Spiel anzeigen.

Die Codelänge wird aus dem Code abgeleitet. Es gibt kein separates
`maxLength`-Feld. Falsche Eingaben bleiben im Foundation-Slice unbegrenzt
wiederholbar und nutzen das vorhandene Runtime-Feedback. Die UI erzeugt das
private Keypad beim Anlegen der Zahleneingabe. Ein Wechsel zu „Information
entdecken“ entfernt es, ein Wechsel zurück erzeugt ein neues; die Input-ID
bleibt erhalten. Collection-Eingaben wählen eine Informationsquelle und
besitzen keine eigene Surface.

Eingaben reagieren im Spiel erst, wenn ihr Rätsel verfügbar ist. Mehrere
Eingaben eines Rätsels sind fest mit AND verknüpft. Sind alle erfüllt, erzeugt
die Runtime einmalig den impliziten Rätselabschluss; die UI fragt dafür kein
Output- oder Effektfeld ab.

### Hinweise

Für jeden optionalen Hinweis wählt die Lehrkraft genau eine verständliche
Offenlegungsstufe:

- **Was ist die Aufgabe? Wo kannst du anfangen?**;
- **Wie kannst du die Aufgabe lösen?**;
- **Was ist die Lösung?**

Die UI speichert dafür intern `severity=orientation`, `approach` oder
`solution`; der technische Feldname und die Enum-Werte bleiben verborgen.
`severity` ist keine frei vergebene Schwierigkeit oder Reihenfolgenummer.

Hinweise können in V0.4 angefordert werden, sobald das zugehörige Rätsel
verfügbar ist. Die Runtime kündigt vor jeder Freigabe die Stufe des nächsten
Hinweises an und verlangt eine ausdrückliche Bestätigung. Das gilt für alle
drei Stufen, damit auch eine Fehlinteraktion keinen Hinweis freigibt. Abbrechen
verändert weder den Freigabestatus noch die Position in der Reihenfolge. Nach
der Bestätigung zeigt die Runtime nur den nächsten Hinweis. Bereits
freigegebene Hinweise bleiben lesbar. Die Arrayreihenfolge ist unabhängig von
`severity` maßgeblich. Zeit- und Fehlversuchsbedingungen sind nicht Teil des
aktuellen Vertrags.

## Spiel-Ende

Unter „Spiel-Ende“ benennt die Lehrkraft genau einen **Ausgang**. World,
Door-Surface und `end.surfaceId` werden automatisch verwaltet. Der geschützte
Endknoten zeigt nur den Endzustand und keinen technischen Auswahlwert.
Wenn alle direkten Vorgängerrätsel des Endknotens abgeschlossen sind, wird das
Ende erreicht und die Tür serverautoritativ geöffnet. Erfolg tritt ein, wenn
die Runtime den Ausgang für alle aktiven Spielenden als erreicht meldet.

## Prüfen, Vorschau und Spiel erstellen

Rätselkarten, Asset-Vorschauen und Graphansicht zeigen den jeweils bearbeiteten
Inhalt ohne gestartete Spielruntime. Die Review-Ansicht konzentriert sich auf
Zielordner, Prüfung, Finalisierung und Packaging.

`Entwurf prüfen`:

- ist immer verfügbar;
- zeigt eine Zusammenfassung nach Fehlern und Warnungen;
- verändert oder verwirft keine Eingaben.

Prüfung und Finalisierung verwenden jeweils exakt den zuletzt vollständig
gespeicherten Draft-Snapshot. Änderungen, Kandidat und eigene Dateien können
daher nicht versehentlich aus verschiedenen UI-Zeitpunkten stammen.
Packaging verwendet ebenfalls die exakte Revision seines vollständig
gespeicherten Snapshots zusammen mit der vollständigen
Finalisierungsidentität. Eine erfolgreiche Hostantwort erhöht diese Revision
genau einmal und wird direkt übernommen; ein veralteter oder doppelter Request
bleibt ein verständlicher Konflikt, ohne den geöffneten Draft zu verwerfen.

Produktionsreports werden strikt geprüft und auf verständliche Meldungen ohne
technische Codes, Pointer oder IDs abgebildet.

Der Host hält `finalizedProjectSha256` privat im Draft: Die UI erhält ihn beim
Laden und Speichern typesicher, zeigt ihn nicht an und setzt ihn nie als
Autorität. Der Hash bindet domänensepariert die kanonische DEER-Identität und,
nach logischem Pfad sortiert, den längenpräfigierten Pfad sowie den vollständigen
Inhalts-SHA-256 jedes verifizierten Custom-Assets. Der öffentliche Runner- und
Netzwerkwert `hostInputSha256` bleibt dagegen ausschließlich der Hash der
kanonischen `deer.json` für die DEER-Kompatibilität. Der UI-Kandidatenhash dient
nur dazu, den aktuell sichtbaren Authoring-Stand zuzuordnen; `deerSha256`
gehört zu den exakten, mit dem stabilen Seed finalisierten Dateibytes.

`Spiel erstellen`:

- ist nur ohne blockierende Fehler aktiv;
- lässt den nativen Host beim ersten Finalisieren einen Seed nur flüchtig
  erzeugen, exakt diesen Kandidaten bauen und produktiv validieren;
- schreibt neue, inhaltsadressierte Dateien zuerst und `deer.json` zuletzt
  atomar; erst nach vollständig erfolgreichem Abschluss wird der Seed zur
  Draft-Persistenz zurückgegeben;
- lässt den nativen Vorgang einen bereits im Draft vorhandenen
  projektgebundenen Seedwert bei jeder weiteren Finalisierung unverändert
  verwenden;
- paketiert das finalisierte Projekt anschließend mit dem gemeinsamen
  Java-Packager und der generischen `WizardRoomTemplate.jar` als
  `<project>/WizardRoom.jar`;
- lässt bei einem Packaging-Fehler das finalisierte Projekt gültig und bietet
  „Spieldatei erneut erstellen“ an;
- lässt den Host die gespeicherte Finalisierungsidentität und die erzeugte JAR
  bestätigen, bevor die UI das Spiel als bereit anzeigt;
- startet die Spieler-JAR nicht.

Reine private Entwurfsnotizen werden nicht finalisiert. Lernziele und
Nachbesprechungsfragen sind dagegen verbindlicher Bestandteil von `deer.json`.

Der Gradle-Aufruf
`:wizard:buildWizardRoomJar -PwizardProject=<projektordner>` bleibt der
Entwickler-/CI-Pfad; der native Host benötigt für das Packaging weder Gradle
noch Node. Die UI erklärt, dass
dieselbe vollständige JAR an alle Spielenden verteilt und mit Java 25 über ihr
Host-/Join-Menü gestartet wird. Die UI beschreibt keine Multiplayerdetails;
der vollständige Start- und Multiplayer-Ablauf steht im
[`Runner-Runtime-Contract`](runner-runtime-contract.md).

`wizard/start_wizard_dev.cmd` ist ausschließlich ein
Entwicklungslauncher. Die Zielgruppen-`.exe` mit gebündelter Runtime bleibt ein
späterer Distributionsmeilenstein. M2 erzeugt keine `.exe` und kein Room-ZIP;
der aktuelle Entwicklungs- und Spieler-JAR-Fluss setzt Java 25 voraus.

Bei Berechtigungs-, Speicherplatz- oder Schreibfehlern bleibt die vorherige
`deer.json` unverändert. Die UI bietet „Erneut versuchen“ und „Anderen Ordner
wählen“. Ein vollständig geschriebenes, exakt zum Recovery-Beleg passendes
Projekt wird beim nächsten Versuch zuerst wiederhergestellt und nicht
verworfen. Ist das frühere Ziel dagegen unvollständig oder ungültig, darf ein
anderer Ordner mit demselben bereits vergebenen Seed finalisiert werden; erst
nach erfolgreicher Validierung und Besitzprüfung ersetzt der Host den
Recovery-Beleg atomar, bevor er das neue Ziel schreibt. Das frühere Ziel und
eine zuletzt gültige gespeicherte Finalisierung bleiben dabei unverändert.
Unbekannte oder alte unreferenzierte Dateien werden nicht gelöscht.

## Blockierende Prüfungen

- fehlende Pflichtangabe;
- doppelte ID oder unbekannte Referenz;
- ungültiges Graphprofil, doppelte Kante oder Zyklus;
- nicht erreichbares Rätsel oder Erfolgsziel;
- nicht unterstützter Rätsel-, Inhalts- oder Bild-/Dateityp;
- fehlendes oder unsicheres Bild / fehlende oder unsichere Datei;
- Informationsquelle ohne Inhalt;
- Rätsel ohne Eingabe;
- unbekannte Formatversion.

## Deterministische Warnungen

- sehr lange Texte;
- Bild ist nicht verwendet;
- Entwurf wurde seit der letzten Finalisierung verändert.

## Redaktionelle Selbstprüfung

Diese Fragen werden als Checkliste gestellt, nicht als automatisch erkannte
Probleme:

- Lässt sich die Lösung aus Aufgabe und Material wirklich ableiten?
- Ist die Aufgabe für Spielende klar?
- Sind die Hinweise verständlich und sinnvoll gestuft?
- Passen Sprache und Umfang zu den vorgesehenen Spielenden?
