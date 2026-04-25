package contrib.hud.dialogs;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import java.util.Objects;

/**
 * Service for attaching dialogs to entities and exposing common dialog flows.
 *
 * @see DialogContext
 * @see DialogType
 */
public final class DialogService {

  private DialogService() {}

  /**
   * Displays a dialog by creating and associating a {@code UIComponent} with an entity.
   *
   * @param context The {@code DialogContext} containing metadata and state for the dialog. Must not
   *     be null.
   * @param willPause Determines if the game or application flow should pause when this dialog is
   *     shown.
   * @param canBeClosed Indicates whether the dialog can be closed by user interaction.
   * @param targetEntityIds An array of entity IDs that the dialog may target or affect.
   * @return The created {@code UIComponent} associated with the dialog entity.
   * @throws NullPointerException if {@code context} is null.
   * @throws DialogCreationException if the dialog entity cannot be found or created.
   */
  public static UIComponent show(
      DialogContext context, boolean willPause, boolean canBeClosed, int[] targetEntityIds) {
    Objects.requireNonNull(context, "context");

    Entity ownerEntity =
        context
            .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
            .flatMap(Game::findEntityById)
            .orElseGet(
                () -> {
                  Entity newEntity = new Entity("dialog-" + context.dialogType());
                  Game.add(newEntity);
                  return Game.findEntityById(newEntity.id())
                      .orElseThrow(
                          () ->
                              new DialogCreationException(
                                  "Cannot find newly created dialog entity"));
                });

    context.owner(ownerEntity.id());

    UIComponent ui = new UIComponent(context, willPause, canBeClosed, targetEntityIds);
    ownerEntity.add(ui);
    return ui;
  }

  /**
   * Displays a dialog by creating and associating a {@code UIComponent} with an entity.
   *
   * @param context The {@code DialogContext} containing metadata and state for the dialog. Must not
   *     be null.
   * @param targetEntityIds An array of entity IDs that the dialog may target or affect.
   * @return The created {@code UIComponent} associated with the dialog entity.
   * @throws NullPointerException if {@code context} is null.
   * @throws DialogCreationException if the dialog entity cannot be found or created.
   */
  public static UIComponent show(final DialogContext context, int... targetEntityIds) {
    return show(context, true, true, targetEntityIds);
  }

  /**
   * Displays a simple "OK" dialog with a specified message and title, allowing the user to confirm
   * the dialog. The dialog is associated with the specified target entities.
   *
   * @param text The message to be displayed in the dialog.
   * @param title The title of the dialog.
   * @param onConfirm A callback function executed when the user confirms the dialog by pressing
   *     "OK".
   * @param targetIds Optional target entity IDs that the dialog may target or be associated with.
   * @return The {@code UIComponent} representing the created dialog.
   */
  public static UIComponent showOkDialog(
      String text, String title, IVoidFunction onConfirm, int... targetIds) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.OK)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .build();

    UIComponent ui = show(context, targetIds);

    ui.registerCallback(DialogContextKeys.ON_CONFIRM, _ -> UIUtils.closeDialog(ui, true, true));
    ui.onClose(_ -> onConfirm.execute());

    return ui;
  }

  /**
   * Displays a "Yes/No" dialog with a specified message and title, allowing the user to respond.
   * The dialog invokes the provided callback functions based on the user's response and is
   * associated with the specified target entities.
   *
   * @param text The message to be displayed in the dialog.
   * @param title The title of the dialog.
   * @param onYes A callback function executed when the user selects "Yes".
   * @param onNo A callback function executed when the user selects "No" or when the dialog is
   *     closed without choosing "Yes".
   * @param targetEntityIds Optional target entity IDs that the dialog may target or be associated
   *     with.
   */
  public static void showYesNoDialog(
      String text, String title, IVoidFunction onYes, IVoidFunction onNo, int... targetEntityIds) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.YES_NO)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .build();

    UIComponent ui = show(context, targetEntityIds);

    ui.registerCallback(
        DialogContextKeys.ON_YES,
        _ -> {
          onYes.execute();
          UIUtils.closeDialog(ui, true, false);
        });
    ui.registerCallback(DialogContextKeys.ON_NO, _ -> UIUtils.closeDialog(ui, true, true));
    ui.onClose(_ -> onNo.execute());
  }
}
