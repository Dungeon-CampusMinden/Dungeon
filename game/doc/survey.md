---
title: "Umfragen erstellen"
---

Vor und nach einem Escape-Room kann das Spiel jedem Spieler eine kurze Umfrage zeigen. Die Fragen
stehen in JSON-Dateien pro Raum. Dieses Dokument beschreibt, welche Fragetypen es gibt und wie man
sie aufschreibt. Programmieren muss man dafür nicht.

![Umfrage im Spiel](./img/survey/overview.png)

## Wo die Dateien liegen

Jeder Raum hat bis zu zwei Umfragen:

| Datei | Wann sie erscheint |
| --- | --- |
| `game/assets/surveys/pre-<roomId>.json` | Bei einem neuen Spiel vor dem Intro. „Weiter“ nach dem Absenden startet das Intro. Ein geladener Spielstand fragt nicht erneut. |
| `game/assets/surveys/post-<roomId>.json` | Als letzter Schritt nach dem Raumergebnis und dem Abspann. „Beenden“ nach dem Absenden schließt die Umfrage; sobald alle Spieler fertig sind, endet das Spiel. |

`<roomId>` ist die Raum-ID, unter der der Raum getrackt wird: `system-recovery`, `soulweaver` oder
`the-last-hour`. Gibt es eine Datei nicht, entfällt diese Umfrage. Ist sie fehlerhaft, entfällt sie
ebenfalls und das Log nennt den Fehler.

Jeder Spieler beantwortet die Umfrage für sich. Ohne Tracking-Zustimmung im Hauptmenü erscheint
keine Umfrage, weil die Antworten dann nicht gespeichert werden dürfen. Spielzeit und
Rätselereignisse beginnen erst nach dem Intro; die Vorher-Umfrage zählt also nicht zur Spielzeit.

### Varianten für A/B-Tests

Jeder Durchlauf gehört zufällig zu Variante `a` oder `b`, etwa die Hälfte der Durchläufe zu jeder.
Die Variante hängt an der Run-ID des Spielstands; ein geladener Spielstand behält sie, und im
Mehrspielermodus haben alle Spieler eines Durchlaufs dieselbe. Gibt es für eine Phase
`<phase>-a-<roomId>.json` und `<phase>-b-<roomId>.json`, zeigt jeder Durchlauf die Datei seiner
Variante statt `<phase>-<roomId>.json`. Fehlt die Datei der Variante, gilt die gemeinsame Datei.

Sollen zwei Wissenstests vorher und nachher wechseln, enthält `pre-a-<roomId>.json` Test A und
`post-a-<roomId>.json` Test B, `pre-b-<roomId>.json` Test B und `post-b-<roomId>.json` Test A. Jede
Datei bekommt ihre eigene `questionnaireId`, etwa `wissenstest-pre-a-v1`; daran erkennt die
Auswertung Phase und Variante jeder Antwort.

## Grundgerüst

```json
{
  "schemaVersion": 1,
  "roomId": "system-recovery",
  "questionnaireId": "escape-room-post-v1",
  "title": { "de": "Kurze Umfrage zum Escape-Room", "en": "Short survey about the escape room" },
  "skippable": true,
  "pages": [
    {
      "title": { "de": "Zu dir", "en": "About you" },
      "questions": [ ]
    }
  ]
}
```

| Feld | Bedeutung |
| --- | --- |
| `schemaVersion` | Immer `1`. |
| `roomId` | Muss am Ende des Dateinamens stehen, etwa `system-recovery` in `pre-a-system-recovery.json`. |
| `questionnaireId` | Kennung des Fragebogens; sie wird mit jeder Antwort gespeichert. Jede Datei braucht ihre eigene Kennung, damit Antworten aus Vorher- und Nachher-Umfrage und aus beiden Varianten unterscheidbar bleiben. Ändern sich Fragen so, dass alte und neue Antworten nicht mehr vergleichbar sind, bekommt der Fragebogen eine neue Kennung, etwa `-v2`. |
| `title` | Überschrift im blauen Kopf des Dialogs. |
| `skippable` | `true` zeigt einen Knopf „Überspringen“ mit Rückfrage, `false` verlangt das Absenden. |
| `pages` | Seiten in Anzeigereihenfolge. Jede Seite hat optional einen `title` und mindestens eine Frage. Die Spieler blättern mit „Weiter“ und „Zurück“. |

### Texte und Sprachen

