package mushRoom.modules.mushrooms;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

public class MushroomItem {

  private static final int maxStackSize = 10;

  public static Object createMushroomItem(Mushrooms type) {
    return switch (type) {
      case Green -> new GreenMushroom();
      case Blue -> new BlueMushroom();
      case Cyan -> new CyanMushroom();
      case Magenta -> new MagentaMushroom();
      case Yellow -> new YellowMushroom();
      default -> new RedMushroom();
    };
  }

  // =========================================================
  // BASE CLASS
  // =========================================================
  public abstract static class BaseMushroom extends Item {

    private final Mushrooms type;

    protected BaseMushroom(Mushrooms type) {
      super(
          type.name() + " Mushroom",
          "A " + type.name() + " mushroom",
          new Animation(new SimpleIPath(type.getTexturePath())),
          new Animation(new SimpleIPath(type.getTexturePath())),
          1,
          maxStackSize);
      this.type = type;
    }

    @Override
    public void use(final Entity user) {
      // Nothing
    }

    public Mushrooms type() {
      return type;
    }
  }

  // =========================================================
  // SUBCLASSES
  // =========================================================
  public static class RedMushroom extends BaseMushroom {
    public RedMushroom() {
      super(Mushrooms.Red);
    }
  }

  public static class GreenMushroom extends BaseMushroom {
    public GreenMushroom() {
      super(Mushrooms.Green);
    }
  }

  public static class BlueMushroom extends BaseMushroom {
    public BlueMushroom() {
      super(Mushrooms.Blue);
    }
  }

  public static class CyanMushroom extends BaseMushroom {
    public CyanMushroom() {
      super(Mushrooms.Cyan);
    }
  }

  public static class MagentaMushroom extends BaseMushroom {
    public MagentaMushroom() {
      super(Mushrooms.Magenta);
    }
  }

  public static class YellowMushroom extends BaseMushroom {
    public YellowMushroom() {
      super(Mushrooms.Yellow);
    }
  }
}
