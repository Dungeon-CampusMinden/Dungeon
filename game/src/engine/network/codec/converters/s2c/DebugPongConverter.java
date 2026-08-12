package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.DebugPong;

/** Converter for server-to-client debug pongs. */
public final class DebugPongConverter
    implements MessageConverter<DebugPong, engine.network.proto.s2c.DebugPong> {
  private static final byte WIRE_TYPE_ID = 26;

  @Override
  public engine.network.proto.s2c.DebugPong toProto(DebugPong message) {
    return engine.network.proto.s2c.DebugPong.newBuilder()
        .setRequestId(message.requestId())
        .setClientTimeNanos(message.clientTimeNanos())
        .setServerReceiveTimeMs(message.serverReceiveTimeMs())
        .setServerSendTimeMs(message.serverSendTimeMs())
        .build();
  }

  @Override
  public DebugPong fromProto(engine.network.proto.s2c.DebugPong proto) {
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
  public Class<engine.network.proto.s2c.DebugPong> protoType() {
    return engine.network.proto.s2c.DebugPong.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.DebugPong> parser() {
    return engine.network.proto.s2c.DebugPong.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
