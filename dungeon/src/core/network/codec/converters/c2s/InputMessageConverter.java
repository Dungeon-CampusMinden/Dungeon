package core.network.codec.converters.c2s;

import com.google.protobuf.ByteString;
import com.google.protobuf.Parser;
import core.network.codec.CommonProtoConverters;
import core.network.codec.MessageConverter;
import core.network.messages.c2s.InputMessage;
import core.utils.Vector2;

/** Converter for client-to-server input messages. */
public final class InputMessageConverter
    implements MessageConverter<InputMessage, core.network.proto.c2s.InputMessage> {
  private static final int DEFAULT_CUSTOM_SCHEMA_VERSION = 1;
  private static final byte WIRE_TYPE_ID = 2;

  @Override
  public core.network.proto.c2s.InputMessage toProto(InputMessage message) {
    core.network.proto.c2s.InputMessage.Builder builder =
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
                .setTarget(CommonProtoConverters.toProto(castSkill.target()))
                .build());
      }
      case INTERACT -> {
        InputMessage.Interact interact = message.payloadAs(InputMessage.Interact.class);
        builder.setInteract(
            core.network.proto.c2s.InteractAction.newBuilder()
                .setTarget(CommonProtoConverters.toProto(interact.target()))
                .build());
      }
      case NEXT_SKILL, PREV_SKILL -> {
        InputMessage.SkillChange change = message.payloadAs(InputMessage.SkillChange.class);
        builder.setSkillChange(
            core.network.proto.c2s.SkillChangeAction.newBuilder()
                .setNextSkill(change.nextSkill())
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

  @Override
  public InputMessage fromProto(core.network.proto.c2s.InputMessage proto) {
    short sequence = CommonProtoConverters.toShortExact(proto.getSequence(), "sequence");
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
            new InputMessage.CastSkill(CommonProtoConverters.fromProto(target), mainSkill));
      }
      case INTERACT -> {
        core.network.proto.common.Point target = proto.getInteract().getTarget();
        yield new InputMessage(
            sessionId,
            clientTick,
            sequence,
            InputMessage.Action.INTERACT,
            new InputMessage.Interact(CommonProtoConverters.fromProto(target)));
      }
      case SKILL_CHANGE -> {
        boolean nextSkill = proto.getSkillChange().getNextSkill();
        boolean mainSkill = proto.getSkillChange().getMainSkill();
        InputMessage.Action action =
            nextSkill ? InputMessage.Action.NEXT_SKILL : InputMessage.Action.PREV_SKILL;
        yield new InputMessage(
            sessionId, clientTick, sequence, action, new InputMessage.SkillChange(nextSkill,  mainSkill));
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
        core.network.proto.c2s.CustomAction custom = proto.getCustom();
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

  @Override
  public Class<InputMessage> domainType() {
    return InputMessage.class;
  }

  @Override
  public Class<core.network.proto.c2s.InputMessage> protoType() {
    return core.network.proto.c2s.InputMessage.class;
  }

  @Override
  public Parser<core.network.proto.c2s.InputMessage> parser() {
    return core.network.proto.c2s.InputMessage.parser();
  }

  @Override
  public byte wireTypeId() {
    return WIRE_TYPE_ID;
  }
}
