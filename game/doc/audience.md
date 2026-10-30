---
title: "Publikumsmodus"
---

Im Publikumsmodus läuft ein Escape-Room auf einem gemeinsamen Spielbildschirm, etwa als
Projektion oder als Bildschirmübertragung. Das Publikum steuert gemeinsam eine Spielfigur und
beantwortet die Rätsel über eine Webseite auf seinen Geräten. Es stimmt in Zeitfenstern ab;
nach jedem Fenster führt das Spiel die häufigste Eingabe aus.

```text
Geräte (Webseite)       <--SSE / HTTP-->  Relay (öffentlich)  <--HTTP, ausgehend--  Spielserver
                                                                                        |
                                              Client des Spielbildschirms (Overlay)  <--Netzwerk------+
```

Der Spielserver bleibt autoritativ: Er zählt die Stimmen, entscheidet bei Gleichstand per Zufall,
führt Schritte und Antworten aus und schreibt das Tracking. Das Relay leitet nur weiter, weil die
Geräte nicht im selben Netz wie der Rechner des Spielbildschirms sein müssen. Betrieb und API
des Relays beschreibt `audience/relay/README.md`.

Der Raum `rooms.audience.AudienceRoom` zeigt alle Rätselarten mit Platzhalterinhalten. Er startet mit
`./gradlew :game:runAudienceRoom` und dem Menüpunkt „Publikumsrunde starten“. Verlangt das Relay
einen Schlüssel, bekommt das Spiel ihn beim Start über `DUNGEON_AUDIENCE_RELAY_KEY`.

Der Raum `rooms.automaton.AutomatonRoom` („Gefangen im Automaten“) nutzt denselben Aufbau mit vier
Fragen zu regulären Sprachen und Grammatiken und einem vierstelligen Türcode. Eine falsche Antwort
zeigt einen Tipp, danach kommt dieselbe Frage noch einmal. Terminal 3 ist anfangs kaputt. An seiner
Stelle steht Kleene, der es repariert, sobald die Figur ihm die Übergangstabelle aus der Truhe
bringt. Die Tabelle ist kein Inventargegenstand, alles läuft über Dialoge. Die Kamera zeigt immer
den ganzen Raum und folgt nicht der Figur. Der Raum nutzt das Design `SYSTEM_RECOVERY`. Ein
Bewegungsfenster dauert dort 5 Sekunden, ein Rätselfenster 2 Minuten. Das Intro besteht aus
mehreren einseitigen Dialogen, die nacheinander erscheinen. Der Raum startet mit
`./gradlew :game:runAutomatonRoom`. F4 im Hauptmenü öffnet den Level-Editor, der ohne Publikum
läuft.

Der Raum `rooms.fomo.FomoRoom` („Gefangen im Modell“) folgt demselben Ablauf für das Modul FOMO.
Die Terminals 1, 2 und 4 stellen die Fragen 1, 2 und 4 des Automatenraums zu regulären Sprachen und
Grammatiken. Terminal 3 fragt nach den Schritten des Modellierungsprozesses. Seine Antwort ist der
Modellierungskreislauf, und nach ihm ordnen sich die Ziffern des Türcodes. Der Raum nutzt das Design
`SYSTEM_RECOVERY`, eigene Terminals und einen Handscanner als Tastenfeld. Stachowiak baut dort
Terminal 3 wieder auf, sobald die Figur ihm den Bauplan aus der Truhe bringt. Der Raum startet mit
`./gradlew :game:runFomoRoom`, sein Level liegt in `levels/fomo`.

Im FOMO-Raum beginnt die Figur in einem Vorraum. Das Intro erklärt dort ausführlich Steuerung,
Abstimmung und Zeitfenster. Im Vorraum steht Robo, ein Roboter auf dem Punkt `tutorial_npc`. Er
stellt eine Single-Choice- und eine Multiple-Choice-Übungsfrage, bei denen jede Antwort zählt, und
nennt danach die Wahl des Publikums. Dann verrät er, dass der Code der Tür ins Labor aus den
letzten drei Ziffern der Telefonnummer besteht. Die Nummer steht auf einer Visitenkarte, die das
Level als `deco-image`-Prefab auf einen Drucker legt. Die Tür ist der Punkt `tutorial_door`, ihr
Tastenfeld `tutorial_panel`, der Code ist `279`.

