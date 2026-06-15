# Network Telemetry Overlay

The network telemetry overlay is a debug view for multiplayer transport, snapshot synchronization,
and frame/dispatch timing. It is intended to answer five questions:

1. Is UDP healthy?
2. Are full snapshots expected baseline/recovery events, or unexplained reliable traffic?
3. Are ACK baselines still present in snapshot history?
4. Is a hitch happening in decode, queueing, dispatch, snapshot apply, reconciliation, ACK send, or
   GC?
5. Is queue age caused by normal frame/tick scheduling, or by real inbound backlog?

Hold `F3` and press `N` to toggle the network telemetry overlay. Pressing `F3` while `N` is already
held also toggles it. When the overlay is open, press `Ctrl+C` to copy the current multiline
telemetry report to the system clipboard.

Server-side telemetry snapshots and debug ping responses are disabled by default. Set
`NetworkConfig.DEBUG_TELEMETRY_ENABLED = true` in a development launcher or test before requesting
server telemetry.

Values marked `n/a` have not been observed yet. Most counters are cumulative since telemetry reset
or process start. Fields named `1/5/30` are rolling windows over the last 1, 5, and 30 seconds.
Fields named `max10` are rolling maxima over the last 10 seconds.

The default overlay intentionally favors root-cause signal over raw counter dumps. Debug-protocol
transport counters and raw cross-process capture timestamps may still be collected internally, but
they are not part of the default rendered report because they rarely change a debugging decision.

## Reading The Overlay

Example shape:

```text
Client
  State: connected id=1 snap=4374
  UDP: ready mode=keepalive ackAge=1.5s
  RTT: 31.3 ms
  Snapshots: applied full=1 delta=2140 stale=0/0 early=0/0 handler=0/0 last=delta@4374 e/r=1/0
  Recovery: missingLocalBase=0 resyncReq=0 lastMissing=n/a->n/a staleFullBytes=0 B
  Apply timings: last=112 us staleCheck=0 us fullApply=60 us deltaMat=26 us reconcile=25 us ackQueue=0 us stale=false
  Runtime timings: frame=16.68 ms net=1 us queue=3.75 ms dispatch=5 us tcpDecode=DebugTelemetrySnapshot 65 us qDepth=0 drain=0 gc=1 ms
  Runtime max10: queue=DeltaSnapshotMessage 17.85 ms dispatch=DeltaSnapshotMessage 1.05 ms tcpDecode=SoundPlayMessage 1.58 ms qDepth=5
  Transport: out tcp=285/1.5 KiB udp=3060/90.2 KiB in tcp=62/92.1 KiB udp=2140/608.5 KiB
Server
  Authoritative: age=600 ms clients=1 udp=1/1
  Snapshots: full=1 lastFull=t306 bytes=34.7 KiB entities=111 delta=2140 lastDelta=t4374 bytes=305 B deltas=1 removals=0 build=187 us reason=INITIAL_SYNC rate1/5/30=38/219/1089
  Recovery: periodic/recovery=0/1 missingBase=0 staleFullBytes=0 B
  History: tick=4410 retained=601/600 cap=10.00s
  Transport: out tcp=62/92.1 KiB udp=2140/608.5 KiB in tcp=285/1.5 KiB udp=3060/90.2 KiB bytes1/5/30=11.1 KiB/64.6 KiB/306.0 KiB
  UDP: fallback=0 oversized=0 sendFail=0 dropped=0 last=n/a/n/a/n/a
  Timings: frame=476 us net=2 us queue=22.47 ms qDepth=0 drain=0 tcpDecode=DebugPing 26 us gc=3 ms
  Timings max10: frame=4.88 ms net=4.57 ms queue=InputMessage 30.76 ms qDepth=4 tcpDecode=SnapshotAck 125 us gc=4 ms
  Client 1: udp=true rtt=n/a activity=15 ms ack=4374 age=36t/600 ms hist=true cap=600t/10.00s full1/5/30=0/0/0 bytes=0 B/0 B/0 B missingBase=0 lastFull=INITIAL_SYNC@306/34.7 KiB age=65.7s
```

## Health Highlighting

