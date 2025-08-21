package contrib.item.concreteItem;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.level.elements.tile.FloorTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * A fairy pickup that restores health on collision.
 *
 * <p>Unlike other items, it cannot be collected into the inventory.
 */
public class ItemFairy extends Item {

  /** The default texture for all fairy pickups. */
  public static final IPath FAIRY_TEXTURE = new SimpleIPath("items/pickups/fairy_pickup.png");

  /**
   * Creates a new fairy pickup item.
   *
   * <p>The item heals the collector on collision and cannot be stored in the inventory.
   */
  public ItemFairy() {
    super("Fee", "Heilt volle HP.", Animation.fromSingleImage(FAIRY_TEXTURE));
  }

  /**
   * A fairy cannot be collected into the inventory.
   *
   * @param itemEntity The entity that represents the item in the world.
   * @param collector The entity who collects the item. (Most likely the hero)
   * @return false, because it cant be collected.
   */
  @Override
  public boolean collect(final Entity itemEntity, final Entity collector) {
    return false;
  }

  @Override
  public boolean drop(final Point position) {
    if (Game.tileAT(position) instanceof FloorTile) {
      TriConsumer<Entity, Entity, Direction> onCollide =
          (self, other, dir) -> {
            Game.hero()
                .ifPresent(
                    hero -> {
                      if (other.equals(hero)) {
                        other
                            .fetch(HealthComponent.class)
                            .ifPresent(
                                health -> health.restoreHealthpoints(health.maximalHealthpoints()));
                        Game.remove(self);
                      }
                    });
          };
      Entity pickUpItem = WorldItemBuilder.buildWorldItem(this, position);
      pickUpItem.add(new CollideComponent(onCollide, CollideComponent.DEFAULT_COLLIDER));
      Game.add(pickUpItem);
      return true;
    }
    return false;
  }
}
