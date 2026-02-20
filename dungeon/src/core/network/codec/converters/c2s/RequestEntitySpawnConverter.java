package core.network.codec.converters.c2s;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.RequestEntitySpawn;

/** Converter for client-to-server entity spawn request messages. */
public final class RequestEntitySpawnConverter
    implements MessageConverter<RequestEntitySpawn, core.network.proto.c2s.RequestEntitySpawn> {
  private static final byte WIRE_TYPE_ID = 5;

  @Override
  public core.network.proto.c2s.RequestEntitySpawn toProto(RequestEntitySpawn message) {
    return core.network.proto.c2s.RequestEntitySpawn.newBuilder()
        .setEntityId(message.entityId())
        .build();
  }

  @Override
  public RequestEntitySpawn fromProto(core.network.proto.c2s.RequestEntitySpawn proto) {
    return new RequestEntitySpawn(proto.getEntityId());
  }

  @Override
  public Class<RequestEntitySpawn> domainType() {
    return RequestEntitySpawn.class;
  }

  @Override
  public Class<core.network.proto.c2s.RequestEntitySpawn> protoType() {
    return core.network.proto.c2s.RequestEntitySpawn.class;
  }

  @Override
  public Parser<core.network.proto.c2s.RequestEntitySpawn> parser() {
    return core.network.proto.c2s.RequestEntitySpawn.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
