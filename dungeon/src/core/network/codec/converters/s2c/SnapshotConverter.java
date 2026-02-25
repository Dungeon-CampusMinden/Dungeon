package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import java.util.ArrayList;
import java.util.List;

/** Converter for server-to-client snapshot messages. */
public final class SnapshotConverter
    implements MessageConverter<SnapshotMessage, core.network.proto.s2c.SnapshotMessage> {
  private static final byte WIRE_TYPE_ID = 18;
  private static final EntityStateConverter ENTITY_STATE_CONVERTER = new EntityStateConverter();

  @Override
  public core.network.proto.s2c.SnapshotMessage toProto(SnapshotMessage message) {
    core.network.proto.s2c.SnapshotMessage.Builder builder =
        core.network.proto.s2c.SnapshotMessage.newBuilder().setServerTick(message.serverTick());
    for (EntityState state : message.entities()) {
      builder.addEntities(ENTITY_STATE_CONVERTER.toProto(state));
    }
    return builder.build();
  }

  @Override
  public SnapshotMessage fromProto(core.network.proto.s2c.SnapshotMessage proto) {
    List<EntityState> entities = new ArrayList<>();
    for (core.network.proto.s2c.EntityState state : proto.getEntitiesList()) {
      entities.add(ENTITY_STATE_CONVERTER.fromProto(state));
    }
    return new SnapshotMessage(proto.getServerTick(), entities);
  }

  @Override
  public Class<SnapshotMessage> domainType() {
    return SnapshotMessage.class;
  }

  @Override
  public Class<core.network.proto.s2c.SnapshotMessage> protoType() {
    return core.network.proto.s2c.SnapshotMessage.class;
  }

  @Override
  public Parser<core.network.proto.s2c.SnapshotMessage> parser() {
    return core.network.proto.s2c.SnapshotMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
