package core.game;

import core.level.elements.ILevel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Registry for optional level-load extensions.
 *
 * <p>Core performs the level storage and player placement work. Feature modules can register here
 * to add their own level preparation without making core depend on those modules.
 */
public final class LevelLoadHooks {
  private static final List<BiConsumer<ILevel, Boolean>> LEVEL_PREPARED = new ArrayList<>();

  private LevelLoadHooks() {}

  /**
   * Registers a callback that runs after a level is prepared and before user level-load callbacks.
   *
   * @param callback callback receiving the level and whether this is its first load
   */
  public static void onLevelPrepared(BiConsumer<ILevel, Boolean> callback) {
    LEVEL_PREPARED.add(Objects.requireNonNull(callback, "callback must not be null"));
  }

  static void executeLevelPrepared(ILevel level, boolean firstLoad) {
    LEVEL_PREPARED.forEach(callback -> callback.accept(level, firstLoad));
  }
}
