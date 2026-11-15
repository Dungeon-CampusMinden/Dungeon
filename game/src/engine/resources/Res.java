package engine.resources;

import java.util.Optional;

/**
 * Handle to a global resource, for example the currently loaded level.
 *
 * <p>Systems keep a {@code Res} instead of the resource itself. The handle resolves the resource on
 * every call to {@link #get()}, so it does not become stale when the resource is replaced at
 * runtime, for example after a level change.
 *
 * <p>Handles for the game are provided by {@link Resources}. Tests can create a fixed handle with
 * {@link #of(Object)} or {@link #empty()} instead of setting global game state.
 *
 * <p>To react when the resource changes, create a {@link Changes} tracker from the handle.
 *
 * @param <T> type of the resource
 */
@FunctionalInterface
public interface Res<T> {

  /**
   * Resolves the current value of the resource.
   *
   * @return the current value, or an empty {@link Optional} if the resource is not available, for
   *     example because no level is loaded yet
   */
  Optional<T> get();

  /**
   * Returns the version of the resource.
   *
   * <p>The version increases every time the resource is inserted, replaced or removed. {@link
   * Changes} uses it to detect changes. Handles that are not backed by the {@link Resources}
   * registry, like {@link #of(Object)} and {@link #empty()}, never change and always return {@code
   * 0}.
   *
   * @return the current version of the resource
   */
  default long version() {
    return 0L;
  }

  /**
   * Creates a handle that always resolves to the given value.
   *
   * <p>Intended for tests that should run against a fixed or mocked resource.
   *
   * @param value the value the handle resolves to; {@code null} behaves like {@link #empty()}
   * @param <T> type of the resource
   * @return a handle that always resolves to {@code value}
   */
  static <T> Res<T> of(final T value) {
    return () -> Optional.ofNullable(value);
  }

  /**
   * Creates a handle for a resource that is not available.
   *
   * <p>Intended for tests that check how a system behaves without the resource, for example without
   * a loaded level.
   *
   * @param <T> type of the resource
   * @return a handle that always resolves to an empty {@link Optional}
   */
  static <T> Res<T> empty() {
    return Optional::empty;
  }
}
