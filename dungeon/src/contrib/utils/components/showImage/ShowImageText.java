package contrib.utils.components.showImage;

import com.badlogic.gdx.graphics.Color;

public class ShowImageText {

  public String text;
  public float scale;
  public Color color;

  public ShowImageText(String text, float scale, Color color) {
    this.text = text;
    this.scale = scale;
    this.color = color;
  }

  public ShowImageText(String text, float scale) {
    this(text, scale, Color.BLACK);
  }

  public ShowImageText(String text) {
    this(text, 1);
  }
}
