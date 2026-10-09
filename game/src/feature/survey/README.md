# Umfragen nach dem Raum

Ein Raum mit Tracking kann seine Spieler nach dem Raumergebnis befragen. Die Fragen stehen in
`game/assets/surveys/<roomId>.json`; `roomId` ist die Raum-ID aus `Tracking.configureRoom`. Das
Format und alle Fragetypen beschreibt [Umfragen erstellen](../../../doc/survey.md). Fehlt die Datei,
ist sie ungültig oder ist Tracking inaktiv, entfällt die Umfrage. Ungültige Dateien meldet das Log
mit dem Grund.

Der Raum registriert den Dialog in jeder Laufzeit und zeigt die Umfrage auf der autoritativen Seite
vor dem Abspann:

```java
SurveyFeature.register();

SurveyFeature.show(() -> CreditsFeature.showAfterGame(ROOM_ID, Game::complete));
```

Jeder Spieler bekommt einen eigenen Dialog. Ohne Spieler-IDs werden alle Spieler gefragt.
`onComplete` läuft, sobald alle abgesendet oder übersprungen haben oder nicht mehr verbunden sind.

## Speicherung

Beim Absenden prüft der autoritative Server die Antworten gegen dieselbe Datei und schreibt je
beantworteter Frage ein `SURVEY_ANSWERED`-Ereignis mit `questionnaireId`, `questionId` und `answer`.
Das Ereignis trägt die Sitzung und die sitzungsgebundene `participantId` des Spielers.
Unbeantwortete optionale Fragen erzeugen kein Ereignis.

Danach sieht der Spieler, wo seine Antworten liegen: vom Tracking-Backend bestätigt, nur lokal
gespeichert (HTTP-Upload deaktiviert), noch ausstehend (keine Bestätigung nach zehn Sekunden; die
Outbox wird später übertragen) oder nicht gespeichert.
