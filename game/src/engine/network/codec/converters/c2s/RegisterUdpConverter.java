package engine.network.codec.converters.c2s;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.RegisterUdp;

/** Converter for client-to-server UDP registration messages. */
public final class RegisterUdpConverter
    implements MessageConverter<RegisterUdp, engine.network.proto.c2s.RegisterUdp> {
  private static final byte WIRE_TYPE_ID = 4;

  @Override
  public engine.network.proto.c2s.RegisterUdp toProto(RegisterUdp message) {
    return engine.network.proto.c2s.RegisterUdp.newBuilder()
        .setSessionId(message.sessionId())
        .setSessionToken(ByteString.copyFrom(message.sessionToken()))
        .setClientId(message.clientId())
        .build();
  }

  @Override
  public RegisterUdp fromProto(engine.network.proto.c2s.RegisterUdp proto) {
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
  public Class<engine.network.proto.c2s.RegisterUdp> protoType() {
    return engine.network.proto.c2s.RegisterUdp.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.RegisterUdp> parser() {
    return engine.network.proto.c2s.RegisterUdp.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
