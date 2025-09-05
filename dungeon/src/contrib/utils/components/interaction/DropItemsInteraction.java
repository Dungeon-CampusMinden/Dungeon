package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class is a specific implementation of the {@link Consumer} interface to use in the {@link
 * InteractionComponent}.
 *
 * <p>The implementation will drop all the items inside the {@link InventoryComponent} of the
 * associated entity on the floor.
 *
 * <p>Note: The entity that will use this function needs an {@link InventoryComponent} and a {@link
 * PositionComponent}. A {@link DrawComponent} is optional.
 *
 * <p>If a {@link DrawComponent} is present, after the interaction, the {@link
 * CoreAnimations#IDLE_RIGHT} animation will be set as the current animation.
 */
public final class DropItemsInteraction implements BiConsumer<Entity, Entity> {

  /**
   * Maximum radius to drop an item from the entity position. If the item cannot be dropped within
   * this radius, it will be dropped at the hero's position.
   *
   * @see #tryDropItem(Item, PositionComponent)
   */
  private static final int MAX_ITEM_DROP_RADIUS = 10;

  /**
   * Will drop all the items inside the {@link InventoryComponent} of the associated entity on the
   * floor. If an item cannot be dropped within a certain radius of the entity, it will be dropped
   * at the hero's position. If the hero is not present, the item will be dropped at (0, 0).
   *
   * <p>Note: The entity that will use this function needs an {@link InventoryComponent} and a
   * {@link PositionComponent}. A {@link DrawComponent} is optional.
   *
   * <p>If a {@link DrawComponent} is present, after the interaction, the {@link
   * CoreAnimations#IDLE_RIGHT} animation will be set as the current animation.
   *
   * @param entity Entity that holds the items to drop.
   * @param who The entity that triggered the interaction (could be null).
   */
  public void accept(final Entity entity, final Entity who) {
    InventoryComponent inventoryComponent =
        entity
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, InventoryComponent.class));
    PositionComponent positionComponent =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    for (Item item : inventoryComponent.items()) {
      if (item != null && !this.tryDropItem(item, positionComponent)) {
        this.dropItemAtHeroPosition(item);
      }
    }
  }

  /**
   * Try to drop an item in a random accessible tile in a range of {@link #MAX_ITEM_DROP_RADIUS}
   * from the entity position.
   *
   * @param item The item to drop
   * @param positionComponent The position component of the entity
   * @return true if the item was dropped, false otherwise
   * @see LevelUtils#randomAccessibleTileInRangeAsPoint(Point, float)
   */
  private boolean tryDropItem(Item item, PositionComponent positionComponent) {
    for (int i = 1; i <= MAX_ITEM_DROP_RADIUS; i++) {
      Point randomTile =
          LevelUtils.randomAccessibleTileInRangeAsPoint(positionComponent.position(), i)
              .orElse(null);
      if (randomTile != null) {
        item.drop(randomTile);
        return true;
      }
    }
    return false;
  }

  /**
   * Drop the item at the hero position. If the hero is not present, the item will be dropped at (0,
   * 0).
   *
   * @param item The item to drop
   */
  private void dropItemAtHeroPosition(Item item) {
    Point heroPosition =
        Game.hero()
            .flatMap(hero -> hero.fetch(PositionComponent.class))
            .map(PositionComponent::position)
            .orElseGet(() -> new Point(0, 0));
    item.drop(heroPosition);
  }
}
