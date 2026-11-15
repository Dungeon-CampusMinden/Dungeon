package engine.resources;

import engine.level.elements.ILevel;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for global resources.
 *
 * <p>A resource is state that exists once in the game, for example the currently loaded level. Each
 * resource is stored under its type, so there is at most one value per type.
 *
 * <p><b>Reading:</b> Systems do not read resources directly. Instead, they request a {@link Res
 * handle}, usually in their constructor, and keep it in a field. The handle looks up the current
 * value on every {@link Res#get()} call, so it stays valid when the value is replaced. Request
 * handles only in constructors, never inside {@code execute()} or in helper methods; helpers get
 * the resolved value as a parameter.
 *
 * <p><b>Writing:</b> Each resource has exactly one owner that inserts, replaces and removes it. For
 * the level, this is the {@link engine.systems.LevelSystem}.
 *
 * <p><b>Reacting to changes:</b> Every insert and every removal increases the {@link
 * #version(Class) version} of the resource. Writing does not call any listeners, because that would
 * change the game state while other systems are still running. Instead, a system that wants to
 * react creates its own {@link Changes} tracker in its constructor and checks it in {@code
 * execute()}.
 *
 * <p><b>Core and feature resources:</b> Core resources of the engine have a typed accessor in this
 * class, like {@link #level()}. Resources defined in a feature or room must not be added here,
 * because the engine must not depend on them. Instead, the resource type offers its own accessor,
 * for example {@code public static Res<MyIndex> res() { return Resources.res(MyIndex.class); }}.
 * This keeps the key in one place. Readers must expect that such a resource is missing, for example
 * because its owning system is not registered.
 */
public final class Resources {

  private static final Map<Class<?>, Object> STORE = new ConcurrentHashMap<>();

  /** Versions only ever increase, also across {@link #clear()}, so trackers never miss a change. */
  private static final Map<Class<?>, Long> VERSIONS = new ConcurrentHashMap<>();

  private Resources() {}

  /**
   * Returns a handle to the currently loaded level.
   *
   * @return a handle resolving to the current level, or to an empty {@link Optional} if no level is
   *     loaded
   */
  public static Res<ILevel> level() {
    return res(ILevel.class);
  }

  /**
   * Returns a handle to the resource of the given type.
   *
   * <p>The handle can be requested before the resource is inserted. It resolves to the value that
   * is stored at the time of each {@link Res#get()} call.
   *
   * @param type the type the resource is stored under
   * @param <T> type of the resource
   * @return a handle resolving to the current value, or to an empty {@link Optional} if no value is
   *     stored
   */
  public static <T> Res<T> res(final Class<T> type) {
    return new RegistryRes<>(type);
  }

  /**
   * Returns the version of the resource of the given type.
   *
   * <p>The version starts at {@code 0} and increases with every insert and every removal. It never
   * decreases.
   *
   * @param type the type the resource is stored under
   * @return the current version of the resource
   */
  public static long version(final Class<?> type) {
    return VERSIONS.getOrDefault(type, 0L);
  }

  /**
   * Inserts or replaces the resource of the given type.
   *
   * <p>Only the owner of a resource should call this method. Passing {@code null} removes the
   * resource.
   *
   * @param type the type the resource is stored under
   * @param value the new value, or {@code null} to remove the resource
   * @param <T> type of the resource
   */
  public static <T> void insert(final Class<T> type, final T value) {
    if (value == null) {
      remove(type);
      return;
    }
    STORE.put(type, value);
    increaseVersion(type);
  }

  /**
   * Removes the resource of the given type.
   *
   * <p>Only the owner of a resource should call this method. Existing handles stay valid and
   * resolve to an empty {@link Optional} until a new value is inserted.
   *
   * @param type the type the resource is stored under
   */
  public static void remove(final Class<?> type) {
    if (STORE.remove(type) != null) {
      increaseVersion(type);
    }
  }

  /**
   * Removes all resources.
   *
   * <p>Intended for tests that need to reset the global state. Like {@link #remove(Class)}, this
   * increases the version of every removed resource.
   */
  public static void clear() {
    new ArrayList<>(STORE.keySet()).forEach(Resources::remove);
  }

  private static void increaseVersion(final Class<?> type) {
    VERSIONS.merge(type, 1L, Long::sum);
  }

  /**
   * Handle that reads its value and version from the registry.
   *
   * @param type the type the resource is stored under
   * @param <T> type of the resource
   */
  private record RegistryRes<T>(Class<T> type) implements Res<T> {

    @Override
    public Optional<T> get() {
      return Optional.ofNullable(type.cast(STORE.get(type)));
    }

    @Override
    public long version() {
      return Resources.version(type);
    }
  }
}
