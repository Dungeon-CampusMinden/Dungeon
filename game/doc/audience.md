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
| Kein Dialog offen | Steuerkreuz und „Benutzen“ | ein Feld weit gehen oder mit dem Feld davor interagieren |
| `MULTIPLE_CHOICE` | farbige Nummern 1 bis n; bei Mehrfachauswahl mehrere und „Abschicken“ | `ON_OPTION_SELECTED` mit dem Wert oder der Liste der Werte |
| `YES_NO` | „Ja“ und „Nein“ | `ON_YES` oder `ON_NO` |
| `FREE_INPUT` | Textfeld | `ON_CONFIRM` mit dem Text |
| `FREE_INPUT` mit `AudienceSystem.CODE_DIGITS` | Ziffernblock für einen Code dieser Länge | `ON_CONFIRM` mit dem Code |
| `OK`, `TEXT`, `DIALOG_DIALOG` | „Weiter“ | `ON_CONFIRM` |
| andere Dialoge, Figur läuft | nichts, „Schaut auf den Spielbildschirm“ | |

Über den Eingaben zeigt das Gerät Titel und Frage des Dialogs, bei Texten den Text selbst.
Auf breiten Bildschirmen stehen Aufgabe und Eingaben nebeneinander, und jede Eingabe hat eine Taste:
Pfeiltasten und Leertaste zum Gehen, Ziffern für Antworten und Codes, Enter zum Abschicken.
Die Antworten von Multiple-Choice-Dialogen erscheinen auf dem Spielbildschirm mit ihrer Nummer,
auf dem Gerät nur als Nummer. Klicks am Rechner des Spielbildschirms beantworten keine Dialoge;
das tun nur die Stimmen.

Die Webseite gibt es auf Deutsch und Englisch. Sie startet in der ersten Sprache, die der Browser
bevorzugt, sonst auf Englisch; der Umschalter oben rechts speichert die Wahl in einem Cookie.
Umfragen zeigen ihre Texte in der gewählten Sprache. Dialogtexte und Abstimmungsergebnisse kommen
vom Spiel und bleiben in dessen Sprache.

## Abstimmung

- Ein Bewegungsfenster beginnt mit der ersten Stimme und dauert 10 Sekunden.
- Ein Rätselfenster beginnt, sobald der Dialog offen ist, und dauert 25 Sekunden. Kommt keine
  Stimme, beginnt ein neues Fenster.
- Ein „Weiter“-Fenster dauert 30 Sekunden. Sobald die Hälfte der Abstimmenden „Weiter“
  gedrückt hat, bleiben nur noch 5 Sekunden. Die Stimme heißt nur „fertig gelesen“; ohne
  Stimmen geht es nach Ablauf trotzdem weiter.
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
- „Benutzen“ interagiert nur mit dem Objekt auf dem Feld direkt vor der Figur. Die Reichweite der
  `Interaction` muss mindestens 1 betragen.
- Rätsel müssen einer der Dialogarten aus der Tabelle oben entsprechen. Zuordnungen mit Truhen,
  Inventar oder Echtzeit-Aufgaben lassen sich nicht abstimmen.
- Texte in `DIALOG_DIALOG` sollten eine Seite lang sein, weil „Weiter“ den ganzen Dialog bestätigt.
- Codes fragt ein `FREE_INPUT` mit `AudienceSystem.CODE_DIGITS` ab. Das Keypad der Engine lässt sich
  nicht abstimmen.

## Umfragen und Tracking

Die Umfragen sind `surveys/pre-<roomId>.json` und `surveys/post-<roomId>.json` im Format aus
[Umfragen erstellen](survey.md). Die Vorher-Umfrage sollte eine Einwilligungsfrage mit
`declinesConsent` enthalten. Anders als im normalen Spiel betrifft eine Ablehnung nur die eine
Person.

Jedes zustimmende Gerät wird ein Tracking-Teilnehmer ohne Netzwerkclient. Neben
`SURVEY_ANSWERED` schreibt der Publikumsmodus:

- `VOTE_CAST`: die gezählte Stimme einer Person in einem Fenster,
- `VOTE_RESOLVED`: die Verteilung eines Fensters und das ausgeführte Ergebnis.

Beide tragen die Fensternummer `window` und den Spielzustand `context` (`move`, `choice`, `text`,
`code`, `continue`). Das Tracking-Backend braucht dafür die Migration
`V003__votes.sql`.

Die Tracking-Sitzung hängt am Client des Spielbildschirms. Verliert er die Verbindung zum
Spielserver, endet sie; nach dem Wiederverbinden zählen die Abstimmungen weiter, werden aber nicht
mehr gespeichert. Weil Server und Client auf demselben Rechner laufen, kommt das nur bei einem
Absturz des Clients vor. Dann hilft nur ein Neustart der Runde.
