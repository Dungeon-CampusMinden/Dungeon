package engine.network.codec.converters.s2c;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.ConnectAck;

/** Converter for server-to-client connect acknowledgment messages. */
public final class ConnectAckConverter
    implements MessageConverter<ConnectAck, engine.network.proto.s2c.ConnectAck> {
  private static final byte WIRE_TYPE_ID = 7;

  @Override
  public engine.network.proto.s2c.ConnectAck toProto(ConnectAck message) {
    return engine.network.proto.s2c.ConnectAck.newBuilder()
        .setClientId(message.clientId())
        .setSessionId(message.sessionId())
        .setSessionToken(ByteString.copyFrom(message.sessionToken()))
        .build();
  }

  @Override
  public ConnectAck fromProto(engine.network.proto.s2c.ConnectAck proto) {
    return new ConnectAck(
        CommonProtoConverters.toShortExact(proto.getClientId(), "client_id"),
        proto.getSessionId(),
        proto.getSessionToken().toByteArray());
  }

  @Override
  public Class<ConnectAck> domainType() {
    return ConnectAck.class;
  }

  @Override
  public Class<engine.network.proto.s2c.ConnectAck> protoType() {
    return engine.network.proto.s2c.ConnectAck.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.ConnectAck> parser() {
    return engine.network.proto.s2c.ConnectAck.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
