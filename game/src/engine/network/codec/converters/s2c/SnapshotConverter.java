package engine.network.codec.converters.s2c;

import com.google.protobuf.Parser;
import engine.network.codec.CommonProtoConverters;
import engine.network.codec.MessageConverter;
import engine.network.messages.s2c.DoorTileState;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.LevelState;
import engine.network.messages.s2c.SnapshotMessage;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Converter for server-to-client snapshot messages. */
public final class SnapshotConverter
    implements MessageConverter<SnapshotMessage, engine.network.proto.s2c.SnapshotMessage> {
  private static final byte WIRE_TYPE_ID = 18;
  private static final EntityStateConverter ENTITY_STATE_CONVERTER = new EntityStateConverter();

  @Override
  public engine.network.proto.s2c.SnapshotMessage toProto(SnapshotMessage message) {
    engine.network.proto.s2c.SnapshotMessage.Builder builder =
        engine.network.proto.s2c.SnapshotMessage.newBuilder().setServerTick(message.serverTick());
    for (EntityState state : message.entities()) {
      builder.addEntities(ENTITY_STATE_CONVERTER.toProto(state));
    }
    builder.setLevelState(toProto(message.levelState()));
    return builder.build();
  }

  @Override
  public SnapshotMessage fromProto(engine.network.proto.s2c.SnapshotMessage proto) {
    List<EntityState> entities = new ArrayList<>();
    for (engine.network.proto.s2c.EntityState state : proto.getEntitiesList()) {
      entities.add(ENTITY_STATE_CONVERTER.fromProto(state));
    }
    return new SnapshotMessage(proto.getServerTick(), entities, fromProto(proto.getLevelState()));
  }

  static engine.network.proto.s2c.LevelState toProto(LevelState message) {
    engine.network.proto.s2c.LevelState.Builder builder =
        engine.network.proto.s2c.LevelState.newBuilder();
    for (DoorTileState doorState : message.doorStates()) {
      builder.addDoorStates(
          engine.network.proto.s2c.DoorState.newBuilder()
              .setCoordinate(CommonProtoConverters.toProto(doorState.coordinate()))
              .setOpen(doorState.open())
              .build());
    }
    return builder.build();
  }

  static LevelState fromProto(engine.network.proto.s2c.LevelState proto) {
    Set<DoorTileState> doorStates = new LinkedHashSet<>();
    for (engine.network.proto.s2c.DoorState doorState : proto.getDoorStatesList()) {
      if (!doorState.hasCoordinate()) {
        throw new IllegalArgumentException("DoorState.coordinate is required.");
      }
      doorStates.add(
          new DoorTileState(
              CommonProtoConverters.fromProto(doorState.getCoordinate()), doorState.getOpen()));
    }
    return new LevelState(doorStates);
  }

  @Override
  public Class<SnapshotMessage> domainType() {
    return SnapshotMessage.class;
  }

  @Override
  public Class<engine.network.proto.s2c.SnapshotMessage> protoType() {
    return engine.network.proto.s2c.SnapshotMessage.class;
  }

  @Override
  public Parser<engine.network.proto.s2c.SnapshotMessage> parser() {
    return engine.network.proto.s2c.SnapshotMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
