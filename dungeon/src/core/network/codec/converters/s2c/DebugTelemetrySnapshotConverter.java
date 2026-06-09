package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.DebugTelemetrySnapshot;
import java.util.ArrayList;
import java.util.List;

/** Converter for server-to-client debug telemetry snapshots. */
public final class DebugTelemetrySnapshotConverter
    implements MessageConverter<
        DebugTelemetrySnapshot, core.network.proto.s2c.DebugTelemetrySnapshot> {
  private static final byte WIRE_TYPE_ID = 25;

  @Override
  public core.network.proto.s2c.DebugTelemetrySnapshot toProto(DebugTelemetrySnapshot message) {
    core.network.proto.s2c.DebugTelemetrySnapshot.Builder builder =
        core.network.proto.s2c.DebugTelemetrySnapshot.newBuilder()
            .setRequestId(message.requestId())
            .setServerTimeMs(message.serverTimeMs())
            .setServerTimeNanos(message.serverTimeNanos())
            .setTcpOutboundMessages(message.transport().tcpOutboundMessages())
            .setTcpOutboundBytes(message.transport().tcpOutboundBytes())
            .setTcpInboundMessages(message.transport().tcpInboundMessages())
            .setTcpInboundBytes(message.transport().tcpInboundBytes())
            .setUdpOutboundMessages(message.transport().udpOutboundMessages())
            .setUdpOutboundBytes(message.transport().udpOutboundBytes())
            .setUdpInboundMessages(message.transport().udpInboundMessages())
            .setUdpInboundBytes(message.transport().udpInboundBytes())
            .setDebugTcpOutboundMessages(message.debugTransport().tcpOutboundMessages())
            .setDebugTcpOutboundBytes(message.debugTransport().tcpOutboundBytes())
            .setDebugTcpInboundMessages(message.debugTransport().tcpInboundMessages())
            .setDebugTcpInboundBytes(message.debugTransport().tcpInboundBytes())
            .setDebugUdpOutboundMessages(message.debugTransport().udpOutboundMessages())
            .setDebugUdpOutboundBytes(message.debugTransport().udpOutboundBytes())
            .setDebugUdpInboundMessages(message.debugTransport().udpInboundMessages())
            .setDebugUdpInboundBytes(message.debugTransport().udpInboundBytes())
            .setUdpFallbacks(message.udp().fallbacks())
            .setUdpOversizedPackets(message.udp().oversizedPackets())
            .setUdpSendFailures(message.udp().sendFailures())
            .setUdpDroppedPackets(message.udp().droppedPackets())
            .setFullSnapshotsSent(message.snapshots().fullSent())
            .setDeltaSnapshotsSent(message.snapshots().deltaSent())
            .setLastFullSnapshotTick(message.snapshots().lastFullTick())
            .setLastFullSnapshotBytes(message.snapshots().lastFullBytes())
            .setLastFullSnapshotEntities(message.snapshots().lastFullEntities())
            .setLastDeltaSnapshotTick(message.snapshots().lastDeltaTick())
            .setLastDeltaSnapshotBytes(message.snapshots().lastDeltaBytes())
            .setLastDeltaSnapshotEntityDeltas(message.snapshots().lastDeltaEntityDeltas())
            .setLastDeltaSnapshotRemovals(message.snapshots().lastDeltaRemovals())
            .setLastSnapshotBuildMicros(message.snapshots().lastBuildMicros())
            .setLastFullSnapshotReason(message.snapshots().lastFullReason())
            .setStaleFullSnapshotBytes(message.snapshots().staleFullBytes())
            .setPeriodicFullSnapshotsSent(message.snapshots().periodicFullSent())
            .setFallbackFullSnapshotsSent(message.snapshots().fallbackFullSent())
            .setMissingBaselineFullFallbacks(message.snapshots().missingBaselineFullFallbacks())
            .setSnapshotHistoryServerTick(message.snapshots().historyServerTick())
            .setSnapshotHistorySize(message.snapshots().historySize())
            .setSnapshotHistoryCapacityTicks(message.snapshots().historyCapacityTicks())
            .setSnapshotHistoryCapacitySeconds(message.snapshots().historyCapacitySeconds())
            .setTransportOutBytesLastSecond(message.windows().transportOutBytesLastSecond())
            .setTransportOutBytesLastFiveSeconds(
                message.windows().transportOutBytesLastFiveSeconds())
            .setTransportOutBytesLastThirtySeconds(
                message.windows().transportOutBytesLastThirtySeconds())
            .setSnapshotsSentLastSecond(message.windows().snapshotsSentLastSecond())
            .setSnapshotsSentLastFiveSeconds(message.windows().snapshotsSentLastFiveSeconds())
            .setSnapshotsSentLastThirtySeconds(message.windows().snapshotsSentLastThirtySeconds())
            .setFullSnapshotsSentLastSecond(message.windows().fullSnapshotsSentLastSecond())
            .setFullSnapshotsSentLastFiveSeconds(
                message.windows().fullSnapshotsSentLastFiveSeconds())
            .setFullSnapshotsSentLastThirtySeconds(
                message.windows().fullSnapshotsSentLastThirtySeconds())
            .setFullSnapshotBytesLastSecond(message.windows().fullSnapshotBytesLastSecond())
            .setFullSnapshotBytesLastFiveSeconds(
                message.windows().fullSnapshotBytesLastFiveSeconds())
            .setFullSnapshotBytesLastThirtySeconds(
                message.windows().fullSnapshotBytesLastThirtySeconds())
            .setLastTcpDecodeType(message.timings().lastTcpDecodeType())
            .setLastTcpDecodeMicros(message.timings().lastTcpDecodeMicros())
            .setMaxTcpDecodeMicrosLastTenSeconds(
                message.timings().maxTcpDecodeMicrosLastTenSeconds())
            .setMaxTcpDecodeTypeLastTenSeconds(message.timings().maxTcpDecodeTypeLastTenSeconds())
            .setLastQueueAgeMicros(message.timings().lastQueueAgeMicros())
            .setMaxQueueAgeMicrosLastTenSeconds(message.timings().maxQueueAgeMicrosLastTenSeconds())
            .setMaxQueueAgeTypeLastTenSeconds(message.timings().maxQueueAgeTypeLastTenSeconds())
            .setLastDispatchMicros(message.timings().lastDispatchMicros())
            .setMaxDispatchMicrosLastTenSeconds(message.timings().maxDispatchMicrosLastTenSeconds())
            .setMaxDispatchTypeLastTenSeconds(message.timings().maxDispatchTypeLastTenSeconds())
            .setLastNetworkDispatchMicros(message.timings().lastNetworkDispatchMicros())
            .setMaxNetworkDispatchMicrosLastTenSeconds(
                message.timings().maxNetworkDispatchMicrosLastTenSeconds())
            .setLastFrameMicros(message.timings().lastFrameMicros())
            .setMaxFrameMicrosLastTenSeconds(message.timings().maxFrameMicrosLastTenSeconds())
            .setLastGcPauseMs(message.timings().lastGcPauseMs())
            .setMaxGcPauseMsLastTenSeconds(message.timings().maxGcPauseMsLastTenSeconds())
            .setLastUdpFallbackReason(message.udp().lastFallbackReason())
            .setLastUdpDropReason(message.udp().lastDropReason())
            .setLastUdpFailureReason(message.udp().lastFailureReason());

    message.clients().stream()
        .map(DebugTelemetrySnapshotConverter::toProto)
        .forEach(builder::addClients);
    return builder.build();
  }

  @Override
  public DebugTelemetrySnapshot fromProto(core.network.proto.s2c.DebugTelemetrySnapshot proto) {
    List<DebugTelemetrySnapshot.Client> clients = new ArrayList<>();
    proto.getClientsList().stream()
        .map(DebugTelemetrySnapshotConverter::fromProto)
        .forEach(clients::add);
    return new DebugTelemetrySnapshot(
        proto.getRequestId(),
        proto.getServerTimeMs(),
        proto.getServerTimeNanos(),
        new DebugTelemetrySnapshot.Transport(
            proto.getTcpOutboundMessages(),
            proto.getTcpOutboundBytes(),
            proto.getTcpInboundMessages(),
            proto.getTcpInboundBytes(),
            proto.getUdpOutboundMessages(),
            proto.getUdpOutboundBytes(),
            proto.getUdpInboundMessages(),
            proto.getUdpInboundBytes()),
        new DebugTelemetrySnapshot.Transport(
            proto.getDebugTcpOutboundMessages(),
            proto.getDebugTcpOutboundBytes(),
            proto.getDebugTcpInboundMessages(),
            proto.getDebugTcpInboundBytes(),
            proto.getDebugUdpOutboundMessages(),
            proto.getDebugUdpOutboundBytes(),
            proto.getDebugUdpInboundMessages(),
            proto.getDebugUdpInboundBytes()),
        new DebugTelemetrySnapshot.Udp(
            proto.getUdpFallbacks(),
            proto.getUdpOversizedPackets(),
            proto.getUdpSendFailures(),
            proto.getUdpDroppedPackets(),
            proto.getLastUdpFallbackReason(),
            proto.getLastUdpDropReason(),
            proto.getLastUdpFailureReason()),
        new DebugTelemetrySnapshot.Snapshots(
            proto.getFullSnapshotsSent(),
            proto.getDeltaSnapshotsSent(),
            proto.getLastFullSnapshotTick(),
            proto.getLastFullSnapshotBytes(),
            proto.getLastFullSnapshotEntities(),
            proto.getLastDeltaSnapshotTick(),
            proto.getLastDeltaSnapshotBytes(),
            proto.getLastDeltaSnapshotEntityDeltas(),
            proto.getLastDeltaSnapshotRemovals(),
            proto.getLastSnapshotBuildMicros(),
            proto.getLastFullSnapshotReason(),
            proto.getStaleFullSnapshotBytes(),
            proto.getPeriodicFullSnapshotsSent(),
            proto.getFallbackFullSnapshotsSent(),
            proto.getMissingBaselineFullFallbacks(),
            proto.getSnapshotHistoryServerTick(),
            proto.getSnapshotHistorySize(),
            proto.getSnapshotHistoryCapacityTicks(),
            proto.getSnapshotHistoryCapacitySeconds()),
        new DebugTelemetrySnapshot.Windows(
            proto.getTransportOutBytesLastSecond(),
            proto.getTransportOutBytesLastFiveSeconds(),
            proto.getTransportOutBytesLastThirtySeconds(),
            proto.getSnapshotsSentLastSecond(),
            proto.getSnapshotsSentLastFiveSeconds(),
            proto.getSnapshotsSentLastThirtySeconds(),
            proto.getFullSnapshotsSentLastSecond(),
            proto.getFullSnapshotsSentLastFiveSeconds(),
            proto.getFullSnapshotsSentLastThirtySeconds(),
            proto.getFullSnapshotBytesLastSecond(),
            proto.getFullSnapshotBytesLastFiveSeconds(),
            proto.getFullSnapshotBytesLastThirtySeconds()),
        new DebugTelemetrySnapshot.Timings(
            proto.getLastTcpDecodeType(),
            proto.getLastTcpDecodeMicros(),
            proto.getMaxTcpDecodeMicrosLastTenSeconds(),
            proto.getMaxTcpDecodeTypeLastTenSeconds(),
            proto.getLastQueueAgeMicros(),
            proto.getMaxQueueAgeMicrosLastTenSeconds(),
            proto.getMaxQueueAgeTypeLastTenSeconds(),
            proto.getLastDispatchMicros(),
            proto.getMaxDispatchMicrosLastTenSeconds(),
            proto.getMaxDispatchTypeLastTenSeconds(),
            proto.getLastNetworkDispatchMicros(),
            proto.getMaxNetworkDispatchMicrosLastTenSeconds(),
            proto.getLastFrameMicros(),
            proto.getMaxFrameMicrosLastTenSeconds(),
            proto.getLastGcPauseMs(),
            proto.getMaxGcPauseMsLastTenSeconds()),
        clients);
  }

  @Override
  public Class<DebugTelemetrySnapshot> domainType() {
    return DebugTelemetrySnapshot.class;
  }

  @Override
  public Class<core.network.proto.s2c.DebugTelemetrySnapshot> protoType() {
    return core.network.proto.s2c.DebugTelemetrySnapshot.class;
  }

  @Override
  public Parser<core.network.proto.s2c.DebugTelemetrySnapshot> parser() {
    return core.network.proto.s2c.DebugTelemetrySnapshot.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }

  private static core.network.proto.s2c.DebugTelemetryClient toProto(
      DebugTelemetrySnapshot.Client client) {
    return core.network.proto.s2c.DebugTelemetryClient.newBuilder()
        .setClientId(client.clientId())
        .setUdpReady(client.udpReady())
        .setRttEstimateMs(client.rttEstimateMs())
        .setLastActivityAgeMs(client.lastActivityAgeMs())
        .setLatestAckedSnapshotTick(client.latestAckedSnapshotTick())
        .setServerCurrentTick(client.serverCurrentTick())
        .setAckAgeTicks(client.ackAgeTicks())
        .setAckAgeMs(client.ackAgeMs())
        .setAckBaselineInHistory(client.ackBaselineInHistory())
        .setHistoryCapacityTicks(client.historyCapacityTicks())
        .setHistoryCapacitySeconds(client.historyCapacitySeconds())
        .setMissingBaselineFullFallbacks(client.missingBaselineFullFallbacks())
        .setFullSnapshotsLastSecond(client.fullSnapshotsLastSecond())
        .setFullSnapshotsLastFiveSeconds(client.fullSnapshotsLastFiveSeconds())
        .setFullSnapshotsLastThirtySeconds(client.fullSnapshotsLastThirtySeconds())
        .setFullSnapshotBytesLastSecond(client.fullSnapshotBytesLastSecond())
        .setFullSnapshotBytesLastFiveSeconds(client.fullSnapshotBytesLastFiveSeconds())
        .setFullSnapshotBytesLastThirtySeconds(client.fullSnapshotBytesLastThirtySeconds())
        .setPeriodicFullSnapshots(client.periodicFullSnapshots())
        .setFallbackFullSnapshots(client.fallbackFullSnapshots())
        .setLastFullSnapshotReason(client.lastFullSnapshotReason())
        .setLastFullSnapshotAgeMs(client.lastFullSnapshotAgeMs())
        .setLastFullSnapshotTick(client.lastFullSnapshotTick())
        .setLastFullSnapshotBytes(client.lastFullSnapshotBytes())
        .build();
  }

  private static DebugTelemetrySnapshot.Client fromProto(
      core.network.proto.s2c.DebugTelemetryClient proto) {
    return new DebugTelemetrySnapshot.Client(
        (short) proto.getClientId(),
        proto.getUdpReady(),
        proto.getRttEstimateMs(),
        proto.getLastActivityAgeMs(),
        proto.getLatestAckedSnapshotTick(),
        proto.getServerCurrentTick(),
        proto.getAckAgeTicks(),
        proto.getAckAgeMs(),
        proto.getAckBaselineInHistory(),
        proto.getHistoryCapacityTicks(),
        proto.getHistoryCapacitySeconds(),
        proto.getMissingBaselineFullFallbacks(),
        proto.getFullSnapshotsLastSecond(),
        proto.getFullSnapshotsLastFiveSeconds(),
        proto.getFullSnapshotsLastThirtySeconds(),
        proto.getFullSnapshotBytesLastSecond(),
        proto.getFullSnapshotBytesLastFiveSeconds(),
        proto.getFullSnapshotBytesLastThirtySeconds(),
        proto.getPeriodicFullSnapshots(),
        proto.getFallbackFullSnapshots(),
        proto.getLastFullSnapshotReason(),
        proto.getLastFullSnapshotAgeMs(),
        proto.getLastFullSnapshotTick(),
        proto.getLastFullSnapshotBytes());
  }
}
