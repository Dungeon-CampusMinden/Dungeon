package core.network.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionHealth;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.network.messages.s2c.ConnectAck;
import core.network.messages.s2c.ConnectReject;
import core.network.messages.s2c.DialogCloseMessage;
import core.network.messages.s2c.DialogShowMessage;
import core.network.messages.s2c.EntityDespawnEvent;
import core.network.messages.s2c.EntitySpawnBatch;
import core.network.messages.s2c.EntitySpawnEvent;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.network.messages.s2c.RegisterAck;
import core.network.messages.s2c.SnapshotMessage;
import core.network.messages.s2c.SoundPlayMessage;
import core.network.messages.s2c.SoundStopMessage;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.DrawInfoData;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link ProtoConverter} s2c message conversions. */
public class ProtoConverterS2CTest {

  private static final float DELTA = 1e-6f;

  private static DrawInfoData createDrawInfo() {
    return new DrawInfoData("character/hero.png", 2.0f, 3.0f, "idle", 20);
  }

  /** Verifies connect ack conversion roundtrip. */
  @Test
  public void testConnectAckRoundTrip() {
    byte[] token = new byte[] {4, 5, 6};
    ConnectAck message = new ConnectAck((short) 7, 42, token);

    core.network.proto.s2c.ConnectAck proto = ProtoConverter.toProto(message);
    assertEquals(7, proto.getClientId());
    assertEquals(42, proto.getSessionId());
    assertArrayEquals(token, proto.getSessionToken().toByteArray());

    ConnectAck roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(message.clientId(), roundTrip.clientId());
    assertEquals(message.sessionId(), roundTrip.sessionId());
    assertArrayEquals(message.sessionToken(), roundTrip.sessionToken());
  }

  /** Verifies connect reject conversion roundtrip. */
  @Test
  public void testConnectRejectRoundTrip() {
    ConnectReject message = new ConnectReject(ConnectReject.Reason.INCOMPATIBLE_VERSION);

    core.network.proto.s2c.ConnectReject proto = ProtoConverter.toProto(message);
    assertEquals(
        core.network.proto.s2c.ConnectReject.RejectReason.REJECT_REASON_INCOMPATIBLE_VERSION,
        proto.getReason());

    ConnectReject roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(message.reason(), roundTrip.reason());
    assertNull(roundTrip.extraData());
  }

  /** Verifies dialog show message conversion roundtrip. */
  @Test
  public void testDialogShowRoundTrip() {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.OK)
            .dialogId("dialog-100")
            .put(DialogContextKeys.TITLE, "Title")
            .put(DialogContextKeys.MESSAGE, "Message")
            .build();
    DialogShowMessage message = new DialogShowMessage(context, false);

    core.network.proto.s2c.DialogShowMessage proto = ProtoConverter.toProto(message);
    assertEquals("dialog-100", proto.getDialogId());
    assertFalse(proto.getCanBeClosed());

    DialogShowMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals("dialog-100", roundTrip.context().dialogId());
    assertFalse(roundTrip.canBeClosed());
    assertEquals("Title", roundTrip.context().require(DialogContextKeys.TITLE, String.class));
  }

  /** Verifies dialog close message conversion roundtrip. */
  @Test
  public void testDialogCloseRoundTrip() {
    DialogCloseMessage message = new DialogCloseMessage("dialog-200");

    core.network.proto.s2c.DialogCloseMessage proto = ProtoConverter.toProto(message);
    assertEquals("dialog-200", proto.getDialogId());

    DialogCloseMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals("dialog-200", roundTrip.dialogId());
  }

  /** Verifies entity spawn event conversion roundtrip. */
  @Test
  public void testEntitySpawnRoundTrip() {
    PositionComponent position = new PositionComponent(new Point(4.0f, 5.0f), Direction.UP);
    position.rotation(45.0f);
    position.scale(Vector2.of(1.5f, 0.75f));
    DrawInfoData drawInfo = createDrawInfo();
    PlayerComponent playerComponent = new PlayerComponent(true, "Hero");
    EntitySpawnEvent message =
        new EntitySpawnEvent(42, position, drawInfo, true, playerComponent, (byte) 1);

    core.network.proto.s2c.EntitySpawnEvent proto = ProtoConverter.toProto(message);
    assertEquals(42, proto.getEntityId());
    assertEquals(4.0f, proto.getPosition().getPosition().getX(), DELTA);
    assertEquals(5.0f, proto.getPosition().getPosition().getY(), DELTA);
    assertEquals(
        core.network.proto.common.Direction.DIRECTION_UP, proto.getPosition().getViewDirection());
    assertEquals(45.0f, proto.getPosition().getRotation(), DELTA);
    assertEquals(1.5f, proto.getPosition().getScale().getX(), DELTA);
    assertEquals(0.75f, proto.getPosition().getScale().getY(), DELTA);
    assertTrue(proto.hasPlayerInfo());
    assertEquals("Hero", proto.getPlayerInfo().getPlayerName());
    assertTrue(proto.getPlayerInfo().getIsLocalPlayer());
    assertEquals(1, proto.getCharacterClassId());

    EntitySpawnEvent roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(42, roundTrip.entityId());
    assertEquals(4.0f, roundTrip.positionComponent().position().x(), DELTA);
    assertEquals(5.0f, roundTrip.positionComponent().position().y(), DELTA);
    assertEquals(Direction.UP, roundTrip.positionComponent().viewDirection());
    assertEquals(45.0f, roundTrip.positionComponent().rotation(), DELTA);
    assertEquals(1.5f, roundTrip.positionComponent().scale().x(), DELTA);
    assertEquals(0.75f, roundTrip.positionComponent().scale().y(), DELTA);
    assertNotNull(roundTrip.playerComponent());
    assertEquals("Hero", roundTrip.playerComponent().playerName());
    assertTrue(roundTrip.playerComponent().isLocal());
    assertEquals(1, roundTrip.characterClassId());
    assertEquals("character/hero.png", roundTrip.drawInfo().texturePath());
    assertEquals(2.0f, roundTrip.drawInfo().scaleX(), DELTA);
    assertEquals(3.0f, roundTrip.drawInfo().scaleY(), DELTA);
    assertEquals("idle", roundTrip.drawInfo().animationName());
    assertEquals(20, roundTrip.drawInfo().currentFrame());
  }

  /** Verifies entity spawn batch conversion roundtrip. */
  @Test
  public void testEntitySpawnBatchRoundTrip() {
    EntitySpawnEvent first =
        new EntitySpawnEvent(
            1,
            new PositionComponent(new Point(1.0f, 1.0f)),
            createDrawInfo(),
            false,
            null,
            (byte) 0);
    EntitySpawnEvent second =
        new EntitySpawnEvent(
            2,
            new PositionComponent(new Point(2.0f, 2.0f)),
            createDrawInfo(),
            true,
            new PlayerComponent(false, "Other"),
            (byte) 2);

    EntitySpawnBatch message = new EntitySpawnBatch(List.of(first, second));
    core.network.proto.s2c.EntitySpawnBatch proto = ProtoConverter.toProto(message);
    assertEquals(2, proto.getEntitiesCount());

    EntitySpawnBatch roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(2, roundTrip.entities().size());
    assertEquals(1, roundTrip.entities().get(0).entityId());
    assertEquals(2, roundTrip.entities().get(1).entityId());
  }

  /** Verifies entity despawn event conversion roundtrip. */
  @Test
  public void testEntityDespawnRoundTrip() {
    EntityDespawnEvent message = new EntityDespawnEvent(99, "destroyed");

    core.network.proto.s2c.EntityDespawnEvent proto = ProtoConverter.toProto(message);
    assertEquals(99, proto.getEntityId());
    assertEquals("destroyed", proto.getReason());

    EntityDespawnEvent roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(99, roundTrip.entityId());
    assertEquals("destroyed", roundTrip.reason());
  }

  /** Verifies entity state conversion roundtrip. */
  @Test
  public void testEntityStateRoundTrip() {
    ItemPotionHealth item = new ItemPotionHealth(HealthPotionType.GREATER);
    EntityState message =
        EntityState.builder()
            .entityId(7)
            .entityName("Goblin")
            .position(new Point(1.5f, 2.5f))
            .viewDirection(Direction.LEFT)
            .rotation(90.0f)
            .scale(Vector2.of(1.25f, 0.75f))
            .currentHealth(5)
            .maxHealth(10)
            .currentMana(2.5f)
            .maxMana(5.0f)
            .stateName("idle")
            .tintColor(0x11223344)
            .inventory(new Item[] {null, item, null})
            .build();

    core.network.proto.s2c.EntityState proto = ProtoConverter.toProto(message);
    assertEquals(7, proto.getEntityId());
    assertTrue(proto.hasEntityName());
    assertEquals("Goblin", proto.getEntityName());

    core.network.proto.s2c.ItemSlot slot = proto.getInventory(1);
    assertEquals(1, slot.getSlotIndex());
    assertTrue(slot.hasItem());
    assertEquals(
        HealthPotionType.GREATER.name(), slot.getItem().getItemDataMap().get("health_potion_type"));
    assertEquals(
        Integer.toString(item.healAmount()), slot.getItem().getItemDataMap().get("heal_amount"));

    EntityState roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(7, roundTrip.entityId());
    assertEquals("Goblin", roundTrip.entityName().orElseThrow());
    assertEquals("LEFT", roundTrip.viewDirection().orElseThrow());
    assertEquals(90.0f, roundTrip.rotation().orElseThrow(), DELTA);
    assertEquals(5, roundTrip.currentHealth().orElseThrow());
    assertEquals(10, roundTrip.maxHealth().orElseThrow());
    assertEquals(2.5f, roundTrip.currentMana().orElseThrow(), DELTA);
    assertEquals(5.0f, roundTrip.maxMana().orElseThrow(), DELTA);
    assertEquals("idle", roundTrip.stateName().orElseThrow());
    assertEquals(0x11223344, roundTrip.tintColor().orElseThrow());
    assertEquals(1.25f, roundTrip.scale().orElseThrow().x(), DELTA);
    assertEquals(0.75f, roundTrip.scale().orElseThrow().y(), DELTA);
    assertEquals(3, roundTrip.inventory().orElseThrow().length);
    assertEquals(ItemPotionHealth.class, roundTrip.inventory().orElseThrow()[1].getClass());
    ItemPotionHealth roundTripItem = (ItemPotionHealth) roundTrip.inventory().orElseThrow()[1];
    assertEquals(HealthPotionType.GREATER, roundTripItem.type());
    assertEquals(item.healAmount(), roundTripItem.healAmount());
  }

  /** Verifies snapshot message conversion roundtrip. */
  @Test
  public void testSnapshotRoundTrip() {
    EntityState state = EntityState.builder().entityId(5).build();
    SnapshotMessage message = new SnapshotMessage(123, List.of(state));

    core.network.proto.s2c.SnapshotMessage proto = ProtoConverter.toProto(message);
    assertEquals(123, proto.getServerTick());
    assertEquals(1, proto.getEntitiesCount());

    SnapshotMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(123, roundTrip.serverTick());
    assertEquals(1, roundTrip.entities().size());
  }

  /** Verifies game over conversion roundtrip. */
  @Test
  public void testGameOverRoundTrip() {
    GameOverEvent message = new GameOverEvent("all_levels_completed");

    core.network.proto.s2c.GameOverEvent proto = ProtoConverter.toProto(message);
    assertEquals("all_levels_completed", proto.getReason());

    GameOverEvent roundTrip = ProtoConverter.fromProto(proto);
    assertEquals("all_levels_completed", roundTrip.reason());
  }

  /** Verifies level change conversion roundtrip. */
  @Test
  public void testLevelChangeRoundTrip() {
    LevelChangeEvent message = new LevelChangeEvent("level-1", "data");

    core.network.proto.s2c.LevelChangeEvent proto = ProtoConverter.toProto(message);
    assertEquals("level-1", proto.getLevelName());
    assertEquals("data", proto.getLevelData());

    LevelChangeEvent roundTrip = ProtoConverter.fromProto(proto);
    assertEquals("level-1", roundTrip.levelName());
    assertEquals("data", roundTrip.levelData());
  }

  /** Verifies register ack conversion roundtrip. */
  @Test
  public void testRegisterAckRoundTrip() {
    RegisterAck message = new RegisterAck(true);

    core.network.proto.s2c.RegisterAck proto = ProtoConverter.toProto(message);
    assertTrue(proto.getOk());

    RegisterAck roundTrip = ProtoConverter.fromProto(proto);
    assertTrue(roundTrip.ok());
  }

  /** Verifies sound play conversion roundtrip. */
  @Test
  public void testSoundPlayRoundTrip() {
    SoundPlayMessage message =
        new SoundPlayMessage(11L, 2, "torch", 0.5f, 1.1f, -0.2f, true, 10.0f, 0.9f);

    core.network.proto.s2c.SoundPlayMessage proto = ProtoConverter.toProto(message);
    assertEquals(11L, proto.getSoundInstanceId());
    assertEquals(2, proto.getEntityId());
    assertEquals("torch", proto.getSoundName());

    SoundPlayMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(11L, roundTrip.soundInstanceId());
    assertEquals(2, roundTrip.entityId());
    assertEquals("torch", roundTrip.soundName());
    assertEquals(0.5f, roundTrip.volume(), DELTA);
  }

  /** Verifies sound stop conversion roundtrip. */
  @Test
  public void testSoundStopRoundTrip() {
    SoundStopMessage message = new SoundStopMessage(55L);

    core.network.proto.s2c.SoundStopMessage proto = ProtoConverter.toProto(message);
    assertEquals(55L, proto.getSoundInstanceId());

    SoundStopMessage roundTrip = ProtoConverter.fromProto(proto);
    assertEquals(55L, roundTrip.soundInstanceId());
  }
}
