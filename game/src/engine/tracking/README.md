# Tracking in Räumen

Tracking läuft nur im autoritativen Serverprozess oder im Einzelspielermodus. Konfiguriere vor
`Game.run()` eine stabile Raum-ID:

```java
Tracking.configureRoom("my-room");
```

`configureRoom` trennt die Deployment-Einstellungen vom Raumcode. Das Deployment kann Folgendes
festlegen:

| Systemeigenschaft | Umgebungsvariable | Standardwert |
| --- | --- | --- |
| `dungeon.tracking.endpoint` | `DUNGEON_TRACKING_ENDPOINT` | `http://127.0.0.1:8088` |
| `dungeon.tracking.apiKey` | `DUNGEON_TRACKING_API_KEY` | keiner |
| `dungeon.tracking.outbox` | `DUNGEON_TRACKING_OUTBOX` | `tracking-outbox` |

`TrackingConfig.TRACKING_ENABLED` steuert die HTTP-Verbindung und ist im Quellcode standardmäßig
`false`. JSONL-Tracking bleibt dabei aktiv. Mit dem Wert `true` verwendet Dungeon den
konfigurierten Endpunkt oder ohne Override das lokale Backend. Das Pause-Menü zeigt bei `false`
"Deaktiviert" statt einen Verbindungsstatus.

Für die E-Mail-Adresse des Betreibers gilt der im Quellcode definierte Standardwert. Der Raumcode
kann als zweites Argument von `configureRoom` eine andere Adresse übergeben. Deployment-Eigenschaften
oder Umgebungsvariablen werden dafür nicht ausgewertet.

Der Endpunkt muss eine absolute HTTP(S)-URI ohne Benutzerinformationen, Abfrage oder Fragment
sein. Für ein lokal betriebenes Backend ist unverschlüsseltes HTTP zulässig.

Das Menü "Spiel hosten" kopiert nicht leere `dungeon.tracking.*`-Systemeigenschaften in die
Umgebung des verwalteten Servers. Sie überschreiben geerbte `DUNGEON_TRACKING_*`-Werte genauso wie
im hostenden Prozess. Der API-Schlüssel erscheint nie in der Befehlszeile der Child-JVM.

Falls ein Deployment den Raumcode nicht ändern kann, lässt sich der Raum mit
`dungeon.tracking.roomId` oder `DUNGEON_TRACKING_ROOM_ID` konfigurieren.

Die Konfiguration allein erzeugt weder eine Sitzung noch eine Outbox. Auf einem
Mehrspielerserver beginnt das Tracking, sobald der erste gültige Client `InitialWorldReady` sendet.
Erhält ein Server nie die Bereitschaftsmeldung eines Spielers, schreibt er keine Tracking-Datei.
Im Einzelspielermodus beginnt das Tracking, nachdem das erste Level geladen wurde und
`Game.player()` den lokalen Spieler enthält. Die Engine erzeugt und verknüpft diesen Teilnehmer
vor dem nächsten Gameplay-Tick.

Die Bereitschaftsgrenze bestimmt die Startzeit der Sitzung. Dungeon erzeugt an diesem Punkt den
Deskriptor und die Outbox und fügt den ersten Teilnehmer hinzu. Der Deskriptor ist der
Startdatensatz der Sitzung. Bootstrap, Wartezeit in der Mehrspieler-Lobby und die erste
Weltübertragung zählen nicht zur erfassten Dauer. Dungeon versucht den Start einmal pro
konfiguriertem Lauf. Nach einem fehlgeschlagenen Start oder dem Ende der Sitzung startet es das
Tracking nicht neu.

Die Raumlogik kann ein Rätsel schon vor dieser Grenze verfügbar machen. Der autoritative Prozess
merkt sich solche Rätselstarts, ohne eine Sitzung oder Outbox anzulegen. Bei Bereitschaft zeichnet
er sie einmalig in ihrer ursprünglichen Reihenfolge direkt nach dem ersten
`PARTICIPANT_JOINED`-Ereignis auf. Ereigniszeit und verstrichene Dauer beginnen deshalb mit der
Bereitschaft, nicht schon während Bootstrap oder Weltübertragung.

Jede konfigurierte Sitzung erzeugt eine neue Datei `<session UUID>.jsonl`. Eine vorhandene Datei
wird nie wiederverwendet. Die erste Zeile enthält den Sitzungsdeskriptor, danach folgen geordnete
Ereignisdatensätze. Eine ordnungsgemäß beendete Sitzung schließt mit einem Abschlussdatensatz. Fehlt
dieser, gilt die Sitzung als unterbrochen. Diese eine Datei enthält alles für den Offline-Import.
Lösche sie erst, wenn das Backend die Sitzung bestätigt oder ein Betreiber sie importiert hat.

Beendet sich ein über "Spiel hosten" gestarteter Server ohne bestätigte Speicherung, übermittelt
sein Statuskanal den absoluten Outbox-Pfad und die konfigurierte Betreiber-E-Mail an den hostenden
Client. Der Client zeigt die Warnung an. Eigenständige Headless-Server schreiben dieselben
Wiederherstellungsangaben in ihr Log.

Lokale Outbox-Fehler stoppen weder das Spiel noch Netzwerk, Abschlussanzeige oder Herunterfahren.
Dungeon protokolliert den fehlgeschlagenen absoluten Pfad und lässt die Wiederherstellungswarnung
offen. Bewahre die gemeldete Outbox zur Prüfung auf, auch wenn der HTTP-Upload deaktiviert war.

Der Raumcode zeichnet nur stabile Rätsel- und Objekt-IDs auf. Die Engine ergänzt Sequenz,
Uhrzeit und monoton verstrichene Zeit:

```java
Tracking.puzzleStarted("storage-access");
Tracking.attempt(
    "storage-access",
    "storage-keypad",
    "keypad-code",
    enteredCode,
    correct,
    participantId);
Tracking.hintUsed("storage-access", "storage-first-digit", participantId);
Tracking.puzzleSolved("storage-access");
Game.complete();
```

Rätselstarts, Rätsellösungen und jede Verwendung eines `(puzzleId, hintId)` werden pro Sitzung
höchstens einmal erfasst. Antwortversuche bleiben vollständig und geordnet. Der Raumcode meldet
Rätselereignisse über `Tracking` und beendet das Spiel über `Game.complete()`. Anschließend beendet
der zentrale Spiellebenszyklus die Tracking-Sitzung.

Die `participantId` kann aus `Tracking.participantForClient(short)` oder
`Tracking.participantForEntity(int)` stammen. Diese UUIDs gelten nur für eine Sitzung. Dungeon
schreibt weder Benutzernamen, Adressen, Client-IDs noch Entity-IDs in Tracking-Dateien.
Unverarbeitete Antworten werden exakt wie übermittelt gespeichert. Betreiber müssen Outboxes daher
als potenziell sensible Daten behandeln und eigene Aufbewahrungs- und Löschregeln festlegen.

Die öffentliche API für Räume besteht aus `Tracking.configureRoom`, `roomId`, `active`,
`outboxPath`, `puzzleStarted`, `attempt`, `hintUsed`, `puzzleSolved`, `participantForClient` und
`participantForEntity`. Die Deployment-Konfiguration stammt aus den aufgeführten Eigenschaften
und Umgebungsvariablen. `TrackingConfig` und sein Builder sind intern im Tracking-Paket.
