package feature.prefabs;

import feature.prefabs.types.BookshelfImagePrefab;
import feature.prefabs.types.DoorKeypadPrefab;
import feature.prefabs.types.LevelHiderPrefab;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Hard-coded registry of prefab definitions available to level files and the editor. */
public final class PrefabRegistry {

  private static final Map<String, Prefab> PREFABS = new LinkedHashMap<>();

  static {
    register(new DoorKeypadPrefab());
    register(new BookshelfImagePrefab());
    register(new LevelHiderPrefab());
  }

  private PrefabRegistry() {}

  /**
   * Registers a prefab definition.
   *
   * @param prefab definition to register
   */
  public static void register(Prefab prefab) {
    Prefab previous = PREFABS.putIfAbsent(prefab.type(), prefab);
    if (previous != null) {
      throw new IllegalStateException("Duplicate prefab type ID: " + prefab.type());
    }
  }

  /**
   * Looks up a prefab definition.
   *
   * @param type stable type ID
   * @return matching definition
   */
  public static Optional<Prefab> find(String type) {
    return Optional.ofNullable(PREFABS.get(type));
  }

  /**
   * Resolves a prefab definition or fails with a level-facing message.
   *
   * @param type stable type ID
   * @return matching definition
   */
  public static Prefab require(String type) {
    return find(type)
        .orElseThrow(() -> new IllegalArgumentException("Unknown prefab type: '" + type + "'"));
  }

  /**
   * Returns definitions in their editor display order.
   *
   * @return registered prefab definitions
   */
  public static Collection<Prefab> all() {
    return Collections.unmodifiableCollection(PREFABS.values());
  }
}
