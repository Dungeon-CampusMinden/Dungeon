package contrib.hud.dialogs;

import core.ui.UiHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Central registry for creating dialogs from {@link DialogContext}s.
 *
 * <p>Backends may either register new dialog creators, register weak fallbacks, or replace existing
 * creators with more specific implementations.
 *
 * @see DialogContext
 * @see DialogType
 */
public final class DialogRegistry {

  private static final Map<DialogType, Function<DialogContext, UiHandle>> REGISTRY =
      new HashMap<>();

  private DialogRegistry() {}

  /**
   * Registers a custom dialog type with the registry.
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
    if (REGISTRY.containsKey(type)) {
      throw new DialogCreationException("Dialog type '" + type + "' is already registered");
    }
    REGISTRY.put(type, creator);
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
    REGISTRY.put(type, creator);
  }

  /**
   * Creates a dialog of the specified type from the given context.
   *
   * @param context The context containing all necessary data for dialog creation
   * @return The UiNodeHandle wrapping the created dialog
   * @throws DialogCreationException if the dialog type is not registered
   */
  public static UiHandle create(DialogContext context) {
    Objects.requireNonNull(context, "context");

    Function<DialogContext, UiHandle> creator = REGISTRY.get(context.dialogType());
    if (creator == null) {
      throw new DialogCreationException("Unknown dialog type: " + context);
    }

    return creator.apply(context);
  }
}