Im FOMO-Raum stimmt das Publikum nicht über einzelne Schritte ab. Es wählt aus einer Liste, wohin
die Figur geht. Die Figur läuft auf dem kürzesten freien Weg hin, dreht sich zum Ziel und benutzt
es. Ein Fenster dafür dauert 15 Sekunden ab der ersten Stimme. Die Liste wächst mit der Geschichte:

| Wann | Neu in der Liste |
| --- | --- |
| Start | Robo |
| Robo hat geübt | Tastenfeld, Drucker |
| Tür ins Labor offen | Stachowiak, Terminal 1, 2 und 4, Türschloss |
| Erstes Gespräch mit Stachowiak | Rechner, Truhe |
| Terminal 3 aufgebaut | Terminal 3 |
| Terminal 3 gelöst | Whiteboard |
| Tür des Labors offen | Ausgang |

Erledigtes verschwindet wieder. Robo, Tastenfeld und Drucker gehen, sobald die Tür ins Labor offen
ist, die Truhe, sobald sie leer ist, und das Türschloss, sobald die Tür des Labors offen ist. Die
Liste ist nach Namen sortiert.

Neben den Quizfragen hat das Labor des FOMO-Raums diese Rätsel:

- Ein gelöstes Terminal nennt keine Position im Türcode. Es zeigt einen Schritt des
  Modellierungskreislaufs und eine Ziffer. Terminal 1 zeigt den dritten Schritt, Terminal 2 den
  ersten, Terminal 3 den vierten und Terminal 4 den zweiten. Der Türcode `4826` nennt die Ziffern in
  der Reihenfolge des Kreislaufs.
- Den Kreislauf zeichnet Stachowiak an das Whiteboard auf dem Punkt `whiteboard`, sobald Terminal 3
  gelöst ist. Vorher ist das Whiteboard leer, damit es die Frage von Terminal 3 nicht verrät. Das
  Bild ist `rooms/fomo/modelling_cycle.png`. Das Whiteboard ist zwei Felder breit und hat eine
  Hitbox über beide Felder, also lässt es sich vor jedem der beiden benutzen.
- Stimmen die Ziffern am Ausgang, aber nicht ihre Reihenfolge, sagt das Tastenfeld genau das.
- Die Truhe mit dem Bauplan hat ein Zahlenschloss mit zwei Ziffern. Daneben steht auf dem Punkt
  `graph_model` ein Rechner. Er zeigt ein Graphmodell des Labors. Darin sind Räume Knoten, Gänge
  Kanten und die Zahlen Gehminuten. Das Schloss will den kürzesten Weg vom Vorraum zum Serverraum.
  Er führt über Flur, Labor und Lager und dauert 13 Minuten, drei andere Wege dauern 14. `13` öffnet
  die Truhe. Stachowiak erwähnt das Graphmodell, wenn er nach dem Bauplan fragt. Das Bild ist
  `rooms/fomo/graph_model.png`.

Das Labor des FOMO-Raums ist ein Raum mit 9 mal 5 Feldern, damit die Wege kurz bleiben. Vom Eingang
aus erreicht die Figur jedes Objekt in höchstens 8 Schritten, das Whiteboard ist am weitesten weg.

Der Raum `rooms.ki.KiRoom` („Gefangen im Entscheidungsbaum“) ist nach dem FOMO-Raum gebaut und
fragt nach maschinellem Lernen und Entscheidungsbäumen. Er startet mit `./gradlew :game:runKiRoom`,
sein Level liegt in `levels/ki`. Die Unterschiede zum FOMO-Raum:

- Im Vorraum liegt auf dem Drucker die Kontaktseite von Carsten Gips
  (`rooms/ki/contact_page.png`). Der Code der Tür ins Labor ist `268`, die letzten drei Ziffern
  seiner Telefonnummer.
- Fünf Terminals auf den Punkten `q1` bis `q5` fragen nach unüberwachtem Lernen, CAL2, Entropie,
  ID3 und Random Forests. Ein gelöstes Terminal zeigt ein Attribut, seinen Informationsgewinn und
  eine Ziffer. Der Türcode `73591` nennt die Ziffern so, wie ID3 die Attribute wählen würde, vom
  größten Informationsgewinn zum kleinsten.
- Quinlan, der Erfinder von ID3, hat Terminal 4 auseinandergenommen. Er baut es wieder auf, sobald
  die Figur ihm den Bauplan aus der Truhe bringt. Ist die ID3-Frage gelöst, schreibt er die Regel
  „größter Informationsgewinn zuerst“ an das Whiteboard.
