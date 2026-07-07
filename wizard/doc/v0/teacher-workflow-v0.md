# Teacher Workflow V0

Status: funktionaler UI-Contract für den Wizard-Prototyp
Stand: 07.07.2026

## Zweck

Dieses Dokument beschreibt nicht das visuelle Design. Es legt fest, wann, wo
und welche Informationen Lehrende im Wizard angeben können und welche
Validierungen greifen.

Festgelegt sind:

- fachliche Daten, die erfasst werden,
- Daten, die die UI automatisch ableitet,
- blockierende Fehler,
- Bedingungen für den Finalisieren-Button,
- Bausteine, die im aktuellen Generator-Slice generierbar sind,
- technische Interna, die nicht zur Hauptsprache der UI werden.

Gestaltung, Komponenten, Navigation, Icons, Microcopy und Interaktionsdetails
bleiben frei.

## Beispiel-Workflow Aus Lehrenden-Sicht

Eine Lehrkraft erstellt einen kleinen Escape Room:

1. Sie legt Titel, Sprache, Zielgruppe, Spielerzahl und Zeitlimit fest.
2. Sie beschreibt Spielrolle, Ausgangslage und Mission.
3. Sie fügt einen Fund-Baustein und ein Keypad-Rätsel hinzu.
4. Die UI zeigt daraus entstehende Oberflächen wie Fundort, Keypad und Tür.
5. Sie ordnet die Rätsel in einer strukturierten Ablaufansicht an.
6. Sie füllt pro Baustein die benötigten Inhalte aus.
7. Sie ergänzt Texte, Bilder, Audio und optionale Hinweise.
8. Die UI prüft den Raum auf blockierende Fehler.
9. Wenn keine blockierenden Fehler existieren, finalisiert sie den Entwurf.
10. Danach kann der Projektordner manuell an den Generator übergeben werden.
    Der Generator erzeugt daraus das spielbare Room-Paket.

## Workflow-Schritte

### 1. Übersicht

Zweck: Status und nächste offene Aufgaben sichtbar machen.

Anzeigen:

- Projekttitel, falls vorhanden,
- Fortschritt je Schritt,
- Anzahl Rätsel,
- offene Pflichtfelder,
- blockierende Fehler,
- Warnungen,
- Preflight-Status.

Validierung:

- keine eigene Validierung,
- aggregiert den Zustand der anderen Schritte.

### 2. Rahmen

Zweck: Allgemeine Sitzungsdaten erfassen.

Pflichtangaben:

- Raumtitel,
- Sprache,
- Zielgruppe,
- Vorwissen,
- minimale und maximale Spielerzahl,
- Zeitlimit,
- Zeitmodus: hartes oder weiches Limit.

Blockiert Finalisierung, wenn:

- ein Pflichtfeld fehlt,
- ein Zahlenbereich ungültig ist.

Nicht als Eingabe sichtbar:

- separate Empfehlung zur Spielerzahl,
- Kooperationsmodus; V0 ist immer kooperativ.

### 3. Szenario

Zweck: Sicherstellen, dass der Escape Room eine Spielsituation hat und nicht nur
Fachinhalt in Spieloberflächen verpackt.

Pflichtangaben:

- Rolle der Spielenden,
- Ausgangslage,
- Mission,
- Intro-Text,
- Erfolgstext,
- Fehlschlagtext.

Warnungen:

- sehr lange Texte,
- Mission wirkt unklar,
- Text beschreibt nur Fachinhalt ohne Spielsituation.

### 4. Bausteine Und Oberflächen

Zweck: Lehrende wählen fachliche Spielbausteine. Die UI leitet daraus
benötigte Oberflächen ab.

Aktive V0-Bausteine:

- Fund / `collection.single`, intern `riddle.type=collection` mit
  `parameters.rewardMode=find_resource`,
- Keypad / `input.numeric`, intern `riddle.type=input` mit
  `parameters.inputMode=numeric`.

