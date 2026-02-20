package core.network.codec;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import contrib.item.Item;
import contrib.item.ItemRegistry;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.*;
import core.network.messages.s2c.*;
import core.network.proto.c2s.CustomAction;
import core.network.proto.common.IntList;
import core.network.proto.common.StringList;
import core.sound.SoundSpec;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DrawInfoData;

import java.util.*;

/** Converts between protobuf messages and domain objects for common network types. */
public final class ProtoConverter {

  private static final String DIALOG_CLOSED_KEY = "CLOSED";
  private static final int DEFAULT_CUSTOM_SCHEMA_VERSION = 1;

  private ProtoConverter() {}

  /**
   * Converts a {@link NetworkMessage} into its protobuf representation.
   *
   * @param message the network message to convert
   * @return the protobuf message
   */
  public static Message toProto(NetworkMessage message) {
    Objects.requireNonNull(message, "message");
    return switch (message) {
      case ConnectRequest msg -> toProto(msg);
      case InputMessage msg -> toProto(msg);
      case DialogResponseMessage msg -> toProto(msg);
      case RegisterUdp msg -> toProto(msg);
      case RequestEntitySpawn msg -> toProto(msg);
      case SoundFinishedMessage msg -> toProto(msg);
      case ConnectAck msg -> toProto(msg);
      case ConnectReject msg -> toProto(msg);
      case DialogShowMessage msg -> toProto(msg);
      case DialogCloseMessage msg -> toProto(msg);
      case EntitySpawnEvent msg -> toProto(msg);
      case EntitySpawnBatch msg -> toProto(msg);
      case EntityDespawnEvent msg -> toProto(msg);
      case EntityState msg -> toProto(msg);
      case GameOverEvent msg -> toProto(msg);
      case LevelChangeEvent msg -> toProto(msg);
      case RegisterAck msg -> toProto(msg);
      case SnapshotMessage msg -> toProto(msg);
      case SoundPlayMessage msg -> toProto(msg);
      case SoundStopMessage msg -> toProto(msg);
      default ->
          throw new IllegalArgumentException(
              "Unsupported network message type: " + message.getClass().getName());
    };
  }

  /**
   * Converts a protobuf message into its domain {@link NetworkMessage} representation.
   *
   * @param message the protobuf message to convert
   * @return the network message
   */
  public static NetworkMessage fromProto(Message message) {
    Objects.requireNonNull(message, "message");
    return switch (message) {
      case core.network.proto.c2s.ConnectRequest msg -> fromProto(msg);
      case core.network.proto.c2s.InputMessage msg -> fromProto(msg);
      case core.network.proto.c2s.DialogResponseMessage msg -> fromProto(msg);
      case core.network.proto.c2s.RegisterUdp msg -> fromProto(msg);
      case core.network.proto.c2s.RequestEntitySpawn msg -> fromProto(msg);
      case core.network.proto.c2s.SoundFinishedMessage msg -> fromProto(msg);
      case core.network.proto.s2c.ConnectAck msg -> fromProto(msg);
      case core.network.proto.s2c.ConnectReject msg -> fromProto(msg);
      case core.network.proto.s2c.DialogShowMessage msg -> fromProto(msg);
      case core.network.proto.s2c.DialogCloseMessage msg -> fromProto(msg);
      case core.network.proto.s2c.EntitySpawnEvent msg -> fromProto(msg);
      case core.network.proto.s2c.EntitySpawnBatch msg -> fromProto(msg);
      case core.network.proto.s2c.EntityDespawnEvent msg -> fromProto(msg);
      case core.network.proto.s2c.EntityState msg -> fromProto(msg);
      case core.network.proto.s2c.GameOverEvent msg -> fromProto(msg);
      case core.network.proto.s2c.LevelChangeEvent msg -> fromProto(msg);
      case core.network.proto.s2c.RegisterAck msg -> fromProto(msg);
      case core.network.proto.s2c.SnapshotMessage msg -> fromProto(msg);
      case core.network.proto.s2c.SoundPlayMessage msg -> fromProto(msg);
      case core.network.proto.s2c.SoundStopMessage msg -> fromProto(msg);
      default ->
          throw new IllegalArgumentException(
              "Unsupported proto message type: " + message.getClass().getName());
    };
  }

  /**
   * Converts a {@link Point} into its protobuf representation.
   *
   * @param point the domain point to convert
   * @return the protobuf point message
   */
  public static core.network.proto.common.Point toProto(Point point) {
    return core.network.proto.common.Point.newBuilder().setX(point.x()).setY(point.y()).build();
  }

  /**
   * Converts a protobuf point into a {@link Point}.
   *
   * @param proto the protobuf point message
   * @return the domain point
   */
  public static Point fromProto(core.network.proto.common.Point proto) {
    return new Point(proto.getX(), proto.getY());
  }

  /**
   * Converts a {@link Vector2} into its protobuf representation.
   *
   * @param vector the domain vector to convert
   * @return the protobuf vector message
   */
  public static core.network.proto.common.Vector2 toProto(Vector2 vector) {
    return core.network.proto.common.Vector2.newBuilder().setX(vector.x()).setY(vector.y()).build();
  }

  /**
   * Converts a protobuf vector into a {@link Vector2}.
   *
   * @param proto the protobuf vector message
   * @return the domain vector
   */
  public static Vector2 fromProto(core.network.proto.common.Vector2 proto) {
    return Vector2.of(proto.getX(), proto.getY());
  }

