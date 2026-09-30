package feature.hud.dialogs;

import com.badlogic.gdx.Input;
import feature.input.configuration.KeyboardConfig;

/** Shared keyboard mapping for dialogs that advance one sequence step at a time. */
public final class DialogAdvanceInput {

  private DialogAdvanceInput() {}

  /**
   * Returns whether a key should advance a sequenced dialog.
   *
   * <p>{@code ESC} intentionally behaves like the configured interaction key here. The dialog
   * itself decides whether that means skipping the typewriter, showing the next page, or completing
   * the sequence.
   *
   * @param keycode the pressed key code
   * @return {@code true} for the interaction key or {@code ESC}
   */
  public static boolean isAdvanceKey(int keycode) {
    return keycode == KeyboardConfig.INTERACT_WORLD.value() || keycode == Input.Keys.ESCAPE;
  }
}
