# The Last Hour Interaction Catalog V0.2

Stand: 09.07.2026
Zweck: Wizard-nahe Zerlegung der wiederverwendbaren Spiel-Elemente aus
`theLastHourEscapeRoom`.

## Ausgangsentscheidungen

- Code und Assets aus The Last Hour sollen wiederverwendet werden.
- Das bestehende Level wird nicht übernommen. Der Raum wird vom Generator neu
  aufgebaut.
- Ziel ist kein exakter Nachbau, sondern eine Wizard-Version relevanter
  Interaktionen.
- Der Wizard startet ohne vorausgewählte The-Last-Hour-Vorlage.
- The Last Hour liefert einen Produkt- und Baustein-Katalog, aber nicht den
  aktiven Foundation-Scope.
- Hilfen bleiben optional. Ein Rätsel hat immer ein `hints`-Array, aber es
  darf leer sein. Im Foundation-Slice können sie ab Aktivierung des Rätsels
  nacheinander angefordert werden.
- Telefon-Dialoge sind eher Story-Events oder Ressourcen, keine eigenen
  Wizard-Rätsel.
- Der Computer soll langfristig als zentrale wiederverwendbare Schnittstelle
  erhalten bleiben. Er ist aber nicht Teil des Foundation-Slices.

## Gelesene Referenzstellen

- `theLastHourEscapeRoom/src/level/LastHourLevel.java`
- `theLastHourEscapeRoom/src/util/Lore.java`
- `theLastHourEscapeRoom/src/modules/computer/ComputerFactory.java`
- `theLastHourEscapeRoom/src/modules/computer/ComputerStateComponent.java`
- `theLastHourEscapeRoom/src/modules/computer/content/LoginTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/EmailsTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/BrowserTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/FileTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/UsbDriveTab.java`
- `theLastHourEscapeRoom/src/modules/computer/content/ControlPanelTab.java`
- `theLastHourEscapeRoom/src/modules/trash/TrashMinigameFactory.java`
- `theLastHourEscapeRoom/src/modules/usbstick/UsbStickItem.java`

## Foundation-Slice

Aktiv für den ersten UI- und Generator-Slice:

| Schritt | Originalelement | Spieleraktion | Wizard-Element | V0-Entscheidung |
|---|---|---|---|---|
| 1 | Login-Notiz oder Code-Hinweis | Hinweis finden/einsammeln | `collection.single` | Internes Mapping; JSON nutzt `riddle.type=collection` und `rewardMode=find_resource` |
| 2 | Storage-Keypad oder Tür-Keypad | Zahlencode eingeben | `input.numeric` | Internes Mapping; JSON nutzt `riddle.type=input` und `inputMode=numeric` |
| 3 | Ausgang freigeben und gemeinsam erreichen | Erfolg nach Abschluss aller Pflichträtsel | Endknoten mit `surfaceId` auf eine Tür | Generator bindet den gemeinsamen Endknoten an Ausgang und Runtime-State |

Der Foundation-Slice ist absichtlich klein. Er soll beweisen, dass
UI-Eingabe, `deer.json`, Asset-Referenzen, Graphvalidierung und Generatorlauf
wirklich zusammenpassen.

## Baustein-Ideensammlung

Diese Bausteine sind mögliche Erweiterungen aus The Last Hour. Sie werden erst
Contract-Teil, wenn UI, Validierung und Generator sie im selben Slice
unterstützen.

| Schritt | Originalelement | Spieleraktion | Geplanter Baustein | Bemerkung |
|---|---|---|---|---|
| 1 | Stromschalter unter Papier | Schalter finden und bestätigen | `state_change.confirm` | braucht Runtime-Zustandsaktionen |
| 2 | Papierkorb-Minispiel | Hinweise finden/einsammeln | `collection.trash_minigame` | braucht Minispiel- und Spawn-Logik |
| 3 | Computer-Login | E-Mail und Passwort eingeben | `input.credentials` | braucht Computer-/Login-Oberfläche |
| 4 | E-Mail-Postfach und Recovery-Link | richtige Mail/URL erkennen | `choice.email_list` | sollte langfristig in Computer-Tab laufen |
| 5 | Recovery-Webseite mit Binary/ASCII-Code | kodierten Wert entschlüsseln | `input.decoded_text` | braucht Ressourcenketten und Decoder-Schritte |
| 6 | USB-Stick-Hinweis und blauer Stick | richtigen USB finden | `collection.item` | braucht Inventar- und Item-Modell |
| 7 | USB am PC verwenden | richtigen Gegenstand an Ziel benutzen | `item_use` | falsche USBs müssen retry-fähig bleiben |
| 8 | Vent-Seriennummer und AC | Controls ausfüllen/schalten | `control_panel` | braucht mehrstufige Control-States |
| 9 | Bildfragmente aus Vent | Fragmente zusammensetzen | `assembly.image_fragments` | braucht Fragment-Spawn und Assembly-UI |
| 10 | Finale Tür | Passwort eingeben und öffnen | `control_panel` oder `input.numeric` | Foundation kann nur einfache Numeric-Variante |

