package feature.hud.dialogs;

import feature.input.configuration.KeyboardConfig;

/** Shared keyboard mapping for dialogs that advance one sequence step at a time. */
public final class DialogAdvanceInput {

  private DialogAdvanceInput() {}

  /**
   * Returns whether a key should advance a sequenced dialog.
   *
   * <p>The close shortcut is routed separately as a {@link DialogCloseEvent}; it must not also
   * advance a page through the keyboard listener.
   *
   * @param keycode the pressed key code
   * @return {@code true} for the interaction key
   */
  public static boolean isAdvanceKey(int keycode) {
    return keycode == KeyboardConfig.INTERACT_WORLD.value();
  }
}
