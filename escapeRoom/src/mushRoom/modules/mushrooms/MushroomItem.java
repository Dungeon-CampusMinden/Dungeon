package mushRoom.modules.mushrooms;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
// Assuming 'Color' is defined or imported, though it is not needed in MushroomItem itself.

public class MushroomItem {

  private static final int maxStackSize = 10;

  public static Object createMushroomItem(Mushrooms type) {
    return switch (type) {
      case RedYellow -> new RedYellowMushroom();
      case GreenGreen -> new GreenGreenMushroom();
      case GreenBrown -> new GreenBrownMushroom();
      case BlueRed -> new BlueRedMushroom();
      case BlueCyan -> new BlueCyanMushroom();
      case CyanOrange -> new CyanOrangeMushroom();
      case CyanRed -> new CyanRedMushroom();
      case MagentaWhite -> new MagentaWhiteMushroom();
      case MagentaBlack -> new MagentaBlackMushroom();
      case YellowGreen -> new YellowGreenMushroom();
      case YellowMagenta -> new YellowMagentaMushroom();
      default -> new RedBlueMushroom();
    };
  }

  // =========================================================
  // BASE CLASS
  // =========================================================
  public abstract static class BaseMushroom extends Item {

    private final Mushrooms type;

    protected BaseMushroom(Mushrooms type) {
      super(
        type.getBaseName() + " Mushroom",
        "A " + type.getBaseName() + " mushroom",
        new Animation(new SimpleIPath(type.getTexturePath())), // Assumes getTexturePath() exists on Mushrooms enum
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
  public static class RedBlueMushroom extends BaseMushroom {
    public RedBlueMushroom() {
      super(Mushrooms.RedBlue);
    }
  }

  public static class RedYellowMushroom extends BaseMushroom {
    public RedYellowMushroom() {
      super(Mushrooms.RedYellow);
    }
  }

  public static class GreenGreenMushroom extends BaseMushroom {
    public GreenGreenMushroom() {
      super(Mushrooms.GreenGreen);
    }
  }

  public static class GreenBrownMushroom extends BaseMushroom {
    public GreenBrownMushroom() {
      super(Mushrooms.GreenBrown);
    }
  }

  public static class BlueRedMushroom extends BaseMushroom {
    public BlueRedMushroom() {
      super(Mushrooms.BlueRed);
    }
  }

  public static class BlueCyanMushroom extends BaseMushroom {
    public BlueCyanMushroom() {
      super(Mushrooms.BlueCyan);
    }
  }

  public static class CyanOrangeMushroom extends BaseMushroom {
    public CyanOrangeMushroom() {
      super(Mushrooms.CyanOrange);
    }
  }

  public static class CyanRedMushroom extends BaseMushroom {
    public CyanRedMushroom() {
      super(Mushrooms.CyanRed);
    }
  }

  public static class MagentaWhiteMushroom extends BaseMushroom {
    public MagentaWhiteMushroom() {
      super(Mushrooms.MagentaWhite);
    }
  }

  public static class MagentaBlackMushroom extends BaseMushroom {
    public MagentaBlackMushroom() {
      super(Mushrooms.MagentaBlack);
    }
  }

  public static class YellowGreenMushroom extends BaseMushroom {
    public YellowGreenMushroom() {
      super(Mushrooms.YellowGreen);
    }
  }

  public static class YellowMagentaMushroom extends BaseMushroom {
    public YellowMagentaMushroom() {
      super(Mushrooms.YellowMagenta);
    }
  }
}
