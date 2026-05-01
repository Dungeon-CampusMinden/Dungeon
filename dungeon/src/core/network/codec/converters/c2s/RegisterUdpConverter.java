package core.network.codec.converters.c2s;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.RegisterUdp;

/** Converter for client-to-server UDP registration messages. */
public final class RegisterUdpConverter
    implements MessageConverter<RegisterUdp, core.network.proto.c2s.RegisterUdp> {
  private static final byte WIRE_TYPE_ID = 4;

  @Override
  public core.network.proto.c2s.RegisterUdp toProto(RegisterUdp message) {
    return core.network.proto.c2s.RegisterUdp.newBuilder()
        .setSessionId(message.sessionId())
        .setSessionToken(ByteString.copyFrom(message.sessionToken()))
        .setClientId(message.clientId())
        .build();
  }

  @Override
  public RegisterUdp fromProto(core.network.proto.c2s.RegisterUdp proto) {
    return new RegisterUdp(
        proto.getSessionId(),
        proto.getSessionToken().toByteArray(),
        CommonProtoConverters.toShortExact(proto.getClientId(), "client_id"));
  }

  @Override
  public Class<RegisterUdp> domainType() {
    return RegisterUdp.class;
  }

  @Override
  public Class<core.network.proto.c2s.RegisterUdp> protoType() {
    return core.network.proto.c2s.RegisterUdp.class;
  }

  @Override
  public Parser<core.network.proto.c2s.RegisterUdp> parser() {
    return core.network.proto.c2s.RegisterUdp.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
