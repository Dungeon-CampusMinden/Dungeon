package core.platform.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import core.platform.WindowAdapter;
import java.util.Optional;

public final class GdxWindowAdapter implements WindowAdapter {

  @Override
  public int width() {
    return Optional.ofNullable(Gdx.graphics).map(Graphics::getWidth).orElse(0);
  }

  @Override
  public int height() {
    return Optional.ofNullable(Gdx.graphics).map(Graphics::getHeight).orElse(0);
  }

  @Override
  public void setTitle(String title) {
    if (Gdx.graphics != null) {
      Gdx.graphics.setTitle(title);
    }
  }
}