Bad values are rendered in red in the overlay. The copied text keeps the same grouped structure and
adds the expected range only beside bad values. Normal values stay white and do not carry range
suffixes.

These thresholds are diagnostic only. They do not change gameplay, routing, reliability, protobuf
payloads, or protocol versions.

| Metric | Red when | Source or rationale |
| --- | --- | --- |
| Server telemetry snapshot age | `> 2000 ms` | Overlay staleness policy |
| Client UDP ACK age | `> NetworkConfig.UDP_STALE_AFTER_MS` | Runtime UDP recovery config |
| Client render frame | `> 1.5 * (1_000_000 / NetworkConfig.SERVER_TICK_HZ)` | Hitch signal; avoids marking normal vsync jitter red |
| Server authoritative tick | `> 1_000_000 / NetworkConfig.SERVER_TICK_HZ` | Runtime tick budget |
| Queue age | `> max(50 ms, 3 ticks)` | Backlog signal; one frame/tick of queue age is normal when depth stays low |
| Full snapshot apply and total full snapshot handler time | `> tick budget` | Full baseline application is larger than a small per-message slice but should not exceed one frame/tick budget |
| Dispatch, decode, network batch, snapshot build, stale check, delta materialization, reconcile, and ACK queue slices | `> tick budget / 4` | Strict work-slice diagnostic budget |
| GC pause | `> tick budget` converted to milliseconds | Runtime tick budget |
| Delta snapshot bytes | `> NetworkConfig.SAFE_UDP_MTU` | Runtime UDP fragmentation avoidance |
| Full snapshot bytes | `> 256 KiB` | Soft overlay diagnostic limit |
| Debug RTT | `> 100 ms` | Overlay responsiveness target |
| Server per-client ACK age | informational while `hist=true` | Baseline availability is the health signal; inactive clients can legitimately stop ACKing until new snapshots arrive |
| Queue depth | `> NetworkConfig.SERVER_TICK_HZ` | Runtime tick-rate-sized backlog budget |
| Per-client full snapshots per second | `> 1` | Full-snapshot churn diagnostic |
| UDP fallback, oversized, send failure, dropped | `> 0` | Expected-zero health counter |
| Stale snapshots and stale full snapshot bytes | `> 0` | Expected-zero health counter |
| Missing local delta baselines and resync requests | `> 0` | Expected-zero health counter |
| Missing-baseline full snapshot fallback | `> 0` | Expected-zero health counter |
| Connected local client UDP state | not `ready` | Runtime health state |
| Server client UDP state | `false` | Runtime health state |
| ACK baseline retained in server history | `false` once an ACK exists | Delta baseline invariant |
| Last full snapshot reason | `MISSING_BASELINE_HISTORY` or `CLIENT_MISSING_BASELINE` | Baseline stability diagnostic |
| Client snapshot handler stale flag | `true` | Snapshot ordering diagnostic |

The network report also describes targets for p95 Input-to-Snapshot latency, jitter, and packet
loss. Those values are not highlighted by this overlay yet because the current telemetry surface
does not measure them directly. Add direct counters or histograms before assigning health colors to
those targets.

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
keepalive every `2.0s` and treats UDP as stale after `4.5s`. Values near or above `4.5s` are
suspicious.

`snap`
: Latest server snapshot tick successfully applied by the client. Expected: increases steadily
during active play. If it stops while the game continues, snapshot delivery or application is stuck.

`rtt`
: Client-measured debug ping round-trip time. Expected local: usually `1-20 ms`; LAN often
`1-30 ms`; internet/tunnel depends on route and may be `40-150+ ms`. Sudden sustained jumps are more
important than the exact value.

Temporary notices
: Copy success/failure and debug stream/ping failures are shown as a short-lived line at the top of
the overlay. They are UI feedback only and are not included in the copied telemetry report. Healthy
stream and ping state is intentionally silent; the `RTT` line is the useful ping signal.

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
happen when newer snapshots already arrived. Missing local delta baselines are tracked separately on
the client snapshot path line and can send a reliable client resync request so the server can
respond with a client-specific recovery full snapshot.

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
for small deltas. Sustained values above the frame/tick budget can cause hitches. The overlay uses
the frame/tick budget here, not the stricter substep budget used for tiny handler slices.

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
: Whether the latest snapshot handler path dropped the snapshot before application. Missing local
delta baselines are not counted in `stale(f/d)`, but this flag can still be `true` for a handler
drop. Expected: usually `false`.

