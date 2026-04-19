package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.EntitySpawnEvent;
import core.utils.components.draw.DrawInfoData;

/** Converter for server-to-client entity spawn event messages. */
public final class EntitySpawnEventConverter
    implements MessageConverter<EntitySpawnEvent, core.network.proto.s2c.EntitySpawnEvent> {
  private static final byte WIRE_TYPE_ID = 11;

  @Override
  public core.network.proto.s2c.EntitySpawnEvent toProto(EntitySpawnEvent message) {
    core.network.proto.s2c.EntitySpawnEvent.Builder builder =
        core.network.proto.s2c.EntitySpawnEvent.newBuilder()
            .setEntityId(message.entityId())
            .setIsPersistent(message.isPersistent());

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
  public EntitySpawnEvent fromProto(core.network.proto.s2c.EntitySpawnEvent proto) {
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
        proto.getIsPersistent(),
        playerComponent,
        characterClassId,
        proto.getMetadataMap());
  }

  @Override
  public Class<EntitySpawnEvent> domainType() {
    return EntitySpawnEvent.class;
  }

  @Override
  public Class<core.network.proto.s2c.EntitySpawnEvent> protoType() {
    return core.network.proto.s2c.EntitySpawnEvent.class;
  }

  @Override
  public Parser<core.network.proto.s2c.EntitySpawnEvent> parser() {
    return core.network.proto.s2c.EntitySpawnEvent.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
