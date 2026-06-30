# Wizard UI Flow V0 Draft

Stand: 30.06.2026  
Status: UI- und Eingabevorschlag zur Diskussion

## Ziel

Dieses Dokument beschreibt, wie der Wizard fuer Lehrende aussehen soll und
welche Angaben in V0 wirklich gemacht werden muessen. Generator-Details,
Petri-Netze, Tokens und technische Runtime-Interna sollen in der UI nicht im
Vordergrund stehen.

Der Wizard soll Lehrende durch eine strukturierte Authoring-Oberflaeche fuehren:

```text
Rahmen festlegen
-> Szenario beschreiben
-> Raetselbausteine und daraus entstehende Oberflaechen planen
-> Raetselablauf konfigurieren
-> Inhalte, Assets und Hinweise ergaenzen
-> Validieren und deer.json exportieren
```

## UI-Grundsaetze

- Der Wizard ist eine separate Browser-/Standalone-Oberflaeche.
- Lehrende bearbeiten keine JSON-Datei direkt.
- Technische Begriffe wie Token, Petri-Netz oder Generator-Action werden in der
  UI durch fachliche Begriffe ersetzt.
- Der Export-Button ist erst aktiv, wenn der Client-Preflight
  gueltig ist.
- Fehler werden am betroffenen Schritt, Raetsel oder Feld angezeigt.
- Warnungen duerfen sichtbar bleiben, blockieren aber nicht.
- V0 nutzt ein Standard-Theme; Custom-Themes, Tilesets, Sprites und UI-Skins
  werden nicht abgefragt.
- V0 fragt keine Lernziele, Evaluation, Debriefing, Telemetrie oder
  Pre-/Post-Tests ab.
- Der Wizard startet ohne vorausgewaehlten Raum. Lehrende bauen den Escape Room
  von Grund auf neu.
- V0 bietet nur die aktuell vorhandenen, aus The Last Hour ableitbaren
  Spiel-Elemente als Bausteine an.

## Hauptnavigation

Empfohlene Schritte:

1. **Uebersicht**
2. **Rahmen**
3. **Szenario**
4. **Raum & Oberflaechen**
5. **Raetselablauf**
6. **Raetsel bearbeiten**
7. **Inhalte & Assets**
8. **Pruefen & Exportieren**

Die linke Navigation sollte jeden Schritt mit Status markieren:

- `leer`
- `unvollstaendig`
- `gueltig`
- `Warnung`
- `Fehler`

## 1. Uebersicht

Zweck: Projektstatus sichtbar machen und die naechsten offenen Aufgaben zeigen.

Pflichtangaben: keine.

Anzeigen:

- Raumtitel
- Fortschritt der Wizard-Schritte
- Anzahl Raetsel
- offene Pflichtfelder
- blockierende Fehler
- Warnungen
- letzter gueltiger Preflight-Status

Aktionen:

- weiter zum naechsten offenen Schritt
- Entwurf speichern
- `deer.json` exportieren, wenn alles gueltig ist

## 2. Rahmen

Zweck: Allgemeine Sitzungsdaten, die der Raum braucht.

Pflichtfelder:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Raumtitel | `metadata.title` | nicht leer |
| Sprache | `metadata.locale` | V0: `de-DE` oder `en-US` |
| Zielgruppe | `session.targetAudience` | nicht leer |
| Vorwissen | `session.priorKnowledge` | darf kurz sein, aber nicht leer |
| Spielerzahl min/max/empfohlen | `session.playerCount` | `1 <= min <= recommended <= max` |
| Zeitlimit | `session.timeLimitMinutes` | positive Zahl |
| Erwartete Dauer | `session.durationMinutes` | positive Zahl |
| Kooperationsmodus | `session.collaborationMode` | V0: `single_player` oder `cooperative` |

Nicht sichtbar oder fest:

- Theme: V0 immer Standard-Theme.
- Levelanzahl: V0 immer ein Level.

## 3. Szenario

Zweck: Story-Rahmen, damit der Raum nicht wie eine Vorlesungsfolie im Spiel
wirkt.

Pflichtfelder:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Rolle der Spielenden | `scenario.playerRole` | nicht leer |
| Ausgangslage | `scenario.premise` | kurzer Fliesstext |
| Mission | `scenario.mission` | klares Ziel |
| Intro-Text | `scenario.introText` | nicht leer |
| Erfolgstext | `scenario.successText` | nicht leer |
| Fehlschlagtext | `scenario.failureText` | nicht leer |