`staleFullBytes`
: Serialized bytes of stale full snapshots dropped by this client. Expected: `0 B`. If this grows,
TCP is delivering full snapshots that the client no longer needs.

`missingLocalBase`
: Deltas that referenced a local baseline the client no longer retained. Expected: `0` during stable
play after warmup.

`resyncReq`
: Reliable client resync requests admitted by the client-side rate limiter. Expected: flat during
stable play; growth should line up with `missingLocalBase` and a bounded
`CLIENT_MISSING_BASELINE` recovery full snapshot.

`lastMissing=<base>-><delta>`
: Latest missing local delta baseline and the delta tick that exposed it.

## Client Timing

`frame`
: Latest client render frame time. Expected at 60 FPS: around `16.6 ms`. Lower is fine. Sustained
values above `16.6 ms` mean the client is missing the 60 FPS frame budget, but the red highlight is
reserved for larger hitch-sized samples.

`net`
: Total time spent draining and dispatching network messages in the latest frame. Expected local:
microseconds to low milliseconds. Values above a few milliseconds should be investigated.

`queue`
: Age of the latest queued network message when it reached the game-thread dispatcher. Expected
local: usually under one frame. Occasional `~16-17 ms` means the message waited one render frame.
Sustained values above the backlog threshold, especially with growing `qDepth`, are suspicious.

`dispatch`
: Handler duration for the latest queued message. Expected: microseconds to low milliseconds. High
values identify game-thread message handlers as the hitch source.

`tcpDecode=<type> <time>`
: Latest TCP protobuf decode message type and decode duration. Expected: usually microseconds. Large
full snapshots may take hundreds of microseconds. Multi-millisecond decode is suspicious.

`Runtime max10`
: Rolling 10 second maxima for queue age, dispatch duration, TCP decode duration, and queue depth.
Each timing includes the message type that produced the maximum. This replaces the old packed
`max10(q/d/dec)` suffix so clipped text cannot look like a standalone unit.

`qDepth=<current>`
: Inbound messages waiting when the latest network poll started. The rolling 10 second maximum
appears separately as `qDepth=<max10>` on `Runtime max10`. Expected: usually `0` or very low. Low
depth with queue age near one frame usually means normal frame/tick scheduling. Growing depth means
the game/server thread is not draining messages fast enough.

`drain`
: Inbound messages removed from the queue during the latest network poll. Expected: often `0` or a
small number. High drain counts together with growing `qDepth` indicate backlog.

`gc`
: Latest observed JVM GC collection-time delta on this process. Expected: `n/a` or low milliseconds.
Large or repeated values around hitches suggest allocation pressure or GC pauses.

## Client Transport

Client `Transport` `out tcp=<messages>/<bytes> udp=<messages>/<bytes>`
: Client gameplay transport sent, excluding debug telemetry. Expected: UDP should carry frequent
input messages. TCP should mostly carry reliable control messages and ACKs.

Client `Transport` `in tcp=<messages>/<bytes> udp=<messages>/<bytes>`
: Client gameplay transport received, excluding debug telemetry. Expected: UDP should carry deltas.
TCP bytes grow when full snapshots or reliable events arrive. Repeated `~35 KiB` TCP bursts usually
mean full snapshots. In stable play those bursts should be rare and explained by `lastFull`/`reason`;
recurring full-sized TCP bursts without initial sync, level change, reconnect, resync, or recovery
are suspicious.

## Server Authoritative

`Authoritative: age=<value>`
: Server telemetry snapshot age. Expected: below `2s` while the stream is healthy. The `age` value
is highlighted red and receives an expected-range suffix in copied telemetry when it exceeds `2s`.

Server `Authoritative: n/a`
: No server telemetry snapshot has arrived yet. This is normal before the stream starts or before a
client has received the first server debug telemetry message.

`clients`
: Number of connected server sessions included in the server telemetry snapshot.