  /**
   * Converts a {@link PositionComponent} into its protobuf representation.
   *
   * @param component the position component to convert
   * @return the protobuf position info
   */
  public static core.network.proto.common.PositionInfo toProto(PositionComponent component) {
    return core.network.proto.common.PositionInfo.newBuilder()
        .setPosition(toProto(component.position()))
        .setViewDirection(toProto(component.viewDirection()))
        .setRotation(component.rotation())
        .setScale(toProto(component.scale()))
        .build();
  }

  /**
   * Converts a protobuf position info into a {@link PositionComponent}.
   *
   * @param proto the protobuf position info
   * @return the position component
   */
  public static PositionComponent fromProto(core.network.proto.common.PositionInfo proto) {
    if (!proto.hasPosition()) {
      throw new IllegalArgumentException("PositionInfo.position is required.");
    }

    PositionComponent component =
        new PositionComponent(fromProto(proto.getPosition()), fromProto(proto.getViewDirection()));
    component.rotation(proto.getRotation());
    if (proto.hasScale()) {
      component.scale(fromProto(proto.getScale()));
    }
    return component;
  }

  /**
   * Converts a {@link Direction} into its protobuf representation.
   *
   * @param direction the domain direction to convert
   * @return the protobuf direction enum
   */
  public static core.network.proto.common.Direction toProto(Direction direction) {
    return switch (direction) {
      case UP -> core.network.proto.common.Direction.DIRECTION_UP;
      case DOWN -> core.network.proto.common.Direction.DIRECTION_DOWN;
      case LEFT -> core.network.proto.common.Direction.DIRECTION_LEFT;
      case RIGHT -> core.network.proto.common.Direction.DIRECTION_RIGHT;
      case NONE -> core.network.proto.common.Direction.DIRECTION_NONE;
    };
  }

  /**
   * Converts a protobuf direction into a {@link Direction}.
   *
   * @param proto the protobuf direction enum
   * @return the domain direction
   */
  public static Direction fromProto(core.network.proto.common.Direction proto) {
    return switch (proto) {
      case DIRECTION_UP -> Direction.UP;
      case DIRECTION_DOWN -> Direction.DOWN;
      case DIRECTION_LEFT -> Direction.LEFT;
      case DIRECTION_RIGHT -> Direction.RIGHT;
      case DIRECTION_NONE, DIRECTION_UNSPECIFIED, UNRECOGNIZED -> Direction.NONE;
    };
  }

  /**
   * Converts a {@link SoundSpec} into its protobuf representation.
   *
   * <p>Target entity IDs are not included in the protobuf definition and are not serialized.
   *
   * @param spec the domain sound specification to convert
   * @return the protobuf sound specification
   */
  public static core.network.proto.common.SoundSpec toProto(SoundSpec spec) {
    return core.network.proto.common.SoundSpec.newBuilder()
        .setInstanceId(spec.instanceId())
        .setSoundName(spec.soundName())
        .setBaseVolume(spec.baseVolume())
        .setLooping(spec.looping())
        .setPitch(spec.pitch())
        .setPan(spec.pan())
        .setMaxDistance(spec.maxDistance())
        .setAttenuationFactor(spec.attenuationFactor())
        .build();
  }

  /**
   * Converts a protobuf sound specification into a {@link SoundSpec}.
   *
   * <p>Target entity IDs are not included in the protobuf definition and default to an empty array.
   *
   * @param proto the protobuf sound specification
   * @return the domain sound specification
   */
  public static SoundSpec fromProto(core.network.proto.common.SoundSpec proto) {
    return SoundSpec.builder(proto.getSoundName())
        .instanceId(proto.getInstanceId())
        .volume(proto.getBaseVolume())
        .looping(proto.getLooping())
        .pitch(proto.getPitch())
        .pan(proto.getPan())
        .maxDistance(proto.getMaxDistance())
        .attenuation(proto.getAttenuationFactor())
        .build();
  }

  /**
   * Converts a {@link ConnectRequest} into its protobuf representation.
   *
   * @param request the domain connect request to convert
   * @return the protobuf connect request
   */
  public static core.network.proto.c2s.ConnectRequest toProto(ConnectRequest request) {
    var builder =
        core.network.proto.c2s.ConnectRequest.newBuilder()
            .setProtocolVersion(request.protocolVersion())
            .setPlayerName(request.playerName());
    if (request.sessionId() != 0) {
      builder.setSessionId(request.sessionId());
    }
    byte[] token = request.sessionToken();
    if (token != null && token.length > 0) {
      builder.setSessionToken(ByteString.copyFrom(token));
    }
    return builder.build();
  }

  /**
   * Converts a protobuf connect request into a {@link ConnectRequest}.
   *
   * @param proto the protobuf connect request
   * @return the domain connect request
   */
  public static ConnectRequest fromProto(core.network.proto.c2s.ConnectRequest proto) {
    int sessionId = proto.hasSessionId() ? proto.getSessionId() : 0;
    byte[] token = proto.hasSessionToken() ? proto.getSessionToken().toByteArray() : new byte[0];
    return new ConnectRequest(
        toShortExact(proto.getProtocolVersion(), "protocol_version"),
        proto.getPlayerName(),
        sessionId,
        token);
  }

