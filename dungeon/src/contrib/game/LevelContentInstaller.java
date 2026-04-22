package contrib.game;

import contrib.entities.deco.DecoFactory;
import contrib.utils.CheckPatternPainter;
import core.Game;
import core.game.LevelLoadHooks;

/** Installs decoration and check-pattern behavior into the core level-load lifecycle. */
public final class LevelContentInstaller {
  private static boolean installed;

  private LevelContentInstaller() {}

  /** Installs decoration spawning and optional check-pattern painting once. */
  public static void install() {
    if (installed) {
      return;
    }

    LevelLoadHooks.onLevelPrepared(
        (level, firstLoad) -> {
          level
              .decorations()
              .forEach(tuple -> Game.add(DecoFactory.createDeco(tuple.b(), tuple.a())));

          if (firstLoad && Game.isCheckPatternEnabled()) {
            CheckPatternPainter.paintCheckerPattern(level.layout());
          }
        });
    installed = true;
  }
}
