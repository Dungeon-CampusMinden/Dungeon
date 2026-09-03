package rooms.lasthour.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.LevelState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogEntry;
import feature.systems.PositionSync;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for Last Hour metadata synchronization. */
public class LastHourSnapshotTranslatorTest {

  private static final float DELTA = 1e-6f;

  @AfterEach
  void cleanupGameState() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** Verifies collider metadata is included for metadata-only spawn events. */
  @Test
  void buildSpawnEventIncludesColliderMetadata() {
    Entity entity = new Entity("trigger");
    PositionComponent positionComponent = new PositionComponent(new Point(4f, 6f));
    positionComponent.scale(Vector2.of(1.5f, 0.75f));
    entity.add(positionComponent);

    CollideComponent collideComponent =
        new CollideComponent(Vector2.of(0.25f, 0.5f), Vector2.of(2f, 1f)).isSolid(false);
    entity.add(collideComponent);
    PositionSync.syncPosition(entity);

    EntitySpawnEvent spawnEvent =
        new LastHourEntitySpawnStrategy().buildSpawnEvent(entity).orElseThrow();

    assertNotNull(spawnEvent.positionComponent());
    assertTrue(spawnEvent.drawInfo() == null);

    CollideComponent mappedCollider =
        LastHourSnapshotTranslator.collideComponentFromMetadata(spawnEvent.metadata())
            .orElseThrow();
    assertFalse(mappedCollider.isSolid());
    assertEquals(2f, mappedCollider.collider().width(), DELTA);
    assertEquals(1f, mappedCollider.collider().height(), DELTA);
    assertEquals(0.25f, mappedCollider.collider().offset().x(), DELTA);
    assertEquals(0.5f, mappedCollider.collider().offset().y(), DELTA);
    assertEquals(4f, mappedCollider.collider().position().x(), DELTA);
    assertEquals(6f, mappedCollider.collider().position().y(), DELTA);
    assertEquals(1.5f, mappedCollider.collider().scale().x(), DELTA);
    assertEquals(0.75f, mappedCollider.collider().scale().y(), DELTA);
  }

  /** Verifies metadata-only collider entities are appended to snapshots. */
  @Test
  void translateToSnapshotIncludesMetadataOnlyColliderEntity() {
    Entity entity = new Entity("invisible-wall");
    PositionComponent positionComponent = new PositionComponent(new Point(8f, 9f));
    positionComponent.scale(Vector2.of(1.25f, 1.5f));
    entity.add(positionComponent);
    entity.add(new CollideComponent(Vector2.of(0.4f, 0.6f), Vector2.of(3f, 2f)));
    PositionSync.syncPosition(entity);
    Game.add(entity);

    SnapshotMessage snapshot =
        new LastHourSnapshotTranslator().translateToSnapshot(1).orElseThrow();
    EntityState entityState =
        snapshot.entities().stream()
            .filter(state -> state.entityId() == entity.id())
            .findFirst()
            .orElseThrow();

    assertEquals(entity.name(), entityState.entityName().orElseThrow());
    assertEquals(positionComponent.position(), entityState.position().orElseThrow());

    CollideComponent mappedCollider =
        LastHourSnapshotTranslator.collideComponentFromMetadata(
                entityState.metadata().orElseThrow())
            .orElseThrow();
    assertEquals(3f, mappedCollider.collider().width(), DELTA);
    assertEquals(2f, mappedCollider.collider().height(), DELTA);
    assertEquals(0.4f, mappedCollider.collider().offset().x(), DELTA);
    assertEquals(0.6f, mappedCollider.collider().offset().y(), DELTA);
    assertEquals(1.25f, mappedCollider.collider().scale().x(), DELTA);
    assertEquals(1.5f, mappedCollider.collider().scale().y(), DELTA);
  }

