# Dungeon tracking

The two tracking modules implement the session tracking data model. `core` contains the
Wizard-independent records and JSON codec. `backend` is a small self-hosted reference service
that accepts those records and stores them in PostgreSQL. It is not a central Dungeon service.

```text
authoritative game server
    -> append-only JSONL outbox on the game host
    -> authenticated HTTP batches with retry
    -> tracking backend on host port 127.0.0.1:8088
    -> private Compose network
    -> runtime role with data access only
    -> PostgreSQL named volume
```

Only the authoritative game process creates session order and elapsed time. Game clients never
receive database credentials. The HTTP boundary accepts types from `tracking:core`; only the
backend can reach PostgreSQL. Compose does not publish the database port, and the host-published
backend port listens only on loopback.

## Docker Compose quick start

Create four independent secret files before the first start. PowerShell:

```powershell
New-Item -ItemType Directory -Force tracking/secrets | Out-Null
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) |
  Set-Content -NoNewline tracking/secrets/postgres_password.txt
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) |
  Set-Content -NoNewline tracking/secrets/tracking_owner_password.txt
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) |
  Set-Content -NoNewline tracking/secrets/tracking_runtime_password.txt
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) |
  Set-Content -NoNewline tracking/secrets/backend_api_key.txt
docker compose -f tracking/compose.yaml up --build -d
docker compose -f tracking/compose.yaml ps
```

Bash:

```bash
mkdir -p tracking/secrets
openssl rand -base64 32 > tracking/secrets/postgres_password.txt
openssl rand -base64 32 > tracking/secrets/tracking_owner_password.txt
openssl rand -base64 32 > tracking/secrets/tracking_runtime_password.txt
openssl rand -base64 32 > tracking/secrets/backend_api_key.txt
chmod 600 tracking/secrets/*.txt
docker compose -f tracking/compose.yaml up --build -d
docker compose -f tracking/compose.yaml ps
```

Compose mounts those files under `/run/secrets`; their values never appear in the Compose file or
image. Git ignores the real files. The `.example` files only document their names and must not be
used as deployed credentials.

The `database` service uses an internal network and the named `postgres_data` volume mounted at
the PostgreSQL 18 data root `/var/lib/postgresql`. It initializes host authentication with
SCRAM-SHA-256. On a new volume, its init script creates a non-superuser schema owner and a separate
non-superuser runtime role. The one-shot `migrate` service connects as the owner, applies migrations,
grants only the required table and column operations to the runtime role, and exits. It grants no
access to the analysis views. The backend starts only after that service succeeds and never
receives the bootstrap or owner password.

Both Java services run as UID/GID 10001, drop all Linux capabilities, have
`no-new-privileges`, and use a read-only root filesystem with a small temporary `/tmp`. The
backend's Java health check calls `GET /health` without adding another network tool to the runtime
image.

The authoritative game server assigns each event a sequence number starting at 1. It writes
every event to a local append-only JSONL outbox and can retry HTTP batches without creating
duplicate database rows. PostgreSQL stores the unchanged payload, including every submitted
answer. `eventType` is one of seven tracked events: `SESSION_STARTED`,
`PARTICIPANT_JOINED`, `PARTICIPANT_LEFT`, `PUZZLE_STARTED`, `ANSWER_SUBMITTED`, `HINT_USED`, or
`PUZZLE_SOLVED`.

## Run locally

Create an empty PostgreSQL database, a schema-owner role, and a separate runtime role. Grant the
runtime role `CONNECT` on the database and `USAGE` on the schema. Run the migration once as the
owner; `DUNGEON_TRACKING_RUNTIME_DATABASE_USER` makes the migrator reconcile the exact runtime
table grants:

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

The backend binds to `127.0.0.1:8088` by default. It does not run DDL at startup. The separate
migration command applies packaged migrations in order and records their versions in
`tracking_schema_migrations`.

Each setting can be an environment variable or a Java system property. A system property wins.

