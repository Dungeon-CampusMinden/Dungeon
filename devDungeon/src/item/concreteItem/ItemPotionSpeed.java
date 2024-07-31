package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import item.effects.SpeedEffect;

/**
 * A potion that increases the speed of the user.
 *
 * <p>The ItemPotionSpeed is a potion that can be used by the player to increase their speed. When
 * used, the potion will be removed from the player's inventory and the player's speed will be
 * increased for a short period of time.
 *
 * @see SpeedEffect
 */
public class ItemPotionSpeed extends Item {
  private static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/potion/speed_potion.png");

  static {
    Item.registerItem(ItemPotionSpeed.class);
  }

  private final SpeedEffect speedEffect;

  /** Constructs a new ItemPotionSpeed. */
  public ItemPotionSpeed() {
    super(
        "Speed Potion",
        "A potion that increases your speed",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.speedEffect = new SpeedEffect(3, 5);
    if (!testEffect()) {
      description("It looks like this potion is broken...");
    }
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              try {
                speedEffect.applySpeedEffect(e);
                component.remove(this);
              } catch (UnsupportedOperationException ignored) {
              }
            });
  }

  /**
   * Tests the effect of the speed potion.
   *
   * <p>This method creates an entity with a velocity component and applies the speed effect to it.
   * If the speed effect changes the velocity of the entity, the method returns true. Otherwise, it
   * returns false.
   *
   * @return True if the speed effect changes the velocity of the entity, false otherwise.
   */
  private boolean testEffect() {
    Entity e = new Entity();
    e.add(new InventoryComponent());
    e.add(new PositionComponent());
    VelocityComponent vc = new VelocityComponent(1, 1);
    e.add(vc);
    try {
      speedEffect.applySpeedEffect(e);
    } catch (UnsupportedOperationException ex) {
      return false;
    }
    return vc.xVelocity() != 1 || vc.yVelocity() != 1;
  }
}
