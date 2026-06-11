package modules.usbstick;

/** Enum representing all USB stick color variants used by the game. */
public enum UsbStickColor {
  /** Red USB stick. */
  Red("Red USB Stick", "A red USB stick.", "items/usb-side-red.png"),

  /** Green USB stick. */
  Green("Green USB Stick", "A green USB stick.", "items/usb-side-green.png"),

  /** Yellow USB stick. */
  Yellow("Yellow USB Stick", "A yellow USB stick.", "items/usb-side-yellow.png"),

  /** Blue USB stick. */
  Blue("Blue USB Stick", "A blue USB stick.", "items/usb-side-blue.png");

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
