# Umfragen nach dem Raum

Ein Raum mit Tracking kann seine Spieler nach dem Raumergebnis befragen. Die Fragen stehen in
`game/assets/surveys/<roomId>.json`; `roomId` ist die Raum-ID aus `Tracking.configureRoom`. Fehlt die
Datei, ist sie ungültig oder ist Tracking inaktiv, entfällt die Umfrage. Ungültige Dateien meldet
das Log mit dem Grund.

Der Raum registriert den Dialog in jeder Laufzeit und zeigt die Umfrage auf der autoritativen Seite
vor dem Abspann:

```java
SurveyFeature.register();

SurveyFeature.show(
    () -> CreditsFeature.showAfterGame(ROOM_ID, Game::complete, player.id()), player.id());
```

Jeder Spieler bekommt einen eigenen Dialog. Ohne Spieler-IDs werden alle Spieler gefragt.
`onComplete` läuft, sobald alle abgesendet oder übersprungen haben.

## Aufbau

```json
{
  "schemaVersion": 1,
  "roomId": "system-recovery",
  "questionnaireId": "escape-room-feedback-v1",
  "title": { "de": "Kurze Umfrage", "en": "Short survey" },
  "skippable": true,
  "pages": [
    {
      "title": { "de": "Zu dir", "en": "About you" },
      "questions": [
        { "id": "age", "type": "number", "required": true,
          "text": { "de": "Wie alt bist du?" }, "min": 10, "max": 99 }
      ]
    }
  ]
}
```

- `questionnaireId` wird mit jeder Antwort gespeichert. Ändern sich Fragen so, dass alte und neue
  Antworten nicht vergleichbar sind, bekommt die Umfrage eine neue ID.
- `skippable` erlaubt das Überspringen nach einer Rückfrage.
- Texte sind wie bei den Credits Objekte mit `de` und/oder `en`. Fehlt die Spielsprache, wird
  `de`, danach `en` angezeigt.
- IDs bestehen aus Buchstaben, Ziffern, `_` und `-`. Frage-IDs sind in der ganzen Datei eindeutig,
  Options-IDs innerhalb ihrer Frage. `other` ist reserviert.
- Unbekannte Felder sind ein Fehler, damit Tippfehler nicht still ignoriert werden.

Jede Frage hat `id`, `type`, `text` und optional `description` (Hilfetext) und `required`
(Standard `false`).

| `type` | Eingabe | Zusätzliche Felder | Gespeicherte Antwort |
| --- | --- | --- | --- |
| `shortText` | einzeiliges Textfeld | `maxLength` (Standard 200) | `"Text"` |
| `longText` | mehrzeiliges Textfeld | `maxLength` (Standard 2000) | `"Text"` |
| `number` | Zahlenfeld | `min`, `max`, `decimals` (Standard `false`) | `42` |
| `scale` | Zahlenreihe, z. B. 1 bis 5 | `min` (1), `max` (5), `minLabel`, `maxLabel` | `4` |
| `singleChoice` | Optionsfelder | `options`, `other` | `{"selected": "a"}` |
| `multiChoice` | Kontrollkästchen | `options`, `other` | `{"selected": ["a", "b"]}` |
| `dropdown` | Auswahlliste | `options` | `{"selected": "a"}` |
| `matrix` | Raster, z. B. Likert-Block | `rows`, `columns` | `{"zeile": "spalte"}` |
| `info` | Text ohne Eingabe | kein `required` | nichts |

`options`, `rows` und `columns` sind Listen aus `{"id": "...", "text": {...}}`. Mit
`"other": true` erscheint zusätzlich „Sonstiges“ mit Freitext; die Antwort enthält dann die ID
`other` und `"otherText"`. Eine Skala umfasst höchstens elf Werte ab mindestens 0. Eine
Pflicht-Matrix verlangt eine Antwort in jeder Zeile.

## Speicherung

Beim Absenden prüft der autoritative Server die Antworten gegen dieselbe Datei und schreibt je
beantworteter Frage ein `SURVEY_ANSWERED`-Ereignis mit `questionnaireId`, `questionId` und `answer`.
Das Ereignis trägt die Sitzung und die sitzungsgebundene `participantId` des Spielers.
Unbeantwortete optionale Fragen erzeugen kein Ereignis.

Danach sieht der Spieler, wo seine Antworten liegen: vom Tracking-Backend bestätigt, nur lokal
gespeichert (HTTP-Upload deaktiviert), noch ausstehend (keine Bestätigung nach zehn Sekunden; die
Outbox wird später übertragen) oder nicht gespeichert.
