package core.platform.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import core.game.PreRunConfiguration;
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

  @Override
  public boolean supportsFullscreen() {
    return Gdx.graphics != null;
  }

  @Override
  public boolean isFullscreen() {
    return Gdx.graphics != null && Gdx.graphics.isFullscreen();
  }

  @Override
  public void setFullscreen(boolean fullscreen) {
    if (Gdx.graphics == null) {
      return;
    }

    if (fullscreen) {
      Gdx.graphics.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
    } else {
      Gdx.graphics.setWindowedMode(
        PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
    }
  }
}
