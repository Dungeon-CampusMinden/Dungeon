package contrib.hud.dialogs;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.ui.UiHandle;
import core.utils.IVoidFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Central factory for creating and displaying dialogs in a unified manner.
 *
 * <p>All dialog creation should go through this factory to ensure consistent behavior and to enable
 * future extensions via the registry system.
 *
 * <p>Backends may either register new dialog creators, register weak fallbacks, or replace existing
 * creators with more specific implementations.
 *
 * @see DialogContext
 * @see DialogType
 */
public class DialogFactory {

  private static final Map<DialogType, Function<DialogContext, UiHandle>> registry =
    new HashMap<>();

  /**
   * Registers a custom dialog type with the factory.
   *
   * <p>This method is strict: if the dialog type is already registered, an exception is thrown.
   *
   * @param type The unique type of the dialog
   * @param creator Function that creates a dialog from a context
   * @throws DialogCreationException if a dialog type with the given name is already registered
   */
  public static void register(DialogType type, Function<DialogContext, UiHandle> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    if (registry.containsKey(type)) {
      throw new DialogCreationException("Dialog type '" + type + "' is already registered");
    }
    registry.put(type, creator);
  }

  /**
   * Registers a dialog type only if it is currently missing.
   *
   * <p>This is useful for weak/fallback backends that should not overwrite more specific dialog
   * implementations.
   *
   * @param type    The unique type of the dialog
   * @param creator Function that creates a dialog from a context
   */
  public static void registerIfAbsent(
    DialogType type, Function<DialogContext, UiHandle> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");

    if (registry.containsKey(type)) {
      return;
    }

    registry.put(type, creator);
  }

  /**
   * Replaces the dialog creator for the given type.
   *
   * <p>This is useful for progressive backend migration where an existing fallback should later be
   * replaced by a real backend-specific implementation.
   *
   * @param type The unique type of the dialog
   * @param creator Function that creates a dialog from a context
   */
  public static void replace(DialogType type, Function<DialogContext, UiHandle> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    registry.put(type, creator);
  }

  /**
   * Creates a dialog of the specified type from the given context.
   *
   * @param ctx The context containing all necessary data for dialog creation
   * @return The UiNodeHandle wrapping the created dialog
   * @throws DialogCreationException if the dialog type is not registered
   */
  public static UiHandle create(DialogContext ctx) {
    Objects.requireNonNull(ctx, "context");

    Function<DialogContext, UiHandle> creator = registry.get(ctx.dialogType());
    if (creator == null) {
      throw new DialogCreationException("Unknown dialog type: " + ctx);
    }

    return creator.apply(ctx);
  }

  /**
   * Displays a dialog by creating and associating a {@code UIComponent} with an entity.
   *
   * @param context The {@code DialogContext} containing metadata and state for the dialog. Must not be null.
   * @param willPause Determines if the game or application flow should pause when this dialog is shown.
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
   * @param context The {@code DialogContext} containing metadata and state for the dialog. Must not be null.
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
   * @param text       The message to be displayed in the dialog.
   * @param title      The title of the dialog.
   * @param onConfirm  A callback function executed when the user confirms the dialog by pressing "OK".
   * @param targetIds  Optional target entity IDs that the dialog may target or be associated with.
   * @return           The {@code UIComponent} representing the created dialog.
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

    ui.registerCallback(DialogContextKeys.ON_CONFIRM, _ -> UIUtils.closeDialog(ui, true, true));
    ui.onClose(_ -> onConfirm.execute());

    return ui;
  }

  /**
   * Displays a "Yes/No" dialog with a specified message and title, allowing the user to respond.
   * The dialog invokes the provided callback functions based on the user's response and is
   * associated with the specified target entities.
   *
   * @param text            The message to be displayed in the dialog.
   * @param title           The title of the dialog.
   * @param onYes           A callback function executed when the user selects "Yes".
   * @param onNo            A callback function executed when the user selects "No" or when the dialog
   *                        is closed without choosing "Yes".
   * @param targetEntityIds Optional target entity IDs that the dialog may target or be associated with.
   */
  public static void showYesNoDialog(
    String text, String title, IVoidFunction onYes, IVoidFunction onNo, int... targetEntityIds) {
    DialogContext ctx =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.YES_NO)
        .put(DialogContextKeys.TITLE, title)
        .put(DialogContextKeys.MESSAGE, text)
        .build();

    UIComponent ui = show(ctx, targetEntityIds);

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
