# Wizard Concept V0

Status: öffentliches V0-Konzept
Stand: 07.07.2026

## Ziel

Der Wizard ist eine separate Authoring-Web-App für nicht-technische Lehrende.
Er führt durch die Erstellung eines einfachen Educational Escape Rooms und
erzeugt nach erfolgreicher Validierung eine `deer.json` mit referenzierten
Assets.

`deer.json` ist das interne DEER-Konfigurationsmodell und die Eingabe für den
Java-Generator. Die UI erzeugt kein spielbares Room-Paket und kein
Generator-ZIP.

```text
Wizard UI -> deer.json + assets/ -> manueller Java-Generator -> Room-Paket
```

## Glossar

- **DEER**: das Authoring-Format für einen erzeugbaren Escape-Room-Entwurf.
- **`deer.json`**: die validierte Authoring-Datei und das einzige UI-
  Contract-Artefakt.
- **Projektordner**: `deer.json` plus referenzierte Assets; dieser Ordner ist
  die manuelle Generator-Eingabe.
- **Room-Paket**: das vom Java-Generator erzeugte spielbare Runtime-Artefakt.
- **Generator-ZIP**: optionaler Transport-/Output-Container des Generators,
  nicht Aufgabe der Wizard-UI.

## V0-Scope

V0 umfasst:

- Rahmenbedingungen: Titel, Sprache, Zielgruppe, Vorwissen, Spielerzahl,
  Zeitlimit und Zeitmodus.
- Szenario: Rolle, Ausgangslage, Mission, Intro, Erfolg und Fehlschlag.
- Einfacher Rätselablauf: lineare Reihenfolge mit klarer Start- und
  Endbedingung.
- Foundation-Bausteine: `collection.single` und `input.numeric`.
- Inhalte: Texte direkt im Wizard, Bilder und Audio als referenzierte Assets.
- Validierung: Pflichtdaten, Asset-Referenzen, unterstützte Bausteine,
  Ablauf, unerreichbare Rätsel, ungewollte Skips und Softlock-Risiken.
- Abschluss: `deer.json` finalisieren und Projektordner für den manuellen
  Generatorlauf bereitstellen.
- Produktentscheidung: Ein Raum hat genau einen Endzustand. Auch spätere
  Versionen können verzweigte Wege haben, führen aber auf dieses eine Ende.

## Authoring-Modell

Lehrende bauen den Escape Room von Grund auf neu. The Last Hour liefert
verfügbare Spielbausteine und wiederverwendbare Assets, aber keine
vorausgewählte Raumstruktur.

Die UI benennt fachliche Oberflächen wie Fundort, Keypad oder Tür und schreibt
intern ein `surfaces`-Register. Rätsel referenzieren konkrete Oberflächen über
`surfaceId`, damit UI und Generator validierbare Verweise verwenden.

Der Rätselgraph ist die einzige öffentliche Quelle für Reihenfolge und
Progression. Die UI soll keine technischen Tokens, Petri-Netze oder
Runtime-Actions in `deer.json` modellieren. Solche Details leitet der Generator
aus Graph, Rätseln und kontrollierten Effekten ab.

## V0-Bausteine

Aktiv im Foundation-Slice:

- `collection.single`: ein Hinweis oder eine Ressource wird an einem
  Fundort gefunden.
- `input.numeric`: ein Zahlen-Code wird an einem Keypad oder einer ähnlichen
  Eingabeoberfläche geprüft.

Die Punktnotation ist eine UI-/Bausteinbezeichnung. In `deer.json` bleiben die
Rätseltypen bewusst grob: `riddle.type` ist `collection` oder `input`; der
konkrete Modus steht in `parameters.rewardMode` bzw. `parameters.inputMode`.

Weitere Bausteine werden erst in das Schema aufgenommen, wenn sie im selben
Slice von UI, Validierung und Generator unterstützt werden. Der
The-Last-Hour-Katalog bleibt dafür Ideensammlung, nicht V0-Vertrag.

## Ablauf Und Validierung

Die UI bildet den Rätselablauf als einfache Reihenfolge mit optionalen
Parallelgruppen ab. Intern entsteht daraus ein Graph mit Start, Rätselknoten,
genau einem Endknoten und reinen Kanten. Die Kanten-Topologie ist die
Abhängigkeitsquelle: Eine Kante von einem Rätsel bedeutet, dass das Ziel nach
Abschluss dieses Rätsels verfügbar wird.

Blockierend sind Game-Breaking-Probleme:

- ein Rätsel ist nicht erreichbar,
- ein Progressionsrätsel kann übersprungen werden,
- ein benötigtes Ergebnis wird nie erzeugt,
- eine Abhängigkeit ist zyklisch oder unlösbar,
- ein Endzustand ist nicht erreichbar,
- Pflichtparameter oder Pflichtassets fehlen,
- ein Baustein wird mit einer inkompatiblen Oberfläche kombiniert,
- ein verwendeter Baustein ist im aktuellen Generator-Slice nicht generierbar.

Warnungen unterstützen die Qualität, blockieren aber nicht die Finalisierung.

## V0-Umsetzungsziel

Der UI-Prototyp macht die Schritte aus `wizard-ui-flow-v0.md` bedienbar,
erzeugt daraus eine schema-valide interne `deer.json` und stellt zusammen mit
den referenzierten Assets einen Projektordner für den manuellen Generatorlauf
bereit.

Für die Umsetzung bleibt `./wizard` der Projekt-Workspace. Die
Konzeptdokumente liegen unter `./wizard/doc/v0`, damit der Root für Web-App,
Entwurfsdaten und Generator-Anbindung frei bleibt.
