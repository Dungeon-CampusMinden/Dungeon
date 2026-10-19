package engine.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.CharacterOptionsRequest;

/** Converter for pre-authentication character-options requests. */
public final class CharacterOptionsRequestConverter
    implements MessageConverter<
        CharacterOptionsRequest, engine.network.proto.c2s.CharacterOptionsRequest> {
  private static final byte WIRE_TYPE_ID = 35;

  @Override
  public engine.network.proto.c2s.CharacterOptionsRequest toProto(CharacterOptionsRequest request) {
    return engine.network.proto.c2s.CharacterOptionsRequest.newBuilder()
        .setProtocolVersion(request.protocolVersion())
        .setPlayerName(request.playerName())
        .build();
  }

  @Override
  public CharacterOptionsRequest fromProto(engine.network.proto.c2s.CharacterOptionsRequest proto) {
    return new CharacterOptionsRequest(
        CommonProtoConverters.toShortExact(proto.getProtocolVersion(), "protocol_version"),
        proto.getPlayerName());
  }

  @Override
  public Class<CharacterOptionsRequest> domainType() {
    return CharacterOptionsRequest.class;
  }

  @Override
  public Class<engine.network.proto.c2s.CharacterOptionsRequest> protoType() {
    return engine.network.proto.c2s.CharacterOptionsRequest.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.CharacterOptionsRequest> parser() {
    return engine.network.proto.c2s.CharacterOptionsRequest.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
