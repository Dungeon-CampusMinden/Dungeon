package engine.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.DebugPing;

/** Converter for client-to-server debug pings. */
public final class DebugPingConverter
    implements MessageConverter<DebugPing, engine.network.proto.c2s.DebugPing> {
  private static final byte WIRE_TYPE_ID = 24;

  @Override
  public engine.network.proto.c2s.DebugPing toProto(DebugPing message) {
    return engine.network.proto.c2s.DebugPing.newBuilder()
        .setRequestId(message.requestId())
        .setClientTimeNanos(message.clientTimeNanos())
        .setLatestRttMs(message.latestRttMs())
        .build();
  }

  @Override
  public DebugPing fromProto(engine.network.proto.c2s.DebugPing proto) {
    return new DebugPing(proto.getRequestId(), proto.getClientTimeNanos(), proto.getLatestRttMs());
  }

  @Override
  public Class<DebugPing> domainType() {
    return DebugPing.class;
  }

  @Override
  public Class<engine.network.proto.c2s.DebugPing> protoType() {
    return engine.network.proto.c2s.DebugPing.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.DebugPing> parser() {
    return engine.network.proto.c2s.DebugPing.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
