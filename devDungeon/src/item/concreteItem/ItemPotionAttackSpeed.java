package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import item.effects.AttackSpeedEffect;

/**
 * A potion that increases the attack speed of the user.
 *
 * <p>The ItemPotionAttackSpeed is a potion that can be used by the player to increase their attack
 * speed. When used, the potion will be removed from the player's inventory and the player's attack
 * speed will be increased for a short period of time.
 *
 * @see AttackSpeedEffect
 */
public class ItemPotionAttackSpeed extends Item {

  private static final IPath DEFAULT_TEXTURE =
      new SimpleIPath("items/potion/attack_speed_potion.png");
  private static final float SPEED_MULTIPLIER = 1.5f;
  private static final int EFFECT_DURATION = 15;

  static {
    Item.registerItem(ItemPotionAttackSpeed.class);
  }

  private final AttackSpeedEffect attackSpeedEffect;

  /** Constructs a new ItemPotionAttackSpeed. */
  public ItemPotionAttackSpeed() {
    super(
        "Attack Speed Potion",
        "A potion that increases the attack speed of the user.",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.attackSpeedEffect = new AttackSpeedEffect(SPEED_MULTIPLIER, EFFECT_DURATION);
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              attackSpeedEffect.applyAttackSpeed(e);
            });
  }
}
