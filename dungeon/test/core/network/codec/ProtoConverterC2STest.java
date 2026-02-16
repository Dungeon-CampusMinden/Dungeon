package core.network.codec;

import core.network.messages.c2s.*;
import core.utils.Point;
import core.utils.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for {@link ProtoConverter} c2s message conversions. */
public class ProtoConverterC2STest {

  private static final float DELTA = 1e-6f;

  /** Verifies connect request conversion with session data. */
  @Test
  public void testConnectRequestRoundTripWithSession() {
    byte[] token = new byte[] {1, 2, 3};
    ConnectRequest request = new ConnectRequest((short) 3, "Alice", 42, token);

    core.network.proto.c2s.ConnectRequest proto = ProtoConverter.toProto(request);
    assertEquals(3, proto.getProtocolVersion());
    assertEquals("Alice", proto.getPlayerName());
    assertTrue(proto.hasSessionId());
    assertEquals(42, proto.getSessionId());
    assertTrue(proto.hasSessionToken());
    assertArrayEquals(token, proto.getSessionToken().toByteArray());

    ConnectRequest roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(request.protocolVersion(), roundTrip.protocolVersion());
    assertEquals(request.playerName(), roundTrip.playerName());
    assertEquals(request.sessionId(), roundTrip.sessionId());
    assertArrayEquals(request.sessionToken(), roundTrip.sessionToken());
  }

  /** Verifies connect request conversion without session data. */
  @Test
  public void testConnectRequestRoundTripWithoutSession() {
    ConnectRequest request = new ConnectRequest((short) 1, "Bob");

    core.network.proto.c2s.ConnectRequest proto = ProtoConverter.toProto(request);
    assertEquals(1, proto.getProtocolVersion());
    assertEquals("Bob", proto.getPlayerName());
    assertFalse(proto.hasSessionId());
    assertFalse(proto.hasSessionToken());

    ConnectRequest roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(0, roundTrip.sessionId());
    assertArrayEquals(new byte[0], roundTrip.sessionToken());
  }

