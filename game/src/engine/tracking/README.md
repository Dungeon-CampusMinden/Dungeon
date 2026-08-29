# Tracking rooms

Tracking runs only in the authoritative server process or in singleplayer. Configure a stable room
ID before `Game.run()`:

```java
Tracking.configureRoom("my-room");
```

`configureRoom` keeps deployment settings separate from room code. The deployment may set:

| System property | Environment variable | Default |
| --- | --- | --- |
| `dungeon.tracking.endpoint` | `DUNGEON_TRACKING_ENDPOINT` | no remote upload |
| `dungeon.tracking.apiKey` | `DUNGEON_TRACKING_API_KEY` | none |
| `dungeon.tracking.outbox` | `DUNGEON_TRACKING_OUTBOX` | `tracking-outbox` |
| `dungeon.tracking.operatorContact` | `DUNGEON_TRACKING_OPERATOR_CONTACT` | none |

The optional endpoint must be an absolute HTTP(S) URI without user-info, query, or fragment.
Plain HTTP remains valid for a locally hosted backend.

The Host Game menu copies nonblank `dungeon.tracking.*` system-property values into the managed
server's environment. They override inherited `DUNGEON_TRACKING_*` values in the same way as in
the hosting process. The API key is never added to the child JVM's command line.

For deployments that cannot change room code, `dungeon.tracking.roomId` or
`DUNGEON_TRACKING_ROOM_ID` configures the room.

Every configured session creates a new `<session UUID>.jsonl` file. It never reuses an existing
file. The adjacent `session.json`, anonymous `participant-<UUID>.json`, and optional `finish.json`
files contain the facts required for offline import. Do not delete these files until the backend
has acknowledged the session or an operator has imported the outbox.

If a server started by the Host Game menu exits with unconfirmed persistence, its managed-process
status channel sends the absolute outbox path and the optional configured operator contact to the
hosting client. The hosting client shows the warning. Standalone headless servers write the same
recovery details to their log.

Local outbox or sidecar failures never stop gameplay, networking, terminal presentation, or
shutdown. Dungeon logs the failed absolute path and keeps the recovery warning pending. A failed
participant sidecar may lose that participant's tracking data for the session, but does not reject
the player's join or disconnect at the gameplay layer. Keep the reported outbox and neighboring
sidecars for inspection even when no remote endpoint was configured.

Room code records only stable puzzle and object IDs. The engine supplies sequence, event ID,
wall-clock time, and monotonic elapsed time:

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
Tracking.completed();
```

Puzzle starts, puzzle solutions, and each `(puzzleId, hintId)` use are recorded at most once per
session. Answer attempts remain complete and ordered.

`participantId` can come from `Tracking.participantForClient(short)` or
`Tracking.participantForEntity(int)`. These UUIDs exist only for one session. Dungeon never writes
usernames, addresses, client IDs, or entity IDs to tracking files. Raw answers are stored exactly as
submitted, so operators must handle outboxes as potentially sensitive data and define
their own retention and deletion procedure.