Jeder angezeigte Text ist ein Objekt mit `de` und/oder `en`, wie bei den Credits. Fehlt die
Spielsprache, zeigt das Spiel `de`, danach `en`. Eine rein deutsche Umfrage braucht also nur `de`.

### Kennungen

`id`-Werte bestehen aus Buchstaben, Ziffern, `_` und `-`. Frage-IDs sind in der ganzen Datei
eindeutig, Options-IDs innerhalb ihrer Frage. `other` ist für „Sonstiges“ reserviert. In der
Auswertung erscheinen nur die IDs, nicht die angezeigten Texte; wähle sie deshalb sprechend und
ändere sie nicht mehr, sobald Daten erhoben werden.

### Felder jeder Frage

| Feld | Pflicht | Bedeutung |
| --- | --- | --- |
| `id` | ja | Kennung der Frage. |
| `type` | ja | Einer der Typen unten. |
| `text` | ja | Fragetext. |
| `description` | nein | Hilfetext in Grau unter der Frage. |
| `required` | nein | `true` macht die Frage zur Pflichtfrage, sichtbar am roten `*`. Standard ist `false`. |

Unbekannte Felder gelten als Fehler, damit sich Tippfehler wie `"requierd"` nicht still einschleichen.

## Fragetypen

### Info-Block: `info`

![Info-Block](./img/survey/info.png)

Text ohne Eingabe, etwa eine Begrüßung, ein Hinweis zur Dauer oder zum Datenschutz. `text` ist die
fett gedruckte erste Zeile, `description` der Fließtext darunter. `required` gibt es hier nicht.

```json
{ "id": "intro", "type": "info",
  "text": { "de": "Danke fürs Spielen!" },
  "description": { "de": "Die Umfrage dauert etwa drei Minuten. Fragen mit * sind Pflichtfragen." } }
```

### Kurzer Text: `shortText`

![Kurzer Text](./img/survey/short-text.png)

Einzeiliges Textfeld für kurze Antworten wie einen Namen oder ein Stichwort.

| Feld | Bedeutung |
| --- | --- |
| `maxLength` | Höchstzahl an Zeichen, Standard 50, höchstens 10.000. Rechts neben der Frage zählt das Spiel mit, etwa `12/50`. |

```json
{ "id": "favorite-puzzle", "type": "shortText",
  "text": { "de": "Welches Rätsel hat dir am besten gefallen?" } }
```

Gespeichert wird der Text, zum Beispiel `"Das Batterie-Rätsel"`.

### Langer Text: `longText`

![Langer Text](./img/survey/long-text.png)

Mehrzeiliges Feld für freie Rückmeldungen. Zeilenumbrüche bleiben erhalten.

| Feld | Bedeutung |
| --- | --- |
| `maxLength` | Höchstzahl an Zeichen mit Zähler wie beim kurzen Text, Standard und höchstens 10.000. |

```json
{ "id": "improvements", "type": "longText", "maxLength": 2000,
  "text": { "de": "Was sollten wir verbessern?" },
  "description": { "de": "Optional. Jede Anmerkung hilft uns." } }
```

### Zahl: `number`

![Zahl](./img/survey/number.png)

Zahlenfeld, zum Beispiel für das Alter. Rechts daneben erklärt das Spiel den erlaubten Bereich.
Buchstaben lassen sich nicht eingeben. Ein Minus lässt sich nur tippen, wenn `min` fehlt oder negativ
ist; Komma und Punkt nur mit `decimals: true`, beide als Dezimaltrennzeichen.

| Feld | Bedeutung |
| --- | --- |
| `min`, `max` | Optionaler kleinster und größter erlaubter Wert. |
| `decimals` | `true` erlaubt Kommazahlen, Standard ist `false` (nur ganze Zahlen). |

```json
{ "id": "age", "type": "number", "required": true, "min": 10, "max": 99,
  "text": { "de": "Wie alt bist du?" } }
```

Gespeichert wird die Zahl, zum Beispiel `24`.

### Skala: `scale`

![Skala](./img/survey/scale.png)

Eine Reihe von Zahlenknöpfen, etwa von 1 bis 5. Die optionalen Beschriftungen der beiden Enden
stehen unter dem ersten und dem letzten Knopf. Ein zweiter Klick auf den gewählten Wert hebt die
Auswahl wieder auf. Geeignet für Bewertungen wie „Wie viel Spaß hat dir der Raum gemacht?“.

