package mushRoom.modules.items;

import contrib.item.Item;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

public class AxeItem extends Item {

  public static final String PATH = "items/weapon/snake_sword.png";

  public AxeItem() {
    super("Axt", "Damit können bestimmte Bäume gefällt werden.", new Animation(new SimpleIPath(PATH)), new Animation(new SimpleIPath(PATH)));
  }

}
