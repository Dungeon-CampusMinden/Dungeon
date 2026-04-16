package modules.usbstick;

import contrib.item.Item;
import core.Entity;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;

/** Utility class that provides factory methods and item implementations for USB sticks. */
public class UsbStickItem {

  /**
   * Create an Item instance for the given USB stick color.
   *
   * @param color the USB stick color to create.
   * @return the created Item for the given color.
   */
  public static Item createUsbStickItem(UsbStickColor color) {
    return switch (color) {
      case Red -> new RedUsbStick();
      case Green -> new GreenUsbStick();
      case Yellow -> new YellowUsbStick();
      case Blue -> new BlueUsbStick();
    };
  }

  /** Base class for USB stick items that holds common behavior and data. */
  public abstract static class BaseUsbStick extends Item {

    private final UsbStickColor color;

    protected BaseUsbStick(UsbStickColor color) {
      super(
          color.displayName(),
          color.description(),
          new Animation(new SimpleIPath(color.getTexturePath())),
          new Animation(new SimpleIPath(color.getTexturePath())));
      this.color = color;
    }

    @Override
    public void use(final Entity user) {
      // Do nothing for now.
    }

    /**
     * Return the color represented by this USB stick item.
     *
     * @return the UsbStickColor enum value for this item.
     */
    public UsbStickColor color() {
      return color;
    }
  }

  /** Red USB stick item. */
  public static class RedUsbStick extends BaseUsbStick {
    /** Creates a new RedUsbStick item. */
    public RedUsbStick() {
      super(UsbStickColor.Red);
    }
  }

  /** Green USB stick item. */
  public static class GreenUsbStick extends BaseUsbStick {
    /** Creates a new GreenUsbStick item. */
    public GreenUsbStick() {
      super(UsbStickColor.Green);
    }
  }

  /** Yellow USB stick item. */
  public static class YellowUsbStick extends BaseUsbStick {
    /** Creates a new YellowUsbStick item. */
    public YellowUsbStick() {
      super(UsbStickColor.Yellow);
    }
  }

  /** Blue USB stick item. */
  public static class BlueUsbStick extends BaseUsbStick {
    /** Creates a new BlueUsbStick item. */
    public BlueUsbStick() {
      super(UsbStickColor.Blue);
    }
  }
}
