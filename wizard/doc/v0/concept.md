# Wizard Concept V0.2

Status: öffentliches Foundation-Konzept
Stand: 09.07.2026

## Produktziel

Der Dungeon Wizard ist eine separate, lokal nutzbare Authoring-App für
nicht-technische Lehrende. Die Foundation-Referenz nutzt eine Web-Oberfläche in
einem Standalone-Host mit nativer Projektordner-Schnittstelle. Sie führt von
einer didaktischen Idee zu einer vollständigen, validierten DEER-Konfiguration,
ohne JSON, Petri-Netze, Runtime-Tokens oder Java-Code als Bedienkonzepte zu
zeigen.

Der Produktfluss trennt Authoring und Generierung:

```text
privater Wizard-Entwurf
-> validierte deer.json + referenzierte Assets
-> Java-Generator
-> generiertes Escape-Room-Modul als ZIP
-> Integration/Build
-> spielbarer Raum
```

Die UI erzeugt kein ZIP, keine `.level`-Datei und keinen Java-Code. Der
Java-Generator erzeugt das Modul-ZIP. Diese Grenze ist verbindlich.

## Rollen und Reifegrad

- **Lehrende Person:** erstellt, prüft und überarbeitet den Entwurf. Sie
  bearbeitet weder JSON noch Generatorparameter.
- **Technische Betreuung:** startet im Foundation-Slice den Java-Generator,
  integriert das generierte Modul und führt den Build aus.
- **Spielende:** nutzen ausschließlich das gebaute Escape Room-Ergebnis.

V0.2 ist damit ein **durchgängiger Contract- und Generator-Prototyp**, aber noch
kein vollständig lehrendenfertiges Endprodukt. Ein lehrendenfertiger Meilenstein
ist erst erreicht, wenn Generierung, Build und Start ohne Terminal- oder
Repositorywissen ausgelöst werden können.

## Begriffe und Artefakte

- **DEER:** Digital Educational Escape Room; hier zugleich das fachliche
  Authoring-Modell.
- **Wizard-Entwurf:** unvollständiger, UI-interner Arbeitsstand. Er wird lokal
  automatisch gespeichert und ist kein Generator-Contract.
- **`deer.json`:** vollständige, schema- und fachlich validierte
  Authoring-Konfiguration.
- **Projektordner:** `deer.json` plus referenzierte Dateien unter
  `assets/custom/`; dies ist die Generator-Eingabe.
- **Generator-ZIP:** vom Java-Generator erzeugtes Escape-Room-Modul mit
  Quellcode, Assets, Build-Datei, Manifest und Validierungsbericht.
- **Spielbares Ergebnis:** das nach Integration und Build gestartete Modul.

## Wissenschaftliche Leitplanken

Das Konzept nutzt die lokale
[Quellenübersicht](../research/source-notes.md). Für V0.2 sind drei
Konsequenzen wichtig:

1. Zielgruppe, Lernziel, Aktivität und erwartetes Lernergebnis müssen
   miteinander verbunden sein
   (`biggs1996constructive`, `clarke2017escaped`, `botturi2020star`).
2. Ein geführter Authoring-Prozess soll die Komplexität der Engine vor
   nicht-technischen Autorinnen und Autoren verbergen
   (`laurent2022authoring`, `roungas2016model`).
3. Statische Validierung ersetzt weder Playtesting noch didaktische Reflexion
   (`fotaris2022room2educ8`, `sanchez2019debriefing`).

Die vollständige Lernziel-Evidenz-Kette, Evaluation, Telemetrie und
LMS-Anbindung bleiben spätere Ausbaustufen. Ein explizites Lernziel ist jedoch
bereits Pflicht, damit der Foundation-Slice tatsächlich einen Educational
Escape Room beschreibt.

## V0.2-Scope

V0.2 umfasst:

- Projekt- und Sitzungsdaten: Titel, Inhaltssprache, Zielgruppe, Vorwissen,
  Spielerzahl und Zeitlimit.
- Lernkontext: mindestens ein Lernziel; jedes Rätsel verweist auf mindestens
  ein Lernziel; optionale Reflexionsfragen für die Nachbesprechung.
- Szenario: Rolle, Ausgangslage, Mission, Intro, Erfolg und Fehlschlag.
- Ablauf: geordnete Abschnitte; mehrere Rätsel in einem Abschnitt sind
  parallel, aber alle Pflichträtsel bleiben notwendig.
- Foundation-Bausteine:
  - **Fund:** Text- oder Bildinhalt an einem Ort finden.
  - **Zahlencode:** einen Code an einem Keypad eingeben.
- Inhalte: Inline-Text, optionale PNG-/JPEG-Dateien und auf Anfrage
  nacheinander angezeigte Hilfen.
- Entwurfsprüfung: Schema, Referenzen, unterstützte Bausteine, Graphstruktur,
  Pfadsicherheit und Generatorfähigkeit.
