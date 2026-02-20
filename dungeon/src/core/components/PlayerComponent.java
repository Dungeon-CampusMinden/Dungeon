package core.components;

import com.badlogic.gdx.Input;
import core.Component;
import core.systems.InputSystem;

/**
 * Marks an entity as playable by the player.
 *
 * <p>This component keeps track of the number of open dialogs in the game. It provides methods to
 * increment and decrement the dialog counter, as well as a method to check if any dialogs are
 * currently open.
 *
 * <p>This component is used to identify the player entity in the game. It contains information
 * about whether the player is the local player and manages the count of open dialogs.
 *
 * @see Input.Keys
 * @see InputSystem
 */
public final class PlayerComponent implements Component {

  private int openDialogs = 0;
  private final boolean isLocalPlayer;
  private final String playerName;

  /**
   * Create a new PlayerComponent.
   *
   * <p>The player name defaults to a local player named "Player".
   */
  public PlayerComponent() {
    this(true);
  }

  /**
   * Create a new PlayerComponent.
   *
   * <p>The player name defaults to "Player".
   *
   * @param isLocalPlayer whether this player is the local hero
   */
  public PlayerComponent(boolean isLocalPlayer) {
    this(isLocalPlayer, "Player");
  }

  /**
   * Create a new PlayerComponent.
   *
   * @param isLocalPlayer whether this player is the local hero
   * @param playerName the name of the player
   */
  public PlayerComponent(boolean isLocalPlayer, String playerName) {
    this.isLocalPlayer = isLocalPlayer;
    this.playerName = playerName;
  }

  /**
   * Gets whether this player is the local hero.
   *
   * @return true if this player is the local hero, otherwise false
   */
  public boolean isLocal() {
    return isLocalPlayer;
  }

  /** Increases the dialogue counter by 1. */
  public void incrementOpenDialogs() {
    openDialogs++;
  }

  /** Decreases the dialogue counter by 1. */
  public void decrementOpenDialogs() {
    openDialogs--;
  }

  /**
   * Indicates whether dialogs are currently open.
   *
   * @return true if dialogs are currently open, otherwise false
   */
  public boolean openDialogs() {
    return openDialogs > 0;
  }

  /**
   * Gets the name of the player.
   *
   * @return the name of the player
   */
  public String playerName() {
    return playerName;
  }
}
