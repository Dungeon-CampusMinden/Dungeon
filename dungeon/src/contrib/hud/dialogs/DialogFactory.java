package contrib.hud.dialogs;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.platform.gdx.hud.dialogs.DialogDesign;
import core.Entity;
import core.Game;
import core.ui.UiNodeHandle;
import core.utils.IVoidFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
/**
 * Central factory for creating and displaying dialogs in a unified manner.
 *
 * <p>All dialog creation should go through this factory to ensure consistent behavior and to enable
 * future extensions via the registry system. Dialog classes are package-private to enforce the use
 * of this factory.
 *
 * <p>Usage:
 *
 * <pre>
 * // Show an OK dialog
 * DialogFactory.showOkDialog("Hello World", "Greeting", () -> System.out.println("OK pressed"));
 *
 * // Show a Yes/No dialog
 * DialogFactory.showYesNoDialog("Continue?", "Confirm", () -> continueAction(), () -> cancelAction());
 * </pre>
 *
 * @see DialogContext
 * @see DialogDesign
 */
public class DialogFactory {

  private static final Map<DialogType, Function<DialogContext, UiNodeHandle>> registry = new HashMap<>();

  /**
   * Registers a custom dialog type with the factory.
   *
   * <p>This allows extending the dialog system with new dialog types without modifying existing
   * code. The creator function receives a {@link DialogContext} and must return a fully configured
   * dialog handle.
   *
   * @param type The unique type of the dialog
   * @param creator Function that creates a dialog from a context
   * @throws DialogCreationException if a dialog type with the given name is already registered
   */
  public static void register(DialogType type, Function<DialogContext, UiNodeHandle> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    if (registry.containsKey(type)) {
      throw new DialogCreationException("Dialog type '" + type + "' is already registered");
    }
    registry.put(type, creator);
  }

  /**
   * Creates a dialog of the specified type from the given context.
   *
   * <p>This method looks up the appropriate dialog creator from the registry based on the dialog
   * type specified in the context and instantiates the dialog.
   *
   * @param ctx The context containing all necessary data for dialog creation
   * @return The UiNodeHandle wrapping the created dialog
   * @throws DialogCreationException if the dialog type is not registered
   */
  public static UiNodeHandle create(DialogContext ctx) {
    Objects.requireNonNull(ctx, "context");

    Function<DialogContext, UiNodeHandle> creator = registry.get(ctx.dialogType());
    if (creator == null) {
      throw new DialogCreationException("Unknown dialog type: " + ctx);
    }

    return creator.apply(ctx);
  }

  /**
   * Creates and displays a dialog of the specified type.
   *
   * <p>This method creates the dialog, centers it on screen, wraps it in a UI entity, and adds it
   * to the game. The entity lifecycle is automatically managed. This overload provides fine-grained
   * control over pause behavior and target entities that should be notified of the dialog's
   * closure.
   *
   * @param context The context containing all necessary data for dialog creation
   * @param willPause whether the dialog will pause the game when displayed
   * @param canBeClosed whether the dialog can be closed by the user
   * @param targetEntityIds array of entity IDs to notify when the dialog is closed
   * @return The UIComponent containing the dialog (use to register callbacks)
   * @throws DialogCreationException if the dialog type is not registered or the entity cannot be
   *     found after creation
   */
  public static UIComponent show(
      DialogContext context, boolean willPause, boolean canBeClosed, int[] targetEntityIds) {
    Objects.requireNonNull(context, "context");

    // Determine the owner entity (who holds the UIComponent)
    Entity ownerEntity =
        context
            .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
            .flatMap(Game::findEntityById)
            .orElseGet(
                () -> {
                  // Create a new temp dialog entity
                  Entity newEntity = new Entity("dialog-" + context.dialogType());
                  Game.add(newEntity);
                  return Game.findEntityById(newEntity.id())
                      .orElseThrow(
                          () ->
                              new DialogCreationException(
                                  "Cannot find newly created dialog entity"));
                });

    // Store owner entity ID in context for network sync
    context.owner(ownerEntity.id());

    UIComponent ui = new UIComponent(context, willPause, canBeClosed, targetEntityIds);
    ownerEntity.add(ui);

    return ui;
  }

  /**
   * Creates and displays a dialog of the specified type.
   *
   * <p>This method creates the dialog, centers it on screen, wraps it in a UI entity, and adds it
   * to the game. The entity lifecycle is automatically managed.
   *
   * @param context The context containing all necessary data for dialog creation
   * @param canBeClosed whether the dialog can be closed by the user
   * @return The UIComponent containing the dialog (use to register callbacks)
   * @throws DialogCreationException if the dialog type is not registered or the entity cannot be
   *     found after creation
   */
  public static UIComponent show(final DialogContext context, boolean canBeClosed) {
    return show(context, true, canBeClosed, new int[0]);
  }

