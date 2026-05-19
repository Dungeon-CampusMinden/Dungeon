package core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import contrib.item.concreteItem.ItemKey;
import core.Entity;
import core.Game;
import core.components.NetworkPositionComponent;
import core.components.PositionComponent;
import core.game.PreRunConfiguration;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.systems.NetworkPositionSmoothingSystem;
import core.utils.Point;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link DefaultSnapshotTranslator}. */
public class DefaultSnapshotTranslatorTest {

  /** Cleans global game state after each test. */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel(null);
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
  }

  /** Verifies snapshot inventory apply grows too-small existing inventory components. */
  @Test
  public void applySnapshotGrowsExistingInventoryForIncomingSlots() {
    DefaultSnapshotTranslator translator = new DefaultSnapshotTranslator();
    Entity entity = new Entity(42);
    entity.add(new InventoryComponent(1));
    Game.add(entity);
    ItemKey key = new ItemKey();
    SnapshotMessage snapshot =
        new SnapshotMessage(
            1,
            List.of(
                EntityState.builder()
                    .entityId(entity.id())
                    .inventory(new Item[] {null, null, key})
                    .build()),
            new LevelState(Set.of()));

    translator.applySnapshot(snapshot, new MessageDispatcher());

    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElseThrow();
    assertEquals(3, inventory.items().length);
    assertInstanceOf(ItemKey.class, inventory.get(2).orElseThrow());
    assertEquals(1, inventory.count(ItemKey.class));
  }

  /** Verifies multiplayer clients smooth small snapshot position updates instead of snapping. */
  @Test
  public void applySnapshotOnMultiplayerClientStoresNetworkPositionTarget() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    DefaultSnapshotTranslator translator = new DefaultSnapshotTranslator();
    Entity entity = new Entity(77);
    PositionComponent position = new PositionComponent(new Point(0, 0));
    entity.add(position);
    Game.add(entity);
    SnapshotMessage snapshot =
        new SnapshotMessage(
            1,
            List.of(EntityState.builder().entityId(entity.id()).position(new Point(1, 0)).build()),
            new LevelState(Set.of()));

    translator.applySnapshot(snapshot, new MessageDispatcher());

    assertEquals(new Point(0, 0), position.position());
    NetworkPositionComponent networkPosition =
        entity.fetch(NetworkPositionComponent.class).orElseThrow();
    assertEquals(new Point(1, 0), networkPosition.targetPosition());

    new NetworkPositionSmoothingSystem().execute();

    assertEquals(0.55f, position.position().x(), 0.0001f);
    assertEquals(0f, position.position().y(), 0.0001f);
  }
}
