package feature.prefabs.types;

import com.badlogic.gdx.Gdx;
import engine.Game;
import engine.utils.components.draw.TextureMap;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProblem;
import feature.prefabs.PrefabProperty;
import java.util.List;
import java.util.Optional;

/** Checks of the assets named by prefab properties, e.g. images, for the problems of prefabs. */
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
  static Optional<PrefabProblem> missingImage(
      PrefabInstance instance, PrefabProperty<String> property) {
    String path = property.get(instance);
    if (Gdx.files == null || imageExists(path)) return Optional.empty();
    return Optional.of(
        PrefabProblem.error(
            instance.name(),
            "There is no image '" + path + "' for '" + property.displayName() + "'"));
  }

  private static boolean imageExists(String path) {
    try {
      String localized = Game.localization().asset(path);
      return Gdx.files.internal(localized).exists() || TextureMap.instance().containsKey(localized);
    } catch (RuntimeException unavailable) {
      // Without the localization or the textures, nothing can be said about the image.
      return true;
    }
  }

  /**
   * Checks whether the sound named by a property of an instance exists.
   *
   * @param instance normalized instance
   * @param property sound name property of its prefab type
   * @return an error if the sound is missing; empty if it exists or can not be checked
   */
  static Optional<PrefabProblem> missingSound(
      PrefabInstance instance, PrefabProperty<String> property) {
    List<String> sounds = SoundEffectPrefab.soundNames();
    String sound = property.get(instance);
    if (sounds.isEmpty() || sounds.contains(sound)) return Optional.empty();
    return Optional.of(
        PrefabProblem.error(
            instance.name(),
            "There is no sound '" + sound + "' for '" + property.displayName() + "'"));
  }
}
