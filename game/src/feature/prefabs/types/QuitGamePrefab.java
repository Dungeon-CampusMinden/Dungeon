package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.level.elements.ILevel;
import engine.network.NetworkUtils;
import engine.network.messages.s2c.GameOverEvent;
import engine.time.PlayClock;
import feature.prefabs.ActionContext;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.save.GameSaves;
import feature.systems.LevelEditorSystem;
import java.util.List;
import java.util.Optional;

/**
 * Server-side logic prefab that ends the game, for all players or only for the triggering one.
 *
 * <p>The result decides how the play time ends: a solved game also deletes the save, a failed one
 * keeps it, so the players can try again from there. When the last player quits alone, the game
 * ends for good. While editing levels, quitting is only shown as a message.
 */
public final class QuitGamePrefab extends Prefab {

  private static final String TYPE = "quit-game";
  private static final String DISPLAY_NAME = "Quit Game";
  private static final String FOR_EVERYONE = PrefabAudience.EVERYONE;
  private static final String FOR_PLAYER = PrefabAudience.TRIGGERING_PLAYER;
  private static final String RESULT_SOLVED = "Solved";
  private static final String RESULT_FAILED = "Failed";
  private static final String REASON = "Game finished";

  /** Who quits the game. */
  public static final PrefabProperty<String> QUIT_FOR =
      PrefabProperty.selection(
          "quit-for", "Quit For", FOR_EVERYONE, List.of(FOR_EVERYONE, FOR_PLAYER));

  /** Whether the players solved the game. */
  public static final PrefabProperty<String> RESULT =
      PrefabProperty.selection(
          "result", "Result", RESULT_SOLVED, List.of(RESULT_SOLVED, RESULT_FAILED));

  /** Quits the game. */
  public static final PrefabAction QUIT =
      PrefabAudience.action("quit", "Quit", QUIT_FOR, QuitGamePrefab::quit);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(QUIT_FOR, RESULT);

  /** Creates the quit-game definition. */
  public QuitGamePrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored quit-game instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public QuitGamePrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(QUIT);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(value(instance, QUIT_FOR) + ", " + value(instance, RESULT).toLowerCase());
  }

  private static void quit(ActionContext context) {
    boolean solved = context.value(RESULT).equals(RESULT_SOLVED);
    if (LevelEditing.active()) {
      LevelEditorSystem.showFeedback(
          "Quit Game (" + (solved ? "solved" : "failed") + ") is skipped while editing",
          Color.YELLOW);
      return;
    }
    Optional<Entity> player = context.player();
    boolean alone = context.value(QUIT_FOR).equals(FOR_PLAYER) && player.isPresent();
    if (alone && Game.allPlayers().anyMatch(other -> other != player.get())) {
      // The others play on, the server removes the player once the client is gone.
      NetworkUtils.entityIdsToClientIds(new int[] {player.get().id()})
          .forEach(client -> Game.network().send(client, new GameOverEvent(REASON), true));
      return;
    }
    end(solved);
  }

  private static void end(boolean solved) {
    Game.playClock().end(solved ? PlayClock.Outcome.SUCCESS : PlayClock.Outcome.FAILURE);
    // A solved game cannot be continued, a failed one restarts from its last save.
    if (solved) GameSaves.delete();
    if (PreRunConfiguration.multiplayerEnabled()) {
      Game.network().broadcast(new GameOverEvent(REASON), true);
    }
    Game.complete();
  }
}