| Feld | Bedeutung |
| --- | --- |
| `min`, `max` | Kleinster und größter Wert, Standard 1 und 5. Höchstens elf Werte, `min` ab 0. |
| `minLabel`, `maxLabel` | Optionale Beschriftung der beiden Enden. |

```json
{ "id": "fun", "type": "scale", "required": true, "min": 1, "max": 5,
  "text": { "de": "Wie viel Spaß hat dir der Raum gemacht?" },
  "minLabel": { "de": "Gar keinen" }, "maxLabel": { "de": "Sehr viel" } }
```

Gespeichert wird der gewählte Wert, zum Beispiel `4`.

### Einfachauswahl: `singleChoice`

![Einfachauswahl](./img/survey/single-choice.png)

Runde Auswahlknöpfe, von denen genau einer gewählt werden kann. Mit `"other": true` erscheint
zusätzlich „Sonstiges“ mit einem Textfeld. Wer dort tippt, wählt „Sonstiges“ automatisch aus; wer
„Sonstiges“ ohne Text wählt, bekommt einen Hinweis.

| Feld | Bedeutung |
| --- | --- |
| `options` | Liste aus `{ "id": "...", "text": { ... } }`. Eine Option kann zusätzlich `"declinesConsent": true` tragen, siehe [Einwilligung ablehnen](#einwilligung-ablehnen). |
| `other` | `true` ergänzt „Sonstiges“ mit Freitext. |

```json
{ "id": "escape-room-experience", "type": "singleChoice", "required": true, "other": true,
  "text": { "de": "Wie oft hast du schon Escape-Rooms gespielt?" },
  "options": [
    { "id": "never", "text": { "de": "Noch nie" } },
    { "id": "once", "text": { "de": "Ein- oder zweimal" } },
    { "id": "often", "text": { "de": "Öfter" } } ] }
```

Gespeichert wird `{"selected": "once"}` oder bei Sonstiges
`{"selected": "other", "otherText": "Nur am PC"}`.

### Mehrfachauswahl: `multiChoice`

![Mehrfachauswahl](./img/survey/multi-choice.png)

Eckige Kontrollkästchen; beliebig viele Optionen sind wählbar. Bei einer Pflichtfrage muss
mindestens eine gewählt sein. `options` und `other` funktionieren wie bei der Einfachauswahl.

```json
{ "id": "liked", "type": "multiChoice", "other": true,
  "text": { "de": "Was hat dir gefallen?" },
  "description": { "de": "Mehrere Antworten möglich." },
  "options": [
    { "id": "puzzles", "text": { "de": "Die Rätsel" } },
    { "id": "story", "text": { "de": "Die Geschichte" } } ] }
```

Gespeichert wird `{"selected": ["puzzles", "other"], "otherText": "Die Musik"}`.

### Auswahlliste: `dropdown`

![Auswahlliste](./img/survey/dropdown.png)

Eine aufklappbare Liste mit einer Auswahl. Sie spart Platz bei vielen Optionen, etwa für
Studiengänge. „Sonstiges“ gibt es hier nicht; nimm dafür eine eigene Option oder eine
Einfachauswahl.

```json
{ "id": "study-program", "type": "dropdown",
  "text": { "de": "Was studierst du?" },
  "options": [
    { "id": "computer-science", "text": { "de": "Informatik" } },
    { "id": "none", "text": { "de": "Ich studiere nicht" } } ] }
```

Gespeichert wird `{"selected": "computer-science"}`.

### Matrix: `matrix`

![Matrix](./img/survey/matrix.png)

Mehrere Aussagen, die alle auf denselben Spalten bewertet werden, etwa eine Likert-Skala von
„Stimme gar nicht zu“ bis „Stimme voll zu“. In jeder Zeile ist eine Antwort wählbar. Eine
Pflicht-Matrix verlangt eine Antwort in jeder Zeile.

| Feld | Bedeutung |
| --- | --- |
| `rows` | Die Aussagen, Liste aus `{ "id": "...", "text": { ... } }`. |
| `columns` | Die Antwortspalten, mindestens zwei, gleiche Form. |

```json
{ "id": "statements", "type": "matrix", "required": true,
  "text": { "de": "Wie sehr stimmst du diesen Aussagen zu?" },
  "rows": [
    { "id": "puzzles-clear", "text": { "de": "Die Rätsel waren verständlich." } },
    { "id": "hints-helpful", "text": { "de": "Die Hinweise haben mir geholfen." } } ],
  "columns": [
    { "id": "1", "text": { "de": "Stimme gar nicht zu" } },
    { "id": "2", "text": { "de": "Stimme eher nicht zu" } },
    { "id": "3", "text": { "de": "Teils, teils" } },
    { "id": "4", "text": { "de": "Stimme eher zu" } },
    { "id": "5", "text": { "de": "Stimme voll zu" } } ] }
```

Gespeichert wird je Zeile die Spalten-ID: `{"puzzles-clear": "4", "hints-helpful": "5"}`.

## Pflichtfragen und Prüfung

![Fehlende Pflichtantworten](./img/survey/validation.png)

„Weiter“ und „Absenden“ prüfen die aktuelle Seite. Fehlt eine Pflichtantwort oder passt eine Zahl
nicht in den Bereich, färbt sich die Frage rot, darunter steht der Grund und die Seite scrollt zur
ersten betroffenen Frage. Bei einer Matrix sind zusätzlich die unbeantworteten Zeilen markiert. Ab
dann prüft das Spiel die Frage bei jeder Änderung; ist sie richtig beantwortet, wird sie wieder
neutral.

## Überspringen

![Überspringen](./img/survey/skip.png)

Mit `"skippable": true` gibt es links unten „Überspringen“. Ein Klick ersetzt die Knöpfe durch „Zur
Umfrage“ und „Ja, überspringen“; erst die Bestätigung beendet die Umfrage. Bis dahin eingegebene
Antworten werden dabei nicht gespeichert.

## Einwilligung ablehnen

Fragt eine Umfrage nach der Einwilligung in die Datenerfassung, markiert `"declinesConsent": true`
die ablehnende Option. Das geht bei `singleChoice` und `dropdown`, nicht bei `multiChoice`.

```json
{ "id": "consent", "type": "singleChoice", "required": true,
  "text": { "de": "Bist du einverstanden, dass deine Daten pseudonym erfasst werden?" },
  "options": [
    { "id": "yes", "text": { "de": "Ja" } },
    { "id": "no", "text": { "de": "Nein" }, "declinesConsent": true } ] }
```

Wählt ein Spieler diese Option, steht unten „Absenden“ mit dem Hinweis „Deine Antworten werden dann
nicht gespeichert.“, auch wenn noch Pflichtfragen offen sind. Nach dem Absenden schließt sich die
Umfrage ohne Bestätigung und das Spiel geht weiter wie nach einer beantworteten Umfrage. Keine
Antwort dieser Umfrage wird gespeichert.

Wie eine abgelehnte Tracking-Zustimmung im Hauptmenü beendet die Ablehnung das Tracking für den
ganzen Durchlauf, im Mehrspielermodus also für alle Spieler. Danach entstehen keine Ereignisse mehr
und die Nachher-Umfrage entfällt. Wer die Umfrage gerade noch ausfüllt, sieht nach dem Absenden
„nicht gespeichert“. Bereits gespeicherte Ereignisse, etwa der Beitritt der Spieler, bleiben
erhalten. Nach dem Laden eines Spielstands gilt wieder die Tracking-Zustimmung aus dem Hauptmenü.

## Nach dem Absenden

![Bestätigung](./img/survey/result.png)

Nach dem Absenden zeigt das Spiel, wo die Antworten gelandet sind:

- beim Tracking-Server angekommen,
- lokal gespeichert, weil kein Tracking-Server eingerichtet ist,
- lokal gespeichert, weil der Tracking-Server nicht erreichbar ist,
- lokal gespeichert, weil der Tracking-Server den Empfang nicht innerhalb von zehn Sekunden bestätigt
  hat,
- nicht gespeichert.

Lokal heißt in der Outbox-Datei des Rechners, der das Spiel hostet. Hat der Tracking-Server die
Antworten der Nachher-Umfrage nicht bestätigt, nennt die Anzeige auf diesem Rechner die Datei und
die Betreiber-E-Mail, an die sie geschickt werden soll. Nach der Vorher-Umfrage fehlt dieser
Hinweis, weil die Datei erst nach dem Spiel vollständig ist.

Für die Auswertung wird jede beantwortete Frage als eigenes Tracking-Ereignis `SURVEY_ANSWERED` mit
`questionnaireId`, `questionId` und `answer` gespeichert. Es gehört zur Sitzung des Raums und zur
sitzungsgebundenen Teilnehmer-ID des Spielers. Unbeantwortete optionale Fragen erzeugen kein
Ereignis. Freitext kann personenbezogene Daten enthalten; frage deshalb nicht nach Namen oder
Kontaktdaten.
