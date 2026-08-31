package feature.utils;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.utils.LevelUtils;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import feature.systems.EventScheduler;

/** ´System that if added makes a player drop his items on the floor, after a certain time. */
public class ItemDropSystem extends System {

  private static final int TIME_UNTIL_ITEMS_DROP_MS = 30000;
  private static final int MAX_ITEM_DROP_RADIUS = 2;

  /** Creates a ItemDropSystem. */
  public ItemDropSystem() {
    super(PlayerComponent.class, InventoryComponent.class);
    onEntityRemove = this::onEntityRemove;
  }

  @Override
  public void execute() {}

  /**
   * Drops the items of the entity that got removed after {@code TIME_UNTIL_ITEMS_DROP_MS} ms on the
   * floor.
   *
   * <p>Edge case: If the user reconnects within 30 seconds and then disconnects again, the items
   * may be dropped before the 30 seconds are up.
   *
   * @param entity entity that is being removed.
   */
  private void onEntityRemove(Entity entity) {
    EventScheduler.scheduleAction(
        () -> {
          if (Game.allPlayers()
              .anyMatch(entity::equals)) { // if player reconnects during reconnect window, abort
            return;
          }
          InventoryComponent inv = entity.fetch(InventoryComponent.class).orElseThrow();
          PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
          for (Item item : inv.items()) {
            if (item == null) {
              continue;
            }
            Point randomTile =
                LevelUtils.randomAccessibleTileInRangeAsPoint(pc.position(), 1).orElse(null);
            if (randomTile != null) {
              item.drop(randomTile);
            } else {
              item.drop(pc.position());
            }
          }
          inv.clear();
        },
        TIME_UNTIL_ITEMS_DROP_MS);
  }
}
