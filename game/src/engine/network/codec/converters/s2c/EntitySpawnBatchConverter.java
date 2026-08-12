package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.EntitySpawnBatch;
import engine.network.messages.s2c.EntitySpawnEvent;
import java.util.ArrayList;
import java.util.List;

/** Converter for server-to-client entity spawn batch messages. */
public final class EntitySpawnBatchConverter
    implements MessageConverter<EntitySpawnBatch, engine.network.proto.s2c.EntitySpawnBatch> {
  private static final byte WIRE_TYPE_ID = 12;
  private static final EntitySpawnEventConverter ENTITY_SPAWN_EVENT_CONVERTER =
      new EntitySpawnEventConverter();

  @Override
  public engine.network.proto.s2c.EntitySpawnBatch toProto(EntitySpawnBatch message) {
    engine.network.proto.s2c.EntitySpawnBatch.Builder builder =
        engine.network.proto.s2c.EntitySpawnBatch.newBuilder();
    for (EntitySpawnEvent event : message.entities()) {
      builder.addEntities(ENTITY_SPAWN_EVENT_CONVERTER.toProto(event));
    }
    return builder.build();
  }

  @Override
  public EntitySpawnBatch fromProto(engine.network.proto.s2c.EntitySpawnBatch proto) {
    List<EntitySpawnEvent> events = new ArrayList<>();
    for (engine.network.proto.s2c.EntitySpawnEvent event : proto.getEntitiesList()) {
      events.add(ENTITY_SPAWN_EVENT_CONVERTER.fromProto(event));
    }
    return new EntitySpawnBatch(events);
  }

  @Override
  public Class<EntitySpawnBatch> domainType() {
    return EntitySpawnBatch.class;
  }

  @Override
  public Class<engine.network.proto.s2c.EntitySpawnBatch> protoType() {
    return engine.network.proto.s2c.EntitySpawnBatch.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.EntitySpawnBatch> parser() {
    return engine.network.proto.s2c.EntitySpawnBatch.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
