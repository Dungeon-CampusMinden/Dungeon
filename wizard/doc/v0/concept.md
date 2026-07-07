# Wizard Concept V0

Status: öffentliches V0-Konzept
Stand: 06.07.2026

## Ziel

Der Wizard ist eine separate Authoring-Web-App für nicht-technische Lehrende.
Er führt durch die Erstellung eines einfachen Educational Escape Rooms und
erzeugt nach erfolgreicher Validierung ein teilbares DEER-Authoring-Bundle
`deer.zip`.

Die `deer.json` ist das interne Authoring-Modell und die Eingabe für den
Generator. Das `deer.zip` ist nur der V0-Transportcontainer für diese
Konfiguration und die referenzierten Custom Assets:

```text
Wizard UI -> interne deer.json + Assets -> deer.zip -> manueller Java-Generator -> Room-Paket
```

Der Wizard erzeugt damit noch kein spielbares Room-Paket. Dieses entsteht erst
im Java-Generator.

## V0-Scope

V0 umfasst:

- Rahmenbedingungen: Titel, Sprache, Zielgruppe, Vorwissen, Spielerzahl,
  Zeitlimit und Zeitmodus.
- Szenario: Rolle, Ausgangslage, Mission, Intro, Erfolg und Fehlschlag.
- Rätselablauf: strukturierter Ablauf mit Reihenfolge und Parallelgruppen.
- Rätselbausteine: aktuell aus The Last Hour ableitbare Mechaniken.
- Inhalte: Texte direkt im Wizard, Bilder und Audio als Upload.
- Validierung: Pflichtdaten, Asset-Referenzen, unterstützte Bausteine,
  Ablauf, Softlocks, unerreichbare Rätsel und ungewollte Skips.
- Export: `deer.zip` als DEER-Authoring-Bundle nach erfolgreicher Validierung.

Nicht Teil von V0 sind spielbare Vorschau, Neu-Generieren, automatischer
Generator-Start, Evaluation/Telemetrie/Debriefing, Lernzielverwaltung, mehrere
Themes, ein Zwischeneditor nach dem Generator und Paketierung jenseits von
`deer.zip` als DEER-Authoring-Bundle.

## Authoring-Modell

Lehrende bauen den Escape Room von Grund auf neu. The Last Hour liefert
verfügbare Spielbausteine und wiederverwendbare Assets, aber keine
vorausgewählte Raumstruktur.

Oberflächen wie Computer, Keypad, Tür, Weltobjekt oder Fragmentbereich
entstehen aus den gewählten Bausteinen. Die UI kann diese Oberflächen sichtbar
benennen und fachlich konfigurieren; Lehrende planen keine technische
Slot-Liste.

Intern schreibt der Wizard die Oberflächen in ein `surfaces`-Register. Rätsel
referenzieren konkrete Oberflächen über `surfaceId`, damit UI und Generator
validierbare Verweise verwenden.

## V0-Bausteine

- `state_change`: einfache Weltaktion, z. B. Stromschalter.
- `collection`: Item, Hinweis oder Ressource finden.
- `input`: Zahlen-, Text-, Login- oder Decoding-Eingabe.
- `choice`: richtige Option auswählen, z. B. E-Mail oder URL.
- `item_use`: bestimmtes Item an einem Ziel verwenden, z. B. USB am PC.
- `assembly`: Fragmente zusammensetzen.
- `control_panel`: wiederverwendbare UI mit mehreren Controls.

## Ablauf Und Validierung

Die UI bildet den Rätselablauf als Reihenfolge mit optionalen Parallelgruppen
ab. Die konkrete Darstellung ist frei, solange daraus ein eindeutiger,
validierbarer Ablauf entsteht.

Blockierend sind Game-Breaking-Probleme:

- ein Rätsel ist nicht erreichbar,
- ein Progressionsrätsel kann übersprungen werden,
- ein benötigtes Ergebnis wird nie erzeugt,
- eine Abhängigkeit ist zyklisch oder unlösbar,
- ein Endzustand ist nicht erreichbar,
- Pflichtparameter oder Pflichtassets fehlen,
- ein Baustein wird mit einer inkompatiblen Oberfläche kombiniert,
- ein verwendeter Baustein ist im aktuellen Generator-Slice nicht generierbar.

Warnungen unterstützen die Qualität, blockieren aber nicht die Bundle-Erstellung.

## V0-Umsetzungsziel

Der UI-Prototyp macht die Schritte aus `wizard-ui-flow-v0.md` bedienbar,
erzeugt daraus eine schema-valide interne `deer.json` und packt ein
validiertes `deer.zip` als Authoring-Bundle. Der Generator wird in V0 manuell
mit diesem Bundle oder dem entpackten Projektordner gestartet und erzeugt erst
danach das Room-Paket.

Für die Umsetzung bleibt `./wizard` der Projekt-Workspace. Die
Konzeptdokumente liegen unter `./wizard/doc/v0`, damit der Root für Web-App,
Bundle-Output und Generator-Anbindung frei bleibt.
