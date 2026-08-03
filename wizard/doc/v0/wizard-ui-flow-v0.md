# Wizard UI Flow V0.3

Status: verbindlicher V0.3-Zielvertrag; Implementierung folgt separat
Stand: 27.07.2026

## Ziel

Dieses Dokument definiert den sichtbaren Authoring-Flow. Feldsemantik und
Runnerregeln stehen in den verlinkten Contract-Dateien; sie werden hier
nicht vollständig wiederholt. Auftrag, Schichtgrenzen, Storage-Port und
Frontend-Abnahme stehen im
[`Frontend-Handoff`](frontend-handoff-overview-v0.md).

```text
Starten oder fortsetzen
-> Eckdaten
-> Geschichte
-> Spielablauf
-> Rätsel, Inhalte & Hinweise
-> Prüfen, Vorschau & finalisieren
```

## Grundsätze

- Lehrende bearbeiten keine JSON-Datei und keine technischen IDs.
- Der unvollständige Entwurf ist nicht `deer.json`.
- Eine ständig erreichbare Übersicht zeigt Navigation, Abschlussgrad,
  blockierende Probleme und Warnungen.
- Graphknoten und Kanten werden aus fachlichen Eingaben abgeleitet.
- Der sichtbare Ablauf ist eine geordnete Abschnittsliste, kein freier Graph.
- `Entwurf prüfen` ist immer aktiv; `Entwurf finalisieren` erst nach einer
  erfolgreichen Prüfung.
- Warnungen blockieren nicht.
- Eine Prüfung garantiert Struktur und Runnerfähigkeit, nicht menschliche
  Lösbarkeit oder Lernerfolg.

## Entwurfslebenszyklus

1. Ein neuer oder wieder geöffneter UI-Entwurf darf unvollständig sein.
2. Änderungen werden lokal automatisch gespeichert.
3. Stabile IDs werden bei der ersten Anlage erzeugt und bei Umbenennung
   beibehalten.
4. Die UI projiziert den Entwurf für jede vollständige Prüfung auf ein
   `deer.json`-Kandidatenobjekt.
5. Nur ein fehlerfreier Kandidat wird finalisiert. Bei der ersten erfolgreichen
   Finalisierung des Projekts erzeugt die UI genau einmal den verpflichtenden
   `seed` im `deer.json`-Kandidaten. Neue inhaltsadressierte Dateien werden
   zuerst geschrieben; `deer.json` wird zuletzt sicher ersetzt.
6. Der Entwurf bleibt danach bearbeitbar. Eine Änderung markiert die letzte
   Finalisierung als veraltet. Weitere Finalisierungen erhalten den bestehenden
   Seedwert unverändert.

V0.3 unterstützt die Wiederaufnahme eigener lokaler Entwürfe. Der Import
beliebiger Room-ZIPs oder manuell veränderter Projektordner ist nicht Teil des
Foundation-Slices. Der Produktfluss erzeugt selbst keine Room-ZIPs.

Der spezifizierte V0.3-Authoring-Host ist eine noch nicht umgesetzte
Standalone-App mit Web-Oberfläche und nativem Storage-Adapter. Ein browser-only
Host folgt erst nach einem eigenen Speicher-/Export-Slice.

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
Raumstruktur. Er erklärt lediglich „Information finden -> Zahlencode lösen ->
Ausgang“.

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

Die stabile `metadata.id` wird einmal aus dem Projekt angelegt und bei späteren
Umbenennungen beibehalten. Sie ist kein frei bearbeitetes Textfeld.

Optional sind `metadata.description`, `metadata.author`, weitere Lernziele und
Fragen für `learningDesign.debriefPrompts`. Lernziele und Nachbesprechung
werden nach `deer.json` projiziert und beeinflussen den Host-Input-Hash. Sie
steuern die Runtime nicht und werden vom aktuellen Runner nicht semantisch
bewertet.

V0.3 zeigt `de-DE` als einzige unterstützte Inhaltssprache und nicht als
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
Identität. Es gibt keinen separaten CLI-Wert für die Spielerzahl und keinen
Startknopf.

## 3. Geschichte

Pflichtangaben:

| Sichtbares Feld | DEER-Ziel |
|---|---|
| Mission | `scenario.mission` |
| Intro-Seiten | `scenario.introText` |
| Erfolgsseiten | `scenario.successText` |
| Fehlschlagseiten bei hartem Zeitlimit | `scenario.failureText`, nur bei `hard` |

V0.3 nutzt das feste Theme `default` und reine Storytexte. Die `themeId` bleibt
als Erweiterungspunkt für zukünftige Themes erhalten. Die drei Seitenfolgen
werden als geordnete, nicht leere Listen bearbeitet; jeder Eintrag entspricht
einer weiterklickbaren Black-Fade-Seite. Nach den Intro-Seiten erscheint die
Mission als hervorgehobene letzte Intro-Seite.

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
- Rätsel hinzufügen;
- Rätsel per Buttons nach oben/unten oder in einen anderen Abschnitt bewegen;
- optionales Drag-and-drop als zusätzliche Bedienung;
- Löschen und Verschieben rückgängig machen.

Die UI erzeugt intern:

- genau einen Startknoten;
- genau einen Rätselknoten pro Rätsel;
- reine AND-Kanten zwischen aufeinanderfolgenden Abschnitten;
- genau einen Endknoten.

Nicht darstellbar in V0.3:

- OR-Verzweigung;
- optionales Pflichträtsel;
- Zyklus;
- manuelle Start-/Endknoten;
- freie Kantenbedingungen.

