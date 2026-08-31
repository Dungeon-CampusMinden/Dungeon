# Dungeon-Tracking

Die beiden Tracking-Module implementieren das Datenmodell für Sitzungstracking. `core` enthält die
vom Wizard unabhängigen Datensätze und den JSON-Codec. `backend` ist ein kleiner selbst gehosteter
Referenzdienst, der diese Datensätze entgegennimmt und in PostgreSQL speichert. Es ist kein
zentraler Dungeon-Dienst.

```text
autoritativer Spielserver
    -> nur wachsende JSONL-Outbox auf dem Spielhost
    -> authentifizierte HTTP-Batches mit Wiederholung
    -> Tracking-Backend auf Hostport 127.0.0.1:8088
    -> separates API- und internes Datenbanknetzwerk
    -> Laufzeitrolle nur mit Datenzugriff
    -> benanntes PostgreSQL-Volume
```

Nur der autoritative Spielprozess erzeugt die Sitzungsreihenfolge und die verstrichene Zeit.
Spielclients erhalten nie Datenbankzugangsdaten. Die HTTP-Grenze akzeptiert Typen aus
`tracking:core`; nur das Backend erreicht PostgreSQL. Compose veröffentlicht den Datenbankport
nicht. Der auf dem Host veröffentlichte Backend-Port lauscht nur auf Loopback.

## Schnellstart mit Docker Compose

Setze vor dem ersten Start vier voneinander unabhängige Zufallswerte in der Hostumgebung.
PowerShell:

```powershell
function New-TrackingSecret {
  [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
}
$env:DUNGEON_TRACKING_POSTGRES_PASSWORD = (New-TrackingSecret)
$env:DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD = (New-TrackingSecret)
$env:DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD = (New-TrackingSecret)
$env:DUNGEON_TRACKING_API_KEY = (New-TrackingSecret)
docker compose -f tracking/compose.yaml up --build -d
docker compose -f tracking/compose.yaml ps
```

Bash:

```bash
export DUNGEON_TRACKING_POSTGRES_PASSWORD="$(openssl rand -base64 32)"
export DUNGEON_TRACKING_OWNER_DATABASE_PASSWORD="$(openssl rand -base64 32)"
export DUNGEON_TRACKING_RUNTIME_DATABASE_PASSWORD="$(openssl rand -base64 32)"
export DUNGEON_TRACKING_API_KEY="$(openssl rand -base64 32)"
docker compose -f tracking/compose.yaml up --build -d
docker compose -f tracking/compose.yaml ps
```

Compose verlangt alle vier Hostvariablen. Fehlt ein Datenbankwert, ist er leer oder besteht er nur
aus Leerraum, bricht Compose noch vor der PostgreSQL-Initialisierung ab. Speichere die Werte in
der Deployment- oder Dienstumgebung, vorzugsweise in deren Secret-Verwaltung. Lege ihre Werte
nicht in diesem Repository ab. Behalte sie über Neustarts und Compose-Befehle hinweg bei. Erzeuge
insbesondere keine neuen Datenbankpasswörter für ein vorhandenes `postgres_data`-Volume, da
PostgreSQL die bei dessen Initialisierung angelegten Zugangsdaten beibehält.

Die Dienste `database` und `migrate` nutzen ausschließlich das interne Datenbanknetzwerk. Das
Backend ist zusätzlich mit dem API-Netzwerk verbunden, über das Docker den Loopback-Port
veröffentlicht. PostgreSQL selbst besitzt keine Verbindung zu diesem Netzwerk und keinen
veröffentlichten Hostport. Das benannte Volume `postgres_data` ist am Datenstamm von PostgreSQL 18
unter `/var/lib/postgresql` eingehängt. Die Hostauthentifizierung wird mit SCRAM-SHA-256
initialisiert. Bei einem neuen Volume legt das Initialisierungsskript einen Schema-Eigentümer ohne
Superuser-Rechte und eine getrennte Laufzeitrolle ebenfalls ohne Superuser-Rechte an. Der einmalig
laufende Dienst `migrate` verbindet sich als Eigentümer, wendet Migrationen an, gewährt der
Laufzeitrolle nur die benötigten Tabellen- und Spaltenoperationen und beendet sich. Er gewährt
keinen Zugriff auf die Analyse-Views. Das Backend startet erst nach erfolgreichem Abschluss dieses
Dienstes und erhält weder das Bootstrap- noch das Eigentümerpasswort.

