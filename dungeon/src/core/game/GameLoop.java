package core.game;

import contrib.entities.deco.DecoFactory;
import contrib.utils.CheckPatternPainter;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.ui.StageHandle;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.*;
import core.game.gdx.GdxGameLoopHost;
import core.sound.player.ISoundPlayer;

/**
 * Facade for starting and interacting with the active game loop.
 *
 * <p>Concrete engine hosts (libGDX, LITIENGINE, ...) live in dedicated packages.
 */
public final class GameLoop {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameLoop.class);
  private static final GameLoopCore CORE = new GameLoopCore();

  private GameLoop() {}

  /**
   * Compatibility hook: old code (e.g. MultiplayerServer) expects this constant.
   *
   * <p>Engine-neutral logic: no libGDX types.
   */
  public static final IVoidFunction onLevelLoad =
    () -> {
      if (!PreRunConfiguration.isNetworkServer()) return; // no authority

      List<Entity> allPlayers = ECSManagement.allPlayers().toList();
      boolean firstLoad = !ECSManagement.levelStorageMap().containsKey(Game.currentLevel().get());
      allPlayers.forEach(ECSManagement::remove);

      // cleanup systems
      Map<Class<? extends System>, System> s = ECSManagement.systems();
      ECSManagement.removeAllSystems();

      ECSManagement.activeEntityStorage(
        ECSManagement.levelStorageMap()
          .computeIfAbsent(Game.currentLevel().orElse(null), k -> new HashSet<>()));

      // re-add systems
      s.values().forEach(ECSManagement::add);

      try {
        allPlayers.forEach(GameLoop::placeOnLevelStart);
      } catch (MissingComponentException e) {
        LOGGER.warn(e.getMessage());
      }

      ECSManagement.allEntities()
        .filter(Entity::isPersistent)
        .map(ECSManagement::remove)
        .forEach(ECSManagement::add);

      Game.currentLevel()
        .ifPresent(
          level ->
            level
              .decorations()
              .forEach(tuple -> Game.add(DecoFactory.createDeco(tuple.b(), tuple.a()))));

      if (firstLoad && Game.isCheckPatternEnabled()) {
        Game.currentLevel()
          .ifPresent(level -> CheckPatternPainter.paintCheckerPattern(level.layout()));
      }

      PreRunConfiguration.userOnLevelLoad().accept(firstLoad);
    };

  /**
   * Compatibility hook: used by LevelChangeEvent handler in the old GameLoop.
   *
   * <p>Engine-neutral logic: no libGDX types.
   */
  public static void placeOnLevelStart(final Entity entity) {
    ECSManagement.add(entity);
    entity
      .fetch(PositionComponent.class)
      .ifPresent(
        pc -> {
          Game.startTile()
            .ifPresentOrElse(
              pc::position, () -> LOGGER.warn("No start tile found for the current level"));
          pc.viewDirection(Direction.DOWN); // look down by default
        });

    // reset animations
    entity.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
  }

  /** Start the game loop using the current default host (currently: libGDX). */
  public static void run() {
    GdxGameLoopHost.run(CORE);
  }

  /** Current tick number from ECS. */
  public static int currentTick() {
    return ECSManagement.currentTick();
  }

  /** HUD stage handle (only present when a UI stage exists). */
  public static Optional<StageHandle> stage() {
    return GdxGameLoopHost.stage();
  }

  /** Sound player used by the active host. */
  public static ISoundPlayer soundPlayer() {
    return GdxGameLoopHost.soundPlayer();
  }
}
