package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import item.effects.RegenerationEffect;

/**
 * A potion that heals the user over time.
 *
 * <p>The ItemPotionRegeneration is a potion that can be used by the player to heal over time. When
 * used, the potion will be removed from the player's inventory and the player will be healed for a
 * short period of time.
 *
 * @see RegenerationEffect
 */
public class ItemPotionRegeneration extends Item {
  private static final IPath DEFAULT_TEXTURE =
      new SimpleIPath("items/potion/regeneration_potion.png");
  private static final int EFFECT_DURATION = 20;
  private static final int HEAL_PER_SECOND = 1;

  static {
    Item.registerItem(ItemPotionRegeneration.class);
  }

  private final RegenerationEffect regenerationEffect;

  /** Constructs a new ItemPotionRegeneration. */
  public ItemPotionRegeneration() {
    super(
        "Regeneration Potion", "A potion that heals you over time", new Animation(DEFAULT_TEXTURE));
    this.regenerationEffect = new RegenerationEffect(HEAL_PER_SECOND, EFFECT_DURATION);
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              regenerationEffect.applyRegeneration(e);
            });
  }
}
