package feature.inventory.items;

import engine.Entity;
import engine.Game;
import engine.level.Tile;
import engine.level.elements.tile.FloorTile;
import engine.utils.Direction;
import engine.utils.Point;
import engine.utils.TriConsumer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.HealthComponent;
import feature.entities.WorldItemBuilder;
import feature.inventory.Item;
import java.util.Map;
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
   * Creates a new heart pickup item that heals 1 health points.
   *
   * <p>The item heals the collector on collision and cannot be stored in the inventory.
   */
  public ItemHeart() {
    this(1);
  }

  /**
   * Returns the amount of healing applied when this heart is collected.
   *
   * @return the healing amount
   */
  public int healAmount() {
    return healAmount;
  }

  @Override
  public Map<String, String> itemData() {
    return Map.of(DATA_KEY_HEAL_AMOUNT, Integer.toString(healAmount));
  }

  /**
   * A heart cannot be collected into the inventory.
   *
   * @param itemEntity The entity that represents the item in the world.
   * @param collector The entity who collects the item. (Most likely the player)
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
            Game.player()
                .ifPresent(
                    player -> {
                      if (other.equals(player)) {
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