  /**
   * Converts an {@link InputMessage} into its protobuf representation.
   *
   * @param message the domain input message to convert
   * @return the protobuf input message
   */
  public static core.network.proto.c2s.InputMessage toProto(InputMessage message) {
    var builder =
        core.network.proto.c2s.InputMessage.newBuilder()
            .setSessionId(message.sessionId())
            .setClientTick(message.clientTick())
            .setSequence(message.sequence());

    switch (message.action()) {
      case MOVE -> {
        InputMessage.Move move = message.payloadAs(InputMessage.Move.class);
        Vector2 direction = move.direction();
        builder.setMove(
            core.network.proto.c2s.MoveAction.newBuilder()
                .setDirection(
                    core.network.proto.common.Vector2.newBuilder()
                        .setX(direction.x())
                        .setY(direction.y())
                        .build())
                .build());
      }
      case CAST_SKILL -> {
        InputMessage.CastSkill castSkill = message.payloadAs(InputMessage.CastSkill.class);
        builder.setCastSkill(
            core.network.proto.c2s.CastSkillAction.newBuilder()
                .setTarget(toProto(castSkill.target()))
                .setMainSkill(castSkill.mainSkill())
                .build());
      }
      case INTERACT -> {
        InputMessage.Interact interact = message.payloadAs(InputMessage.Interact.class);
        builder.setInteract(
            core.network.proto.c2s.InteractAction.newBuilder()
                .setTarget(toProto(interact.target()))
                .build());
      }
      case NEXT_SKILL, PREV_SKILL -> {
        InputMessage.SkillChange change = message.payloadAs(InputMessage.SkillChange.class);
        builder.setSkillChange(
            core.network.proto.c2s.SkillChangeAction.newBuilder()
                .setNextSkill(change.nextSkill())
                .setMainSkill(change.mainSkill())
                .build());
      }
      case INV_DROP -> {
        InputMessage.InventoryDrop drop = message.payloadAs(InputMessage.InventoryDrop.class);
        builder.setInvDrop(
            core.network.proto.c2s.InventoryDropAction.newBuilder()
                .setSlotIndex(drop.slotIndex())
                .build());
      }
      case INV_MOVE -> {
        InputMessage.InventoryMove move = message.payloadAs(InputMessage.InventoryMove.class);
        builder.setInvMove(
            core.network.proto.c2s.InventoryMoveAction.newBuilder()
                .setFromSlot(move.fromSlot())
                .setToSlot(move.toSlot())
                .build());
      }
      case INV_USE -> {
        InputMessage.InventoryUse use = message.payloadAs(InputMessage.InventoryUse.class);
        builder.setInvUse(
            core.network.proto.c2s.InventoryUseAction.newBuilder()
                .setSlotIndex(use.slotIndex())
                .build());
      }
      case TOGGLE_INVENTORY ->
          builder.setToggleInventory(
              core.network.proto.c2s.ToggleInventoryAction.newBuilder().build());
      case CUSTOM -> {
        InputMessage.Custom custom = message.payloadAs(InputMessage.Custom.class);
        builder.setCustom(
            core.network.proto.c2s.CustomAction.newBuilder()
                .setCommandId(custom.commandId())
                .setPayload(ByteString.copyFrom(custom.payload()))
                .setSchemaVersion(custom.schemaVersion())
                .build());
      }
    }

    return builder.build();
  }