`udp=<ready>/<clients>`
: Number of clients for which the server considers UDP ready. Expected: all clients ready during
stable play, for example `1/1` or `2/2`.

## Server Snapshots

`full`
: Cumulative full snapshots sent by the server. Expected: low but not necessarily zero after
startup. The current strategy is demand-driven: full snapshots are sent for initial sync, level
change, reconnect, client missing-baseline recovery, server missing-baseline-history recovery, hard
server resync, or an explicitly enabled bounded safety fallback. Stable play should not show
clock-driven full snapshot growth.

`lastFull=t<tick> bytes=<bytes> entities=<entities>`
: Tick, serialized size, and entity count of the last full snapshot. In current Last Hour sessions,
around `30-40 KiB` and roughly `100+` entities can be normal.

`delta`
: Cumulative delta snapshots sent by the server. Expected: grows steadily during active play.

`lastDelta=t<tick> bytes=<bytes> deltas=<deltas> removals=<removals>`
: Tick, serialized size, entity delta count, and removal count of the last delta snapshot. Expected:
small bytes and small `d`/`r` during stable play.

`build`
: Server time to build the authoritative snapshot before sending. Expected: microseconds to low
milliseconds. Red starts above the work-slice budget, which is `tick budget / 4`; at `60 Hz` that is
about `4.17 ms`. Values above the full `16.67 ms` tick budget are severe.

`reason`
: Reason for the last full snapshot. The current server path normally emits:

- `INITIAL_SYNC`: first reliable full snapshot baseline after initial world load.
- `CLIENT_MISSING_BASELINE`: client reported that it cannot materialize a delta because its local
  `baseTick` is missing.
- `MISSING_BASELINE_HISTORY`: client ACK references a baseline no longer retained by server history.
- `LEVEL_CHANGE`: level changed and baselines were invalidated.
- `RECONNECT`: client reconnected and needs a fresh baseline.
- `SERVER_FORCED_RESYNC`: server forced a resync or no specific reason was attached.

The enum also contains `NO_ACK`, `PERIODIC_BASELINE`, and `CLIENT_RESYNC_REQUEST` for fallback or
explicit resync paths. Those values are not normally produced by the current stable sync path.

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

`retained=<current>/<capacity>`
: Number of retained baseline snapshots and maximum unprotected rolling snapshots. Protected
acknowledged or in-flight client baselines may be retained outside the rolling capacity, so values
such as `601/600` can be healthy.

`cap`
: Approximate time coverage of the snapshot history. With the current `600` retained snapshots at
`60 Hz`, this is about `10.00s`. The server protects acknowledged and in-flight baseline ticks
outside the ordinary rolling capacity, so age alone is not a health failure while `hist=true`.

## Server Transport

Server `Transport` `out`
: Server gameplay transport sent, excluding debug telemetry. UDP should carry deltas. TCP bytes grow
with reliable events and full snapshots.

`bytes1/5/30`
: Rolling server outbound gameplay bytes over the last 1, 5, and 30 seconds. Expected stable local
play should mostly reflect small delta traffic plus reliable control/debug-independent gameplay
events. Large full-snapshot-sized bursts should be explained by initial sync, reconnect, level
change, missing-baseline recovery, hard resync, or an explicitly enabled safety fallback.

Server `Transport` `in`
: Server gameplay transport received, excluding debug telemetry. Expected: mostly UDP input and TCP
ACK/control messages.

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
local: usually under one tick/frame. Queue age only becomes a red backlog signal above the
configured backlog threshold or when it lines up with growing queue depth.

`Timings max10`
: Rolling 10 second maxima for server frame, network batch, queue age, queue depth, TCP decode, and
GC. The queue and TCP decode entries include the message type that produced the maximum. A queue max
on `InputMessage` is more relevant to gameplay latency than a max on debug telemetry.

`qDepth=<current>`
: Server inbound messages waiting when the latest network poll started. The rolling 10 second
maximum appears separately as `qDepth=<max10>` on `Timings max10`. Low depth with queue age near one
tick is usually normal scheduling; growing depth means real backlog.

`drain`
: Server inbound messages removed from the queue during the latest network poll. High values are
only concerning when `qDepth` also grows or frame/tick time rises.