- Vorschau: nicht-spielbare Zusammenfassung von Story, Ablauf, Aufgaben,
  Materialien und Hilfen.
- Finalisierung: sicheres Schreiben im Projektordner; neue
  inhaltsadressierte Assets werden zuerst geschrieben, `deer.json` zuletzt
  ersetzt.

Nicht Teil des Foundation-Slices sind:

- freie Graphbearbeitung oder optionale Alternativpfade,
- Themes, Musik, Audio, Computer, E-Mail, USB, Assembly oder Control Panel,
- zeit- oder versuchsabhängige Hint-Freischaltung,
- integrierte 3D-Vorschau,
- browser-only Deployment ohne nativen Projektordner-Adapter,
- automatischer UI-Aufruf des Java-Generators,
- Ein-Klick-Spielerpaket, Installer oder Launcher,
- formale Lernstandsdiagnostik, Telemetrie oder LMS-Integration.

## Sichtbarer Authoring-Flow

```text
Starten oder fortsetzen
-> Eckdaten & Lernziel
-> Geschichte
-> Spielablauf
-> Rätsel, Inhalte & Hilfen
-> Prüfen, Vorschau & finalisieren
```

Eine Übersicht mit Fortschritt und Problemen bleibt ständig erreichbar; sie ist
kein zusätzlicher Arbeitsschritt. Technische Oberflächen werden beim Bearbeiten
eines Rätsels als „Ort oder Gerät“ abgefragt und intern abgeleitet.

Der sichtbare Ablauf ist eine Liste geordneter Abschnitte. Der Wizard erzeugt
daraus einen azyklischen Graphen mit Start, genau einem Knoten pro Rätsel und
einem gemeinsamen Erfolgsziel. Mehrere eingehende Kanten bedeuten **alle
Vorgänger erforderlich**. V0.2 kennt keine OR-Verzweigungen.

## Erfolg und Fehlschlag

Der Rätselgraph besitzt genau ein gemeinsames **Erfolgsziel**. Im
Foundation-Slice wird es erreicht, wenn alle Pflichträtsel abgeschlossen sind,
die Ausgangstür freigegeben wurde und alle aktiven Spielenden den Ausgang
erreicht haben.

Ein hartes Zeitlimit kann die Sitzung getrennt davon als fehlgeschlagen
beenden. Dies ist kein zweites Graphende. Ein weiches Zeitlimit beendet den
Raum nicht.

## Validierung und Qualitätsgrenze

Blockierende Prüfungen garantieren nur:

- syntaktisch gültige und vollständige Konfiguration,
- eindeutige IDs und gültige Referenzen,
- einen azyklischen, erreichbaren Ablauf innerhalb des V0.2-Graphprofils,
- passende Rätsel-, Ort-/Gerät- und Assetkombinationen,
- ausschließlich vom aktuellen Generator unterstützte Werte,
- sichere, vorhandene Projektpfade.

Sie können nicht beweisen, dass ein Hinweis verständlich, eine Lösung aus dem
Material ableitbar, die Schwierigkeit passend oder das Lernziel erreicht ist.
Diese Punkte erscheinen als redaktionelle Prüffragen und müssen durch
Playtesting bewertet werden.

## Generator-Leitplanken

Der Generator:

- behandelt `deer.json` und Assets als unveränderliche Eingabe,
- validiert Schema und Fachregeln erneut,
- erzeugt standardmäßig deterministisch aus kanonischem Input und Asset-Hashes,
- dokumentiert Generatorversion, Input-Hash, Seed und Layoutprofil im Manifest,
- erzeugt Geometrie, Slots, Runtime-Zustände und Java-Adapter,
- schreibt das Modul-ZIP erst nach erfolgreicher Validierung vollständig,
- erzeugt gemeinsame Progression serverautoritativ und multiplayer-sicher.

Der Foundation-Generator braucht insbesondere generische Adapter für
Fundabschluss, Keypadabschluss, Graphaktivierung, Ausgang und Timer. Diese
Abstraktionen existieren im aktuellen Runtime-Code noch nicht als
durchgängiger Generator-Layer und sind Teil des Foundation-Slices.

## Definition des Foundation-Erfolgs

Der Slice gilt erst als abgeschlossen, wenn ein Entwurf:

1. im Wizard erstellt, gespeichert, wieder geöffnet und finalisiert werden kann,
2. als `deer.json` das Schema und alle Fachregeln erfüllt,
3. vom Java-Generator reproduzierbar in ein Modul-ZIP übersetzt wird,
4. als generiertes Modul mit dem realen Gradle-Setup baut,
5. lokal und im Multiplayer denselben gemeinsamen Fortschritt zeigt,
6. im Beispiel „Fund -> Zahlencode -> Ausgang“ vollständig spielbar ist,
7. nach einem Playtest im ursprünglichen Wizard-Entwurf überarbeitet und erneut
   generiert werden kann.
