package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.InventorySlotState;
import java.util.ArrayList;
import java.util.List;

/** Converter for server-to-client entity state messages. */
public final class EntityStateConverter
    implements MessageConverter<EntityState, core.network.proto.s2c.EntityState> {
  private static final byte WIRE_TYPE_ID = 14;

  @Override
  public core.network.proto.s2c.EntityState toProto(EntityState message) {
    core.network.proto.s2c.EntityState.Builder builder =
        core.network.proto.s2c.EntityState.newBuilder().setEntityId(message.entityId());

    message.entityName().ifPresent(builder::setEntityName);
    boolean hasPosition = message.position().isPresent();
    boolean hasViewDirection = message.viewDirection().isPresent();
    boolean hasRotation = message.rotation().isPresent();
    boolean hasScale = message.scale().isPresent();
    if (hasPosition || hasViewDirection || hasRotation || hasScale) {
      core.network.proto.common.PositionInfo.Builder positionInfo =
          core.network.proto.common.PositionInfo.newBuilder();
      message.position().map(CommonProtoConverters::toProto).ifPresent(positionInfo::setPosition);
      message
          .viewDirection()
          .map(CommonProtoConverters::parseDirection)
          .map(CommonProtoConverters::toProto)
          .ifPresent(positionInfo::setViewDirection);
      message.rotation().ifPresent(positionInfo::setRotation);
      message.scale().map(CommonProtoConverters::toProto).ifPresent(positionInfo::setScale);
      builder.setPosition(positionInfo.build());
    }
    message.currentHealth().ifPresent(builder::setCurrentHealth);
    message.maxHealth().ifPresent(builder::setMaxHealth);
    message.currentMana().ifPresent(builder::setCurrentMana);
    message.maxMana().ifPresent(builder::setMaxMana);
    message.stateName().ifPresent(builder::setStateName);
    message.tintColor().ifPresent(builder::setTintColor);

    message
        .inventory()
        .ifPresent(
            slots -> {
              for (InventorySlotState inventorySlot : slots) {
                core.network.proto.s2c.ItemSlot.Builder slot =
                    core.network.proto.s2c.ItemSlot.newBuilder()
                        .setSlotIndex(inventorySlot.slotIndex());
                if (inventorySlot.item() != null) {
                  slot.setItem(CommonProtoConverters.toProto(inventorySlot.item()));
                }
                builder.addInventory(slot);
              }
            });
    message.metadata().filter(metadata -> !metadata.isEmpty()).ifPresent(builder::putAllMetadata);

    return builder.build();
  }

  @Override
  public EntityState fromProto(core.network.proto.s2c.EntityState proto) {
    EntityState.Builder builder = EntityState.builder().entityId(proto.getEntityId());

    if (proto.hasEntityName()) {
      builder.entityName(proto.getEntityName());
    }
    if (proto.hasPosition()) {
      core.network.proto.common.PositionInfo positionInfo = proto.getPosition();
      if (positionInfo.hasPosition()) {
        builder.position(CommonProtoConverters.fromProto(positionInfo.getPosition()));
      }
      if (positionInfo.hasViewDirection()) {
        core.network.proto.common.Direction viewDirection = positionInfo.getViewDirection();
        if (viewDirection != core.network.proto.common.Direction.DIRECTION_UNSPECIFIED
            && viewDirection != core.network.proto.common.Direction.UNRECOGNIZED) {
          builder.viewDirection(CommonProtoConverters.fromProto(viewDirection));
        }
      }
      if (positionInfo.hasRotation()) {
        builder.rotation(positionInfo.getRotation());
      }
      if (positionInfo.hasScale()) {
        builder.scale(CommonProtoConverters.fromProto(positionInfo.getScale()));
      }
    }
    if (proto.hasCurrentHealth()) {
      builder.currentHealth(proto.getCurrentHealth());
    }
    if (proto.hasMaxHealth()) {
      builder.maxHealth(proto.getMaxHealth());
    }
    if (proto.hasCurrentMana()) {
      builder.currentMana(proto.getCurrentMana());
    }
    if (proto.hasMaxMana()) {
      builder.maxMana(proto.getMaxMana());
    }
    if (proto.hasStateName()) {
      builder.stateName(proto.getStateName());
    }
    if (proto.hasTintColor()) {
      builder.tintColor(proto.getTintColor());
    }

    List<core.network.proto.s2c.ItemSlot> slots = proto.getInventoryList();
    if (!slots.isEmpty()) {
      List<InventorySlotState> inventorySlots = new ArrayList<>(slots.size());
      for (core.network.proto.s2c.ItemSlot slot : slots) {
        int index = slot.getSlotIndex();
        inventorySlots.add(
            new InventorySlotState(
                index,
                slot.hasItem() ? CommonProtoConverters.itemStateFromProto(slot.getItem()) : null));
      }
      builder.inventorySlots(inventorySlots);
    }

    if (!proto.getMetadataMap().isEmpty()) {
      builder.metadata(proto.getMetadataMap());
    }

    return builder.build();
  }

  @Override
  public Class<EntityState> domainType() {
    return EntityState.class;
  }

  @Override
  public Class<core.network.proto.s2c.EntityState> protoType() {
    return core.network.proto.s2c.EntityState.class;
  }

  @Override
  public Parser<core.network.proto.s2c.EntityState> parser() {
    return core.network.proto.s2c.EntityState.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
