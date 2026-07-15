# Wizard UI Flow V0.2

Status: kanonischer UI-Contract
Stand: 09.07.2026

## Ziel

Dieses Dokument definiert den sichtbaren Authoring-Flow. Feldsemantik und
Generatorregeln stehen in den verlinkten Contract-Dateien; sie werden hier
nicht vollständig wiederholt.

```text
Starten oder fortsetzen
-> Eckdaten & Lernziel
-> Geschichte
-> Spielablauf
-> Rätsel, Inhalte & Hilfen
-> Prüfen, Vorschau & finalisieren
```

## Grundsätze

- Lehrende bearbeiten keine JSON-Datei und keine technischen IDs.
- Der unvollständige Entwurf ist nicht `deer.json`.
- Eine ständig erreichbare Übersicht zeigt Navigation, Abschlussgrad,
  blockierende Probleme und Warnungen.
- Technische Orte, Graphknoten und Kanten werden aus fachlichen Eingaben
  abgeleitet.
- Der sichtbare Ablauf ist eine geordnete Abschnittsliste, kein freier Graph.
- `Entwurf prüfen` ist immer aktiv; `Entwurf finalisieren` erst nach einer
  erfolgreichen Prüfung.
- Warnungen blockieren nicht.
- Eine Prüfung garantiert Struktur und Generatorfähigkeit, nicht menschliche
  Lösbarkeit oder Lernerfolg.

## Entwurfslebenszyklus

1. Ein neuer oder wieder geöffneter UI-Entwurf darf unvollständig sein.
2. Änderungen werden lokal automatisch gespeichert.
3. Stabile IDs werden bei der ersten Anlage erzeugt und bei Umbenennung
   beibehalten.
4. Die UI projiziert den Entwurf für jede vollständige Prüfung auf ein
   `deer.json`-Kandidatenobjekt.
5. Nur ein fehlerfreier Kandidat wird finalisiert. Neue inhaltsadressierte
   Dateien werden zuerst geschrieben; `deer.json` wird zuletzt sicher ersetzt.
6. Der Entwurf bleibt danach bearbeitbar. Eine Änderung markiert die letzte
   Finalisierung als veraltet.

V0.2 unterstützt die Wiederaufnahme eigener lokaler Entwürfe. Der Import
beliebiger Generator-ZIPs oder manuell veränderter Projektordner ist nicht Teil
des Foundation-Slices.

Der Foundation-Host ist eine Standalone-App mit Web-Oberfläche und nativem
Storage-Adapter. Ein browser-only Host folgt erst nach einem eigenen
Speicher-/Export-Slice.

## Dauerhafte Übersicht

Die Übersicht ist Navigation, kein Arbeitsschritt. Sie zeigt:

- Projekttitel;
- Schritte mit getrenntem Abschlussstatus und Fehler-/Warnungszähler;
- Anzahl Abschnitte und Rätsel;
- Zeitpunkt der letzten lokalen Speicherung;
- Zeitpunkt und Zustand der letzten Finalisierung;
- nächsten sinnvollen Arbeitsschritt.

Abschlussstatus:

- `nicht begonnen`;
- `in Bearbeitung`;
- `vollständig`.

Fehler und Warnungen werden separat gezählt. Status wird nie ausschließlich
durch Farbe vermittelt.

## 1. Starten oder fortsetzen

Aktionen:

- neuen Entwurf anlegen;
- letzten lokalen Entwurf fortsetzen;
- einen anderen eigenen lokalen Entwurf öffnen;
- einen kleinen geführten Beispielentwurf kopieren.

Der Beispielentwurf ist entfernbar und keine vorausgewählte The-Last-Hour-
Raumstruktur. Er erklärt lediglich „Fund -> Zahlencode -> Ausgang“.

Beim Anlegen wird ein Projektname abgefragt. Der Projektordner kann spätestens
vor der ersten Bildauswahl festgelegt werden.

## 2. Eckdaten & Lernziel

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

Optional:

- Kurzbeschreibung und Autor;
- weitere Lernziele;
- eine oder mehrere Reflexionsfragen für die Nachbesprechung.

V0.2 zeigt `de-DE` als einzige unterstützte Inhaltssprache und nicht als
scheinbar freie Sprachauswahl. Die Sprache der Wizard-Oberfläche ist eine
separate UI-Einstellung.

Blockierend:

- leeres Pflichtfeld;
- keine Lernziele;
- Spielerzahl außerhalb `1..4` oder `min > max`;
- Zeitlimit außerhalb `1..240` Minuten.

Hinweis:

- Die Lernzielangabe ist fachliche Orientierung, keine automatische
  Kompetenzmessung.

## 3. Geschichte

Pflichtangaben:

| Sichtbares Feld | DEER-Ziel |
|---|---|
| Rolle der Spielenden | `scenario.playerRole` |
| Ausgangslage | `scenario.premise` |
| Mission | `scenario.mission` |
| Intro | `scenario.introText` |
| Erfolg | `scenario.successText` |
| Fehlschlag bei hartem Zeitlimit | `scenario.failureText`, nur bei `hard` |

V0.2 nutzt ein festes Standard-Theme und reine Storytexte. Lore-Bilder,
Intro-/Ambient-Audio und Theme-Auswahl folgen erst in einer späteren Version,
der diese Inhalte tatsächlich abbildet.

Warnungen:

- sehr langer Text;
- Mission ohne erkennbare Spielsituation;
- Erfolgstext widerspricht dem gemeinsamen Ausgang.

## 4. Spielablauf

Die UI zeigt geordnete **Abschnitte**:

- Abschnitte werden nacheinander relevant.
- Ein Abschnitt enthält ein oder mehrere Rätsel.
- Mehrere Rätsel im selben Abschnitt sind parallel verfügbar.
- Alle Rätsel sind Pflichträtsel.
- Der nächste Abschnitt wird erst verfügbar, wenn alle Rätsel des vorherigen
  Abschnitts abgeschlossen sind.

Aktionen:

- Abschnitt hinzufügen, umbenennen, verschieben oder löschen;
- Rätsel **Fund** oder **Zahlencode** hinzufügen;
- Rätsel per Buttons nach oben/unten oder in einen anderen Abschnitt bewegen;
- optionales Drag-and-drop als zusätzliche Bedienung;
- Löschen und Verschieben rückgängig machen.

Die UI erzeugt intern:

- genau einen Startknoten;
- genau einen Rätselknoten pro Rätsel;
- reine AND-Kanten zwischen aufeinanderfolgenden Abschnitten;
- genau einen Endknoten mit Ausgangsreferenz.

Nicht darstellbar in V0.2:

- OR-Verzweigung;
- optionales Pflichträtsel;
- Zyklus;
- manuelle Start-/Endknoten;
- freie Kantenbedingungen.

## 5. Rätsel, Inhalte & Hilfen

Gemeinsam pro Rätsel:

- Name;
- kurze Aufgabe für Spielende;
- mindestens ein zugeordnetes Lernziel;
- geschätzte Dauer in Minuten;
- Schwierigkeit optional;
- null oder mehr geordnete Hilfen.

### Fund

Pflicht:

- fachlicher Ort, z. B. „Schreibtisch“ oder „Raum“;
- Fundart: Behälter oder sichtbares Weltobjekt;
- mindestens ein notwendiger Hinweis- oder Aufgabentext direkt im Wizard;
- optional zusätzlich eine PNG-/JPEG-Datei.

Die UI leitet technische IDs und Ort-/Gerät-Art ab. Ein Bild benötigt eine
nicht-spoilernde Alternativbeschreibung oder die explizite Kennzeichnung
„rein dekorativ“. Herkunft und Lizenz werden beim Upload erfasst.

### Zahlencode

Pflicht:

- fachliches Gerät, z. B. „Tür-Keypad“;
- erwarteter Code mit 1 bis 8 Ziffern.

Optional:

- Ziffernanzahl im Spiel anzeigen.

Die Codelänge wird aus dem Code abgeleitet. Es gibt kein separates
`maxLength`-Feld. Falsche Eingaben bleiben im Foundation-Slice unbegrenzt
wiederholbar und nutzen das vorhandene Runtime-Feedback.

### Hilfen

Hilfen können in V0.2 sofort angefordert werden, sobald das zugehörige Rätsel
verfügbar ist. Die Runtime zeigt auf Anfrage nur die nächste Hilfe; die UI-
Reihenfolge wird intern als `severity=1..n` materialisiert. Zeit-,
Fehlversuchs- und Rätselabschlussbedingungen sind noch nicht aktiv.

„Hinweis“ bezeichnet notwendigen Rätselinhalt; „Hilfe“ bezeichnet optionale
Unterstützung.

### Ausgang

Die UI fragt genau eine Ausgangstür ab. Sie liegt am
gemeinsamen Endknoten. Wenn alle letzten Pflichträtsel abgeschlossen sind, wird
die Tür serverautoritativ geöffnet. Erfolg tritt ein, wenn die Runtime den
Ausgang für alle aktiven Spielenden als erreicht meldet.