`tcpDecode=<type> <time>`
: Latest TCP decode type and duration on the server. Expected: tiny for `SnapshotAck`, larger for
big reliable messages.

`gc=<last>`
: Latest GC collection-time delta on `Timings`. The rolling 10 second maximum appears separately as
`gc=<max10>` on `Timings max10`. Expected: `n/a` or low milliseconds. Large values near hitches
indicate GC involvement.

## Server Client Lines

Each connected client gets one line.

`rtt`
: Latest client-measured debug ping round-trip time reported back to the server on the next
`DebugPing`. The first ping can show `n/a` until the client has received at least one `DebugPong`;
after that it should broadly match the local `RTT` line.

`ack`
: Latest snapshot tick acknowledged by this client. Expected: tracks close behind server history
tick during stable play.

`age=<ticks>t/<time>`
: Age of the latest ACK relative to the current server tick. During client inactivity this can count
above one second because no new snapshots need acknowledgement. The value is informational; the
health signal is whether `hist` is still `true` and whether missing-baseline recovery counters or
full-snapshot churn start growing.

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

`missingBase`
: Full snapshots to this client caused by missing baseline history. Expected after warmup: ideally
`0` and not increasing.

`lastFull=<reason>@<tick>/<bytes>`
: Reason, tick, and size of the last full snapshot sent to this client.

`age`
: Time since the last full snapshot sent to this client. If this stays low and full window counters
keep growing, the client is in a repeated full-snapshot loop.

## Healthy Local Test Checklist

For client and host on the same machine, a healthy stable period usually looks like this:

- Client and server UDP both `ready`, with `fallback=0`, `oversized=0`, `sendFail=0`, and
  `dropped=0`.
- Client `stale(f/d)` stays `0/0` or nearly zero.
- Server client `hist=true`; ACK `age` can count above one second during inactivity without being a
  problem by itself.
- `missingBase` does not increase after warmup.
- Client `missingLocalBase` and `resyncReq` stay flat at `0` after warmup.
- `full1/5/30` becomes `0/0/0` after the initial full snapshot ages out unless reconnect, level
  change, missing-baseline recovery, hard resync, or an explicitly enabled safety fallback occurs.
- `periodic/recovery` shows periodic `0`; recovery may include initial sync, but should stay flat
  during stable play.
- Client `early`, `handler`, and `stale(f/d)` stay `0/0` or nearly zero.
- Full snapshot apply time usually stays low locally and should not exceed the frame/tick budget.
- Client `frame` stays around `16.6 ms` for 60 FPS and does not show hitch-sized spikes.
- Client/server `queue`, `dispatch`, `tcpDecode`, and `gc` maxima do not align with visible hitches
  or growing backlog.
- Client/server `qDepth` stays low. Queue age near one frame/tick is usually fine when depth stays
  low.

## Red Flags

- `udp=fallback` or server UDP readiness below client count.
- Any UDP `fallback`, `oversized`, `sendFail`, or repeated `dropped` growth.
- Server client `hist=false`.
- `reason=MISSING_BASELINE_HISTORY` recurring during stable play.
- `reason=CLIENT_MISSING_BASELINE` recurring during stable play.
- Client `missingLocalBase` or `resyncReq` recurring during stable play.
- `periodic/recovery` periodic side growing when no safety fallback was intentionally enabled.
- `periodic/recovery` recovery side growing without initial sync, reconnect, level change,
  missing-baseline recovery, or hard resync.
- `full1/5/30` staying non-zero or repeatedly returning to non-zero during otherwise stable play.
- `staleFullBytes` increasing on the client.
- Client/server `queue` repeatedly above the backlog threshold, especially with growing `qDepth` or
  a gameplay queue max type such as `InputMessage` or `SnapshotMessage`.
- Client `dispatch` repeatedly above the work-slice budget.
- Client/server `qDepth` or `drain` repeatedly high or growing, which means real inbound backlog
  rather than ordinary frame/tick scheduling.
- Client `last` or `fullApply` snapshot timings above the frame/tick budget.
- `build`, `deltaMat`, `reconcile`, or `ackQueue` timings above the work-slice budget.
- GC values repeatedly spiking near visible stutters.
