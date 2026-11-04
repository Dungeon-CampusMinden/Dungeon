package engine.components;

import engine.Component;

/**
 * Marks an entity as playable by the player.
 *
 * <p>This component identifies player entities and stores whether the player is local together with
 * its display name.
 */
public final class PlayerComponent implements Component {

  private final boolean isLocalPlayer;
  private final String playerName;
  private boolean controlsLocked;

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

  /**
   * Whether the server currently locks the controls of this player, e.g. during a cutscene. The
   * lock is server state and reaches clients through snapshots.
   *
   * @return true while gameplay input of this player has no effect
   */
  public boolean controlsLocked() {
    return controlsLocked;
  }

  /**
   * Stores the lock state. Game logic uses {@code HeroController.controlsLocked}, which also ends
   * the input in progress; this setter alone is for mirroring the server on clients.
   *
   * @param locked true if the controls are locked
   */
  public void controlsLocked(boolean locked) {
    controlsLocked = locked;
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