## Einordnung Vorhandener Elemente

| Element | Einordnung |
|---|---|
| Intro/Outro | Szenario-Text, kein Rätsel. |
| Timer | Session-/Scenario-Konfiguration. |
| Telefonanrufe | Story-Event oder Ressource. |
| Allgemeines Virus-/Falschaktion-System | Späteres Runtime-Fehler- und Feedbackmodell. |
| Licht, Heizung, Kamera im Control Panel | Teil eines späteren Control-Panel-Bausteins. |
| Decoy-Vents, leere Container, Fake-Dateien | Optionale Ressourcen- oder Decoy-Inhalte. |

## Computer-Strategie

Der Computer ist in The Last Hour nicht nur ein einzelnes Rätsel, sondern ein
wiederkehrender Interaktionsort: Login, E-Mails, Browser, Dateien, USB-Laufwerk
und Control Panel laufen dort zusammen. Deshalb sollte der Wizard den Computer
nicht als viele unverbundene Einzelobjekte behandeln.

Empfehlung:

1. **Zielbild:** ein zentraler Computer pro Raum oder Szenario, der mehrere vom
   Generator konfigurierte Tabs aufnehmen kann.
2. **Spätere Umsetzung:** vorhandene Computer-Codebasis wiederverwenden, aber
   nur eine kleine Menge generischer Tab-/Dialogmuster parametrisieren:
   `login`, `choice`, `file/resource`, `usb_drive`, `control_panel`.
3. **Fallback:** Wenn die Generalisierung des Computers zu groß wird, darf ein
   einzelnes Rätsel weiterhin über einen normalen Dialog laufen. Das sollte
   aber als technische Vereinfachung gelten, nicht als langfristiges
   Authoring-Modell.

Damit bleibt der Computer als zentrale Schnittstelle sichtbar, ohne dass der
Foundation-Slice den kompletten The-Last-Hour-Computer als frei
konfigurierbare Plattform implementieren muss.

## Collection vs. Assembly

`collection` bedeutet: Der Spieler findet, sammelt oder öffnet etwas. Die
eigentliche Herausforderung ist der Fundort, die Zugangsbedingung oder das
Durchsuchen eines Containers.

Beispiele:

- Login-Notiz im Schreibtisch finden.
- Hinweis im Papierkorb-Minispiel finden.
- richtigen USB-Stick finden.
- Datei im Computer als Ressource öffnen.

`assembly` bedeutet: Mehrere Teile müssen aktiv zusammengesetzt, geordnet oder
kombiniert werden, damit daraus eine neue Information entsteht.

Beispiele:

- Bildschnipsel zu einem lesbaren finalen Code zusammensetzen.
- mehrere Fragmente in die richtige Reihenfolge bringen.
- aus mehreren Teilgrafiken eine lesbare Ressource erzeugen.

Das finale Papierfragment-Rätsel aus The Last Hour ist deshalb `assembly`, nicht
`collection`: Das Ergebnis ist nicht nur ein gefundener Gegenstand, sondern ein
zusammengesetztes Bild, das danach als Ressource den finalen Code liefert.

## Parameter-Grenze

Die verbindliche Foundation-Tabelle steht in
[`parameter-table-v0.md`](parameter-table-v0.md). Dieses Katalogdokument ist
Ideensammlung und definiert keine zusätzlichen Pflichtfelder für den aktuellen
Contract.

Progression liegt im `riddleGraph` über reine Kanten-Topologie. V0.2 erlaubt
nur geordnete Abschnitte: mehrere eingehende Rätselkanten bedeuten, dass alle
Vorgänger erfüllt sein müssen. Der Endknoten referenziert genau eine
Ausgangstür. Runtime-Tokens, Petri-Netze, konkrete Slots und Spielzustände
leitet der Generator ab.

## Nächste Klärung

Nach dem Foundation-Slice muss entschieden werden, welcher Baustein als nächstes
wirklich generatorfähig wird:

- zentraler Computer mit mehreren generierten Tabs,
- einfache `state_change`-Weltaktionen,
- `choice.email_list`,
- `item_use` mit retry-fähigem Fehlerzustand,
- oder `assembly.image_fragments`.

Neue Bausteine kommen erst mit einer neuen `formatVersion` in
`deer.schema.json` und `parameter-table-v0.md`, wenn UI, TypeScript- und
Java-Validierung, Generator, Runtime und Multiplayer-Synchronisation sie im
gleichen Slice unterstützen.
