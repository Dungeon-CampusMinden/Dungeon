package core.platform.litiengine;

import core.platform.RuntimeAdapter;

/**
 * Runtime adapter for the LITIENGINE backend.
 *
 * <p>Maps {@link #requestExit()} to {@code de.gurkenlabs.litiengine.Game.exit()}.
 */
public final class LitiengineRuntimeAdapter implements RuntimeAdapter {

  @Override
  public void requestExit() {
    de.gurkenlabs.litiengine.Game.exit();
  }

  @Override
  public boolean isHeadless() {
    // LITIENGINE provides a "no GUI mode" flag.
    // If no GUI is shown, we treat it as headless.
    return de.gurkenlabs.litiengine.Game.isInNoGUIMode();
  }
}
