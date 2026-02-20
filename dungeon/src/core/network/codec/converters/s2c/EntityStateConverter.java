package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import contrib.item.Item;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.EntityState;
import core.utils.Direction;
import core.utils.Vector2;
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
    if (hasPosition) {
      // TODO: Support partial position updates once snapshot diffing is introduced.
      Direction viewDirection =
          message.viewDirection().map(CommonProtoConverters::parseDirection).orElse(Direction.NONE);
      float rotation = message.rotation().orElse(0.0f);
      Vector2 scale = message.scale().orElse(Vector2.ONE);

      core.network.proto.common.PositionInfo positionInfo =
          core.network.proto.common.PositionInfo.newBuilder()
              .setPosition(CommonProtoConverters.toProto(message.position().orElseThrow()))
              .setViewDirection(CommonProtoConverters.toProto(viewDirection))
              .setRotation(rotation)
              .setScale(CommonProtoConverters.toProto(scale))
              .build();
      builder.setPosition(positionInfo);
    } else if (hasViewDirection || hasRotation || hasScale) {
      throw new IllegalArgumentException("Position is required to send rotation or scale.");
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
            items -> {
              for (int i = 0; i < items.length; i++) {
                core.network.proto.s2c.ItemSlot.Builder slot =
                    core.network.proto.s2c.ItemSlot.newBuilder().setSlotIndex(i);
                Item item = items[i];
                if (item != null) {
                  slot.setItem(CommonProtoConverters.toProto(item));
                }
                builder.addInventory(slot);
              }
            });

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
      builder.position(CommonProtoConverters.fromProto(positionInfo.getPosition()));
      core.network.proto.common.Direction viewDirection = positionInfo.getViewDirection();
      if (viewDirection != core.network.proto.common.Direction.DIRECTION_UNSPECIFIED
          && viewDirection != core.network.proto.common.Direction.UNRECOGNIZED) {
        builder.viewDirection(CommonProtoConverters.fromProto(viewDirection));
      }
      builder.rotation(positionInfo.getRotation());
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
      int maxIndex =
          slots.stream().mapToInt(core.network.proto.s2c.ItemSlot::getSlotIndex).max().orElse(-1);
      if (maxIndex < 0) {
        throw new IllegalArgumentException("Inventory slot indices must be non-negative.");
      }
      Item[] items = new Item[maxIndex + 1];
      for (core.network.proto.s2c.ItemSlot slot : slots) {
        int index = slot.getSlotIndex();
        if (index < 0 || index >= items.length) {
          throw new IllegalArgumentException("Inventory slot index out of range: " + index);
        }
        if (slot.hasItem()) {
          items[index] = CommonProtoConverters.fromProto(slot.getItem());
        }
      }
      builder.inventory(items);
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
