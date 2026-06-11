package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.EntitySpawnBatch;
import core.network.messages.s2c.EntitySpawnEvent;
import java.util.ArrayList;
import java.util.List;

/** Converter for server-to-client entity spawn batch messages. */
public final class EntitySpawnBatchConverter
    implements MessageConverter<EntitySpawnBatch, core.network.proto.s2c.EntitySpawnBatch> {
  private static final byte WIRE_TYPE_ID = 12;
  private static final EntitySpawnEventConverter ENTITY_SPAWN_EVENT_CONVERTER =
      new EntitySpawnEventConverter();

  @Override
  public core.network.proto.s2c.EntitySpawnBatch toProto(EntitySpawnBatch message) {
    core.network.proto.s2c.EntitySpawnBatch.Builder builder =
        core.network.proto.s2c.EntitySpawnBatch.newBuilder();
    for (EntitySpawnEvent event : message.entities()) {
      builder.addEntities(ENTITY_SPAWN_EVENT_CONVERTER.toProto(event));
    }
    return builder.build();
  }

  @Override
  public EntitySpawnBatch fromProto(core.network.proto.s2c.EntitySpawnBatch proto) {
    List<EntitySpawnEvent> events = new ArrayList<>();
    for (core.network.proto.s2c.EntitySpawnEvent event : proto.getEntitiesList()) {
      events.add(ENTITY_SPAWN_EVENT_CONVERTER.fromProto(event));
    }
    return new EntitySpawnBatch(events);
  }

  @Override
  public Class<EntitySpawnBatch> domainType() {
    return EntitySpawnBatch.class;
  }

  @Override
  public Class<core.network.proto.s2c.EntitySpawnBatch> protoType() {
    return core.network.proto.s2c.EntitySpawnBatch.class;
  }

  @Override
  public Parser<core.network.proto.s2c.EntitySpawnBatch> parser() {
    return core.network.proto.s2c.EntitySpawnBatch.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
