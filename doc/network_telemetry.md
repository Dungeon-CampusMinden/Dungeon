# Network Telemetry Overlay

The network telemetry overlay is a debug view for multiplayer transport, snapshot synchronization,
and frame/dispatch timing. It is intended to answer four questions:

1. Is UDP healthy?
2. Are full snapshots expected, or are they fallback/resync traffic?
3. Are ACK baselines still present in snapshot history?
4. Is a hitch happening in decode, queueing, dispatch, snapshot apply, reconciliation, ACK send, or
   GC?

When the network telemetry overlay is open, press `Ctrl+C` to copy the current multiline telemetry
string to the system clipboard.

Values marked `n/a` have not been observed yet. Most counters are cumulative since telemetry reset
or process start. Fields named `1/5/30` are rolling windows over the last 1, 5, and 30 seconds.
Fields named `max10` are rolling maxima over the last 10 seconds.

## Reading The Overlay

Example shape:

```text
Network Telemetry
Client local: connected id=1 udp=ready mode=keepalive ackAge=822 ms snap=2630 rtt=15.9 ms debug=pong 15.9 ms
Client snapshots: full=20 delta=338 stale(f/d)=0/0 last=full@2630 e/r=111/0 2.17 ms
Client snapshot path: staleCheck=0 us fullApply=2.10 ms deltaMat=0 us reconcile=18 us ack=46 us stale=false staleFullBytes=0 B
Client timing: frame=16.66 ms net=2 us queue=17.00 ms dispatch=2.19 ms tcpDecode=SnapshotMessage 293 us max10(q/d/dec)=17.00 ms/3.66 ms/SnapshotMessage 332 us gc=1 ms
Client transport out: tcp=1563/5.1 KiB udp=347/8.5 KiB
Client transport in:  tcp=1241/1222.9 KiB udp=338/97.3 KiB
Client debug tx/rx: tcp=19/36 udp=0/0
Server authoritative: clients=1 udp=1/1 capture(c/s)=1830512251700/1830512220300
Server snapshots: full=19 last=t2270/34.7 KiB/e=111 | delta=335 last=t2590/299 B/d=1/r=0 build=138 us reason=MISSING_BASELINE_HISTORY periodic/fallback=0/19 missingBase=8 staleFullBytes=0 B rate1/5/30=69/196/354
Server history: tick=2590 size=128/128 cap=2.13 s
Server transport out: tcp=1239/1188.1 KiB udp=335/96.6 KiB bytes1/5/30=19.6 KiB/91.1 KiB/1284.8 KiB
Server transport in:  tcp=1559/5.1 KiB udp=346/8.5 KiB
Server debug tx/rx: tcp=34/18 udp=0/0
Server UDP: fallback=0 oversized=0 sendFail=0 dropped=0 last(f/drop/fail)=n/a/n/a/n/a
Server timing: frame=282 us max10=2.23 ms net=4 us max10=81 us queue=8.23 ms max10=17.62 ms tcpDecode=SnapshotAck 9 us max10=SnapshotAck 67 us gc=1 ms/5 ms
Server client 1: ack=2589 age=1t/17 ms hist=true cap=128t/2.13 s full1/5/30=0/1/19 bytes=0 B/34.7 KiB/660.1 KiB periodic/fallback=0/19 missingBase=8 lastFull=MISSING_BASELINE_HISTORY@2270/34.7 KiB age=5.1 s
```

## Client Local

`connected`
: Whether the client TCP session is connected. Expected: `connected` during play. `disconnected`
means all other client-side values may be stale.

`id`
: The client id assigned by the server. Expected: positive after connect. `0` means no assigned id.

`udp`
: Client-side UDP state. Expected: `ready` during stable play. `fallback` means unreliable traffic
is using TCP fallback or UDP is not established.

`mode`
: UDP maintenance state. `retry` means the client is trying to establish or restore UDP.
`keepalive` means UDP is healthy and periodic keepalive registration is running. Expected:
`keepalive` during stable play.

`ackAge`
: Age of the last UDP registration acknowledgement on the client. This is not the snapshot ACK age.
Expected local/LAN: usually below the keepalive interval plus jitter. Current config sends UDP
keepalive every `2.0 s` and treats UDP as stale after `4.5 s`. Values near or above `4.5 s` are
suspicious.

`snap`
: Latest server snapshot tick successfully applied by the client. Expected: increases steadily
during active play. If it stops while the game continues, snapshot delivery or application is stuck.

`rtt`
: Client-measured debug ping round-trip time. Expected local: usually `1-20 ms`; LAN often
`1-30 ms`; internet/tunnel depends on route and may be `40-150+ ms`. Sudden sustained jumps are more
important than the exact value.

