package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.EntityDespawnEvent;

/** Converter for server-to-client entity despawn messages. */
public final class EntityDespawnConverter
    implements MessageConverter<EntityDespawnEvent, core.network.proto.s2c.EntityDespawnEvent> {
  private static final byte WIRE_TYPE_ID = 13;

  @Override
  public core.network.proto.s2c.EntityDespawnEvent toProto(EntityDespawnEvent message) {
    return core.network.proto.s2c.EntityDespawnEvent.newBuilder()
        .setEntityId(message.entityId())
        .setReason(message.reason())
        .build();
  }

  @Override
  public EntityDespawnEvent fromProto(core.network.proto.s2c.EntityDespawnEvent proto) {
    return new EntityDespawnEvent(proto.getEntityId(), proto.getReason());
  }

  @Override
  public Class<EntityDespawnEvent> domainType() {
    return EntityDespawnEvent.class;
  }

  @Override
  public Class<core.network.proto.s2c.EntityDespawnEvent> protoType() {
    return core.network.proto.s2c.EntityDespawnEvent.class;
  }

  @Override
  public Parser<core.network.proto.s2c.EntityDespawnEvent> parser() {
    return core.network.proto.s2c.EntityDespawnEvent.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