Beide Java-Dienste laufen mit UID/GID 10001, legen alle Linux-Capabilities ab, verwenden
`no-new-privileges` und haben ein schreibgeschütztes Root-Dateisystem mit einem kleinen temporären
`/tmp`. Der Java-Healthcheck des Backends ruft `GET /health` auf, ohne dem Laufzeit-Image ein
weiteres Netzwerkwerkzeug hinzuzufügen.

Der autoritative Spielserver vergibt für jedes Ereignis eine Sequenznummer ab 1. Er schreibt jedes
Ereignis in eine lokale, nur wachsende JSONL-Outbox und kann HTTP-Batches wiederholen, ohne
doppelte Datenbankzeilen zu erzeugen. PostgreSQL speichert die unveränderte Nutzlast einschließlich
jeder übermittelten Antwort. Der Sitzungsdeskriptor ist der Startdatensatz. `eventType` ist eines
von sechs erfassten Ereignissen: `PARTICIPANT_JOINED`, `PARTICIPANT_LEFT`, `PUZZLE_STARTED`,
`ANSWER_SUBMITTED`, `HINT_USED` oder `PUZZLE_SOLVED`.

## Lokal ausführen

Lege eine leere PostgreSQL-Datenbank, eine Rolle als Schema-Eigentümer und eine getrennte
Laufzeitrolle an. Gewähre der Laufzeitrolle `CONNECT` auf die Datenbank und `USAGE` auf das Schema.
Führe die Migration einmal als Eigentümer aus. Mit `DUNGEON_TRACKING_RUNTIME_DATABASE_USER`
gleicht der Migrator die exakten Tabellenrechte der Laufzeitrolle ab:

```powershell
$env:DUNGEON_TRACKING_DATABASE_URL = 'jdbc:postgresql://127.0.0.1:5432/dungeon_tracking'
$env:DUNGEON_TRACKING_DATABASE_USER = 'dungeon_tracking_owner'
$env:DUNGEON_TRACKING_DATABASE_PASSWORD = 'replace-owner-password'
$env:DUNGEON_TRACKING_RUNTIME_DATABASE_USER = 'dungeon_tracking_runtime'
./gradlew.bat :tracking:backend:migrateDatabase
$env:DUNGEON_TRACKING_DATABASE_USER = 'dungeon_tracking_runtime'
$env:DUNGEON_TRACKING_DATABASE_PASSWORD = 'replace-runtime-password'
Remove-Item Env:DUNGEON_TRACKING_RUNTIME_DATABASE_USER
./gradlew.bat :tracking:backend:run
```

Das Backend bindet standardmäßig an `127.0.0.1:8088`. Beim Start führt es kein DDL aus. Der
getrennte Migrationsbefehl wendet die enthaltenen Migrationen der Reihe nach an und vermerkt ihre
Versionen in `tracking_schema_migrations`.

Jede Einstellung kann als Umgebungsvariable oder Java-Systemeigenschaft gesetzt werden. Die
Systemeigenschaft hat Vorrang.

| Zweck | Umgebungsvariable | Systemeigenschaft | Standardwert |
| --- | --- | --- | --- |
| Bindeadresse | `DUNGEON_TRACKING_BIND_HOST` | `dungeon.tracking.bindHost` | `127.0.0.1` |
| Port | `DUNGEON_TRACKING_PORT` | `dungeon.tracking.port` | `8088` |
| JDBC-URL | `DUNGEON_TRACKING_DATABASE_URL` | `dungeon.tracking.databaseUrl` | erforderlich |
| Datenbankbenutzer | `DUNGEON_TRACKING_DATABASE_USER` | `dungeon.tracking.databaseUser` | JDBC-Standard |
| Datenbankpasswort | `DUNGEON_TRACKING_DATABASE_PASSWORD` | `dungeon.tracking.databasePassword` | JDBC-Standard |
| Rolle für Laufzeitrechte, nur Migrator | `DUNGEON_TRACKING_RUNTIME_DATABASE_USER` | `dungeon.tracking.runtimeDatabaseUser` | deaktiviert |
| Bearer-API-Schlüssel | `DUNGEON_TRACKING_API_KEY` | `dungeon.tracking.apiKey` | deaktiviert |
| Maximale Anfragegröße in Bytes | `DUNGEON_TRACKING_MAX_BODY_BYTES` | `dungeon.tracking.maxBodyBytes` | `1048576` |
| Maximale Ereignisse pro Batch | `DUNGEON_TRACKING_MAX_BATCH_EVENTS` | `dungeon.tracking.maxBatchEvents` | `500` |

