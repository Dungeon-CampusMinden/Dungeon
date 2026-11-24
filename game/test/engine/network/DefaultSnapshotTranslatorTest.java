package engine.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.LevelState;
import engine.network.messages.s2c.SnapshotMessage;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import feature.inventory.items.ItemKey;
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

  /** Verifies the authoritative snapshot also shrinks a client inventory when required. */
  @Test
  public void applySnapshotShrinksExistingInventoryToIncomingSlotCount() {
    DefaultSnapshotTranslator translator = new DefaultSnapshotTranslator();
    Entity entity = new Entity(43);
    entity.add(new InventoryComponent(24));
    Game.add(entity);
    ItemKey key = new ItemKey();
    SnapshotMessage snapshot =
        new SnapshotMessage(
            1,
            List.of(
                EntityState.builder().entityId(entity.id()).inventory(new Item[] {key}).build()),
            new LevelState(Set.of()));

    translator.applySnapshot(snapshot, new MessageDispatcher());

    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElseThrow();
    assertEquals(1, inventory.items().length);
    assertInstanceOf(ItemKey.class, inventory.get(0).orElseThrow());
    assertEquals(1, inventory.count(ItemKey.class));
  }
}