  /**
   * Converts a protobuf input message into an {@link InputMessage}.
   *
   * @param proto the protobuf input message
   * @return the domain input message
   */
  public static InputMessage fromProto(core.network.proto.c2s.InputMessage proto) {
    short sequence = toShortExact(proto.getSequence(), "sequence");
    int sessionId = proto.getSessionId();
    int clientTick = proto.getClientTick();

    return switch (proto.getActionCase()) {
      case MOVE -> {
        core.network.proto.common.Vector2 direction = proto.getMove().getDirection();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.MOVE,
            new InputMessage.Move(Vector2.of(direction.getX(), direction.getY())));
      }
      case CAST_SKILL -> {
        core.network.proto.common.Point target = proto.getCastSkill().getTarget();
        boolean mainSkill = proto.getCastSkill().getMainSkill();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.CAST_SKILL,
            new InputMessage.CastSkill(fromProto(target), mainSkill));
      }
      case INTERACT -> {
        core.network.proto.common.Point target = proto.getInteract().getTarget();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INTERACT,
            new InputMessage.Interact(fromProto(target)));
      }
      case SKILL_CHANGE -> {
        boolean nextSkill = proto.getSkillChange().getNextSkill();
        boolean mainSkill = proto.getSkillChange().getMainSkill();
        InputMessage.Action action =
            nextSkill ? InputMessage.Action.NEXT_SKILL
              : InputMessage.Action.PREV_SKILL;
        yield new InputMessage(
            sessionId, clientTick, sequence, action, new InputMessage.SkillChange(nextSkill, mainSkill));
      }
      case TOGGLE_INVENTORY ->
          new InputMessage(
              sessionId,
              clientTick,
              sequence,
              InputMessage.Action.TOGGLE_INVENTORY,
              new InputMessage.ToggleInventory());
      case INV_DROP -> {
        int slotIndex = proto.getInvDrop().getSlotIndex();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INV_DROP,
            new InputMessage.InventoryDrop(slotIndex));
      }
      case INV_MOVE -> {
        int fromSlot = proto.getInvMove().getFromSlot();
        int toSlot = proto.getInvMove().getToSlot();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INV_MOVE,
            new InputMessage.InventoryMove(fromSlot, toSlot));
      }
      case INV_USE -> {
        int slotIndex = proto.getInvUse().getSlotIndex();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INV_USE,
            new InputMessage.InventoryUse(slotIndex));
      }
      case CUSTOM -> {
        CustomAction custom = proto.getCustom();
        int schemaVersion =
            custom.getSchemaVersion() <= 0
                ? DEFAULT_CUSTOM_SCHEMA_VERSION
                : custom.getSchemaVersion();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.CUSTOM,
            new InputMessage.Custom(
                custom.getCommandId(), custom.getPayload().toByteArray(), schemaVersion));
      }
      case ACTION_NOT_SET -> throw new IllegalArgumentException("InputMessage action is required.");
    };
  }

  /**
   * Converts a {@link DialogResponseMessage} into its protobuf representation.
   *
   * @param message the domain dialog response message to convert
   * @return the protobuf dialog response message
   */
  public static core.network.proto.c2s.DialogResponseMessage toProto(
      DialogResponseMessage message) {
    String callbackKey = message.callbackKey();
    if (callbackKey == null) {
      callbackKey = DIALOG_CLOSED_KEY;
    }
    var builder =
        core.network.proto.c2s.DialogResponseMessage.newBuilder()
            .setDialogId(message.dialogId())
            .setCallbackKey(callbackKey);
    DialogResponseMessage.Payload payload = message.payload();
    if (payload != null) {
      setDialogPayload(builder, payload);
    }
    return builder.build();
  }

  /**
   * Converts a protobuf dialog response message into a {@link DialogResponseMessage}.
   *
   * @param proto the protobuf dialog response message
   * @return the domain dialog response message
   */
  public static DialogResponseMessage fromProto(
      core.network.proto.c2s.DialogResponseMessage proto) {
    String callbackKey = proto.getCallbackKey();
    if (DIALOG_CLOSED_KEY.equals(callbackKey)) {
      callbackKey = null;
    }
    DialogResponseMessage.Payload payload = parseDialogPayload(proto);
    return new DialogResponseMessage(proto.getDialogId(), callbackKey, payload);
  }

  /**
   * Converts a {@link RegisterUdp} into its protobuf representation.
   *
   * @param message the domain UDP registration to convert
   * @return the protobuf UDP registration
   */
  public static core.network.proto.c2s.RegisterUdp toProto(RegisterUdp message) {
    return core.network.proto.c2s.RegisterUdp.newBuilder()
        .setSessionId(message.sessionId())
        .setSessionToken(ByteString.copyFrom(message.sessionToken()))
        .setClientId(message.clientId())
        .build();
  }

  /**
   * Converts a protobuf UDP registration into a {@link RegisterUdp}.
   *
   * @param proto the protobuf UDP registration
   * @return the domain UDP registration
   */
  public static RegisterUdp fromProto(core.network.proto.c2s.RegisterUdp proto) {
    return new RegisterUdp(
        proto.getSessionId(),
        proto.getSessionToken().toByteArray(),
        toShortExact(proto.getClientId(), "client_id"));
  }

  /**
   * Converts a {@link RequestEntitySpawn} into its protobuf representation.
   *
   * @param message the domain request to convert
   * @return the protobuf request
   */
  public static core.network.proto.c2s.RequestEntitySpawn toProto(RequestEntitySpawn message) {
    return core.network.proto.c2s.RequestEntitySpawn.newBuilder()
        .setEntityId(message.entityId())
        .build();
  }

  /**
   * Converts a protobuf entity spawn request into a {@link RequestEntitySpawn}.
   *
   * @param proto the protobuf entity spawn request
   * @return the domain entity spawn request
   */
  public static RequestEntitySpawn fromProto(core.network.proto.c2s.RequestEntitySpawn proto) {
    return new RequestEntitySpawn(proto.getEntityId());
  }

  /**
   * Converts a {@link SoundFinishedMessage} into its protobuf representation.
   *
   * @param message the domain sound finished message to convert
   * @return the protobuf sound finished message
   */
  public static core.network.proto.c2s.SoundFinishedMessage toProto(SoundFinishedMessage message) {
    return core.network.proto.c2s.SoundFinishedMessage.newBuilder()
        .setSoundInstanceId(message.soundInstanceId())
        .build();
  }

  /**
   * Converts a protobuf sound finished message into a {@link SoundFinishedMessage}.
   *
   * @param proto the protobuf sound finished message
   * @return the domain sound finished message
   */
  public static SoundFinishedMessage fromProto(core.network.proto.c2s.SoundFinishedMessage proto) {
    return new SoundFinishedMessage(proto.getSoundInstanceId());
  }

  /**
   * Converts a {@link ConnectAck} into its protobuf representation.
   *
   * @param message the domain connect ack message to convert
   * @return the protobuf connect ack message
   */
  public static core.network.proto.s2c.ConnectAck toProto(ConnectAck message) {
    return core.network.proto.s2c.ConnectAck.newBuilder()
        .setClientId(message.clientId())
        .setSessionId(message.sessionId())
        .setSessionToken(ByteString.copyFrom(message.sessionToken()))
        .build();
  }

  /**
   * Converts a protobuf connect ack message into a {@link ConnectAck}.
   *
   * @param proto the protobuf connect ack message
   * @return the domain connect ack message
   */
  public static ConnectAck fromProto(core.network.proto.s2c.ConnectAck proto) {
    return new ConnectAck(
        toShortExact(proto.getClientId(), "client_id"),
        proto.getSessionId(),
        proto.getSessionToken().toByteArray());
  }

  /**
   * Converts a {@link ConnectReject} into its protobuf representation.
   *
   * @param message the domain connect reject message to convert
   * @return the protobuf connect reject message
   */
  public static core.network.proto.s2c.ConnectReject toProto(ConnectReject message) {
    ConnectReject.Reason reason = ConnectReject.Reason.fromCode(message.reason());
    return core.network.proto.s2c.ConnectReject.newBuilder().setReason(toProto(reason)).build();
  }

  /**
   * Converts a protobuf connect reject message into a {@link ConnectReject}.
   *
   * @param proto the protobuf connect reject message
   * @return the domain connect reject message
   */
  public static ConnectReject fromProto(core.network.proto.s2c.ConnectReject proto) {
    return new ConnectReject(fromProto(proto.getReason()));
  }

  /**
   * Converts a {@link DialogShowMessage} into its protobuf representation.
   *
   * @param message the domain dialog show message to convert
   * @return the protobuf dialog show message
   */
  public static core.network.proto.s2c.DialogShowMessage toProto(DialogShowMessage message) {
    return DialogContextProtoConverter.toProto(message.context(), message.canBeClosed());
  }

  /**
   * Converts a protobuf dialog show message into a {@link DialogShowMessage}.
   *
   * @param proto the protobuf dialog show message
   * @return the domain dialog show message
   */
  public static DialogShowMessage fromProto(core.network.proto.s2c.DialogShowMessage proto) {
    return new DialogShowMessage(
        DialogContextProtoConverter.fromProto(proto), proto.getCanBeClosed());
  }

  /**
   * Converts a {@link DialogCloseMessage} into its protobuf representation.
   *
   * @param message the domain dialog close message to convert
   * @return the protobuf dialog close message
   */
  public static core.network.proto.s2c.DialogCloseMessage toProto(DialogCloseMessage message) {
    return core.network.proto.s2c.DialogCloseMessage.newBuilder()
        .setDialogId(message.dialogId())
        .build();
  }

  /**
   * Converts a protobuf dialog close message into a {@link DialogCloseMessage}.
   *
   * @param proto the protobuf dialog close message
   * @return the domain dialog close message
   */
  public static DialogCloseMessage fromProto(core.network.proto.s2c.DialogCloseMessage proto) {
    return new DialogCloseMessage(proto.getDialogId());
  }

  /**
   * Converts an {@link EntitySpawnEvent} into its protobuf representation.
   *
   * @param message the domain spawn event to convert
   * @return the protobuf spawn event
   */
  public static core.network.proto.s2c.EntitySpawnEvent toProto(EntitySpawnEvent message) {
    var builder =
        core.network.proto.s2c.EntitySpawnEvent.newBuilder()
            .setEntityId(message.entityId())
            .setPosition(toProto(message.positionComponent()))
            .setDrawInfo(toProto(message.drawInfo()))
            .setIsPersistent(message.isPersistent());

    PlayerComponent playerComponent = message.playerComponent();
    if (playerComponent != null) {
      builder.setPlayerInfo(toProto(playerComponent));
    }

    if (playerComponent != null || message.characterClassId() != 0) {
      builder.setCharacterClassId(message.characterClassId());
    }

    return builder.build();
  }

  /**
   * Converts a protobuf spawn event into an {@link EntitySpawnEvent}.
   *
   * @param proto the protobuf spawn event
   * @return the domain spawn event
   */
  public static EntitySpawnEvent fromProto(core.network.proto.s2c.EntitySpawnEvent proto) {
    PositionComponent position = fromProto(proto.getPosition());
    DrawInfoData drawInfo = fromProto(proto.getDrawInfo());
    PlayerComponent playerComponent =
        proto.hasPlayerInfo() ? fromProto(proto.getPlayerInfo()) : null;
    byte characterClassId =
        proto.hasCharacterClassId()
            ? toByteExact(proto.getCharacterClassId(), "character_class_id")
            : 0;

    return new EntitySpawnEvent(
        proto.getEntityId(),
        position,
        drawInfo,
        proto.getIsPersistent(),
        playerComponent,
        characterClassId);
  }

  /**
   * Converts an {@link EntitySpawnBatch} into its protobuf representation.
   *
   * @param message the domain spawn batch to convert
   * @return the protobuf spawn batch
   */
  public static core.network.proto.s2c.EntitySpawnBatch toProto(EntitySpawnBatch message) {
    var builder = core.network.proto.s2c.EntitySpawnBatch.newBuilder();
    for (EntitySpawnEvent event : message.entities()) {
      builder.addEntities(toProto(event));
    }
    return builder.build();
  }

  /**
   * Converts a protobuf spawn batch into an {@link EntitySpawnBatch}.
   *
   * @param proto the protobuf spawn batch
   * @return the domain spawn batch
   */
  public static EntitySpawnBatch fromProto(core.network.proto.s2c.EntitySpawnBatch proto) {
    List<EntitySpawnEvent> events = new ArrayList<>();
    for (core.network.proto.s2c.EntitySpawnEvent event : proto.getEntitiesList()) {
      events.add(fromProto(event));
    }
    return new EntitySpawnBatch(events);
  }

  /**
   * Converts an {@link EntityDespawnEvent} into its protobuf representation.
   *
   * @param message the domain despawn event to convert
   * @return the protobuf despawn event
   */
  public static core.network.proto.s2c.EntityDespawnEvent toProto(EntityDespawnEvent message) {
    return core.network.proto.s2c.EntityDespawnEvent.newBuilder()
        .setEntityId(message.entityId())
        .setReason(message.reason())
        .build();
  }

  /**
   * Converts a protobuf despawn event into an {@link EntityDespawnEvent}.
   *
   * @param proto the protobuf despawn event
   * @return the domain despawn event
   */
  public static EntityDespawnEvent fromProto(core.network.proto.s2c.EntityDespawnEvent proto) {
    return new EntityDespawnEvent(proto.getEntityId(), proto.getReason());
  }

  /**
   * Converts an {@link EntityState} into its protobuf representation.
   *
   * @param message the domain entity state to convert
   * @return the protobuf entity state
   */
  public static core.network.proto.s2c.EntityState toProto(EntityState message) {
    var builder = core.network.proto.s2c.EntityState.newBuilder().setEntityId(message.entityId());

    message.entityName().ifPresent(builder::setEntityName);
    boolean hasPosition = message.position().isPresent();
    boolean hasViewDirection = message.viewDirection().isPresent();
    boolean hasRotation = message.rotation().isPresent();
    boolean hasScale = message.scale().isPresent();
    if (hasPosition) {
      // TODO: Support partial position updates once snapshot diffing is introduced.
      Direction viewDirection =
          message.viewDirection().map(ProtoConverter::parseDirection).orElse(Direction.NONE);
      float rotation = message.rotation().orElse(0.0f);
      Vector2 scale = message.scale().orElse(Vector2.ONE);

      core.network.proto.common.PositionInfo positionInfo =
          core.network.proto.common.PositionInfo.newBuilder()
              .setPosition(toProto(message.position().orElseThrow()))
              .setViewDirection(toProto(viewDirection))
              .setRotation(rotation)
              .setScale(toProto(scale))
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
                  slot.setItem(toProtoItem(item));
                }
                builder.addInventory(slot);
              }
            });

    return builder.build();
  }

  /**
   * Converts a protobuf entity state into an {@link EntityState}.
   *
   * @param proto the protobuf entity state
   * @return the domain entity state
   */
  public static EntityState fromProto(core.network.proto.s2c.EntityState proto) {
    EntityState.Builder builder = EntityState.builder().entityId(proto.getEntityId());

    if (proto.hasEntityName()) {
      builder.entityName(proto.getEntityName());
    }
    if (proto.hasPosition()) {
      core.network.proto.common.PositionInfo positionInfo = proto.getPosition();
      builder.position(fromProto(positionInfo.getPosition()));
      core.network.proto.common.Direction viewDirection = positionInfo.getViewDirection();
      if (viewDirection != core.network.proto.common.Direction.DIRECTION_UNSPECIFIED
          && viewDirection != core.network.proto.common.Direction.UNRECOGNIZED) {
        builder.viewDirection(fromProto(viewDirection));
      }
      builder.rotation(positionInfo.getRotation());
      if (positionInfo.hasScale()) {
        builder.scale(fromProto(positionInfo.getScale()));
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
          items[index] = fromProtoItem(slot.getItem());
        }
      }
      builder.inventory(items);
    }

    return builder.build();
  }

  /**
   * Converts a {@link SnapshotMessage} into its protobuf representation.
   *
   * @param message the domain snapshot message to convert
   * @return the protobuf snapshot message
   */
  public static core.network.proto.s2c.SnapshotMessage toProto(SnapshotMessage message) {
    var builder =
        core.network.proto.s2c.SnapshotMessage.newBuilder().setServerTick(message.serverTick());
    for (EntityState state : message.entities()) {
      builder.addEntities(toProto(state));
    }
    return builder.build();
  }

  /**
   * Converts a protobuf snapshot message into a {@link SnapshotMessage}.
   *
   * @param proto the protobuf snapshot message
   * @return the domain snapshot message
   */
  public static SnapshotMessage fromProto(core.network.proto.s2c.SnapshotMessage proto) {
    List<EntityState> entities = new ArrayList<>();
    for (core.network.proto.s2c.EntityState state : proto.getEntitiesList()) {
      entities.add(fromProto(state));
    }
    return new SnapshotMessage(proto.getServerTick(), entities);
  }

  /**
   * Converts a {@link GameOverEvent} into its protobuf representation.
   *
   * @param message the domain game over event to convert
   * @return the protobuf game over event
   */
  public static core.network.proto.s2c.GameOverEvent toProto(GameOverEvent message) {
    return core.network.proto.s2c.GameOverEvent.newBuilder().setReason(message.reason()).build();
  }

  /**
   * Converts a protobuf game over event into a {@link GameOverEvent}.
   *
   * @param proto the protobuf game over event
   * @return the domain game over event
   */
  public static GameOverEvent fromProto(core.network.proto.s2c.GameOverEvent proto) {
    return new GameOverEvent(proto.getReason());
  }

  /**
   * Converts a {@link LevelChangeEvent} into its protobuf representation.
   *
   * @param message the domain level change event to convert
   * @return the protobuf level change event
   */
  public static core.network.proto.s2c.LevelChangeEvent toProto(LevelChangeEvent message) {
    return core.network.proto.s2c.LevelChangeEvent.newBuilder()
        .setLevelName(message.levelName())
        .setLevelData(message.levelData())
        .build();
  }

  /**
   * Converts a protobuf level change event into a {@link LevelChangeEvent}.
   *
   * @param proto the protobuf level change event
   * @return the domain level change event
   */
  public static LevelChangeEvent fromProto(core.network.proto.s2c.LevelChangeEvent proto) {
    return new LevelChangeEvent(proto.getLevelName(), proto.getLevelData());
  }

  /**
   * Converts a {@link RegisterAck} into its protobuf representation.
   *
   * @param message the domain register ack to convert
   * @return the protobuf register ack
   */
  public static core.network.proto.s2c.RegisterAck toProto(RegisterAck message) {
    return core.network.proto.s2c.RegisterAck.newBuilder().setOk(message.ok()).build();
  }

  /**
   * Converts a protobuf register ack into a {@link RegisterAck}.
   *
   * @param proto the protobuf register ack
   * @return the domain register ack
   */
  public static RegisterAck fromProto(core.network.proto.s2c.RegisterAck proto) {
    return new RegisterAck(proto.getOk());
  }

  /**
   * Converts a {@link SoundPlayMessage} into its protobuf representation.
   *
   * @param message the domain sound play message to convert
   * @return the protobuf sound play message
   */
  public static core.network.proto.s2c.SoundPlayMessage toProto(SoundPlayMessage message) {
    return core.network.proto.s2c.SoundPlayMessage.newBuilder()
      .setEntityId(message.entityId())
      .setSpec(toProto(message.soundSpec()))
      .build();
  }

  /**
   * Converts a protobuf sound play message into a {@link SoundPlayMessage}.
   *
   * @param proto the protobuf sound play message
   * @return the domain sound play message
   */
  public static SoundPlayMessage fromProto(core.network.proto.s2c.SoundPlayMessage proto) {
    return new SoundPlayMessage(proto.getEntityId(), fromProto(proto.getSpec()));
  }

  /**
   * Converts a {@link SoundStopMessage} into its protobuf representation.
   *
   * @param message the domain sound stop message to convert
   * @return the protobuf sound stop message
   */
  public static core.network.proto.s2c.SoundStopMessage toProto(SoundStopMessage message) {
    return core.network.proto.s2c.SoundStopMessage.newBuilder()
        .setSoundInstanceId(message.soundInstanceId())
        .build();
  }

  /**
   * Converts a protobuf sound stop message into a {@link SoundStopMessage}.
   *
   * @param proto the protobuf sound stop message
   * @return the domain sound stop message
   */
  public static SoundStopMessage fromProto(core.network.proto.s2c.SoundStopMessage proto) {
    return new SoundStopMessage(proto.getSoundInstanceId());
  }

  private static core.network.proto.s2c.ConnectReject.RejectReason toProto(
      ConnectReject.Reason reason) {
    return switch (reason) {
      case INVALID_NAME ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INVALID_NAME;
      case INCOMPATIBLE_VERSION ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INCOMPATIBLE_VERSION;
      case NO_SESSION_FOUND ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_NO_SESSION_FOUND;
      case INVALID_SESSION_TOKEN ->
          core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INVALID_SESSION_TOKEN;
      case OTHER -> core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_OTHER;
    };
  }

  private static ConnectReject.Reason fromProto(
      core.network.proto.s2c.ConnectReject.RejectReason reason) {
    return switch (reason) {
      case REJECT_REASON_INVALID_NAME -> ConnectReject.Reason.INVALID_NAME;
      case REJECT_REASON_INCOMPATIBLE_VERSION -> ConnectReject.Reason.INCOMPATIBLE_VERSION;
      case REJECT_REASON_NO_SESSION_FOUND -> ConnectReject.Reason.NO_SESSION_FOUND;
      case REJECT_REASON_INVALID_SESSION_TOKEN -> ConnectReject.Reason.INVALID_SESSION_TOKEN;
      case REJECT_REASON_OTHER, REJECT_REASON_UNSPECIFIED, UNRECOGNIZED ->
          ConnectReject.Reason.OTHER;
    };
  }

  private static core.network.proto.s2c.DrawInfo toProto(DrawInfoData drawInfo) {
    if (drawInfo == null) {
      throw new IllegalArgumentException("DrawInfoData is required.");
    }
    String texturePath = drawInfo.texturePath();
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException("DrawInfoData.texturePath is required.");
    }

    core.network.proto.s2c.DrawInfo.Builder builder =
        core.network.proto.s2c.DrawInfo.newBuilder().setTexturePath(texturePath);
    if (drawInfo.scaleX() != null) {
      builder.setScaleX(drawInfo.scaleX());
    }
    if (drawInfo.scaleY() != null) {
      builder.setScaleY(drawInfo.scaleY());
    }

    String animationName = drawInfo.animationName();
    if (animationName != null && !animationName.isEmpty()) {
      int frameIndex = drawInfo.currentFrame() != null ? Math.max(0, drawInfo.currentFrame()) : 0;
      builder.setCurrentAnimation(
          core.network.proto.s2c.AnimationInfo.newBuilder()
              .setAnimationName(animationName)
              .setCurrentFrame(frameIndex)
              .build());
    }

    return builder.build();
  }

  private static DrawInfoData fromProto(core.network.proto.s2c.DrawInfo proto) {
    if (proto.getTexturePath().isEmpty()) {
      throw new IllegalArgumentException("DrawInfo.texture_path is required.");
    }

    Float scaleX = proto.hasScaleX() ? proto.getScaleX() : null;
    Float scaleY = proto.hasScaleY() ? proto.getScaleY() : null;
    String animationName = null;
    Integer currentFrame = null;

    if (proto.hasCurrentAnimation()) {
      core.network.proto.s2c.AnimationInfo animationInfo = proto.getCurrentAnimation();
      if (!animationInfo.getAnimationName().isEmpty()) {
        animationName = animationInfo.getAnimationName();
        currentFrame = animationInfo.getCurrentFrame();
      }
    }

    return new DrawInfoData(proto.getTexturePath(), scaleX, scaleY, animationName, currentFrame);
  }

  private static core.network.proto.s2c.PlayerInfo toProto(PlayerComponent component) {
    return core.network.proto.s2c.PlayerInfo.newBuilder()
        .setPlayerName(component.playerName())
        .setIsLocalPlayer(component.isLocal())
        .build();
  }

  private static PlayerComponent fromProto(core.network.proto.s2c.PlayerInfo proto) {
    return new PlayerComponent(proto.getIsLocalPlayer(), proto.getPlayerName());
  }

  private static core.network.proto.common.Item toProtoItem(Item item) {
    String itemType = ItemRegistry.idFor(item);
    core.network.proto.common.Item.Builder builder =
        core.network.proto.common.Item.newBuilder()
            .setItemType(itemType)
            .setStackSize(item.stackSize())
            .setMaxStackSize(item.maxStackSize());

    Map<String, String> itemData = item.itemData();
    if (itemData != null && !itemData.isEmpty()) {
      builder.putAllItemData(itemData);
    }

    return builder.build();
  }

  private static Item fromProtoItem(core.network.proto.common.Item proto) {
    String itemType = proto.getItemType();
    Class<? extends Item> itemClass =
        ItemRegistry.lookup(itemType)
            .orElseThrow(() -> new IllegalArgumentException("Unknown item type: " + itemType));

    Map<String, String> itemData = proto.getItemDataMap();
    try {
      Optional<Item> itemFromData =
          itemData.isEmpty() ? Optional.empty() : ItemRegistry.create(itemType, itemData);
      if (!itemData.isEmpty() && itemFromData.isEmpty()) {
        throw new IllegalArgumentException(
            "Item data provided but no factory registered for item type: " + itemType);
      }
      Item item;
      if (itemFromData.isPresent()) {
        item = itemFromData.get();
      } else {
        item = itemClass.getDeclaredConstructor().newInstance();
      }
      int maxStackSize = proto.getMaxStackSize();
      if (maxStackSize > 0) {
        item.maxStackSize(maxStackSize);
      }
      item.stackSize(proto.getStackSize());
      return item;
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("Failed to instantiate item type: " + itemType, e);
    }
  }

  private static Direction parseDirection(String direction) {
    if (direction == null) {
      return Direction.NONE;
    }
    try {
      return Direction.valueOf(direction);
    } catch (IllegalArgumentException e) {
      return Direction.NONE;
    }
  }

  private static void setDialogPayload(
      core.network.proto.c2s.DialogResponseMessage.Builder builder,
      DialogResponseMessage.Payload payload) {
    switch (payload) {
      case DialogResponseMessage.StringValue(String value5) -> builder.setStringValue(value5);
      case DialogResponseMessage.IntValue(int value4) -> builder.setIntValue(value4);
      case DialogResponseMessage.LongValue(long value3) -> builder.setLongValue(value3);
      case DialogResponseMessage.FloatValue(float value2) -> builder.setFloatValue(value2);
      case DialogResponseMessage.DoubleValue(double value1) -> builder.setDoubleValue(value1);
      case DialogResponseMessage.BoolValue(boolean value1) -> builder.setBoolValue(value1);
      case DialogResponseMessage.StringList(String[] stringArray) ->
          builder.setStringList(StringList.newBuilder()
                  .addAllValues(Arrays.asList(stringArray)));
      case DialogResponseMessage.IntList(int[] intArray) -> {
        IntList.Builder listBuilder = IntList.newBuilder();
        for (int value : intArray) {
          listBuilder.addValues(value);
        }
        builder.setIntList(listBuilder);
      }
      default ->
          throw new IllegalArgumentException(
              "Unsupported dialog response payload type: " + payload.getClass().getName());
    }
  }

  private static DialogResponseMessage.Payload parseDialogPayload(
      core.network.proto.c2s.DialogResponseMessage proto) {
    return switch (proto.getPayloadCase()) {
      case STRING_VALUE -> new DialogResponseMessage.StringValue(proto.getStringValue());
      case INT_VALUE -> new DialogResponseMessage.IntValue(proto.getIntValue());
      case LONG_VALUE -> new DialogResponseMessage.LongValue(proto.getLongValue());
      case FLOAT_VALUE -> new DialogResponseMessage.FloatValue(proto.getFloatValue());
      case DOUBLE_VALUE -> new DialogResponseMessage.DoubleValue(proto.getDoubleValue());
      case BOOL_VALUE -> new DialogResponseMessage.BoolValue(proto.getBoolValue());
      case STRING_LIST ->
          new DialogResponseMessage.StringList(
              proto.getStringList().getValuesList().toArray(new String[0]));
      case INT_LIST ->
          new DialogResponseMessage.IntList(
              proto.getIntList().getValuesList().stream().mapToInt(i -> i).toArray());
      case PAYLOAD_NOT_SET -> null;
    };
  }

  private static byte toByteExact(int value, String fieldName) {
    if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
      throw new IllegalArgumentException(fieldName + " out of range for byte: " + value);
    }
    return (byte) value;
  }

  private static short toShortExact(int value, String fieldName) {
    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
      throw new IllegalArgumentException(fieldName + " out of range for short: " + value);
    }
    return (short) value;
  }
}
