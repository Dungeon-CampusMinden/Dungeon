package item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import item.effects.AttackSpeedEffect;

public class ItemPotionAttackSpeedPotion extends Item {
  public static final IPath DEFAULT_TEXTURE =
      new SimpleIPath("items/potion/attack_speed_potion.png");

  static {
    Item.ITEMS.put(
        ItemPotionAttackSpeedPotion.class.getSimpleName(), ItemPotionAttackSpeedPotion.class);
  }

  private final AttackSpeedEffect attackSpeedEffect;

  public ItemPotionAttackSpeedPotion() {
    super(
        "Attack Speed Potion",
        "A potion that increases the attack speed of the user.",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.attackSpeedEffect = new AttackSpeedEffect(1.5f, 15);
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.remove(this);
              this.attackSpeedEffect.applyAttackSpeed(e);
            });
  }
}