  /** Verifies move action conversion. */
  @Test
  public void testInputMessageMoveRoundTrip() {
    InputMessage message =
        new InputMessage(
            5,
            10,
            (short) 7,
            InputMessage.Action.MOVE,
            new InputMessage.Move(Vector2.of(1.5f, -2.0f)));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(core.network.proto.c2s.InputMessage.ActionCase.MOVE, proto.getActionCase());
    assertEquals(1.5f, proto.getMove().getDirection().getX(), DELTA);
    assertEquals(-2.0f, proto.getMove().getDirection().getY(), DELTA);

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.MOVE, roundTrip.action());
    InputMessage.Move move = roundTrip.payloadAs(InputMessage.Move.class);
    assertEquals(1.5f, move.direction().x(), DELTA);
    assertEquals(-2.0f, move.direction().y(), DELTA);
  }

  /** Verifies cast skill action conversion. */
  @Test
  public void testInputMessageCastSkillRoundTrip() {
    InputMessage message =
        new InputMessage(
            1,
            2,
            (short) 3,
            InputMessage.Action.CAST_SKILL,
            new InputMessage.CastSkill(new Point(3.0f, 4.0f), true));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(core.network.proto.c2s.InputMessage.ActionCase.CAST_SKILL, proto.getActionCase());
    assertEquals(3.0f, proto.getCastSkill().getTarget().getX(), DELTA);
    assertEquals(4.0f, proto.getCastSkill().getTarget().getY(), DELTA);

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.CAST_SKILL, roundTrip.action());
    InputMessage.CastSkill castSkill = roundTrip.payloadAs(InputMessage.CastSkill.class);
    assertEquals(3.0f, castSkill.target().x(), DELTA);
    assertEquals(4.0f, castSkill.target().y(), DELTA);
  }

  /** Verifies interact action conversion. */
  @Test
  public void testInputMessageInteractRoundTrip() {
    InputMessage message =
        new InputMessage(
            9,
            8,
            (short) 7,
            InputMessage.Action.INTERACT,
            new InputMessage.Interact(new Point(-1.0f, 2.5f)));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(core.network.proto.c2s.InputMessage.ActionCase.INTERACT, proto.getActionCase());
    assertEquals(-1.0f, proto.getInteract().getTarget().getX(), DELTA);
    assertEquals(2.5f, proto.getInteract().getTarget().getY(), DELTA);

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.INTERACT, roundTrip.action());
    InputMessage.Interact interact = roundTrip.payloadAs(InputMessage.Interact.class);
    assertEquals(-1.0f, interact.target().x(), DELTA);
    assertEquals(2.5f, interact.target().y(), DELTA);
  }

  /** Verifies next skill action conversion. */
  @Test
  public void testInputMessageNextSkillRoundTrip() {
    InputMessage message =
        new InputMessage(
            1, 1, (short) 1, InputMessage.Action.NEXT_SKILL, new InputMessage.SkillChange(true, true));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(
        core.network.proto.c2s.InputMessage.ActionCase.SKILL_CHANGE, proto.getActionCase());
    assertTrue(proto.getSkillChange().getNextSkill());

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.NEXT_SKILL, roundTrip.action());
    InputMessage.SkillChange change = roundTrip.payloadAs(InputMessage.SkillChange.class);
    assertTrue(change.nextSkill());
  }

  /** Verifies previous skill action conversion. */
  @Test
  public void testInputMessagePrevSkillRoundTrip() {
    InputMessage message =
        new InputMessage(
            2, 3, (short) 4, InputMessage.Action.PREV_SKILL, new InputMessage.SkillChange(false, true));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(
        core.network.proto.c2s.InputMessage.ActionCase.SKILL_CHANGE, proto.getActionCase());
    assertFalse(proto.getSkillChange().getNextSkill());

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.PREV_SKILL, roundTrip.action());
    InputMessage.SkillChange change = roundTrip.payloadAs(InputMessage.SkillChange.class);
    assertFalse(change.nextSkill());
  }

  /** Verifies inventory drop action conversion. */
  @Test
  public void testInputMessageInventoryDropRoundTrip() {
    InputMessage message =
        new InputMessage(
            3, 4, (short) 5, InputMessage.Action.INV_DROP, new InputMessage.InventoryDrop(2));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(core.network.proto.c2s.InputMessage.ActionCase.INV_DROP, proto.getActionCase());
    assertEquals(2, proto.getInvDrop().getSlotIndex());

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.INV_DROP, roundTrip.action());
    InputMessage.InventoryDrop drop = roundTrip.payloadAs(InputMessage.InventoryDrop.class);
    assertEquals(2, drop.slotIndex());
  }

  /** Verifies inventory move action conversion. */
  @Test
  public void testInputMessageInventoryMoveRoundTrip() {
    InputMessage message =
        new InputMessage(
            3, 4, (short) 5, InputMessage.Action.INV_MOVE, new InputMessage.InventoryMove(-1, 4));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(core.network.proto.c2s.InputMessage.ActionCase.INV_MOVE, proto.getActionCase());
    assertEquals(-1, proto.getInvMove().getFromSlot());
    assertEquals(4, proto.getInvMove().getToSlot());

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.INV_MOVE, roundTrip.action());
    InputMessage.InventoryMove move = roundTrip.payloadAs(InputMessage.InventoryMove.class);
    assertEquals(-1, move.fromSlot());
    assertEquals(4, move.toSlot());
  }

  /** Verifies inventory use action conversion. */
  @Test
  public void testInputMessageInventoryUseRoundTrip() {
    InputMessage message =
        new InputMessage(
            3, 4, (short) 5, InputMessage.Action.INV_USE, new InputMessage.InventoryUse(7));

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(core.network.proto.c2s.InputMessage.ActionCase.INV_USE, proto.getActionCase());
    assertEquals(7, proto.getInvUse().getSlotIndex());

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.INV_USE, roundTrip.action());
    InputMessage.InventoryUse use = roundTrip.payloadAs(InputMessage.InventoryUse.class);
    assertEquals(7, use.slotIndex());
  }

  /** Verifies toggle inventory action conversion. */
  @Test
  public void testInputMessageToggleInventoryRoundTrip() {
    InputMessage message =
        new InputMessage(
            1,
            1,
            (short) 1,
            InputMessage.Action.TOGGLE_INVENTORY,
            new InputMessage.ToggleInventory());

    core.network.proto.c2s.InputMessage proto = ProtoConverter.toProto(message);
    assertEquals(
        core.network.proto.c2s.InputMessage.ActionCase.TOGGLE_INVENTORY, proto.getActionCase());

    InputMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(InputMessage.Action.TOGGLE_INVENTORY, roundTrip.action());
    roundTrip.payloadAs(InputMessage.ToggleInventory.class);
  }

  /** Ensures input message actions are required. */
  @Test
  public void testInputMessageActionRequired() {
    core.network.proto.c2s.InputMessage proto =
        core.network.proto.c2s.InputMessage.newBuilder()
            .setSessionId(1)
            .setClientTick(2)
            .setSequence(3)
            .build();

    assertThrows(IllegalArgumentException.class, () -> ProtoConverter.fromProto(proto));
  }

  /** Verifies dialog response conversion with custom data and closed callback. */
  @Test
  public void testDialogResponseClosedRoundTrip() {
    DialogResponseMessage message = new DialogResponseMessage("dialog-1", null, "payload");

    core.network.proto.c2s.DialogResponseMessage proto = ProtoConverter.toProto(message);
    assertEquals("dialog-1", proto.getDialogId());
    assertEquals("CLOSED", proto.getCallbackKey());
    assertEquals("payload", proto.getStringValue());

    DialogResponseMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals("dialog-1", roundTrip.dialogId());
    assertNull(roundTrip.callbackKey());
    assertEquals("payload", roundTrip.data());
  }

  /** Verifies dialog response conversion without custom data. */
  @Test
  public void testDialogResponseRoundTrip() {
    DialogResponseMessage message = new DialogResponseMessage("dialog-2", "onConfirm", null);

    core.network.proto.c2s.DialogResponseMessage proto = ProtoConverter.toProto(message);
    assertEquals("dialog-2", proto.getDialogId());
    assertEquals("onConfirm", proto.getCallbackKey());
    assertEquals(
        core.network.proto.c2s.DialogResponseMessage.PayloadCase.PAYLOAD_NOT_SET,
        proto.getPayloadCase());

    DialogResponseMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals("dialog-2", roundTrip.dialogId());
    assertEquals("onConfirm", roundTrip.callbackKey());
    assertNull(roundTrip.data());
  }

  /** Verifies UDP registration conversion. */
  @Test
  public void testRegisterUdpRoundTrip() {
    byte[] token = new byte[] {5, 6};
    RegisterUdp message = new RegisterUdp(12, token, (short) 4);

    core.network.proto.c2s.RegisterUdp proto = ProtoConverter.toProto(message);
    assertEquals(12, proto.getSessionId());
    assertArrayEquals(token, proto.getSessionToken().toByteArray());
    assertEquals(4, proto.getClientId());

    RegisterUdp roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(12, roundTrip.sessionId());
    assertArrayEquals(token, roundTrip.sessionToken());
    assertEquals(4, roundTrip.clientId());
  }

  /** Verifies entity spawn request conversion. */
  @Test
  public void testRequestEntitySpawnRoundTrip() {
    RequestEntitySpawn message = new RequestEntitySpawn(99);

    core.network.proto.c2s.RequestEntitySpawn proto = ProtoConverter.toProto(message);
    assertEquals(99, proto.getEntityId());

    RequestEntitySpawn roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(99, roundTrip.entityId());
  }

  /** Verifies sound finished message conversion. */
  @Test
  public void testSoundFinishedRoundTrip() {
    SoundFinishedMessage message = new SoundFinishedMessage(123L);

    core.network.proto.c2s.SoundFinishedMessage proto = ProtoConverter.toProto(message);
    assertEquals(123L, proto.getSoundInstanceId());

    SoundFinishedMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(123L, roundTrip.soundInstanceId());
  }
}
