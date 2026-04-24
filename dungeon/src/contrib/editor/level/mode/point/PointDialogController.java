package contrib.editor.level.mode.point;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.utils.InputManager;
import core.utils.Point;

/**
 * Controller responsible for managing the "Add Named Point" dialog in the application.
 * It integrates with the {@code PointPlacementController} to add named points to a system
 * and handles the UI interactions for the dialog.
 *
 * <p>This class provides methods to open, check, and close the dialog, ensuring that
 * the user can input a name for a point to be placed at a specified location.
 *
 * <p>It also manages communication between the user interface and the backend logic
 * to handle point placement.
 */
final class PointDialogController {
  private final PointPlacementController placementController;

  private UIComponent addPointDialog;

  PointDialogController(PointPlacementController placementController) {
    this.placementController = placementController;
  }

  boolean isOpen() {
    return addPointDialog != null && addPointDialog.isVisible();
  }

  void openAddNamedPointDialog(Point snapPos) {
    InputManager.consumeTypedCharacters();

    if (isOpen()) {
      return;
    }

    Entity player = Game.player().orElse(null);
    DialogContext context =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.FREE_INPUT)
        .put(DialogContextKeys.TITLE, "Add Named Point")
        .put(DialogContextKeys.QUESTION, "Name of new point")
        .build();

    UIComponent dialogUI =
      player != null ? DialogFactory.show(context, player.id()) : DialogFactory.show(context);

    InputManager.consumeTypedCharacters();
    addPointDialog = dialogUI;

    dialogUI.registerCallback(
      DialogContextKeys.INPUT_CALLBACK,
      data -> {
        if (data instanceof String string && !string.isBlank()) {
          placementController.addPoint(string, snapPos);
        }

        UIUtils.closeDialog(dialogUI, true);
        InputManager.consumeTypedCharacters();
        addPointDialog = null;
      });

    dialogUI.registerCallback(
      DialogContextKeys.ON_CANCEL,
      _ -> {
        UIUtils.closeDialog(dialogUI, true);
        addPointDialog = null;
      });

    dialogUI.onClose(_ -> addPointDialog = null);
  }

  void close() {
    if (addPointDialog == null) {
      return;
    }

    UIUtils.closeDialog(addPointDialog, true);
    InputManager.consumeTypedCharacters();
    addPointDialog = null;
  }
}