- Das Zahlenschloss der Truhe öffnet `83`. Die Zahl liefert der Entscheidungsbaum auf dem Rechner
  am Punkt `decision_tree` (`rooms/ki/decision_tree.png`) für Regen und starken Wind. Die
  eingeritzte Luftfeuchte spielt auf diesem Ast keine Rolle.

Alle drei Räume lesen aus ihrer Level-Datei die benannten Punkte `q1` bis `q4` für die Terminals,
im KI-Raum bis `q5`. Dazu kommen `chest` für die Truhe, `panel` für das Tastenfeld, `door` für die
Tür und `exit` für das Feld dahinter. Die Level-Dateien enthalten keine Dekorationen. Im
Level-Editor (F4) lassen sich Punkte verschieben und Dekorationen setzen. Die Einträge mit `Lab` im
Namen nutzen die Labor-Grafiken aus `objects/tech`. Stehende Geräte blockieren ihr Feld, Wandgeräte
nicht.

## Ablauf

1. Das Spiel legt beim Start eine Sitzung im Relay an. Der Spielbildschirm zeigt die Adresse, einen
   sechsstelligen Code und einen QR-Code, dessen Link direkt beitritt.
2. Wer beitritt, beantwortet zuerst die Vorher-Umfrage. Wer die Einwilligung ablehnt, schaut nur
   zu und stimmt nicht ab; von dieser Person wird nichts gespeichert.
3. Die Spielleitung startet die Runde mit Enter. Nach einem Countdown beginnt das Spiel.
   Beitreten ist jederzeit möglich; Adresse, Code und QR-Code stehen klein in einer Ecke.
4. Am Spielende erscheint auf den Geräten die Nachher-Umfrage. Der Spielbildschirm zählt die
   abgeschickten Umfragen. Enter schließt die Sitzung, spätestens schließt sie sich nach 15
   Minuten.

Die Ende-Taste beendet das Spiel vorzeitig, etwa wenn die Zeit knapp wird. Die Spielzeit
endet dann als nicht gelöst.

Die Spielleitung kann das Spiel jederzeit pausieren: P oder Esc öffnet das Pause-Menü, auch
über einem offenen Rätsel. Ein Klick auf die Ecke mit dem Code zeigt die Beitrittsansicht groß
für Nachzügler; Enter oder ein weiterer Klick setzt fort. In der Pause stehen Spielzeit,
Abstimmungsfenster, Restzeit und Ergebnisanzeige still, die Geräte zeigen „Pausiert“.
Abgegebene Stimmen bleiben, neue zählen erst nach der Pause. Beitreten und die Umfragen gehen
weiter.

## Was die Geräte zeigen

Die Webseite zeigt nur die Eingaben, die zum aktuellen Spielzustand passen. Das Spiel leitet sie
aus dem offenen Dialog der Figur ab:

| Spielzustand | Eingabe auf dem Gerät | Ausgeführt als |
| --- | --- | --- |
| Kein Dialog offen, kein Ziel erreichbar | Steuerkreuz und „Benutzen“ | ein Feld weit gehen oder mit dem Feld davor interagieren |
| Kein Dialog offen, ein `AudienceTarget` erreichbar | Liste der Ziele mit Buchstabe und Name | zum Ziel gehen und es benutzen |
| `MULTIPLE_CHOICE` | farbige Buchstaben A, B, C … mit dem Text der Antwort; bei Mehrfachauswahl mehrere und „Abschicken“ | `ON_OPTION_SELECTED` mit dem Wert oder der Liste der Werte |
| `YES_NO` | „Ja“ und „Nein“ | `ON_YES` oder `ON_NO` |
| `FREE_INPUT` | Textfeld | `ON_CONFIRM` mit dem Text |
| `FREE_INPUT` mit `AudienceSystem.CODE_DIGITS` | Ziffernblock für einen Code dieser Länge | `ON_CONFIRM` mit dem Code |
| `OK`, `TEXT`, `DIALOG_DIALOG` | „Weiter“ | `ON_CONFIRM` |
| `IMAGE`, etwa vom Prefab `deco-image` | „Weiter“ | `ON_CLOSE`, dann schließt das Bild |
| andere Dialoge, Figur läuft | nichts, „Schaut auf den Spielbildschirm“ | |

