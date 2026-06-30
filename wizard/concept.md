# Wizard Concept V0

Status: aktueller Kurzstand nach Scope-Bereinigung  
Stand: 30.06.2026

## Ziel

Der Wizard ist eine separate, nicht-technische Authoring-Oberflaeche fuer
Lehrende. Er fuehrt durch die Erstellung eines einfachen Educational Escape
Rooms und exportiert als erstes Ergebnis eine valide `deer.json`.

Die `deer.json` ist das Authoring-Modell:

```text
Wizard UI -> deer.json -> spaeter: Java-Generator -> spielbares Dungeon-Paket
```

## V0-Scope

V0 konzentriert sich auf:

- Rahmenbedingungen: Titel, Sprache, Zielgruppe, Vorwissen, Spielerzahl,
  Dauer und Zeitlimit.
- Szenario: Rolle, Ausgangslage, Mission, Intro, Erfolg und Fehlschlag.
- Raetselablauf: strukturierter Ablauf mit erlaubten Parallelgruppen statt
  beliebig freiem Graph fuer Lehrende.
- Raetselbausteine: alle aktuell aus The Last Hour ableitbaren Mechaniken.
- Inhalte: Texte direkt im Wizard, Bilder und Audio als Upload.
- Validierung: keine Softlocks, keine unerreichbaren Raetsel, keine ungewollten
  Skips, keine fehlenden Pflichtparameter oder Assets.
- Export: `deer.json`.

Nicht V0:

- Lernziele,
- Evaluation,
- Debriefing,
- Telemetrie,
- Pre-/Post-Tests,
- mehrere Themes oder Custom-Themes,
- Zwischeneditor nach dem Generator,
- fertiger `deer.zip`-Export als erste UI-Pflicht.

## Authoring-Modell

Lehrende bauen den Escape Room von Grund auf neu. The Last Hour liefert nur die
aktuell vorhandenen Spielbausteine und wiederverwendbare Assets, aber keine
vorausgewaehlte Raumstruktur.

Oberflaechen wie Computer, Keypad, Tuer, Weltobjekt oder Fragmentbereich
entstehen aus den gewaehlten Bausteinen. Die UI darf diese Oberflaechen sichtbar
benennen und bei Bedarf konfigurieren, aber Lehrende sollen keine technische
Slot-Liste vorab planen muessen.

## V0-Bausteine

- `state_change`: einfache Weltaktion, z. B. Stromschalter.
- `collection`: Item, Hinweis oder Ressource finden.
- `input`: Zahlen-, Text-, Login- oder Decoding-Eingabe.
- `choice`: richtige Option auswaehlen, z. B. E-Mail/URL.
- `item_use`: bestimmtes Item an einem Ziel verwenden, z. B. USB am PC.
- `assembly`: Fragmente zusammensetzen.
- `control_panel`: wiederverwendbare UI mit mehreren Controls.

## Graph-Regeln

Die UI sollte bevorzugt eine strukturierte Darstellung mit Reihenfolge und
Parallelgruppen anbieten. Ein Canvas kann spaeter als alternative Visualisierung
dienen, sollte aber nicht die erste Bedienlogik erzwingen.

Blockierend sind nur Game-Breaking-Probleme:

- ein Raetsel ist nicht erreichbar,
- ein Raetsel kann uebersprungen werden, obwohl es Progression ist,
- ein benoetigtes Ergebnis wird nie erzeugt,
- eine Abhaengigkeit ist zyklisch oder unloesbar,
- ein Endzustand ist nicht erreichbar,
- Pflichtparameter oder Pflichtassets fehlen,
- ein Baustein wird mit einer inkompatiblen Oberflaeche kombiniert.

Warnungen duerfen helfen, sollen aber den Export nicht blockieren.

## Naechster Praktischer Schritt

Der naechste sinnvolle Schritt ist ein UI-Prototyp, der die Schritte aus
`wizard-ui-flow-v0.md` bedienbar macht und daraus eine schema-valide
`deer.json` exportiert.
