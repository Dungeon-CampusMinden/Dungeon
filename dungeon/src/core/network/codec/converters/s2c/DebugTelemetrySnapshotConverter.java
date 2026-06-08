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
            .setTransportOutBytesLastSecond(message.windows().transportOutBytesLastSecond())
            .setTransportOutBytesLastFiveSeconds(
                message.windows().transportOutBytesLastFiveSeconds())
            .setSnapshotsSentLastSecond(message.windows().snapshotsSentLastSecond())
            .setSnapshotsSentLastFiveSeconds(message.windows().snapshotsSentLastFiveSeconds())
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
            proto.getLastSnapshotBuildMicros()),
        new DebugTelemetrySnapshot.Windows(
            proto.getTransportOutBytesLastSecond(),
            proto.getTransportOutBytesLastFiveSeconds(),
            proto.getSnapshotsSentLastSecond(),
            proto.getSnapshotsSentLastFiveSeconds()),
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
        .build();
  }

  private static DebugTelemetrySnapshot.Client fromProto(
      core.network.proto.s2c.DebugTelemetryClient proto) {
    return new DebugTelemetrySnapshot.Client(
        (short) proto.getClientId(),
        proto.getUdpReady(),
        proto.getRttEstimateMs(),
        proto.getLastActivityAgeMs(),
        proto.getLatestAckedSnapshotTick());
  }
}
