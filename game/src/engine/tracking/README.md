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

Configuration alone does not create a session or an outbox. On a multiplayer server, tracking
starts when the first valid client sends `InitialWorldReady`. A server that never receives a ready
player writes no tracking file. In singleplayer, tracking starts after the initial level has loaded
and `Game.player()` contains the local player. The engine creates and associates that participant
before the first following gameplay tick.

The readiness boundary sets the session start time. Dungeon creates the descriptor and outbox,
writes `SESSION_STARTED`, and joins the first participant at that point. Bootstrap, multiplayer
lobby time, and initial world transfer are not part of the tracked duration. Dungeon attempts this
start once per configured run. It does not restart tracking after a start failure or after the
session finishes.

Room logic may make a puzzle available before that boundary. The authoritative process remembers
such puzzle starts without creating a session or outbox. At readiness it records them once, in
their original order, directly after the first `PARTICIPANT_JOINED` event. Their event time and
elapsed duration therefore begin at readiness rather than during bootstrap or world transfer.

Every configured session creates a new `<session UUID>.jsonl` file. It never reuses an existing
file. The first line contains the session descriptor, followed by ordered event records. A cleanly
closed session ends with a finish record. A missing finish record marks an interrupted session.
This one file contains everything required for offline import. Do not delete it until the backend
has acknowledged the session or an operator has imported it.

If a server started by the Host Game menu exits with unconfirmed persistence, its managed-process
status channel sends the absolute outbox path and the optional configured operator contact to the
hosting client. The hosting client shows the warning. Standalone headless servers write the same
recovery details to their log.

Local outbox failures never stop gameplay, networking, terminal presentation, or shutdown.
Dungeon logs the failed absolute path and keeps the recovery warning pending. Keep the reported
outbox for inspection even when no remote endpoint was configured.

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
Game.complete();
```

Puzzle starts, puzzle solutions, and each `(puzzleId, hintId)` use are recorded at most once per
session. Answer attempts remain complete and ordered. Room code reports puzzle events through
`Tracking`; it ends gameplay through `Game.complete()`. The central game lifecycle then
finishes the tracking session.

`participantId` can come from `Tracking.participantForClient(short)` or
`Tracking.participantForEntity(int)`. These UUIDs exist only for one session. Dungeon never writes
usernames, addresses, client IDs, or entity IDs to tracking files. Raw answers are stored exactly as
submitted, so operators must handle outboxes as potentially sensitive data and define
their own retention and deletion procedure.

The public room-facing API consists of `Tracking.configureRoom`, `roomId`, `active`, `outboxPath`,
`puzzleStarted`, `attempt`, `hintUsed`, `puzzleSolved`, `participantForClient`, and
`participantForEntity`. Deployment configuration is read from the listed properties and
environment variables. `TrackingConfig` and its builder are internal to the tracking package.
