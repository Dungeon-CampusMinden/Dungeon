package mushRoom.modules.mushrooms;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import mushRoom.Sounds;

/** Utility class that provides factory methods and item implementations for mushrooms. */
public class MushroomItem {

  private static final int maxStackSize = 10;

  /**
   * Create an Item instance for the given mushroom type.
   *
   * @param type the mushroom type to create.
   * @return the created Item for the given type.
   */
  public static Item createMushroomItem(Mushrooms type) {
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

  /** Base class for mushroom items that holds common behavior and data. */
  public abstract static class BaseMushroom extends Item {

    private final Mushrooms type;

    protected BaseMushroom(Mushrooms type) {
      super(
          type.displayName(),
          type.descriptionShort(),
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

    @Override
    public boolean collect(Entity itemEntity, Entity collector) {
      Sounds.MUSHROOM_PICKUP_SOUND.play();
      return super.collect(itemEntity, collector);
    }

    /**
     * Return the mushroom type represented by this item.
     *
     * @return the Mushrooms enum value for this item.
     */
    public Mushrooms type() {
      return type;
    }
  }

  /** RedBlue mushroom item. */
  public static class RedBlueMushroom extends BaseMushroom {
    /** Creates a new RedBlueMushroom item. */
    public RedBlueMushroom() {
      super(Mushrooms.RedBlue);
    }
  }

  /** RedYellow mushroom item. */
  public static class RedYellowMushroom extends BaseMushroom {
    /** Creates a new RedYellowMushroom item. */
    public RedYellowMushroom() {
      super(Mushrooms.RedYellow);
    }
  }

  /** GreenGreen mushroom item. */
  public static class GreenGreenMushroom extends BaseMushroom {
    /** Creates a new GreenGreenMushroom item. */
    public GreenGreenMushroom() {
      super(Mushrooms.GreenGreen);
    }
  }

  /** GreenBrown mushroom item. */
  public static class GreenBrownMushroom extends BaseMushroom {
    /** Creates a new GreenBrownMushroom item. */
    public GreenBrownMushroom() {
      super(Mushrooms.GreenBrown);
    }
  }

  /** BlueRed mushroom item. */
  public static class BlueRedMushroom extends BaseMushroom {
    /** Creates a new BlueRedMushroom item. */
    public BlueRedMushroom() {
      super(Mushrooms.BlueRed);
    }
  }

  /** BlueCyan mushroom item. */
  public static class BlueCyanMushroom extends BaseMushroom {
    /** Creates a new BlueCyanMushroom item. */
    public BlueCyanMushroom() {
      super(Mushrooms.BlueCyan);
    }
  }

  /** CyanOrange mushroom item. */
  public static class CyanOrangeMushroom extends BaseMushroom {
    /** Creates a new CyanOrangeMushroom item. */
    public CyanOrangeMushroom() {
      super(Mushrooms.CyanOrange);
    }
  }

  /** CyanRed mushroom item. */
  public static class CyanRedMushroom extends BaseMushroom {
    /** Creates a new CyanRedMushroom item. */
    public CyanRedMushroom() {
      super(Mushrooms.CyanRed);
    }
  }

  /** MagentaWhite mushroom item. */
  public static class MagentaWhiteMushroom extends BaseMushroom {
    /** Creates a new MagentaWhiteMushroom item. */
    public MagentaWhiteMushroom() {
      super(Mushrooms.MagentaWhite);
    }
  }

  /** MagentaBlack mushroom item. */
  public static class MagentaBlackMushroom extends BaseMushroom {
    /** Creates a new MagentaBlackMushroom item. */
    public MagentaBlackMushroom() {
      super(Mushrooms.MagentaBlack);
    }
  }

  /** YellowGreen mushroom item. */
  public static class YellowGreenMushroom extends BaseMushroom {
    /** Creates a new YellowGreenMushroom item. */
    public YellowGreenMushroom() {
      super(Mushrooms.YellowGreen);
    }
  }

  /** YellowMagenta mushroom item. */
  public static class YellowMagentaMushroom extends BaseMushroom {
    /** Creates a new YellowMagentaMushroom item. */
    public YellowMagentaMushroom() {
      super(Mushrooms.YellowMagenta);
    }
  }
}
