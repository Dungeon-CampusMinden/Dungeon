package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.crafting.CraftingIngredient;
import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * This class represents a health potion item in the game. The health potion can be used to restore
 * health points to the entity that uses it. The amount of health restored is determined by the
 * heal_amount attribute. The health potion has a default texture that is used for its visual
 * representation in the game.
 */
public final class ItemPotionHealth extends Item {

  /** The default texture for all health potions. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/potion/health_potion.png");

  /** The amount of health that this potion restores when used. */
  private final int heal_amount;

  /**
   * Constructs a new health potion with the specified type. The type determines the name and the
   * healing amount of the potion. The {@link #DEFAULT_TEXTURE default texture} is used for the
   * visual representation of the potion.
   *
   * @param type The type of the health potion to be created.
   */
  public ItemPotionHealth(HealthPotionType type) {
    super(
        type.getName() + " Health Potion",
        "It heals you for " + type.getHealAmount() + " health points.",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.heal_amount = type.getHealAmount();
  }

  /**
   * Constructs a new health potion with the specified name, healing amount, and texture.
   *
   * @param itemName The name of the health potion.
   * @param healAmount The amount of health that the potion restores when used.
   * @param texture The texture used for the visual representation of the potion.
   */
  public ItemPotionHealth(String itemName, int healAmount, IPath texture) {
    super(
        itemName,
        "It heals you for " + healAmount + " health points.",
        Animation.fromSingleImage(texture));
    this.heal_amount = healAmount;
  }

  /**
   * Uses the health potion on the specified entity. The potion is removed from the entity's
   * inventory and the entity is healed by the heal_amount.
   *
   * @param e The entity that uses the potion.
   */
  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              this.healUser(this.heal_amount, e);
            });
  }

  /**
   * Heals the specified entity by the specified amount. The healing is done by applying negative
   * damage to the specified amount to the entity.
   *
   * @param amount The amount of health to restore to the entity.
   * @param e The entity to heal.
   */
  private void healUser(int amount, Entity e) {
    e.fetch(HealthComponent.class)
        .ifPresent(hc -> hc.receiveHit(new Damage(-amount, DamageType.HEAL, null)));
  }

  @Override
  public boolean match(final CraftingIngredient input) {
    if (!(input instanceof ItemPotionHealth other)) {
      return super.match(input);
    }
    return other.heal_amount == this.heal_amount;
  }
}
