package mushRoom.modules.items;

import contrib.item.Item;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

public class LanternItem extends Item {

  public static final String PATH = "items/gloves/fire_gloves.png";

  public LanternItem() {
    super("Leuchtende Handschuhe", "Bringen etwas Licht in diese Angelegenheit", new Animation(new SimpleIPath(PATH)), new Animation(new SimpleIPath(PATH)));
  }

}