`debug`
: Status of the debug telemetry stream or ping. Expected: `stream active`, `pong <time>`, or
`telemetry copied` after copying. Other possible statuses include `telemetry copy failed`, `stream
stopped`, `stop failed`, `ping failed`, and `stream start failed`. Repeated failures mean debug
telemetry itself is not healthy.

## Client Snapshots

`full`
: Cumulative number of full snapshots applied by the client. Expected: a few on connect, reconnect,
level change, resync, or periodic server baseline. During stable play this should grow at most at
the configured periodic baseline cadence unless recovery traffic is happening.

`delta`
: Cumulative number of delta snapshots applied by the client. Expected: grows steadily during play.
With the current `60 Hz` server delta rate, local healthy play can approach roughly 60 per second,
minus skipped/no-change frames.

`stale(f/d)`
: Stale full and delta snapshots dropped before application. Expected: `0/0` or very low. A rising
full count means reliable full snapshots are arriving too late. A rising delta count can happen when
newer snapshots already arrived or the local baseline is missing.

`last=<kind>@<tick>`
: The kind and server tick of the latest handled snapshot. Expected: tick increases over time.
`full` means the most recent applied snapshot was a full snapshot; `delta` means normal delta sync.

`e/r`
: Entity states and removals handled by the latest snapshot. For full snapshots, removals are
normally `0`. For deltas, small entity counts are expected in stable play. Large entity counts in
deltas mean many entities changed.

Trailing duration
: Total client handler time for the latest applied snapshot. Expected local: usually under a few
milliseconds. Values above the frame budget (`16.6 ms` at 60 FPS) are visible-risk territory.

## Client Snapshot Path

`staleCheck`
: Time spent checking whether the snapshot is obsolete before expensive work. For deltas this also
includes local baseline lookup. Expected: near `0 us`.

`fullApply`
: Time spent applying a full snapshot, or applying the changed snapshot produced from a delta through
the shared snapshot translator. Expected local: often `1-5 ms` for large full snapshots, much lower
for small deltas. Sustained values above `16.6 ms` can cause frame hitches.

`deltaMat`
: Time spent materializing a delta against a retained baseline. Expected: usually below `1 ms`.
High values point to expensive delta reconstruction.

`reconcile`
: Time spent reconciling tracked network entities after snapshot application, including removals.
Expected: microseconds to low milliseconds. High values point to entity tracking or removal costs.

`ack`
: Time spent sending the snapshot acknowledgement. Expected: microseconds. High values suggest the
send path is blocked or allocating heavily.

`stale`
: Whether the latest snapshot handler path dropped the snapshot as stale. For deltas, `true` can
also mean the local baseline was missing. Expected: usually `false`.

`staleFullBytes`
: Serialized bytes of stale full snapshots dropped by this client. Expected: `0 B`. If this grows,
TCP is delivering full snapshots that the client no longer needs.

## Client Timing

`frame`
: Latest client render frame time. Expected at 60 FPS: around `16.6 ms`. Lower is fine. Sustained
values above `16.6 ms` mean the client is missing the 60 FPS frame budget.

`net`
: Total time spent draining and dispatching network messages in the latest frame. Expected local:
microseconds to low milliseconds. Values above a few milliseconds should be investigated.

`queue`
: Age of the latest queued network message when it reached the game-thread dispatcher. Expected
local: usually under one frame. Occasional `~16-17 ms` means the message waited one render frame.
Sustained `50+ ms` is suspicious.

`dispatch`
: Handler duration for the latest queued message. Expected: microseconds to low milliseconds. High
values identify game-thread message handlers as the hitch source.

`tcpDecode=<type> <time>`
: Latest TCP protobuf decode message type and decode duration. Expected: usually microseconds. Large
full snapshots may take hundreds of microseconds. Multi-millisecond decode is suspicious.

`max10(q/d/dec)`
: Rolling 10 second maxima for queue age, dispatch duration, and TCP decode duration. The decode
entry includes the message type. Use this to catch spikes that are gone by the time you read the
latest values.

`gc`
: Latest observed JVM GC collection-time delta on this process. Expected: `n/a` or low milliseconds.
Large or repeated values around hitches suggest allocation pressure or GC pauses.

## Client Transport

`Client transport out: tcp=<messages>/<bytes> udp=<messages>/<bytes>`
: Client gameplay transport sent, excluding debug telemetry. Expected: UDP should carry frequent
input messages. TCP should mostly carry reliable control messages and ACKs.

`Client transport in: tcp=<messages>/<bytes> udp=<messages>/<bytes>`
: Client gameplay transport received, excluding debug telemetry. Expected: UDP should carry deltas.
TCP bytes grow when full snapshots or reliable events arrive. Repeated `~35 KiB` TCP bursts usually
mean full snapshots; periodic baseline bursts are normal at the configured cadence.

