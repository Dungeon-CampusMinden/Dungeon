package feature.prefabs.types;

import com.badlogic.gdx.Gdx;
import engine.Game;
import engine.language.Localization;
import engine.level.elements.ILevel;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProblem;
import feature.prefabs.PrefabProperty;
import java.util.List;
import java.util.function.Predicate;

/** Shared checks for the problems of prefabs, e.g. of the assets named by their properties. */
final class PrefabAssets {

  private PrefabAssets() {}

  /**
   * Checks whether the image named by a property of an instance exists, for its language or as it
   * is named.
   *
   * @param instance normalized instance
   * @param property image path property of its prefab type
   * @return an error if the image is missing; empty if it exists or can not be checked
   */
  static List<PrefabProblem> missingImage(
      PrefabInstance instance, PrefabProperty<String> property) {
    Localization localization = Game.localization();
    return missing(
        instance,
        property,
        "image",
        path -> Gdx.files == null || localization.assetExists(localization.asset(path)));
  }

  /**
   * Checks whether the sound named by a property of an instance exists.
   *
   * @param instance normalized instance
   * @param property sound name property of its prefab type
   * @return an error if the sound is missing; empty if it exists or can not be checked
   */
  static List<PrefabProblem> missingSound(
      PrefabInstance instance, PrefabProperty<String> property) {
    List<String> sounds = SoundEffectPrefab.soundNames();
    return missing(
        instance, property, "sound", sound -> sounds.isEmpty() || sounds.contains(sound));
  }

  /**
   * Lists the other active instances of the type of an instance, e.g. to report a clash.
   *
   * @param level level of the instance
   * @param instance instance to find the others of
   * @param matching further condition on the other instances
   * @return their names in single quotes separated by commas, empty if there are none
   */
  static String otherActive(
      ILevel level, PrefabInstance instance, Predicate<PrefabInstance> matching) {
    return String.join(
        ", ",
        level.prefabs().stream()
            .filter(other -> other.active() && other.type().equals(instance.type()))
            .filter(other -> !other.name().equals(instance.name()) && matching.test(other))
            .map(other -> "'" + other.name() + "'")
            .toList());
  }

  /**
   * Checks whether the thing named by a property of an instance exists.
   *
   * @param instance normalized instance
   * @param property property of its prefab type naming the thing
   * @param kind what the property names, e.g. {@code "image"}
   * @param exists whether a name can be used
   * @return an error if the named thing is missing, otherwise empty
   */
  static List<PrefabProblem> missing(
      PrefabInstance instance,
      PrefabProperty<String> property,
      String kind,
      Predicate<String> exists) {
    String name = property.get(instance);
    if (exists.test(name)) return List.of();
    return List.of(
        PrefabProblem.error(
            instance.name(),
            "There is no " + kind + " '" + name + "' for '" + property.displayName() + "'"));
  }
}