  /**
   * Creates and displays a dialog of the specified type that can be closed by the user.
   *
   * @param context The context containing all necessary data for dialog creation
   * @return The UIComponent containing the dialog (use to register callbacks)
   * @throws DialogCreationException if the dialog type is not registered or the entity cannot be
   *     found after creation
   */
  public static UIComponent show(final DialogContext context) {
    return show(context, true);
  }

  /**
   * Creates and displays a dialog of the specified type for specific target entities.
   *
   * @param context The context containing all necessary data for dialog creation
   * @param targetEntityIds array of entity IDs to notify when the dialog is closed
   * @return The UIComponent containing the dialog (use to register callbacks)
   * @throws DialogCreationException if the dialog type is not registered or the entity cannot be
   *     found after creation
   */
  public static UIComponent show(final DialogContext context, int... targetEntityIds) {
    return show(context, true, true, targetEntityIds);
  }

  /**
   * Shows a simple OK dialog with a message and a single confirmation button.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the OK button is pressed
   * @param targetIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showOkDialog(
      String text, String title, IVoidFunction onConfirm, int... targetIds) {
    DialogContext ctx =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.OK)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .build();

    UIComponent ui = show(ctx, targetIds);

    // Register callback
    ui.registerCallback(DialogContextKeys.ON_CONFIRM, data -> UIUtils.closeDialog(ui, true, true));

    // Default onClose behavior (e.g. when pressing ESC)
    ui.onClose(uic -> onConfirm.execute());

    return ui;
  }

  /**
   * Shows a Yes/No confirmation dialog with separate callbacks for each option.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onYes Callback executed when the Yes button is pressed
   * @param onNo Callback executed when the No button is pressed
   * @param targetEntityIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showYesNoDialog(
      String text, String title, IVoidFunction onYes, IVoidFunction onNo, int... targetEntityIds) {
    DialogContext ctx =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.YES_NO)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .build();

    UIComponent ui = show(ctx, targetEntityIds);

    // Register callbacks
    ui.registerCallback(
        DialogContextKeys.ON_YES,
        data -> {
          onYes.execute();
          UIUtils.closeDialog(ui, true, false);
        });
    ui.registerCallback(DialogContextKeys.ON_NO, data -> UIUtils.closeDialog(ui, true, true));

    // Default onClose behavior (e.g. when pressing ESC)
    ui.onClose(uic -> onNo.execute());

    return ui;
  }

    /**
     * Shows a simple text dialog with a message and an optional close button.
     *
     * @param text The message to display in the dialog body
     * @param title The dialog window title
     * @param canClose whether the dialog can be closed by the user
     * @return The {@link UIComponent} containing the dialog
     */
    public static UIComponent showTextDialog(
      final String text, final String title, final boolean canClose) {
      DialogContext ctx =
        DialogContext.builder()
          .type(DialogType.DefaultTypes.TEXT)
          .put(DialogContextKeys.TITLE, title)
          .put(DialogContextKeys.MESSAGE, text)
          .build();

      return show(ctx, canClose);
    }

    /**
     * Shows a simple text dialog with a message and an optional close button.
     *
     * @param text The message to display in the dialog body
     * @param title The dialog window title
     * @param canClose whether the dialog can be closed by the user
     * @param targetEntityIds array of entity IDs to notify when the dialog is closed
     * @return The {@link UIComponent} containing the dialog
     */
    public static UIComponent showTextDialog(
      final String text,
      final String title,
      final boolean canClose,
      final int... targetEntityIds) {
      DialogContext ctx =
        DialogContext.builder()
          .type(DialogType.DefaultTypes.TEXT)
          .put(DialogContextKeys.TITLE, title)
          .put(DialogContextKeys.MESSAGE, text)
          .build();

      return show(ctx, true, canClose, targetEntityIds);
    }

    /**
     * Shows an image dialog with an optional confirmation callback.
     *
     * @param imagePath The path to the image file to display
     * @param title The dialog window title
     * @param onConfirm Callback executed when the dialog is confirmed (nullable)
     * @param targetEntityIds array of entity IDs to notify when the dialog is closed
     * @return The {@link UIComponent} containing the dialog
     */
    public static UIComponent showImageDialog(
      String imagePath, String title, IVoidFunction onConfirm, int... targetEntityIds) {
      DialogContext ctx =
        DialogContext.builder()
          .type(DialogType.DefaultTypes.IMAGE)
          .put(DialogContextKeys.TITLE, title)
          .put(DialogContextKeys.IMAGE, imagePath)
          .build();

      UIComponent ui = show(ctx, targetEntityIds);

      if (onConfirm != null) {
        ui.registerCallback(
          DialogContextKeys.ON_CONFIRM,
          data -> {
            onConfirm.execute();
            UIUtils.closeDialog(ui, true);
          });
      }

      return ui;
    }
}
