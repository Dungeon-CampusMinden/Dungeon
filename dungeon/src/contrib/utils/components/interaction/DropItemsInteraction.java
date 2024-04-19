package contrib.utils.components.interaction;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.CoreAnimations;
import java.util.Arrays;
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
   * Small Helper to determine the position of the dropped item. Implements a simple circle drop.
   *
   * @param positionComponent The PositionComponent of the chest.
   * @param radian Radian of the current Item.
   * @return A Point in a unit vector around the chest.
   */
  private static Point calculateDropPosition(
      final PositionComponent positionComponent, double radian) {
    return new Point(
        (float) Math.cos(radian * Math.PI) + positionComponent.position().x,
        (float) Math.sin(radian * Math.PI) + positionComponent.position().y);
  }

  /**
   * Will drop all the items inside the {@link InventoryComponent} of the associated entity on the
   * floor.
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
    Item[] itemData = inventoryComponent.items();
    Arrays.stream(itemData)
        .forEach(
            item -> {
              if (item != null) {
                boolean itemDropped = false;
                for (int i = 1; i <= 10; i++) {
                  Coordinate randomTile =
                      LevelUtils.randomAccessibleTileCoordinateInRange(
                              positionComponent.position(), i)
                          .orElse(null);
                  if (randomTile != null) {
                    item.drop(randomTile.toPoint());
                    itemDropped = true;
                    break;
                  }
                }
                if (!itemDropped) {
                  // if no tile was found, drop on the hero
                  item.drop(
                      Game.hero()
                          .flatMap(hero -> hero.fetch(PositionComponent.class))
                          .map(PositionComponent::position)
                          .orElseGet(() -> new Point(0, 0)));
                }
              }
            });

    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            x ->
                x.animation(CoreAnimations.IDLE_RIGHT)
                    .ifPresent(y -> x.queueAnimation(y.duration(), CoreAnimations.IDLE_RIGHT)));
  }
}
