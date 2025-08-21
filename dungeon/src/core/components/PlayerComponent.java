package core.components;

import core.Component;

/**
 * Marker component for the player entity.
 *
 * <p>This component keeps track of the number of open dialogs in the game. It provides methods to
 * increment and decrement the dialog counter, as well as a method to check if any dialogs are
 * currently open.
 *
 * <p>This component is used to identify the player entity in the game. It contains information
 * about whether the player is the local hero and manages the count of open dialogs.
 *
 * @see Input.Keys
 * @see core.systems.PlayerSystem
 */
public final class PlayerComponent implements Component {

  private int openDialogs = 0;
  private final boolean isLocalHero;
  private final String playerName;

  /** Create a new PlayerComponent. */
  public PlayerComponent(boolean isLocalHero) {
    this(isLocalHero, "Player");
  }

  /** Create a new PlayerComponent. */
  public PlayerComponent(boolean isLocalHero, String playerName) {
    this.isLocalHero = isLocalHero;
    this.playerName = playerName;
  }

  /** Gets whether this player is the local hero. */
  public boolean isLocalHero() {
    return isLocalHero;
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

  /** Gets the name of the player. */
  public String playerName() {
    return playerName;
  }
}
