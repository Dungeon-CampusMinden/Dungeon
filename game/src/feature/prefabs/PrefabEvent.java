package feature.prefabs;

import java.util.Objects;

/**
 * Identifies a runtime event a prefab type can raise for its instances, such as a dialog having
 * been read.
 *
 * <p>Listeners are registered per instance through the bound prefab view and are kept while the
 * instance is reset or updated. They are dropped when the instance is removed or the level is
 * reloaded.
 *
 * @param <T> payload passed to listeners
 */
public final class PrefabEvent<T> {

  private final String name;

  /**
   * Creates a new event identity.
   *
   * @param name descriptive name used for debugging
   */
  public PrefabEvent(String name) {
    this.name = Objects.requireNonNull(name, "name");
  }

  @Override
  public String toString() {
    return name;
  }
}