Abgeleitete aktive Oberflächen:

- Raum,
- Fundort,
- Keypad,
- Tür.

Post-V0-Bausteine wie Computer-Login, E-Mail-Auswahl, USB, Control Panel und
Bildfragmente bleiben deaktiviert, bis der Generator sie unterstützt.

UI-Regeln:

- Lehrende legen nicht zuerst eine technische Slot-Liste an.
- Wenn ein Baustein eine Oberfläche braucht, erzeugt die UI sie automatisch
  oder bietet eine passende vorhandene Oberfläche zur Auswahl an.
- Intern schreibt die UI diese Oberflächen in das `surfaces`-Array der
  `deer.json`; Rätselparameter referenzieren sie über `surfaceId`.

Blockiert Finalisierung, wenn:

- ein Baustein keine benötigte Oberfläche hat,
- eine inkompatible Baustein-/Oberflächen-Kombination entsteht,
- ein Baustein im aktuellen Generator-Slice noch nicht generierbar ist.

### 5. Rätselablauf

Zweck: Festlegen, welche Rätsel in welcher Reihenfolge gelöst werden müssen.

Fachliches Modell:

- Der Ablauf besteht aus Gruppen.
- Gruppen werden nacheinander gelöst.
- Eine Gruppe kann ein oder mehrere Rätsel enthalten.
- Mehrere Rätsel in derselben Gruppe gelten als parallel lösbar.
- Die nächste Gruppe wird erst relevant, wenn alle Progressionsrätsel der
  vorherigen Gruppe abgeschlossen werden können.

Die Darstellung ist frei: Liste, Timeline, Board, Kartenansicht oder Canvas
sind möglich, solange daraus eindeutig eine gültige Reihenfolge mit optionalen
Parallelgruppen abgeleitet werden kann.

V0 erzeugt hier nicht:

- frei gezogene beliebige Graphkanten,
- optionale Rätselpfade,
- alternative Enden,
- mehrere Level.

Blockiert Finalisierung, wenn:

- ein Softlock möglich ist,
- ein Rätsel unerreichbar ist,
- ein Progressionsrätsel übersprungen werden kann,
- eine Abhängigkeit zyklisch oder unlösbar ist,
- der Endzustand nicht erreicht werden kann.

### 6. Rätsel Bearbeiten

Zweck: Die konkreten Inhalte und Lösungen pro Baustein erfassen.

Gemeinsame Pflichtangaben pro Rätsel:

- sichtbarer Name,
- Bausteintyp,
- Aufgabe für Spielende,
- Erfolg/Freischaltung aus kontrollierter Auswahl,
- benötigte Inhalte oder Assets.

Typ-spezifische Pflichtangaben:

| Baustein | Pflichtangaben |
|---|---|
| Fund | Fundtyp, Fundort, Hinweis oder Ressource |
| Keypad | Zahlencode, maximale Länge, Erfolg |

Optional:

- Schwierigkeit,
- geschätzte Dauer,
- Hinweise,
- Erfolgstext,
- Fehlertext.

Blockiert Finalisierung, wenn:

- Pflichtparameter fehlen,
- eine Lösung nicht zum Eingabetyp passt,
- Erfolg/Freischaltung mit dem Ablauf unvereinbar ist.

### 7. Inhalte, Assets Und Hinweise

Zweck: Texte, Bilder, Audio und optionale Hilfen zentral verwalten.

V0-Eingaben:

- Texte direkt im Wizard,
- Bilder als Upload,
- Audio als Upload,
- optionale Hinweise pro Rätsel.

Theme-, Tileset-, Sprite-, UI-Skin- und Office/PDF-Uploads sind keine
Runtime-Assets dieses V0-Flows.

Blockiert Finalisierung, wenn:

- ein required Asset fehlt,
- ein Asset nicht unterstützt wird,
- ein Rätsel auf eine nicht vorhandene Ressource verweist,
- ein Hint auf ein nicht vorhandenes Rätsel verweist.

Warnungen:

- Rätsel ohne Hinweise,
- ungenutzte Assets,
- sehr lange Texte.

Hint-Freischaltungen:

- ohne Bedingung: sofort verfügbar,
- nach Zeit,
- nach Fehlversuchen,
- nach gelöstem Rätsel.

### 8. Prüfen Und Entwurf Finalisieren

Zweck: Letzte nicht-technische Prüfung vor dem manuellen Generator-Handoff.

Anzeigen:

- alle blockierenden Fehler,
- alle Warnungen,
- Anzahl Rätsel,
- geschätzte Gesamtdauer,
- verwendete Bausteine,
- verwendete Assets.

Hauptaktion:

- `Entwurf finalisieren`.

Validierung:

- Schema-Preflight,
- Pflichtfelder,
- Bausteinparameter,
- Oberflächen,
- Asset-Referenzen,
- Ablauf/Softlock-Prüfung.

Finalisierung ist nur erlaubt, wenn keine blockierenden Fehler existieren.
Warnungen blockieren nicht.

## Validierungszeitpunkte

| Zeitpunkt | Zweck | Beispiel |
|---|---|---|
| Direkt am Feld | schnelle Rückmeldung | leerer Titel, ungültige Spielerzahl |
| Beim Verlassen eines Schritts | Schrittstatus setzen | Szenario unvollständig |
| Nach Bausteinänderung | abgeleitete Oberflächen prüfen | Keypad braucht Keypad-Oberfläche |
| Nach Ablaufänderung | Softlocks und Skips verhindern | Rätsel wird unerreichbar |
| Vor Finalisierung | finaler Preflight | Schema, Assets, Graph, Pflichtparameter |

## Fehlerstufen

### Blockierend

Blockierend ist alles, was zu einem technisch oder spielerisch kaputten Raum
führen kann:

- fehlende Pflichtdaten,
- fehlende required Assets,
- inkompatible Baustein-/Oberflächen-Kombination,
- unerreichbares Rätsel,
- ungewollt überspringbares Progressionsrätsel,
- Softlock,
- zyklische oder unlösbare Abhängigkeit,
- nicht erreichbarer Endzustand,
- nicht generierbarer Baustein im aktuellen Generator-Slice.

### Warnung

Warnungen helfen bei Qualität, blockieren aber nicht:

- lange Texte,
- keine Hinweise,
- schwache Story-Einbettung,
- ungenutzte Assets,
- geschätzte Dauer passt schlecht zum Zeitlimit.

## Automatisch Abgeleitete Daten

Die UI erzeugt technische Daten automatisch:

- stabile IDs,
- `surfaces` aus den gewählten Bausteinen,
- `riddleGraph`-Kanten mit `completedRiddles`,
- kontrollierte `successEffect`-Werte,
- Slot-Typen,
- leere Arrays für `resources`, `hints` und `assetIds`,
- Standardwerte für Theme und Levelanzahl.

Lehrende bearbeiten diese Werte nicht direkt. Runtime-Tokens, Petri-Netze,
Trigger und konkrete Slots entstehen erst im Generator.

## Gestaltungsspielraum Für Die UI

Frei wählbar:

- Layout und visuelle Hierarchie,
- konkrete Komponenten,
- Drag-and-drop oder Buttons,
- Listen-, Board-, Timeline- oder Canvas-Darstellung,
- Icons, Farben und Microcopy,
- ob Schritte strikt nacheinander oder frei anwählbar sind.

Fest:

- Finalisierung ist nur bei gültigem Preflight aktiv.
- Technische Interna werden nicht zur Hauptsprache der UI.
- Der Ablauf muss in eine valide `deer.json` übersetzbar sein.
- V0 erzeugt keine optionalen Progressionsrätsel oder alternative Enden.
