package contrib.item.concreteItem;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.elements.tile.FloorTile;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;

/**
 * A heart pickup that restores health on collision.
 *
 * <p>Unlike other items, it cannot be collected into the inventory.
 */
public class ItemHeart extends Item {
  /** The default texture for all heart pickups. */
  public static final IPath HEART_TEXTURE = new SimpleIPath("items/pickups/heart_pickup.png");

  private final int healAmount;

  /**
   * Creates a new heart pickup item.
   *
   * <p>The item heals the collector on collision and cannot be stored in the inventory.
   *
   * @param healAmount the amount of healing applied.
   */
  public ItemHeart(int healAmount) {
    super("Herz", "Heilt ein wenig HP.", new Animation(HEART_TEXTURE));
    this.healAmount = healAmount;
  }

  /**
   * A heart cannot be collected into the inventory.
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
  public Optional<Entity> drop(final Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof FloorTile) {
      TriConsumer<Entity, Entity, Direction> onCollide =
          (self, other, dir) -> {
            Game.hero()
                .ifPresent(
                    hero -> {
                      if (other.equals(hero)) {
                        other
                            .fetch(HealthComponent.class)
                            .ifPresent(health -> health.restoreHealthpoints(healAmount));
                        Game.remove(self);
                      }
                    });
          };
      Entity pickUpItem = WorldItemBuilder.buildWorldItem(this, position);
      pickUpItem.add(new CollideComponent(onCollide, CollideComponent.DEFAULT_COLLIDER));
      Game.add(pickUpItem);
      return Optional.of(pickUpItem);
    }
    return Optional.empty();
  }
}