Optionale Felder:

- ein bis drei Lore-Texte
- optionales Lore-Bild
- optionales Intro-/Ambient-Audio

Client-Warnungen:

- Text sehr lang
- Mission ist unklar formuliert
- Intro beschreibt nur Fachinhalt, aber keine Spielsituation

## 4. Raum & Oberflaechen

Zweck: Sichtbar machen, welche Interaktionsorte aus den gewaehlten Bausteinen
entstehen. Lehrende sollen keine technische Slot-Struktur vorab planen.

V0 startet nicht mit vorausgewaehlten Oberflaechen. Oberflaechen werden aus den
gewaehlten Raetselbausteinen abgeleitet. Wenn Lehrende z. B. ein
Computer-Login-Raetsel anlegen, erzeugt die UI daraus einen benoetigten Computer
und fragt nur noch die fachlich sichtbaren Eigenschaften ab.

Moegliche abgeleitete Oberflaechen:

| Oberflaeche | Sichtbarer Name | Zweck |
|---|---|---|
| `computer_main` | Labor-PC | Login, E-Mails, Browser, Dateien, USB, Control Panel |
| `keypad_storage` | Storage-Keypad | Zahlencode fuer Storage |
| `door_storage` | Storage-Tuer | durch Keypad oder Control Panel oeffnen |
| `door_exit` | Ausgangstuer | final oeffnen |
| `vent_main` | Lueftung | Seriennummer und Papierfragmente |
| `trash_slots` | Papierkoerbe | Funde und Trash-Minispiel |
| `container_slots` | Container/Schreibtische/Regale | Hinweise und Items |
| `assembly_area` | Fragmentbereich | Bildfragmente zusammensetzen |

Pflichtangaben entstehen aus den gewaehlten Raetseln:

| UI-Feld | Bedeutung | Validierung |
|---|---|---|
| mindestens ein Computer | fuer computernahe Raetsel | vorhanden, wenn Computer-Raetsel genutzt werden |
| mindestens ein Keypad-Slot | fuer Keypad-Raetsel | vorhanden, wenn `input.numeric` als Keypad genutzt wird |
| mindestens ein Container/Fundort | fuer `collection` | vorhanden, wenn Fund-Raetsel genutzt werden |
| mindestens ein Assembly-Bereich | fuer Fragmente | vorhanden, wenn `assembly.image_fragments` genutzt wird |

Lehrende waehlen primaer Bausteine wie "Computer-Login", "Keypad" oder
"Control Panel". Die UI leitet daraus die benoetigten Oberflaechen ab und zeigt
sie zur Kontrolle an. Wenn eine Eingabe wie Passwort oder Code benoetigt wird,
kann die UI anbieten, den Wert automatisch vorzuschlagen oder manuell
festzulegen.

## 5. Raetselablauf

Zweck: Festlegen, welche Raetsel in welcher Reihenfolge geloest werden muessen.

Empfohlenes UI-Modell:

- strukturierte Ablauf-Liste mit optionalen Parallelgruppen
- jedes Raetsel als Karte
- Abhaengigkeiten als "danach freigeschaltet"
- keine sichtbaren Token-Namen fuer Lehrende
- ein Canvas kann spaeter eine alternative Visualisierung sein, sollte aber
  nicht die erste Bedienlogik erzwingen

Pflichtangaben pro Knoten:

| UI-Feld | Interne Bedeutung | Validierung |
|---|---|---|
| Raetselname | `riddle.title` | eindeutig genug |
| Baustein-Typ | `riddle.type` | V0-Typ |
| Kurzaufgabe | `playerFacingTask` | nicht leer |
| Vorgaenger | `requiresTokens` indirekt | darf keinen Zyklus erzeugen |
| Ergebnis/Freischaltung | `producesTokens` indirekt | muss zu spaeterem Schritt passen |

V0-Regeln:

- Alle Progressionsraetsel muessen auf einem durchspielbaren Pfad liegen.
- Keine optionalen Raetsel.
- Branches duerfen Parallelitaet ausdruecken, aber keine Raetsel ueberspringen.
- Der Wizard zeigt Fehler sofort im Graphen.
- Der Editor verhindert oder markiert Zyklen, unerreichbare Knoten und
  Abhaengigkeiten, die erst nach dem benoetigten Raetsel verfuegbar werden.