Ist der API-Schlüssel konfiguriert, verlangen die Tracking-Endpunkte
`Authorization: Bearer <key>`. Ein explizit konfiguriertes Datenbankpasswort oder ein API-Schlüssel
muss mindestens ein Zeichen enthalten, das kein Leerraum ist. Das Backend entfernt Leerraum am
Anfang und Ende des API-Schlüssels, verwendet Datenbankpasswörter aber exakt wie konfiguriert.
`GET /health` bleibt ohne Authentifizierung erreichbar, damit ein lokaler Prozessmonitor den
PostgreSQL-Zugriff prüfen kann. Der Dienst speichert oder protokolliert weder Quell-IP-Adressen,
HTTP-User-Agents noch Anfrage-Bodys.

## HTTP-API

Alle Anfrage- und Antwort-Bodys verwenden `application/json`.

- `GET /health` gibt `200` mit `{"status":"ok"}` zurück, wenn PostgreSQL erreichbar ist, sonst
  `503`.
- `POST /tracking/sessions/{sessionId}/events` akzeptiert einen `TrackingBatch`. Deskriptor,
  Teilnehmer und Ereignisse müssen zur Sitzung im Pfad gehören. Jeder Teilnehmer eines Ereignisses
  muss in der Teilnehmerliste des Batches stehen. Die Ereignisse eines nicht leeren Batches bilden
  eine lückenlose aufsteigende Sequenz. Das Wiederholen identischer Daten gelingt. Eine Lücke oder
  abweichender Inhalt für eine vorhandene Sequenz oder Teilnehmer-ID ergibt `409`.
- `GET /tracking/sessions/{sessionId}/ack` gibt die höchste gespeicherte Sequenz zurück. Eine Sitzung
  ohne Ereignisse gibt 0 zurück.
- `POST /tracking/sessions/{sessionId}/finish` akzeptiert `TrackingSessionFinish`. Der Status ist
  `COMPLETED` oder `ABORTED`. `finalSequence` ist das letzte Ereignis, das dem Backend bereits
  vorliegen muss. Null bedeutet, dass die Sitzung keine Ereignisse hat. Der Abschluss gelingt nur,
  wenn die Datenbank jede Sequenz von 1 bis zu diesem Wert enthält. Eine abgebrochene Sitzung kann
  ihr zuletzt aktives Rätsel nennen. Das Wiederholen eines identischen Abschlusses gelingt; ein
  abweichender Abschluss ergibt `409`.

Nach einem erfolgreichen Abschluss akzeptiert der Ereignis-Upload nur noch identische
Wiederholungen bereits gespeicherter Sequenzen, Teilnehmerdaten und des Sitzungsdeskriptors. Neue
Daten, Sequenzen oder abweichende Inhalte ergeben `409`. Aufnahme und Abschluss sperren dieselbe
Sitzungszeile. Ein laufender Batch wird daher entweder vor der Vollständigkeitsprüfung festgeschrieben
oder wartet, bis der Endzustand sichtbar ist.

Eine nicht abgeschlossene Datenbankzeile hat `status = NULL` und `ended_at = NULL`. Dieser
Nullzustand bedeutet nur, dass noch keine Abschlussanfrage eingetroffen ist. Gespeicherte
Endstatuswerte sind ausschließlich `COMPLETED` oder `ABORTED`.

So sieht ein minimaler erster Batch ohne Ereignisse aus:

```json
{
  "schemaVersion": 1,
  "session": {
    "schemaVersion": 1,
    "sessionId": "25aac31d-bfc4-47f7-90b9-ad449a9e595a",
    "roomId": "the-last-hour",
    "startedAt": "2026-08-29T12:00:00Z"
  },
  "participants": [],
  "events": []
}
```

Ein Antwortversuch verwendet `eventType: "ANSWER_SUBMITTED"`, setzt `outcome` auf `CORRECT` oder
`INCORRECT` und behält die vollständige Antwort:

```json
{
  "answer": "0417",
  "answerKind": "keypad-code",
  "attemptNumber": 3
}
```

