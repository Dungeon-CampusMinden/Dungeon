# Frontend Handoff V0.3

Status: verbindlicher V0.3-Handoff; Implementierung folgt separat

## Auftrag

Die Frontend-Umsetzung baut eine lokale Standalone-App mit Web-Oberfläche für
nicht-technische Lehrende. Sie führt einen unvollständigen privaten Entwurf,
prüft ihn und finalisiert daraus genau diesen öffentlichen Projektordner:

```text
<project>/
  deer.json
  assets/custom/...  # nur bei eigenen Bildern
```

Die UI startet keine Spielruntime. Sie erzeugt weder Java-Code noch ein
Raummodul, Buildskripte oder ein ZIP. Der generische Java-Runner bleibt die
Autorität für Projektvalidierung und Runtimefähigkeit. Aus dem finalisierten
Projekt erzeugt der separate Gradle-Packager eine projektspezifische
ausführbare `WizardRoom.jar`; seine Anbindung an die Authoring-UI bleibt eine
spätere dünne Integration.

Der festgelegte Foundation-Slice bildet **Informationsquellen**,
**Zahlencode**, verpflichtendes Entdecken und den gemeinsamen Ausgang ab.
Das Schema definiert diesen Rätselvertrag; Runner und Frontend müssen ihn
gemeinsam erfüllen. Der Frontend-Anteil ist fertig, sobald die
Definition-of-Done am Ende dieses Dokuments erfüllt ist. Die Authoring-UI
selbst ist noch kein
Ein-Klick-Produkt: Eine technische Betreuung ruft den Packager auf und verteilt
die fertige JAR. Spielende starten Host oder Join danach über deren Main-Menü;
ein separates Serverartefakt ist nicht erforderlich.

## Rollen

- **Lehrende Person:** erstellt, prüft, finalisiert und überarbeitet den
  privaten Entwurf, ohne JSON oder technische IDs zu bearbeiten.
- **Technische Betreuung:** finalisiert beziehungsweise validiert das
  Authoring-Projekt, erzeugt `WizardRoom.jar` und verteilt dieselbe JAR an alle
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
5. [`../../examples/foundation-v0.3/`](../../examples/foundation-v0.3/) ist
   das kanonische Projektbeispiel.

Multiplayer und Spielruntime sind kein Frontend-Auftrag. Ihre Grenze steht in
[`runner-runtime-contract.md`](runner-runtime-contract.md).

## Verantwortungsgrenze

| Schicht | Besitzt | Besitzt ausdrücklich nicht |
|---|---|---|
| Web-UI | sichtbaren Flow, privaten Draft, unmittelbare Feldhinweise, Vorschau | Projektdateisystem, Runner-Runtime, Multiplayer |
| Nativer Host-Adapter | Draft-Persistenz, Uploadbytes, Auswahl aus der internen Assetliste, Produktionsvalidierung, atomare Finalisierung | fachliche UI-Navigation, Foundation-Spielzustand |
| Java-Runner | normative Projektvalidierung, Ableitung und Host-/Join-Runtime | privaten Draft, UI-Zustand, Projektbearbeitung |
| Gradle-Packager | validiertes Projekt als projektspezifische ausführbare JAR | privaten Draft, UI-Navigation, neue DEER-Semantik |

Der Frontend-Code darf lokale Feldhinweise für schnelle Rückmeldung berechnen.
Er darf aber keine zweite normative semantische Validierung etablieren.
`Entwurf finalisieren` verwendet immer das Ergebnis der produktiven
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

Fachlich sichtbare Orte und Geräte werden innerhalb der Rätselschritte
abgefragt. Die UI leitet daraus stabile Surfaces der geschlossenen Arten
`world`, `container`, `keypad` und `door` sowie die jeweiligen `surfaceId`-
Bindungen ab. Der technische Begriff „Surface“ und die IDs bleiben verborgen.
Ein Computer ist noch keine Surface-Art des aktiven Profils. Die UI bietet
weder ein Output-/Effektfeld noch OR-Regeln an; Progression und Ausgang gehören
allein dem abgeleiteten Rätselgraphen.

## Draft- und Storage-Port

Das Draftformat ist privat und darf unvollständig sein. Es ist kein zweites
DEER-Format. Seine konkrete Serialisierung ist eine Frontend-Entscheidung,
muss aber eine eigene Versionskennung besitzen und mindestens erhalten:

- stabile Draft- und Projektidentität;
- alle sichtbaren Eingaben und stabil erzeugten Authoring-IDs;
- bei Uploads die Bildbytes, bei Spielbildern den internen Pfad sowie jeweils
  Anzeigename und Lizenzmetadaten;
