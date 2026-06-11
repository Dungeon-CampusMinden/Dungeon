package core.network.codec.converters.s2c;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.ConnectAck;

/** Converter for server-to-client connect acknowledgment messages. */
public final class ConnectAckConverter
    implements MessageConverter<ConnectAck, core.network.proto.s2c.ConnectAck> {
  private static final byte WIRE_TYPE_ID = 7;

  @Override
  public core.network.proto.s2c.ConnectAck toProto(ConnectAck message) {
    return core.network.proto.s2c.ConnectAck.newBuilder()
        .setClientId(message.clientId())
        .setSessionId(message.sessionId())
        .setSessionToken(ByteString.copyFrom(message.sessionToken()))
        .build();
  }

  @Override
  public ConnectAck fromProto(core.network.proto.s2c.ConnectAck proto) {
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
  public Class<core.network.proto.s2c.ConnectAck> protoType() {
    return core.network.proto.s2c.ConnectAck.class;
  }

  @Override
  public Parser<core.network.proto.s2c.ConnectAck> parser() {
    return core.network.proto.s2c.ConnectAck.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
