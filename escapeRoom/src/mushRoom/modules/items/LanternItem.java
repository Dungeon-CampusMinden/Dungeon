package mushRoom.modules.items;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

public class LanternItem extends Item {

  public static final String PATH = "items/gloves/fire_gloves.png";

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
