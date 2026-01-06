package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.inventory.InventoryGUI;
import contrib.modules.keypad.KeypadUI;
import contrib.utils.AttributeBarUtil;
import contrib.utils.components.showImage.ShowImageUI;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import core.utils.logging.DungeonLogger;
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

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogFactory.class);
  private static final Map<DialogType, Function<DialogContext, Group>> registry = new HashMap<>();

  static {
    register(DialogType.DefaultTypes.OK, OkDialog::build);
    register(DialogType.DefaultTypes.YES_NO, YesNoDialog::build);
    register(DialogType.DefaultTypes.TEXT, TextDialog::build);
    register(DialogType.DefaultTypes.IMAGE, ShowImageUI::build);
    register(DialogType.DefaultTypes.FREE_INPUT, FreeInputDialog::build);
    register(DialogType.DefaultTypes.INVENTORY, InventoryGUI::buildSimple);
    register(DialogType.DefaultTypes.DUAL_INVENTORY, InventoryGUI::buildDual);
    register(DialogType.DefaultTypes.CRAFTING_GUI, CraftingGUI::build);
    register(DialogType.DefaultTypes.KEYPAD, KeypadUI::build);
    register(DialogType.DefaultTypes.PROGRESS_BAR, AttributeBarUtil::buildProgressBar);
    LOGGER.debug("Registered built-in dialog types");
  }

  /**
   * Registers a custom dialog type with the factory.
   *
   * <p>This allows extending the dialog system with new dialog types without modifying existing
   * code. The creator function receives a {@link DialogContext} and must return a fully configured
   * {@link Dialog}.
   *
   * @param type The unique type of the dialog
   * @param creator Function that creates a dialog from a context
   * @throws DialogCreationException if a dialog type with the given name is already registered
   */
  public static void register(DialogType type, Function<DialogContext, Group> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    if (registry.containsKey(type)) {
      throw new DialogCreationException("Dialog type '" + type + "' is already registered");
    }
    registry.put(type, creator);
  }

  /**
   * Creates a dialog of the specified type without displaying it.
   *
   * <p>This method only creates the dialog instance based on the provided context. It does not add
   * it to any UI or manage its lifecycle.
   *
   * @param ctx The context containing all necessary data for dialog creation
   * @return The created dialog instance
   * @throws DialogCreationException if the dialog type is not registered
   */
  public static Group create(DialogContext ctx) {
    Objects.requireNonNull(ctx, "context");
    Function<DialogContext, Group> creator = registry.get(ctx.dialogType());
    if (creator == null) {
      throw new DialogCreationException("Unknown dialog type: " + ctx);
    }
    Group dialog = creator.apply(ctx);
    if (ctx.center()) {
      UIUtils.center(dialog);
    }
    return dialog;
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
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          onConfirm.execute();
          UIUtils.closeDialog(ui, true);
        });

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
          UIUtils.closeDialog(ui, true);
        });
    ui.registerCallback(
        DialogContextKeys.ON_NO,
        data -> {
          onNo.execute();
          UIUtils.closeDialog(ui, true);
        });

    return ui;
  }

  /**
   * Shows a customizable text dialog with optional multiple buttons and custom result handling.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the confirm button is pressed (can be null)
   * @param confirmLabel Label for the confirm button (uses default if null)
   * @param cancelLabel Label for the cancel button (no cancel button if null)
   * @param additionalButtons List of additional button labels (can be null)
   * @param targetEntityIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showTextDialog(
      String text,
      String title,
      IVoidFunction onConfirm,
      String confirmLabel,
      String cancelLabel,
      String[] additionalButtons,
      int... targetEntityIds) {
    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text);
    if (confirmLabel != null) builder.put(DialogContextKeys.CONFIRM_LABEL, confirmLabel);
    if (cancelLabel != null) builder.put(DialogContextKeys.CANCEL_LABEL, cancelLabel);
    if (additionalButtons != null)
      builder.put(DialogContextKeys.ADDITIONAL_BUTTONS, additionalButtons);

    UIComponent ui = show(builder.build(), targetEntityIds);

    // Register callbacks
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
