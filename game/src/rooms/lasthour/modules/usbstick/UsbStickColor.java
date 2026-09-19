package rooms.lasthour.modules.usbstick;

import rooms.lasthour.util.translation.TranslationKey;

/** Enum representing all USB stick color variants used by the game. */
public enum UsbStickColor {
  /** Red USB stick. */
  Red(
      TranslationKey.RedUSBStickName,
      TranslationKey.RedUSBStickDescription,
      "items/usb-side-red.png"),

  /** Green USB stick. */
  Green(
      TranslationKey.GreenUSBStickName,
      TranslationKey.GreenUSBStickDescription,
      "items/usb-side-green.png"),

  /** Yellow USB stick. */
  Yellow(
      TranslationKey.YellowUSBStickName,
      TranslationKey.YellowUSBStickDescription,
      "items/usb-side-yellow.png"),

  /** Blue USB stick. */
  Blue(
      TranslationKey.BlueUSBStickName,
      TranslationKey.BlueUSBStickDescription,
      "items/usb-side-blue.png");

  private final String displayName;
  private final String description;
  private final String texturePath;

  /**
   * Create a UsbStickColor enum element.
   *
   * @param displayName the display name of the USB stick
   * @param description short description of the USB stick
   * @param texturePath the texture path for the USB stick image
   */
  UsbStickColor(String displayName, String description, String texturePath) {
    this.displayName = displayName;
    this.description = description;
    this.texturePath = texturePath;
  }

  /**
   * Returns the display name of this USB stick color.
   *
   * @return the display name
   */
  public String displayName() {
    return displayName;
  }

  /**
   * Returns the short description for this USB stick color.
   *
   * @return the short description
   */
  public String description() {
    return description;
  }

  /**
   * Returns the texture path for this USB stick color.
   *
   * @return the texture path
   */
  public String getTexturePath() {
    return texturePath;
  }
}
