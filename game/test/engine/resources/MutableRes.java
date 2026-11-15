package engine.resources;

import java.util.Optional;

/**
 * Test helper: a resource handle whose value can be replaced without using the global registry.
 *
 * <p>Use it in system tests to simulate that a resource changes, for example a level change:
 *
 * <pre>{@code
 * MutableRes<ILevel> level = new MutableRes<>(firstLevel);
 * MySystem system = new MySystem(level);
 * system.execute();
 * level.set(secondLevel); // the system notices the change in its next execute()
 * }</pre>
 *
 * @param <T> type of the resource
 */
public final class MutableRes<T> implements Res<T> {

  private T value;
  private long version;

  /**
   * Creates a handle with the given initial value.
   *
   * @param initialValue the initial value; {@code null} for an absent resource
   */
  public MutableRes(final T initialValue) {
    this.value = initialValue;
  }

  /**
   * Replaces the value and increases the version, like an insert into the registry.
   *
   * @param newValue the new value; {@code null} to remove the resource
   */
  public void set(final T newValue) {
    this.value = newValue;
    this.version++;
  }

  @Override
  public Optional<T> get() {
    return Optional.ofNullable(value);
  }

  @Override
  public long version() {
    return version;
  }
}