## 5. Rätsel, Inhalte & Hinweise

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
kopiert. Für einen eigenen Upload behält der private Draft die Bildbytes. Beim
Finalisieren berechnet der native Adapter SHA-256, schreibt die Datei unter
`assets/custom/<12-hashzeichen>-<normalisierter-name>` und ersetzt erst danach
`deer.json` atomar. Die Auswahlvariante erscheint nicht als zusätzliches Feld
in den öffentlichen `source`-Metadaten.

Die UI leitet aus dem fachlichen Behälter eine stabile Container-Surface und die
`surfaceId` der Informationsquelle ab. Informationsquellen dürfen unabhängig
vom Status des zugehörigen Rätsels gelesen werden. Soll das Entdecken selbst
verpflichtend sein, fügt die UI dafür eine Collection-Eingabe hinzu. Diese
verwendet dieselbe Surface und wird erst durch eine Interaktion bei verfügbarem
Rätsel erfüllt; früheres Lesen wird nicht angerechnet. Andernfalls liefert die
Quelle nur Information oder Aufgabeninhalt. Die einmalige World-Surface
beschreibt ausschließlich den gemeinsamen Raum und ist keine Fundstation.

### Eingaben

Mindestens eine Eingabe ist Pflicht. V0.3 bietet geschlossen:

- **Zahlencode**;
- **Information entdecken**, wenn eine Informationsquelle zwingend gefunden
  werden muss.

Für einen Zahlencode sind Pflicht:

- ein fachliches Keypad;
- erwarteter Code mit 1 bis 8 Ziffern.

Optional:

- Ziffernanzahl im Spiel anzeigen.

Die Codelänge wird aus dem Code abgeleitet. Es gibt kein separates
`maxLength`-Feld. Falsche Eingaben bleiben im Foundation-Slice unbegrenzt
wiederholbar und nutzen das vorhandene Runtime-Feedback. Die UI leitet eine
stabile Keypad-Surface und die `surfaceId` der Eingabe ab.

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

Hinweise können in V0.3 angefordert werden, sobald das zugehörige Rätsel
verfügbar ist. Die Runtime kündigt vor jeder Freigabe die Stufe des nächsten
Hinweises an und verlangt eine ausdrückliche Bestätigung. Das gilt für alle
drei Stufen, damit auch eine Fehlinteraktion keinen Hinweis freigibt. Abbrechen
verändert weder den Freigabestatus noch die Position in der Reihenfolge. Nach
der Bestätigung zeigt die Runtime nur den nächsten Hinweis. Bereits
freigegebene Hinweise bleiben lesbar. Die Arrayreihenfolge ist unabhängig von
`severity` maßgeblich. Zeit- und Fehlversuchsbedingungen sind nicht Teil des
aktuellen Vertrags.

### Ausgang

Die UI fragt genau eine fachliche Ausgangstür ab und leitet daraus die stabile
Door-Surface sowie `end.surfaceId` ab. Der Endknoten besitzt diese Tür allein.
Wenn alle direkten Vorgängerrätsel des Endknotens abgeschlossen sind, wird das
Ende erreicht und die Tür serverautoritativ geöffnet. Erfolg tritt ein, wenn
die Runtime den Ausgang für alle aktiven Spielenden als erreicht meldet.

## 6. Prüfen, Vorschau & finalisieren

Die Vorschau zeigt ohne Runtime:

- Eckdaten;
- Intro-Seiten, Mission, Erfolgsseiten und bei hartem Zeitlimit
  Fehlschlagseiten;
- Abschnitte, Parallelität und erwartete Reihenfolge;
- Aufgaben, Materialien und Hinweise;
- verwendete Bilder.

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

> „Zahlencode“ hat noch keinen Code. Gib 1 bis 8 Ziffern ein.
> **Zum Rätsel**

`Entwurf finalisieren`:

- ist nur ohne blockierende Fehler aktiv;
- schreibt neue, inhaltsadressierte Dateien zuerst, erzeugt beim ersten
  erfolgreichen Abschluss genau einmal das `seed`-Feld und ersetzt
  `deer.json` zuletzt;
- erhält einen bereits vorhandenen projektgebundenen Seedwert bei jeder
  weiteren Finalisierung unverändert;
- zeigt den Zielordner und die nächsten Runner-Schritte für die technische
  Betreuung;
- startet den Runner nicht.

Reine private Entwurfsnotizen werden nicht finalisiert. Lernziele und
Nachbesprechungsfragen sind dagegen verbindlicher Bestandteil von `deer.json`.

Die Handoff-Hilfe nennt die Produktionsvalidierung und den noch extern
aufzurufenden Packager
`:wizard:buildWizardRoomJar -PwizardProject=<projektordner>`. Sie erklärt, dass
dieselbe vollständige JAR an alle Spielenden verteilt und mit Java 25 über ihr
Host-/Join-Menü gestartet wird. Die optionale Entwicklungs-CLI mit `validate`,
`host` und `join` bleibt dokumentiert. Die UI beschreibt keine
Multiplayerdetails; der vollständige Start- und Multiplayer-Ablauf steht im
[`Runner-Runtime-Contract`](runner-runtime-contract.md).

Bei Berechtigungs-, Speicherplatz- oder Schreibfehlern bleibt die vorherige
`deer.json` unverändert. Die UI bietet „Erneut versuchen“ und „Anderen Ordner
wählen“. Unbekannte oder alte unreferenzierte Dateien werden nicht gelöscht.

## Blockierende Prüfungen

- fehlende Pflichtangabe;
- doppelte ID oder unbekannte Referenz;
- ungültiges Abschnitts-/Graphprofil;
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
