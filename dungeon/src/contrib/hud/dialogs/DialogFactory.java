package contrib.hud.dialogs;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
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
 * future extensions via the registry system.
 *
 * <p>Backends may either register new dialog creators, register weak fallbacks, or replace existing
 * creators with more specific implementations.
 *
 * @see DialogContext
 * @see DialogType
 */
public class DialogFactory {

  private static final Map<DialogType, Function<DialogContext, UiNodeHandle>> registry =
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
  public static void register(DialogType type, Function<DialogContext, UiNodeHandle> creator) {
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
   * @param type The unique type of the dialog
   * @param creator Function that creates a dialog from a context
   * @return {@code true} if the creator was inserted, {@code false} if a creator already existed
   */
  public static boolean registerIfAbsent(
    DialogType type, Function<DialogContext, UiNodeHandle> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");

    if (registry.containsKey(type)) {
      return false;
    }

    registry.put(type, creator);
    return true;
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
  public static void replace(DialogType type, Function<DialogContext, UiNodeHandle> creator) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(creator, "creator");
    registry.put(type, creator);
  }

  /**
   * Checks whether a dialog creator is already registered for the given type.
   *
   * @param type the dialog type to check
   * @return {@code true} if a creator is registered, otherwise {@code false}
   */
  public static boolean isRegistered(DialogType type) {
    Objects.requireNonNull(type, "type");
    return registry.containsKey(type);
  }

  /**
   * Creates a dialog of the specified type from the given context.
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

  public static UIComponent show(final DialogContext context, int... targetEntityIds) {
    return show(context, true, true, targetEntityIds);
  }

  public static UIComponent showOkDialog(
    String text, String title, IVoidFunction onConfirm, int... targetIds) {
    DialogContext ctx =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.OK)
        .put(DialogContextKeys.TITLE, title)
        .put(DialogContextKeys.MESSAGE, text)
        .build();

    UIComponent ui = show(ctx, targetIds);

    ui.registerCallback(DialogContextKeys.ON_CONFIRM, data -> UIUtils.closeDialog(ui, true, true));
    ui.onClose(uic -> onConfirm.execute());

    return ui;
  }

  public static UIComponent showYesNoDialog(
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
      data -> {
        onYes.execute();
        UIUtils.closeDialog(ui, true, false);
      });
    ui.registerCallback(DialogContextKeys.ON_NO, data -> UIUtils.closeDialog(ui, true, true));
    ui.onClose(uic -> onNo.execute());

    return ui;
  }
}
