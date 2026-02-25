package core.network.codec.converters.c2s;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.ConnectRequest;

/** Converter for client-to-server connect request messages. */
public final class ConnectRequestConverter
    implements MessageConverter<ConnectRequest, core.network.proto.c2s.ConnectRequest> {
  private static final byte WIRE_TYPE_ID = 1;

  @Override
  public core.network.proto.c2s.ConnectRequest toProto(ConnectRequest request) {
    core.network.proto.c2s.ConnectRequest.Builder builder =
        core.network.proto.c2s.ConnectRequest.newBuilder()
            .setProtocolVersion(request.protocolVersion())
            .setPlayerName(request.playerName());
    if (request.sessionId() != 0) {
      builder.setSessionId(request.sessionId());
    }
    byte[] token = request.sessionToken();
    if (token != null && token.length > 0) {
      builder.setSessionToken(ByteString.copyFrom(token));
    }
    return builder.build();
  }

  @Override
  public ConnectRequest fromProto(core.network.proto.c2s.ConnectRequest proto) {
    int sessionId = proto.hasSessionId() ? proto.getSessionId() : 0;
    byte[] token = proto.hasSessionToken() ? proto.getSessionToken().toByteArray() : new byte[0];
    return new ConnectRequest(
        CommonProtoConverters.toShortExact(proto.getProtocolVersion(), "protocol_version"),
        proto.getPlayerName(),
        sessionId,
        token);
  }

  @Override
  public Class<ConnectRequest> domainType() {
    return ConnectRequest.class;
  }

  @Override
  public Class<core.network.proto.c2s.ConnectRequest> protoType() {
    return core.network.proto.c2s.ConnectRequest.class;
  }

  @Override
  public Parser<core.network.proto.c2s.ConnectRequest> parser() {
    return core.network.proto.c2s.ConnectRequest.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