`Client debug tx/rx`
: Debug telemetry messages sent/received by TCP and UDP. Expected: TCP grows while the overlay is
open. UDP is normally `0/0` for debug telemetry.

## Server Authoritative

`server telemetry stale <age>`
: Optional prefix shown when the last server telemetry snapshot is older than `2 s`. Expected: not
shown while the stream is healthy.

`Server authoritative: n/a`
: No server telemetry snapshot has arrived yet. This is normal before the stream starts or before a
client has received the first server debug telemetry message.

`clients`
: Number of connected server sessions included in the server telemetry snapshot.

`udp=<ready>/<clients>`
: Number of clients for which the server considers UDP ready. Expected: all clients ready during
stable play, for example `1/1` or `2/2`.

`capture(c/s)`
: Client and server monotonic capture timestamps in nanoseconds. They are useful for comparing
changes within one side, not for subtracting client minus server across different JVMs or machines.

## Server Snapshots

`full`
: Cumulative full snapshots sent by the server. Expected: low but not necessarily zero after
startup. The current strategy sends periodic full baselines about every `6 s` per client. Additional
growth should be explainable by connect, reconnect, level change, or missing baseline recovery.

`last=t<tick>/<bytes>/e=<entities>`
: Tick, serialized size, and entity count of the last full snapshot. In current Last Hour sessions,
around `30-40 KiB` and roughly `100+` entities can be normal.

`delta`
: Cumulative delta snapshots sent by the server. Expected: grows steadily during active play.

`last=t<tick>/<bytes>/d=<deltas>/r=<removals>`
: Tick, serialized size, entity delta count, and removal count of the last delta snapshot. Expected:
small bytes and small `d`/`r` during stable play.

`build`
: Server time to build the authoritative snapshot before sending. Expected: microseconds to low
milliseconds. Sustained values above `16.6 ms` would exceed the 60 Hz tick budget.

`reason`
: Reason for the last full snapshot. Possible values:

- `NO_ACK`: client has not acknowledged a snapshot yet.
- `PERIODIC_BASELINE`: configured periodic baseline interval elapsed.
- `MISSING_BASELINE_HISTORY`: client ACK references a baseline no longer retained by server history.
- `LEVEL_CHANGE`: level changed and baselines were invalidated.
- `RECONNECT`: client reconnected and needs a fresh baseline.
- `CLIENT_RESYNC_REQUEST`: client explicitly requested resync.
- `SERVER_FORCED_RESYNC`: server forced a resync or no specific reason was attached.

`periodic/fallback`
: Cumulative full snapshots split into periodic baselines and non-periodic fallback/resync reasons.
Expected stable play under the current strategy shows periodic growth at the configured baseline
cadence. If fallback grows during stable local play, baseline/ACK health needs investigation.

`missingBase`
: Subset of fallback full snapshots caused by missing server baseline history. Expected after warmup:
ideally `0` and not increasing. Growth here is a strong signal that ACK/history capacity or ordering
needs attention.

`staleFullBytes`
: Server-side stale full bytes counter. This is usually `0` because stale full snapshot drops happen
on clients. In a separate server JVM this should usually remain `0`; use the client
`staleFullBytes` line for client-side stale full snapshot cost.

`rate1/5/30`
: Rolling count of all snapshots sent in the last 1, 5, and 30 seconds. At `60 Hz`, a stable one
client session can be near `60/300/1800` if every snapshot has changes. Lower is normal when few
changes are emitted.

## Server History

`tick`
: Server tick of the latest snapshot-history sample.

`size=<current>/<capacity>`
: Number of retained baseline snapshots and maximum retained snapshots. Expected after warmup:
usually full, for example `128/128`.

`cap`
: Approximate time coverage of the snapshot history. With `128` retained snapshots at `60 Hz`, this
is about `2.13 s`. ACK age should normally stay well below this. If ACK age exceeds this, missing
baseline fallback becomes likely.

## Server Transport

`Server transport out`
: Server gameplay transport sent, excluding debug telemetry. UDP should carry deltas. TCP bytes grow
with reliable events and full snapshots.

`bytes1/5/30`
: Rolling server outbound gameplay bytes over the last 1, 5, and 30 seconds. Expected stable local
play can show periodic full-baseline bytes at the configured cadence. Extra large bursts outside
that cadence should be explained by reliable events, reconnects, level changes, or resync.

`Server transport in`
: Server gameplay transport received, excluding debug telemetry. Expected: mostly UDP input and TCP
ACK/control messages.

`Server debug tx/rx`
: Debug telemetry messages sent/received by TCP and UDP. Expected: TCP grows while overlay streaming
is active.