- Frei editierbar bedeutet in V0 nicht beliebig: Der Graph darf kreativ
  angeordnet werden, bleibt aber durch Validierungsregeln eingeschraenkt.

## 6. Raetsel Bearbeiten

Zweck: Die konkreten Eingaben fuer jedes Raetsel erfassen. Die UI zeigt nur die
Felder, die zum gewaehlten Baustein passen.

### 6.1 Stromschalter / `state_change.confirm`

Pflichtfelder:

- sichtbarer Name
- Aufgabe fuer Spielende
- Oberflaeche/Fundort, meist `world`
- Zielobjekt, z. B. Schalter
- Bestaetigungsfrage
- Erfolgstext
- Freischaltung aus kontrollierter Auswahl, z. B. "Computer einschalten"

Optionale Felder:

- Abbrechen-Text
- Sound

### 6.2 Fund / `collection`

Pflichtfelder:

- sichtbarer Name
- Aufgabe fuer Spielende
- Fundtyp: Container, Weltobjekt, Papierkorb-Minispiel, Computer-Datei
- Fundort/Oberflaeche
- Reward oder Ressource
- Freischaltung aus kontrollierter Auswahl

Zusaetzlich bei Papierkorb-Minispiel:

- Anzahl Papierobjekte
- Asset fuer gefundenes Objekt

### 6.3 Computer-Login / `input.credentials`

Pflichtfelder:

- Computer-Oberflaeche
- Feldliste
- je Feld: Label, erwarteter Wert, geheim ja/nein
- Erfolg: Computer-Tabs freischalten

V0-Default fuer The Last Hour:

- Feld 1: E-Mail
- Feld 2: Passwort

### 6.4 E-Mail-Auswahl / `choice.email_list`

Pflichtfelder:

- Computer-Oberflaeche
- Liste von E-Mails
- mindestens zwei Optionen
- genau eine korrekte Option
- pro E-Mail: Absender, Absenderadresse, Betreff, Inhalt
- bei Link-Aufgaben: Linktext und URL
- Erfolg: Recovery-/Browser-Seite freischalten

V0-Entscheidung:

- bevorzugt als Computer-Tab
- einfacher Dialog nur als technischer Fallback

### 6.5 Decoding-Eingabe / `input.decoded_text`

Pflichtfelder:

- Oberflaeche, meist Computer
- kodierter Wert
- erwartete Antwort
- Decoding-Schritte, z. B. Binary -> Hex -> ASCII
- Ressourcen, die die Decoding-Schritte erklaeren
- Erfolg: Dokument oder naechstes Raetsel freischalten

### 6.6 Keypad / `input.numeric`

Pflichtfelder:

- Keypad-Oberflaeche
- erwarteter Zahlencode
- maximale Laenge
- Erfolg: Tuer oeffnen oder Bereich freischalten

Optionale Felder:

- falsche Eingabe Feedback
- Ziffernanzahl anzeigen ja/nein

### 6.7 USB Verwenden / `item_use`

Pflichtfelder:

- Computer-Oberflaeche
- Zielobjekt, meist PC
- Liste verfuegbarer USB-Sticks
- welcher Stick korrekt ist
- Verhalten bei falschem Stick
- Erfolg: USB-Laufwerk oder Control-Panel-Zugang freischalten

The-Last-Hour-Default:

- mehrere farbige USB-Sticks
- blauer USB ist korrekt
- falscher USB erzeugt `Unknown Device`
- nach kurzer Zeit Reset auf eingeschalteten, ausgeloggten PC
- erneuter Versuch bleibt moeglich

### 6.8 Control Panel / `control_panel`

Pflichtfelder:

- Computer-/Panel-Oberflaeche
- Liste der Controls
- Abschlusszustand
- Freischaltung bei Abschluss

Pflicht pro Control:

- Label
- Typ: Button, Toggle, Textfeld, Passwortfeld
- erwarteter Wert, falls Eingabefeld
- gesetzter interner Zustand
- benoetigte vorherige Panel-Zustaende, falls vorhanden

The-Last-Hour-nahe Controls:

- Vent-Seriennummer eingeben
- AC einschalten
- finale Tuer mit Passwort entsperren
- finale Tuer oeffnen

### 6.9 Bildfragmente / `assembly.image_fragments`

Pflichtfelder:

- Ausgangsbild
- Anzahl Fragmente
- Spawn-/Startausloeser, z. B. AC eingeschaltet
- Ergebnis-Ressource, z. B. finales Code-Bild
- Erfolg: finale Information verfuegbar machen

V0 nutzt das vorhandene Puzzle-/Item-System.

## 7. Inhalte & Assets

Zweck: Alle Texte, Bilder und Audio-Dateien an einer Stelle verwalten.

Pflichtbereiche:

| Bereich | Pflicht, wenn... |
|---|---|
| Texte | ein Raetsel Text/Lore/Ressource nutzt |
| Bilder | eine Ressource oder Assembly ein Bild nutzt |
| Audio | nur wenn Audio in Szenario oder Feedback aktiviert ist |
| Hinweise | optional, aber pro Raetsel als leeres Array vorhanden |

V0-erlaubt:

- Text direkt im Wizard
- Bilder als Upload
- Audio als Upload

Nicht V0:

- Themes
- Tilesets
- Sprites
- UI-Skins
- beliebige Office-/PDF-Dateien als Runtime-Dokumente

## 8. Pruefen & Exportieren

Zweck: Lehrende sehen vor dem JSON-Export eine klare, nicht-technische
Checkliste.

Blockierende Fehler:

- Pflichtfeld fehlt
- Raetsel ohne Fundort/Oberflaeche
- benoetigte Ressource fehlt
- Asset fehlt
- Raetsel im Ablauf nicht erreichbar
- Progression kann nicht abgeschlossen werden
- Progressionsraetsel kann uebersprungen werden
- Softlock oder zyklische Abhaengigkeit
- Aktion passt nicht zur gewaehlten Oberflaeche
- Computer-Raetsel ohne Computer
- Keypad-Raetsel ohne Keypad

Warnungen:

- sehr lange Texte
- Raetsel ohne Hinweise
- viele Raetsel ohne klare Story-Einbettung
- erwartete Dauer deutlich hoeher als Zeitlimit

Hauptaktion:

- `deer.json exportieren`

Der Button ist deaktiviert, solange blockierende Fehler existieren.

## Lehrenden-Sicht Auf The Last Hour V0

Der Wizard soll nicht automatisch eine The-Last-Hour-Vorlage anlegen. Diese
Liste beschreibt nur, welche The-Last-Hour-nahen Bausteine in V0 verfuegbar sein
sollten, damit ein sinngemaesser Nachbau moeglich ist:

1. Strom einschalten
2. Login-Hinweise finden
3. Computer-Login
4. richtige E-Mail erkennen
5. Recovery-Code decodieren
6. Storage-Code aus Dokument entschluesseln
7. Storage-Keypad oeffnen
8. richtigen USB-Stick finden
9. USB am PC verwenden
10. Vent-Seriennummer im Control Panel eintragen
11. AC einschalten
12. Bildfragmente zusammensetzen
13. finale Tuer oeffnen

Lehrende bauen den Raum selbst aus diesen Bausteinen zusammen. Die UI darf
Vorschlaege, Beispiele oder leere Baustein-Karten anbieten, aber keine fertige
Raumstruktur vorauswaehlen.

## Aktuelle Entscheidungen

1. V0 startet leer. Es gibt keine vorausgewaehlte The-Last-Hour-Vorlage.
2. V0 nutzt nur die aus The Last Hour ableitbaren Spiel-Elemente als
   verfuegbare Bausteine.
3. Der Computer ist kein eigener Hauptschritt, sondern eine Surface, an die
   Raetsel oder Informationen gebunden werden koennen.
4. Lehrende waehlen Oberflaechen und Bausteine aus. Werte wie Passwort oder Code
   koennen automatisch vorgeschlagen oder manuell angegeben werden.
5. Die erste sichtbare Abschlussaktion ist der Export einer validen
   `deer.json`, nicht der fertige `deer.zip`-Export.
6. Blockierend sind nur game-breaking Fehler: Softlocks, unerreichbare Raetsel,
   ungewollte Skips, fehlende Pflichtdaten und inkompatible Baustein-/Surface-
   Kombinationen.

## Noch Zu Klaeren

1. Welche Graph-Operationen sind in V0 erlaubt: Karte verschieben, Abhaengigkeit
   ziehen, Parallelgruppe erstellen, Reihenfolge per Drag-and-drop?
2. Welche konkrete Komponente nutzt der UI-Prototyp fuer die strukturierte
   Ablauf-Liste?
