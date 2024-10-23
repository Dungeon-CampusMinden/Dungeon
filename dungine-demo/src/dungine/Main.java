package dungine;

import dungine.state.StateMainMenu;
import dungine.state.StateTransition;

/**
 * Main class of the Dungine demo application. It contains the main method that starts the Dungine
 * game engine and initializes the game state.
 */
public class Main {

  /**
   * Main method of the Dungine demo application. It starts the Dungine game engine and initializes
   * the game state.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    Dungine dungine = new Dungine(false);
    dungine.setState(new StateMainMenu(dungine));
    dungine.setStateTransition(new StateTransition(dungine));
    dungine.start();
  }
}