  /** Verifies keypad and collider metadata can be applied from the same snapshot entry. */
  @Test
  void applySnapshotUpdatesKeypadAndColliderStateTogether() {
    Entity entity = new Entity("keypad");
    entity.add(new PositionComponent(new Point(1f, 1f)));
    entity.add(new KeypadComponent(List.of(9, 8, 7), () -> {}, true));
    entity.add(new CollideComponent());
    Game.add(entity);

    CollideComponent updatedCollider =
        new CollideComponent(Vector2.of(0.3f, 0.4f), Vector2.of(2.5f, 1.5f)).isSolid(false);
    updatedCollider.collider().position(new Point(10f, 11f));
    updatedCollider.collider().scale(Vector2.of(1.1f, 0.9f));

    Map<String, String> metadata = new HashMap<>();
    metadata.put(
        LastHourEntitySpawnStrategy.METADATA_TYPE, LastHourEntitySpawnStrategy.TYPE_KEYPAD);
    metadata.put(LastHourEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS, "1,2,3");
    metadata.put(LastHourEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS, "1,2");
    metadata.put(LastHourEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED, "true");
    metadata.put(LastHourEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT, "false");
    metadata.putAll(LastHourSnapshotTranslator.collideMetadata(updatedCollider));

    SnapshotMessage snapshot =
        new SnapshotMessage(
            2,
            List.of(
                EntityState.builder()
                    .entityId(entity.id())
                    .position(new Point(10f, 11f))
                    .scale(Vector2.of(1.1f, 0.9f))
                    .metadata(metadata)
                    .build()),
            new LevelState(Set.of()));

    new LastHourSnapshotTranslator().applySnapshot(snapshot, null);

    KeypadComponent keypadComponent = entity.fetch(KeypadComponent.class).orElseThrow();
    assertEquals(List.of(9, 8, 7), keypadComponent.correctDigits());
    assertEquals(List.of(1, 2), keypadComponent.enteredDigits());
    assertTrue(keypadComponent.isUnlocked());
    assertFalse(keypadComponent.showDigitCount());

    CollideComponent collideComponent = entity.fetch(CollideComponent.class).orElseThrow();
    assertFalse(collideComponent.isSolid());
    assertEquals(2.5f, collideComponent.collider().width(), DELTA);
    assertEquals(1.5f, collideComponent.collider().height(), DELTA);
    assertEquals(0.3f, collideComponent.collider().offset().x(), DELTA);
    assertEquals(0.4f, collideComponent.collider().offset().y(), DELTA);
    assertEquals(10f, collideComponent.collider().position().x(), DELTA);
    assertEquals(11f, collideComponent.collider().position().y(), DELTA);
    assertEquals(1.1f, collideComponent.collider().scale().x(), DELTA);
    assertEquals(0.9f, collideComponent.collider().scale().y(), DELTA);
  }

  /** Verifies quest log metadata preserves whether entries were created by a player. */
  @Test
  void questLogMetadataPreservesUserCreatedFlag() {
    QuestLogComponent questLog = new QuestLogComponent();
    questLog.add("Notes", new QuestLogEntry("Player note", 42, true, "Ada", false));

    Map<String, String> metadata = LastHourSnapshotTranslator.questLogMetadata(questLog);

    QuestLogEntry restoredEntry =
        LastHourSnapshotTranslator.questLogFromMetadata(metadata).orElseThrow().get("Notes").get(0);
    assertTrue(restoredEntry.userCreated());
    assertEquals("Ada", restoredEntry.owner());
  }

  /** Verifies old quest log metadata without userCreated is still read as game-created. */
  @Test
  void questLogMetadataWithoutUserCreatedDefaultsToFalse() {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(
        LastHourEntitySpawnStrategy.METADATA_TYPE, LastHourEntitySpawnStrategy.TYPE_QUESTLOG);
    metadata.put(
        LastHourEntitySpawnStrategy.METADATA_QUESTLOG_ENTRIES,
        String.join(",", encode("Main"), encode("Door opened"), "17", encode("System"), "false"));

    QuestLogEntry restoredEntry =
        LastHourSnapshotTranslator.questLogFromMetadata(metadata).orElseThrow().get("Main").get(0);
    assertFalse(restoredEntry.userCreated());
    assertEquals("System", restoredEntry.owner());
  }

  private static String encode(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
