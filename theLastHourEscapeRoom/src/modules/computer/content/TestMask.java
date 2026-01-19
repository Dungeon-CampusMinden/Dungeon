package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import core.utils.components.draw.TextureGenerator;

public class TestMask extends ComputerTab {

  public TestMask(String key, String title, boolean closeable, Color color) {
    super(key, title, closeable);
    Drawable mask = new TextureRegionDrawable(TextureGenerator.generateColorTexture(1, 1, color));
    this.setBackground(mask);
  }

  @Override
  protected void createActors() {
  }
}
