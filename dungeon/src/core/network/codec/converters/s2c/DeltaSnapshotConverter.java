package core.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import core.network.codec.MessageConverter;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.EntityDelta;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.EntityStateField;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Converter for server-to-client delta snapshot messages. */
public final class DeltaSnapshotConverter
    implements MessageConverter<DeltaSnapshotMessage, core.network.proto.s2c.DeltaSnapshotMessage> {
  private static final byte WIRE_TYPE_ID = 21;
  private static final EntityStateConverter ENTITY_STATE_CONVERTER = new EntityStateConverter();

  @Override
  public core.network.proto.s2c.DeltaSnapshotMessage toProto(DeltaSnapshotMessage message) {
    core.network.proto.s2c.DeltaSnapshotMessage.Builder builder =
        core.network.proto.s2c.DeltaSnapshotMessage.newBuilder()
            .setBaseTick(message.baseTick())
            .setServerTick(message.serverTick())
            .addAllRemovedEntityIds(message.removedEntityIds());

    for (EntityDelta delta : message.entityDeltas()) {
      builder.addEntityDeltas(toProto(delta));
    }
    message
        .levelStateDeltaOptional()
        .map(SnapshotConverter::toProto)
        .ifPresent(builder::setLevelStateDelta);
    return builder.build();
  }

  @Override
  public DeltaSnapshotMessage fromProto(core.network.proto.s2c.DeltaSnapshotMessage proto) {
    List<EntityDelta> entityDeltas = new ArrayList<>();
    for (core.network.proto.s2c.EntityDelta delta : proto.getEntityDeltasList()) {
      entityDeltas.add(fromProto(delta));
    }

    return new DeltaSnapshotMessage(
        proto.getBaseTick(),
        proto.getServerTick(),
        entityDeltas,
        proto.getRemovedEntityIdsList(),
        proto.hasLevelStateDelta()
            ? SnapshotConverter.fromProto(proto.getLevelStateDelta())
            : null);
  }

  private static core.network.proto.s2c.EntityDelta toProto(EntityDelta delta) {
    core.network.proto.s2c.EntityDelta.Builder builder =
        core.network.proto.s2c.EntityDelta.newBuilder()
            .setEntityId(delta.entityId())
            .setChangedState(ENTITY_STATE_CONVERTER.toProto(delta.changedState()));
    for (EntityStateField field : delta.clearedFields()) {
      builder.addClearedFields(toProto(field));
    }
    return builder.build();
  }

  private static EntityDelta fromProto(core.network.proto.s2c.EntityDelta proto) {
    EntityState changedState =
        proto.hasChangedState()
            ? ENTITY_STATE_CONVERTER.fromProto(proto.getChangedState())
            : EntityState.builder().entityId(proto.getEntityId()).build();
    Set<EntityStateField> clearedFields = new LinkedHashSet<>();
    for (core.network.proto.s2c.EntityStateField field : proto.getClearedFieldsList()) {
      fromProto(field).ifPresent(clearedFields::add);
    }
    return new EntityDelta(proto.getEntityId(), changedState, clearedFields);
  }

  private static core.network.proto.s2c.EntityStateField toProto(EntityStateField field) {
    return switch (field) {
      case ENTITY_NAME -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_ENTITY_NAME;
      case POSITION -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_POSITION;
      case VIEW_DIRECTION ->
          core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_VIEW_DIRECTION;
      case ROTATION -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_ROTATION;
      case SCALE -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_SCALE;
      case CURRENT_HEALTH ->
          core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_CURRENT_HEALTH;
      case MAX_HEALTH -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_MAX_HEALTH;
      case CURRENT_MANA -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_CURRENT_MANA;
      case MAX_MANA -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_MAX_MANA;
      case STATE_NAME -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_STATE_NAME;
      case TINT_COLOR -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_TINT_COLOR;
      case INVENTORY -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_INVENTORY;
      case METADATA -> core.network.proto.s2c.EntityStateField.ENTITY_STATE_FIELD_METADATA;
    };
  }

  private static Optional<EntityStateField> fromProto(
      core.network.proto.s2c.EntityStateField field) {
    return switch (field) {
      case ENTITY_STATE_FIELD_ENTITY_NAME -> Optional.of(EntityStateField.ENTITY_NAME);
      case ENTITY_STATE_FIELD_POSITION -> Optional.of(EntityStateField.POSITION);
      case ENTITY_STATE_FIELD_VIEW_DIRECTION -> Optional.of(EntityStateField.VIEW_DIRECTION);
      case ENTITY_STATE_FIELD_ROTATION -> Optional.of(EntityStateField.ROTATION);
      case ENTITY_STATE_FIELD_SCALE -> Optional.of(EntityStateField.SCALE);
      case ENTITY_STATE_FIELD_CURRENT_HEALTH -> Optional.of(EntityStateField.CURRENT_HEALTH);
      case ENTITY_STATE_FIELD_MAX_HEALTH -> Optional.of(EntityStateField.MAX_HEALTH);
      case ENTITY_STATE_FIELD_CURRENT_MANA -> Optional.of(EntityStateField.CURRENT_MANA);
      case ENTITY_STATE_FIELD_MAX_MANA -> Optional.of(EntityStateField.MAX_MANA);
      case ENTITY_STATE_FIELD_STATE_NAME -> Optional.of(EntityStateField.STATE_NAME);
      case ENTITY_STATE_FIELD_TINT_COLOR -> Optional.of(EntityStateField.TINT_COLOR);
      case ENTITY_STATE_FIELD_INVENTORY -> Optional.of(EntityStateField.INVENTORY);
      case ENTITY_STATE_FIELD_METADATA -> Optional.of(EntityStateField.METADATA);
      case ENTITY_STATE_FIELD_UNSPECIFIED, UNRECOGNIZED -> Optional.empty();
    };
  }

  @Override
  public Class<DeltaSnapshotMessage> domainType() {
    return DeltaSnapshotMessage.class;
  }

  @Override
  public Class<core.network.proto.s2c.DeltaSnapshotMessage> protoType() {
    return core.network.proto.s2c.DeltaSnapshotMessage.class;
  }

  @Override
  public Parser<core.network.proto.s2c.DeltaSnapshotMessage> parser() {
    return core.network.proto.s2c.DeltaSnapshotMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
