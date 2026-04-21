package core.game;

import contrib.entities.deco.DecoFactory;
import contrib.utils.CheckPatternPainter;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.game.loop.GameLoopHost;
import core.platform.Platform;
import core.ui.StageHandle;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.*;
import core.sound.player.ISoundPlayer;

/**
 * Represents the runtime configuration and management for the game,
 * including level loading, game loop execution, and system integration.
 *
 * <p>This final class provides static methods for managing essential
 * game runtime operations such as initializing levels, handling the game
 * loop, and accessing game services like the UI stage and sound player.
 */
public final class GameRuntime {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameRuntime.class);
  private static final GameLoop CORE = new GameLoop();

  private GameRuntime() {}

  /**
   * Callback function executed when a new level is loaded.
   *
   * <p>This callback performs essential level initialization tasks:
   * <ul>
   *   <li>Removes and re-adds all player entities to the new level
   *   <li>Re-initializes all systems for the new level
   *   <li>Places player entities at the level's start tile
   *   <li>Re-adds persistent entities to the new level
   *   <li>Spawns decorations from the level configuration
   *   <li>Paints a checker pattern (if enabled and this is the first load)
   * </ul>
   *
   * <p>This callback is only executed on the server (if network mode is active).
   */
  public static final IVoidFunction onLevelLoad =
    () -> {
      if (!PreRunConfiguration.isNetworkServer()) return; // no authority

      List<Entity> allPlayers = ECSManagement.allPlayers().toList();
      boolean firstLoad = Game.currentLevel()
        .map(level -> !ECSManagement.levelStorageMap().containsKey(level))
        .orElse(true);
      allPlayers.forEach(ECSManagement::remove);

      // cleanup systems
      Map<Class<? extends System>, System> s = ECSManagement.systems();
      ECSManagement.removeAllSystems();

      ECSManagement.activeEntityStorage(
        ECSManagement.levelStorageMap()
          .computeIfAbsent(Game.currentLevel().orElse(null), _ -> new HashSet<>()));

      // re-add systems
      s.values().forEach(ECSManagement::add);

      try {
        allPlayers.forEach(GameRuntime::placeOnLevelStart);
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
   * Places an entity on the level at the level's start tile.
   *
   * <p>This method adds the entity to the game and positions it at the current level's start tile.
   * It also sets the entity's initial view direction to DOWN. If the entity has a DrawComponent,
   * its animation state is reset.
   *
   * <p>This method is used during level loading and level transitions to properly initialize
   * player and other placed entities.
   *
   * @param entity the entity to place on the level start tile
   * @throws IllegalStateException if entity cannot be added
   * @see #onLevelLoad
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

  /**
   * Starts the game loop using the default arguments.
   *
   * <p>This method starts the game loop via the installed GameLoopHost with no additional arguments.
   * It is equivalent to calling {@link #run(String[])}.
   *
   * @throws IllegalStateException if no GameLoopHost has been installed via {@link Platform#loopHost(GameLoopHost)}
   * @see #run(String[])
   */
  public static void run() {
    run(new String[0]);
  }

  /**
   * Starts the game loop with the specified command-line arguments.
   *
   * <p>This method delegates to the installed GameLoopHost to start and run the main game loop.
   * The GameLoopHost manages the actual rendering and execution loop.
   *
   * @param args command-line arguments to pass to the GameLoopHost, or null (treated as an empty array)
   * @throws IllegalStateException if no GameLoopHost has been installed via {@link Platform#loopHost(GameLoopHost)}
   * @see Platform#loopHost(GameLoopHost)
   */
  public static void run(String[] args) {
    GameLoopHost host = Platform.loopHost();

    if (host == null) {
      throw new IllegalStateException(
        "No GameLoopHost installed. Set Platform.loopHost(...) before calling GameLoop.run().");
    }

    host.run(args == null ? new String[0] : args, CORE);
  }

  /**
   * Gets the current game tick number.
   *
   * <p>The tick counter increments with each frame update executed by the game loop. Ticks can be
   * used to track game progression and schedule time-dependent events.
   *
   * @return the current tick number
   * @see ECSManagement#currentTick()
   */
  public static int currentTick() {
    return ECSManagement.currentTick();
  }

  /**
   * Gets the game stage handle (UI container).
   *
   * <p>The stage handle provides access to the UI system and allows adding actors and managing
   * UI elements. This method returns an empty Optional if no GameLoopHost is installed.
   *
   * @return an Optional containing the stage handle, or empty if not available
   */
  public static Optional<StageHandle> stage() {
    GameLoopHost host = Platform.loopHost();
    return host == null ? Optional.empty() : host.stage();
  }

  /**
   * Gets the sound player for audio playback.
   *
   * <p>The sound player provides audio capabilities for the game. If no GameLoopHost is installed, it
   * returns a no-op sound player that safely handles all operations without producing sound.
   *
   * @return the ISoundPlayer instance from the GameLoopHost, or a no-op implementation if unavailable
   */
  public static ISoundPlayer soundPlayer() {
    GameLoopHost host = Platform.loopHost();
    return host == null ? new core.sound.player.NoSoundPlayer() : host.soundPlayer();
  }
}
