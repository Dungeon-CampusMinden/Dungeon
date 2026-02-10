package core.network.codec;

import com.google.protobuf.ByteString;
import core.network.messages.c2s.ConnectRequest;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.InputMessage;
import core.network.messages.c2s.RegisterUdp;
import core.network.messages.c2s.RequestEntitySpawn;
import core.network.messages.c2s.SoundFinishedMessage;
import core.sound.SoundSpec;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Converts between protobuf messages and domain objects for common network types.
 */
public final class ProtoConverter {

  private static final String DIALOG_CLOSED_KEY = "CLOSED";

  private ProtoConverter() {}

  /**
   * Converts a {@link Point} into its protobuf representation.
   *
   * @param point the domain point to convert
   * @return the protobuf point message
   */
  public static core.network.proto.common.Point toProto(Point point) {
    return core.network.proto.common.Point.newBuilder()
        .setX(point.x())
        .setY(point.y())
        .build();
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
    return core.network.proto.common.Vector2.newBuilder()
        .setX(vector.x())
        .setY(vector.y())
        .build();
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
   * <p>The deprecated {@link InputMessage.Action#TOGGLE_INVENTORY} is not supported and will
   * result in an exception.
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
        Point point = requirePoint(message);
        builder.setMove(
            core.network.proto.c2s.MoveAction.newBuilder()
                .setDirection(
                    core.network.proto.common.Vector2.newBuilder()
                        .setX(point.x())
                        .setY(point.y())
                        .build())
                .build());
      }
      case CAST_SKILL -> {
        Point point = requirePoint(message);
        builder.setCastSkill(
            core.network.proto.c2s.CastSkillAction.newBuilder().setTarget(toProto(point)).build());
      }
      case INTERACT -> {
        Point point = requirePoint(message);
        builder.setInteract(
            core.network.proto.c2s.InteractAction.newBuilder().setTarget(toProto(point)).build());
      }
      case NEXT_SKILL ->
          builder.setSkillChange(
              core.network.proto.c2s.SkillChangeAction.newBuilder().setNextSkill(true).build());
      case PREV_SKILL ->
          builder.setSkillChange(
              core.network.proto.c2s.SkillChangeAction.newBuilder().setNextSkill(false).build());
      case INV_DROP -> {
        Point point = requirePoint(message);
        builder.setInvDrop(
            core.network.proto.c2s.InventoryDropAction.newBuilder()
                .setSlotIndex((int) point.x())
                .build());
      }
      case INV_MOVE -> {
        Point point = requirePoint(message);
        builder.setInvMove(
            core.network.proto.c2s.InventoryMoveAction.newBuilder()
                .setFromSlot((int) point.x())
                .setToSlot((int) point.y())
                .build());
      }
      case INV_USE -> {
        Point point = requirePoint(message);
        builder.setInvUse(
            core.network.proto.c2s.InventoryUseAction.newBuilder()
                .setSlotIndex((int) point.x())
                .build());
      }
      case TOGGLE_INVENTORY ->
          throw new IllegalArgumentException("Toggle inventory action is deprecated.");
    }

    return builder.build();
  }

  /**
   * Converts a protobuf input message into an {@link InputMessage}.
   *
   * <p>The deprecated toggle inventory action is not supported and will result in an exception.
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
            new Point(direction.getX(), direction.getY()));
      }
      case CAST_SKILL -> {
        core.network.proto.common.Point target = proto.getCastSkill().getTarget();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.CAST_SKILL,
            fromProto(target));
      }
      case INTERACT -> {
        core.network.proto.common.Point target = proto.getInteract().getTarget();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INTERACT,
            fromProto(target));
      }
      case SKILL_CHANGE -> {
        boolean nextSkill = proto.getSkillChange().getNextSkill();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            nextSkill ? InputMessage.Action.NEXT_SKILL : InputMessage.Action.PREV_SKILL,
            null);
      }
      case INV_DROP -> {
        int slotIndex = proto.getInvDrop().getSlotIndex();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INV_DROP,
            new Point(slotIndex, 0));
      }
      case INV_MOVE -> {
        int fromSlot = proto.getInvMove().getFromSlot();
        int toSlot = proto.getInvMove().getToSlot();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INV_MOVE,
            new Point(fromSlot, toSlot));
      }
      case INV_USE -> {
        int slotIndex = proto.getInvUse().getSlotIndex();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INV_USE,
            new Point(slotIndex, 0));
      }
      case ACTION_NOT_SET ->
          throw new IllegalArgumentException("InputMessage action is required.");
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
    Serializable data = message.data();
    if (data != null) {
      builder.setCustomData(ByteString.copyFrom(serializeDialogData(data)));
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
    Serializable data = null;
    if (proto.hasCustomData()) {
      data = deserializeDialogData(proto.getCustomData());
    }
    return new DialogResponseMessage(proto.getDialogId(), callbackKey, data);
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
  public static core.network.proto.c2s.SoundFinishedMessage toProto(
      SoundFinishedMessage message) {
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
  public static SoundFinishedMessage fromProto(
      core.network.proto.c2s.SoundFinishedMessage proto) {
    return new SoundFinishedMessage(proto.getSoundInstanceId());
  }

  private static Point requirePoint(InputMessage message) {
    Point point = message.point();
    if (point == null) {
      throw new IllegalArgumentException(
          "InputMessage point is required for action " + message.action());
    }
    return point;
  }

  private static byte[] serializeDialogData(Serializable data) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(data);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to serialize dialog data.", e);
    }
  }

  private static Serializable deserializeDialogData(ByteString data) {
    byte[] bytes = data.toByteArray();
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      Object result = ois.readObject();
      if (result instanceof Serializable serializable) {
        return serializable;
      }
      throw new IllegalArgumentException("Dialog data is not serializable.");
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalArgumentException("Failed to deserialize dialog data.", e);
    }
  }

  private static short toShortExact(int value, String fieldName) {
    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
      throw new IllegalArgumentException(
          fieldName + " out of range for short: " + value);
    }
    return (short) value;
  }
}
