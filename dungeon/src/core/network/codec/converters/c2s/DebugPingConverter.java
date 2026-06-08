package core.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.DebugPing;

/** Converter for client-to-server debug pings. */
public final class DebugPingConverter
    implements MessageConverter<DebugPing, core.network.proto.c2s.DebugPing> {
  private static final byte WIRE_TYPE_ID = 24;

  @Override
  public core.network.proto.c2s.DebugPing toProto(DebugPing message) {
    return core.network.proto.c2s.DebugPing.newBuilder()
        .setRequestId(message.requestId())
        .setClientTimeNanos(message.clientTimeNanos())
        .build();
  }

  @Override
  public DebugPing fromProto(core.network.proto.c2s.DebugPing proto) {
    return new DebugPing(proto.getRequestId(), proto.getClientTimeNanos());
  }

  @Override
  public Class<DebugPing> domainType() {
    return DebugPing.class;
  }

  @Override
  public Class<core.network.proto.c2s.DebugPing> protoType() {
    return core.network.proto.c2s.DebugPing.class;
  }

  @Override
  public Parser<core.network.proto.c2s.DebugPing> parser() {
    return core.network.proto.c2s.DebugPing.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