Nur ein tatsächlich von den Spielern verwendeter Hinweis erzeugt `eventType: "HINT_USED"`. Das
bloße Anzeigen oder Anbieten eines Hinweises zählt nicht.

Ist das Beispiel als `batch.json` gespeichert, kann PowerShell es hochladen und die Bestätigung
abrufen:

```powershell
$sessionId = '25aac31d-bfc4-47f7-90b9-ad449a9e595a'
$headers = @{ Authorization = "Bearer $env:DUNGEON_TRACKING_API_KEY" }
Invoke-RestMethod -Method Post -Headers $headers -ContentType application/json `
  -InFile batch.json "http://127.0.0.1:8088/tracking/sessions/$sessionId/events"
Invoke-RestMethod -Headers $headers `
  "http://127.0.0.1:8088/tracking/sessions/$sessionId/ack"
```

Sende nach der Bestätigung des vollständigen Batches ein JSON-Dokument `TrackingSessionFinish` an
den Pfad `/finish` derselben Sitzung. Für den ereignislosen Batch oben:

```json
{
  "schemaVersion": 1,
  "sessionId": "25aac31d-bfc4-47f7-90b9-ad449a9e595a",
  "finalSequence": 0,
  "status": "COMPLETED",
  "endedAt": "2026-08-29T12:30:00Z",
  "elapsedMonotonicMs": 1800000
}
```

## Datenbank und Analyse

`tracking_sessions`, `tracking_participants` und `tracking_events` sind die Quelltabellen.
Die Primärschlüssel aus `(session_id, session_sequence)` und `(session_id, participant_id)` machen
Upload-Wiederholungen idempotent. `tracking_events.payload` und `tracking_events.event_json` sind
JSONB. Das Backend speichert vollständige Antwortnutzlasten, ohne Felder zu entfernen oder
umzuformen.

Die Teilnehmertabelle speichert nur `session_id`, die sitzungsgebundene `participant_id` und das
unveränderliche `room_played_before`. Zeitpunkte für Beitritt, Verlassen und erneute Verbindung
bleiben kanonische Ereignisse. Der Outbox-Importer rekonstruiert daraus den ersten Beitritt, den
aktuellen Verlassen-Status und die Angabe zum früheren Spiel des Teilnehmers. Das Backend führt
keine zweite veränderliche Lebenszyklus-Zusammenfassung, die veraltete Wiederholungen überschreiben
könnten.

Die Migration erzeugt folgende schreibgeschützte Analyse-Views:

- `v_session_summary` leitet Spielerzahl, Gesamtdauer, Status und die ID des beim Abbruch aktiven
  Rätsels ab. Hat die Sitzung keines, bleibt der Wert `NULL`.
- `v_puzzle_summary` leitet ab, ob und wann ein Rätsel gelöst wurde, wie lange es dauerte, wie viele
  Antwortversuche es gab und wie viele `HINT_USED`-Ereignisse auftraten.
- `v_attempts_answers` stellt jeden Antwortversuch mit Antwortart, Versuchsnummer, Ergebnis,
  vollständiger Antwort und ursprünglicher Nutzlast bereit.

Die Laufzeitrolle des Backends kann diese Views nicht abfragen. Benötigt ein Analyst Zugriff, lege
eine eigene Betreiberrolle an und gewähre als Schema-Eigentümer nur die Verbindung zur Datenbank,
die Schemanutzung und Lesezugriff auf die Views:

```sql
CREATE ROLE dungeon_tracking_analyst LOGIN;
GRANT CONNECT ON DATABASE dungeon_tracking TO dungeon_tracking_analyst;
GRANT USAGE ON SCHEMA public TO dungeon_tracking_analyst;
GRANT SELECT ON v_session_summary, v_puzzle_summary, v_attempts_answers
    TO dungeon_tracking_analyst;
```

Setze das Passwort dieser Rolle interaktiv mit `\password dungeon_tracking_analyst`. Lege es nicht
in der Backend- oder Compose-Umgebung ab. Der Abgleich der Laufzeitrechte entzieht zuerst frühere
Tabellen- und View-Rechte. Erneutes Ausführen des Migrators entfernt daher auch Rechte aus älteren,
weiter gefassten Konfigurationen.

## Eine JSONL-Outbox importieren

