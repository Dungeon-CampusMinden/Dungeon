package feature.prefabs.types;

import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRegistry;

/** Shared helpers for prefabs that temporarily control a target door. */
final class DoorPrefabSupport {

  private static final String DOOR_KEYPAD_TYPE = "door-keypad";
  private static final String DOOR_LEVER_TYPE = "door-lever";
  private static final String DOOR_POSITION_PROPERTY = "doorPosition";

  private DoorPrefabSupport() {}

  /**
   * Checks whether another authored keypad or lever instance also targets the same door.
   *
   * @param level owning level
   * @param position target door position
   * @param removing instance being removed
   * @return whether another registered door-control prefab targets this position
   */
  static boolean hasOtherTarget(ILevel level, Point position, PrefabInstance removing) {
    for (PrefabInstance candidate : level.prefabs()) {
      if (candidate.name().equals(removing.name()) && candidate.type().equals(removing.type())) {
        continue;
      }
      if (!candidate.type().equals(DOOR_KEYPAD_TYPE)
          && !candidate.type().equals(DOOR_LEVER_TYPE)) {
        continue;
      }
      try {
        Prefab prefab = PrefabRegistry.require(candidate.type());
        PrefabInstance normalized = prefab.normalize(candidate);
        PrefabProperty<?> property =
            prefab.properties().stream()
                .filter(descriptor -> descriptor.key().equals(DOOR_POSITION_PROPERTY))
                .findFirst()
                .orElse(null);
        if (property != null && position.equals(property.get(normalized))) return true;
      } catch (IllegalArgumentException ignored) {
        // Ignore invalid editor-time records; they cannot reliably claim a door target.
      }
    }
    return false;
  }
}
