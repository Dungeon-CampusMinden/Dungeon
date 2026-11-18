package feature.prefabs.types;

import feature.prefabs.PrefabInstance;
import java.util.List;

/** A prefab type whose instances react to game flags, e.g. a flag trigger. */
interface FlagReader {

  /**
   * Returns the flags an instance reacts to.
   *
   * @param instance normalized instance of the prefab type
   * @return names of the flags
   */
  List<String> readFlags(PrefabInstance instance);
}