Das Spiel schreibt für jede Sitzung genau eine Datei `<sessionId>.jsonl`. Erfassung und Spiel
benötigen kein laufendes Backend. Der erste typisierte Datensatz enthält den Sitzungsdeskriptor.
Darauf folgen typisierte Ereignisdatensätze in Sequenzreihenfolge. Eine ordnungsgemäß geschlossene
Sitzung hat einen letzten Abschlussdatensatz.

Der Importbefehl benötigt dagegen ein laufendes Backend. Er importiert eine JSONL-Datei über
dieselbe HTTP-API wie die Live-Übertragung, rekonstruiert Teilnehmerdaten aus
`PARTICIPANT_JOINED`- und `PARTICIPANT_LEFT`-Ereignissen, sendet Ereignisse in Batches und überträgt
einen vorhandenen Abschlussdatensatz. Wiederholungen sind nach den normalen API-Regeln idempotent:

```powershell
./gradlew.bat :tracking:backend:importOutbox --args='--url http://127.0.0.1:8088 --outbox 25aac31d-bfc4-47f7-90b9-ad449a9e595a.jsonl'
```

Setze `DUNGEON_TRACKING_API_KEY`, wenn das Backend Authentifizierung verlangt. Mit
`--batch-size <n>` bleibt der Importer unter einem abweichenden Serverlimit. Der Importer gibt nur
Anzahlen und die letzte Sequenz aus. API-Schlüssel, Ereignisnutzlasten oder Antworten gibt er nie
aus.

Der Importer ermittelt die letzte Zeilengrenze aus den Rohbytes und decodiert den vollständigen
Präfix strikt als UTF-8. Hinterlässt ein Absturz einen nicht abgeschlossenen Rest, der mitten in
JSON oder einem mehrbyteigen UTF-8-Zeichen endet, warnt er auf stderr und ignoriert nur diesen
Rest. Alle vorherigen vollständigen Ereignisse bleiben importierbar. Ein abgeschnittener Abschluss
lässt die importierte Sitzung daher offen. Ein gültiger letzter Datensatz ohne abschließenden
Zeilentrenner wird importiert. Diese Wiederherstellung gilt nur für einen unvollständigen letzten
Rest. Ungültiges UTF-8 oder JSON in einer früheren Zeile oder in einer Zeile mit abschließendem
Zeilentrenner bleibt ein harter Fehler.

## Das Spiel konfigurieren

Der Spielhost benötigt die Backend-URL und denselben API-Schlüssel, aber keine JDBC-URL,
Datenbankrolle oder Datenbankpasswort. Konfiguriere den Raum wie gewohnt und setze vor dem Start
des autoritativen Servers diese Deployment-Werte:

```powershell
$env:DUNGEON_TRACKING_ENDPOINT = 'http://127.0.0.1:8088'
$env:DUNGEON_TRACKING_API_KEY = 'same-value-configured-for-the-backend'
$env:DUNGEON_TRACKING_OUTBOX = 'tracking-outbox'
```

Die entsprechenden Java-Eigenschaften sind `dungeon.tracking.endpoint`,
`dungeon.tracking.apiKey` und `dungeon.tracking.outbox`. Die Engine schreibt die Outbox immer, auch
bei funktionierendem Upload. Ist das Backend nicht erreichbar, bleibt die Sitzung spielbar und
der Betreiber kann später den Importer ausführen.

## Stack betreiben und aktualisieren

Prüfe Zustand und Logs, ohne Anfragenutzlasten auszugeben:

```powershell
docker compose -f tracking/compose.yaml ps
Invoke-RestMethod http://127.0.0.1:8088/health
docker compose -f tracking/compose.yaml logs backend database
docker compose -f tracking/compose.yaml logs migrate
```

`docker compose -f tracking/compose.yaml down` stoppt die Container und bewahrt die
PostgreSQL-Daten. Die zusätzliche Option `--volumes` löscht das Datenbank-Volume und darf nur
verwendet werden, wenn die gespeicherten Tracking-Daten absichtlich verworfen werden sollen.

Erstelle vor Aktualisierungen eine logische Sicherung:

```powershell
docker compose -f tracking/compose.yaml exec -T database pg_dump `
  -U dungeon_tracking_owner -d dungeon_tracking -Fc > dungeon-tracking.dump
```

Bash verwendet denselben Befehl ohne PowerShell-Backtick zur Zeilenfortsetzung:

```bash
docker compose -f tracking/compose.yaml exec -T database \
  pg_dump -U dungeon_tracking_owner -d dungeon_tracking -Fc > dungeon-tracking.dump
