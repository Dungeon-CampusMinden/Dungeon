package rooms.mushroom.modules.items;

import engine.Entity;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.inventory.Item;
import rooms.mushroom.Sounds;

/** Item representing glowing gloves that provide light. */
public class LanternItem extends Item {

  private static final String PATH = "items/gloves/fire_gloves.png";

  /** Constructs a new LanternItem. */
  public LanternItem() {
    super(
        "Leuchtende Handschuhe",
        "Bringen etwas Licht in diese Angelegenheit",
        new Animation(new SimpleIPath(PATH)),
        new Animation(new SimpleIPath(PATH)));
  }

  @Override
  public void use(Entity user) {
    // Nothing
  }

  @Override
  public boolean collect(Entity itemEntity, Entity collector) {
    Sounds.KEY_ITEM_PICKUP_SOUND.play();
    return super.collect(itemEntity, collector);
  }
}
