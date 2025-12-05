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
import core.components.PlayerComponent;
import core.utils.IVoidFunction;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
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
 *
 * // Show a task dialog with automatic grading
 * DialogFactory.showTaskYesNoDialog(myTask);
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
   * Registers a custom dialog dialogType with the factory.
   *
   * <p>This allows extending the dialog system with new dialog types without modifying existing
   * code. The creator function receives a {@link DialogContext} and must return a fully configured
   * {@link Dialog}.
   *
   * @param type The unique dialogType of the dialog
   * @param creator Function that creates a dialog from a context
   * @throws DialogCreationException if a dialog dialogType with the given name is already
   *     registered
   */
  public static void register(DialogType type, Function<DialogContext, Group> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    if (registry.containsKey(type)) {
      throw new DialogCreationException("Dialog dialogType '" + type + "' is already registered");
    }
    registry.put(type, creator);
  }

  /**
   * Creates a dialog of the specified dialogType without displaying it.
   *
   * <p>This method only creates the dialog instance based on the provided context. It does not add
   * it to any UI or manage its lifecycle.
   *
   * @param context The context containing all necessary data for dialog creation
   * @return The created dialog instance
   * @throws DialogCreationException if the dialog dialogType is not registered
   */
  public static Group create(DialogContext context) {
    Objects.requireNonNull(context, "context");
    Function<DialogContext, Group> creator = registry.get(context.dialogType());
    if (creator == null) {
      throw new DialogCreationException(
          "Unknown dialog dialogType: " + context.dialogType().type());
    }
    Group dialog = creator.apply(context);
    if (context.center()) {
      UIUtils.center(dialog);
    }
    return dialog;
  }

  /**
   * Creates and displays a dialog of the specified dialogType.
   *
   * <p>This method creates the dialog, centers it on screen, wraps it in a UI entity, and adds it
   * to the game. The entity lifecycle is automatically managed.
   *
   * @param context The context containing all necessary data for dialog creation
   * @return The entity containing the dialog UI component
   * @throws DialogCreationException if the dialog dialogType is not registered or the entity cannot
   *     be found after creation
   */
  public static Entity show(final DialogContext context) {
    Objects.requireNonNull(context, "context");

    Entity newEntity = new Entity("dialog-" + context.dialogType());
    Game.add(newEntity);

    Entity entity =
        Game.findEntityById(newEntity.id())
            .orElseThrow(
                () -> new DialogCreationException("Cannot find newly created dialog entity"));

    DialogContext effectiveContext =
        context.toBuilder().put(DialogContextKeys.ENTITY, entity.id()).build();
    showDialog(effectiveContext, entity, true, new int[0]);
    Game.add(entity);
    return entity;
  }

  /**
   * Show the given dialog on the screen for the specified target entities.
   *
   * @param dialogContext the context that defines the dialog to be shown
   * @param entity the entity on which the dialog is being stored
   * @param willPause whether the dialog should pause the game or not
   * @param targetEntityIds the target entity ids this UI should be shown for (e.g. for inventory
   *     UIs). Empty array for all entities.
   */
  private static void showDialog(
      DialogContext dialogContext, Entity entity, boolean willPause, int[] targetEntityIds) {
    // displays this dialog, caches the dialog callback, and increments and decrements the dialog
    Game.player()
        .flatMap(player -> player.fetch(PlayerComponent.class))
        .ifPresentOrElse(
            playerPC -> {
              // counter so that the inventory is not opened while the dialog is displayed
              playerPC.incrementOpenDialogs();

              UIComponent ui = new UIComponent(dialogContext, willPause, targetEntityIds);
              IVoidFunction oldOnClose = ui.onClose();

              ui.onClose(
                  () -> {
                    playerPC.decrementOpenDialogs();
                    oldOnClose.execute();
                  });

              entity.add(ui);
            },
            () -> LOGGER.warn("No player entity found to show dialog."));
  }

  /**
   * Shows a simple OK dialog with a message and a single confirmation button.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the OK button is pressed
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Entity showOkDialog(String text, String title, IVoidFunction onConfirm) {
    return show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.OK)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .putCallback(DialogContextKeys.ON_CONFIRM, onConfirm)
            .build());
  }

  /**
   * Shows a Yes/No confirmation dialog with separate callbacks for each option.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onYes Callback executed when the Yes button is pressed
   * @param onNo Callback executed when the No button is pressed
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Entity showYesNoDialog(
      String text, String title, IVoidFunction onYes, IVoidFunction onNo) {
    return show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.YES_NO)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .putCallback(DialogContextKeys.ON_YES, onYes)
            .putCallback(DialogContextKeys.ON_NO, onNo)
            .build());
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
   * @param customHandler Custom result handler for button clicks (can be null)
   * @return A tuple containing the entity (with lifecycle management) and the dialog instance
   */
  public static Entity showTextDialog(
      String text,
      String title,
      IVoidFunction onConfirm,
      String confirmLabel,
      String cancelLabel,
      String[] additionalButtons,
      BiFunction<Dialog, String, Boolean> customHandler) {
    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text);
    if (onConfirm != null) builder.putCallback(DialogContextKeys.ON_CONFIRM, onConfirm);
    if (confirmLabel != null) builder.put(DialogContextKeys.CONFIRM_LABEL, confirmLabel);
    if (cancelLabel != null) builder.put(DialogContextKeys.CANCEL_LABEL, cancelLabel);
    if (additionalButtons != null)
      builder.put(DialogContextKeys.ADDITIONAL_BUTTONS, additionalButtons);
    if (customHandler != null) builder.putCallback(DialogContextKeys.RESULT_HANDLER, customHandler);
    return show(builder.build());
  }
}