## Server UDP

`fallback`
: Unreliable messages routed through TCP because UDP was unavailable or failed. Expected: `0` in a
healthy local/LAN run.

`oversized`
: UDP payloads rejected because they exceeded the safe MTU. Expected: `0`. Growth means an
unreliable message is too large for UDP and will likely fall back.

`sendFail`
: UDP write failures. Expected: `0`.

`dropped`
: Inbound UDP packets dropped before dispatch because of decode errors, invalid size, unknown
session, stale mapping, or missing registration. Expected: `0` or very low.

`last(f/drop/fail)`
: Last UDP fallback, inbound drop, and send-failure reason in that order. Expected:
`n/a/n/a/n/a` when no UDP problem has occurred.

## Server Timing

`frame`
: Latest authoritative server tick duration. Expected local: well below `16.6 ms` at `60 Hz`.

`max10`
: Rolling 10 second maximum for the preceding timing category.

`net`
: Latest server network poll/dispatch batch duration. Expected: microseconds to low milliseconds.

`queue`
: Age of the latest server inbound message when it reached the game-thread dispatcher. Expected
local: usually under one tick/frame. Sustained high values mean the server is not draining input
fast enough.

`tcpDecode=<type> <time>`
: Latest TCP decode type and duration on the server. Expected: tiny for `SnapshotAck`, larger for
big reliable messages.

`gc=<last>/<max10>`
: Latest and rolling 10 second maximum GC collection-time delta. Expected: `n/a` or low
milliseconds. Large values near hitches indicate GC involvement.

## Server Client Lines

Each connected client gets one line.

`ack`
: Latest snapshot tick acknowledged by this client. Expected: tracks close behind server history
tick during stable play.

`age=<ticks>t/<time>`
: Age of the latest ACK relative to the current server tick. Expected local: usually a few ticks or
less. It should stay far below the history capacity.

`hist`
: Whether the acknowledged baseline exists in server `SnapshotHistory`. Expected: `true` during
stable play. `false` means the server cannot build a delta from that ACK and may send a full
snapshot.

`cap`
: Per-client view of snapshot history capacity in ticks and seconds. This should match the server
history capacity.

`full1/5/30`
: Full snapshots sent to this client in the last 1, 5, and 30 seconds. With the current periodic
full-baseline strategy, healthy stable play can show roughly one full snapshot every `6 s` per
client, so the 30 second window can be around `5`. Values materially above the configured periodic
cadence are suspicious.

`bytes`
: Full snapshot bytes sent to this client in the last 1, 5, and 30 seconds. In stable play,
periodic full bytes can appear at the `6 s` baseline cadence. Extra fallback full bytes are
suspicious.

`periodic/fallback`
: Cumulative full snapshots to this client split by periodic baselines and fallback/resync reasons.
Periodic growth at the configured cadence is normal. Fallback growth in stable play is suspicious.

`missingBase`
: Full snapshots to this client caused by missing baseline history. Expected after warmup: ideally
`0` and not increasing.

`lastFull=<reason>@<tick>/<bytes>`
: Reason, tick, and size of the last full snapshot sent to this client.

`age`
: Time since the last full snapshot sent to this client. If this stays low and full window counters
keep growing, the client is in a full snapshot storm.

## Healthy Local Test Checklist

For client and host on the same machine, a healthy stable period usually looks like this:

- Client and server UDP both `ready`, with `fallback=0`, `oversized=0`, `sendFail=0`, and
  `dropped=0`.
- Client `stale(f/d)` stays `0/0` or nearly zero.
- Server client `hist=true`.
- Server client ACK `age` stays far below `cap`.
- `missingBase` does not increase after warmup.
- `full1/5/30` matches the configured periodic cadence; fallback and `missingBase` stay flat after
  warmup.
- Full snapshot apply time stays below a few milliseconds locally.
- Client `frame` stays around `16.6 ms` for 60 FPS.
- Client/server `queue`, `dispatch`, `tcpDecode`, and `gc` maxima do not align with visible hitches.

## Red Flags

- `udp=fallback` or server UDP readiness below client count.
- Any UDP `fallback`, `oversized`, `sendFail`, or repeated `dropped` growth.
- Server client `hist=false`.
- Server client ACK age approaching or exceeding history `cap`.
- `reason=MISSING_BASELINE_HISTORY` recurring during stable play.
- `periodic/fallback` fallback side growing without reconnect or level change.
- `full1/5/30` materially above the configured periodic cadence.
- `staleFullBytes` increasing on the client.
- Client `queue` or `dispatch` repeatedly above one frame.
- Snapshot apply, materialization, reconciliation, or ACK timings above the frame budget.
- GC values repeatedly spiking near visible stutters.
