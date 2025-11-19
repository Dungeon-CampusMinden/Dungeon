package mushRoom.modules.mushrooms;

import com.badlogic.gdx.graphics.Color;
import core.utils.components.draw.shader.AbstractShader;
import core.utils.components.draw.shader.HueRemapShader;

public enum Mushrooms {
  Red(false),
  Green(false),
  Blue(false),
  Cyan(true),
  Magenta(true),
  Yellow(true),
  ;

  public final boolean poisonous;

  Mushrooms(boolean poisonous) {
    this.poisonous = poisonous;
  }

  AbstractShader getShader() {
    return switch (this) {
      case Green -> new HueRemapShader(0, 0.333f);
      case Blue -> new HueRemapShader(0, 0.666f);
      case Cyan -> new HueRemapShader(0, 0.5f);
      case Magenta -> new HueRemapShader(0, 0.833f);
      case Yellow -> new HueRemapShader(0, 0.166f);
      default -> new HueRemapShader(0, 0);
    };
  }

  public String getTexturePath() {
    return "@gen/mushrooms/" + this.name().toLowerCase() + ".png";
  }

  public Color getColor() {
    return switch (this) {
      case Red -> Color.RED;
      case Green -> Color.GREEN;
      case Blue -> Color.BLUE;
      case Cyan -> Color.CYAN;
      case Magenta -> Color.MAGENTA;
      case Yellow -> Color.YELLOW;
    };
  }
}
