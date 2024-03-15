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

public class ItemPotionSpeedPotion extends Item {
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/potion/speed_potion.png");

  static {
    Item.ITEMS.put(ItemPotionSpeedPotion.class.getSimpleName(), ItemPotionSpeedPotion.class);
  }

  private final SpeedEffect speedEffect;

  public ItemPotionSpeedPotion() {
    super(
        "Speed Potion",
        "A potion that increases your speed",
        Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.speedEffect = new SpeedEffect(3, 5);
    if (!this.testEffect()) {
      this.description("It looks like this potion is broken...");
    }
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              try {
                this.speedEffect.applySpeedEffect(e);
                component.remove(this);
              } catch (UnsupportedOperationException ignored) {
              }
            });
  }

  private boolean testEffect() {
    Entity e = new Entity();
    e.add(new InventoryComponent());
    e.add(new PositionComponent());
    VelocityComponent vc = new VelocityComponent(1, 1);
    e.add(vc);
    try {
      this.speedEffect.applySpeedEffect(e);
    } catch (UnsupportedOperationException ex) {
      return false;
    }
    return vc.xVelocity() != 1 || vc.yVelocity() != 1;
  }
}