| Purpose | Environment variable | System property | Default |
| --- | --- | --- | --- |
| Bind address | `DUNGEON_TRACKING_BIND_HOST` | `dungeon.tracking.bindHost` | `127.0.0.1` |
| Port | `DUNGEON_TRACKING_PORT` | `dungeon.tracking.port` | `8088` |
| JDBC URL | `DUNGEON_TRACKING_DATABASE_URL` | `dungeon.tracking.databaseUrl` | required |
| Database user | `DUNGEON_TRACKING_DATABASE_USER` | `dungeon.tracking.databaseUser` | JDBC default |
| Database password | `DUNGEON_TRACKING_DATABASE_PASSWORD` | `dungeon.tracking.databasePassword` | JDBC default |
| Database password file | `DUNGEON_TRACKING_DATABASE_PASSWORD_FILE` | `dungeon.tracking.databasePasswordFile` | disabled |
| Runtime grant role, migrator only | `DUNGEON_TRACKING_RUNTIME_DATABASE_USER` | `dungeon.tracking.runtimeDatabaseUser` | disabled |
| Bearer API key | `DUNGEON_TRACKING_API_KEY` | `dungeon.tracking.apiKey` | disabled |
| Bearer API-key file | `DUNGEON_TRACKING_API_KEY_FILE` | `dungeon.tracking.apiKeyFile` | disabled |
| Maximum body bytes | `DUNGEON_TRACKING_MAX_BODY_BYTES` | `dungeon.tracking.maxBodyBytes` | `1048576` |
| Maximum events per batch | `DUNGEON_TRACKING_MAX_BATCH_EVENTS` | `dungeon.tracking.maxBatchEvents` | `500` |

When configured, the API key is required as `Authorization: Bearer <key>` on tracking endpoints.
For each secret, configure either its direct value or its file, never both. Startup fails on a
collision, unreadable file, or empty secret file.
`GET /health` remains unauthenticated so a local process monitor can check PostgreSQL access.
The service does not persist or log source IP addresses, HTTP user agents, or request bodies.

## HTTP API

All request and response bodies use `application/json`.

- `GET /health` returns `200` with `{"status":"ok"}` when PostgreSQL is reachable, otherwise
  `503`.
- `POST /tracking/sessions/{sessionId}/events` accepts a `TrackingBatch`. The descriptor,
  participants, and events must refer to the path session. Every event participant must appear in
  the batch's participant list. Events in a non-empty batch form one contiguous ascending
  sequence. A retry of identical data succeeds. A gap or different content for an existing
  sequence, event ID, or participant ID returns `409`.
- `GET /tracking/sessions/{sessionId}/ack` returns the highest persisted sequence. A session with
  no events returns 0.
- `POST /tracking/sessions/{sessionId}/finish` accepts `TrackingSessionFinish`. The status is
  `COMPLETED` or `ABORTED`. `finalSequence` is the last event the backend must already hold;
  zero means the session has no events. Finish succeeds only when the database contains every
  sequence from 1 through that value. An aborted session may name its last active puzzle.
  Repeating the identical finish succeeds; a different finish returns `409`.

After a successful finish, event upload accepts only identical repetitions of already stored
sequences, participant facts, and the session descriptor. A new fact, sequence, or different
content returns `409`. Ingest and finish serialize on the same session-row lock, so an in-flight
batch either commits before the completeness check or waits until the terminal state is visible.

An unfinished database row has `status = NULL` and `ended_at = NULL`; the null state means only
that no finish request has arrived. Persisted terminal status values are strictly `COMPLETED` or
`ABORTED`.

A minimal first batch looks like this:

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
  "events": [{
    "schemaVersion": 1,
    "sessionId": "25aac31d-bfc4-47f7-90b9-ad449a9e595a",
    "sessionSequence": 1,
    "eventId": "25aac31d-bfc4-47f7-90b9-ad449a9e595a:1",
    "roomId": "the-last-hour",
    "eventType": "SESSION_STARTED",
    "elapsedMonotonicMs": 0,
    "occurredAt": "2026-08-29T12:00:00Z",
    "payload": {}
  }]
}
```

An answer attempt uses `eventType: "ANSWER_SUBMITTED"`, sets `outcome` to `CORRECT` or
`INCORRECT`, and keeps the full answer:

```json
{
  "answer": "0417",
  "answerKind": "keypad-code",
  "attemptNumber": 3
}
```

Only a hint the players actually use produces `eventType: "HINT_USED"`.
Merely showing or offering a hint is not counted.

With the example saved as `batch.json`, PowerShell can upload it and read the acknowledgement:

```powershell
$sessionId = '25aac31d-bfc4-47f7-90b9-ad449a9e595a'
$headers = @{ Authorization = "Bearer $(Get-Content -Raw tracking/secrets/backend_api_key.txt)" }
Invoke-RestMethod -Method Post -Headers $headers -ContentType application/json `
  -InFile batch.json "http://127.0.0.1:8088/tracking/sessions/$sessionId/events"
Invoke-RestMethod -Headers $headers `
  "http://127.0.0.1:8088/tracking/sessions/$sessionId/ack"
