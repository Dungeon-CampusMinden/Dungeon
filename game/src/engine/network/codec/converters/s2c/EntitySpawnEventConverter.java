package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.utils.components.draw.DrawInfoData;

/** Converter for server-to-client entity spawn event messages. */
public final class EntitySpawnEventConverter
    implements MessageConverter<EntitySpawnEvent, engine.network.proto.s2c.EntitySpawnEvent> {
  private static final byte WIRE_TYPE_ID = 11;

  @Override
  public engine.network.proto.s2c.EntitySpawnEvent toProto(EntitySpawnEvent message) {
    engine.network.proto.s2c.EntitySpawnEvent.Builder builder =
        engine.network.proto.s2c.EntitySpawnEvent.newBuilder().setEntityId(message.entityId());

    if (message.positionComponent() != null) {
      builder.setPosition(CommonProtoConverters.toProto(message.positionComponent()));
    }

    if (message.drawInfo() != null) {
      builder.setDrawInfo(CommonProtoConverters.toProto(message.drawInfo()));
    }

    PlayerComponent playerComponent = message.playerComponent();
    if (playerComponent != null) {
      builder.setPlayerInfo(CommonProtoConverters.toProto(playerComponent));
    }

    if (playerComponent != null || message.characterClassId() != 0) {
      builder.setCharacterClassId(message.characterClassId());
    }

    if (!message.metadata().isEmpty()) {
      builder.putAllMetadata(message.metadata());
    }

    return builder.build();
  }

  @Override
  public EntitySpawnEvent fromProto(engine.network.proto.s2c.EntitySpawnEvent proto) {
    PositionComponent position =
        proto.hasPosition() ? CommonProtoConverters.fromProto(proto.getPosition()) : null;
    DrawInfoData drawInfo =
        proto.hasDrawInfo() ? CommonProtoConverters.fromProto(proto.getDrawInfo()) : null;
    PlayerComponent playerComponent =
        proto.hasPlayerInfo() ? CommonProtoConverters.fromProto(proto.getPlayerInfo()) : null;
    byte characterClassId =
        proto.hasCharacterClassId()
            ? CommonProtoConverters.toByteExact(proto.getCharacterClassId(), "character_class_id")
            : 0;

    return new EntitySpawnEvent(
        proto.getEntityId(),
        position,
        drawInfo,
        playerComponent,
        characterClassId,
        proto.getMetadataMap());
  }

  @Override
  public Class<EntitySpawnEvent> domainType() {
    return EntitySpawnEvent.class;
  }

  @Override
  public Class<engine.network.proto.s2c.EntitySpawnEvent> protoType() {
    return engine.network.proto.s2c.EntitySpawnEvent.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.EntitySpawnEvent> parser() {
    return engine.network.proto.s2c.EntitySpawnEvent.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
