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
 * @see Input.Keys
 * @see InputSystem
 */
public final class PlayerComponent implements Component {

  private int openDialogs = 0;

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
