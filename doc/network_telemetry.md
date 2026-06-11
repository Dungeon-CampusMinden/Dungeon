# Network Telemetry Overlay

The network telemetry overlay is a debug view for multiplayer transport, snapshot synchronization,
and frame/dispatch timing. It is intended to answer five questions:

1. Is UDP healthy?
2. Are full snapshots expected baseline/recovery events, or unexplained reliable traffic?
3. Are ACK baselines still present in snapshot history?
4. Is a hitch happening in decode, queueing, dispatch, snapshot apply, reconciliation, ACK send, or
   GC?
5. Is queue age caused by normal frame/tick scheduling, or by real inbound backlog?

When the network telemetry overlay is open, press `Ctrl+C` to copy the current multiline telemetry
string to the system clipboard.

Values marked `n/a` have not been observed yet. Most counters are cumulative since telemetry reset
or process start. Fields named `1/5/30` are rolling windows over the last 1, 5, and 30 seconds.
Fields named `max10` are rolling maxima over the last 10 seconds.

## Reading The Overlay

Example shape:

```text
Network Telemetry
Client local: connected id=1 udp=ready mode=keepalive ackAge=1.5 s snap=4374 rtt=31.3 ms debug=pong 31.3 ms
Client snapshots: full=1 delta=2140 stale(f/d)=0/0 early=0/0 handler=0/0 last=delta@4374 e/r=1/0 112 us
Client snapshot path: staleCheck=0 us fullApply=60 us deltaMat=26 us reconcile=25 us ackQueue=0 us stale=false staleDrop=n/a:n/a@n/a staleFullBytes=0 B
Client timing: frame=16.68 ms net=1 us queue=3.75 ms dispatch=5 us tcpDecode=DebugTelemetrySnapshot 65 us max10(q/d/dec)=17.85 ms DeltaSnapshotMessage/1.05 ms DeltaSnapshotMessage/SoundPlayMessage 1.58 ms qDepth=0/5 drain=0 gc=1 ms
Client transport out: tcp=285/1.5 KiB udp=3060/90.2 KiB
Client transport in:  tcp=62/92.1 KiB udp=2140/608.5 KiB
Client debug tx/rx: tcp=63/124 udp=0/0
Server authoritative: clients=1 udp=1/1 capture(c/s)=16885016949900/16885012724600
Server snapshots: full=1 last=t306/34.7 KiB/e=111 | delta=2140 last=t4374/305 B/d=1/r=0 build=187 us reason=INITIAL_SYNC periodic/recovery=0/1 missingBase=0 staleFullBytes=0 B rate1/5/30=38/219/1089
Server history: tick=4410 size=601/600 cap=10.00 s
Server transport out: tcp=62/92.1 KiB udp=2140/608.5 KiB bytes1/5/30=11.1 KiB/64.6 KiB/306.0 KiB
Server transport in:  tcp=285/1.5 KiB udp=3060/90.2 KiB
Server debug tx/rx: tcp=123/63 udp=0/0
Server UDP: fallback=0 oversized=0 sendFail=0 dropped=0 last(f/drop/fail)=n/a/n/a/n/a
Server timing: frame=476 us max10=4.88 ms net=2 us max10=4.57 ms queue=22.47 ms max10=InputMessage 30.76 ms qDepth=0/4 drain=0 tcpDecode=DebugPing 26 us max10=SnapshotAck 125 us gc=3 ms/4 ms
Server client 1: ack=4374 age=36t/600 ms hist=true cap=600t/10.00 s full1/5/30=0/0/0 bytes=0 B/0 B/0 B periodic/recovery=0/1 missingBase=0 lastFull=INITIAL_SYNC@306/34.7 KiB age=65.7 s
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
level change, client missing-baseline recovery, server missing-baseline-history recovery, or hard
server resync. During stable play this should stay flat after initial sync unless a concrete
recovery reason is recorded.

`delta`
: Cumulative number of delta snapshots applied by the client. Expected: grows steadily during play.
With the current `60 Hz` server delta rate, local healthy play can approach roughly 60 per second,
minus skipped/no-change frames.

`stale(f/d)`
: Total stale full and delta snapshots dropped before application. Expected: `0/0` or very low. A
rising full count means reliable full snapshots are arriving too late. A rising delta count can
happen when newer snapshots already arrived or the local baseline is missing. A missing local delta
baseline now also sends a reliable client resync request so the server can respond with a
client-specific recovery full snapshot.

`early`
: Stale full and delta snapshots dropped before the snapshot handler ran. This includes queue-time
or pre-dispatch stale drops. Expected: `0/0` in healthy stable play. If this rises while handler
stale stays flat, early rejection is protecting the expensive apply/reconciliation path.

`handler`
: Stale full and delta snapshots dropped by the snapshot handlers themselves. These checks are
correctness backstops after early rejection. Expected: `0/0` in healthy stable play.

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

`ackQueue`
: Time spent queueing or sending the snapshot acknowledgement. Full snapshots use an immediate
reliable ACK because they refresh the delta baseline; deltas usually use coalesced or piggybacked
ACKs. Expected: microseconds. High values suggest the send path is blocked or allocating heavily.

`stale`
: Whether the latest snapshot handler path dropped the snapshot as stale. For deltas, `true` can
also mean the local baseline was missing. Missing local baselines should be rare; when they happen,
the client requests a `CLIENT_MISSING_BASELINE` recovery full snapshot. Expected: usually `false`.

`staleDrop=<stage>:<kind>@<tick>`
: The latest stale-drop location, snapshot kind, and server tick. `stage` is usually `early`,
`handler`, or `n/a`; `kind` is `full`, `delta`, or `n/a`. Use this with `early` and `handler` to see
whether obsolete snapshots are being rejected before expensive work.

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
: Rolling 10 second maxima for queue age, dispatch duration, and TCP decode duration. Queue and
dispatch entries include the message type after the duration; the decode entry includes the message
type before the duration. Use this to catch spikes that are gone by the time you read the latest
values.

`qDepth=<current>/<max10>`
: Inbound messages waiting when the latest network poll started, plus the rolling 10 second maximum
depth. Expected: usually `0` or very low. Low depth with queue age near one frame usually means
normal frame/tick scheduling. Growing depth means the game/server thread is not draining messages
fast enough.

`drain`
: Inbound messages removed from the queue during the latest network poll. Expected: often `0` or a
small number. High drain counts together with growing `qDepth` indicate backlog.

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
mean full snapshots. In stable play those bursts should be rare and explained by `lastFull`/`reason`;
recurring full-sized TCP bursts without initial sync, level change, reconnect, resync, or recovery
are suspicious.

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
startup. The current strategy is demand-driven: full snapshots are sent for initial sync, level
change, reconnect, client missing-baseline recovery, server missing-baseline-history recovery, hard
server resync, or an explicitly enabled bounded safety fallback. Stable play should not show
clock-driven full snapshot growth.

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
- `INITIAL_SYNC`: first reliable full snapshot baseline after initial world load.
- `PERIODIC_BASELINE`: optional bounded periodic safety fallback, if that fallback is explicitly
  enabled.
- `CLIENT_MISSING_BASELINE`: client reported that it cannot materialize a delta because its local
  `baseTick` is missing.
- `MISSING_BASELINE_HISTORY`: client ACK references a baseline no longer retained by server history.
- `LEVEL_CHANGE`: level changed and baselines were invalidated.
- `RECONNECT`: client reconnected and needs a fresh baseline.
- `CLIENT_RESYNC_REQUEST`: client explicitly requested a general resync.
- `SERVER_FORCED_RESYNC`: server forced a resync or no specific reason was attached.

`periodic/recovery`
: Cumulative full snapshots split into optional periodic safety fallbacks and demand-driven recovery
or baseline reasons. Expected stable play after initial sync: periodic stays `0`, and recovery stays
flat unless a concrete recovery event occurs.

`missingBase`
: Subset of recovery full snapshots caused by missing server baseline history. Expected after
warmup: ideally `0` and not increasing. Growth here is a strong signal that ACK/history capacity or
ordering needs attention. Client-local missing baselines are reported through the
`CLIENT_MISSING_BASELINE` full-snapshot reason and recovery count; they are not included in
`missingBase`.

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
: Number of retained baseline snapshots and maximum unprotected rolling snapshots. Protected
acknowledged or in-flight client baselines may be retained outside the rolling capacity, so values
such as `601/600` can be healthy.

`cap`
: Approximate time coverage of the snapshot history. With the current `600` retained snapshots at
`60 Hz`, this is about `10.00 s`. ACK age should normally stay well below this. If ACK age exceeds
this, missing baseline fallback becomes likely.

## Server Transport

`Server transport out`
: Server gameplay transport sent, excluding debug telemetry. UDP should carry deltas. TCP bytes grow
with reliable events and full snapshots.

`bytes1/5/30`
: Rolling server outbound gameplay bytes over the last 1, 5, and 30 seconds. Expected stable local
play should mostly reflect small delta traffic plus reliable control/debug-independent gameplay
events. Large full-snapshot-sized bursts should be explained by initial sync, reconnect, level
change, missing-baseline recovery, hard resync, or an explicitly enabled safety fallback.

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

`max10=<type> <time>`
: Rolling 10 second maximum server inbound queue age and the message type that produced it. A max on
`InputMessage` is more relevant to gameplay latency than a max on debug telemetry.

`qDepth=<current>/<max10>`
: Server inbound messages waiting when the latest network poll started, plus the rolling 10 second
maximum depth. Low depth with queue age near one tick is usually normal scheduling; growing depth
means real backlog.

`drain`
: Server inbound messages removed from the queue during the latest network poll. High values are
only concerning when `qDepth` also grows or frame/tick time rises.

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
stable play. `false` means the server cannot build a delta from that ACK and may send a rate-limited
`MISSING_BASELINE_HISTORY` recovery full snapshot.

`cap`
: Per-client view of snapshot history capacity in ticks and seconds. This should match the server
history capacity.

`full1/5/30`
: Full snapshots sent to this client in the last 1, 5, and 30 seconds. Healthy stable play should
show `0/0/0` after the initial full snapshot ages out of the rolling windows. A non-zero value is
expected shortly after initial sync, reconnect, level change, missing-baseline recovery, hard resync,
or an explicitly enabled safety fallback.

`bytes`
: Full snapshot bytes sent to this client in the last 1, 5, and 30 seconds. Healthy stable play
should show `0 B/0 B/0 B` after the initial full snapshot ages out. Full-snapshot bytes during
stable play should line up with a concrete `lastFull` reason.

`periodic/recovery`
: Cumulative full snapshots to this client split by optional periodic safety fallbacks and
demand-driven recovery or baseline reasons. Expected stable play after initial sync: periodic stays
`0`, and recovery stays flat.

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
- `full1/5/30` becomes `0/0/0` after the initial full snapshot ages out unless reconnect, level
  change, missing-baseline recovery, hard resync, or an explicitly enabled safety fallback occurs.
- `periodic/recovery` shows periodic `0`; recovery may include initial sync, but should stay flat
  during stable play.
- Client `early`, `handler`, and `stale(f/d)` stay `0/0` or nearly zero.
- Full snapshot apply time stays below a few milliseconds locally.
- Client `frame` stays around `16.6 ms` for 60 FPS.
- Client/server `queue`, `dispatch`, `tcpDecode`, and `gc` maxima do not align with visible hitches.
- Client/server `qDepth` stays low. Queue age near one frame/tick is usually fine when depth stays
  low.

## Red Flags

- `udp=fallback` or server UDP readiness below client count.
- Any UDP `fallback`, `oversized`, `sendFail`, or repeated `dropped` growth.
- Server client `hist=false`.
- Server client ACK age approaching or exceeding history `cap`.
- `reason=MISSING_BASELINE_HISTORY` recurring during stable play.
- `reason=CLIENT_MISSING_BASELINE` recurring during stable play.
- `periodic/recovery` periodic side growing when no safety fallback was intentionally enabled.
- `periodic/recovery` recovery side growing without initial sync, reconnect, level change,
  missing-baseline recovery, or hard resync.
- `full1/5/30` staying non-zero or repeatedly returning to non-zero during otherwise stable play.
- `staleFullBytes` increasing on the client.
- Client `queue` or `dispatch` repeatedly above one frame, especially when the queue max type is a
  gameplay message such as `InputMessage` or `SnapshotMessage`.
- Client/server `qDepth` or `drain` repeatedly high or growing, which means real inbound backlog
  rather than ordinary frame/tick scheduling.
- Snapshot apply, materialization, reconciliation, or ACK timings above the frame budget.
- GC values repeatedly spiking near visible stutters.
