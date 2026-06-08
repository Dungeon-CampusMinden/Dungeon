package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.DebugPong;

/** Converter for server-to-client debug pongs. */
public final class DebugPongConverter
    implements MessageConverter<DebugPong, core.network.proto.s2c.DebugPong> {
  private static final byte WIRE_TYPE_ID = 26;

  @Override
  public core.network.proto.s2c.DebugPong toProto(DebugPong message) {
    return core.network.proto.s2c.DebugPong.newBuilder()
        .setRequestId(message.requestId())
        .setClientTimeNanos(message.clientTimeNanos())
        .setServerReceiveTimeMs(message.serverReceiveTimeMs())
        .setServerSendTimeMs(message.serverSendTimeMs())
        .build();
  }

  @Override
  public DebugPong fromProto(core.network.proto.s2c.DebugPong proto) {
    return new DebugPong(
        proto.getRequestId(),
        proto.getClientTimeNanos(),
        proto.getServerReceiveTimeMs(),
        proto.getServerSendTimeMs());
  }

  @Override
  public Class<DebugPong> domainType() {
    return DebugPong.class;
  }

  @Override
  public Class<core.network.proto.s2c.DebugPong> protoType() {
    return core.network.proto.s2c.DebugPong.class;
  }

  @Override
  public Parser<core.network.proto.s2c.DebugPong> parser() {
    return core.network.proto.s2c.DebugPong.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
