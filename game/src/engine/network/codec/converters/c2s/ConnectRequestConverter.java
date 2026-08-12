package engine.network.codec.converters.c2s;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.c2s.ConnectRequest;
import java.util.Optional;

/** Converter for client-to-server connect request messages. */
public final class ConnectRequestConverter
    implements MessageConverter<ConnectRequest, engine.network.proto.c2s.ConnectRequest> {
  private static final byte WIRE_TYPE_ID = 1;

  @Override
  public engine.network.proto.c2s.ConnectRequest toProto(ConnectRequest request) {
    engine.network.proto.c2s.ConnectRequest.Builder builder =
        engine.network.proto.c2s.ConnectRequest.newBuilder()
            .setProtocolVersion(request.protocolVersion())
            .setPlayerName(request.playerName());
    if (request.sessionId() != 0) {
      builder.setSessionId(request.sessionId());
    }
    byte[] token = request.sessionToken();
    if (token != null && token.length > 0) {
      builder.setSessionToken(ByteString.copyFrom(token));
    }
    request
        .characterClass()
        .ifPresent(characterClass -> builder.setCharacterClassId(characterClass.ordinal()));
    return builder.build();
  }

  @Override
  public ConnectRequest fromProto(engine.network.proto.c2s.ConnectRequest proto) {
    int sessionId = proto.hasSessionId() ? proto.getSessionId() : 0;
    byte[] token = proto.hasSessionToken() ? proto.getSessionToken().toByteArray() : new byte[0];
    return new ConnectRequest(
        CommonProtoConverters.toShortExact(proto.getProtocolVersion(), "protocol_version"),
        proto.getPlayerName(),
        sessionId,
        token,
        proto.hasCharacterClassId()
            ? Optional.of(
                feature.entities.CharacterClass.fromByteId(
                    CommonProtoConverters.toByteExact(
                        proto.getCharacterClassId(), "character_class_id")))
            : Optional.empty());
  }

  @Override
  public Class<ConnectRequest> domainType() {
    return ConnectRequest.class;
  }

  @Override
  public Class<engine.network.proto.c2s.ConnectRequest> protoType() {
    return engine.network.proto.c2s.ConnectRequest.class;
  }

  @Override
  public Parser<engine.network.proto.c2s.ConnectRequest> parser() {
    return engine.network.proto.c2s.ConnectRequest.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
