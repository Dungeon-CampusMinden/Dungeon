package core.platform.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import core.utils.InputManager;

/**
 * Bridges libGDX InputProcessor events into the engine-agnostic InputManager.
 */
public final class GdxInputBridge {
  private static InputProcessor installedWrapper;

  private GdxInputBridge() {}

  public static void install() {
    if (Gdx.input == null) return;

    final InputProcessor current = Gdx.input.getInputProcessor();
    if (current == null) return;

    // Avoid stacking wrappers.
    if (current == installedWrapper) return;

    final InputProcessor delegate = current;

    installedWrapper =
      new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
          InputManager.notifyKeyDown(keycode);
          return delegate.keyDown(keycode);
        }

        @Override
        public boolean keyUp(int keycode) {
          InputManager.notifyKeyUp(keycode);
          return delegate.keyUp(keycode);
        }

        @Override
        public boolean keyTyped(char character) {
          return delegate.keyTyped(character);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
          InputManager.notifyButtonDown(button);
          return delegate.touchDown(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
          InputManager.notifyButtonUp(button);
          return delegate.touchUp(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
          InputManager.notifyButtonUp(button);
          return delegate.touchCancelled(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
          return delegate.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
          return delegate.mouseMoved(screenX, screenY);
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
          return delegate.scrolled(amountX, amountY);
        }
      };

    Gdx.input.setInputProcessor(installedWrapper);
  }
}
