package contrib.item.concreteItem;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.crafting.CraftingIngredient;
import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents a health potion item in the game. The health potion can be used to restore
 * health points to the entity that uses it. The amount of health restored is determined by the
 * heal_amount attribute. The health potion has a default texture that is used for its visual
 * representation in the game.
 */
public class ItemPotionHealth extends Item {

  /** The default texture for all health potions. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/potion/health_potion.png");

  /** The type metadata used to configure this potion. */
  private final HealthPotionType type;

  /** The amount of health that this potion restores when used. */
  private final int healAmount;

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
        new Animation(DEFAULT_TEXTURE));
    this.type = type;
    this.healAmount = type.getHealAmount();
  }

  /**
   * Constructs a new WEAK health potion. The potion restores 7 health points when used. The {@link
   * #DEFAULT_TEXTURE default texture} is used for the visual representation of the potion.
   *
   * @see HealthPotionType#WEAK
   */
  public ItemPotionHealth() {
    this(HealthPotionType.WEAK);
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
              component.removeOne(this);
              this.healUser(this.healAmount, e);
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
    return other.healAmount == this.healAmount;
  }

  /**
   * Returns the potion type used to configure this item.
   *
   * @return the health potion type
   */
  public HealthPotionType type() {
    return type;
  }

  /**
   * Returns the amount of health restored when this potion is used.
   *
   * @return the healing amount
   */
  public int healAmount() {
    return healAmount;
  }

  @Override
  public Map<String, String> itemData() {
    return Map.of(
        DATA_KEY_POTION_TYPE, type.name(),
        DATA_KEY_HEAL_AMOUNT, Integer.toString(healAmount));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ItemPotionHealth other)) return false;

    return this.healAmount == other.healAmount;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ItemPotionHealth.class, healAmount);
  }
}
