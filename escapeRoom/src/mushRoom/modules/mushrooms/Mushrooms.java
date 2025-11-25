package mushRoom.modules.mushrooms;

import com.badlogic.gdx.graphics.Color;

public enum Mushrooms {
  RedBlue(true, Color.RED, Color.BLUE),
  RedYellow(false, Color.RED, Color.YELLOW),
  GreenGreen(true, Color.GREEN, Color.GREEN),
  GreenBrown(false, Color.GREEN, new Color(0.36f, 0.26f, 0.2f, 1f)),
  BlueRed(false, Color.BLUE, Color.RED),
  BlueCyan(true, Color.BLUE, Color.CYAN),
  CyanOrange(true, Color.CYAN, Color.ORANGE),
  CyanRed(false, Color.CYAN, Color.RED),
  MagentaWhite(false, Color.MAGENTA, Color.WHITE),
  MagentaBlack(true, Color.MAGENTA, Color.BLACK),
  YellowGreen(true, Color.YELLOW, Color.GREEN),
  YellowMagenta(false, Color.YELLOW, Color.MAGENTA),
  ;

  public final boolean poisonous;
  public final Color baseColor;
  public final Color outlineColor;

  Mushrooms(boolean poisonous, Color baseColor, Color outlineColor) {
    this.poisonous = poisonous;
    this.baseColor = baseColor;
    this.outlineColor = outlineColor;
  }

  public String getTexturePath() {
    return "@gen/mushrooms/" + this.name().toLowerCase() + ".png";
  }

  public Color getColor() {
    return baseColor;
  }

  public Color getOutlineColor() {
    return outlineColor;
  }

  public String getBaseName() {
    String name = this.name();
    for (int i = 1; i < name.length(); i++) {
      if (Character.isUpperCase(name.charAt(i))) {
        return name.substring(0, i);
      }
    }
    return name;
  }
}