```

Send a `TrackingSessionFinish` JSON document to the same session's `/finish` path after the final
event acknowledgement. For the batch above:

```json
{
  "schemaVersion": 1,
  "sessionId": "25aac31d-bfc4-47f7-90b9-ad449a9e595a",
  "finalSequence": 1,
  "status": "COMPLETED",
  "endedAt": "2026-08-29T12:30:00Z",
  "elapsedMonotonicMs": 1800000
}
```

## Database and analysis

`tracking_sessions`, `tracking_participants`, and `tracking_events` are the source tables.
Primary keys on session sequence and participant ID plus a unique event ID make upload retries
idempotent. `tracking_events.payload` and
`tracking_events.event_json` are JSONB. The backend stores full answer payloads without removing
or reshaping fields.

The participant table stores only `session_id`, the session-scoped `participant_id`, and immutable
`room_played_before`. Join, leave, and reconnect times remain canonical events. Sidecars also carry
the first join and current leave time for offline operator context, but the backend does not keep a
second mutable lifecycle summary that stale retries could overwrite.

The migration creates these read-only analysis views:

- `v_session_summary` derives player count, total duration, status, and the aborted puzzle ID,
  retaining `NULL` when the session has none.
- `v_puzzle_summary` derives whether and when a puzzle was solved, its duration, answer attempt
  count, and the number of `HINT_USED` events.
- `v_attempts_answers` exposes each answer attempt with answer kind, attempt number, outcome, full
  answer, and original payload.

The backend runtime role cannot query these views. Create a separate operator role when an analyst
needs access, then grant only database connection, schema usage, and view reads as the schema
owner:

```sql
CREATE ROLE dungeon_tracking_analyst LOGIN;
GRANT CONNECT ON DATABASE dungeon_tracking TO dungeon_tracking_analyst;
GRANT USAGE ON SCHEMA public TO dungeon_tracking_analyst;
GRANT SELECT ON v_session_summary, v_puzzle_summary, v_attempts_answers
    TO dungeon_tracking_analyst;
```

Set that role's password interactively with `\password dungeon_tracking_analyst`; do not place it
in the backend or Compose secrets. The runtime grant reconciliation first revokes prior table and
view privileges, so rerunning the migrator also removes grants from older, broader configurations.

## Import a JSONL outbox

The game writes events as one `TrackingEvent` per JSONL line. In the same directory it writes
`<sessionId>.session.json`, zero or more
`<sessionId>.participant-<participantId>.json` files, and optional terminal facts in
`<sessionId>.finish.json`. The importer discovers those sidecars from the first outbox event and
sends them through the same endpoint and idempotency rules as a running game:

```powershell
./gradlew.bat :tracking:backend:importOutbox --args='--url http://127.0.0.1:8088 --outbox 25aac31d-bfc4-47f7-90b9-ad449a9e595a.jsonl'
```

For an empty outbox, its filename must be `<sessionId>.jsonl` or `<sessionId>.events.jsonl` so the
importer can locate the sidecars. Explicit `--session`, repeated `--participant`, and `--finish`
arguments override discovery. Add `--api-key-file tracking/secrets/backend_api_key.txt` when the
backend requires one and `--batch-size <n>` to stay under a custom server limit. Alternatively,
the importer reads `DUNGEON_TRACKING_API_KEY`. An explicit API-key file takes precedence over the
environment variable. The importer prints counts and the final sequence only. It never prints API
keys, event payloads, or answers.

The importer identifies the last line boundary from raw bytes and strictly decodes the completed
prefix as UTF-8. If a crash leaves an unterminated tail that ends inside JSON or a multibyte UTF-8
character, it warns on stderr, ignores only that tail, and imports every completed event. A valid
unterminated final event is imported. Invalid UTF-8 or JSON in an earlier line, or in any line that
ends with a line terminator, remains a hard error.

## Configure the game

The game host needs the backend URL and the same API key, but no JDBC URL, database role, or
database password. Configure the room as usual and set these deployment values before starting
the authoritative server:

```powershell
$env:DUNGEON_TRACKING_ENDPOINT = 'http://127.0.0.1:8088'
$env:DUNGEON_TRACKING_API_KEY = Get-Content -Raw tracking/secrets/backend_api_key.txt
$env:DUNGEON_TRACKING_OUTBOX = 'tracking-outbox'
```

The equivalent Java properties are `dungeon.tracking.endpoint`, `dungeon.tracking.apiKey`, and
`dungeon.tracking.outbox`. The engine always writes the outbox, even while uploads work. If the
backend is unavailable, the session remains playable and the operator can run the importer later.

## Operate and update the stack

Inspect health and logs without printing request payloads:

```powershell
docker compose -f tracking/compose.yaml ps
Invoke-RestMethod http://127.0.0.1:8088/health
docker compose -f tracking/compose.yaml logs backend database
docker compose -f tracking/compose.yaml logs migrate
```

Stop containers while preserving PostgreSQL data with
`docker compose -f tracking/compose.yaml down`. Adding `--volumes` deletes the database volume and
must only be used when the stored tracking data is intentionally discarded.

Create a logical backup before upgrades:

```powershell
docker compose -f tracking/compose.yaml exec -T database pg_dump `
  -U dungeon_tracking_owner -d dungeon_tracking -Fc > dungeon-tracking.dump
```