- gewählten Projektordner, sofern vorhanden;
- den Seed eines bereits finalisierten Projekts;
- Zeitpunkt und Zustand der letzten Speicherung und Finalisierung.

Ein unbekanntes Draftformat wird verständlich abgelehnt und nicht teilweise
geladen. V0.3 benötigt keine Migration zwischen Draftversionen.

Der native Host-Adapter stellt der Web-UI genau diese logischen Operationen
bereit; konkrete Methodennamen dürfen dem verwendeten Stack folgen:

| Operation | Erfolg | Fehlergarantie |
|---|---|---|
| Drafts auflisten, anlegen, laden und speichern | vollständiger privater Snapshot | vorhandener Snapshot bleibt unverändert |
| Projektordner wählen | explizit vom Nutzer bestätigter nativer Ordner | Abbruch verändert keinen Draft |
| Spielbibliothek-Asset auswählen oder eigenes PNG/JPEG hochladen | interne Referenz oder geprüfte Uploadbytes, jeweils mit Anzeigename und Lizenzmetadaten | keine Teilübernahme |
| vollständigen Kandidaten prüfen | [`RunnerReport`](runner-report.schema.json) der Produktionsvalidierung | Zielprojekt bleibt unverändert |
| Kandidaten finalisieren | neue Custom-Dateien zuerst, `deer.json` zuletzt atomar ersetzt | letzte gültige Finalisierung bleibt verwendbar |

Für eine Prüfung vor der ersten Finalisierung darf der Adapter im privaten
Prüfbereich einen temporären gültigen Seed einsetzen. Dieser Wert wird weder
als Projektseed gespeichert noch als endgültige Projektidentität angezeigt.
Bei der ersten Finalisierung erzeugt die UI den echten Seed, prüft den exakten
Kandidaten erneut und schreibt nur dieses erfolgreich geprüfte Ergebnis.
Für ein Spielbild schreibt der Adapter nur den internen Pfad und die
Lizenzmetadaten in `deer.json`; das Bild wird nicht kopiert. Für einen Upload
berechnet er SHA-256, schreibt die Datei unter
`assets/custom/<12-hashzeichen>-<normalisierter-name>` und ersetzt erst danach
`deer.json` atomar. Die private Draftrepräsentation darf beide Varianten als
Union unterscheiden. Diese technische Unterscheidung wird nicht als Feld in
`source` exportiert.

Der Adapter darf die Produktionsvalidierung direkt als Java-Bibliothek oder
über `wizard-runner validate` anbinden. In beiden Fällen ist ausschließlich
das in [`runner-report.schema.json`](runner-report.schema.json) definierte
Ergebnis die Brücke zur Web-UI. Temporäre Prüfdateien liegen außerhalb des
Zielprojekts und werden nach der Prüfung entfernt.

Bis zur späteren UI-Anbindung erzeugt die technische Betreuung das
Spielerartefakt nach erfolgreicher Finalisierung mit:

```text
gradlew.bat :wizard:buildWizardRoomJar -PwizardProject=<projektordner>
```

Die Ausgabe `wizard/build/libs/WizardRoom.jar` wird an Host und alle weiteren
Spielenden verteilt. Dass die JAR das vollständige Projekt und
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

## Frontend-Definition-of-Done

Der Foundation-Frontend-Slice ist fertig, wenn das kanonische Beispiel:

1. als unvollständiger Draft gespeichert und nach Neustart geöffnet werden
   kann;
2. ohne technische Begriffe und vollständig per Tastatur erstellt werden kann;
3. verständliche Fehler und Warnungen mit direktem Sprung zum betroffenen
   Element zeigt;
4. Story, Ablauf, Parallelität, Inhalte, Hinweise einschließlich ihrer
   Offenlegungsstufen und Bilder ohne Runtime voranzeigt und Lernziele,
   Nachbesprechung sowie die fachlichen Orte/Geräte vollständig erhält;
5. über die Produktionsvalidierung geprüft und mit seinem vorhandenen
   inhaltsadressierten Custom-Asset sowie stabil erzeugtem Seed sicher
   finalisiert wird;
6. nach Änderungen erneut finalisiert werden kann, ohne Seed oder vorhandene
   gültige Ausgabe bei einem Fehler zu verlieren.

Ein separates Abnahmeszenario wählt ein Bild aus der Spielbibliothek und
finalisiert ausschließlich dessen internen Pfad und Lizenzmetadaten. Dabei wird
keine Custom-Kopie erzeugt und das kanonische Foundation-Beispiel bleibt
inhaltlich unverändert.

Framework, Styling-System und konkrete Draftserialisierung sind keine
öffentlichen Verträge. Sie dürfen frei gewählt werden, solange der sichtbare
UI-Contract, der Storage-Port und die DEER-/Runner-Grenze eingehalten werden.
