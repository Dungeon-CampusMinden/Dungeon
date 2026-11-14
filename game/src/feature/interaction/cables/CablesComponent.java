package feature.interaction.cables;

import engine.Component;
import engine.Entity;
import java.util.function.Consumer;

/**
 * A cable puzzle on an entity, such as a fuse box: its cables and sockets, whether all cables are
 * connected and the reactions to the players' plugging.
 *
 * <p>Only the server holds this component. {@link Cables#show} opens the puzzle for a player and
 * hands the wiring the player's dialog sends back to this component, which checks it again.
 */
public class CablesComponent implements Component {

  private final CablePuzzle puzzle;
  private final boolean once;
  private boolean connected;
  private Consumer<Entity> onConnected = player -> {};
  private Consumer<Entity> onWrongPlug = player -> {};

  /**
   * Creates the component of a puzzle whose cables all hang loose.
   *
   * @param puzzle the cables and sockets
   * @param once whether the puzzle stays connected once solved; otherwise every opening starts with
   *     loose cables and the puzzle can be solved again
   */
  public CablesComponent(CablePuzzle puzzle, boolean once) {
    this.puzzle = puzzle;
    this.once = once;
  }

  /**
   * Returns the cables and sockets.
   *
   * @return the puzzle
   */
  public CablePuzzle puzzle() {
    return puzzle;
  }

  /**
   * Returns whether all cables are connected. A connected puzzle shows its cables plugged in and
   * ignores further wirings.
   *
   * @return true if the puzzle has been solved and stays solved
   */
  public boolean connected() {
    return connected;
  }

  /**
   * Sets whether all cables are connected, e.g. to restore a saved game.
   *
   * @param connected true if the puzzle has been solved
   */
  public void connected(boolean connected) {
    this.connected = connected;
  }

  /**
   * Sets the reaction to a correct wiring, called after the server checked it.
   *
   * @param onConnected receives the player that plugged in the last cable
   */
  public void onConnected(Consumer<Entity> onConnected) {
    this.onConnected = onConnected;
  }

  /**
   * Sets the reaction to a cable put into a socket it does not belong in.
   *
   * @param onWrongPlug receives the player that tried it
   */
  public void onWrongPlug(Consumer<Entity> onWrongPlug) {
    this.onWrongPlug = onWrongPlug;
  }

  /**
   * Checks the wiring a player's dialog sent and reacts to a correct one.
   *
   * @param wiring index of the socket of every cable
   * @param player the player that plugged in the last cable
   * @return true if the wiring connected the puzzle
   */
  boolean connect(int[] wiring, Entity player) {
    if (connected || !puzzle.connected(wiring)) return false;
    connected = once;
    onConnected.accept(player);
    return true;
  }

  /**
   * Reacts to a plug a player's dialog reported as wrong, if it really is.
   *
   * @param plug index of the cable and index of the socket
   * @param player the player that tried it
   */
  void plugWrong(int[] plug, Entity player) {
    if (puzzle.wrong(plug)) onWrongPlug.accept(player);
  }
}
