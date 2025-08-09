package core.components;

import core.Component;

/**
 * Marker component for the player entity.
 *
 * <p>This component is used to identify the player entity in the game. It contains information
 * about whether the player is the local hero and manages the count of open dialogs.
 *
 * @see PlayerComponent#isLocalHero()
 * @see PlayerComponent#incrementOpenDialogs()
 * @see PlayerComponent#decrementOpenDialogs()
 * @see InputComponent InputComponent, for handling player input
 */
public final class PlayerComponent implements Component {

  private int openDialogs = 0;
  private final boolean isLocalHero;

  /** Create a new PlayerComponent. */
  public PlayerComponent(boolean isLocalHero) {
    this.isLocalHero = isLocalHero;
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
}