```

Stelle die Sicherung in einer leeren Datenbank `dungeon_tracking` wieder her:

```bash
docker compose -f tracking/compose.yaml exec -T database \
  pg_restore -U dungeon_tracking_owner -d dungeon_tracking --clean --if-exists < dungeon-tracking.dump
```

Führe dieselbe Eingabeumleitung unter PowerShell über `cmd.exe` aus:

```powershell
cmd.exe /c "docker compose -f tracking/compose.yaml exec -T database pg_restore -U dungeon_tracking_owner -d dungeon_tracking --clean --if-exists < dungeon-tracking.dump"
```

Erstelle für eine Aktualisierung eine Sicherung, lies die Versionshinweise von PostgreSQL und
Temurin und aktualisiere sowohl das exakte Tag als auch den Multiarchitektur-Digest. Baue danach
mit `docker compose -f tracking/compose.yaml build --pull` neu und starte mit
`docker compose -f tracking/compose.yaml up -d`. Prüfe anschließend beide Health-Zustände und frage
die Zusammenfassungs-Views ab. Ein Wechsel der PostgreSQL-Hauptversion erfordert das
Dump-/Restore- oder `pg_upgrade`-Verfahren von PostgreSQL. Der Austausch des Images migriert das
benannte Volume nicht.

Das Tracking-Deployment fixiert PostgreSQL `18.6-alpine3.23`, Temurin JDK/JRE `25.0.4_7` auf
Ubuntu Noble und pgJDBC `42.7.13`. Dependabot prüft die Gradle-, Backend-Dockerfile- und
Compose-Verzeichnisse wöchentlich. Digest-Änderungen müssen weiterhin geprüft werden, da das Tag
allein nicht die tatsächlich eingesetzte Version bestimmt.

## Datengrenze und Verantwortung für das Deployment

Teilnehmer-UUIDs werden nur für eine Sitzung erzeugt. `roomPlayedBefore` kann aus einem
zurücksetzbaren clientlokalen Kennzeichen stammen. Das Backend enthält jedoch weder eine
Personentabelle noch Namen, IP-Adresse, Gerätefingerabdruck oder eine stabile sitzungsübergreifende
Kennung. Das Protokoll enthält keinen Betreiberkontakt, fest einprogrammierten Endpunkt, Feld für
Datenschutzhinweise oder Aufbewahrungsfeld.

Vollständige übermittelte Antworten einschließlich Freitext werden unverändert in der JSONL-Outbox
und in PostgreSQL gespeichert. Freitext kann personenbezogene Daten enthalten. Diese
Implementierung ist daher nicht automatisch oder zwingend datenschutzkonform. Der Betreiber muss
einen rechtmäßigen Zweck festlegen, Teilnehmer angemessen informieren und Zugriffs-, Aufbewahrungs-
und Löschregeln definieren. Dies ist ein technischer Hinweis, keine Rechtsberatung.

Wer eine Instanz betreibt, bestimmt Endpunkt, Zugangsdaten, Netzwerkkontrollen,
Teilnehmerinformationen und Löschplan. Stelle den standardmäßig nicht authentifizierten Dienst
nicht außerhalb von localhost bereit. Schalte bei einem entfernten Deployment einen
TLS-Reverse-Proxy davor, aktiviere die API-Schlüssel-Authentifizierung, beschränke die Quellnetze und
halte PostgreSQL nur aus dem Backend-Netzwerk erreichbar. Rotiere den API-Schlüssel, indem du
`DUNGEON_TRACKING_API_KEY` für Backend und alle Clients aktualisierst und anschließend den
Backend-Container neu erstellst. Rotiere bei einem vorhandenen PostgreSQL-Volume
`dungeon_tracking_owner` und `dungeon_tracking_runtime` mit dem interaktiven PostgreSQL-Befehl
`\password`, aktualisiere den zugehörigen Wert in der Hostumgebung und erstelle den betroffenen
Java-Dienst neu. `DUNGEON_TRACKING_POSTGRES_PASSWORD` initialisiert ein leeres Volume. Eine spätere
Änderung ändert das gespeicherte Passwort nicht. Die Compose-Standardwerte sind eine lokale
Ausgangskonfiguration, keine über das Internet erreichbare Ingress-Konfiguration.
