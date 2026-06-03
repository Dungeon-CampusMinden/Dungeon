package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.inventory.InventoryGUI;
import contrib.modules.keypad.KeypadUI;
import contrib.modules.puzzle.PuzzleDialog;
import contrib.utils.AttributeBarUtil;
import contrib.utils.components.showImage.ShowImageUI;
import core.Entity;
import core.Game;
import core.game.PreRunConfiguration;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.IVoidFunction;
import core.utils.logging.DungeonLogger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
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
 * <pre>{@code
 * // Show an OK dialog
 * DialogFactory.showOkDialog("Hello World", "Greeting", () -> System.out.println("OK pressed"));
 *
 * // Show a Yes/No dialog
 * DialogFactory.showYesNoDialog("Continue?", "Confirm", () -> continueAction(), () -> cancelAction());
 * }</pre>
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
    register(DialogType.DefaultTypes.PAUSE_MENU, PauseDialog::build);
    register(DialogType.DefaultTypes.MULTIPLE_CHOICE, MultipleChoiceDialog::build);
    register(DialogType.DefaultTypes.DIALOG_DIALOG, DialogDialog::build);
    register(DialogType.DefaultTypes.PUZZLE, PuzzleDialog::build);
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
   */
  public static void register(DialogType type, Function<DialogContext, Group> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    registry.put(type, creator);
  }

  /**
   * Returns an unmodifiable view of all currently registered {@link DialogType}s.
   *
   * @return a {@link Set} containing every registered dialog type
   */
  public static Set<DialogType> registeredTypes() {
    return Collections.unmodifiableSet(registry.keySet());
  }

  /**
   * Creates a dialog of the specified type without displaying it.
   *
   * <p>This method only creates the dialog instance based on the provided context. It does not add
   * it to any UI or manage its lifecycle.
   *
   * @param ctx The context containing all necessary data for dialog creation
   * @return The created dialog instance
   * @throws DialogCreationException if the dialog type is not registered or creation fails
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
      DialogContext context, boolean willPause, boolean canBeClosed, int... targetEntityIds) {
    Objects.requireNonNull(context, "context");

    // Determine the owner entity (who holds the UIComponent)
    Entity ownerEntity =
        context
            .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
            .flatMap(Game::findEntityById)
            .orElseGet(
                () -> {
                  // Create a new temp dialog entity
                  Entity newEntity;
                  if (PreRunConfiguration.isNetworkServer()) {
                    newEntity = new Entity("dialog-" + context.dialogType());
                  } else {
                    newEntity = Entity.createLocalEntity("dialog-" + context.dialogType());
                  }
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
    return show(context, true, canBeClosed);
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
   * @param title The dialog window title. Leave blank or null for no title.
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
          UIUtils.closeDialog(ui);
        });

    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onConfirm.execute());

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
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(
        DialogContextKeys.ON_NO,
        data -> {
          onNo.execute();
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onNo.execute());

    return ui;
  }

  /**
   * Shows a dialog for a text message. Similar to an OK dialog, but designed for bigger texts that
   * need scrolling.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param onConfirm Callback executed when the confirm button is pressed
   * @param confirmLabel Label for the confirm button (uses default if null)
   * @param targetEntityIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showTextDialog(
      String text,
      String title,
      IVoidFunction onConfirm,
      String confirmLabel,
      int... targetEntityIds) {
    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text);
    if (confirmLabel != null) builder.put(DialogContextKeys.CONFIRM_LABEL, confirmLabel);

    UIComponent ui = show(builder.build(), targetEntityIds);

    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          onConfirm.execute();
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onConfirm.execute());

    return ui;
  }

  /**
   * Shows a dialog for a text message. Similar to an OK dialog, but designed for bigger texts that
   * need scrolling.
   *
   * @param text The message to display in the dialog body
   * @param title The dialog window title
   * @param inputPrefill The pre-filled text in the input field
   * @param inputPlaceholder The placeholder text for the input field
   * @param onConfirm Callback executed when the confirm button is pressed
   * @param confirmLabel Label for the confirm button (uses default if null)
   * @param cancelLabel Label for the cancel button (uses default if null)
   * @param onCancel Callback executed when the cancel button is pressed
   * @param targetEntityIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showInputDialog(
      String text,
      String title,
      String inputPrefill,
      String inputPlaceholder,
      String confirmLabel,
      String cancelLabel,
      Consumer<DialogResponseMessage.Payload> onConfirm,
      IVoidFunction onCancel,
      int... targetEntityIds) {
    Objects.requireNonNull(onConfirm, "onConfirm callback cannot be null");
    Objects.requireNonNull(onCancel, "onCancel callback cannot be null");
    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.FREE_INPUT)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .put(DialogContextKeys.INPUT_PREFILL, inputPrefill)
            .put(DialogContextKeys.INPUT_PLACEHOLDER, inputPlaceholder)
            .put(DialogContextKeys.CONFIRM_LABEL, confirmLabel)
            .put(DialogContextKeys.CANCEL_LABEL, cancelLabel);

    UIComponent ui = show(builder.build(), targetEntityIds);

    // Register callbacks
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          onConfirm.accept(data);
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(
        DialogContextKeys.ON_CANCEL,
        data -> {
          onCancel.execute();
          UIUtils.closeDialog(ui);
        });

    return ui;
  }

  /**
   * Shows a multiple choice dialog with a script-driven question (see {@link DialogScript} for the
   * supported {@code [speaker]} / {@code [speaker clear]} / {@code [p]} markup) and a list of
   * selectable options.
   *
   * <p>The {@code dialog} string is parsed exactly like for {@link #showDialogDialog}: it can
   * contain multiple speaker pages separated by {@code [p]} and {@code [speaker ...]} tags. The
   * selectable options appear once the script has finished playing and the last page's typewriter
   * has fully revealed its text.
   *
   * @param dialog The dialog script (non-blank). May contain {@code [speaker]}/{@code [p]} markup.
   * @param title The dialog window title (may be blank for no title)
   * @param options The list of {@link ChoiceOption}s to choose from
   * @param canCancel Whether to append a cancel option
   * @param onSelected Callback receiving the selected option value as a {@link
   *     DialogResponseMessage.Payload}
   * @param onCancel Callback executed when cancel is pressed (only relevant if canCancel is true)
   * @param targetEntityIds The target entity IDs for which the dialog is displayed
   * @return The {@link UIComponent} containing the dialog
   */
  public static UIComponent showMultipleChoiceDialog(
      String dialog,
      String title,
      List<ChoiceOption> options,
      boolean canCancel,
      Consumer<DialogResponseMessage.Payload> onSelected,
      IVoidFunction onCancel,
      int... targetEntityIds) {
    Objects.requireNonNull(dialog, "dialog string cannot be null");
    if (dialog.isBlank()) {
      throw new IllegalArgumentException("dialog string cannot be blank");
    }
    Objects.requireNonNull(options, "options list cannot be null");
    Objects.requireNonNull(onSelected, "onSelected callback cannot be null");

    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.MULTIPLE_CHOICE)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.DIALOG, dialog)
            .put(DialogContextKeys.OPTIONS, new ChoiceOptions(options))
            .put(DialogContextKeys.CAN_CANCEL, canCancel);

    UIComponent ui = show(builder.build(), targetEntityIds);

    ui.registerCallback(
        DialogContextKeys.ON_OPTION_SELECTED,
        data -> {
          onSelected.accept(data);
          UIUtils.closeDialog(ui);
        });

    if (canCancel && onCancel != null) {
      ui.registerCallback(
          DialogContextKeys.ON_CANCEL,
          data -> {
            onCancel.execute();
            UIUtils.closeDialog(ui);
          });
    }
    return ui;
  }

  /**
   * Shows a sequenced speaker dialogue (NPC dialog) from a single script string.
   *
   * <p>The {@code dialog} string is split into pages by the {@code [p]} tag. Each page may begin
   * with an optional {@code [speaker img=<path> name="<displayName>"]} tag that sets the speaker
   * portrait and display name for that page. If a page omits the tag, it inherits the speaker from
   * the previous page (the very first page without a tag has no speaker, in which case its
   * portrait/name column is omitted entirely). If a page includes a speaker tag, unspecified
   * speaker parameters on that tag fall back to a default placeholder image and no name. The
   * special form {@code [speaker clear]} resets the speaker to "no speaker" for this and subsequent
   * pages until another {@code [speaker]} tag is seen. Pages are shown one after another. The user
   * advances by clicking anywhere on the dialog or pressing the configured interact key. While a
   * typewriter reveal is still running, the advance instead skips to the end of the current text.
   * After the last page has been confirmed, {@code onFinished} is invoked and the dialog is closed.
   *
   * @param dialog The non-empty dialog script.
   * @param onFinished Callback executed after the last page has been confirmed.
   * @param targetEntityIds The target entity IDs for which the dialog is displayed.
   * @return The {@link UIComponent} containing the dialog.
   */
  public static UIComponent showDialogDialog(
      String dialog, IVoidFunction onFinished, int... targetEntityIds) {
    Objects.requireNonNull(dialog, "dialog string cannot be null");
    if (dialog.isBlank()) {
      throw new IllegalArgumentException("dialog string cannot be blank");
    }
    Objects.requireNonNull(onFinished, "onFinished callback cannot be null");

    DialogContext ctx =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.DIALOG_DIALOG)
            .put(DialogContextKeys.DIALOG, dialog)
            .build();

    UIComponent ui = show(ctx, targetEntityIds);

    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          onFinished.execute();
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onFinished.execute());

    return ui;
  }
}
