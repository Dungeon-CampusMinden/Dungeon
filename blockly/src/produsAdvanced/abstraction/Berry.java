package produsAdvanced.abstraction;

import contrib.item.Item;
import core.Entity;
import core.components.DrawComponent;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class Berry extends Item {

  public static String DONUT_TEXTURE = "items/resource/donut.png";
  public static String BERRY_TEXTURE = "items/resource/berry.png";

  private static final IPath DEFAULT_TEXTURE = new SimpleIPath(BERRY_TEXTURE);
  private final boolean toxic;

  static {
    Item.registerItem(Berry.class);
  }

  public Berry(boolean toxic) {
    super("Eine Beere.", " Könnte sie giftig sein?", Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.toxic = toxic;
  }

  public void changeTexture(String texture) {
    this.worldAnimation(Animation.fromSingleImage(new SimpleIPath(texture)));
    this.inventoryAnimation(Animation.fromSingleImage(new SimpleIPath(texture)));
    this.
  }

  @Override
  public void use(final Entity user) {}

  public boolean isToxic() {
    return toxic;
  }
}