Bash uses the same command without PowerShell's line-continuation backtick:

```bash
docker compose -f tracking/compose.yaml exec -T database \
  pg_dump -U dungeon_tracking_owner -d dungeon_tracking -Fc > dungeon-tracking.dump
```

Restore into an empty `dungeon_tracking` database:

```bash
docker compose -f tracking/compose.yaml exec -T database \
  pg_restore -U dungeon_tracking_owner -d dungeon_tracking --clean --if-exists < dungeon-tracking.dump
```

From PowerShell, run the same input redirection through `cmd.exe`:

```powershell
cmd.exe /c "docker compose -f tracking/compose.yaml exec -T database pg_restore -U dungeon_tracking_owner -d dungeon_tracking --clean --if-exists < dungeon-tracking.dump"
```

For an upgrade, make a backup, review PostgreSQL and Temurin release notes, update both the exact
tag and multi-architecture digest, rebuild with `docker compose -f tracking/compose.yaml build
--pull`, and start with `docker compose -f tracking/compose.yaml up -d`. Check both health states
and query the summary views afterward. PostgreSQL major-version upgrades need PostgreSQL's own
dump/restore or `pg_upgrade` procedure; replacing the image does not migrate the named volume.

The tracking deployment pins PostgreSQL `18.6-alpine3.23`, Temurin JDK/JRE `25.0.4_7` on Ubuntu
Noble, and pgJDBC `42.7.13`. Dependabot checks the Gradle, backend Dockerfile, and Compose
directories weekly. Digest changes still need review because the tag alone is not the deployed
identity.

## Data boundary and deployment responsibility

Participant UUIDs are created for one session only. `roomPlayedBefore` may come from a resettable
client-local flag, but the backend has no person table, name, IP address, device fingerprint, or
stable cross-session identifier. The protocol contains no operator contact, hardcoded endpoint,
privacy-notice field, or retention field.

Whoever deploys an instance chooses its endpoint, credentials, network controls, participant
information, and deletion schedule. Full answers can contain personal text, so the operator must
define and communicate the lawful purpose and deletion process outside this technical event
model. Do not expose the default unauthenticated service beyond localhost. For a remote deployment,
put it behind a TLS reverse proxy, keep API-key authentication enabled, restrict source networks,
and keep PostgreSQL reachable only from the backend network. Rotate the API key by replacing its
file and recreating the backend container. For an existing PostgreSQL volume, rotate
`dungeon_tracking_owner` and `dungeon_tracking_runtime` with PostgreSQL's interactive `\password`
command, update the matching owner or runtime secret file, and recreate the affected Java service.
The bootstrap `postgres_password.txt` is read only while PostgreSQL initializes an empty volume;
changing that file later does not change the stored password. The Compose defaults are a local
baseline, not an Internet-facing ingress configuration.