Das Prefab `door-keypad` öffnet einen `KEYPAD`-Dialog, über den das Publikum nicht abstimmen kann.
Eine Tür mit Code baut ein Raum deshalb selbst, mit einem `FREE_INPUT`-Dialog und
`AudienceSystem.CODE_DIGITS`, wie der FOMO-Raum.

Über den Eingaben zeigt das Gerät Titel und Frage des Dialogs, bei Texten den Text selbst.
Auf breiten Bildschirmen stehen Aufgabe und Eingaben nebeneinander, und jede Eingabe hat eine Taste:
Pfeiltasten und Leertaste zum Gehen, Buchstaben für Antworten und Ziele, Ziffern für Ja/Nein und
Codes, Enter zum Abschicken. Die Antworten von Multiple-Choice-Dialogen erscheinen auf dem
Spielbildschirm und auf dem Gerät mit demselben Buchstaben, auf dem Gerät als eine Zeile je
Antwort. Die Geräte schicken als Stimme weiterhin die Nummer der Antwort, ab 1 gezählt. Klicks am
Rechner des Spielbildschirms beantworten keine Dialoge; das tun nur die Stimmen.

Die Webseite gibt es auf Deutsch und Englisch. Sie startet in der ersten Sprache, die der Browser
bevorzugt, sonst auf Englisch; der Umschalter oben rechts speichert die Wahl in einem Cookie.
Umfragen zeigen ihre Texte in der gewählten Sprache. Dialogtexte und Abstimmungsergebnisse kommen
vom Spiel und bleiben in dessen Sprache.

Der Spielbildschirm umrandet alle interagierbaren Objekte dauerhaft, nicht nur das Objekt unter dem
Mauszeiger. Alle tragen den halb durchsichtigen gelben Rand, das Objekt, das „Benutzen“ gerade
auslösen würde, den roten. Dieses Objekt bestimmt der Server und schickt seine ID mit dem
HUD-Zustand an den Client, weil der Client die Hitbox der Figur nicht kennt. Solange ein Dialog
offen ist, sind die Ränder aus.

In einem Raum mit Zielen tragen nur die Ziele den gelben Rand, die das Publikum gerade wählen kann.
Den roten Rand gibt es dort nicht, weil es kein „Benutzen“ gibt. Während das Publikum wählt, steht
über jedem Ziel ein Schild mit demselben Buchstaben und Namen wie auf dem Gerät. Welche Ziele das
sind und wo ihre Schilder hingehören, schickt der Server mit dem HUD-Zustand.

## Abstimmung

- Ein Bewegungsfenster beginnt mit der ersten Stimme und dauert 10 Sekunden. Die Wahl eines Ziels
  dauert genauso lange. Haben alle abgestimmt, bleiben dort wie bei einem Rätsel nur noch 3
  Sekunden.
- Ein Rätselfenster beginnt, sobald der Dialog offen ist, und dauert 25 Sekunden. Hat die Hälfte
  der Abstimmenden eine Stimme abgegeben, bleiben nur noch 10 Sekunden, haben alle abgestimmt,
  nur noch 3, um die Stimme zu ändern. Kommt keine Stimme, beginnt ein neues Fenster.
- Ein „Weiter“-Fenster dauert 30 Sekunden. Sobald die Hälfte der Abstimmenden „Weiter“
  gedrückt hat, bleiben nur noch 5 Sekunden, sobald alle gedrückt haben, nur noch 2. Die Stimme
  heißt nur „fertig gelesen“; ohne Stimmen geht es nach Ablauf trotzdem weiter.
- Pro Person und Fenster zählt die letzte Stimme. Bei Mehrfachauswahl zählt die ganze
  Kombination. Text und Codes zählen ohne Leerzeichen und kleingeschrieben, „O(log n)“ und
  „o(logn)“ sind also dieselbe Stimme; in dieser Form kommt die Antwort auch beim Rätsel an.
  Führende Nullen bleiben erhalten.
- Bei Gleichstand wählt das Spiel eine der häufigsten Eingaben mit gleicher Wahrscheinlichkeit.
- Wechselt der Spielzustand, verfallen die Stimmen des alten Fensters.

Nach jedem Fenster steht das Ergebnis 5 Sekunden auf dem Spielbildschirm und den Geräten. Die
Dauern stehen in `AudienceConfig` und lassen sich pro Raum ändern.

## Einen Raum für den Publikumsmodus bauen