## 6. Prüfen, Vorschau & finalisieren

Die Vorschau zeigt ohne Runtime:

- Eckdaten und Lernziele;
- Intro, Mission, Erfolg und bei hartem Zeitlimit Fehlschlag;
- Abschnitte, Parallelität und erwartete Reihenfolge;
- Aufgaben, Materialien und Hilfen;
- geschätzte Dauer als kritischen Pfad, nicht als bloße Summe paralleler
  Rätsel;
- verwendete Bilder und deren Alternativbeschreibungen;
- Reflexionsfragen in einem Abschnitt „Nachbesprechung“.

`Entwurf prüfen`:

- ist immer verfügbar;
- zeigt eine Zusammenfassung nach Fehlern und Warnungen;
- fokussiert auf Wunsch das betroffene Element;
- verändert oder verwirft keine Eingaben.

Jedes Problem enthält sichtbar:

1. Problem;
2. Auswirkung;
3. konkrete Korrektur;
4. Aktion „Zum Feld“ oder „Zum Rätsel“.

Beispiel:

> „Zahlencode“ hat noch kein Lernziel. Ordne mindestens ein Lernziel zu.
> **Zum Rätsel**

`Entwurf finalisieren`:

- ist nur ohne blockierende Fehler aktiv;
- schreibt neue, inhaltsadressierte Dateien zuerst und ersetzt `deer.json`
  zuletzt;
- zeigt den Zielordner und den nächsten Schritt für die technische Betreuung;
- startet den Generator nicht.

Bei Berechtigungs-, Speicherplatz- oder Schreibfehlern bleibt die vorherige
`deer.json` unverändert. Die UI bietet „Erneut versuchen“ und „Anderen Ordner
wählen“. Unbekannte oder alte unreferenzierte Dateien werden nicht gelöscht.

## Blockierende Prüfungen

- fehlende Pflichtangabe oder Lernzielzuordnung;
- doppelte ID oder unbekannte Referenz;
- ungültiges Abschnitts-/Graphprofil;
- nicht erreichbares Rätsel oder Erfolgsziel;
- nicht unterstützter Rätsel-, Inhalts-, Bild-/Datei- oder Ort-/Gerät-Typ;
- inkompatibler Ort/Gerät-Typ;
- fehlendes oder unsicheres Bild / fehlende oder unsichere Datei;
- Bild ohne Alternativbeschreibung oder Dekorativ-Kennzeichnung;
- notwendiger Bildinhalt ohne begleitenden Textinhalt;
- unbekannte Formatversion.

## Deterministische Warnungen

- schwieriges Rätsel ohne Hilfe;
- Lernziel wird von keinem Rätsel referenziert;
- geschätzter kritischer Pfad passt schlecht zum Zeitlimit;
- sehr lange Texte;
- Bild ist nicht verwendet;
- Entwurf wurde seit der letzten Finalisierung verändert;
- kein Playtest im lokalen Draft protokolliert.

## Redaktionelle Selbstprüfung

Diese Fragen werden als Checkliste gestellt, nicht als automatisch erkannte
Probleme:

- Lässt sich die Lösung aus Aufgabe und Material wirklich ableiten?
- Trägt jedes Rätsel sinnvoll zu seinem Lernziel bei?
- Sind Schwierigkeit und Sprache für die Zielgruppe passend?
- Ist die Reflexionsfrage für die Nachbesprechung brauchbar?

Der UI-Draft kann lokal ein Playtest-Protokoll mit Datum, Testgruppe,
Ergebnis und Notizen speichern. Dieses Protokoll gehört nicht in `deer.json`.

## Barrierefreiheits-Baseline

- WCAG 2.2 AA als Ziel für die Authoring-UI;
- alle Funktionen per Tastatur;
- Button-Alternative zu Drag-and-drop;
- sichtbarer Fokus und logische Fokusreihenfolge;
- Labels und Fehler programmatisch verknüpft;
- Status nicht nur durch Farbe;
- Fokus auf Fehlerzusammenfassung nach Prüfung;
- Zoom/Reflow und Reduced Motion;
- verständliche Dateiauswahl und Bildbeschreibung.

Diese Zusage gilt nicht automatisch für die LibGDX-Spielruntime. Informative
Bilder dürfen im Foundation-Raum deshalb nicht der einzige Träger einer
notwendigen Information sein; die Beschreibung bleibt in der Generatorausgabe
erhalten und wird vom Foundation-Renderer als Textalternative angeboten, wo
die Runtime dies unterstützt.
