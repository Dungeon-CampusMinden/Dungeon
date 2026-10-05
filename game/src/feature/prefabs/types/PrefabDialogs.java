package feature.prefabs.types;

import engine.Entity;
import engine.utils.IVoidFunction;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;

/** Shared dialog behavior of prefabs that show images or dialogs to players. */
final class PrefabDialogs {

  private PrefabDialogs() {}

  /**
   * Shows an image dialog to one player.
   *
   * <p>The image path is resolved through {@link engine.language.Localization#asset(String)} by the
   * displaying client, so a variant for its language (e.g. {@code image_en.png}) is used if it
   * exists, falling back to the given path otherwise.
   *
   * @param imagePath image to show
   * @param who player to show the image to
   * @param onClosed called once the player closed the image
   */
  static void showImage(String imagePath, Entity who, Runnable onClosed) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, imagePath)
            .build();
    UIComponent ui = DialogFactory.show(context, who.id());
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onClosed.run());
  }

  /**
   * Wraps an action so it runs at most once, since a dialog may report being finished and closed.
   *
   * @param action action to run
   * @return callback running the action on its first call only
   */
  static IVoidFunction once(Runnable action) {
    boolean[] done = {false};
    return () -> {
      if (done[0]) return;
      done[0] = true;
      action.run();
    };
  }
}