Der Spielserver fügt das `AudienceSystem` vor dem `VelocitySystem` hinzu, weil es die Schritte der
Figur steuert. Der Client ruft `AudienceHud.register()` im `onConfigure` auf, also vor dem
Verbindungsaufbau, und `AudienceHud.install()` in seinem Setup. Ist der Raum gelöst, ruft er
`audience.end(PlayClock.Outcome.SUCCESS)` auf. `rooms.audience.AudienceRoom` zeigt ein vollständiges
Beispiel.

Beim Levelbau gilt:

- Die Figur bewegt sich nur in ganzen Feldern. Interagierbare Objekte stehen auf einem eigenen
  Feld und brauchen ein freies Nachbarfeld, von dem aus die Figur sie ansehen kann.
- „Benutzen“ interagiert nur mit dem Objekt auf dem Feld direkt vor der Figur. Ein Objekt belegt
  jedes Feld, in das seine Hitbox hineinreicht, ein Objekt ohne Hitbox nur das Feld seiner
  Position. Breite Objekte wie ein Whiteboard brauchen deshalb eine Hitbox über ihre ganze Breite.
  Die Größe des Sprites zählt nicht, weil der Server die Größe einzelner Bilder nicht kennt. Die
  Reichweite der `Interaction` muss mindestens 1 betragen.
- Rätsel müssen einer der Dialogarten aus der Tabelle oben entsprechen. Zuordnungen mit Truhen,
  Inventar oder Echtzeit-Aufgaben lassen sich nicht abstimmen.
- Texte in `DIALOG_DIALOG` sollten eine Seite lang sein, weil „Weiter“ den ganzen Dialog bestätigt.
- Codes fragt ein `FREE_INPUT` mit `AudienceSystem.CODE_DIGITS` ab. Das Keypad der Engine lässt sich
  nicht abstimmen.
- Statt einzelner Schritte kann ein Raum das Publikum Ziele wählen lassen. Dafür bekommt jedes
  Objekt ein `AudienceTarget` mit einem eindeutigen Namen und einer Bedingung, wann es in der Liste
  steht. Ein Feld, auf das die Figur gehen soll, etwa hinter der letzten Tür, markiert
  `AudienceTarget.tile(...)`. Ein Ziel steht nur in der Liste, solange seine Bedingung gilt und die
  Figur es erreicht. Was hinter einer geschlossenen Tür liegt, fehlt also von selbst. Ist kein Ziel
  erreichbar, bekommt das Publikum das Steuerkreuz. Die Wegsuche weicht allen festen Objekten aus,
  jedes Ziel braucht also ein freies, erreichbares Nachbarfeld.
- Die Figur steht mit der Mitte ihrer Hitbox auf der Mitte des Feldes, auch wenn ihr Sprite größer
  als ein Feld ist. Deshalb funktioniert jede Spielfigur. Andere Figuren wie einen NPC stellt
  `AudienceSystem.standOn(entity, tile)` genauso auf ein Feld.

## Umfragen und Tracking

Die Umfragen sind `surveys/pre-<roomId>.json` und `surveys/post-<roomId>.json` im Format aus
[Umfragen erstellen](survey.md). Die Vorher-Umfrage sollte eine Einwilligungsfrage mit
`declinesConsent` enthalten. Anders als im normalen Spiel betrifft eine Ablehnung nur die eine
Person.

Jedes zustimmende Gerät wird ein Tracking-Teilnehmer ohne Netzwerkclient. Neben
`SURVEY_ANSWERED` schreibt der Publikumsmodus:

- `VOTE_CAST`: die gezählte Stimme einer Person in einem Fenster,
- `VOTE_RESOLVED`: die Verteilung eines Fensters und das ausgeführte Ergebnis.

Beide tragen die Fensternummer `window` und den Spielzustand `context` (`move`, `goto`, `choice`,
`text`, `code`, `continue`). Bei `goto` ist die Stimme der Name des Ziels. Das Tracking-Backend
braucht dafür die Migration `V003__votes.sql`.

Die Tracking-Sitzung hängt am Client des Spielbildschirms. Verliert er die Verbindung zum
Spielserver, endet sie; nach dem Wiederverbinden zählen die Abstimmungen weiter, werden aber nicht
mehr gespeichert. Weil Server und Client auf demselben Rechner laufen, kommt das nur bei einem
Absturz des Clients vor. Dann hilft nur ein Neustart der Runde.
